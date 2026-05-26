/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.image.image.equation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.parsington.Variable;
import org.scijava.parsington.eval.DefaultTreeEvaluator;

/**
 * A Parsington {@code TreeEvaluator} tailored for evaluating equations over
 * pixel positions.
 * <p>
 * Beyond the standard math operators, this evaluator exposes:
 * <ul>
 * <li>{@code p} — the position array; access dimensions via {@code p[0]},
 * {@code p[1]}, …</li>
 * <li>{@code x}, {@code y}, {@code z}, {@code t} — shorthand for the first
 * four dimensions, where applicable.</li>
 * <li>{@code pi}, {@code PI}, {@code e}, {@code E} — math constants.</li>
 * <li>All {@code public static} numeric methods of {@link Math} (e.g.
 * {@code sin}, {@code cos}, {@code exp}, {@code sqrt}, {@code pow},
 * {@code atan2}, {@code min}, {@code max}, {@code floor}, …).</li>
 * <li>{@code random()} — deterministic pseudo-random draw in {@code [0, 1)},
 * seeded by both an evaluator-wide seed and the current position. The same
 * position always yields the same value, regardless of iteration order.</li>
 * </ul>
 *
 * @author Curtis Rueden
 */
public class EquationEvaluator extends DefaultTreeEvaluator {

	private static final Map<String, List<Method>> MATH_METHODS = indexMath();

	/** Per-axis shorthand names, in order. */
	private static final String[] AXIS_NAMES = { "x", "y", "z", "t" };

	private final long seed;

	/** Current pixel position; updated by the driver loop before each evaluate. */
	private long[] pos = new long[0];

	public EquationEvaluator(final long seed) {
		this.seed = seed;
		setStrict(false);
	}

	/** Updates the position for the next evaluation. */
	public void position(final long[] newPos) {
		this.pos = newPos;
	}

	// -- Evaluator overrides --

	@Override
	public Object get(final String name) {
		switch (name) {
			case "p":
				return pos;
			case "pi":
			case "PI":
				return Math.PI;
			case "e":
			case "E":
				return Math.E;
			default:
		}
		for (int d = 0; d < AXIS_NAMES.length && d < pos.length; d++) {
			if (AXIS_NAMES[d].equals(name)) return pos[d];
		}
		return super.get(name);
	}

	@Override
	public Object brackets(final Object... args) {
		// Wrap bracket indices so function() can distinguish p[i] from cos(x).
		return new BracketIndex(args);
	}

	@Override
	public Object parens(final Object... args) {
		// Single-arg parens unwrap; multi-arg propagate as an array so function()
		// can pick the right Math overload.
		if (args.length == 1) return args[0];
		return args;
	}

	@Override
	public Object function(final Object fn, final Object args) {
		final String name = functionName(fn);

		// Array indexing: p[i], or any variable holding an array.
		if (args instanceof BracketIndex) {
			final Object[] idxs = ((BracketIndex) args).indices;
			final Object container = (fn instanceof Variable) ? get(name) : fn;
			return indexInto(container, idxs);
		}

		// Special-case deterministic random().
		if ("random".equals(name)) return deterministicRandom();

		// Otherwise, dispatch to java.lang.Math.
		final Object[] rawArgs = (args instanceof Object[]) ? (Object[]) args
			: new Object[] { args };
		final Object[] callArgs = new Object[rawArgs.length];
		for (int i = 0; i < rawArgs.length; i++)
			callArgs[i] = value(rawArgs[i]);
		final Object result = invokeMath(name, callArgs);
		if (result != null) return result;

		// Fallback to default behavior (will likely fail loudly).
		return super.function(fn, args);
	}

	// -- Helpers --

	private static String functionName(final Object fn) {
		if (fn instanceof Variable) return ((Variable) fn).getToken();
		return String.valueOf(fn);
	}

	private static Object indexInto(final Object container, final Object[] idxs) {
		if (idxs.length != 1) {
			throw new IllegalArgumentException(
				"Only 1-D indexing is supported; got " + idxs.length + " indices");
		}
		final int i = ((Number) idxs[0]).intValue();
		if (container instanceof long[]) return ((long[]) container)[i];
		if (container instanceof double[]) return ((double[]) container)[i];
		if (container instanceof int[]) return (long) ((int[]) container)[i];
		if (container instanceof Object[]) return ((Object[]) container)[i];
		if (container instanceof List) return ((List<?>) container).get(i);
		throw new IllegalArgumentException("Cannot index into " + (container == null
			? "null" : container.getClass().getName()));
	}

	private double deterministicRandom() {
		long h = seed;
		for (int i = 0; i < pos.length; i++) {
			h = splitMix64(h ^ splitMix64(pos[i] + 0x9E3779B97F4A7C15L * (i + 1)));
		}
		// Upper 53 bits → [0, 1)
		return (h >>> 11) * 0x1.0p-53;
	}

	private static long splitMix64(long z) {
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	private static Object invokeMath(final String name, final Object[] args) {
		final List<Method> candidates = MATH_METHODS.get(name);
		if (candidates == null) return null;
		// Prefer the (double, double, ...) overload for floating-point math.
		Method best = null;
		for (final Method m : candidates) {
			if (m.getParameterCount() != args.length) continue;
			if (best == null || prefersDouble(m, best)) best = m;
		}
		if (best == null) return null;
		final Class<?>[] paramTypes = best.getParameterTypes();
		final Object[] coerced = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			coerced[i] = coerce(args[i], paramTypes[i]);
		}
		try {
			return best.invoke(null, coerced);
		}
		catch (final ReflectiveOperationException exc) {
			throw new IllegalStateException("Failed to invoke Math." + name, exc);
		}
	}

	private static boolean prefersDouble(final Method candidate,
		final Method incumbent)
	{
		final boolean candAllDouble = allDoubleParams(candidate);
		final boolean incAllDouble = allDoubleParams(incumbent);
		return candAllDouble && !incAllDouble;
	}

	private static boolean allDoubleParams(final Method m) {
		for (final Class<?> p : m.getParameterTypes()) {
			if (p != double.class) return false;
		}
		return true;
	}

	private static Object coerce(final Object value, final Class<?> target) {
		if (target == double.class) return ((Number) value).doubleValue();
		if (target == float.class) return ((Number) value).floatValue();
		if (target == long.class) return ((Number) value).longValue();
		if (target == int.class) return ((Number) value).intValue();
		return value;
	}

	private static Map<String, List<Method>> indexMath() {
		final Map<String, List<Method>> map = new HashMap<>();
		for (final Method m : Math.class.getMethods()) {
			final int mods = m.getModifiers();
			if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods)) continue;
			if ("random".equals(m.getName())) continue; // handled deterministically
			if (!Number.class.isAssignableFrom(boxed(m.getReturnType()))) continue;
			boolean numericArgs = true;
			for (final Class<?> p : m.getParameterTypes()) {
				if (!Number.class.isAssignableFrom(boxed(p))) {
					numericArgs = false;
					break;
				}
			}
			if (!numericArgs) continue;
			map.computeIfAbsent(m.getName(), k -> new ArrayList<>()).add(m);
		}
		return map;
	}

	private static Class<?> boxed(final Class<?> c) {
		if (c == double.class) return Double.class;
		if (c == float.class) return Float.class;
		if (c == long.class) return Long.class;
		if (c == int.class) return Integer.class;
		if (c == short.class) return Short.class;
		if (c == byte.class) return Byte.class;
		return c;
	}

	private static final class BracketIndex {

		final Object[] indices;

		BracketIndex(final Object[] indices) {
			this.indices = Arrays.copyOf(indices, indices.length);
		}
	}
}
