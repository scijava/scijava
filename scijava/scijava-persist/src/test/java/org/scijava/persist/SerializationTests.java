package org.scijava.persist;

import com.google.gson.Gson;
import net.imagej.ImageJ;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.persist.testobjects.Circle;
import org.scijava.persist.testobjects.Shape;
import org.scijava.persist.testobjects.Shapes;

public class SerializationTests {
    static ImageJ ij;
    static Gson gson;

    public static void main(String... args) {
        ij = new ImageJ();
        ij.ui().showUI();
        if (ij.context()==null) {
            System.out.println("Null context");
        }
        gson = ScijavaGsonHelper.getGson(ij.context());
    }

    @Before
    public void openFiji() {
        // Initializes static SourceService and Display Service and plugins for serialization
        ij = new ImageJ();
        ij.ui().showUI();
        if (ij.context()==null) {
            System.out.println("Null context");
        }
        gson = ScijavaGsonHelper.getGson(ij.context(), true);
    }

    @After
    public void closeFiji() throws Exception {
        // Closes ij context
        ij.context().close(); //TODO : understand why this is not working
    }

    /**
     * Test
     * {@link org.scijava.persist.testobjects.CircleAdapter}
     */
    @Test
    public void testCircleObject() {
        Shape circle = new Circle();
        testSerializationDeserialization(gson, circle, Shape.class);
    }

    /**
     * Test adapters located in
     * {@link org.scijava.persist.testobjects.Shapes}
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
     * Just makes a loop serialize / deserialize / reserialize and checks
     * whether the string representation is identical
     *
     * @param gson serializer/deserializer
     * @param o object to serialize and deserialize
     * @param c class of the object
     */
    public static void testSerializationDeserialization(Gson gson, Object o, Class<?> c) {
        String json = gson.toJson(o, c);
        System.out.println(json);
        Object oRestored = gson.fromJson(json, c);
        String json2 = gson.toJson(oRestored, c);
        System.out.println(json2);
        Assert.assertEquals(json, json2);
    }
}