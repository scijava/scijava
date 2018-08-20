package org.scijava.struct;

import java.util.function.Supplier;

public interface MemberInstance<T> extends Supplier<T> {

	Member<T> member();

	default boolean isReadable() {
		return false;
	}
	default boolean isWritable() {
		return false;
	}

	/**
	 * Get's the value of the member.
	 * 
	 * @return The value of the {@link Member} with the given key.
	 * @throws UnsupportedOperationException if the member is not readable (see
	 *           {@link #isReadable()}).
	 */
	@Override
	default T get() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the value of the member.
	 * 
	 * @param value The value to set.
	 * @throws UnsupportedOperationException if the member is not writable (see
	 *           {@link #isWritable()}).
	 */
	default void set(Object value) {
		throw new UnsupportedOperationException();
	}

}
