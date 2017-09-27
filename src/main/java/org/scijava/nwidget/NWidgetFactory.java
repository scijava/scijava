package org.scijava.nwidget;

import org.scijava.plugin.SingletonPlugin;
import org.scijava.struct.MemberInstance;

public interface NWidgetFactory<W extends NWidget> extends SingletonPlugin {

	boolean supports(MemberInstance<?> model);

	W create(MemberInstance<?> model,
		NWidgetPanelFactory<?, ? extends W> panelFactory);

	Class<W> widgetType();
}
