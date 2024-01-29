/*-
 * #%L
 * SciJava library for generic type reasoning.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A partial {@link TypeExtractor} implementation that specializes in recovering
 * sub{@link Type}s based on a defined super{@link Type}.
 *
 * @param <T> the super{@link Type} that implementations cover
 * @author Gabriel Selzer
 */
public abstract class SubTypeExtractor<T> implements TypeExtractor {

	protected abstract Class<?> getRawType();

	/**
	 * Returns a {@link Type} array, where the {@code i}th {@link Type} in the
	 * array is the {@code i}th type parameter of the {@link Class} returned by
	 * {@link #getRawType()}. Similar to
	 * {@link ParameterizedType#getActualTypeArguments()}, but works on
	 * {@link Object}s instead of {@link ParameterizedType}s.
	 *
	 * @param r the {@link TypeReifier} used to reify the type parameters.
	 * @param object the {@link Object} to extract type parameters from.
	 * @see ParameterizedType#getActualTypeArguments()
	 * @return the actual type arguments of {@code Object}, with respect to the
	 *         {@link Type} returned by {@link #getRawType()}
	 */
	protected abstract Type[] getTypeParameters(final TypeReifier r,
		final T object);

	@Override
	public boolean canReify(TypeReifier r, Class<?> cls) {
		return getRawType().isAssignableFrom(cls);
	}

	@Override
	public Type reify(final TypeReifier r, final Object object) {
		if (!canReify(r, object.getClass())) throw new IllegalArgumentException(
			this + " can only reify Objects of Class " + getRawType()
				.getSimpleName() + "!");
		@SuppressWarnings("unchecked")
		final Type[] typeVars = getTypeParameters(r, (T) object);
		return TypeTools.parameterizeViaSuperType(object.getClass(), getRawType(),
			typeVars);
	}

}
