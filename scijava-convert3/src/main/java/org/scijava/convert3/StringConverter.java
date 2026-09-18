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

/**
 * Converts text to a value: a number, a character, a boolean, an enum
 * constant.
 * <p>
 * This is the converter a script, a command line and a text field all need,
 * since each of them has only strings to offer.
 * </p>
 * <p>
 * NB: text that is not a value of the wanted type converts to nothing, rather
 * than to zero. A half-typed number is not a zero, and a caller that wants to
 * know the difference gets to.
 * </p>
 *
 * @author Curtis Rueden
 */
public class StringConverter implements Converter<String, Object> {

	@Override
	public Type sourceType() {
		return String.class;
	}

	@Override
	public Type destType() {
		return Object.class;
	}

	@Override
	public boolean supports(final Object source, final Type dest) {
		if (!(source instanceof String) || dest == null) return false;
		final Class<?> raw = Types.raw(dest);
		if (raw == null) return false;
		final Class<?> type = Classes.box(raw);
		return type == Character.class || type == Boolean.class || type
			.isEnum() || isNumeric(type);
	}

	@Override
	public Object convert(final Object source, final Type dest) {
		final String text = (String) source;
		final Class<?> type = Classes.box(Types.raw(dest));
		if (type == Character.class) {
			return text.isEmpty() ? null : text.charAt(0);
		}
		if (type == Boolean.class) {
			if (text.equalsIgnoreCase("true")) return Boolean.TRUE;
			if (text.equalsIgnoreCase("false")) return Boolean.FALSE;
			return null;
		}
		if (type.isEnum()) {
			try {
				return Types.enumValue(text, type);
			}
			catch (final IllegalArgumentException exc) {
				return null; // NB: no such constant
			}
		}
		try {
			if (type == Byte.class) return Byte.valueOf(text);
			if (type == Short.class) return Short.valueOf(text);
			if (type == Integer.class) return Integer.valueOf(text);
			if (type == Long.class) return Long.valueOf(text);
			if (type == Float.class) return Float.valueOf(text);
			if (type == Double.class) return Double.valueOf(text);
			if (type == BigInteger.class) return new BigInteger(text);
			if (type == BigDecimal.class) return new BigDecimal(text);
		}
		catch (final NumberFormatException exc) {
			return null; // NB: not a number yet, or not a number at all
		}
		return null;
	}

	// -- Helper methods --

	private static boolean isNumeric(final Class<?> type) {
		return type == Byte.class || type == Short.class || type == Integer.class ||
			type == Long.class || type == Float.class || type == Double.class ||
			type == BigInteger.class || type == BigDecimal.class;
	}
}
