package org.scijava.widget;

import org.scijava.plugin.SingletonPlugin;
import org.scijava.struct.MemberInstance;

public interface WidgetFactory<W extends Widget> extends SingletonPlugin {

	boolean supports(MemberInstance<?> model);

	W create(MemberInstance<?> model,
		NWidgetPanelFactory<? extends W> panelFactory);

	Class<W> widgetType();
}
