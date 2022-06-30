/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava Ops developers.
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

package org.scijava.types.extractors;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import org.scijava.types.TypeExtractor;
import org.scijava.types.TypeReifier;
import org.scijava.types.Types;

/**
 * {@link TypeExtractor} plugin which operates on {@link Iterable} objects.
 * <p>
 * In an attempt to balance performance and correctness, we examine the first
 * 100 elements of the iteration and obtain the greatest common supertype of
 * each.
 * </p>
 *
 * @author Curtis Rueden
 */
public class IterableTypeExtractor implements TypeExtractor<Iterable<?>> {

	@Override
	public Type reify(final TypeReifier t, final Iterable<?> o, final int n) {
		if (n != 0) throw new IndexOutOfBoundsException();

		final Iterator<?> iterator = o.iterator();
		if (!iterator.hasNext()) return null;

		// Obtain the element type using the TypeService.
		int typesToCheck = 100;
		// can we make this more efficient (possibly a parallel stream)?
		Type[] types = StreamSupport.stream(o.spliterator(), false) //
			.limit(typesToCheck) //
			.map(s -> t.reify(s)) //
			.toArray(Type[]::new);

		return Types.greatestCommonSuperType(types, true);
		// TODO: Avoid infinite recursion when the list references itself.
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class<Iterable<?>> getRawType() {
		return (Class) Iterable.class;
	}

	/**
	 * Corresponds to org.scijava.Priority.LOW_PRIORITY
	 */
	@Override
	public double priority() {
		return -100;
	}

}
