
package org.scijava.nwidget;

import java.util.List;

import javax.swing.JPanel;

import org.scijava.struct.StructInstance;
import org.scijava.widget.UIComponent;

public class NSwingWidgetPanelFactory<C> implements
	NWidgetPanelFactory<C, NSwingWidget>
{

	@Override
	public SwingWidgetPanel create(final StructInstance<C> struct,
		final List<? extends NSwingWidget> widgets)
	{
		return new SwingWidgetPanel(struct, widgets);
	}

	@Override
	public Class<NSwingWidget> widgetType() {
		return NSwingWidget.class;
	}

	// -- Helper classes --

	private class SwingWidgetPanel extends NAbstractWidgetPanel<C> implements
		UIComponent<JPanel>
	{

		private final List<? extends NSwingWidget> widgets;
		private JPanel panel;

		public SwingWidgetPanel(final StructInstance<C> struct,
			final List<? extends NSwingWidget> widgets)
		{
			super(struct);
			this.widgets = widgets;
		}

		@Override
		public JPanel getComponent() {
			if (panel == null) {
				panel = new JPanel();
				widgets.stream().forEach(w -> panel.add(w.getComponent()));
			}
			return panel;
		}

		@Override
		public Class<JPanel> getComponentType() {
			return JPanel.class;
		}

	}
}
