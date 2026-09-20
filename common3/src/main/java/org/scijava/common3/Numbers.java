/*
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.common3;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Useful methods for working with {@link Number}s.
 *
 * @author Curtis Rueden
 */
public final class Numbers {

	private Numbers() {
		// NB: prevent instantiation of utility class.
	}

	/**
	 * Converts the given value to a {@link Number} of the given type.
	 * <p>
	 * Numbers are narrowed or widened as needed, and strings are parsed. Note
	 * that unlike SciJava Common's {@code NumberUtils.toNumber}, no general
	 * conversion framework is consulted: this method handles numbers and
	 * strings only, so that this module remains dependency-free.
	 * </p>
	 *
	 * @param value the value to convert
	 * @param type the desired numeric type
	 * @return the converted number, or {@code null} if the value is
	 *         {@code null}, or cannot be interpreted as a number of that type
	 */
	public static Number toNumber(final Object value, final Class<?> type) {
		if (value == null) return null;
		final Number n;
		if (value instanceof Number) n = (Number) value;
		else {
			final String s = value.toString().trim();
			if (s.isEmpty()) return null;
			try {
				n = new BigDecimal(s);
			}
			catch (final NumberFormatException exc) {
				return null;
			}
		}
		if (Classes.isByte(type)) return n.byteValue();
		if (Classes.isShort(type)) return n.shortValue();
		if (Classes.isInteger(type)) return n.intValue();
		if (Classes.isLong(type)) return n.longValue();
		if (Classes.isFloat(type)) return n.floatValue();
		if (Classes.isDouble(type)) return n.doubleValue();
		if (BigInteger.class.isAssignableFrom(type)) return bigInteger(n);
		if (BigDecimal.class.isAssignableFrom(type)) return bigDecimal(n);
		if (Classes.isNumber(type)) return n;
		return null;
	}

	/**
	 * Converts the given number to a {@link BigDecimal}, without the loss of
	 * accuracy that routing a long or {@link BigInteger} through
	 * {@link Number#doubleValue} would cause.
	 */
	public static BigDecimal bigDecimal(final Number n) {
		if (n instanceof BigDecimal) return (BigDecimal) n;
		if (n instanceof BigInteger) return new BigDecimal((BigInteger) n);
		if (n instanceof Long || n instanceof Integer || n instanceof Short ||
			n instanceof Byte)
		{
			return BigDecimal.valueOf(n.longValue());
		}
		return BigDecimal.valueOf(n.doubleValue());
	}

	/** Converts the given number to a {@link BigInteger}. */
	public static BigInteger bigInteger(final Number n) {
		if (n instanceof BigInteger) return (BigInteger) n;
		return BigInteger.valueOf(n.longValue());
	}

	/**
	 * Gets the smallest value of the given numeric type.
	 *
	 * @return the minimum, or {@code null} if the type is not numeric
	 */
	public static Number minimum(final Class<?> type) {
		if (Classes.isByte(type)) return Byte.MIN_VALUE;
		if (Classes.isShort(type)) return Short.MIN_VALUE;
		if (Classes.isInteger(type)) return Integer.MIN_VALUE;
		if (Classes.isLong(type)) return Long.MIN_VALUE;
		if (Classes.isFloat(type)) return -Float.MAX_VALUE;
		if (Classes.isDouble(type)) return -Double.MAX_VALUE;
		// NB: fallback for Number.class itself.
		if (Classes.isNumber(type)) return -Double.MAX_VALUE;
		return null;
	}

	/**
	 * Gets the largest value of the given numeric type.
	 *
	 * @return the maximum, or {@code null} if the type is not numeric
	 */
	public static Number maximum(final Class<?> type) {
		if (Classes.isByte(type)) return Byte.MAX_VALUE;
		if (Classes.isShort(type)) return Short.MAX_VALUE;
		if (Classes.isInteger(type)) return Integer.MAX_VALUE;
		if (Classes.isLong(type)) return Long.MAX_VALUE;
		if (Classes.isFloat(type)) return Float.MAX_VALUE;
		if (Classes.isDouble(type)) return Double.MAX_VALUE;
		// NB: fallback for Number.class itself.
		if (Classes.isNumber(type)) return Double.MAX_VALUE;
		return null;
	}

	/**
	 * Gets a sensible default value for the given numeric type and range: the
	 * minimum if there is one, else the maximum, else zero.
	 */
	public static Number defaultValue(final Number min, final Number max,
		final Class<?> type)
	{
		if (min != null) return min;
		if (max != null) return max;
		return toNumber("0", type);
	}

	/**
	 * Clamps the given value to the given range.
	 *
	 * @return the value, or the bound it exceeds; or
	 *         {@link #defaultValue(Number, Number, Class)} if the value is
	 *         {@code null}
	 */
	public static Number clamp(final Class<?> type, final Number value,
		final Number min, final Number max)
	{
		if (value == null) return defaultValue(min, max, type);
		if (Comparable.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			final Comparable<Number> cValue = (Comparable<Number>) value;
			if (min != null && cValue.compareTo(min) < 0) return min;
			if (max != null && cValue.compareTo(max) > 0) return max;
		}
		return value;
	}

	/**
	 * Multiplies the given values, throwing rather than overflowing.
	 *
	 * @param values the values to multiply
	 * @return the product
	 * @throws ArithmeticException if the product overflows an {@code int}
	 */
	public static int safeMultiply32(final long... values) {
		return Math.toIntExact(safeMultiply64(values));
	}

	/**
	 * Multiplies the given values, throwing rather than overflowing.
	 *
	 * @param values the values to multiply
	 * @return the product
	 * @throws ArithmeticException if the product overflows a {@code long}
	 */
	public static long safeMultiply64(final long... values) {
		long product = 1;
		for (final long value : values)
			product = Math.multiplyExact(product, value);
		return product;
	}
}
