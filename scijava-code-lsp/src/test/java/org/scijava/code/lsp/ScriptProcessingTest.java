/*-
 * #%L
 * Language Server Protocol bridge for SciJava code intelligence.
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

package org.scijava.code.lsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.log.LogLevel;
import org.scijava.log.LogService;

/**
 * Tests {@link ScriptProcessing}.
 *
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public class ScriptProcessingTest {

	private Context context;
	private ScriptProcessing processing;

	@BeforeEach
	public void setUp() {
		context = new Context();
		processing = new ScriptProcessing(() -> context);
	}

	@AfterEach
	public void tearDown() {
		context.dispose();
	}

	/**
	 * Tests that new ({@code #@}) script declarations are removed when they
	 * start a line in the script.
	 */
	@Test
	public void testNewStyleParameters() {
		final ScriptProcessing.Processed p = processing.of("" + //
			"#@ String name\n" + // 0
			"#@script(name=\"x\")\n" + // 1: directives too
			"x = 1\n" + // 2
			"#@output int n\n" + // 3: even after code
			"  #@ int indented"); // 4: but only at a line's start
		assertEquals(Arrays.asList(0, 1, 3), removedLines(p));
		assertEquals(Arrays.asList("name: String", "n: int"), declarations(p));
	}

	/**
	 * Tests that old ({@code // @}) parameter declarations are removed, in the
	 * script's header only.
	 */
	@Test
	public void testOldStyleParameters() {
		final ScriptProcessing.Processed p = processing.of("" + //
			"// @String name\n" + // 0
			"\n" + // 1: blank lines keep the header going
			"# @int n\n" + // 2: in any language's comments
			"// Greets n\n" + // 3: but words end it, even in comments
			"// @String late"); // 4
		assertEquals(Arrays.asList(0, 2), removedLines(p));
		assertEquals(Arrays.asList("name: String", "n: int"), declarations(p));
	}

	/** Tests that shebangs are removed, on the first line only. */
	@Test
	public void testShebang() {
		final ScriptProcessing.Processed p = processing.of("" + //
			"#!/usr/bin/env groovy\n" + // 0
			"#!late"); // 1
		assertEquals(List.of(0), removedLines(p));
		assertTrue(p.parameters.isEmpty());
	}

	/**
	 * Tests script parameter lines are removed even when incomplete
	 */
	@Test
	public void testRejectedParameters() {
		final ScriptProcessing.Processed p = processing.of("" + //
			"#@ NoSuchType thing\n" + // 0: unknown type, so Object
			"#@ Str\n" + // 1: no name yet
			"#@ String (label=\"Unfinished\n" + // 2: unclosed
			"x = 1");
		// All three parameter lines are removed
		assertEquals(Arrays.asList(0, 1, 2), removedLines(p));
		// even though only one is complete
		assertEquals(Collections.singletonMap("thing", Object.class),
			p.parameters);
	}

	@Test
	public void testNothingToProcess() {
		final ScriptProcessing.Processed p = processing.of("x = 1\n# comment");
		assertEquals(Collections.emptySet(), p.removedLines);
		assertEquals(Collections.emptyMap(), p.parameters);
	}

	// -- Helper methods --

	/** The lines removed from a processed script, in order. */
	private static List<Integer> removedLines(
		final ScriptProcessing.Processed p)
	{
		return new ArrayList<>(new TreeSet<>(p.removedLines));
	}

	/** The variables declared by a processed script, in order: "name: Type". */
	private static List<String> declarations(
		final ScriptProcessing.Processed p)
	{
		final List<String> result = new ArrayList<>();
		p.parameters.forEach((name, type) -> result.add(name + ": " + type
			.getSimpleName()));
		return result;
	}

	/** The variables a script's parameters define, as processed. */
	private Map<String, Class<?>> parameters(final String text) {
		return processing.of(text).parameters;
	}
}
