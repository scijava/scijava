package org.scijava.ops.simplify;

import java.lang.reflect.Type;
import java.util.Arrays;

public class TypePair {

	private final Type a;
	private final Type b;

	public TypePair(Type a, Type b) {
		this.a = a;
		this.b = b;
	}

	public Type getA() {
		return a;
	}

	public Type getB() {
		return b;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof TypePair)) return false;
		TypePair thatPair = (TypePair) that;
		return a.equals(thatPair.getA()) && b.equals(thatPair.getB());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Type[] { a, b });
	}
}