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

import java.io.File;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.scijava.context.Plugin;
import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.WidgetFactory;
import org.scijava.ui3.WidgetPanelFactory;
import org.scijava.ui3.Widgets;

/**
 * A widget for a file or directory: a path to type, and a button to browse.
 *
 * @author Curtis Rueden
 */
public class FxFile extends FxWidget {

	private final TextField path = new TextField();
	private final HBox box;

	public FxFile(final ParameterNode node, final ParameterModel model) {
		super(node, model);
		final Button browse = new Button("Browse...");
		browse.setOnAction(e -> browse());
		box = new HBox(4, path, browse);
		HBox.setHgrow(path, Priority.ALWAYS);
		refresh();
		path.textProperty().addListener((obs, old, text) -> update(text.isEmpty()
			? null : new File(text)));
	}

	@Override
	public Node control() {
		return box;
	}

	@Override
	protected void doRefresh() {
		final Object value = value();
		final String text = value == null ? "" : value.toString();
		if (!text.equals(path.getText())) path.setText(text);
	}

	// -- Helper methods --

	private void browse() {
		final File chosen;
		if (Widgets.isStyle(node(), "directory")) {
			final DirectoryChooser chooser = new DirectoryChooser();
			chosen = chooser.showDialog(box.getScene().getWindow());
		}
		else {
			final FileChooser chooser = new FileChooser();
			chosen = Widgets.isStyle(node(), "save") //
				? chooser.showSaveDialog(box.getScene().getWindow()) //
				: chooser.showOpenDialog(box.getScene().getWindow());
		}
		if (chosen != null) path.setText(chosen.getPath());
	}

	/** Makes {@link FxFile}s. */
	@Plugin(type = WidgetFactory.class)
	public static class Factory implements FxWidgetFactory {

		@Override
		public boolean supports(final ParameterNode node) {
			return node.choices().isEmpty() && Widgets.type(node) == File.class;
		}

		@Override
		public FxWidget create(final ParameterNode node, final ParameterModel model,
			final WidgetPanelFactory<FxWidget> panels)
		{
			return new FxFile(node, model);
		}
	}
}
