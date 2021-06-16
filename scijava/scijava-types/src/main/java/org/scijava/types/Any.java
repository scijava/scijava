package org.scijava.types;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * This {@link Type} represents a Type that can be assigned to any other Type.
 * Note that this Type is different from a TypeVariable in that it is
 * conceptually a concrete type, not a stand-in for a type.
 * 
 * @author Gabe Selzer
 *
 */
public class Any implements Type {
	@Override
	public String toString() {
		return "Any";
	}

	// the superclasses of this Any
	Type[] upperBounds;
	// the subclasses of this Any
	Type[] lowerBounds;

	public Any() {
		this.upperBounds = new Type[] {};
		this.lowerBounds = new Type[] {};
	}

	public Any(Type[] upperBounds) {
		this.upperBounds = upperBounds;
		this.lowerBounds = new Type[] {};
	}

	public Any(Type[] upperBounds, Type[] lowerBounds) {
		this.upperBounds = upperBounds;
		this.lowerBounds = lowerBounds;
	}

	public Type[] getUpperBounds() {
		return upperBounds;
	}

	public Type[] getLowerBounds() {
		return lowerBounds;
	}

	/**
	 * This method returns true iff:
	 * <ul>
	 * <li>{@code obj} is an {@link Any}
	 * <li>{@code obj.getUpperBounds} is equal to {@code upperBounds}
	 * (disregarding the order of each array)
	 * <li>{@code obj.getLowerBounds} is equal to {@code lowerBounds}
	 * (disregarding the order of each array)
	 * <p>
	 * This is a rather strict definition of equality, however it is necessary to
	 * preserve transitivity.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Any) || obj == null) return false;
		Any other = (Any) obj;

		return equalBounds(upperBounds, other.getUpperBounds()) && equalBounds(
			lowerBounds, other.getLowerBounds());
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (Type t : upperBounds)
			hash ^= t.hashCode();
		for (Type t : lowerBounds)
			hash ^= t.hashCode();
		return hash;
	}

	private boolean equalBounds(Type[] ours, Type[] theirs) {
		if (ours.length != theirs.length) return false;

		List<Type> ourList = Arrays.asList(ours);
		List<Type> theirList = Arrays.asList(theirs);

		for (Type t : ourList) {
			if (!theirList.contains(t)) return false;
		}

		return true;
	}

}
