package org.scijava.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.scijava.types.Types;

/**
 * One element (i.e. item/field/member) of a {@link Struct}.
 * 
 * @author Curtis Rueden
 * @author Christian Dietz
 */
public interface Member<T> {

	/** Unique name of the member. */
	String getKey();

	/** Description of the member. */
	String getDescription();

	/**
	 * Gets the type of the member, including Java generic parameters.
	 * 
	 * @see Field#getGenericType()
	 */
	// TODO: Use Type<T> or Nil<T> from new scijava-types.
	Type getType();
	
	/**
	 * Gets the {@link Class} of the member's type, or null if {@link #getType()}
	 * does not return a raw class.
	 */
	@SuppressWarnings("unchecked")
	default Class<T> getRawType() {
		return (Class<T>) Types.raw(getType());
	}

	/** Gets the input/output type of the member. */
	// TODO: fork ItemIO and rename to MemberIO (?)
	ItemIO getIOType();

	/** Gets whether the member is an input. */
	default boolean isInput() {
		return getIOType() == ItemIO.INPUT || getIOType() == ItemIO.CONTAINER || getIOType() == ItemIO.MUTABLE;
	}

	/** Gets whether the member is an output. */
	default boolean isOutput() {
		return getIOType() == ItemIO.OUTPUT || getIOType() == ItemIO.CONTAINER || getIOType() == ItemIO.MUTABLE;
	}

	default boolean isStruct() {
		return false;
	}

	default boolean isRequired() {
		return true;
	}

	default Struct childStruct() {
		return null;
	}

	default MemberInstance<T> createInstance(
		@SuppressWarnings("unused") Object o)
	{
		return () -> Member.this;
	}
}
