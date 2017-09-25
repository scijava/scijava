
package org.scijava.nwidget;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.scijava.Context;
import org.scijava.nwidget.NWidgetPanel;
import org.scijava.nwidget.NWidgetService;
import org.scijava.nwidget.swing.NSwingWidgetPanelFactory;
import org.scijava.param.Parameter;
import org.scijava.param.ParameterStructs;
import org.scijava.struct.StructInstance;
import org.scijava.ui.swing.SwingDialog;
import org.scijava.widget.UIComponent;

public class NWidgetDemo {

	public static void main(final String... args) throws Exception {
		final Context context = new Context();
		final NWidgetService widgetService = context.service(NWidgetService.class);

		final Object person = new Object() {

			@Parameter
			private String name = "Chuckles McGee";
//			@Parameter(min = "0", max = "100", style = NumberWidget.SCROLL_BAR_STYLE)
			@Parameter
			private Integer age = 27;
		};
		final StructInstance<Object> structInstance = //
			ParameterStructs.create(person);

		final NSwingWidgetPanelFactory<Object> factory =
			new NSwingWidgetPanelFactory<>();

		// create a panel
		final NWidgetPanel<Object> panel = //
			widgetService.createPanel(structInstance, factory);

		// show the panel in a hacky way, for now
		final Component c = ((UIComponent<JPanel>) panel).getComponent();
		final SwingDialog dialog = new SwingDialog(c, JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE, true);
		final int rval = dialog.show();
	}

}
