
package org.scijava.nwidget;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.scijava.plugin.Plugin;
import org.scijava.struct.StructInstance;
import org.scijava.widget.UIComponent;

@Plugin(type = NWidgetPanelFactory.class)
public class NSwingWidgetPanelFactory<C> implements
	NWidgetPanelFactory<C, NSwingWidget>
{

	@Override
	public WidgetPanel create(final StructInstance<C> struct,
		final List<? extends NSwingWidget> widgets)
	{
		return new WidgetPanel(struct, widgets);
	}

	@Override
	public Class<NSwingWidget> widgetType() {
		return NSwingWidget.class;
	}

	// -- Helper classes --

	private class WidgetPanel extends NAbstractWidgetPanel<C> implements
		UIComponent<JPanel>
	{

		private final List<? extends NSwingWidget> widgets;
		private JPanel panel;

		public WidgetPanel(final StructInstance<C> struct,
			final List<? extends NSwingWidget> widgets)
		{
			super(struct);
			this.widgets = widgets;
		}

		@Override
		public JPanel getComponent() {
			if (panel != null) return panel;

			panel = new JPanel();
			final MigLayout layout =
					new MigLayout("fillx,wrap 2", "[right]10[fill,grow]");
			panel.setLayout(layout);

			widgets.stream().forEach(widget -> {
				// add widget to panel
				final String label = widget.getLabel();
				if (label != null) {
					// widget is prefixed by a label
					final JLabel l = new JLabel(label);
					final String desc = widget.getDescription();
					if (desc != null && !desc.isEmpty()) l.setToolTipText(desc);
					panel.add(l);
					panel.add(widget.getComponent());
				}
				else {
					// widget occupies entire row
					getComponent().add(widget.getComponent(), "span");
				}
			});

			return panel;
		}

		@Override
		public Class<JPanel> getComponentType() {
			return JPanel.class;
		}
	}
}
