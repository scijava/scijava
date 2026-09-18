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

import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.scijava.harvest.ParameterModel;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The dialog a user fills in, in JavaFX.
 *
 * @author Curtis Rueden
 */
public class FxDialog {

	private static final Logger log = LoggerFactory.getLogger(FxDialog.class);

	private final ParameterModel model;
	private final WidgetPanels<FxWidget> builder;
	private final Stage stage;
	private final BorderPane content = new BorderPane();
	private final ScrollPane scroll = new ScrollPane();
	private final Button okButton = new Button("OK");
	private final Label problem = new Label();

	private FxPanel panel;
	private boolean accepted;
	private boolean rebuilding;
	private boolean sized;

	public FxDialog(final Window owner, final String title,
		final ParameterModel model, final List<WidgetFactory<FxWidget>> factories)
	{
		this.model = model;
		builder = new WidgetPanels<>(factories, new FxPanel.Factory(model));

		stage = new Stage();
		stage.setTitle(title);
		stage.initModality(Modality.APPLICATION_MODAL);
		if (owner != null) stage.initOwner(owner);

		problem.setStyle("-fx-text-fill: darkred;");
		okButton.setDefaultButton(true);
		okButton.setOnAction(e -> {
			accepted = true;
			stage.close();
		});
		final Button cancelButton = new Button("Cancel");
		cancelButton.setCancelButton(true);
		cancelButton.setOnAction(e -> stage.close());

		final HBox buttons = new HBox(6, okButton, cancelButton);
		buttons.setAlignment(Pos.CENTER_RIGHT);
		final BorderPane south = new BorderPane();
		south.setCenter(problem);
		BorderPane.setAlignment(problem, Pos.CENTER_LEFT);
		south.setRight(buttons);
		south.setPadding(new Insets(8));

		scroll.setFitToWidth(true);
		scroll.setStyle("-fx-background-color: transparent;");
		content.setCenter(scroll);
		content.setBottom(south);
		content.setPadding(new Insets(8, 8, 0, 8));
		HBox.setHgrow(content, Priority.ALWAYS);

		model.onTreeChanged(tree -> Platform.runLater(() -> {
			log.debug("Parameter tree changed; rebuilding widgets");
			rebuild();
		}));
		model.values().onChange(change -> Platform.runLater(this::refresh));

		rebuild();
		stage.setScene(new Scene(content));
	}

	/**
	 * Shows the dialog and waits for the user.
	 *
	 * @return true if the user accepted, false if they dismissed it
	 */
	public boolean showDialog() {
		stage.showAndWait();
		return accepted;
	}

	/** Gets the window itself, to position or decorate it. */
	public Stage stage() {
		return stage;
	}

	// -- Helper methods --

	private void rebuild() {
		if (rebuilding) return;
		rebuilding = true;
		try {
			panel = (FxPanel) builder.build(model);
			log.debug("Built {} widget(s) for {}", panel.widgets().size(), stage
				.getTitle());
			if (panel.widgets().isEmpty()) {
				log.warn("No widgets for {}: either it declares no inputs, or no " +
					"widget factory accepted them", stage.getTitle());
			}
			scroll.setContent(panel.control());
			validate();
			// NB: as in Swing - the dialog is sized once and may then grow for
			// parameters that appear, but never moves and never shrinks.
			if (!sized) {
				stage.sizeToScene();
				stage.centerOnScreen();
				sized = true;
			}
			else {
				final double width = Math.max(stage.getWidth(), content.prefWidth(-1));
				final double height = Math.max(stage.getHeight(), content.prefHeight(
					width));
				stage.setWidth(width);
				stage.setHeight(height);
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
		okButton.setDisable(!problems.isEmpty());
		problem.setText(problems.isEmpty() ? "" : problems.values().iterator()
			.next());
	}
}
