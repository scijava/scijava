package org.scijava.struct;

import java.util.Iterator;
import java.util.List;

/** An instance of a {@link Struct}. */
public interface StructInstance<C> extends Iterable<MemberInstance<?>>
{

	List<MemberInstance<?>> members();

	/**
	 * @return the {@link Struct} that describes this instance
	 */
	Struct struct();

	/**
	 * @return the object backing this instance of the struct
	 */
	C object();

	/**
	 * @param key
	 * @return the {@link Member} for the given key
	 */
	MemberInstance<?> member(String key);

	@Override
	default Iterator<MemberInstance<?>> iterator() {
		return members().iterator();
	}
}