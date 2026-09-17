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

import org.scijava.context.Plugin;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.priority.Priority;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanelFactory;
import org.scijava.ui3.Widgets;

/**
 * Makes {@link SwingMessageWidget}s.
 *
 * @author Curtis Rueden
 */
@Plugin(type = WidgetFactory.class)
public class SwingMessageWidgetFactory implements SwingWidgetFactory {

	@Override
	public boolean supports(final ParameterNode node) {
		return Widgets.isStyle(node, SwingMessageWidget.MESSAGE);
	}

	@Override
	public SwingWidget create(final ParameterNode node,
		final ParameterModel model, final WidgetPanelFactory<SwingWidget> panels)
	{
		return new SwingMessageWidget(node, model);
	}

	@Override
	public double priority() {
		// NB: an explicit "show this, do not collect it" outranks the type.
		return Priority.VERY_HIGH;
	}
}
