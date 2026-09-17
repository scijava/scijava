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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.context.Access;
import org.scijava.execute.Executables;
import org.scijava.harvest.ParameterModel;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanels;

/**
 * Tests that the widgets a dialog builds match the parameters it was given,
 * and that they follow the parameters as values change.
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class SwingWidgetsTest {

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

	@Test
	public void testWidgetPerParameterType() {
		final ParameterModel model = model(new Demos.KitchenSink());
		final SwingPanel panel = panel(model);

		assertEquals(List.of( //
			SwingMessageWidget.class, // message
			SwingTextWidget.class, // name
			SwingNumberWidget.class, // percent
			SwingNumberWidget.class, // iterations
			SwingNumberWidget.class, // weight
			SwingChoiceWidget.class, // method, an enum
			SwingNumberWidget.class, // boxed, an Integer rather than an int
			SwingTextWidget.class, // exact, a BigDecimal a spinner cannot step
			SwingTextWidget.class, // initial, a char
			SwingToggleWidget.class, // enabled
			SwingChoiceWidget.class, // statistic
			SwingFileWidget.class, // input
			SwingFileWidget.class, // outputDir
			SwingTextWidget.class, // secret
			SwingTextWidget.class), // notes
			types(panel));
	}

	/** A group appears, with its own widgets, when the value asks for it. */
	@Test
	public void testGroupAppearsWhenToggled() {
		final ParameterModel model = model(new Demos.AdvancedToggle());
		assertEquals(2, panel(model).widgets().size());

		model.set("advanced", true);

		final SwingPanel panel = panel(model);
		assertEquals(3, panel.widgets().size());
		final SwingWidget group = panel.widgets().get(2);
		final SwingPanel nested = assertInstanceOf(SwingPanel.class, group);
		assertFalse(nested.isLabeled()); // NB: a group carries its own title
		assertEquals(List.of(SwingNumberWidget.class, SwingToggleWidget.class),
			types(nested));
	}

	/** The number of widgets follows a value, not the class's fields. */
	@Test
	public void testGeneratedGroupGrows() {
		final ParameterModel model = model(new Demos.GeneratedGroup());
		assertEquals(2, nested(panel(model)).widgets().size());

		model.set("numDims", 4);

		assertEquals(4, nested(panel(model)).widgets().size());
	}

	/** A choice widget offers whatever the other value implies, right now. */
	@Test
	public void testComputedChoicesFollowAnotherValue() {
		final ParameterModel model = model(new Demos.ComputedChoices());
		assertEquals(List.of("X", "Y", "Z", "Channel"), model.tree().find("column")
			.orElseThrow().choices());

		model.set("file", "table.csv");

		final SwingPanel panel = panel(model);
		final SwingChoiceWidget column = assertInstanceOf(SwingChoiceWidget.class,
			panel.widgets().get(1));
		assertEquals(List.of("id", "label", "area", "mean"), column.node()
			.choices());
	}

	/** An enum offers its own constants, with no annotation. */
	@Test
	public void testEnumChoices() {
		final ParameterModel model = model(new Demos.KitchenSink());
		final SwingChoiceWidget method = assertInstanceOf(SwingChoiceWidget.class,
			panel(model).widgets().get(5));
		assertEquals(List.of(Demos.Method.values()), method.node().choices());
	}

	/**
	 * A widget reads its node out of the current tree, not the one it was built
	 * from - so computed choices stay in step even when the dialog's shape has
	 * not changed and nothing was rebuilt.
	 */
	@Test
	public void testWidgetSeesRecomputedChoices() {
		final ParameterModel model = model(new Demos.ComputedChoices());
		final SwingPanel panel = panel(model);
		final SwingChoiceWidget column = assertInstanceOf(SwingChoiceWidget.class,
			panel.widgets().get(1));
		assertEquals(List.of("X", "Y", "Z", "Channel"), column.node().choices());

		model.set("file", "table.csv");

		// NB: the same widget object, never rebuilt.
		assertEquals(List.of("id", "label", "area", "mean"), column.node()
			.choices());
		panel.refresh();
		assertEquals("id", model.get("column"));
	}

	/** A third value decides whether two others move together. */
	@Test
	public void testLockedAspectRatio() {
		final ParameterModel model = model(new Demos.LockAspectRatio());

		model.set("width", 400);
		assertEquals(300, model.get("height"));

		model.set("lock", false);
		model.set("width", 800);
		assertEquals(300, model.get("height"));
	}

	/** A widget writes through the model, so callbacks run. */
	@Test
	public void testWidgetEditRunsCallback() {
		final ParameterModel model = model(new Demos.LinkedValues());
		final SwingPanel panel = panel(model);
		final SwingNumberWidget celsius = assertInstanceOf(SwingNumberWidget.class,
			panel.widgets().get(0));

		celsius.update(100.0);

		assertEquals(212.0, model.get("fahrenheit"));
	}

	/** Problems are reported per parameter, and clear when fixed. */
	@Test
	public void testValidationMarksTheOffendingParameter() {
		final ParameterModel model = model(new Demos.LiveValidation());
		final SwingPanel panel = panel(model);
		assertTrue(model.problems().isEmpty());

		model.set("count", 42);
		panel.showProblems(model.problems());
		assertEquals("Count must be between 1 and 10", model.problems().get(
			"count"));

		model.set("count", 3);
		assertTrue(model.problems().isEmpty());
	}

	// -- Helper methods --

	private static ParameterModel model(final Runnable object) {
		return new ParameterModel(Executables.executableOf(object, Access.lookupIn(
			object.getClass())).create());
	}

	private static SwingPanel panel(final ParameterModel model) {
		return (SwingPanel) new WidgetPanels<>(FACTORIES, new SwingPanelFactory(
			model)).build(model);
	}

	private static SwingPanel nested(final SwingPanel panel) {
		return panel.widgets().stream() //
			.filter(SwingPanel.class::isInstance).map(SwingPanel.class::cast) //
			.findFirst().orElseThrow();
	}

	private static List<Class<?>> types(final SwingPanel panel) {
		return panel.widgets().stream().map(Object::getClass) //
			.collect(Collectors.toList());
	}
}
