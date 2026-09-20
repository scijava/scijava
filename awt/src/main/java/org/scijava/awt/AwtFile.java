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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Window;
import java.io.File;

import org.scijava.context.Plugin;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanelFactory;
import org.scijava.ui3.Widgets;

/**
 * A widget for a file: a path to type, and a button to browse.
 * <p>
 * NB: {@link FileDialog} is the platform's own dialog, which is a point in
 * AWT's favor - and it cannot choose a directory on every platform, which is a
 * point against. A directory parameter therefore falls back to typing the
 * path, rather than pretending.
 * </p>
 *
 * @author Curtis Rueden
 */
public class AwtFile extends AwtWidget {

	private final TextField path = new TextField(16);
	private final Panel panel = new Panel(new BorderLayout(4, 0));

	public AwtFile(final ParameterNode node, final ParameterModel model) {
		super(node, model);
		final Button browse = new Button("Browse...");
		browse.addActionListener(e -> browse());
		panel.add(path, BorderLayout.CENTER);
		panel.add(browse, BorderLayout.EAST);
		refresh();
		path.addTextListener(e -> {
			final String text = path.getText();
			update(text.isEmpty() ? null : new File(text));
		});
	}

	@Override
	public Component component() {
		return panel;
	}

	@Override
	protected void doRefresh() {
		final Object value = value();
		final String text = value == null ? "" : value.toString();
		if (!text.equals(path.getText())) path.setText(text);
	}

	// -- Helper methods --

	private void browse() {
		final FileDialog dialog = new FileDialog(frame(), "Choose a file", //
			Widgets.isStyle(node(), "save") ? FileDialog.SAVE : FileDialog.LOAD);
		final String text = path.getText();
		if (!text.isEmpty()) dialog.setFile(text);
		dialog.setVisible(true);
		if (dialog.getFile() == null) return;
		path.setText(new File(dialog.getDirectory(), dialog.getFile()).getPath());
	}

	/** Finds a frame to own the file dialog, AWT requiring one. */
	private Frame frame() {
		for (Component c = panel; c != null; c = c.getParent()) {
			if (c instanceof Frame) return (Frame) c;
			if (c instanceof Dialog) {
				final Window owner = ((Dialog) c).getOwner();
				if (owner instanceof Frame) return (Frame) owner;
			}
		}
		return null;
	}

	/** Makes {@link AwtFile}s. */
	@Plugin(type = WidgetFactory.class)
	public static class Factory implements AwtWidgetFactory {

		@Override
		public boolean supports(final ParameterNode node) {
			return node.choices().isEmpty() && Widgets.type(node) == File.class;
		}

		@Override
		public AwtWidget create(final ParameterNode node,
			final ParameterModel model, final WidgetPanelFactory<AwtWidget> panels)
		{
			return new AwtFile(node, model);
		}
	}
}
