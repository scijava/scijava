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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.scijava.harvest.ParameterModel;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanels;

/**
 * The dialog a user fills in: the parameters, an OK and a Cancel.
 * <p>
 * It rebuilds itself whenever the shape of the parameters changes, which is
 * how a value that reveals a group, or decides how many parameters there are,
 * takes effect as the user types.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SwingDialog {

	private final ParameterModel model;
	private final WidgetPanels<SwingWidget> builder;
	private final JDialog dialog;
	private final JPanel content;
	private final JButton okButton;
	private final JLabel problem;

	private SwingPanel panel;
	private boolean accepted;
	private boolean rebuilding;

	public SwingDialog(final Window owner, final String title,
		final ParameterModel model,
		final List<WidgetFactory<SwingWidget>> factories)
	{
		this.model = model;
		builder = new WidgetPanels<>(factories, new SwingPanelFactory(model));

		dialog = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
		content = new JPanel(new BorderLayout());
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		problem = new JLabel(" ");
		problem.setForeground(java.awt.Color.RED.darker());

		okButton = new JButton("OK");
		okButton.addActionListener(e -> {
			accepted = true;
			dialog.dispose();
		});
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dialog.dispose());

		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(okButton);
		buttons.add(cancelButton);

		final JPanel south = new JPanel(new BorderLayout());
		south.add(problem, BorderLayout.CENTER);
		south.add(buttons, BorderLayout.EAST);

		dialog.setLayout(new BorderLayout());
		dialog.add(content, BorderLayout.CENTER);
		dialog.add(south, BorderLayout.SOUTH);
		dialog.getRootPane().setDefaultButton(okButton);

		// NB: a value may reshape the dialog - reveal a group, add parameters -
		// so the tree is rebuilt on every change and the widgets follow it.
		model.onTreeChanged(tree -> SwingUtilities.invokeLater(this::rebuild));
		// NB: a callback may change values other than the one the user touched,
		// with the shape unchanged; those widgets still have to catch up.
		model.values().onChange(change -> SwingUtilities.invokeLater(
			this::refresh));

		rebuild();
	}

	/**
	 * Shows the dialog and waits for the user.
	 *
	 * @return true if the user accepted, false if they cancelled
	 */
	public boolean showDialog() {
		dialog.setVisible(true); // NB: modal, so this blocks until disposed
		return accepted;
	}

	/** Gets the dialog itself, to position or decorate it. */
	public JDialog dialog() {
		return dialog;
	}

	// -- Helper methods --

	/** Builds the widgets afresh, for the shape the values now imply. */
	private void rebuild() {
		if (rebuilding) return;
		rebuilding = true;
		try {
			panel = (SwingPanel) builder.build(model);
			content.removeAll();
			content.add(new JScrollPane(panel.component()), BorderLayout.CENTER);
			validate();
			dialog.pack();
			final Dimension size = dialog.getSize();
			// NB: keep a dialog with many parameters on the screen; the scroll
			// pane takes care of the rest.
			final Dimension screen = dialog.getGraphicsConfiguration() == null ? null
				: dialog.getGraphicsConfiguration().getBounds().getSize();
			if (screen != null && size.height > screen.height * 0.8) {
				dialog.setSize(size.width + 20, (int) (screen.height * 0.8));
			}
			dialog.setLocationRelativeTo(dialog.getOwner());
		}
		finally {
			rebuilding = false;
		}
	}

	private void refresh() {
		if (rebuilding || panel == null) return;
		panel.refresh();
		validate();
	}

	/** Shows what is wrong, and lets the user proceed only when nothing is. */
	private void validate() {
		final Map<String, String> problems = model.problems();
		panel.showProblems(problems);
		okButton.setEnabled(problems.isEmpty());
		problem.setText(problems.isEmpty() ? " " : problems.values().iterator()
			.next());
	}
}
