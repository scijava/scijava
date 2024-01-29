/*-
 * #%L
 * Extensible serialization mechanism for persisting objects.
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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
