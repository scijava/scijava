package org.scijava.struct;


public interface ValueAccessible<T> {

	T get(Object o);

	void set(T value, Object o);

}
