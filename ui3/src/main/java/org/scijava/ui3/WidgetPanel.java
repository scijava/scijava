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
import java.util.Map;

/**
 * A widget that holds other widgets: a whole dialog, or one box within it.
 * <p>
 * NB: a panel is itself a {@link Widget}, which is what lets a group nest
 * inside another panel with no special case - a dialog is a list of widgets,
 * some of which happen to contain more.
 * </p>
 *
 * @param <W> the kind of widget this panel holds
 * @author Curtis Rueden
 */
public interface WidgetPanel<W extends Widget> extends Widget {

	/** Gets the widgets in this panel, in display order. */
	List<W> widgets();

	@Override
	default void refresh() {
		widgets().forEach(Widget::refresh);
	}

	/**
	 * Marks the parameters the model reports problems with, and clears the
	 * marks on the rest.
	 * <p>
	 * NB: a panel decides how a problem looks - a red label, an icon, a tooltip
	 * - but that there is something to show, and that it is shown per
	 * parameter and cleared when fixed, is the same everywhere.
	 * </p>
	 *
	 * @param problems what is wrong, by parameter name, as
	 *          {@link org.scijava.harvest.ParameterModel#problems()} reports
	 */
	default void showProblems(final Map<String, String> problems) {
		for (final Widget widget : widgets()) {
			if (widget instanceof WidgetPanel) {
				((WidgetPanel<?>) widget).showProblems(problems);
			}
		}
	}
}
