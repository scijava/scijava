
package org.scijava.nwidget.swing;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.scijava.nwidget.NAbstractWidgetPanel;
import org.scijava.nwidget.NWidgetPanel;
import org.scijava.nwidget.NWidgets;
import org.scijava.nwidget.NWidgetPanelFactory;
import org.scijava.plugin.Plugin;
import org.scijava.struct.StructInstance;
import org.scijava.widget.UIComponent;

@Plugin(type = NWidgetPanelFactory.class)
public class NSwingWidgetPanelFactory implements
	NWidgetPanelFactory<NSwingWidget>
{

	@Override
	public <C> NWidgetPanel<C> create(final StructInstance<C> struct,
		final List<? extends NSwingWidget> widgets)
	{
		return new WidgetPanel<>(struct, widgets);
	}

	@Override
	public Class<NSwingWidget> widgetType() {
		return NSwingWidget.class;
	}

	// -- Helper classes --

	public class WidgetPanel<C> extends NAbstractWidgetPanel<C> implements
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

			for (final NSwingWidget widget : widgets) {
				// add widget to panel
				final String label = NWidgets.label(widget);
				if (label != null) {
					// widget is prefixed by a label
					final JLabel l = new JLabel(label);
					final String desc = NWidgets.description(widget);
					if (desc != null && !desc.isEmpty()) l.setToolTipText(desc);
					panel.add(l);
					panel.add(widget.getComponent());
				}
				else {
					// widget occupies entire row
					getComponent().add(widget.getComponent(), "span");
				}
			}

			return panel;
		}

		@Override
		public Class<JPanel> getComponentType() {
			return JPanel.class;
		}
	}
}
