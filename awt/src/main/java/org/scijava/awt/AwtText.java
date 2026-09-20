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

import java.awt.Component;
import java.awt.TextArea;
import java.awt.TextComponent;
import java.awt.TextField;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.scijava.context.Plugin;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.priority.Priority;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanelFactory;
import org.scijava.ui3.Widgets;

/**
 * A widget for text: a field, an echo-masked field, or a multi-line area.
 *
 * @author Curtis Rueden
 */
public class AwtText extends AwtWidget {

	private final TextComponent text;

	public AwtText(final ParameterNode node, final ParameterModel model) {
		super(node, model);
		final int columns = Integer.parseInt(Widgets.styleValue(node, "columns")
			.orElse("16"));
		if (Widgets.isStyle(node, "text area")) {
			text = new TextArea("", 4, columns, TextArea.SCROLLBARS_VERTICAL_ONLY);
		}
		else {
			final TextField field = new TextField("", columns);
			// NB: AWT has no password field, only an echo character - which is
			// what a password field is, underneath.
			if (Widgets.isStyle(node, "password")) field.setEchoChar('*');
			text = field;
		}
		refresh();
		text.addTextListener(e -> changed(text.getText()));
	}

	@Override
	public Component component() {
		return text;
	}

	@Override
	protected void doRefresh() {
		final Object value = value();
		final String s = value == null ? "" : value.toString();
		if (!s.equals(text.getText())) text.setText(s);
	}

	// -- Helper methods --

	private void changed(final String s) {
		final Class<?> type = Widgets.box(Widgets.type(node()));
		if (type == String.class) update(s);
		else if (type == Character.class) {
			if (!s.isEmpty()) update(s.charAt(0));
		}
		else {
			final Number value = Widgets.toType(s, type);
			if (value != null) update(value);
		}
	}

	/** Makes {@link AwtText}s. */
	@Plugin(type = WidgetFactory.class)
	public static class Factory implements AwtWidgetFactory {

		@Override
		public boolean supports(final ParameterNode node) {
			if (!node.choices().isEmpty()) return false;
			final Class<?> type = Widgets.box(Widgets.type(node));
			return Widgets.isText(node) || type == BigInteger.class ||
				type == BigDecimal.class;
		}

		@Override
		public AwtWidget create(final ParameterNode node,
			final ParameterModel model, final WidgetPanelFactory<AwtWidget> panels)
		{
			return new AwtText(node, model);
		}

		@Override
		public double priority() {
			return Priority.LOW;
		}
	}
}
