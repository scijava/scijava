package org.scijava.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.scijava.ItemIO;

/**
 * Metadata about an item of a {@link StructInfo} object.
 * 
 * @author Curtis Rueden
 */
public interface StructItem<T> {

	/** Unique name of the item. */
	String getKey();

	/**
	 * Gets the type of the item, including Java generic parameters.
	 * 
	 * @see Field#getGenericType()
	 */
	// TODO: Use Type<T> or Nil<T> from new scijava-types.
	Type getType();
	
	/**
	 * Gets the {@link Class} of the item's type, or null if {@link #getType()}
	 * does not return a raw class.
	 */
	default Class<T> getRawType() {
		final Type type = getType();
		if (!(type instanceof Class)) return null;
		@SuppressWarnings("unchecked")
		final Class<T> rawType = (Class<T>) type;
		return rawType;
	}

	/** Gets the input/output type of the item. */
	ItemIO getIOType();

	/** Gets whether the item is an input. */
	default boolean isInput() {
		return getIOType() == ItemIO.INPUT || getIOType() == ItemIO.BOTH;
	}

	/** Gets whether the item is an output. */
	default boolean isOutput() {
		return getIOType() == ItemIO.OUTPUT || getIOType() == ItemIO.BOTH;
	}
}
