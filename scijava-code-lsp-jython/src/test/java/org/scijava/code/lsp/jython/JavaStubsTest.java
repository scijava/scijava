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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.FileChangeType;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link JavaStubs}, with the JDK's classes.
 *
 * @author Gabriel Selzer
 */
public class JavaStubsTest {

	private final JavaStubs stubs = new JavaStubs(new File("unused"), getClass()
		.getClassLoader());

	@Test
	public void testStub() {
		final Set<String> refs = new TreeSet<>();
		final String stub = stubs.stub("java.util", refs);
		// Classes, with their superclasses and interfaces.
		assertTrue(stub.contains("\nclass ArrayList(AbstractList, List, " +
			"RandomAccess, java.lang.Cloneable, java.io.Serializable):\n"), stub);
		// Nested classes, inside their outer ones.
		final String map = section(stub, "Map");
		assertTrue(map.contains("\n    class Entry(java.lang.Object):\n"), map);
		// Constants as class variables.
		assertTrue(section(stub, "Calendar").contains(
			"\n    YEAR: typing.ClassVar[int]\n"), stub);
		// Overloads, constructors too; static methods; Java types as Python's.
		// NB: Parameter names are the Java ones only if compiled with them.
		final String list = section(stub, "ArrayList");
		has(list, "    @typing.overload\n" +
			"    def __init__\\(self, \\w+: Collection\\) -> None: \\.\\.\\.\n");
		has(list, "    @typing.overload\n" +
			"    def add\\(self, \\w+: int, \\w+: java\\.lang\\.Object\\) -> None");
		has(stub, "    @staticmethod\n" +
			"    def asList\\(\\*\\w+: java\\.lang\\.Object\\) -> List: \\.\\.\\.\n");
		has(stub, "    def copyOf\\(\\w+: list\\[float\\], \\w+: int\\) -> " +
			"list\\[float\\]: \\.\\.\\.\n");
		// Getters as properties, as Jython offers them.
		assertTrue(list.contains("\n    empty: bool\n"), list);
		// Their names as Jython's: getURL() as URL, not uRL.
		final String connection = section(stubs.stub("java.net", new TreeSet<>()),
			"URLConnection");
		assertTrue(connection.contains("\n    URL: URL\n"), connection);
		assertFalse(connection.contains("uRL"), connection);
		// Not Python (e.g. Outer$Inner): left out.
		assertFalse(stub.contains("$"), stub);
		// Other packages: imported, and noted.
		assertTrue(stub.contains("\nimport java.io\n"), stub);
		assertTrue(refs.containsAll(Arrays.asList("java.io", "java.lang",
			"java.util.function")), refs.toString());
		assertFalse(refs.contains("java.util"), refs.toString());
		// Fields.
		assertTrue(section(stubs.stub("java.awt", new TreeSet<>()),
			"GridBagConstraints").contains("\n    gridx: int\n"));
		// No classes: no stub.
		assertNull(stubs.stub("os", new TreeSet<>()));
	}

	@Test
	public void testEnsure() throws Exception {
		// NB: Under target, not a temporary directory: the stubs of the packages
		// referred to (and of theirs) go on being written after the test.
		final File dir = new File("target", "java-stubs-test").getAbsoluteFile();
		delete(dir);
		final JavaStubs stubs = new JavaStubs(dir, getClass().getClassLoader());
		final CompletableFuture<Void> util = new CompletableFuture<>();
		stubs.listen(events -> {
			if (events.stream().anyMatch(e -> e.getType() == FileChangeType.Changed &&
				e.getUri().endsWith("/java/util/__init__.pyi"))) util.complete(null);
		});
		stubs.ensure(Collections.singleton("java.util.concurrent")).get(30,
			TimeUnit.SECONDS);
		assertTrue(new File(dir, "java/util/concurrent/__init__.pyi").isFile());
		// Parents are packages too.
		assertTrue(new File(dir, "java/__init__.pyi").isFile());
		// The packages referred to follow (e.g. java.util).
		util.get(30, TimeUnit.SECONDS);
		assertTrue(new String(Files.readAllBytes(new File(dir,
			"java/util/__init__.pyi").toPath()), StandardCharsets.UTF_8).contains(
				"\nclass ArrayList("));
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

	private static void delete(final File dir) throws java.io.IOException {
		if (!dir.exists()) return;
		try (java.util.stream.Stream<java.nio.file.Path> paths = Files.walk(dir
			.toPath()))
		{
			paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p
				.toFile().delete());
		}
	}
}
