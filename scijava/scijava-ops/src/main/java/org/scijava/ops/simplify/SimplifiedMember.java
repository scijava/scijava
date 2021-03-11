package org.scijava.ops.simplify;

import java.lang.reflect.Type;

import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;

public class SimplifiedMember<T> implements Member<T>{

	final Member<T> original;
	final Type newType;

	public SimplifiedMember(Member<T> original, Type newType) {
		this.original = original;
		this.newType = newType;
	}

	@Override
	public String getKey() {
		return original.getKey();
	}

	@Override
	public String getDescription() {
		return original.getDescription();
	}

	@Override
	public Type getType() {
		return newType;
	}

	@Override
	public ItemIO getIOType() {
		return original.getIOType();
	}

}
