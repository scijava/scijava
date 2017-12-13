package org.scijava.swing.widget;

import org.scijava.widget.WidgetFactory;

public interface SwingWidgetFactory extends WidgetFactory<SwingWidget> {

	@Override
	default Class<SwingWidget> widgetType() {
		return SwingWidget.class;
	}
}
