package org.scijava.swing.widget;

import org.scijava.widget.NWidgetFactory;

public interface SwingWidgetFactory extends NWidgetFactory<NSwingWidget> {

	@Override
	default Class<NSwingWidget> widgetType() {
		return NSwingWidget.class;
	}
}
