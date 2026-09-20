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

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.WidgetPanel;
import org.scijava.ui3.WidgetPanelFactory;

/**
 * A panel of AWT widgets: the body of a dialog, or one group within it.
 * <p>
 * NB: AWT has no titled border and no collapsible pane, so a group is a label
 * above its contents and a collapsible one is a button that hides them. Doing
 * it by hand is the price of the toolkit; that the <em>contract</em> asks for
 * nothing more than "show a group, let it be folded" is the finding.
 * </p>
 *
 * @author Curtis Rueden
 */
public class AwtPanel extends AwtWidget implements WidgetPanel<AwtWidget> {

	private final List<AwtWidget> widgets;
	private final Map<String, Label> labels = new LinkedHashMap<>();
	private final Panel body = new Panel(new GridBagLayout());
	private final Panel panel = new Panel(new GridBagLayout());

	private boolean collapsed;

	public AwtPanel(final ParameterNode group, final List<AwtWidget> widgets,
		final ParameterModel model)
	{
		super(group, model);
		this.widgets = new ArrayList<>(widgets);
		layOut();

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.anchor = GridBagConstraints.LINE_START;
		if (group == null) {
			c.gridy = 0;
			panel.add(body, c);
			return;
		}
		if (group.isCollapsible()) {
			collapsed = group.isCollapsed();
			final Button header = new Button(title(group));
			header.addActionListener(e -> {
				collapsed = !collapsed;
				body.setVisible(!collapsed);
				header.setLabel(title(group));
				panel.invalidate();
				panel.validate();
			});
			c.gridy = 0;
			panel.add(header, c);
			body.setVisible(!collapsed);
		}
		else {
			c.gridy = 0;
			panel.add(new Label(group.label()), c);
		}
		c.gridy = 1;
		c.insets = new Insets(0, 12, 0, 0); // NB: indent, for want of a border
		panel.add(body, c);
	}

	@Override
	public List<AwtWidget> widgets() {
		return List.copyOf(widgets);
	}

	@Override
	public Component component() {
		return panel;
	}

	@Override
	public boolean isLabeled() {
		return false; // NB: a group carries its own title.
	}

	@Override
	public void showProblems(final Map<String, String> problems) {
		labels.forEach((key, label) -> {
			final String problem = problems.get(key);
			label.setForeground(problem == null ? Color.BLACK : Color.RED.darker());
		});
		WidgetPanel.super.showProblems(problems);
	}

	@Override
	protected void doRefresh() {
		widgets.forEach(AwtWidget::refresh);
	}

	// -- Helper methods --

	private String title(final ParameterNode group) {
		return (collapsed ? "▶ " : "▼ ") + group.label();
	}

	private void layOut() {
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);
		int row = 0;
		for (final AwtWidget widget : widgets) {
			c.gridy = row++;
			c.fill = GridBagConstraints.HORIZONTAL;
			if (widget.isLabeled()) {
				final ParameterNode node = widget.node();
				final Label label = new Label(node.label(), Label.RIGHT);
				node.member().ifPresent(m -> labels.put(m.member().key(), label));
				c.gridx = 0;
				c.weightx = 0;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.LINE_END;
				body.add(label, c);
				c.gridx = 1;
				c.weightx = 1;
				c.anchor = GridBagConstraints.LINE_START;
				body.add(widget.component(), c);
			}
			else {
				c.gridx = 0;
				c.weightx = 1;
				c.gridwidth = 2;
				c.anchor = GridBagConstraints.LINE_START;
				body.add(widget.component(), c);
				c.gridwidth = 1;
			}
		}
	}

	/** Makes {@link AwtPanel}s. */
	public static class Factory implements WidgetPanelFactory<AwtWidget> {

		private final ParameterModel model;

		public Factory(final ParameterModel model) {
			this.model = model;
		}

		@Override
		public AwtWidget create(final ParameterNode group,
			final List<AwtWidget> widgets)
		{
			return new AwtPanel(group, widgets, model);
		}
	}
}
