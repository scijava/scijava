package org.scijava.struct;

import java.util.function.Supplier;

public interface MemberInstance<T> extends Supplier<T> {

	Member<T> member();

	boolean isReadable();
	boolean isWritable();

	/**
	 * Get's the value of the member.
	 * 
	 * @return The value of the {@link Member} with the given key.
	 * @throws IllegalArgumentException if the member is not readable (see
	 *           {@link #isReadable()}).
	 */
	@Override
	T get();

	/**
	 * Sets the value of the member.
	 * 
	 * @param value The value to set.
	 * @throws IllegalArgumentException if the member is not writable (see
	 *           {@link #isWritable()}).
	 */
	void set(Object value);

}
