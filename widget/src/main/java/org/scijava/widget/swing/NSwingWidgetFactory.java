package org.scijava.nwidget.swing;

import org.scijava.nwidget.NWidgetFactory;

public interface NSwingWidgetFactory extends NWidgetFactory<NSwingWidget> {

	@Override
	default Class<NSwingWidget> widgetType() {
		return NSwingWidget.class;
	}
}
