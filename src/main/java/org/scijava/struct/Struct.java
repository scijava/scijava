
package org.scijava.struct;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Struct<C> {

	private final StructInfo<?> info;
	private final C object;
	private final ItemAccessor<C> accessor;
	private final ItemMutator<C> mutator;

	private Map<String, StructItem<?>> itemMap;

	public Struct(final StructInfo<? extends StructItem<?>> info, final C object,
		final ItemAccessor<C> accessor, final ItemMutator<C> mutator)
	{
		this.info = info;
		this.object = object;
		this.accessor = accessor;
		this.mutator = mutator;
		itemMap = info.items().stream().collect(Collectors.toMap(StructItem::getKey,
			Function.identity()));
	}

	public StructInfo<?> info() {
		return info;
	}

	public C object() {
		return object;
	}

	public <T> T get(final StructItem<T> item) {
		return accessor.get(item, object);
	}

	// TODO: Consider allowing dot-separated key names for concise nesting.
	public Object get(final String key) {
		return accessor.get(item(key), object);
	}

	public StructItem<?> item(String key) {
		return itemMap.get(key);
	}

	public <T> void set(final StructItem<T> item, final T value) {
		mutator.set(item, value, object);
	}

	public <T> void set(final String key, final T value) {
		// TODO: Check the type for compatibity at runtime.
		@SuppressWarnings("unchecked")
		StructItem<T> item = (StructItem<T>) item(key);
		set(item, value);
	}
}
