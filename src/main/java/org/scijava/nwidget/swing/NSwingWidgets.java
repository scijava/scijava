
package org.scijava.nwidget.swing;

import javax.swing.JComponent;

import org.scijava.nwidget.NWidget;
import org.scijava.nwidget.NWidgets;

/** Utility class for working with {@link NWidget}s. */
public final class NSwingWidgets {

	private NSwingWidgets() {
		// NB: Prevent instantiation of utility class.
	}

	/** Assigns the widget's description as the given component's tool tip. */
	public static void setToolTip(final NSwingWidget widget, final JComponent c) {
		final String desc = NWidgets.description(widget);
		if (desc == null || desc.isEmpty()) return;
		c.setToolTipText(desc);
	}
}
