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

import java.awt.Choice;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.scijava.context.Plugin;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.priority.Priority;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanelFactory;

/**
 * A widget for a parameter with a known set of values, declared or computed.
 * <p>
 * NB: {@link Choice} holds strings and nothing else, so the widget keeps the
 * values beside it and maps by position. A toolkit with no model layer is
 * where a contract that assumed one would show.
 * </p>
 *
 * @author Curtis Rueden
 */
public class AwtChoice extends AwtWidget {

	private final Choice choice = new Choice();
	private final List<Object> values = new ArrayList<>();

	public AwtChoice(final ParameterNode node, final ParameterModel model) {
		super(node, model);
		refresh();
		choice.addItemListener(e -> {
			final int index = choice.getSelectedIndex();
			if (index >= 0 && index < values.size()) update(values.get(index));
		});
	}

	@Override
	public Component component() {
		return choice;
	}

	@Override
	protected void doRefresh() {
		final List<Object> choices = node().choices();
		if (!choices.equals(values)) {
			choice.removeAll();
			values.clear();
			for (final Object item : choices) {
				values.add(item);
				choice.add(String.valueOf(item));
			}
		}
		final Object value = value();
		final int index = indexOf(value);
		if (index >= 0 && choice.getSelectedIndex() != index) {
			choice.select(index);
		}
	}

	// -- Helper methods --

	/** Finds the position a value stands at, declared choices being strings. */
	private int indexOf(final Object value) {
		for (int i = 0; i < values.size(); i++) {
			if (Objects.equals(values.get(i), value)) return i;
		}
		for (int i = 0; i < values.size(); i++) {
			if (String.valueOf(values.get(i)).equals(String.valueOf(value))) return i;
		}
		return -1;
	}

	/** Makes {@link AwtChoice}s. */
	@Plugin(type = WidgetFactory.class)
	public static class Factory implements AwtWidgetFactory {

		@Override
		public boolean supports(final ParameterNode node) {
			return !node.choices().isEmpty();
		}

		@Override
		public AwtWidget create(final ParameterNode node,
			final ParameterModel model, final WidgetPanelFactory<AwtWidget> panels)
		{
			return new AwtChoice(node, model);
		}

		@Override
		public double priority() {
			return Priority.HIGH;
		}
	}
}
