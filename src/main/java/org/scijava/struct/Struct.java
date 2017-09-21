package org.scijava.struct;

public interface Struct<C> {

	StructInfo<?> info();

	C object();

	StructItem<?> item(String key);

	ValueAccessible<?> accessibleItem(String key);

	Object get(String key);

	void set(String key, Object value);

}