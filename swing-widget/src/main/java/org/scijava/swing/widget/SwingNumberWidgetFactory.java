package org.scijava.swing.widget;

import java.awt.Adjustable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParsePosition;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.scijava.convert.ConvertService;
import org.scijava.widget.AbstractWidget;
import org.scijava.widget.NumberWidget;
import org.scijava.widget.Widget;
import org.scijava.widget.WidgetFactory;
import org.scijava.widget.WidgetPanelFactory;
import org.scijava.widget.Widgets;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.MemberInstance;
import org.scijava.thread.ThreadService;
import org.scijava.ui.swing.widget.SpinnerNumberModelFactory;
import org.scijava.util.ClassUtils;

@Plugin(type = WidgetFactory.class)
public class SwingNumberWidgetFactory implements SwingWidgetFactory {

	@Parameter
	private ConvertService convertService;

	@Parameter
	private ThreadService threadService;

	@Override
	public boolean supports(final MemberInstance<?> model) {
		return ClassUtils.isNumber(model.member().getRawType());
	}

	@Override
	public SwingWidget create(final MemberInstance<?> model,
		final WidgetPanelFactory<? extends SwingWidget> panelFactory)
	{
		return new Widget(model);
	}

	// -- Helper classes --

	private class Widget extends AbstractWidget implements SwingWidget,
		NumberWidget, AdjustmentListener, ChangeListener
	{

		private JPanel panel;
		private JScrollBar scrollBar;
		private JSlider slider;
		private JSpinner spinner;

		public Widget(final MemberInstance<?> model) {
			super(model);
		}

		@Override
		public JPanel getComponent() {
			if (panel != null) return panel;

			panel = new JPanel();
			final MigLayout layout = new MigLayout("fillx,ins 3 0 3 0",
				"[fill,grow|pref]");
			panel.setLayout(layout);

			final Number min = number(Widgets.minimum(this), null);
			final Number max = number(Widgets.maximum(this), null);
			final Number softMin = number(Widgets.softMinimum(this), min);
			final Number softMax = number(Widgets.softMaximum(this), max);
			final Number stepSize = number(Widgets.stepSize(this), 1);

			// add optional widgets, if specified
			if (Widgets.isStyle(this, SCROLL_BAR_STYLE)) {
				int smx = softMax.intValue();
				if (smx < Integer.MAX_VALUE) smx++;
				scrollBar =
					new JScrollBar(Adjustable.HORIZONTAL, softMin.intValue(), 1, softMin
						.intValue(), smx);
				scrollBar.setUnitIncrement(stepSize.intValue());
				SwingWidgets.setToolTip(this, scrollBar);
				getComponent().add(scrollBar);
				scrollBar.addAdjustmentListener(this);
			}
			else if (Widgets.isStyle(this, SLIDER_STYLE)) {
				slider =
					new JSlider(softMin.intValue(), softMax.intValue(), softMin.intValue());
				slider.setMajorTickSpacing((softMax.intValue() - softMin.intValue()) / 4);
				slider.setMinorTickSpacing(stepSize.intValue());
				slider.setPaintLabels(true);
				slider.setPaintTicks(true);
				SwingWidgets.setToolTip(this, slider);
				getComponent().add(slider);
				slider.addChangeListener(this);
			}

			// add spinner widget
			final Class<?> type = model().member().getRawType();
			final Number v = modelValue();
			final Number value = v == null ? 0 : v;
			final SpinnerNumberModel spinnerModel =
				new SpinnerNumberModelFactory().createModel(value, min, max, stepSize);
			spinner = new JSpinner(spinnerModel);
			fixSpinner(type);
			SwingWidgets.setToolTip(this, spinner);
			panel.add(spinner);
			limitWidth(200);
			spinner.addChangeListener(this);

			spinner.setValue(modelValue());
			syncSliders();

			return panel;
		}

		// -- AdjustmentListener methods --

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent e) {
			// sync spinner with scroll bar value
			final int value = scrollBar.getValue();
			spinner.setValue(value);
		}

		// -- ChangeListener methods --

		@Override
		public void stateChanged(final ChangeEvent e) {
			final Object source = e.getSource();
			if (source == slider) {
				// sync spinner with slider value
				final int value = slider.getValue();
				spinner.setValue(value);
			}
			else if (source == spinner) {
				// sync slider and/or scroll bar with spinner value
				syncSliders();
			}
			model().set(spinner.getValue());
		}

