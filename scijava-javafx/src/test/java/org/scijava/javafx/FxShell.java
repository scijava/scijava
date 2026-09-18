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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.scijava.command3.Commands;
import org.scijava.command3.CommandInfo;
import org.scijava.command3.MenuTree;
import org.scijava.context.Context;
import org.scijava.execute.ExecutionResult;
import org.scijava.execute.Runner;

/**
 * The same small application as {@code org.scijava.swing.Shell}, in JavaFX.
 * <p>
 * Run it with:
 * </p>
 *
 * <pre>
 * mvn -pl scijava-javafx test-compile exec:java -Denforcer.skip \
 *   -Dexec.classpathScope=test -Dexec.mainClass=org.scijava.javafx.FxShell
 * </pre>
 * <p>
 * It is worth comparing the two side by side. The commands are the same
 * declarations, the menu tree is the same model walked by the same
 * {@code Menus}, the dialogs are built by the same {@code WidgetPanels}: what
 * differs is the controls, and this file's plumbing for JavaFX's own
 * application thread.
 * </p>
 *
 * @author Curtis Rueden
 */
public class FxShell {

	private final Context context = Context.create();
	private final List<CommandInfo> commands = entries();
	private final Runner runner;

	private final Stage stage = new Stage();
	private final Label status = new Label("Ready");
	private final ProgressBar progress = new ProgressBar();
	private final TextField search = new TextField();
	private final ListView<CommandInfo> matches = new ListView<>();

	public FxShell() {
		runner = Runner.of(List.of(FxInputHarvester.of(context)), List.of());

		final BorderPane root = new BorderPane();
		root.setTop(new VBox(FxMenus.create(MenuTree.of(commands), this::run),
			searchPane()));
		root.setCenter(matches);
		root.setBottom(statusPane());

		matches.setCellFactory(list -> new ListCell<>() {

			@Override
			protected void updateItem(final CommandInfo item, final boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : describe(item));
			}
		});
		matches.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) runSelected();
		});
		search.textProperty().addListener((obs, old, text) -> filter());
		search.setOnAction(e -> runSelected());
		filter();

		stage.setTitle("SciJava");
		stage.setScene(new Scene(root, 520, 300));
		stage.setOnCloseRequest(e -> Platform.exit());
	}

	public static void main(final String... args) {
		// NB: not Application.launch - see FxThread.start.
		FxThread.runAndWait(() -> new FxShell().stage.show());
	}

	// -- Helper methods --

	/** Gathers what this application can run: commands, and scripts. */
	private List<CommandInfo> entries() {
		final List<CommandInfo> entries = new java.util.ArrayList<>(Commands.discover(
			context));
		final java.net.URL url = FxShell.class.getResource("/scripts");
		if (url != null) {
			try {
				new org.scijava.script3.ScriptFinder().find(java.nio.file.Paths.get(url
					.toURI())).forEach(found -> entries.add(CommandInfo.of(found.script(),
						found.metadata())));
			}
			catch (final java.net.URISyntaxException exc) {
				// NB: no scripts to add, which is not a reason to fail to start.
			}
		}
		return entries;
	}

	private HBox searchPane() {
		final HBox pane = new HBox(6, new Label("Search:"), search);
		pane.setPadding(new Insets(8));
		HBox.setHgrow(search, Priority.ALWAYS);
		return pane;
	}

	private VBox statusPane() {
		progress.setMaxWidth(Double.MAX_VALUE);
		progress.setPrefHeight(4);
		progress.setVisible(false);
		final VBox pane = new VBox(progress, status);
		VBox.setMargin(status, new Insets(4, 8, 4, 8));
		return pane;
	}

	/** Narrows the list to the commands whose name contains what was typed. */
	private void filter() {
		final String text = search.getText().trim().toLowerCase(Locale.ROOT);
		final List<CommandInfo> found = commands.stream() //
			.filter(c -> describe(c).toLowerCase(Locale.ROOT).contains(text)) //
			.collect(Collectors.toList());
		matches.setItems(FXCollections.observableArrayList(found));
		if (!found.isEmpty()) matches.getSelectionModel().select(0);
		status.setText(found.size() + " of " + commands.size() + " commands");
	}

	private void runSelected() {
		final CommandInfo command = matches.getSelectionModel().getSelectedItem();
		if (command != null) run(command);
	}

	/** Runs a command, and says what came back. */
	private void run(final CommandInfo command) {
		status.setText("Running " + command.label() + "...");
		progress.setVisible(true);
		final Future<ExecutionResult> future = runner.run(command, Map.of());
		new Thread(() -> {
			final String message = describe(future);
			Platform.runLater(() -> {
				progress.setVisible(false);
				status.setText(message);
			});
		}, "shell-" + command.name()).start();
	}

	private static String describe(final CommandInfo command) {
		return command.menuPath().map(path -> path.replace(">", " ▸ ")) //
			.orElseGet(() -> command.label() + " (not in any menu)");
	}

	private static String describe(final Future<ExecutionResult> future) {
		final ExecutionResult result;
		try {
			result = future.get();
		}
		catch (final Exception exc) {
			return "Failed: " + exc.getMessage();
		}
		if (result.isDeclined()) return result.reason().orElse("Declined");
		if (result.outputs().isEmpty()) return "Done";
		return result.outputs().values().stream().map(String::valueOf) //
			.collect(Collectors.joining("; "));
	}
}
