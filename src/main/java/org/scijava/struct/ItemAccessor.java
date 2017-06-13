package org.scijava.struct;


public interface ItemAccessor<C> {

	<T> T get(StructItem<T> item, C o);
//	Object get(String name, C o); // DO WE NEED IT
}
