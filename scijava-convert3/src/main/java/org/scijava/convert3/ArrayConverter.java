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

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

import org.scijava.common3.Types;
import org.scijava.priority.Priority;

/**
 * Converts to an array, converting each element on the way.
 * <p>
 * A collection becomes an array, an array becomes an array of another
 * component type, and a lone value becomes an array of one - which is what
 * makes {@code "3"} usable where {@code int[]} is wanted, a thing scripts and
 * command lines do constantly.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ArrayConverter implements Converter<Object, Object> {

	@Override
	public Type sourceType() {
		return Object.class;
	}

	@Override
	public Type destType() {
		return Object[].class;
	}

	@Override
	public boolean supports(final Object source, final Type dest) {
		return source != null && Types.component(dest) != null;
	}

	@Override
	public Object convert(final Object source, final Type dest) {
		final Type componentType = Types.component(dest);
		final Class<?> component = Types.raw(componentType);
		if (component == null) return null;

		final Object[] elements = elements(source);
		final Object array = Array.newInstance(component, elements.length);
		final Converters converters = Converters.get();
		for (int i = 0; i < elements.length; i++) {
			// NB: each element is converted in turn, so a List<String> reaches an
			// int[] without anything knowing about that pair specifically.
			final Object element = converters.tryConvert(elements[i], componentType)
				.orElse(null);
			if (element == null && elements[i] != null) return null;
			Array.set(array, i, element);
		}
		return array;
	}

	@Override
	public double priority() {
		return Priority.HIGH;
	}

	// -- Helper methods --

	private static Object[] elements(final Object source) {
		if (source instanceof Collection) {
			return ((Collection<?>) source).toArray();
		}
		if (source.getClass().isArray()) {
			final int length = Array.getLength(source);
			final Object[] elements = new Object[length];
			for (int i = 0; i < length; i++)
				elements[i] = Array.get(source, i);
			return elements;
		}
		return new Object[] { source };
	}
}
