package org.scijava.swing.widget;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.scijava.ValidityException;
import org.scijava.widget.NAbstractWidget;
import org.scijava.widget.NObjectWidget;
import org.scijava.widget.NWidgetFactory;
import org.scijava.widget.NWidgetPanel;
import org.scijava.widget.NWidgetPanelFactory;
import org.scijava.widget.NWidgetService;
import org.scijava.widget.swing.NSwingWidgetPanelFactory.WidgetPanel;
import org.scijava.object.ObjectService;
import org.scijava.param.ParameterStructs;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.StructInstance;

@Plugin(type = NWidgetFactory.class)
public class SwingObjectWidgetFactory implements SwingWidgetFactory {

	@Parameter
	private NWidgetService widgetService;

	@Parameter
	private ObjectService objectService;

	@Override
	public boolean supports(final MemberInstance<?> model) {
		return !choices(model).isEmpty();
	}

	@Override
	public NSwingWidget create(final MemberInstance<?> memberInstance,
		final NWidgetPanelFactory<? extends NSwingWidget> panelFactory)
	{
		return new Widget<>(memberInstance, panelFactory);
	}

	// -- Helper methods --

	private <T> List<T> choices(MemberInstance<T> model) {
		// FIXME: probably want a more dynamic way for a member instance to specify
		// its multiple choice options. Maybe add API to MemberInstance for that?
		return objectService.getObjects(model.member().getRawType());
	}

	// -- Helper classes --

	private class Widget<C> extends NAbstractWidget implements NSwingWidget,
		NObjectWidget
	{

		private JPanel panel;
		private List<JPanel> subPanels;
		private MemberInstance<C> typedModel;

		private NWidgetPanelFactory<? extends NSwingWidget> panelFactory;

		public Widget(final MemberInstance<C> model,
			NWidgetPanelFactory<? extends NSwingWidget> panelFactory)
		{
			super(model);
			this.typedModel = model;
			this.panelFactory = panelFactory;
		}

		@Override
		public JPanel getComponent() {
			if (panel != null) return panel;

			panel = new JPanel();
			panel.setLayout(new MigLayout("fillx,wrap 1", "[fill,grow]"));
			subPanels = new ArrayList<>();

			final List<C> choices = choices(typedModel);
			JComboBox<C> comboBox = new JComboBox<>();
			panel.add(comboBox);

			for (final C choice : choices) {
				comboBox.addItem(choice);
				try {
					final StructInstance<?> structInstance = //
						ParameterStructs.create(choice);
					 subPanels.add(createPanel(structInstance));
				}
				catch (final ValidityException exc) {
					// FIXME: Handle this.
				}
			}
			refreshSubPanel(comboBox);
			
			comboBox.addItemListener(e -> {
				panel.remove(1);
				refreshSubPanel(comboBox);
				panel.validate();
				panel.repaint();
			});

			return panel;
		}

		private void refreshSubPanel(final JComboBox<?> comboBox) {
			final int index = comboBox.getSelectedIndex();
			panel.add(subPanels.get(index));
			
		}

		private <S> JPanel createPanel(final StructInstance<S> structInstance) {
			final NWidgetPanel<S> widgetPanel = widgetService.createPanel(
				structInstance, panelFactory);
			if (!(widgetPanel instanceof SwingWidgetPanelFactory.WidgetPanel))
				throw new IllegalStateException("OMGWTF");
			final WidgetPanel<S> swingWidgetPanel =
				(SwingWidgetPanelFactory.WidgetPanel<S>) widgetPanel;
			return swingWidgetPanel.getComponent();
		}
	}
}
