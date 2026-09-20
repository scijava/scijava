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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.List;

import javax.swing.JSpinner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.harvest.ParameterModel;
import org.scijava.script3.ScriptExecutable;
import org.scijava.script3.Scripts;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanels;

/**
 * Tests that a script gets the same dialog a Java command gets.
 * <p>
 * This is the claim the whole stack rests on: the widgets know about
 * parameters, not about where the parameters came from. A script declares
 * them in its header rather than in annotated fields, and everything above
 * behaves identically - including callbacks, which SciJava Common could not
 * do for scripts at all.
 * </p>
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class ScriptWidgetsTest {

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

	/** A script's declared parameters get the widgets their types imply. */
	@Test
	public void testWidgetsForAScript() {
		final SwingPanel panel = panel(script("" + //
			"#@ String name\n" + //
			"#@ int(min = 0, max = 100, style = \"slider\") percent\n" + //
			"#@ boolean enabled\n" + //
			"#@ String(choices = \"Mean,Median,Max\") statistic\n" + //
			"#@ File input\n" + //
			"#@output String summary\n"));

		assertEquals(List.of( //
			SwingTextWidget.class, //
			SwingNumberWidget.class, //
			SwingToggleWidget.class, //
			SwingChoiceWidget.class, //
			SwingFileWidget.class), //
			panel.widgets().stream().map(Object::getClass).collect(java.util.stream
				.Collectors.toList()));
	}

	/** A script's label and bounds reach the widget, as a command's would. */
	@Test
	public void testScriptMetadata() {
		final SwingPanel panel = panel(script( //
			"#@ double(label = \"Blur radius\", min = 0, max = 20) sigma\n"));

		assertEquals("Blur radius", panel.widgets().get(0).node().label());
	}

	/** A script's callback runs when a widget writes through the model. */
	@Test
	public void testScriptCallbackFromAWidget() {
		final ParameterModel model = model(script("" + //
			"#@ double(callback = \"celsiusChanged\") celsius\n" + //
			"#@ double fahrenheit\n" + //
			"def celsiusChanged() {\n" + //
			"  fahrenheit = celsius * 9 / 5 + 32\n" + //
			"}\n"));
		final SwingPanel panel = (SwingPanel) new WidgetPanels<>(FACTORIES,
			new SwingPanelFactory(model)).build(model);
		final SwingNumberWidget celsius = assertInstanceOf(SwingNumberWidget.class,
			panel.widgets().get(0));

		// NB: through the actual control, which is the path a user takes.
		spinner(celsius.component()).setValue(100.0);

		assertEquals(212.0, model.get("fahrenheit"));
	}

	/** A script may compute its own choices, as a command may. */
	@Test
	public void testScriptComputedChoices() {
		final ParameterModel model = model(script("" + //
			"#@ String(choices = \"stack.tif,table.csv\", value = \"stack.tif\") file\n" + //
			"#@ String(choicesFrom = \"columns\") column\n" + //
			"def columns() {\n" + //
			"  file.endsWith(\".tif\") ? [\"X\", \"Y\", \"Z\"] : [\"id\", \"area\"]\n" + //
			"}\n"));

		assertEquals(List.of("X", "Y", "Z"), model.tree().find("column")
			.orElseThrow().choices());

		model.set("file", "table.csv");

		assertEquals(List.of("id", "area"), model.tree().find("column")
			.orElseThrow().choices());
	}

	/** A script's validator reports problems the same way. */
	@Test
	public void testScriptValidation() {
		final ParameterModel model = model(script("" + //
			"#@ int(validator = \"checkCount\") count\n" + //
			"def checkCount(value) {\n" + //
			"  value > 0 && value <= 10 ? null : \"Count must be between 1 and 10\"\n" + //
			"}\n"));

		model.set("count", 5);
		assertTrue(model.problems().isEmpty());

		model.set("count", 42);
		assertEquals("Count must be between 1 and 10", model.problems().get(
			"count"));
	}

	// -- Helper methods --

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

	private static ScriptExecutable script(final String code) {
		return Scripts.get().of("test.groovy", code, "groovy");
	}

	private static ParameterModel model(final ScriptExecutable script) {
		return new ParameterModel(script.create());
	}

	private static SwingPanel panel(final ScriptExecutable script) {
		final ParameterModel model = model(script);
		return (SwingPanel) new WidgetPanels<>(FACTORIES, new SwingPanelFactory(
			model)).build(model);
	}
}
