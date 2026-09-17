/*
 * #%L
 * Toolkit-agnostic contracts for widgets and input harvesting.
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

package org.scijava.ui3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import org.scijava.execute.ParameterMember;
import org.scijava.harvest.ParameterNode;
import org.scijava.struct.MemberInstance;

/**
 * What every widget needs to read off a parameter: its bounds, its style
 * hints, whether it is a number at all.
 *
 * @author Curtis Rueden
 */
public final class Widgets {

	private Widgets() {
		// prevent instantiation of utility class
	}

	/** Gets the raw type of the parameter a node edits. */
	public static Class<?> type(final ParameterNode node) {
		final Optional<MemberInstance<?>> member = node.member();
		if (member.isEmpty()) return null;
		return org.scijava.common3.Types.raw(member.get().member().type());
	}

	/** Gets whether the node edits a number. */
	public static boolean isNumber(final ParameterNode node) {
		final Class<?> type = type(node);
		return type != null && (Number.class.isAssignableFrom(box(type)));
	}

	/** Gets whether the node edits a true/false value. */
	public static boolean isBoolean(final ParameterNode node) {
		final Class<?> type = type(node);
		return type == boolean.class || type == Boolean.class;
	}

	/** Gets whether the node edits text. */
	public static boolean isText(final ParameterNode node) {
		final Class<?> type = type(node);
		return type == String.class || type == char.class || type ==
			Character.class;
	}

	/** Gets one piece of a parameter's metadata. */
	public static Optional<String> attr(final ParameterNode node,
		final String key)
	{
		return node.member().map(MemberInstance::member) //
			.filter(ParameterMember.class::isInstance) //
			.flatMap(m -> ((ParameterMember<?>) m).attr(key));
	}

	/**
	 * Gets whether the parameter carries the given style hint.
	 *
	 * @see org.scijava.execute.Parameter#style()
	 */
	public static boolean isStyle(final ParameterNode node, final String hint) {
		return attr(node, ParameterMember.STYLE).map(style -> {
			for (final String part : style.split(","))
				if (part.trim().equalsIgnoreCase(hint)) return true;
			return false;
		}).orElse(false);
	}

	/**
	 * Gets the value of a style hint written as {@code name:value}, such as the
	 * {@code format} of a number widget.
	 */
	public static Optional<String> styleValue(final ParameterNode node,
		final String name)
	{
		return attr(node, ParameterMember.STYLE).flatMap(style -> {
			for (final String part : style.split(",")) {
				final int colon = part.indexOf(':');
				if (colon < 0) continue;
				if (part.substring(0, colon).trim().equalsIgnoreCase(name)) {
					return Optional.of(part.substring(colon + 1).trim());
				}
			}
			return Optional.empty();
		});
	}

	/** Gets the parameter's declared minimum, in the parameter's own type. */
	public static Number min(final ParameterNode node) {
		return number(node, ParameterMember.MIN);
	}

	/** Gets the parameter's declared maximum, in the parameter's own type. */
	public static Number max(final ParameterNode node) {
		return number(node, ParameterMember.MAX);
	}

	/** Gets the parameter's step size, defaulting to one. */
	public static Number stepSize(final ParameterNode node) {
		final Number step = number(node, ParameterMember.STEP_SIZE);
		return step == null ? toType("1", type(node)) : step;
	}

	/**
	 * Reads a number in the given type.
	 * <p>
	 * NB: bounds arrive as strings, because metadata has to survive a script
	 * header; the widget wants them in the parameter's own type, so that a
	 * {@code long} parameter is not silently bounded by a {@code double}.
	 * </p>
	 *
	 * @return the parsed number, or null if the text is not a number
	 */
	public static Number toType(final String text, final Class<?> type) {
		if (text == null || text.isEmpty()) return null;
		try {
			final Class<?> t = box(type);
			if (t == Byte.class) return Byte.valueOf(text);
			if (t == Short.class) return Short.valueOf(text);
			if (t == Integer.class) return Integer.valueOf(text);
			if (t == Long.class) return Long.valueOf(text);
			if (t == Float.class) return Float.valueOf(text);
			if (t == BigInteger.class) return new BigInteger(text);
			if (t == BigDecimal.class) return new BigDecimal(text);
			return Double.valueOf(text);
		}
		catch (final NumberFormatException exc) {
			return null;
		}
	}

	/** Gets the wrapper type for a primitive, or the type itself. */
	public static Class<?> box(final Class<?> type) {
		if (type == null || !type.isPrimitive()) return type;
		if (type == byte.class) return Byte.class;
		if (type == short.class) return Short.class;
		if (type == int.class) return Integer.class;
		if (type == long.class) return Long.class;
		if (type == float.class) return Float.class;
		if (type == double.class) return Double.class;
		if (type == boolean.class) return Boolean.class;
		if (type == char.class) return Character.class;
		return type;
	}

	// -- Helper methods --

	private static Number number(final ParameterNode node, final String key) {
		return attr(node, key).map(text -> toType(text, type(node))).orElse(null);
	}
}
