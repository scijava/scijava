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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.event.TextEvent;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.harvest.ParameterModel;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanel;
import org.scijava.ui3.test.SampleCommands;
import org.scijava.ui3.test.WidgetConformance;

/**
 * Runs the shared conformance suite against the AWT widgets.
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class AwtWidgetsTest extends WidgetConformance<AwtWidget> {

	static boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

	private static final List<WidgetFactory<AwtWidget>> FACTORIES = List.of( //
		new AwtMessage.Factory(), //
		new AwtChoice.Factory(), //
		new AwtNumber.Factory(), //
		new AwtToggle.Factory(), //
		new AwtFile.Factory(), //
		new AwtText.Factory());

	@Override
	protected WidgetPanel<AwtWidget> panel(final ParameterModel model) {
		return build(model, FACTORIES, new AwtPanel.Factory(model));
	}

	@Override
	protected Class<?> widgetType(final Kind kind) {
		switch (kind) {
			case MESSAGE:
				return AwtMessage.class;
			case TEXT:
				return AwtText.class;
			case NUMBER:
				return AwtNumber.class;
			case TOGGLE:
				return AwtToggle.class;
			case CHOICE:
				return AwtChoice.class;
			case FILE:
				return AwtFile.class;
			case PANEL:
				return AwtPanel.class;
			default:
				throw new IllegalArgumentException(String.valueOf(kind));
		}
	}

	/**
	 * Typing in the field runs the callback, which is the path a user takes.
	 * <p>
	 * NB: AWT has no spinner, so the number widget <em>is</em> a text field -
	 * and the same conformance suite passes regardless.
	 * </p>
	 */
	@Test
	public void testTypingInTheControlRunsCallback() {
		final ParameterModel model = model(new SampleCommands.LinkedValues());
		final AwtPanel panel = (AwtPanel) panel(model);
		final AwtNumber celsius = (AwtNumber) panel.widgets().get(0);

		type(celsius, "100.0");

		assertEquals(212.0, model.get("fahrenheit"));
	}

	/** Half-typed text is not a value: a lone minus sign writes nothing. */
	@Test
	public void testPartialInputIsNotAValue() {
		final ParameterModel model = model(new SampleCommands.LinkedValues());
		final AwtPanel panel = (AwtPanel) panel(model);
		final AwtNumber celsius = (AwtNumber) panel.widgets().get(0);

		type(celsius, "-");

		assertEquals(0.0, model.get("celsius"));
		assertEquals(32.0, model.get("fahrenheit"));
	}

	/** A value outside the declared bounds is clamped, not accepted. */
	@Test
	public void testBoundsWithoutASpinner() {
		final ParameterModel model = model(new SampleCommands.KitchenSink());
		final AwtPanel panel = (AwtPanel) panel(model);
		final AwtNumber percent = (AwtNumber) panel.widgets().get(2);

		type(percent, "500");

		assertEquals(100, model.get("percent"));
		assertTrue(model.problems().isEmpty());
	}

	// -- Helper methods --

	/**
	 * Types the given text into a widget's field.
	 * <p>
	 * NB: {@link TextComponent#setText} does <em>not</em> notify text listeners
	 * in AWT, where a Swing document would. So the test says what typing says:
	 * new text, and an event to go with it.
	 * </p>
	 */
	private static void type(final AwtWidget widget, final String text) {
		final TextField field = field(widget.component());
		field.setText(text);
		field.dispatchEvent(new TextEvent(field, TextEvent.TEXT_VALUE_CHANGED));
	}

	/** Finds the text field inside a widget's components. */
	private static TextField field(final Component component) {
		if (component instanceof TextField) return (TextField) component;
		if (component instanceof Container) {
			for (final Component child : ((Container) component).getComponents()) {
				final TextField found = field(child);
				if (found != null) return found;
			}
		}
		return null;
	}
}
