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

import javafx.scene.Node;
import javafx.scene.control.Label;

import org.scijava.context.Plugin;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.priority.Priority;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanelFactory;
import org.scijava.ui3.Widgets;

/**
 * A widget that shows text rather than collecting it.
 *
 * @author Curtis Rueden
 */
public class FxMessage extends FxWidget {

	private final Label label = new Label();

	public FxMessage(final ParameterNode node, final ParameterModel model) {
		super(node, model);
		label.setWrapText(true);
		refresh();
	}

	@Override
	public Node control() {
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

	/** Makes {@link FxMessage}s. */
	@Plugin(type = WidgetFactory.class)
	public static class Factory implements FxWidgetFactory {

		@Override
		public boolean supports(final ParameterNode node) {
			return Widgets.isStyle(node, "message");
		}

		@Override
		public FxWidget create(final ParameterNode node, final ParameterModel model,
			final WidgetPanelFactory<FxWidget> panels)
		{
			return new FxMessage(node, model);
		}

		@Override
		public double priority() {
			return Priority.VERY_HIGH;
		}
	}
}
