package org.scijava.ops.types;

import java.lang.reflect.Type;

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

}
