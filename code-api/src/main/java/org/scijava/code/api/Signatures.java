/*-
 * #%L
 * Core API for SciJava code intelligence features.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.code.api;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.scijava.code.api.SignatureHelp.Fit;
import org.scijava.code.api.SignatureHelp.Signature;

/**
 * Helpers for rating signatures against arguments, for completers whose
 * types are Java classes (e.g. Jython and Groovy, calling Java methods). See
 * {@link CodeCompleter#signatureHelp}.
 *
 * @author Gabriel Selzer
 */
public final class Signatures {

	private static final Map<String, Class<?>> PRIMITIVES = new HashMap<>();
	static {
		for (final Class<?> c : new Class<?>[] { boolean.class, byte.class,
			char.class, short.class, int.class, long.class, float.class,
			double.class, void.class })
		{
			PRIMITIVES.put(c.getName(), c);
		}
	}

	private Signatures() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Rates passing a value of type {@code argType} to a parameter of type
	 * {@code paramType}, with conversions as scripting languages calling Java
	 * typically make them:
	 * <ul>
	 * <li>{@link Fit#MATCH}: the same type, an assignable one (with boxing), or
	 * the same kind of number (integral to integral, floating-point to
	 * floating-point).</li>
	 * <li>{@link Fit#CONVERSION}: an integral number to a floating-point
	 * parameter.</li>
	 * <li>{@link Fit#MISMATCH}: anything else.</li>
	 * <li>{@link Fit#UNKNOWN}: either type is unknown.</li>
	 * </ul>
	 *
	 * @param argType E.g. {@code int}, {@code java.lang.String} or
	 *          {@code long[]}; or null if unknown.
	 * @param paramType Likewise.
	 * @param loader Loads classes by name.
	 */
	public static Fit fit(final String argType, final String paramType,
		final ClassLoader loader)
	{
		if (argType == null || paramType == null) return Fit.UNKNOWN;
		if (argType.equals(paramType)) return Fit.MATCH;
		final Class<?> a = loadType(argType, loader);
		final Class<?> p = loadType(paramType, loader);
		if (a == null || p == null) return Fit.UNKNOWN;
		final Class<?> ba = box(a), bp = box(p);
		if (bp.isAssignableFrom(ba)) return Fit.MATCH;
		if (isIntegral(ba)) {
			if (isIntegral(bp)) return Fit.MATCH;
			if (isFloating(bp)) return Fit.CONVERSION;
		}
		if (isFloating(ba) && isFloating(bp)) return Fit.MATCH;
		return Fit.MISMATCH;
	}

	/**
	 * Rates a callable against the types of the arguments typed so far: as
	 * well as its worst-fitting argument; {@link Fit#MISMATCH} if there are
	 * more arguments than parameters; {@link Fit#UNKNOWN} if no argument's type
	 * is known.
	 *
	 * @param callable The signature to rate.
	 * @param argTypes Types of the arguments, null where unknown.
	 * @param fit Rates an argument type against a parameter type, e.g.
	 *          {@link #fit(String, String, ClassLoader)}.
	 */
	public static Fit rate(final Completion callable, final List<String> argTypes,
		final BiFunction<String, String, Fit> fit)
	{
		final List<Completion.Parameter> params = callable.parameters();
		Fit worst = null;
		for (int i = 0; i < argTypes.size(); i++) {
			if (argTypes.get(i) == null) continue;
			if (i >= params.size()) return Fit.MISMATCH;
			final Fit f = fit.apply(argTypes.get(i), params.get(i).type());
			if (f == Fit.UNKNOWN) continue;
			if (worst == null || f.ordinal() > worst.ordinal()) worst = f;
		}
		return worst == null ? Fit.UNKNOWN : worst;
	}

	/**
	 * Rates each callable against the arguments, and orders them for display:
	 * matches first, then those needing conversions, unrated ones, and
	 * mismatches last; otherwise keeping their order.
	 */
	public static List<Signature> rateAll(final List<Completion> callables,
		final List<String> argTypes, final BiFunction<String, String, Fit> fit)
	{
		final List<Signature> out = new ArrayList<>();
		for (final Completion c : callables) {
			out.add(new Signature(c, rate(c, argTypes, fit)));
		}
		out.sort(Comparator.comparingInt(s -> rank(s.fit())));
		return out;
	}

	/**
	 * Splits the arguments of the call whose opening parenthesis is at
	 * {@code open} into their source text, up to {@code caret}: the last one
	 * is the argument the caret is in. Understands nested brackets and quoted
	 * strings (with {@code '} and {@code "}), as most scripting languages
	 * write them.
	 *
	 * @return The arguments, or null if the call is closed before the caret.
	 */
	public static List<String> arguments(final String text, final int open,
		final int caret)
	{
		final List<String> args = new ArrayList<>();
		int start = open + 1, depth = 0;
		char quote = 0;
		for (int i = start; i < caret; i++) {
			final char c = text.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
				continue;
			}
			if (c == '"' || c == '\'') quote = c;
			else if (c == '(' || c == '[' || c == '{') depth++;
			else if (c == ')' || c == ']' || c == '}') {
				if (depth == 0) return null;
				depth--;
			}
			else if (c == ',' && depth == 0) {
				args.add(text.substring(start, i));
				start = i + 1;
			}
		}
		args.add(text.substring(start, caret));
		return args;
	}

	/**
	 * Finds the opening parenthesis of the call around the caret: the nearest
	 * unclosed {@code (} before it, on the same line; or -1 if none.
	 */
	public static int callStart(final String text, final int caret) {
		int depth = 0;
		for (int i = Math.min(caret, text.length()) - 1; i >= 0; i--) {
			final char c = text.charAt(i);
			if (c == '\n') return -1;
			if (c == ')' || c == ']' || c == '}') depth++;
			else if (c == '(' || c == '[' || c == '{') {
				if (depth > 0) depth--;
				else return c == '(' ? i : -1; // NB: Not in a call, but a list.
			}
		}
		return -1;
	}

	/** Loads a type such as {@code int}, {@code long[]} or {@code a.b.C}. */
	public static Class<?> loadType(final String name, final ClassLoader loader) {
		if (name.endsWith("[]")) {
			final Class<?> component = loadType(name.substring(0, name.length() -
				2), loader);
			return component == null ? null : Array.newInstance(component, 0)
				.getClass();
		}
		final Class<?> primitive = PRIMITIVES.get(name);
		if (primitive != null) return primitive;
		try {
			return Class.forName(name, false, loader);
		}
		catch (final ClassNotFoundException | LinkageError exc) {
			return null;
		}
	}

	// -- Helper methods --

	private static int rank(final Fit fit) {
		switch (fit) {
			case MATCH:
				return 0;
			case CONVERSION:
				return 1;
			case UNKNOWN:
				return 2;
			default:
				return 3;
		}
	}

	private static Class<?> box(final Class<?> c) {
		if (!c.isPrimitive()) return c;
		if (c == int.class) return Integer.class;
		if (c == long.class) return Long.class;
		if (c == double.class) return Double.class;
		if (c == float.class) return Float.class;
		if (c == boolean.class) return Boolean.class;
		if (c == char.class) return Character.class;
		if (c == short.class) return Short.class;
		if (c == byte.class) return Byte.class;
		return Void.class;
	}

	private static boolean isIntegral(final Class<?> c) {
		return c == Integer.class || c == Long.class || c == Short.class ||
			c == Byte.class || c == BigInteger.class;
	}

	private static boolean isFloating(final Class<?> c) {
		return c == Double.class || c == Float.class || c == BigDecimal.class;
	}
}
