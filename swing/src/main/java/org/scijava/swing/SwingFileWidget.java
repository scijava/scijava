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

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.Widgets;

/**
 * A widget for a file or directory: a path to type, and a button to browse.
 *
 * @author Curtis Rueden
 */
public class SwingFileWidget extends SwingWidget implements DocumentListener {

	/** Style hint: choose a directory rather than a file. */
	public static final String DIRECTORY = "directory";

	/** Style hint: choose where to save, rather than what to open. */
	public static final String SAVE = "save";

	private final JTextField path;
	private final JPanel component;

	public SwingFileWidget(final ParameterNode node,
		final ParameterModel model)
	{
		super(node, model);
		path = new JTextField(20);
		final JButton browse = new JButton("Browse...");
		browse.addActionListener(e -> browse());

		component = panel();
		component.setLayout(new BorderLayout(4, 0));
		component.add(path, BorderLayout.CENTER);
		component.add(browse, BorderLayout.EAST);

		refresh();
		path.getDocument().addDocumentListener(this);
	}

	@Override
	public JComponent component() {
		return component;
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		changed();
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		changed();
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		changed();
	}

	@Override
	protected void doRefresh() {
		final Object value = value();
		final String text = value == null ? "" : value.toString();
		if (!text.equals(path.getText())) path.setText(text);
	}

	// -- Helper methods --

	private void changed() {
		final String text = path.getText();
		update(text.isEmpty() ? null : new File(text));
	}

	private void browse() {
		final JFileChooser chooser = new JFileChooser();
		final String text = path.getText();
		if (!text.isEmpty()) chooser.setSelectedFile(new File(text));
		if (Widgets.isStyle(node(), DIRECTORY)) {
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		final int result = Widgets.isStyle(node(), SAVE) //
			? chooser.showSaveDialog(component) : chooser.showOpenDialog(component);
		if (result != JFileChooser.APPROVE_OPTION) return;
		path.setText(chooser.getSelectedFile().getPath());
	}
}
