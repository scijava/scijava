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

/**
 * The bookkeeping every widget does, whatever toolkit it is made of.
 * <p>
 * NB: this is here because three bindings wrote it identically. Two would have
 * been a coincidence; three is the shape of the problem - a widget must find
 * its node in the <em>current</em> tree, must write through the model rather
 * than to the parameter, and must not mistake its own refresh for the user
 * typing. None of that is a statement about controls.
 * </p>
 *
 * @author Curtis Rueden
 */
public abstract class AbstractWidget implements Widget {

	private final ParameterNode node;
	private final ParameterModel model;
	private final String key;

	private boolean refreshing;

	/**
	 * @param node the parameter to edit, or null for a panel standing for no
	 *          parameter
	 * @param model the values to read and write
	 */
	protected AbstractWidget(final ParameterNode node,
		final ParameterModel model)
	{
		this.node = node;
		this.model = model;
		this.key = node == null ? null : node.member() //
			.map(m -> m.member().key()).orElse(null);
	}

	@Override
	public ParameterNode node() {
		if (key == null) return node; // NB: a group, or the whole dialog
		// NB: the tree is rebuilt whenever a value changes, so the node handed to
		// this widget at construction goes stale - its choices in particular,
		// which a computed `choicesFrom` resolves afresh each time. Reading the
		// node back out of the current tree is what keeps a chooser showing the
		// values the other widgets now imply.
		return model.tree().find(key).orElse(node);
	}

	/** Gets the values this widget reads and writes. */
	public ParameterModel model() {
		return model;
	}

	@Override
	public final void refresh() {
		// NB: writing a value into a control fires its listeners, which would
		// write it straight back into the model and, where a callback is
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

	/** Re-reads the parameter's value into the controls. */
	protected abstract void doRefresh();

	/** Gets the parameter's name. */
	protected String key() {
		if (key == null) throw new IllegalStateException("Not a parameter widget");
		return key;
	}

	/** Gets the parameter's current value. */
	protected Object value() {
		return model.get(key());
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
}
