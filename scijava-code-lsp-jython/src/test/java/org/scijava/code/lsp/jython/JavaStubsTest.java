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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link JavaStubs}.
 *
 * @author Gabriel Selzer
 */
public class JavaStubsTest {

	private static final String PKG =
		"org.scijava.code.lsp.jython";

	/** Lists just the example classes. */
	private static final Function<String, List<String>> CLASSES = pkg -> PKG
		.equals(pkg) ? Collections.singletonList(PKG + ".JavaStubsTest") : pkg
			.equals("java.util") ? Collections.singletonList("java.util.Locale")
				: Collections.emptyList();

	@Test
	public void testStub() {
		final JavaStubs stubs = new JavaStubs(new File("unused"), getClass()
			.getClassLoader(), pkg -> Collections.singletonList(PKG +
				".JavaStubsTest"));
		final Set<String> refs = new TreeSet<>();
		final String stub = stubs.stub(PKG, refs);
		// The test class: its nested classes, inside.
		assertTrue(stub.contains("\nclass JavaStubsTest(java.lang.Object):\n"), stub);
		assertTrue(stub.contains(
			"\n    class Picture(JavaStubsTest.Base, java.lang.Cloneable):\n"), stub);
		// Fields; constant ones as class variables.
		assertTrue(stub.contains("        MAX: typing.ClassVar[int]\n"), stub);
		assertTrue(stub.contains("        title: str\n"), stub);
		// Overloads, constructors too; static methods; Java types as Python's.
		// NB: Parameter names are the Java ones only if compiled with them.
		has(stub, "        @typing.overload\n" +
			"        def __init__\\(self, \\w+: str\\) -> None: \\.\\.\\.\n");
		has(stub, "        @typing.overload\n" +
			"        def resize\\(self, \\w+: int, \\w+: int\\) -> " +
			"JavaStubsTest\\.Picture: \\.\\.\\.\n");
		has(stub, "        @typing.overload\n" +
			"        def resize\\(self, \\w+: float\\) -> JavaStubsTest\\.Picture");
		has(stub, "        @staticmethod\n" +
			"        def open\\(\\w+: str, \\*\\w+: str\\) -> " +
			"JavaStubsTest\\.Picture: \\.\\.\\.\n");
		assertTrue(stub.contains("        def pixels(self) -> list[float]: ...\n"), stub);
		assertTrue(stub.contains(
			"        def locale(self) -> java.util.Locale: ...\n"), stub);
		// Getters as properties, as Jython offers them.
		assertTrue(stub.contains("        width: int\n"), stub);
		assertTrue(stub.contains("        opaque: bool\n"), stub);
		// Not Python: left out.
		assertFalse(stub.contains("def lambda"), stub);
		// Other packages: imported, and noted.
		assertTrue(stub.contains("\nimport java.util\n"), stub);
		assertEquals(new TreeSet<>(Arrays.asList("java.lang", "java.util")), refs);
		// No classes: no stub.
		assertNull(new JavaStubs(new File("unused"), getClass().getClassLoader(),
			pkg -> Collections.emptyList()).stub("os", new TreeSet<>()));
	}

	@Test
	public void testEnsure() throws Exception {
		final File dir = Files.createTempDirectory("jython-stubs").toFile();
		try {
			final JavaStubs stubs = new JavaStubs(dir, getClass().getClassLoader(),
				CLASSES);
			stubs.ensure(Collections.singleton(PKG)).get(30, TimeUnit.SECONDS);
			final File stub = new File(dir, PKG.replace('.', File.separatorChar) +
				File.separator + "__init__.pyi");
			assertTrue(stub.isFile());
			// Parents are packages too.
			assertTrue(new File(dir, "org" + File.separator + "__init__.pyi")
				.isFile());
			// The packages referred to follow (here, java.util; java.lang has no
			// classes listed).
			for (int i = 0; i < 100 && !new File(dir, "java/util/__init__.pyi")
				.isFile(); i++) Thread.sleep(50);
			assertTrue(new String(Files.readAllBytes(new File(dir,
				"java/util/__init__.pyi").toPath()), StandardCharsets.UTF_8).contains(
					"class Locale("));
			assertFalse(new File(dir, "java/lang/__init__.pyi").exists());
		}
		finally {
			try (java.util.stream.Stream<java.nio.file.Path> paths = Files.walk(dir
				.toPath()))
			{
				paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p
					.toFile().delete());
			}
		}
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

	// -- Example classes --

	public static class Base {

		public boolean isOpaque() {
			return true;
		}
	}

	public static class Picture extends Base implements Cloneable {

		public static final int MAX = 1 << 16;
		public String title;

		public Picture(final String title) {
			this.title = title;
		}

		public Picture(final String title, final int width) {
			this(title);
		}

		public Picture resize(final int width, final int height) {
			return this;
		}

		public Picture resize(final double scale) {
			return this;
		}

		public static Picture open(final String path, final String... options) {
			return null;
		}

		public double[] pixels() {
			return null;
		}

		public java.util.Locale locale() {
			return null;
		}

		public int getWidth() {
			return 0;
		}
	}
}
