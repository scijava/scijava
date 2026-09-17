/*
 * #%L
 * Swing widgets, and a dialog to harvest inputs with them.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.Widgets;

/**
 * A widget for a number: a spinner, optionally beside a slider or a scroll
 * bar.
 * <p>
 * The spinner enforces the parameter's hard {@code min} and {@code max}; the
 * slider spans its {@code softMin} and {@code softMax}, which is how a
 * parameter whose useful range is far narrower than its permitted one stays
 * draggable.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SwingNumberWidget extends SwingWidget {

	/** Style hint: put a slider beside the spinner. */
	public static final String SLIDER = "slider";

	/** Style hint: put a scroll bar beside the spinner. */
	public static final String SCROLL_BAR = "scroll bar";

	/** Style hint: mark the slider's scale. Takes a value: {@code ticks:10}. */
	public static final String TICKS = "ticks";

	private final Class<?> type;
	private final Number min;
	private final Number max;
	private final Number softMin;
	private final Number softMax;
	private final Number step;

	private final JSpinner spinner;
	private final JSlider slider;
	private final JScrollBar scrollBar;
	private final JPanel component;

	private boolean syncing;

	public SwingNumberWidget(final ParameterNode node,
		final ParameterModel model)
	{
		super(node, model);
		type = Widgets.box(Widgets.type(node));
		min = Widgets.min(node);
		max = Widgets.max(node);
		softMin = Widgets.softMin(node);
		softMax = Widgets.softMax(node);
		step = Widgets.stepSize(node);

		spinner = new JSpinner(spinnerModel());
		final Dimension size = spinner.getPreferredSize();
		size.width = Math.max(size.width, 80);
		spinner.setPreferredSize(size);

		component = panel();
		component.setLayout(new BorderLayout(4, 0));

		final boolean scalable = softMin != null && softMax != null;
		slider = scalable && Widgets.isStyle(node, SLIDER) ? slider() : null;
		scrollBar = slider == null && scalable && Widgets.isStyle(node, SCROLL_BAR)
			? scrollBar() : null;
		final JComponent scale = slider != null ? slider : scrollBar;
		if (scale != null) component.add(scale, BorderLayout.CENTER);
		component.add(spinner, scale == null ? BorderLayout.CENTER
			: BorderLayout.EAST);

		refresh();
		spinner.addChangeListener(e -> {
			syncScale();
			update(number(spinner.getValue()));
		});
		if (slider != null) slider.addChangeListener(e -> {
			if (!syncing) spinner.setValue(number(fromScale(slider.getValue())));
		});
		if (scrollBar != null) scrollBar.addAdjustmentListener(e -> {
			if (!syncing) spinner.setValue(number(fromScale(scrollBar.getValue())));
		});
	}

	@Override
	public JComponent component() {
		return component;
	}

	@Override
	protected void doRefresh() {
		final Number value = number(value());
		if (!value.equals(spinner.getValue())) spinner.setValue(value);
		syncScale();
	}

	// -- Helper methods --

	private SpinnerNumberModel spinnerModel() {
		final Number value = number(value());
		// NB: SpinnerNumberModel steps in the type of the value it holds, so
		// handing it a value of the parameter's own type is what keeps an int
		// parameter integral.
		return new SpinnerNumberModel(value, (Comparable<?>) min,
			(Comparable<?>) max, step);
	}

	private JSlider slider() {
		final JSlider s = new JSlider(0, steps());
		s.setOpaque(false);
		// NB: the marks come first, because they make the slider taller: asking
		// for its preferred height before adding them clips the labels.
		Widgets.styleValue(node(), TICKS).ifPresent(count -> label(s, count));
		s.setPreferredSize(new Dimension(160, s.getPreferredSize().height));
		return s;
	}

	private JScrollBar scrollBar() {
		final JScrollBar s = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, steps() +
			1);
		s.setPreferredSize(new Dimension(160, s.getPreferredSize().height));
		return s;
	}

	/** Marks the slider's scale, with the values the marks stand for. */
	private void label(final JSlider s, final String count) {
		final int ticks;
		try {
			ticks = Math.max(1, Integer.parseInt(count));
		}
		catch (final NumberFormatException exc) {
			return; // NB: an unreadable hint is no reason to lose the slider
		}
		final int spacing = Math.max(1, steps() / ticks);
		s.setMajorTickSpacing(spacing);
		s.setPaintTicks(true);
		final Hashtable<Integer, JLabel> labels = new Hashtable<>();
		for (int i = 0; i <= ticks; i++) {
			final int pos = Math.min(steps(), i * spacing);
			labels.put(pos, new JLabel(String.valueOf(number(fromScale(pos)))));
		}
		s.setLabelTable(labels);
		s.setPaintLabels(true);
	}

	/** Gets how many steps the scale spans, capped at a usable number. */
	private int steps() {
		final double span = softMax.doubleValue() - softMin.doubleValue();
		final double stepped = span / step.doubleValue();
		// NB: a double parameter with a tiny step would ask for billions of
		// slider positions; the spinner beside it stays exact either way.
		return (int) Math.max(1, Math.min(stepped, 10_000));
	}

	private void syncScale() {
		if (slider == null && scrollBar == null) return;
		syncing = true;
		try {
			final double value = number(spinner.getValue()).doubleValue();
			final double span = softMax.doubleValue() - softMin.doubleValue();
			final int pos = (int) Math.round((value - softMin.doubleValue()) / span *
				steps());
			final int clamped = Math.max(0, Math.min(steps(), pos));
			if (slider != null) slider.setValue(clamped);
			if (scrollBar != null) scrollBar.setValue(clamped);
		}
		finally {
			syncing = false;
		}
	}

	private double fromScale(final int pos) {
		final double span = softMax.doubleValue() - softMin.doubleValue();
		return softMin.doubleValue() + span * pos / steps();
	}

	/** Reads a value as the parameter's own numeric type. */
	private Number number(final Object value) {
		if (value == null) {
			if (min != null) return number(min);
			return (Number) Widgets.toType("0", type);
		}
		final Number n = value instanceof Number ? (Number) value //
			: Widgets.toType(String.valueOf(value), type);
		if (n == null) return (Number) Widgets.toType("0", type);
		if (type == Byte.class) return n.byteValue();
		if (type == Short.class) return n.shortValue();
		if (type == Integer.class) return n.intValue();
		if (type == Long.class) return n.longValue();
		if (type == Float.class) return n.floatValue();
		return n.doubleValue();
	}
}
