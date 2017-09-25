package org.scijava.nwidget;

import org.scijava.struct.MemberInstance;

public interface NWidget {
	MemberInstance<?> model();

	/**
	 * Gets the label to use next to the widget, or null if the widget should
	 * occupy the entire panel row.
	 */
	default String getLabel() {
		return null;
	}
	
	default String getDescription() {
		return null;
	}
}
