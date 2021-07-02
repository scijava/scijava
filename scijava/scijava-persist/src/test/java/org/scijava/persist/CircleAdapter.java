
package org.scijava.persist;

import org.scijava.persist.IClassRuntimeAdapter;
import org.scijava.plugin.Plugin;

@Plugin(type = IClassRuntimeAdapter.class)
public class CircleAdapter implements IClassRuntimeAdapter<Shape, Circle> {

	@Override
	public Class<? extends Shape> getBaseClass() {
		return Shape.class;
	}

	@Override
	public Class<? extends Circle> getRunTimeClass() {
		return Circle.class;
	}

}
