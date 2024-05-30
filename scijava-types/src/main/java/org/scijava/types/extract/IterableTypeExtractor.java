/*
 * #%L
 * SciJava library for generic type reasoning.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.types.extract;

import java.lang.reflect.Type;
import java.util.stream.StreamSupport;

import org.scijava.priority.Priority;
import org.scijava.common3.Any;
import org.scijava.common3.Types;

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
public class IterableTypeExtractor extends SubTypeExtractor<Iterable<?>> {

	@Override
	public double priority() {
		return Priority.VERY_LOW;
	}

	@Override
	public Class<?> baseClass() {
		return Iterable.class;
	}

	@Override
	protected Type[] getTypeParameters(TypeReifier r, Iterable<?> object) {
		// Obtain the element type using the TypeService.
		int typesToCheck = 100;
		// can we make this more efficient (possibly a parallel stream)?
		Type[] types = StreamSupport.stream(object.spliterator(), false) //
			.limit(typesToCheck) //
			.map(r::reify) //
			.toArray(Type[]::new);

		Type actual = Types.commonSuperTypeOf(types);
		if (actual == null) actual = new Any();
		return new Type[] { actual };
	}

}
