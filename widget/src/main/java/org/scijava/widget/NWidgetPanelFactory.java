package org.scijava.nwidget;

import java.util.List;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.struct.StructInstance;

public interface NWidgetPanelFactory<W extends NWidget>
	extends SciJavaPlugin
{

	<C> NWidgetPanel<C> create(StructInstance<C> structInstance,
		List<? extends W> widgets);

	Class<W> widgetType();
}
