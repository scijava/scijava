
package org.scijava.param;

import org.scijava.struct.ItemMutator;
import org.scijava.struct.StructItem;
import org.scijava.util.ClassUtils;

/** {@link ItemMutator} for {@link FieldParameterItem}s. */
public class ParameterMutator<C> implements ItemMutator<C> {

	@Override
	public <T> void set(final StructItem<T> item, final T value, final C o) {
		ClassUtils.setValue(ParameterStructs.field(item), o, value);
	}
}
