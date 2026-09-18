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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.harvest.ParameterModel;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanel;
import org.scijava.ui3.test.SampleCommands;
import org.scijava.ui3.test.WidgetConformance;

/**
 * Runs the shared conformance suite against the JavaFX widgets.
 * <p>
 * It is the same suite the Swing binding runs, which is the point: the
 * assertions are about the contracts, so two bindings passing them behave
 * alike wherever a caller can tell.
 * </p>
 * <p>
 * NB: JavaFX controls cannot be constructed at all before the toolkit is
 * running, so every widget here is built on the JavaFX thread.
 * </p>
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class FxWidgetsTest extends WidgetConformance<FxWidget> {

	/**
	 * NB: not {@code GraphicsEnvironment.isHeadless()} - that is AWT's answer,
	 * and this module does not read {@code java.desktop} at all. Whether JavaFX
	 * can start is a question only JavaFX can answer.
	 */
	static boolean isHeadless() {
		try {
			FxThread.start();
			return false;
		}
		catch (final Throwable t) {
			return true;
		}
	}

	private static final List<WidgetFactory<FxWidget>> FACTORIES = List.of( //
		new FxMessage.Factory(), //
		new FxChoice.Factory(), //
		new FxNumber.Factory(), //
		new FxToggle.Factory(), //
		new FxFile.Factory(), //
		new FxText.Factory());

	@Override
	protected WidgetPanel<FxWidget> panel(final ParameterModel model) {
		return FxThread.get(() -> build(model, FACTORIES, new FxPanel.Factory(
			model)));
	}

	@Override
	protected Class<?> widgetType(final Kind kind) {
		switch (kind) {
			case MESSAGE:
				return FxMessage.class;
			case TEXT:
				return FxText.class;
			case NUMBER:
				return FxNumber.class;
			case TOGGLE:
				return FxToggle.class;
			case CHOICE:
				return FxChoice.class;
			case FILE:
				return FxFile.class;
			case PANEL:
				return FxPanel.class;
			default:
				throw new IllegalArgumentException(String.valueOf(kind));
		}
	}

	/** A widget's own update path runs the callback, not only the model's. */
	@Test
	public void testWidgetEditRunsCallback() {
		final ParameterModel model = model(new SampleCommands.LinkedValues());
		final FxPanel panel = (FxPanel) panel(model);
		final FxNumber celsius = (FxNumber) panel.widgets().get(0);

		FxThread.runAndWait(() -> celsius.update(100.0));

		assertEquals(212.0, model.get("fahrenheit"));
	}

	/** Accelerators read the same notation the Swing binding accepts. */
	@Test
	public void testAccelerators() {
		final KeyCombination shortcutO = FxMenus.accelerator("^O");
		assertNotNull(shortcutO);
		assertEquals(new KeyCharacterCombination("O", KeyCombination.SHORTCUT_DOWN),
			shortcutO);
		assertEquals(new KeyCharacterCombination("C", KeyCombination.SHORTCUT_DOWN,
			KeyCombination.SHIFT_DOWN), FxMenus.accelerator("^+C"));
		assertEquals(KeyCombination.keyCombination("Ctrl+Shift+N"), FxMenus
			.accelerator("Ctrl+Shift+N"));
	}
}
