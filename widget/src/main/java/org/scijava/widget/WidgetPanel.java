package org.scijava.widget;

import org.scijava.struct.StructInstance;

public interface WidgetPanel<C> {
	StructInstance<C> struct();
}
