/*
 * #%L
 * SciJava library for generic type reasoning.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.types.extract;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.types.Any;
import org.scijava.types.ExampleTypes.Bag;
import org.scijava.types.ExampleTypes.BlueThing;
import org.scijava.types.ExampleTypes.RedThing;
import org.scijava.types.Nil;
import org.scijava.types.GenericTyped;

/**
 * Tests {@link DefaultTypeReifier}, including core {@link TypeExtractor}
 * implementations.
 *
 * @author Curtis Rueden
 */
public class DefaultTypeReifierTest {

	private TypeReifier types;

	@BeforeEach
	public void setUp() {
		Discoverer serviceLoaderDiscoverer = Discoverer.using(ServiceLoader::load);
		types = new DefaultTypeReifier(serviceLoaderDiscoverer);
	}

	@AfterEach
	public void tearDown() {
		types = null;
	}

	/** Tests type extraction for non-generic objects. */
	@Test
	public void testClass() {
		final Type stringType = types.reify("Hello");
		Assertions.assertEquals(String.class, stringType);
	}

	/** Tests type extraction for {@code null} objects. */
	@Test
	public void testNull() {
		final Type nullType = types.reify(null);
		Assertions.assertTrue(Any.class.isInstance(nullType));
	}

	/** Tests type extraction for {@link Nil} objects. */
	@Test
	public void testNil() {
		final Nil<List<Float>> nilFloatList = new Nil<List<Float>>() {};
		final Type nilFloatListType = types.reify(nilFloatList);
		Assertions.assertEquals(nilFloatList.getType(), nilFloatListType);
	}

	/** Tests type extraction for {@link GenericTyped} objects. */
	@Test
	public void testGenericTyped() {
		final Object numberThing = new GenericTyped() {

			@Override
			public Type getType() {
				return Number.class;
			}
		};
		Assertions.assertEquals(Number.class, types.reify(numberThing));
	}

	/** Tests type extraction for {@link Iterable} objects. */
	@Test
	public void testIterable() {
		final List<String> stringList = //
			new ArrayList<>(Collections.singletonList("Hi"));
		final Type stringListType = types.reify(stringList);
		Assertions.assertEquals(new Nil<ArrayList<String>>() {}.getType(),
			stringListType);
	}

	/** Tests type extraction for {@link Map} objects. */
	@Test
	public void testMap() {
		final Map<String, Integer> mapSI = //
			new HashMap<>(Collections.singletonMap("Curtis", 37));
		final Type mapSIType = types.reify(mapSI);
		Assertions.assertEquals(new Nil<HashMap<String, Integer>>() {}.getType(),
			mapSIType);
	}

	/** Tests nested type extraction of a complex object. */
	@Test
	public void testNested() {
		// List of organization test scores.
		// For each organization, we have a table of students by name.
		final List<Map<String, List<Integer>>> testScores = new ArrayList<>();

		final Map<String, List<Integer>> hogwarts = new HashMap<>();
		hogwarts.put("Hermione", new ArrayList<>(Arrays.asList(100, 99, 101)));
		hogwarts.put("Ron", new ArrayList<>(Arrays.asList(45, 56, 82)));
		testScores.add(hogwarts);

		final Map<String, List<Integer>> highlights = new HashMap<>();
		highlights.put("Goofus", new ArrayList<>(Arrays.asList(12, 0, 23)));
		highlights.put("Gallant", new ArrayList<>(Arrays.asList(87, 92, 96)));
		testScores.add(highlights);

		final Type testScoresType = types.reify(testScores);
		Assertions.assertEquals(
			new Nil<ArrayList<HashMap<String, ArrayList<Integer>>>>()
			{}.getType(), testScoresType);
	}

	/**
	 * Tests type extraction for recursively typed objects (e.g.,
	 * {@code F extends Foo<F>}.
	 */
	@Test
	public void testRecursiveTyping() {
		final Bag<BlueThing> blueBag = new Bag<>();
		blueBag.add(new BlueThing());

		final Type blueBagType = types.reify(blueBag);
		Assertions.assertEquals(new Nil<Bag<BlueThing>>() {}.getType(),
			blueBagType);

		final Bag<RedThing> redBag = new Bag<>();
		redBag.add(new RedThing());

		final Type redBagType = types.reify(redBag);
		Assertions.assertEquals(new Nil<Bag<RedThing>>() {}.getType(), redBagType);
	}
}
