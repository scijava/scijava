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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;

/**
 * Turns the shape a dialog should have into the widgets that show it.
 * <p>
 * The walk is the same for every toolkit - descend the tree, ask the
 * highest-priority factory that accepts each parameter, wrap each group in a
 * panel - so it lives here, and a toolkit supplies only the factories.
 * </p>
 *
 * @param <W> the kind of widget being built
 * @author Curtis Rueden
 */
public class WidgetPanels<W extends Widget> {

	private final List<WidgetFactory<W>> factories;
	private final WidgetPanelFactory<W> panels;

	/**
	 * @param factories the widgets available, of which the highest-priority one
	 *          accepting a parameter wins
	 * @param panels makes the panel for the dialog and for each group
	 */
	public WidgetPanels(final List<WidgetFactory<W>> factories,
		final WidgetPanelFactory<W> panels)
	{
		this.factories = new ArrayList<>(factories);
		this.factories.sort(Comparator.comparingDouble(WidgetFactory<W>::priority)
			.reversed());
		this.panels = panels;
	}

	/** Builds the widgets for the model's current shape. */
	public W build(final ParameterModel model) {
		return panels.create(null, widgets(model.tree().nodes(), model));
	}

	/**
	 * Builds the widget for one node: a panel if it is a group, otherwise
	 * whichever widget accepts the parameter.
	 *
	 * @return the widget, or null if no factory accepts the parameter
	 */
	public W build(final ParameterNode node, final ParameterModel model) {
		if (node.isGroup()) {
			return panels.create(node, widgets(node.children(), model));
		}
		for (final WidgetFactory<W> factory : factories) {
			if (factory.supports(node)) return factory.create(node, model, panels);
		}
		// NB: a parameter no widget accepts is left out rather than fatal, so
		// that an unusual type does not make the whole dialog unavailable.
		return null;
	}

	// -- Helper methods --

	private List<W> widgets(final List<ParameterNode> nodes,
		final ParameterModel model)
	{
		final List<W> widgets = new ArrayList<>();
		for (final ParameterNode node : nodes) {
			final W widget = build(node, model);
			if (widget != null) widgets.add(widget);
		}
		return widgets;
	}
}
