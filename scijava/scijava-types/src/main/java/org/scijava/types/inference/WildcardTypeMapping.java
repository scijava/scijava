
package org.scijava.types.inference;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.types.Types;

/**
 * A data structure retaining information about the mapping of a
 * {@link TypeVariable} to a {@link Type} bounded by a {@link WildcardType}
 * within a type-inferring context.
 * 
 * @author Gabriel Selzer
 */
public class WildcardTypeMapping extends TypeMapping {

	/**
	 * Current Java Language Specifications allow only one lower bound on any
	 * {@link WildcardType}. This method returns that singular bound, or
	 * {@code null} if this {@code WildcardType} has no lower bound.
	 *
	 * @param newType - the {@link WildcardType} for which we will find the lower
	 *          bound
	 * @return the lower bound of {@code newType}
	 */
	private static Type getLowerBound(WildcardType newType) {
		Type[] lowerBounds = newType.getLowerBounds();
		if (lowerBounds.length == 0) {
			return null;
		}
		else if (lowerBounds.length == 1) {
			return lowerBounds[0];
		}
		throw new TypeInferenceException(newType + //
			" is an impossible WildcardType. " + //
			"The Java language specification currently prevents multiple lower bounds " + //
			Arrays.toString(lowerBounds)); //
	}

	/**
	 * Current Java Language Specifications allow only one upper bound on any
	 * {@link WildcardType}. This method returns that singular bound, or
	 * {@code null} if this {@code WildcardType} has no upper bound.
	 *
	 * @param newType - the {@link WildcardType} for which we will find the upper
	 *          bound
	 * @return the upper bound of {@code newType}
	 */
	private static Type getUpperBound(WildcardType newType) {
		Type[] upperBounds = newType.getUpperBounds();
		if (upperBounds.length == 0) {
			return Object.class;
		}
		else if (upperBounds.length == 1) {
			return upperBounds[0];
		}
		else {
			throw new TypeInferenceException(newType + //
				" is an impossible WildcardType. " + //
				"The Java language specification currently prevents multiple upper bounds " + //
				Arrays.toString(upperBounds)); //
		}
	}

	private List<Type> lowerBoundList;

	public WildcardTypeMapping(TypeVariable<?> typeVar, WildcardType mappedType,
		boolean malleable)
	{
		super(typeVar, getUpperBound(mappedType), malleable);
		lowerBoundList = new ArrayList<>();
		Type mappedTypeLowerBound = getLowerBound(mappedType);
		if (mappedTypeLowerBound != null) {
			lowerBoundList.add(mappedTypeLowerBound);
		}
	}

	/**
	 * Attempts to accommodate {@code newType} into the current mapping between
	 * {@code typeVar} and {@code mappedType} <em>given</em> the existing
	 * malleability of {@code mappedType} and the malleability imposed by
	 * {@code newType}. If {@code newType} cannot be accommodated, a
	 * {@link TypeInferenceException} will be thrown. Note that it is not a
	 * guarantee that either the existing {@code mappedType} or {@code newType}
	 * will become the new {@link #mappedType} after the method ends;
	 * {@link #mappedType} could be a supertype of these two {@link Type}s.
	 * 
	 * @param otherType - the type that will be refined into {@link #mappedType}
	 * @param newTypeMalleability - the malleability of {@code otherType},
	 *          determined by the context from which {@code otherType} came.
	 */
	@Override
	public void refine(Type otherType, boolean newTypeMalleability) {
		if (otherType instanceof WildcardType) {
			refineWildcard((WildcardType) otherType, newTypeMalleability);
		}
		else {
			super.refine(otherType, newTypeMalleability);
		}
		for (Type lowerBound : lowerBoundList) {
			if (!Types.isAssignable(lowerBound, mappedType))
				throw new TypeInferenceException(typeVar +
					" cannot simultaneoustly be mapped to " + otherType + " and " +
					mappedType);
		}
	}

	private void refineWildcard(WildcardType otherType,
		boolean newTypeMalleability)
	{
		Type otherLowerBound = getLowerBound(otherType);
		if (otherLowerBound != null) {
			lowerBoundList.add(otherLowerBound);
		}
		Type otherUpperBound = getUpperBound(otherType);
		super.refine(otherUpperBound, newTypeMalleability);
	}
}
