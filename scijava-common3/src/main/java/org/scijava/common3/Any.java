/*-
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.common3;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * This {@link Type} represents a Type that can be assigned to any other Type.
 * Note that this Type is different from a TypeVariable in that it is
 * conceptually a concrete type, not a stand-in for a type.
 *
 * @author Gabriel Selzer
 */
public final class Any implements Type {

	@Override
	public String toString() {
		return "Any";
	}

	// the superclasses of this Any
	private Type[] upperBounds;
	// the subclasses of this Any
	private Type[] lowerBounds;

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

	// NB: These bounds accessor methods are named to be
	// consistent with those of java.lang.reflect.WildcardType.

	public Type[] getUpperBounds() {
		return upperBounds;
	}

	public Type[] getLowerBounds() {
		return lowerBounds;
	}

	/**
	 * This method returns true iff:
	 * <ul>
	 * <li>{@code obj} is an {@link Any}</li>
	 * <li>{@code obj.getUpperBounds} is equal to {@code this.getUpperBounds}
	 * (disregarding the order of each array)</li>
	 * <li>{@code obj.getLowerBounds} is equal to {@code this.getLowerBounds}
	 * (disregarding the order of each array)</li>
	 * </ul>
	 * This is a rather strict definition of equality, however it is necessary to
	 * preserve transitivity.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Any) || obj == null) return false;
		Any other = (Any) obj;

		return equalBounds(getUpperBounds(), other.getUpperBounds()) &&
			equalBounds(getLowerBounds(), other.getLowerBounds());
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (Type t : getUpperBounds())
			hash ^= t.hashCode();
		for (Type t : getLowerBounds())
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

	public static boolean is(Object o) {
		return o instanceof Any || o.equals(Any.class);
	}

}
