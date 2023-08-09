
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
		return TypeTools.raiseParametersToClass(object.getClass(), getRawType(),
			typeVars);
	}

}
