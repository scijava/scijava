/*-
 * #%L
 * Integration tests for the scijava-context library.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.context.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.scijava.context.Context;
import org.scijava.discovery.Discovery;

/**
 * Tests plugin discovery: the {@code @Plugin} annotation, the index the
 * annotation processor writes at build time, and {@link Context#plugins}.
 * <p>
 * This lives in its own module so the whole path is real - the annotation is
 * processed by an actual build, and the implementations sit in a package that
 * is opened to the container but exported to nobody.
 * </p>
 *
 * @author Curtis Rueden
 */
public class PluginDiscoveryTest {

	private static final String LOUD = "org.scijava.context.test.impl.LoudShouter";
	private static final String QUIET =
		"org.scijava.context.test.impl.QuietShouter";

	/** Annotate a class, and it is found. No configuration file to write. */
	@Test
	public void testPluginsAreDiscovered() {
		try (final Context context = Context.create()) {
			final List<String> names = context.plugins(Shouter.class).stream() //
				.map(Discovery::implClassName) //
				.collect(Collectors.toList());
			// NB: highest priority first.
			assertEquals(List.of(LOUD, QUIET), names);
		}
	}

	/**
	 * The point of an index: metadata is readable without loading the plugin
	 * class, which is what makes building a large menu cheap.
	 */
	@Test
	public void testMetadataWithoutLoadingTheClass() {
		try (final Context context = Context.create()) {
			final Discovery<Shouter> loud = context.plugins(Shouter.class).get(0);

			assertEquals(LOUD, loud.implClassName());
			assertEquals("loud", loud.attr("name").orElse(null));
			assertEquals("Loud shouter", loud.attr("label").orElse(null));
			assertEquals("11", loud.attr("volume").orElse(null));
			assertEquals(100.0, loud.priority());

			// None of that constructed anything.
			assertFalse(constructedLoudShouter(),
				"reading metadata constructed the plugin");

			// Asking for the object is what constructs it.
			assertEquals("HELLO", loud.get().shout());
			assertTrue(constructedLoudShouter());
		}
	}

	/** The implementations stay encapsulated: opened to the container only. */
	@Test
	public void testImplementationsAreNotExported() {
		final Module module = Shouter.class.getModule();
		assertTrue(module.isNamed(), "not running on the module path");
		assertFalse(module.isExported("org.scijava.context.test.impl"),
			"the implementation package must not be exported");
		assertTrue(module.isOpen("org.scijava.context.test.impl",
			Context.class.getModule()), "the container needs reflective access");
	}

	/** An unindexed type yields nothing rather than failing. */
	@Test
	public void testUnknownPluginType() {
		try (final Context context = Context.create()) {
			assertTrue(context.plugins(Runnable.class).isEmpty());
		}
	}

	/**
	 * NB: read through the module system rather than touching the class, since
	 * referring to the static field would itself load it.
	 */
	private static boolean constructedLoudShouter() {
		try {
			final Class<?> c = Class.forName(LOUD);
			return c.getField("constructed").getBoolean(null);
		}
		catch (final ReflectiveOperationException exc) {
			throw new AssertionError(exc);
		}
	}
}
