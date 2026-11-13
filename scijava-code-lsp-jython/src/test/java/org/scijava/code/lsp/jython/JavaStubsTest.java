/*-
 * #%L
 * Jython code intelligence for SciJava, via Jython's own parser.
 * %%
 * Copyright (C) 2020 - 2025 SciJava developers.
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
package org.scijava.code.lsp.jython;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link JavaStubs}, with the JDK's classes.
 *
 * @author Gabriel Selzer
 */
public class JavaStubsTest {

	private static final JavaStubs STUBS = JavaStubs.instance();

	/** The packages java.util's stub refers to. */
	private static final Set<String> UTIL_REFS = new TreeSet<>();

	/** The stub of java.util (written once: it takes a while). */
	private static String util;

	@BeforeAll
	public static void stubUtil() {
		util = STUBS.stub("java.util", UTIL_REFS);
	}

	@Test
	public void testClasses() {
		// With their superclasses and interfaces.
		assertTrue(util.contains("\nclass ArrayList(AbstractList, List, " +
			"RandomAccess, java.lang.Cloneable, java.io.Serializable):\n"), util);
	}

	@Test
	public void testNestedClasses() {
		// Inside their outer ones.
		final String map = section(util, "Map");
		assertTrue(map.contains("\n    class Entry(java.lang.Object):\n"), map);
	}

	@Test
	public void testFields() {
		assertTrue(section(STUBS.stub("java.awt", new TreeSet<>()),
			"GridBagConstraints").contains("\n    gridx: int\n"));
	}

	@Test
	public void testConstants() {
		// As class variables.
		final String calendar = section(util, "Calendar");
		assertTrue(calendar.contains("\n    YEAR: typing.ClassVar[int]\n"),
			calendar);
	}

	@Test
	public void testOverloads() {
		// NB: Parameter names are the Java ones only if compiled with them.
		final String list = section(util, "ArrayList");
		// Constructors too.
		has(list, "    @typing.overload\n" +
			"    def __init__\\(self, \\w+: Collection\\) -> None: \\.\\.\\.\n");
		has(list, "    @typing.overload\n" +
			"    def add\\(self, \\w+: int, \\w+: java\\.lang\\.Object\\) -> None");
	}

	@Test
	public void testStaticMethods() {
		// With varargs.
		has(section(util, "Arrays"), "    @staticmethod\n" +
			"    def asList\\(\\*\\w+: java\\.lang\\.Object\\) -> List: \\.\\.\\.\n");
	}

	@Test
	public void testTypes() {
		// Java types as Python's, e.g. arrays as lists.
		has(section(util, "Arrays"), "    def copyOf\\(\\w+: list\\[float\\], " +
			"\\w+: int\\) -> list\\[float\\]: \\.\\.\\.\n");
	}

	@Test
	public void testReadOnlyProperties() {
		// A getter, with no setter.
		final String list = section(util, "ArrayList");
		assertTrue(list.contains("\n    @property\n    def empty(self) -> bool: " +
			"...\n"), list);
		// Not settable, either way.
		assertFalse(list.contains("\n    empty: bool\n"), list);
		assertFalse(list.contains("@empty.setter"), list);
	}

	@Test
	public void testProperties() {
		// A getter, with a setter.
		final String calendar = section(util, "Calendar");
		assertTrue(calendar.contains("\n    timeInMillis: int\n"), calendar);
		assertFalse(calendar.contains("def timeInMillis("), calendar);
	}

	@Test
	public void testWriteOnlyProperties() {
		// A setter, with no getter.
		final String random = section(util, "Random");
		assertTrue(random.contains("\n    seed: int\n"), random);
	}

	@Test
	public void testPropertyNames() {
		// As Jython's: getURL() as URL, not uRL.
		final String connection = section(STUBS.stub("java.net",
			new TreeSet<>()), "URLConnection");
		assertTrue(connection.contains("\n    def URL(self) -> URL: ...\n"),
			connection);
		assertFalse(connection.contains("uRL"), connection);
	}

	@Test
	public void testReferences() {
		// Other packages: imported, and noted.
		assertTrue(util.contains("\nimport java.io\n"), util);
		assertTrue(UTIL_REFS.containsAll(Arrays.asList("java.io", "java.lang",
			"java.util.function")), UTIL_REFS.toString());
		assertFalse(UTIL_REFS.contains("java.util"), UTIL_REFS.toString());
	}

	@Test
	public void testNoClasses() {
		// No stub.
		assertNull(STUBS.stub("os", new TreeSet<>()));
	}

	@Test
	public void testEnsure() throws Exception {
		// NB: The stubs may have been written already, by other tests sharing
		// the instance: either way, they're there once ensured.
		final File dir = STUBS.dir();
		STUBS.ensure(Collections.singleton("java.util.concurrent")).get(30,
			TimeUnit.SECONDS);
		assertTrue(new File(dir, "java/util/concurrent/__init__.pyi").isFile());
		// Parents are packages too.
		assertTrue(new File(dir, "java/__init__.pyi").isFile());
		// The packages referred to follow (e.g. java.util), in the background.
		final File util = new File(dir, "java/util/__init__.pyi");
		final long deadline = System.currentTimeMillis() + 30_000;
		while (!contains(util, "\nclass ArrayList(") && System
			.currentTimeMillis() < deadline)
		{
			Thread.sleep(100);
		}
		assertTrue(contains(util, "\nclass ArrayList("));
	}

	private static boolean contains(final File file, final String text)
		throws java.io.IOException
	{
		return file.isFile() && new String(Files.readAllBytes(file.toPath()),
			StandardCharsets.UTF_8).contains(text);
	}

	@Test
	public void testImports() {
		assertEquals(new TreeSet<>(Arrays.asList("ij", "java.util", "os",
			"ij.process")), JavaStubs.imports("from ij import IJ, ImagePlus\n" +
				"import java.util\nimport os, sys\n  from ij.process import *\n" +
				"x = 1 # import nothing\nfrom . import y\n"));
	}

	private static void has(final String stub, final String regex) {
		assertTrue(java.util.regex.Pattern.compile(regex)
			.matcher(stub).find(), regex + " in:\n" + stub);
	}

	/** The stub of a (top-level) class, from a package's stub. */
	private static String section(final String stub, final String name) {
		final int start = stub.indexOf("\nclass " + name + "(");
		assertTrue(start >= 0, "no class " + name);
		final int end = stub.indexOf("\nclass ", start + 1);
		return stub.substring(start, end < 0 ? stub.length() : end);
	}

}
