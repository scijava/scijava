package org.scijava.param;

import java.lang.reflect.Type;

import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;

/**
 * {@link Member} whose {@link Type} has been converted into another {@link Type}
 * 
 * @author Gabriel Selzer
 *
 * @param <T>
 */
public class ConvertedParameterMember<T> implements Member<T>{
	
	final Member<T> original;
	final Type newType;
	
	public ConvertedParameterMember(Member<T> original, Type newType) {
		this.original = original;
		this.newType = newType;
	}

	@Override
	public String getKey() {
		return original.getKey();
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
