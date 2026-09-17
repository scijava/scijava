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

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;

/**
 * A widget that shows text rather than collecting it: a heading, a note, a
 * warning that another value has just provoked.
 * <p>
 * The parameter's value is the message, so a callback can rewrite it.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SwingMessageWidget extends SwingWidget {

	/** Style hint: show this parameter rather than collecting it. */
	public static final String MESSAGE = "message";

	private final JLabel label;

	public SwingMessageWidget(final ParameterNode node,
		final ParameterModel model)
	{
		super(node, model);
		label = new JLabel();
		refresh();
	}

	@Override
	public JComponent component() {
		return label;
	}

	@Override
	public boolean isLabeled() {
		return false; // NB: the message is the whole row.
	}

	@Override
	protected void doRefresh() {
		final Object value = value();
		label.setText(value == null ? "" : value.toString());
	}
}
