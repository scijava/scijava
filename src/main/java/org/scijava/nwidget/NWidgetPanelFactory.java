package org.scijava.nwidget;

import java.util.List;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.struct.StructInstance;

public interface NWidgetPanelFactory<C, W extends NWidget>
	extends SciJavaPlugin
{

	NWidgetPanel<C> create(StructInstance<C> struct, List<? extends W> widgets);
	Class<W> widgetType();

}
