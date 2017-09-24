
package org.scijava.nwidget;

import org.scijava.struct.StructInstance;

public class NAbstractWidgetPanel<C> implements NWidgetPanel<C> {

	private StructInstance<C> struct;

	public NAbstractWidgetPanel(StructInstance<C> struct) {
		this.struct = struct;
	}

	@Override
	public StructInstance<C> struct() { return struct; }

}
