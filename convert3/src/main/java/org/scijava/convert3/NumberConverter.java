/*
 * #%L
 * Converting a value to the type something else wants.
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

package org.scijava.convert3;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.scijava.common3.Classes;
import org.scijava.common3.Types;
import org.scijava.priority.Priority;

/**
 * Converts a number to another kind of number.
 * <p>
 * NB: narrowing is allowed and silent, as it is in Java itself: a
 * {@code double} parameter given a {@code long} is the ordinary case, and a
 * {@code long} parameter given a {@code double} rounds toward zero exactly as
 * a cast would. SciJava Common spread this over eight converter classes, one
 * per target type.
 * </p>
 *
 * @author Curtis Rueden
 */
public class NumberConverter implements Converter<Number, Number> {

	@Override
	public Type sourceType() {
		return Number.class;
	}

	@Override
	public Type destType() {
		return Number.class;
	}

	@Override
	public boolean supports(final Object source, final Type dest) {
		if (!(source instanceof Number) || dest == null) return false;
		final Class<?> raw = Types.raw(dest);
		return raw != null && isNumeric(Classes.box(raw));
	}

	@Override
	public Object convert(final Object source, final Type dest) {
		final Number number = (Number) source;
		final Class<?> type = Classes.box(Types.raw(dest));
		if (type == Byte.class) return number.byteValue();
		if (type == Short.class) return number.shortValue();
		if (type == Integer.class) return number.intValue();
		if (type == Long.class) return number.longValue();
		if (type == Float.class) return number.floatValue();
		if (type == Double.class) return number.doubleValue();
		if (type == BigInteger.class) return BigInteger.valueOf(number
			.longValue());
		if (type == BigDecimal.class) return toBigDecimal(number);
		return null;
	}

	@Override
	public double priority() {
		return Priority.HIGH;
	}

	// -- Helper methods --

	private static boolean isNumeric(final Class<?> type) {
		return type == Byte.class || type == Short.class || type == Integer.class ||
			type == Long.class || type == Float.class || type == Double.class ||
			type == BigInteger.class || type == BigDecimal.class;
	}

	private static BigDecimal toBigDecimal(final Number number) {
		if (number instanceof BigDecimal) return (BigDecimal) number;
		if (number instanceof BigInteger) return new BigDecimal(
			(BigInteger) number);
		// NB: through the string, so that 0.1 is 0.1 and not the double nearest
		// to it. A BigDecimal parameter asked for exactness by its type.
		return new BigDecimal(number.toString());
	}
}
