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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.List;

import javax.swing.JSpinner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.harvest.ParameterModel;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanel;
import org.scijava.ui3.test.SampleCommands;
import org.scijava.ui3.test.WidgetConformance;

/**
 * Runs the shared conformance suite against the Swing widgets, and adds what
 * is Swing's own.
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class SwingWidgetsTest extends WidgetConformance<SwingWidget> {

	static boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

	private static final List<WidgetFactory<SwingWidget>> FACTORIES = List.of( //
		new SwingMessageWidgetFactory(), //
		new SwingChoiceWidgetFactory(), //
		new SwingNumberWidgetFactory(), //
		new SwingToggleWidgetFactory(), //
		new SwingFileWidgetFactory(), //
		new SwingTextWidgetFactory());

	@Override
	protected WidgetPanel<SwingWidget> panel(final ParameterModel model) {
		return build(model, FACTORIES, new SwingPanelFactory(model));
	}

	@Override
	protected Class<?> widgetType(final Kind kind) {
		switch (kind) {
			case MESSAGE:
				return SwingMessageWidget.class;
			case TEXT:
				return SwingTextWidget.class;
			case NUMBER:
				return SwingNumberWidget.class;
			case TOGGLE:
				return SwingToggleWidget.class;
			case CHOICE:
				return SwingChoiceWidget.class;
			case FILE:
				return SwingFileWidget.class;
			case PANEL:
				return SwingPanel.class;
			default:
				throw new IllegalArgumentException(String.valueOf(kind));
		}
	}

	/**
	 * Driving the actual control runs the callback, which is the path a user
	 * takes and the one a mocked-out test would miss.
	 */
	@Test
	public void testTypingInTheControlRunsCallback() {
		final ParameterModel model = model(new SampleCommands.LinkedValues());
		final SwingPanel panel = (SwingPanel) panel(model);
		final SwingNumberWidget celsius = assertInstanceOf(SwingNumberWidget.class,
			panel.widgets().get(0));

		spinner(celsius.component()).setValue(100.0);

		assertEquals(212.0, model.get("fahrenheit"));
	}

	/** Finds the spinner inside a widget's components. */
	private static JSpinner spinner(final Container container) {
		for (final Component child : container.getComponents()) {
			if (child instanceof JSpinner) return (JSpinner) child;
			if (child instanceof Container) {
				final JSpinner found = spinner((Container) child);
				if (found != null) return found;
			}
		}
		return null;
	}

	/** The panel puts a label beside a widget that wants one, and not else. */
	@Test
	public void testLabeling() {
		final SwingPanel panel = (SwingPanel) panel(model(
			new SampleCommands.KitchenSink()));
		assertEquals(false, panel.widgets().get(0).isLabeled()); // the message
		assertEquals(true, panel.widgets().get(1).isLabeled()); // the name
	}
}
