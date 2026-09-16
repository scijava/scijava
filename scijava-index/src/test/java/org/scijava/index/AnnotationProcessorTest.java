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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link AnnotationProcessor} by compiling a class the way a downstream
 * project would: with this component on the annotation processor path, and
 * nothing else configured.
 * <p>
 * This is the mechanism users actually depend on - annotate a class, and it is
 * discoverable with no configuration files to write - so it is worth testing
 * through javac rather than by calling the indexer directly.
 * </p>
 *
 * @author Curtis Rueden
 */
public class AnnotationProcessorTest {

	@Test
	public void testProcessorWritesIndex(@TempDir final Path outputDir)
		throws IOException
	{
		final String source = "" + //
			"package test;\n" + //
			"import org.scijava.index.Shape;\n" + //
			"import org.scijava.index.Widget;\n" + //
			"@Widget(type = Shape.class, label = \"A triangle\")\n" + //
			"public class Triangle implements Shape {\n" + //
			"  public String describe() { return \"triangle\"; }\n" + //
			"}\n";

		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		final List<String> options = Arrays.asList( //
			"-classpath", System.getProperty("java.class.path"), //
			"-processorpath", System.getProperty("java.class.path"), //
			"-processor", AnnotationProcessor.class.getName(), //
			"-d", outputDir.toString());
		final boolean compiled = compiler.getTask(null, null, null, options, null,
			List.of(new SourceString("test.Triangle", source))).call();
		assertTrue(compiled, "test source failed to compile");

		// The processor must have written an index for the annotation.
		final Path index = outputDir.resolve("META-INF/json/" + Widget.class
			.getName());
		assertTrue(Files.exists(index), "no index written to " + index);
		final String json = new String(Files.readAllBytes(index),
			StandardCharsets.UTF_8);
		assertTrue(json.contains("test.Triangle"), "index lacks the class: " +
			json);
		assertTrue(json.contains("A triangle"), "index lacks the metadata: " +
			json);

		// ...and the index must be readable, without loading the class.
		final ClassLoader loader = new URLClassLoader( //
			new URL[] { outputDir.toUri().toURL() }, getClass().getClassLoader());
		final IndexDiscoverer<Widget> discoverer = new IndexDiscoverer<>( //
			Widget.class, item -> item.annotation().type().getName(), //
			item -> java.util.Map.of("label", item.annotation().label()), //
			item -> item.annotation().priority(), loader);
		final List<String> found = discoverer.discover(Shape.class).stream() //
			.map(d -> d.implClassName()) //
			.filter(n -> n.startsWith("test.")) //
			.collect(java.util.stream.Collectors.toList());
		assertEquals(List.of("test.Triangle"), found);
	}

	/** A source file held in a string. */
	private static class SourceString extends SimpleJavaFileObject {

		private final String code;

		SourceString(final String className, final String code) {
			super(URI.create("string:///" + className.replace('.', '/') +
				Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
			return code;
		}
	}
}
