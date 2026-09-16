/*
 * #%L
 * Annotation indexing, for discovery without class loading.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discovery;

/**
 * Tests {@link IndexDiscoverer}, and with it the property that makes an
 * annotation index worth having: it reports what is available, and what its
 * metadata says, without loading any implementation class.
 *
 * @author Curtis Rueden
 */
public class IndexDiscovererTest {

	private static final String SQUARE = "org.scijava.index.Square";

	/**
	 * Indexes the test classes.
	 * <p>
	 * This component cannot run its own annotation processor while compiling
	 * itself, so the index is built here instead, exactly as SciJava Common
	 * builds its own.
	 * </p>
	 */
	@BeforeAll
	public static void buildIndex() throws Exception {
		final File testClasses = new File(Square.class.getProtectionDomain()
			.getCodeSource().getLocation().toURI());
		new DirectoryIndexer().index(testClasses);
	}

	private static IndexDiscoverer<Widget> discoverer() {
		return new IndexDiscoverer<>(Widget.class, //
			item -> item.annotation().type().getName(), //
			item -> {
				final Map<String, String> attrs = new HashMap<>();
				attrs.put("label", item.annotation().label());
				return attrs;
			}, //
			item -> item.annotation().priority(), //
			Thread.currentThread().getContextClassLoader());
	}

	/** Discovery reads metadata without constructing anything. */
	@Test
	public void testDiscoveryDoesNotConstruct() {
		final List<Discovery<Shape>> discoveries = discoverer().discover(
			Shape.class);

		final Discovery<Shape> square = byClassName(discoveries, Square.class
			.getName());
		assertNotNull(square, "Square was not discovered");
		assertEquals("A square", square.attr("label").orElse(null));
		assertEquals(10.0, square.priority());
		assertFalse(Square.constructed,
			"discovery must not construct the implementation");

		// Only now does the class come into existence.
		assertEquals("square", square.get().describe());
		assertTrue(Square.constructed);
	}

	/**
	 * The whole point: discovery never asks the class loader for the
	 * implementation class, so an index-backed discoverer can answer what is
	 * available without paying to load it.
	 * <p>
	 * NB: this cannot be shown with a static flag on the implementation, since
	 * reading such a flag would itself load the class. It takes a class loader
	 * that records what it is asked for.
	 * </p>
	 */
	@Test
	public void testDiscoveryDoesNotLoadClasses() {
		final RecordingClassLoader loader = new RecordingClassLoader( //
			Thread.currentThread().getContextClassLoader());
		final IndexDiscoverer<Widget> discoverer = new IndexDiscoverer<>( //
			Widget.class, item -> item.annotation().type().getName(), //
			item -> Map.of(), item -> item.annotation().priority(), loader);

		final List<Discovery<Shape>> discoveries = discoverer.discover(Shape.class);
		final Discovery<Shape> square = byClassName(discoveries, SQUARE);
		assertNotNull(square);
		assertFalse(loader.loaded.contains(SQUARE),
			"discovery must not load the implementation class");

		// Asking for the object is what loads it.
		assertNotNull(square.get());
		assertTrue(loader.loaded.contains(SQUARE));
	}

	/** Discovery yields only implementations of the requested type. */
	@Test
	public void testFiltersByProvidedType() {
		final List<String> shapes = discoverer().discover(Shape.class).stream() //
			.map(Discovery::implClassName) //
			.sorted() //
			.collect(Collectors.toList());
		assertEquals(List.of(Circle.class.getName(), Square.class.getName()),
			shapes);

		final List<String> colors = discoverer().discover(Color.class).stream() //
			.map(Discovery::implClassName) //
			.collect(Collectors.toList());
		assertEquals(List.of(Red.class.getName()), colors);
	}

	/** An unindexed type yields nothing rather than failing. */
	@Test
	public void testUnknownTypeYieldsNothing() {
		assertTrue(discoverer().discover(Runnable.class).isEmpty());
	}

	/** Priorities come from the index, so they can sort without loading. */
	@Test
	public void testPriorityFromMetadata() {
		final List<Discovery<Shape>> sorted = discoverer().discover(Shape.class)
			.stream() //
			.sorted((a, b) -> Double.compare(b.priority(), a.priority())) //
			.collect(Collectors.toList());
		assertEquals(Square.class.getName(), sorted.get(0).implClassName());
		assertEquals(Circle.class.getName(), sorted.get(1).implClassName());
	}

	/** A class loader that records every class it is asked to load. */
	private static class RecordingClassLoader extends ClassLoader {

		private final Set<String> loaded = ConcurrentHashMap.newKeySet();

		RecordingClassLoader(final ClassLoader parent) {
			super(parent);
		}

		@Override
		protected Class<?> loadClass(final String name, final boolean resolve)
			throws ClassNotFoundException
		{
			loaded.add(name);
			return super.loadClass(name, resolve);
		}
	}

	private static Discovery<Shape> byClassName(
		final List<Discovery<Shape>> discoveries, final String className)
	{
		return discoveries.stream() //
			.filter(d -> className.equals(d.implClassName())) //
			.findFirst().orElse(null);
	}
}
