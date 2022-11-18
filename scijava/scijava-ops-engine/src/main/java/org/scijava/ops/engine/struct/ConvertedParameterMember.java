package org.scijava.ops.engine.struct;

import java.lang.reflect.Type;

import org.scijava.struct.FunctionalMethodType;
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
	final ItemIO ioType;
	
	public ConvertedParameterMember(Member<T> original, FunctionalMethodType newType) {
		this.original = original;
		this.newType = newType.type();
		this.ioType = newType.itemIO();
	}

	public static <M> ConvertedParameterMember<M> from(Member<M> original, FunctionalMethodType newType) {
		return new ConvertedParameterMember<>(original, newType);
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
		return ioType;
	}

	@Override
	public boolean isRequired() {
		return original.isRequired();
	}
}
