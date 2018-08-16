package org.scijava.struct;

import java.util.Iterator;
import java.util.List;

/**
 * A structure consisting of typed fields called {@link Member}s.
 * 
 * @author Curtis Rueden
 * @author Christian Dietz
 */
public interface Struct extends Iterable<Member<?>> {

	List<Member<?>> members();
	
	@Override
	default Iterator<Member<?>> iterator() {
		return members().iterator();
	}

	default <C> StructInstance<C> createInstance(final C object) {
		return new DefaultStructInstance<>(this, object);
	}
}
