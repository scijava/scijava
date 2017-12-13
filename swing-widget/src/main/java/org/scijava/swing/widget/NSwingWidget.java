package org.scijava.widget.swing;

import javax.swing.JPanel;

import org.scijava.widget.Widget;

public interface SwingWidget extends Widget, UIComponent<JPanel> {

	@Override
	default Class<JPanel> getComponentType() {
		return JPanel.class;
	}
}
