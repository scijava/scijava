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

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.Widget;

/**
 * A {@link Widget} made of JavaFX controls.
 * <p>
 * NB: deliberately a near-copy of {@code org.scijava.swing.SwingWidget}, and
 * that is the finding: what a binding has to write is the toolkit-specific
 * part and a few lines of bookkeeping, not any part of the model.
 * </p>
 *
 * @author Curtis Rueden
 */
public abstract class FxWidget implements Widget {

	private final ParameterNode node;
	private final ParameterModel model;
	private final String key;

	private boolean refreshing;

	protected FxWidget(final ParameterNode node, final ParameterModel model) {
		this.node = node;
		this.model = model;
		this.key = node == null ? null : node.member() //
			.map(m -> m.member().key()).orElse(null);
	}

	/** Gets the control to place in the dialog. */
	public abstract Node control();

	@Override
	public boolean isLabeled() {
		return true;
	}

	@Override
	public ParameterNode node() {
		if (key == null) return node; // NB: a group, or the whole dialog
		// NB: the tree is rebuilt on every change, so the node handed to this
		// widget at construction goes stale; the current one is the one to ask.
		return model.tree().find(key).orElse(node);
	}

	/** Gets the values this widget reads and writes. */
	public ParameterModel model() {
		return model;
	}

	@Override
	public final void refresh() {
		// NB: writing a value into a control fires its listeners, which would
		// write it straight back into the model. The flag is what stops a
		// refresh from looking like the user typing.
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

	/** Reports that the user changed this widget's value. */
	protected void update(final Object value) {
		if (refreshing) return;
		model.set(key(), value);
	}
}
