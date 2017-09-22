package org.scijava.struct;

public interface Struct<C> {

	/**
	 * @return the {@link StructInfo} that describes this Struct
	 */
	StructInfo<?> info();

	/**
	 * @return the object that instantiates the
	 */
	C object();

	/**
	 * @param key
	 * @return the {@link StructItem} for the given key
	 */
	StructItem<?> item(String key);

	/**
	 * Returns the {@link StructItem} for the given key as {@link ValueAccessible}
	 * iff it is a {@link ValueAccessible}. Otherwise returns <code>null</code>.
	 */
	ValueAccessible<?> accessibleItem(String key);

	/**
	 * @param key
	 * @return the value of the {@link StructItem} with the given key, IFF the item
	 *         is {@link ValueAccessible}.
	 * @throws IllegalArgumentException
	 *             if the item is not {@link ValueAccessible} or no item for the key
	 *             exists
	 */
	Object get(String key);

	/**
	 * Set the ... coresponding the key with the given value
	 * 
	 * @param key
	 * @param value
	 */
	void set(String key, Object value);

}