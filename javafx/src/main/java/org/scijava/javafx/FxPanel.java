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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.WidgetPanel;
import org.scijava.ui3.WidgetPanelFactory;

/**
 * A panel of JavaFX widgets: the body of a dialog, or one group within it.
 * <p>
 * A collapsible group is a {@link TitledPane}, which JavaFX gives for free and
 * Swing has to be talked into.
 * </p>
 *
 * @author Curtis Rueden
 */
public class FxPanel extends FxWidget implements WidgetPanel<FxWidget> {

	private final List<FxWidget> widgets;
	private final Map<String, Label> labels = new LinkedHashMap<>();
	private final GridPane body = new GridPane();
	private final Node control;

	public FxPanel(final ParameterNode group, final List<FxWidget> widgets,
		final ParameterModel model)
	{
		super(group, model);
		this.widgets = new ArrayList<>(widgets);

		body.setHgap(6);
		body.setVgap(4);
		body.setPadding(new Insets(4));
		final ColumnConstraints labelColumn = new ColumnConstraints();
		labelColumn.setHalignment(HPos.RIGHT);
		final ColumnConstraints widgetColumn = new ColumnConstraints();
		widgetColumn.setHgrow(Priority.ALWAYS);
		widgetColumn.setFillWidth(true);
		body.getColumnConstraints().addAll(labelColumn, widgetColumn);
		layOut();

		if (group == null) {
			control = body;
		}
		else {
			final TitledPane pane = new TitledPane(group.label(), body);
			pane.setCollapsible(group.isCollapsible());
			pane.setExpanded(!group.isCollapsed());
			control = pane;
		}
	}

	@Override
	public List<FxWidget> widgets() {
		return List.copyOf(widgets);
	}

	@Override
	public Node control() {
		return control;
	}

	@Override
	public boolean isLabeled() {
		return false; // NB: a group carries its own title.
	}

	@Override
	public void showProblems(final Map<String, String> problems) {
		labels.forEach((key, label) -> {
			final String problem = problems.get(key);
			label.setStyle(problem == null ? "" : "-fx-text-fill: darkred;");
			label.setTooltip(problem == null ? null : new Tooltip(problem));
		});
		WidgetPanel.super.showProblems(problems);
	}

	@Override
	protected void doRefresh() {
		widgets.forEach(FxWidget::refresh);
	}

	// -- Helper methods --

	private void layOut() {
		int row = 0;
		for (final FxWidget widget : widgets) {
			if (widget.isLabeled()) {
				final ParameterNode node = widget.node();
				final Label label = new Label(node.label());
				node.member().map(m -> m.member().description()) //
					.filter(d -> d != null && !d.isEmpty()) //
					.ifPresent(d -> label.setTooltip(new Tooltip(d)));
				node.member().ifPresent(m -> labels.put(m.member().key(), label));
				body.add(label, 0, row);
				body.add(widget.control(), 1, row);
			}
			else {
				// NB: an unlabeled widget - a group, a message - takes the row.
				body.add(widget.control(), 0, row, 2, 1);
			}
			row++;
		}
	}

	/** Makes {@link FxPanel}s. */
	public static class Factory implements WidgetPanelFactory<FxWidget> {

		private final ParameterModel model;

		public Factory(final ParameterModel model) {
			this.model = model;
		}

		@Override
		public FxWidget create(final ParameterNode group,
			final List<FxWidget> widgets)
		{
			return new FxPanel(group, widgets, model);
		}
	}
}
