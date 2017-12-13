package org.scijava.widget;

import java.util.List;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.struct.StructInstance;

public interface WidgetPanelFactory<W extends Widget>
	extends SciJavaPlugin
{

	<C> WidgetPanel<C> create(StructInstance<C> structInstance,
		List<? extends W> widgets);

	Class<W> widgetType();
}
