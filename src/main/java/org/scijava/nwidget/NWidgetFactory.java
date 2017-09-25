package org.scijava.nwidget;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.struct.MemberInstance;

// TODO extend SingletonPlugin ?
public interface NWidgetFactory<W extends NWidget> extends SciJavaPlugin {

	boolean supports(MemberInstance<?> memberInstance);
	NWidget create(MemberInstance<?> memberInstance);

	Class<W> widgetType();
}
