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

import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;

/**
 * A widget for a parameter with a known set of values, declared or computed.
 * <p>
 * NB: it asks the node for its {@link ParameterNode#choices() choices} each
 * time it refreshes, so a parameter whose values depend on another - the axes
 * of the chosen image, the columns of the chosen table - simply works. In
 * SciJava Common this needed a {@code DynamicCommand}.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SwingChoiceWidget extends SwingWidget {

	private final JComboBox<Object> comboBox;

	public SwingChoiceWidget(final ParameterNode node,
		final ParameterModel model)
	{
		super(node, model);
		comboBox = new JComboBox<>();
		refresh();
		comboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				update(comboBox.getSelectedItem());
			}
		});
	}

	@Override
	public JComponent component() {
		return comboBox;
	}

	@Override
	protected void doRefresh() {
		final List<Object> choices = node().choices();
		if (!sameItems(choices)) {
			comboBox.setModel(new DefaultComboBoxModel<>(choices.toArray()));
		}
		final Object value = value();
		final Object selected = value == null ? null : asChoice(value, choices);
		if (!Objects.equals(comboBox.getSelectedItem(), selected)) {
			comboBox.setSelectedItem(selected);
		}
	}

	// -- Helper methods --

	private boolean sameItems(final List<Object> choices) {
		if (comboBox.getItemCount() != choices.size()) return false;
		for (int i = 0; i < choices.size(); i++) {
			if (!Objects.equals(comboBox.getItemAt(i), choices.get(i))) return false;
		}
		return true;
	}

	/**
	 * Finds the choice a value stands for.
	 * <p>
	 * NB: declared choices arrive as strings, while the parameter may hold a
	 * number or an enum constant, so matching on {@code toString} is what keeps
	 * the right item selected.
	 * </p>
	 */
	private Object asChoice(final Object value, final List<Object> choices) {
		for (final Object choice : choices) {
			if (Objects.equals(choice, value)) return choice;
		}
		for (final Object choice : choices) {
			if (String.valueOf(choice).equals(String.valueOf(value))) return choice;
		}
		return null;
	}
}
