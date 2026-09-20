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

import java.util.List;

import org.scijava.harvest.ParameterNode;

/**
 * Makes panels to hold widgets.
 * <p>
 * NB: a factory is handed to {@link WidgetFactory#create}, which is what lets
 * a widget build a panel <em>inside</em> itself. That is how a parameter
 * containing other parameters - a chosen implementation, a settings object, a
 * generated group - is rendered without the dialog knowing the shape in
 * advance.
 * </p>
 *
 * @param <W> the kind of widget these panels hold
 * @author Curtis Rueden
 */
public interface WidgetPanelFactory<W extends Widget> {

	/**
	 * Makes a panel holding the given widgets.
	 *
	 * @param group the node these widgets belong to, or null at the top level
	 * @param widgets what the panel holds
	 * @return the new panel, which is itself a widget
	 */
	W create(ParameterNode group, List<W> widgets);
}
