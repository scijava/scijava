
package org.scijava.widget;

import org.scijava.struct.StructInstance;

public class AbstractWidgetPanel<C> implements WidgetPanel<C> {

	private StructInstance<C> struct;

	public AbstractWidgetPanel(StructInstance<C> struct) {
		this.struct = struct;
	}

	@Override
	public StructInstance<C> struct() { return struct; }

}
