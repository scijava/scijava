package org.scijava.nwidget;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Member;

public interface NWidgetFactory<W extends NWidget> extends SciJavaPlugin {

	boolean supports(Member<?> item);
	NWidget create(StructInstance<?> struct, String key);

	Class<W> widgetType();
}
