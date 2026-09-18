/*
 * #%L
 * Sample commands and conformance tests, for every toolkit binding.
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

package org.scijava.ui3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.scijava.context.Access;
import org.scijava.execute.Executables;
import org.scijava.harvest.ParameterModel;
import org.scijava.ui3.Widget;
import org.scijava.ui3.WidgetPanel;
import org.scijava.ui3.WidgetPanels;

/**
 * What every toolkit binding must do, asserted once.
 * <p>
 * A binding extends this and says how to make its widgets; the assertions are
 * about the contracts, so a binding that passes them behaves like the others
 * wherever it matters. Anything a binding does <em>beyond</em> this - which
 * control it chooses, how it lays a panel out - belongs in its own tests.
 * </p>
 *
 * @param <W> the binding's widget type
 * @author Curtis Rueden
 */
public abstract class WidgetConformance<W extends Widget> {

	/** Builds the widgets for the given model, as this toolkit makes them. */
	protected abstract WidgetPanel<W> panel(ParameterModel model);

	/**
	 * Gets the kind of widget this binding uses for the given parameter kind,
	 * so that the shared tests can name types without knowing the toolkit's.
	 */
	protected abstract Class<?> widgetType(Kind kind);

	/** The kinds of widget every binding provides. */
	public enum Kind {
			MESSAGE, TEXT, NUMBER, TOGGLE, CHOICE, FILE, PANEL
	}

	/** Every parameter type gets the kind of widget it should. */
	@Test
	public void testWidgetPerParameterType() {
		final WidgetPanel<W> panel = panel(model(new SampleCommands.KitchenSink()));
		assertEquals(List.of( //
			widgetType(Kind.MESSAGE), // message
			widgetType(Kind.TEXT), // name
			widgetType(Kind.NUMBER), // percent
			widgetType(Kind.NUMBER), // iterations
			widgetType(Kind.NUMBER), // weight
			widgetType(Kind.CHOICE), // method, an enum
			widgetType(Kind.NUMBER), // boxed, an Integer rather than an int
			widgetType(Kind.TEXT), // exact, a BigDecimal no spinner can step
			widgetType(Kind.TEXT), // initial, a char
			widgetType(Kind.TOGGLE), // enabled
			widgetType(Kind.CHOICE), // statistic
			widgetType(Kind.FILE), // input
			widgetType(Kind.FILE), // outputDir
			widgetType(Kind.TEXT), // secret
			widgetType(Kind.TEXT)), // notes
			types(panel));
	}

	/** An enum offers its own constants, with no annotation. */
	@Test
	public void testEnumChoices() {
		final ParameterModel model = model(new SampleCommands.KitchenSink());
		assertEquals(List.of(SampleCommands.Method.values()), panel(model)
			.widgets().get(5).node().choices());
	}

	/** A group appears, with its own widgets, when a value asks for it. */
	@Test
	public void testGroupAppearsWhenToggled() {
		final ParameterModel model = model(new SampleCommands.AdvancedToggle());
		assertEquals(2, panel(model).widgets().size());

		model.set("advanced", true);

		final WidgetPanel<W> panel = panel(model);
		assertEquals(3, panel.widgets().size());
		final W group = panel.widgets().get(2);
		assertFalse(group.isLabeled(), "a group carries its own title");
		final WidgetPanel<?> nested = assertInstanceOf(WidgetPanel.class, group);
		assertEquals(List.of(widgetType(Kind.NUMBER), widgetType(Kind.TOGGLE)),
			types(nested));
	}

	/** The number of widgets follows a value, not the class's fields. */
	@Test
	public void testGeneratedGroupGrows() {
		final ParameterModel model = model(new SampleCommands.GeneratedGroup());
		assertEquals(2, nested(panel(model)).widgets().size());

		model.set("numDims", 4);

		assertEquals(4, nested(panel(model)).widgets().size());
	}

	/**
	 * A widget reads its node out of the current tree, not the one it was built
	 * from, so computed choices stay in step even when nothing was rebuilt.
	 */
	@Test
	public void testWidgetSeesRecomputedChoices() {
		final ParameterModel model = model(new SampleCommands.ComputedChoices());
		final WidgetPanel<W> panel = panel(model);
		final W column = panel.widgets().get(1);
		assertEquals(List.of("X", "Y", "Z", "Channel"), column.node().choices());

		model.set("file", "table.csv");

		// NB: the same widget object, never rebuilt.
		assertEquals(List.of("id", "label", "area", "mean"), column.node()
			.choices());
		panel.refresh();
		assertEquals("id", model.get("column"));
	}

	/** A widget writes through the model, so callbacks run. */
	@Test
	public void testEditRunsCallback() {
		final ParameterModel model = model(new SampleCommands.LockAspectRatio());
		panel(model); // NB: the widgets exist and are listening

		model.set("width", 400);
		assertEquals(300, model.get("height"));

		model.set("lock", false);
		model.set("width", 800);
		assertEquals(300, model.get("height"));
	}

	/** Problems are reported per parameter, and clear when fixed. */
	@Test
	public void testValidation() {
		final ParameterModel model = model(new SampleCommands.LiveValidation());
		final WidgetPanel<W> panel = panel(model);
		assertTrue(model.problems().isEmpty());

		model.set("count", 42);
		// NB: showing them must not throw, whatever a binding does to show them.
		panel.showProblems(model.problems());
		assertEquals("Count must be between 1 and 10", model.problems().get(
			"count"));

		model.set("count", 3);
		panel.showProblems(model.problems());
		assertTrue(model.problems().isEmpty());
	}

	/** A file parameter is rendered, rather than falling through to text. */
	@Test
	public void testFileParameter() {
		final ParameterModel model = model(new SampleCommands.KitchenSink());
		final W input = panel(model).widgets().get(11);
		assertEquals(File.class, org.scijava.ui3.Widgets.type(input.node()));
		assertEquals(widgetType(Kind.FILE), input.getClass());
	}

	// -- Internal methods --

	/** Builds the model for a sample command, as a container would. */
	protected static ParameterModel model(final Runnable object) {
		return new ParameterModel(Executables.executableOf(object, Access.lookupIn(
			object.getClass())).create());
	}

	/** Builds the widgets, using the toolkit's own panel factory. */
	protected static <W extends Widget> WidgetPanel<W> build(
		final ParameterModel model, final List<? extends org.scijava.ui3.WidgetFactory<W>> factories,
		final org.scijava.ui3.WidgetPanelFactory<W> panels)
	{
		@SuppressWarnings("unchecked")
		final WidgetPanel<W> panel = (WidgetPanel<W>) new WidgetPanels<>(List.copyOf(
			factories), panels).build(model);
		return panel;
	}

	private WidgetPanel<?> nested(final WidgetPanel<W> panel) {
		return panel.widgets().stream() //
			.filter(WidgetPanel.class::isInstance) //
			.map(w -> (WidgetPanel<?>) w) //
			.findFirst().orElseThrow();
	}

	private static List<Class<?>> types(final WidgetPanel<?> panel) {
		return panel.widgets().stream().map(Object::getClass) //
			.collect(Collectors.toList());
	}
}
