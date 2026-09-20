/*
 * #%L
 * Toolkit-agnostic contracts for widgets and input harvesting.
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

package org.scijava.ui3;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.priority.Priority;

/**
 * Creates widgets of one kind, for the parameters it recognizes.
 * <p>
 * Contribute one as a plugin:
 * </p>
 *
 * <pre>
 * &#64;Plugin(type = WidgetFactory.class)
 * public class SwingNumberWidgetFactory implements WidgetFactory&lt;SwingWidget&gt; { ... }
 * </pre>
 * <p>
 * Where several factories accept a parameter, the highest priority wins - so a
 * specific widget for a particular type can outrank a general one.
 * </p>
 *
 * @param <W> the kind of widget this factory makes, which is how a toolkit
 *          keeps its own widgets together
 * @author Curtis Rueden
 */
public interface WidgetFactory<W extends Widget> {

	/** Gets whether this factory can make a widget for the given parameter. */
	boolean supports(ParameterNode node);

	/**
	 * Makes a widget for the given parameter.
	 *
	 * @param node the parameter to edit
	 * @param model the values to read and write; setting through it is what
	 *          runs callbacks and reshapes the dialog
	 * @param panels makes panels, for a widget whose parameter contains others
	 * @return the new widget
	 */
	W create(ParameterNode node, ParameterModel model,
		WidgetPanelFactory<W> panels);

	/** Gets the kind of widget this factory makes. */
	Class<W> widgetType();

	/** Sorts factories that accept the same parameter. See {@link Priority}. */
	default double priority() {
		return Priority.NORMAL;
	}
}
