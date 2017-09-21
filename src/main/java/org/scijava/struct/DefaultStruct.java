
package org.scijava.struct;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.util.ConversionUtils;

public class DefaultStruct<C> implements Struct<C> {

	private final StructInfo<?> info;
	private final C object;

	private final Map<String, StructItem<?>> itemMap;

	public DefaultStruct(final StructInfo<? extends StructItem<?>> info,
		final C object)
	{
		this.info = info;
		this.object = object;
		itemMap = info.items().stream().collect(//
			Collectors.toMap(StructItem::getKey, Function.identity()));
	}

	@Override
	public StructInfo<?> info() {
		return info;
	}

	@Override
	public C object() {
		return object;
	}

	@Override
	public StructItem<?> item(final String key) {
		return itemMap.get(key);
	}

	@Override
	public ValueAccessible<?> accessibleItem(final String key) {
		final StructItem<?> item = item(key);
		return item instanceof ValueAccessible ? (ValueAccessible<?>) item : null;
	}

	// TODO: Consider allowing dot-separated key names for concise nesting.

	@Override
	public Object get(final String key) {
		return accessibleOrDie(key).get(object);
	}

	
	@Override
	public void set(final String key, final Object value) {
		final StructItem<?> item = itemOrDie(key);
		setOrDie(item, value);
	}

	// -- Helper methods --

	private StructItem<?> itemOrDie(final String key) {
		final StructItem<?> item = item(key);
		if (item == null) throw new NoSuchElementException("No such item: " + key);
		return item;
	}

	private ValueAccessible<?> accessibleOrDie(final String key) {
		return accessibleOrDie(itemOrDie(key));
	}

	private <T> ValueAccessible<T> accessibleOrDie(final StructItem<T> item) {
		if (!(item instanceof ValueAccessible)) {
			throw new IllegalArgumentException("Inaccessible item: " + item.getKey());
		}
		@SuppressWarnings("unchecked")
		final ValueAccessible<T> access = (ValueAccessible<T>) item;
		return access;
	}

	private <T> void setOrDie(final StructItem<T> item, final Object value) {
		// check that item type is compatible
		final Class<?> itemType = item.getRawType();
		if (!ConversionUtils.canAssign(value, item.getRawType())) {
			throw new IllegalArgumentException(
				"Cannot assign value to item of type " + itemType);
		}
		@SuppressWarnings("unchecked")
		final T tValue = (T) value;
		accessibleOrDie(item).set(tValue, object);
	}
}
