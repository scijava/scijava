
package org.scijava.nwidget;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.nwidget.swing.NSwingWidgetPanelFactory;
import org.scijava.object.ObjectService;
import org.scijava.param.Parameter;
import org.scijava.param.ParameterStructs;
import org.scijava.struct.StructInstance;
import org.scijava.ui.swing.SwingDialog;
import org.scijava.widget.UIComponent;

public class NWidgetDemo {

	private interface Joke {}

	public static void main(final String... args) throws Exception {
		final Context context = new Context();
		final NWidgetService widgetService = context.service(NWidgetService.class);
		
		final ObjectService objectService = context.service(ObjectService.class);
		objectService.addObject(new Joke() {
			@Parameter(label = "Setup")
			private String setup;
			@Parameter(label = "Punchline")
			private String punchline;
			@Override
			public String toString() {
				return "Regular joke";
			}
		});
		objectService.addObject(new Joke() {
			@Parameter(label = "Who's there?")
			private String whosThere;
			@Parameter(label = "Punchline")
			private String punchline;
			@Override
			public String toString() {
				return "Knock-knock joke";
			}
		});

		final Object person = new Object() {

			@Parameter
			private String name = "Chuckles McGee";
			@Parameter(min = "0")
			private int age = 27;
			@Parameter(min = "0", max = "100", style = NNumberWidget.SCROLL_BAR_STYLE)
			private int percent = 50;
			@Parameter(min = "10", max = "25", style = NNumberWidget.SLIDER_STYLE)
			private int jokes = 20;
			@Parameter(style = NTextWidget.PASSWORD_STYLE)
			private String password;
//			@Parameter(style = NTextWidget.AREA_STYLE)
//			private String description =
//				"I am a clown student in my fourth year at Dell'Arte International. " +
//					"I like juggling, unicycles and banana cream pies.";
			@Parameter(label = "Favorite joke")
			private Joke joke;
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
		dialog.setTitle("NWidgetDemo");
		final int rval = dialog.show();
		System.out.println("return value = " + rval);
	}

}