		// -- Helper methods --

		private Number number(final Object value, final Number defaultValue) {
			// FIXME: discuss best way forward
			// Right now, the widgets do not have the T of their model
			// Should they? It is nice that they don't, mostly.
			// But here, would be handy to have a utility method that converts from
			// the given value to one compatible with this widget's model.
			// How deep should this conversion logic live.
			final Class<?> modelType = model().member().getRawType();
			final Number converted = //
				(Number) convertService.convert(value, modelType);
			return converted == null ? defaultValue : converted;
		}

		/**
		 * Limit component width to a certain maximum. This is a HACK to work around
		 * an issue with Double-based spinners that attempt to size themselves very
		 * large (presumably to match Double.MAX_VALUE).
		 */
		private void limitWidth(final int maxWidth) {
			final Dimension minSize = spinner.getMinimumSize();
			if (minSize.width > maxWidth) {
				minSize.width = maxWidth;
				spinner.setMinimumSize(minSize);
			}
			final Dimension prefSize = spinner.getPreferredSize();
			if (prefSize.width > maxWidth) {
				prefSize.width = maxWidth;
				spinner.setPreferredSize(prefSize);
			}
		}

		/** Improves behavior of the {@link JSpinner} widget. */
		private void fixSpinner(final Class<?> type) {
			fixSpinnerType(type);
			fixSpinnerFocus();
		}

		/**
		 * Fixes spinners that display {@link BigDecimal} or {@link BigInteger}
		 * values. This is a HACK to work around the fact that
		 * {@link DecimalFormat#parse(String, ParsePosition)} uses {@link Double}
		 * and/or {@link Long} by default, hence losing precision.
		 */
		private void fixSpinnerType(final Class<?> type) {
			if (!BigDecimal.class.isAssignableFrom(type) &&
					!BigInteger.class.isAssignableFrom(type))
			{
				return;
			}
			final JComponent editor = spinner.getEditor();
			final JSpinner.NumberEditor numberEditor = (JSpinner.NumberEditor) editor;
			final DecimalFormat decimalFormat = numberEditor.getFormat();
			decimalFormat.setParseBigDecimal(true);
		}

		/**
		 * Tries to ensure that the text of a {@link JSpinner} becomes selected when
		 * it first receives the focus.
		 * <p>
		 * Adapted from <a href="http://stackoverflow.com/q/20971050">this SO
		 * post</a>.
		 */
		private void fixSpinnerFocus() {
			for (final Component c : spinner.getEditor().getComponents()) {
				if (!(c instanceof JTextField)) continue;
				final JTextField textField = (JTextField) c;

				textField.addFocusListener(new FocusListener() {

					@Override
					public void focusGained(final FocusEvent e) {
						queueSelection();
					}

					@Override
					public void focusLost(final FocusEvent e) {
						queueSelection();
					}

					private void queueSelection() {
						threadService.queue(new Runnable() {

							@Override
							public void run() {
								textField.selectAll();
							}
						});
					}

				});
			}
		}

		/** Sets slider values to match the spinner. */
		private void syncSliders() {
			if (slider != null) {
				// clamp value within slider bounds
				int value = modelValue().intValue();
				if (value < slider.getMinimum()) value = slider.getMinimum();
				else if (value > slider.getMaximum()) value = slider.getMaximum();
				slider.removeChangeListener(this);
				slider.setValue(value);
				slider.addChangeListener(this);
			}
			if (scrollBar != null) {
				// clamp value within scroll bar bounds
				int value = modelValue().intValue();
				if (value < scrollBar.getMinimum()) value = scrollBar.getMinimum();
				else if (value > scrollBar.getMaximum()) value = scrollBar.getMaximum();
				scrollBar.removeAdjustmentListener(this);
				scrollBar.setValue(modelValue().intValue());
				scrollBar.addAdjustmentListener(this);
			}
		}

		private Number modelValue() {
			final Number value = (Number) model().get();

			// TODO: Decide whether to do this here.
//			if (isMultipleChoice()) return ensureValidChoice(value);
//			if (getObjectPool().size() > 0) return ensureValidObject(value);
			return value == null ? 0 : value;
		}
	}
}
