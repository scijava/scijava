package org.scijava.nwidget;

public interface NSwingWidgetFactory extends NWidgetFactory<NSwingWidget> {

	@Override
	default Class<NSwingWidget> widgetType() {
		return NSwingWidget.class;
	}
}
