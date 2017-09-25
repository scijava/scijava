package org.scijava.nwidget.swing;

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

import org.scijava.log.Logger;
import org.scijava.nwidget.NAbstractWidget;
import org.scijava.nwidget.NTextWidget;
import org.scijava.nwidget.NWidget;
import org.scijava.nwidget.NWidgetFactory;
import org.scijava.nwidget.NWidgets;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.MemberInstance;
import org.scijava.ui.swing.widget.DocumentSizeFilter;
import org.scijava.util.ClassUtils;

@Plugin(type = NWidgetFactory.class)
public class NSwingTextWidgetFactory implements NSwingWidgetFactory {

	@Parameter
	private Logger log;

	@Override
	public boolean supports(final MemberInstance<?> model) {
		return ClassUtils.isText(model.member().getRawType());
	}

	@Override
	public NWidget create(final MemberInstance<?> memberInstance) {
		return new Widget(memberInstance);
	}

	// -- Helper classes --

	private class Widget extends NAbstractWidget implements NSwingWidget,
		NTextWidget, DocumentListener
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
				NWidgets.intProperty(this, COLUMNS_PROPERTY, 6);

			// construct text widget of the appropriate style, if specified
			if (NWidgets.isStyle(this, AREA_STYLE)) {
				final int rows = NWidgets.intProperty(this, ROWS_PROPERTY, 5);
				textComponent = new JTextArea("", rows, columns);
			}
			else if (NWidgets.isStyle(this, PASSWORD_STYLE)) {
				textComponent = new JPasswordField("", columns);
			}
			else {
				textComponent = new JTextField("", columns);
			}
			NSwingWidgets.setToolTip(this, textComponent);
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
