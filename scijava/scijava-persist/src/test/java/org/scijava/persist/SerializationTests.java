
package org.scijava.persist;

import com.google.gson.Gson;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializationTests {

	static Context context;
	static Gson gson;

	public static void main(String... args) {
		context = new Context(IObjectScijavaAdapterService.class);
		if (context == null) {
			System.out.println("Null context");
		}
		gson = ScijavaGsonHelper.getGson(context);
	}

	@BeforeEach
	public void openFiji() {
		// Initializes static SourceService and Display Service and plugins for
		// serialization
		context = new Context(IObjectScijavaAdapterService.class);
		if (context == null) {
			System.out.println("Null context");
		}
		gson = ScijavaGsonHelper.getGson(context, true);
	}

	@AfterEach
	public void closeFiji() throws Exception {
		context.dispose();
		context = null;
		gson = null;
	}

	/**
	 * Test {@link org.scijava.persist.CircleAdapter}
	 */
	@Test
	public void testCircleObject() {
		Shape circle = new Circle();
		testSerializationDeserialization(gson, circle, Shape.class);
	}

	/**
	 * Test adapters located in {@link org.scijava.persist.Shapes}
	 */
	@Test
	public void testDrawingObject() {
		Shapes.Drawing drawing = new Shapes.Drawing();
		drawing.bottomShape = new Circle();
		drawing.topShape = new Shapes.Diamond();
		drawing.middleShape = new Shapes.Rectangle();
		testSerializationDeserialization(gson, drawing, Shapes.Drawing.class);
	}

	/**
	 * Just makes a loop serialize / deserialize / reserialize and checks whether
	 * the string representation is identical
	 *
	 * @param gson serializer/deserializer
	 * @param o object to serialize and deserialize
	 * @param c class of the object
	 */
	public static void testSerializationDeserialization(Gson gson, Object o,
		Class<?> c)
	{
		String json = gson.toJson(o, c);
		System.out.println(json);
		Object oRestored = gson.fromJson(json, c);
		String json2 = gson.toJson(oRestored, c);
		System.out.println(json2);
		assertEquals(json, json2);
	}
}
