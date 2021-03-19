package org.scijava.param;

import java.lang.reflect.Type;

import org.scijava.struct.ItemIO;


/**
 * Wrapper to pair a type of a method signature with its {@link ItemIO}.
 * 
 * @author David Kolb
 */
public class FunctionalMethodType {

	private final Type type;
	private final ItemIO itemIO;
	
	public FunctionalMethodType(final Type type, final ItemIO itemIO) {
		this.type = type;
		this.itemIO = itemIO;
	}
	
	public Type type() {
		return this.type;
	}
	public ItemIO itemIO(){
		return this.itemIO;
	}
	
	@Override
	public String toString() {
		return itemIO + " : " + type.getTypeName();
	}
}
