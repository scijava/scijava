/*
 * #%L
 * AWT widgets and platform plumbing, with no Swing anywhere.
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

package org.scijava.awt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.scijava.context.Plugin;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanelFactory;
import org.scijava.ui3.Widgets;

/**
 * A widget for a number: a text field, optionally beside a scroll bar.
 * <p>
 * NB: AWT has no spinner and no slider, only {@link Scrollbar}. This is the
 * harshest test of the contracts so far - the widget must honor {@code min},
 * {@code max}, {@code stepSize} and the soft bounds with nothing but a text
 * field and integer scroll positions - and none of it needed the model to
 * change. A scale in steps, rather than in the parameter's own units, is what
 * lets a scroll bar drive a {@code double}.
 * </p>
 *
 * @author Curtis Rueden
 */
public class AwtNumber extends AwtWidget {

	private final Class<?> type;
	private final Number min;
	private final Number max;
	private final Number softMin;
	private final Number softMax;
	private final Number step;

	private final TextField field = new TextField(8);
	private final Scrollbar scrollBar;
	private final Panel panel = new Panel(new BorderLayout(4, 0));

	private boolean syncing;

	public AwtNumber(final ParameterNode node, final ParameterModel model) {
		super(node, model);
		type = Widgets.box(Widgets.type(node));
		min = Widgets.min(node);
		max = Widgets.max(node);
		softMin = Widgets.softMin(node);
		softMax = Widgets.softMax(node);
		step = Widgets.stepSize(node);

		final boolean scalable = softMin != null && softMax != null && //
			(Widgets.isStyle(node, "slider") || Widgets.isStyle(node, "scroll bar"));
		scrollBar = scalable ? new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, steps() +
			1) : null;
		if (scrollBar != null) panel.add(scrollBar, BorderLayout.CENTER);
		panel.add(field, scrollBar == null ? BorderLayout.CENTER
			: BorderLayout.EAST);

		refresh();
		field.addTextListener(e -> typed());
		if (scrollBar != null) {
			scrollBar.addAdjustmentListener(e -> {
				if (syncing) return;
				final Number value = number(fromScale(scrollBar.getValue()));
				field.setText(String.valueOf(value));
				update(value);
			});
		}
	}

	@Override
	public Component component() {
		return panel;
	}

	@Override
	protected void doRefresh() {
		final Number value = number(value());
		final String text = String.valueOf(value);
		if (!text.equals(field.getText())) field.setText(text);
		syncScale(value);
	}

	// -- Helper methods --

	private void typed() {
		final Number typed = Widgets.toType(field.getText(), type);
		// NB: half-typed text is not a value: "-" and "" are how a number is
		// entered, not a reason to write a zero into the parameter.
		if (typed == null) return;
		final Number clamped = clamp(typed);
		syncScale(clamped);
		update(clamped);
	}

	private Number clamp(final Number value) {
		if (min != null && value.doubleValue() < min.doubleValue()) return min;
		if (max != null && value.doubleValue() > max.doubleValue()) return max;
		return value;
	}

	/** Gets how many steps the scale spans, capped at a usable number. */
	private int steps() {
		final double span = softMax.doubleValue() - softMin.doubleValue();
		return (int) Math.max(1, Math.min(span / step.doubleValue(), 10_000));
	}

	private void syncScale(final Number value) {
		if (scrollBar == null) return;
		syncing = true;
		try {
			final double span = softMax.doubleValue() - softMin.doubleValue();
			final int pos = (int) Math.round((value.doubleValue() - softMin
				.doubleValue()) / span * steps());
			scrollBar.setValue(Math.max(0, Math.min(steps(), pos)));
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
		if (value == null) return min != null ? number(min) : (Number) Widgets
			.toType("0", type);
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

	/** Makes {@link AwtNumber}s. */
	@Plugin(type = WidgetFactory.class)
	public static class Factory implements AwtWidgetFactory {

		@Override
		public boolean supports(final ParameterNode node) {
			if (!node.choices().isEmpty() || !Widgets.isNumber(node)) return false;
			final Class<?> type = Widgets.box(Widgets.type(node));
			return type != BigInteger.class && type != BigDecimal.class;
		}

		@Override
		public AwtWidget create(final ParameterNode node,
			final ParameterModel model, final WidgetPanelFactory<AwtWidget> panels)
		{
			return new AwtNumber(node, model);
		}
	}
}
