package org.scijava.struct;

import java.util.Iterator;
import java.util.List;

public interface StructInfo<I extends StructItem<?>> extends Iterable<I> {

	List<I> items();
	
	<C> Struct<C> structOf(C o);
	
	@Override
	default Iterator<I> iterator() {
		return items().iterator();
	}
}
