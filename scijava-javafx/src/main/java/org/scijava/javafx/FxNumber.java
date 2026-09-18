/*
 * #%L
 * JavaFX widgets, and a dialog to harvest inputs with them.
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

package org.scijava.javafx;

import java.math.BigDecimal;
import java.math.BigInteger;

import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import org.scijava.context.Plugin;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanelFactory;
import org.scijava.ui3.Widgets;

/**
 * A widget for a number: a spinner, optionally beside a slider or scroll bar.
 * <p>
 * NB: JavaFX has no equivalent of {@code SpinnerNumberModel}'s type
 * preservation - its factories are {@code Integer} or {@code Double} and
 * nothing else - so the widget converts on the way out. This is the kind of
 * difference a second binding is for: the same parameter metadata, a different
 * amount of work to honor it.
 * </p>
 *
 * @author Curtis Rueden
 */
public class FxNumber extends FxWidget {

	private final Class<?> type;
	private final Number min;
	private final Number max;
	private final Number softMin;
	private final Number softMax;
	private final Number step;

	private final Spinner<Double> spinner;
	private final Slider slider;
	private final ScrollBar scrollBar;
	private final HBox box;

	private boolean syncing;

	public FxNumber(final ParameterNode node, final ParameterModel model) {
		super(node, model);
		type = Widgets.box(Widgets.type(node));
		min = Widgets.min(node);
		max = Widgets.max(node);
		softMin = Widgets.softMin(node);
		softMax = Widgets.softMax(node);
		step = Widgets.stepSize(node);

		spinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory( //
			min == null ? -Double.MAX_VALUE : min.doubleValue(), //
			max == null ? Double.MAX_VALUE : max.doubleValue(), //
			number(value()).doubleValue(), step.doubleValue()));
		spinner.setEditable(true);
		spinner.setPrefWidth(110);

		final boolean scalable = softMin != null && softMax != null;
		slider = scalable && Widgets.isStyle(node, "slider") ? slider() : null;
		scrollBar = slider == null && scalable && Widgets.isStyle(node,
			"scroll bar") ? scrollBar() : null;
		final Node scale = slider != null ? slider : scrollBar;

		box = new HBox(4);
		if (scale != null) {
			box.getChildren().add(scale);
			HBox.setHgrow(scale, Priority.ALWAYS);
		}
		box.getChildren().add(spinner);

		refresh();
		spinner.valueProperty().addListener((obs, old, value) -> {
			syncScale();
			update(number(value));
		});
		// NB: an editable JavaFX spinner does not commit what was typed unless
		// the user presses Enter, so a value typed and then tabbed away from
		// would be silently lost.
		spinner.getEditor().textProperty().addListener((obs, old, text) -> {
			final Number typed = Widgets.toType(text, Double.class);
			if (typed != null) update(number(typed));
		});
		if (slider != null) slider.valueProperty().addListener((obs, old, pos) -> {
			if (!syncing) spinner.getValueFactory().setValue(fromScale(pos
				.doubleValue()));
		});
		if (scrollBar != null) scrollBar.valueProperty().addListener((obs, old,
			pos) -> {
			if (!syncing) spinner.getValueFactory().setValue(fromScale(pos
				.doubleValue()));
		});
	}

	@Override
	public Node control() {
		return box;
	}

	@Override
	protected void doRefresh() {
		final double value = number(value()).doubleValue();
		if (spinner.getValue() == null || spinner.getValue() != value) {
			spinner.getValueFactory().setValue(value);
		}
		syncScale();
	}

	// -- Helper methods --

	private Slider slider() {
		final Slider s = new Slider(softMin.doubleValue(), softMax.doubleValue(),
			number(value()).doubleValue());
		s.setPrefWidth(160);
		Widgets.styleValue(node(), "ticks").ifPresent(count -> {
			try {
				final int ticks = Math.max(1, Integer.parseInt(count));
				s.setMajorTickUnit((softMax.doubleValue() - softMin.doubleValue()) /
					ticks);
				s.setMinorTickCount(0);
				s.setShowTickMarks(true);
				s.setShowTickLabels(true);
			}
			catch (final NumberFormatException exc) {
				// NB: an unreadable hint is no reason to lose the slider.
			}
		});
		return s;
	}

	private ScrollBar scrollBar() {
		final ScrollBar s = new ScrollBar();
		s.setMin(softMin.doubleValue());
		s.setMax(softMax.doubleValue());
		s.setUnitIncrement(step.doubleValue());
		s.setPrefWidth(160);
		return s;
	}

	private void syncScale() {
		if (slider == null && scrollBar == null) return;
		syncing = true;
		try {
			final double value = spinner.getValue() == null ? 0 : spinner.getValue();
			final double clamped = Math.max(softMin.doubleValue(), Math.min(softMax
				.doubleValue(), value));
			if (slider != null) slider.setValue(clamped);
			if (scrollBar != null) scrollBar.setValue(clamped);
		}
		finally {
			syncing = false;
		}
	}

	/** Snaps a position on the scale to a whole number of steps. */
	private double fromScale(final double pos) {
		final double steps = Math.round((pos - softMin.doubleValue()) / step
			.doubleValue());
		return softMin.doubleValue() + steps * step.doubleValue();
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

	/** Makes {@link FxNumber}s. */
	@Plugin(type = WidgetFactory.class)
	public static class Factory implements FxWidgetFactory {

		@Override
		public boolean supports(final ParameterNode node) {
			if (!node.choices().isEmpty() || !Widgets.isNumber(node)) return false;
			final Class<?> type = Widgets.box(Widgets.type(node));
			return type != BigInteger.class && type != BigDecimal.class;
		}

		@Override
		public FxWidget create(final ParameterNode node, final ParameterModel model,
			final WidgetPanelFactory<FxWidget> panels)
		{
			return new FxNumber(node, model);
		}
	}
}
