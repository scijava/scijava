
package org.scijava.struct;

import java.util.List;
import java.util.stream.Collectors;

public final class Structs {

	private Structs() {
		// NB: Prevent instantiation of utility class.
	}

	public static <C> Struct<C> create(final StructInfo<?> info, final C object) {
		return new Struct<>(info, object);
	}

	public static Struct<?> create(final Struct<?> parent, final String key) {
		return create(parent, parent.item(key));
	}

	public static <T> Struct<T> create(final Struct<?> parent,
		final StructItem<T> item)
	{
		if (!item.isStruct() || !(item instanceof ValueAccessible<?>)) return null;
		@SuppressWarnings("unchecked")
		final ValueAccessible<T> access = (ValueAccessible<T>) item;
		final T value = access.get(parent.object());
		return value == null ? null : create(item.childInfo(), value);
	}

	public static List<ValueAccessible<?>> accessibleItems(
		final StructInfo<?> info)
	{
		return accessibleItems(info.items());
	}

	public static List<ValueAccessible<?>> accessibleItems(
		final List<? extends StructItem<?>> items)
	{
		return items.stream().filter(//
			item -> ValueAccessible.class.isInstance(item)//
		).map(item -> (ValueAccessible<?>) item).collect(Collectors.toList());
	}
}
