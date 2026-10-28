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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.scijava.common3.Types;
import org.scijava.priority.Priority;

/**
 * Converts to a collection, converting each element on the way.
 * <p>
 * The element type comes from the destination's type parameter where there is
 * one, so a {@code List<Double>} given a list of strings arrives holding
 * doubles rather than strings that will fail later.
 * </p>
 *
 * @author Curtis Rueden
 */
public class CollectionConverter implements Converter<Object, Collection<?>> {

	@Override
	public Type sourceType() {
		return Object.class;
	}

	@Override
	public Type destType() {
		return Collection.class;
	}

	@Override
	public boolean supports(final Object source, final Type dest) {
		if (source == null) return false;
		final Class<?> raw = Types.raw(dest);
		if (raw == null || !Collection.class.isAssignableFrom(raw)) return false;
		// NB: only the shapes we can actually instantiate. A caller wanting its
		// own collection type can contribute a converter for it.
		return raw.isAssignableFrom(ArrayList.class) || raw.isAssignableFrom(
			LinkedHashSet.class);
	}

	@Override
	public Object convert(final Object source, final Type dest) {
		final Class<?> raw = Types.raw(dest);
		final Type elementType = elementType(dest);
		final Collection<Object> collection = Set.class.isAssignableFrom(raw)
			? new LinkedHashSet<>() : new ArrayList<>();

		final Converters converters = Converters.get();
		for (final Object element : elements(source)) {
			if (elementType == null || elementType == Object.class) {
				collection.add(element);
				continue;
			}
			final Object converted = converters.tryConvert(element, elementType)
				.orElse(null);
			if (converted == null && element != null) return null;
			collection.add(converted);
		}
		return collection;
	}

	@Override
	public double priority() {
		return Priority.HIGH;
	}

	// -- Helper methods --

	/** Gets what the collection holds, if the type says. */
	private static Type elementType(final Type dest) {
		final Type[] params = Types.typeParamsOf(dest, Collection.class);
		return params == null || params.length == 0 ? null : params[0];
	}

	private static List<Object> elements(final Object source) {
		final List<Object> elements = new ArrayList<>();
		if (source instanceof Collection) {
			elements.addAll((Collection<?>) source);
		}
		else if (source.getClass().isArray()) {
			final int length = Array.getLength(source);
			for (int i = 0; i < length; i++)
				elements.add(Array.get(source, i));
		}
		else elements.add(source);
		return elements;
	}
}
