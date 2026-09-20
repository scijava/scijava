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

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;

/**
 * A widget for a true/false value.
 *
 * @author Curtis Rueden
 */
public class SwingToggleWidget extends SwingWidget {

	private final JCheckBox checkBox;

	public SwingToggleWidget(final ParameterNode node,
		final ParameterModel model)
	{
		super(node, model);
		checkBox = new JCheckBox();
		checkBox.setOpaque(false);
		refresh();
		checkBox.addItemListener(e -> update(checkBox.isSelected()));
	}

	@Override
	public JComponent component() {
		return checkBox;
	}

	@Override
	protected void doRefresh() {
		final Object value = value();
		final boolean selected = value instanceof Boolean && (Boolean) value;
		if (checkBox.isSelected() != selected) checkBox.setSelected(selected);
	}
}
