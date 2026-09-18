/*
 * #%L
 * Presenting SciJava Common's modules as SciJava3 commands.
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

package org.scijava.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.command3.CommandInfo;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.Widgets;

/**
 * Tests that a legacy command produces a SciJava3 dialog model.
 * <p>
 * This is where the bridge's claim ends, and deliberately: a command written
 * years ago against the old annotations, in a jar nobody is going to
 * recompile, described well enough that any toolkit can render it - labels,
 * bounds, slider style, choices - with its callbacks firing when a value
 * changes. That a correct model becomes correct widgets is established by the
 * bindings' shared conformance suite, in Swing, JavaFX and AWT alike.
 * </p>
 *
 * @author Curtis Rueden
 */
public class LegacyDialogTest {

	private static LegacyContext legacy;

	@BeforeAll
	public static void startLegacyContext() {
		// NB: no SciJava Common type is named here, which is the point:
		// LegacyContext keeps them out of the bridge's signatures, so a module
		// using the bridge need not `requires org.scijava`.
		legacy = LegacyContext.start();
	}

	@AfterAll
	public static void disposeLegacyContext() {
		if (legacy != null) legacy.close();
	}

	/** The dialog shows the parameters, and only the parameters. */
	@Test
	public void testTheDialogsShape() {
		final ParameterModel model = model("LegacyBlur");

		assertEquals(List.of("Blur radius", "Edges", "Input"), model.tree().nodes()
			.stream().map(ParameterNode::label).collect(Collectors.toList()));
	}

	/** Legacy widget metadata survives, in the form a widget asks for. */
	@Test
	public void testWidgetMetadata() {
		final ParameterModel model = model("LegacyBlur");
		final ParameterNode sigma = model.tree().find("sigma").orElseThrow();

		assertEquals(0.0, Widgets.min(sigma));
		assertEquals(20.0, Widgets.max(sigma));
		assertEquals(0.5, Widgets.stepSize(sigma));
		assertTrue(Widgets.isStyle(sigma, "slider"), "the legacy widget style");
		assertTrue(Widgets.isNumber(sigma));

		final ParameterNode edges = model.tree().find("edges").orElseThrow();
		assertEquals(List.of("Reflect", "Zero", "Wrap"), edges.choices());
	}

	/** A legacy callback fires when the dialog writes a value. */
	@Test
	public void testCallbackThroughTheDialogModel() {
		final ParameterModel model = model("LegacyTemperature");

		model.set("celsius", 100.0);

		assertEquals(212.0, model.get("fahrenheit"));
	}

	/** A legacy validator reports problems the SciJava3 way. */
	@Test
	public void testLegacyValidator() {
		final ParameterModel model = model("LegacyValidation");

		model.set("count", 5);
		assertTrue(model.problems().isEmpty(), model.problems().toString());

		model.set("count", 42);
		assertEquals("Count must be between 1 and 10", model.problems().get(
			"count"));
	}

	/** A DynamicCommand's run-time parameters are in the dialog too. */
	@Test
	public void testDynamicParametersInTheDialog() {
		final ParameterModel model = model("LegacyDynamic");

		assertEquals(List.of("Name", "Added at run time"), model.tree().nodes()
			.stream().map(ParameterNode::label).collect(Collectors.toList()));
	}

	// -- Helper methods --

	private static ParameterModel model(final String simpleName) {
		final CommandInfo command = legacy.commands().stream() //
			.filter(c -> c.name().endsWith("$" + simpleName)) //
			.findFirst().orElseThrow(() -> new AssertionError("Not found: " +
				simpleName));
		return new ParameterModel(command.create());
	}
}
