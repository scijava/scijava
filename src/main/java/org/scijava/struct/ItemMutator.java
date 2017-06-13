package org.scijava.struct;


public interface ItemMutator<C> {

	<T> void set(StructItem<T> item, T value, C o);
//	void set(String name, Object value, C o); // DO WE NEED IT
}
