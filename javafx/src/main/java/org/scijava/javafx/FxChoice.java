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

import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;

import org.scijava.context.Plugin;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.priority.Priority;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanelFactory;

/**
 * A widget for a parameter with a known set of values, declared or computed.
 *
 * @author Curtis Rueden
 */
public class FxChoice extends FxWidget {

	private final ComboBox<Object> comboBox = new ComboBox<>();

	public FxChoice(final ParameterNode node, final ParameterModel model) {
		super(node, model);
		comboBox.setMaxWidth(Double.MAX_VALUE);
		refresh();
		comboBox.valueProperty().addListener((obs, old, value) -> {
			if (value != null) update(value);
		});
	}

	@Override
	public Node control() {
		return comboBox;
	}

	@Override
	protected void doRefresh() {
		final List<Object> choices = node().choices();
		if (!choices.equals(comboBox.getItems())) {
			comboBox.setItems(FXCollections.observableArrayList(choices));
		}
		final Object value = value();
		final Object selected = value == null ? null : asChoice(value, choices);
		if (!Objects.equals(comboBox.getValue(), selected)) {
			comboBox.setValue(selected);
		}
	}

	// -- Helper methods --

	/** Finds the choice a value stands for, declared choices being strings. */
	private Object asChoice(final Object value, final List<Object> choices) {
		for (final Object choice : choices) {
			if (Objects.equals(choice, value)) return choice;
		}
		for (final Object choice : choices) {
			if (String.valueOf(choice).equals(String.valueOf(value))) return choice;
		}
		return null;
	}

	/** Makes {@link FxChoice}s. */
	@Plugin(type = WidgetFactory.class)
	public static class Factory implements FxWidgetFactory {

		@Override
		public boolean supports(final ParameterNode node) {
			return !node.choices().isEmpty();
		}

		@Override
		public FxWidget create(final ParameterNode node, final ParameterModel model,
			final WidgetPanelFactory<FxWidget> panels)
		{
			return new FxChoice(node, model);
		}

		@Override
		public double priority() {
			return Priority.HIGH;
		}
	}
}
