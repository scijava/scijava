
package org.scijava.widget;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.scijava.Context;
import org.scijava.nwidget.NSwingWidgetPanelFactory;
import org.scijava.nwidget.NWidgetPanel;
import org.scijava.nwidget.NWidgetService;
import org.scijava.param.Parameter;
import org.scijava.param.ParameterStructs;
import org.scijava.struct.StructInstance;
import org.scijava.ui.swing.SwingDialog;

public class NWidgetExplore {

	public static void main(final String... args) throws Exception {
		final Context context = new Context();
		final Object person = new Object() {

			@Parameter
			private String name;
			@Parameter
			private Integer age;
		};
		final StructInstance<Object> struct = ParameterStructs.create(person);
		final NSwingWidgetPanelFactory<Object> factory =
			new NSwingWidgetPanelFactory<>();
		final NWidgetPanel<Object> panel = //
			context.service(NWidgetService.class).createPanel(struct, factory);

		final Component c = ((UIComponent<JPanel>) panel).getComponent();
		final SwingDialog dialog = new SwingDialog(c, JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE, true);
		final int rval = dialog.show();
	}

}
