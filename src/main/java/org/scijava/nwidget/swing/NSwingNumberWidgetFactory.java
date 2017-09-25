package org.scijava.nwidget.swing;

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
import org.scijava.nwidget.NAbstractWidget;
import org.scijava.nwidget.NWidget;
import org.scijava.nwidget.NWidgetFactory;
import org.scijava.nwidget.NNumberWidget;
import org.scijava.param.ParameterMember;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.thread.ThreadService;
import org.scijava.ui.swing.widget.SpinnerNumberModelFactory;
import org.scijava.util.ClassUtils;

@Plugin(type = NWidgetFactory.class)
public class NSwingNumberWidgetFactory implements NSwingWidgetFactory {

	@Parameter
	private ConvertService convertService;

	@Parameter
	private ThreadService threadService;

	@Override
	public boolean supports(final MemberInstance<?> model) {
		Member<?> member = model.member();
		return ClassUtils.isNumber(member.getRawType()) &&
			member instanceof ParameterMember;
	}

	@Override
	public NWidget create(final MemberInstance<?> model) {
		return new Widget(model);
	}

	// -- Helper classes --

	private class Widget extends NAbstractWidget implements NSwingWidget,
		NNumberWidget, AdjustmentListener, ChangeListener
	{

		private final ParameterMember<?> pMember;

		private JPanel panel;
		private JScrollBar scrollBar;
		private JSlider slider;
		private JSpinner spinner;

		public Widget(final MemberInstance<?> model) {
			super(model);
			pMember = (ParameterMember<?>) model.member();
		}

		// TODO - migrate these to some abstract base class w/ ParameterMember?
		@Override
		public String getLabel() {
			final String label = pMember.getLabel();
			if (label != null && !label.isEmpty()) return label;

			final String name = pMember.getKey();
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		@Override
		public String getDescription() {
			return pMember.getDescription();
		}

		@Override
		public JPanel getComponent() {
			if (panel != null) return panel;

			panel = new JPanel();
			final MigLayout layout = new MigLayout("fillx,ins 3 0 3 0",
				"[fill,grow|pref]");
			panel.setLayout(layout);

			final Number min = number(pMember.getMinimumValue(), null);
			final Number max = number(pMember.getMaximumValue(), null);
			final Number softMin = number(pMember.getSoftMinimum(), min);
			final Number softMax = number(pMember.getSoftMaximum(), max);
			final Number stepSize = number(pMember.getStepSize(), 1);

			// add optional widgets, if specified
			if (isStyle(SCROLL_BAR_STYLE)) {
				int smx = softMax.intValue();
				if (smx < Integer.MAX_VALUE) smx++;
				scrollBar =
					new JScrollBar(Adjustable.HORIZONTAL, softMin.intValue(), 1, softMin
						.intValue(), smx);
				scrollBar.setUnitIncrement(stepSize.intValue());
				setToolTip(scrollBar);
				getComponent().add(scrollBar);
				scrollBar.addAdjustmentListener(this);
			}
			else if (isStyle(SLIDER_STYLE)) {
				slider =
					new JSlider(softMin.intValue(), softMax.intValue(), softMin.intValue());
				slider.setMajorTickSpacing((softMax.intValue() - softMin.intValue()) / 4);
				slider.setMinorTickSpacing(stepSize.intValue());
				slider.setPaintLabels(true);
				slider.setPaintTicks(true);
				setToolTip(slider);
				getComponent().add(slider);
				slider.addChangeListener(this);
			}

			// add spinner widget
			final Class<?> type = pMember.getRawType();
			final Number v = getValue();
			final Number value = v == null ? 0 : v;
			final SpinnerNumberModel spinnerModel =
				new SpinnerNumberModelFactory().createModel(value, min, max, stepSize);
			spinner = new JSpinner(spinnerModel);
			fixSpinner(type);
			setToolTip(spinner);
			panel.add(spinner);
			limitWidth(200);
			spinner.addChangeListener(this);

			spinner.setValue(getValue());
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
			final Number converted = convertService.convert(value, Number.class);
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
				int value = getValue().intValue();
				if (value < slider.getMinimum()) value = slider.getMinimum();
				else if (value > slider.getMaximum()) value = slider.getMaximum();
				slider.removeChangeListener(this);
				slider.setValue(value);
				slider.addChangeListener(this);
			}
			if (scrollBar != null) {
				// clamp value within scroll bar bounds
				int value = getValue().intValue();
				if (value < scrollBar.getMinimum()) value = scrollBar.getMinimum();
				else if (value > scrollBar.getMaximum()) value = scrollBar.getMaximum();
				scrollBar.removeAdjustmentListener(this);
				scrollBar.setValue(getValue().intValue());
				scrollBar.addAdjustmentListener(this);
			}
		}

		private boolean isStyle(final String style) {
			final String widgetStyle = pMember.getWidgetStyle();
			if (widgetStyle == null) return style == null;
			for (final String s : widgetStyle.split(",")) {
				if (s.equals(style)) return true;
			}
			return false;
		}

		private Number getValue() {
			final Number value = (Number) model().get();

			// TODO: Decide whether to do this here.
//			if (isMultipleChoice()) return ensureValidChoice(value);
//			if (getObjectPool().size() > 0) return ensureValidObject(value);
			return value == null ? 0 : value;
		}

		/** Assigns the model's description as the given component's tool tip. */
		private void setToolTip(final JComponent c) {
			final String desc = pMember.getDescription();
			if (desc == null || desc.isEmpty()) return;
			c.setToolTipText(desc);
		}
	}
}
