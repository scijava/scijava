package org.scijava.swing.widget;

import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.scijava.context.Inject;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;
import org.scijava.struct.MemberInstance;
import org.scijava.ui.swing.widget.DocumentSizeFilter;
import org.scijava.util.ClassUtils;
import org.scijava.widget.AbstractWidget;
import org.scijava.widget.TextWidget;
import org.scijava.widget.WidgetFactory;
import org.scijava.widget.WidgetPanelFactory;
import org.scijava.widget.Widgets;

@Plugin(type = WidgetFactory.class)
public class SwingTextWidgetFactory implements SwingWidgetFactory {

	@Inject
	private LogService log;

	@Override
	public boolean supports(final MemberInstance<?> model) {
		return ClassUtils.isText(model.member().getRawType());
	}

	@Override
	public SwingWidget create(final MemberInstance<?> model,
		final WidgetPanelFactory<? extends SwingWidget> panelFactory)
	{
		return new Widget(model);
	}

	// -- Helper classes --

	private class Widget extends AbstractWidget implements SwingWidget,
		TextWidget, DocumentListener
	{

		private JPanel panel;
		private JTextComponent textComponent;

		public Widget(final MemberInstance<?> model) {
			super(model);
		}

		@Override
		public JPanel getComponent() {
			if (panel != null) return panel;

			panel = new JPanel();
			final MigLayout layout = new MigLayout("fillx,ins 3 0 3 0",
				"[fill,grow|pref]");
			panel.setLayout(layout);

			final int columns = //
				Widgets.intProperty(this, COLUMNS_PROPERTY, 6);

			// construct text widget of the appropriate style, if specified
			if (Widgets.isStyle(this, AREA_STYLE)) {
				final int rows = Widgets.intProperty(this, ROWS_PROPERTY, 5);
				textComponent = new JTextArea("", rows, columns);
			}
			else if (Widgets.isStyle(this, PASSWORD_STYLE)) {
				textComponent = new JPasswordField("", columns);
			}
			else {
				textComponent = new JTextField("", columns);
			}
			SwingWidgets.setToolTip(this, textComponent);
			getComponent().add(textComponent);
			limitLength();

			textComponent.setText(modelValue());
			textComponent.getDocument().addDocumentListener(this);

			return panel;
		}

		// -- DocumentListener methods --

		@Override
		public void changedUpdate(final DocumentEvent e) {
			model().set(textComponent.getText());
		}

		@Override
		public void insertUpdate(final DocumentEvent e) {
			model().set(textComponent.getText());
		}

		@Override
		public void removeUpdate(final DocumentEvent e) {
			model().set(textComponent.getText());
		}

		// -- Helper methods --

		private void limitLength() {
			// only limit length for single-character inputs
			if (!ClassUtils.isCharacter(model().member().getRawType())) return;

			// limit text field to a single character
			final int maxChars = 1;
			final Document doc = textComponent.getDocument();
			if (doc instanceof AbstractDocument) {
				final DocumentFilter docFilter = new DocumentSizeFilter(maxChars);
				((AbstractDocument) doc).setDocumentFilter(docFilter);
			}
			else if (log != null) {
				log.warn("Unknown document type: " + doc.getClass().getName());
			}
		}
		
		private String modelValue() {
			String value = (String) model().get();
			return value == null ? "" : value;
		}
	}
}
