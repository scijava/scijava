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
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

import org.scijava.harvest.ParameterModel;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The dialog a user fills in, in AWT.
 *
 * @author Curtis Rueden
 */
public class AwtDialog {

	private static final Logger log = LoggerFactory.getLogger(AwtDialog.class);

	private final ParameterModel model;
	private final WidgetPanels<AwtWidget> builder;
	private final Dialog dialog;
	private final Panel content = new Panel(new BorderLayout());
	private final Button okButton = new Button("OK");
	private final Label problem = new Label();

	private AwtPanel panel;
	private boolean accepted;
	private boolean rebuilding;
	private boolean placed;

	public AwtDialog(final Frame owner, final String title,
		final ParameterModel model, final List<WidgetFactory<AwtWidget>> factories)
	{
		this.model = model;
		builder = new WidgetPanels<>(factories, new AwtPanel.Factory(model));

		// NB: AWT dialogs need a frame, even a throwaway one.
		dialog = new Dialog(owner == null ? new Frame() : owner, title, true);

		problem.setForeground(Color.RED.darker());
		okButton.addActionListener(e -> {
			accepted = true;
			close();
		});
		final Button cancelButton = new Button("Cancel");
		cancelButton.addActionListener(e -> close());
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				close();
			}
		});

		final Panel buttons = new Panel();
		buttons.add(okButton);
		buttons.add(cancelButton);
		final Panel south = new Panel(new BorderLayout());
		south.add(problem, BorderLayout.CENTER);
		south.add(buttons, BorderLayout.EAST);

		dialog.setLayout(new BorderLayout());
		dialog.add(content, BorderLayout.CENTER);
		dialog.add(south, BorderLayout.SOUTH);

		model.onTreeChanged(tree -> Edt.later(() -> {
			log.debug("Parameter tree changed; rebuilding widgets");
			rebuild();
		}));
		model.values().onChange(change -> Edt.later(this::refresh));

		rebuild();
	}

	/**
	 * Shows the dialog and waits for the user.
	 *
	 * @return true if the user accepted, false if they dismissed it
	 */
	public boolean showDialog() {
		dialog.setVisible(true); // NB: modal, so this blocks until disposed
		return accepted;
	}

	/** Gets the window itself, to position or decorate it. */
	public Window window() {
		return dialog;
	}

	// -- Helper methods --

	private void close() {
		dialog.setVisible(false);
		dialog.dispose();
	}

	private void rebuild() {
		if (rebuilding) return;
		rebuilding = true;
		try {
			panel = (AwtPanel) builder.build(model);
			log.debug("Built {} widget(s) for {}", panel.widgets().size(), dialog
				.getTitle());
			if (panel.widgets().isEmpty()) {
				log.warn("No widgets for {}: either it declares no inputs, or no " +
					"widget factory accepted them", dialog.getTitle());
			}
			content.removeAll();
			content.add(panel.component(), BorderLayout.NORTH);
			validate();
			// NB: as in the other bindings - sized once, may grow, never moves
			// and never shrinks.
			final int width = dialog.getWidth();
			final int height = dialog.getHeight();
			dialog.pack();
			if (placed) {
				dialog.setSize(Math.max(width, dialog.getWidth()), Math.max(height,
					dialog.getHeight()));
			}
			else {
				dialog.setLocationRelativeTo(dialog.getOwner());
				placed = true;
			}
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
		problem.setText(problems.isEmpty() ? "" : problems.values().iterator()
			.next());
	}
}
