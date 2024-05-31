/*-
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.scijava.common3.Types;

/**
 * A partial {@link TypeExtractor} implementation that specializes in recovering
 * sub{@link Type}s based on a defined super{@link Type}.
 *
 * @param <T> the super{@link Type} that implementations cover
 * @author Gabriel Selzer
 */
public abstract class SubTypeExtractor<T> implements TypeExtractor {

	/**
	 * Returns a {@link Type} array, where the {@code i}th {@link Type} in the
	 * array is the {@code i}th type parameter of the {@link Class} returned by
	 * {@link #baseClass()}. Similar to
	 * {@link ParameterizedType#getActualTypeArguments()}, but works on
	 * {@link Object}s instead of {@link ParameterizedType}s.
	 *
	 * @param r the {@link TypeReifier} used to reify the type parameters.
	 * @param object the {@link Object} to extract type parameters from.
	 * @see ParameterizedType#getActualTypeArguments()
	 * @return the actual type arguments of {@code Object}, with respect to the
	 *         {@link Type} returned by {@link #baseClass()}
	 */
	protected abstract Type[] getTypeParameters(final TypeReifier r,
		final T object);

	@Override
	public Type reify(final TypeReifier r, final Object object) {
		if (!baseClass().isAssignableFrom(object.getClass()))
			throw new IllegalArgumentException(this.getClass().getName() +
				" can only reify Objects of Class " + baseClass().getSimpleName() +
				"!");
		@SuppressWarnings("unchecked")
		final Type[] typeVars = getTypeParameters(r, (T) object);

		// TODO: Check whether Types already has a method with this functionality.
		return parameterizeViaSuperType(object.getClass(), baseClass(), typeVars);
	}

	/**
	 * Returns {@code cls}, parameterized with {@link TypeVariable} bounds defined
	 * by a set of type parameters <b>on some superclass </b> of {@code cls}
	 *
	 * @param cls the {@link Class} to be parameterized
	 * @param superCls a super{@link Class} of {@code cls}
	 * @param superClsTypeVars the type parameters of {@code supercls}, to be
	 *          raised to the {@link Class} {@code cls}
	 * @return {@code cls}, but parameterized with the type variables in
	 *         {@code superClsTypeVars} against superclass {@code superCls}
	 */
	private static Type parameterizeViaSuperType(Class<?> cls,
		final Class<?> superCls, final Type... superClsTypeVars)
	{
		Type t = Types.parameterize(cls);
		Type[] typeVars = Types.typeParamsOf(t, superCls);
		if (typeVars.length != superClsTypeVars.length) {
			throw new IllegalArgumentException("Type variables " + Arrays.toString(
				typeVars) + " of class " + cls +
				" did not match the expected type variables " + Arrays.toString(
					superClsTypeVars) + " of superclass " + superCls);
		}
		Map<TypeVariable<?>, Type> map = new HashMap<>();
		for (int i = 0; i < typeVars.length; i++) {
			if (typeVars[i] instanceof TypeVariable) map.put(
				(TypeVariable<?>) typeVars[i], superClsTypeVars[i]);
		}
		return Types.unroll(t, map);
	}
}
