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
import javax.swing.JPanel;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.Widget;

/**
 * A {@link Widget} made of Swing components.
 *
 * @author Curtis Rueden
 */
public abstract class SwingWidget implements Widget {

	private final ParameterNode node;
	private final ParameterModel model;

	private boolean refreshing;

	protected SwingWidget(final ParameterNode node, final ParameterModel model) {
		this.node = node;
		this.model = model;
	}

	/** Gets the component to place in the dialog. */
	public abstract JComponent component();

	/**
	 * Gets whether the dialog should put a label beside this widget. A widget
	 * that says no gets the whole row.
	 */
	public boolean isLabeled() {
		return true;
	}

	@Override
	public ParameterNode node() {
		return node;
	}

	/** Gets the values this widget reads and writes. */
	public ParameterModel model() {
		return model;
	}

	@Override
	public final void refresh() {
		// NB: writing a value into a Swing component fires its listeners, which
		// would write it straight back into the model and, where a callback is
		// involved, keep going. The flag is what stops a refresh from looking
		// like the user typing.
		refreshing = true;
		try {
			doRefresh();
		}
		finally {
			refreshing = false;
		}
	}

	// -- Internal methods --

	/** Re-reads the parameter's value into the components. */
	protected abstract void doRefresh();

	/** Gets the parameter's name. */
	protected String key() {
		return node.member().orElseThrow().member().key();
	}

	/** Gets the parameter's current value. */
	protected Object value() {
		return node.member().orElseThrow().get();
	}

	/**
	 * Reports that the user changed this widget's value.
	 * <p>
	 * It goes through the model rather than to the parameter directly, which is
	 * what runs the callback and, if the shape of the dialog depends on this
	 * value, rebuilds it.
	 * </p>
	 */
	protected void update(final Object value) {
		if (refreshing) return;
		model.set(key(), value);
	}

	/** Makes a panel with no padding, for a widget of several components. */
	protected static JPanel panel() {
		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		return panel;
	}
}
