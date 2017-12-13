
package org.scijava.swing.widget;

import javax.swing.JComponent;

import org.scijava.widget.NWidget;
import org.scijava.widget.NWidgets;

/** Utility class for working with {@link NWidget}s and Swing. */
public final class SwingWidgets {

	private SwingWidgets() {
		// NB: Prevent instantiation of utility class.
	}

	/** Assigns the widget's description as the given component's tool tip. */
	public static void setToolTip(final NWidget widget, final JComponent c) {
		final String desc = NWidgets.description(widget);
		if (desc == null || desc.isEmpty()) return;
		c.setToolTipText(desc);
	}
}
