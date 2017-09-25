package org.scijava.nwidget.swing;

import javax.swing.JPanel;

import org.scijava.nwidget.NWidget;
import org.scijava.widget.UIComponent;

public interface NSwingWidget extends NWidget, UIComponent<JPanel> {

	@Override
	default Class<JPanel> getComponentType() {
		return JPanel.class;
	}
}
