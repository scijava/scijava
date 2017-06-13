
package org.scijava.param;

import org.scijava.struct.ItemAccessor;
import org.scijava.struct.StructItem;
import org.scijava.util.ClassUtils;

/** {@link ItemAccessor} for {@link ParameterItem}s. */
public class ParameterAccessor<C> implements ItemAccessor<C> {

	@Override
	public <T> T get(final StructItem<T> item, final C o) {
		@SuppressWarnings("unchecked")
		final T value = (T) ClassUtils.getValue(ParameterStructs.field(item), o);
		return value;
	}
}
