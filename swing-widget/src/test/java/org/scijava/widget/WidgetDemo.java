
package org.scijava.widget;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.scijava.widget.swing.SwingWidgetPanelFactory;
import org.scijava.param.Parameter;
import org.scijava.param.ParameterStructs;
import org.scijava.struct.StructInstance;
import org.scijava.ui.swing.SwingDialog;
import org.scijava.widget.UIComponent;

public class WidgetDemo {

	private interface Joke {}

	public static void main(final String... args) throws Exception {
		final Context context = new Context();
		final WidgetService widgetService = context.service(WidgetService.class);
		
		final ObjectService objectService = context.service(ObjectService.class);
		objectService.addObject(new Joke() {
			@Parameter(label = "Setup")
			private String setup;
			@Parameter(label = "Punchline")
			private String punchline;
			@Parameter
			private int laughs = 0;
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

			@Parameter
			@FileStyle(type = DIRECTORIES_ONLY, extensions = {"tif"}, regex = "foo.*bar")
			private File directory;
			@Parameter(min = "0")
			private int age = 27;

			@Parameter
			@ScrollBarStyle(min = "0", max = "100")
			private int percent = 50;

			@Parameter(label = "Best Jokes"})
			@SliderStyle(min = "10", max = "25", softMin = "", stepSize = "1")
			private int jokes = 20;

			// NB: This gets hooked into the MemberInstance.set(T) method.
			// So whenever model changes, view changes.
			// The set method, after calling the callback, needs to trigger a
			// full recomparison of the entire StructInstance, and then trigger
			// listener notification for all changed values.
//			@Callback("jokes")
			private void jokeChanged() {
				percent = percentOfLaughs(joke);
			}

			@Parameter(style = TextWidget.PASSWORD_STYLE)
			private String password;
//			@Parameter(style = TextWidget.AREA_STYLE)
//			private String description =
//				"I am a clown student in my fourth year at Dell'Arte International. " +
//					"I like juggling, unicycles and banana cream pies.";
			@Parameter(label = "Favorite joke")
			private Joke joke;
		};
		final StructInstance<Object> structInstance = //
			ParameterStructs.create(person);

		final SwingWidgetPanelFactory panelFactory = new SwingWidgetPanelFactory();

		// create a panel
		final WidgetPanel<Object> panel = //
			widgetService.createPanel(structInstance, panelFactory);

		// show the panel in a hacky way, for now
		final Component c = ((UIComponent<JPanel>) panel).getComponent();
		final SwingDialog dialog = new SwingDialog(c, JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE, true);
		dialog.setTitle("WidgetDemo");
		final int rval = dialog.show();
		System.out.println("return value = " + rval);
	}

}
