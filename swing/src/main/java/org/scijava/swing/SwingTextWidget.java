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
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.Widgets;

/**
 * A widget for text: a field, a password field, or a multi-line area.
 *
 * @author Curtis Rueden
 */
public class SwingTextWidget extends SwingWidget implements DocumentListener {

	/** Style hint: hide what the user types. */
	public static final String PASSWORD = "password";

	/** Style hint: give the user several lines. */
	public static final String AREA = "text area";

	private final JTextComponent text;
	private final JComponent component;

	public SwingTextWidget(final ParameterNode node,
		final ParameterModel model)
	{
		super(node, model);
		final int columns = Integer.parseInt(Widgets.styleValue(node, "columns")
			.orElse("16"));
		if (Widgets.isStyle(node, AREA)) {
			final JTextArea area = new JTextArea(5, columns);
			area.setLineWrap(true);
			area.setWrapStyleWord(true);
			text = area;
			component = new JScrollPane(area);
		}
		else if (Widgets.isStyle(node, PASSWORD)) {
			text = new JPasswordField(columns);
			component = text;
		}
		else {
			text = new JTextField(columns);
			component = text;
		}
		refresh();
		text.getDocument().addDocumentListener(this);
	}

	@Override
	public JComponent component() {
		return component;
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		changed();
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		changed();
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		changed();
	}

	@Override
	protected void doRefresh() {
		final Object value = value();
		final String s = value == null ? "" : value.toString();
		if (!s.equals(text.getText())) text.setText(s);
	}

	// -- Helper methods --

	private void changed() {
		final String s = text.getText();
		final Class<?> type = Widgets.box(Widgets.type(node()));
		if (type == String.class) {
			update(s);
		}
		else if (type == Character.class) {
			if (!s.isEmpty()) update(s.charAt(0));
		}
		else {
			// NB: BigInteger and BigDecimal land here, a spinner being unable to
			// step them. Half-typed text is not a value, so an unparseable field
			// leaves the parameter alone rather than writing a zero into it.
			final Number value = Widgets.toType(s, type);
			if (value != null) update(value);
		}
	}
}
