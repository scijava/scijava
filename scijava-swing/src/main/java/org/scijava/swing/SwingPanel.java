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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.WidgetPanel;

/**
 * A panel of Swing widgets: the body of a dialog, or one group within it.
 * <p>
 * A group renders as a titled box, and a collapsible one gets a header the
 * user can click to fold it away. Since a panel is itself a
 * {@link SwingWidget}, a group sits in its parent's list like any other
 * widget, however deeply nested.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SwingPanel extends SwingWidget implements
	WidgetPanel<SwingWidget>
{

	private final List<SwingWidget> widgets;
	private final Map<String, JLabel> labels = new LinkedHashMap<>();
	private final JPanel component;
	private final JPanel body;

	private boolean collapsed;

	public SwingPanel(final ParameterNode group, final List<SwingWidget> widgets,
		final org.scijava.harvest.ParameterModel model)
	{
		super(group, model);
		this.widgets = new ArrayList<>(widgets);

		body = new JPanel(new GridBagLayout());
		body.setOpaque(false);
		layOut();

		if (group == null) {
			component = body;
			return;
		}

		component = new JPanel();
		component.setOpaque(false);
		component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
		if (group.isCollapsible()) {
			collapsed = group.isCollapsed();
			final JButton header = new JButton();
			header.setBorderPainted(false);
			header.setContentAreaFilled(false);
			header.setFocusPainted(false);
			header.setHorizontalAlignment(JButton.LEFT);
			header.addActionListener(e -> {
				collapsed = !collapsed;
				body.setVisible(!collapsed);
				header.setText(title(group));
				component.revalidate();
			});
			header.setText(title(group));
			component.add(header);
			body.setVisible(!collapsed);
		}
		else {
			component.setBorder(BorderFactory.createTitledBorder(group.label()));
		}
		component.add(body);
	}

	@Override
	public List<SwingWidget> widgets() {
		return List.copyOf(widgets);
	}

	@Override
	public JComponent component() {
		return component;
	}

	@Override
	public boolean isLabeled() {
		return false; // NB: a group carries its own title.
	}

	/**
	 * Marks the parameters the model reports problems with, and clears the
	 * marks on the rest.
	 *
	 * @param problems what is wrong, by parameter name
	 */
	public void showProblems(final Map<String, String> problems) {
		for (final Map.Entry<String, JLabel> entry : labels.entrySet()) {
			final String problem = problems.get(entry.getKey());
			final JLabel label = entry.getValue();
			label.setForeground(problem == null ? null : Color.RED.darker());
			if (problem != null) label.setToolTipText(problem);
		}
		for (final SwingWidget widget : widgets) {
			if (widget instanceof SwingPanel) {
				((SwingPanel) widget).showProblems(problems);
			}
		}
	}

	@Override
	protected void doRefresh() {
		widgets.forEach(SwingWidget::refresh);
	}

	// -- Helper methods --

	private String title(final ParameterNode group) {
		return (collapsed ? "▶ " : "▼ ") + group.label();
	}

	/** Lays the widgets out in two columns: labels right, widgets filling. */
	private void layOut() {
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);
		int row = 0;
		for (final SwingWidget widget : widgets) {
			c.gridy = row++;
			c.fill = GridBagConstraints.HORIZONTAL;
			if (widget.isLabeled()) {
				final ParameterNode node = widget.node();
				final JLabel label = new JLabel(node.label());
				node.member().map(m -> m.member().description()) //
					.filter(d -> d != null && !d.isEmpty()) //
					.ifPresent(label::setToolTipText);
				node.member().ifPresent(m -> labels.put(m.member().key(), label));
				c.gridx = 0;
				c.weightx = 0;
				c.anchor = GridBagConstraints.LINE_END;
				c.gridwidth = 1;
				body.add(label, c);
				c.gridx = 1;
				c.weightx = 1;
				c.anchor = GridBagConstraints.LINE_START;
				body.add(widget.component(), c);
			}
			else {
				// NB: an unlabeled widget - a group, a message - takes the row.
				c.gridx = 0;
				c.weightx = 1;
				c.gridwidth = 2;
				c.anchor = GridBagConstraints.LINE_START;
				body.add(widget.component(), c);
				c.gridwidth = 1;
			}
		}
	}
}
