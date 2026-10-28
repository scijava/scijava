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

package org.scijava.code.lsp.impl;

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

	@Test
	public void testRemovedLines() {
		// The lines SciJava removes before running a script: its shebang, its
		// #@ lines (parameters and directives), and old-style parameters in its
		// header.
		final ScriptProcessing.Processed p = processing.of("" + //
			"#!/usr/bin/env groovy\n" + // 0
			"// @String old\n" + // 1
			"#@script(name=\"x\")\n" + // 2
			"#@ int n\n" + // 3
			"println n // @not\n" + // 4: not these, which are the script's
			"  #@ indented\n" + // 5
			"# comment\n" + // 6
			"// @late\n" + // 7
			"#!late"); // 8
		assertEquals(Arrays.asList(0, 1, 2, 3), new ArrayList<>(new TreeSet<>(
			p.removedLines)));
		assertEquals(Arrays.asList("old", "n"), new ArrayList<>(p.parameters
			.keySet()));
	}

	@Test
	public void testRejectedParameters() {
		// Parameters SciJava rejects are likely being typed: their lines are
		// removed all the same, and the variables they declare known.
		final ScriptProcessing.Processed p = processing.of("" + //
			"#@ NoSuchType thing\n" + //
			"#@ Str\n" + //
			"x = 1");
		assertEquals(Arrays.asList(0, 1), new ArrayList<>(new TreeSet<>(
			p.removedLines)));
		assertSame(Object.class, p.parameters.get("thing"));
	}

	@Test
	public void testNothingToProcess() {
		final ScriptProcessing.Processed p = processing.of("x = 1\n# comment");
		assertEquals(Collections.emptySet(), p.removedLines);
		assertEquals(Collections.emptyMap(), p.parameters);
	}

	@Test
	public void testInputsAndOutputs() {
		final Map<String, Class<?>> params = parameters("" + //
			"#@ String (label=\"Please enter your name\") name\n" + //
			"#@output String greeting\n" + //
			"#@ int count\n" + //
			"\n" + //
			"greeting = \"Hello, \" + name + \"!\"\n");
		assertEquals(Arrays.asList("name", "count", "greeting"), //
			new ArrayList<>(params.keySet()));
		assertSame(String.class, params.get("name"));
		assertSame(int.class, params.get("count"));
		assertSame(String.class, params.get("greeting"));
	}

	@Test
	public void testNoParameters() {
		assertTrue(parameters("x = 1\n").isEmpty());
		// The implicit return value output is not a declared variable.
		assertTrue(parameters("#@script(name=\"x\")\n").isEmpty());
	}

	@Test
	public void testHalfTypedAndUnresolvable() {
		final Map<String, Class<?>> params = parameters("" + //
			"#@ String name\n" + //
			"#@ NoSuchType thing\n" + //
			"#@ Str\n" + //
			"#@ String (label=\"Unfinished\n");
		assertEquals(Arrays.asList("name", "thing"), //
			new ArrayList<>(params.keySet()));
		assertSame(String.class, params.get("name"));
		assertSame(Object.class, params.get("thing"));
	}

	@Test
	public void testScriptDirective() {
		final List<String> warnings = new ArrayList<>();
		context.service(LogService.class).addLogListener(message -> {
			if (message.level() == LogLevel.WARN) warnings.add(message.text());
		});
		final Map<String, Class<?>> params = parameters("" + //
			"#@script(env=\"env.toml\", scheme=\"pixi.toml\")\n" + //
			"#@ String name\n");
		assertEquals(Arrays.asList("name"), new ArrayList<>(params.keySet()));
		// The directive is not a parameter, so it is not warned about.
		assertTrue(warnings.isEmpty(), warnings.toString());
	}

	@Test
	public void testCached() {
		final Map<String, Class<?>> first = parameters(
			"#@ String name\nx = 1\n");
		// Non-declaration edits reuse the parse.
		assertSame(first, parameters("#@ String name\nx = 12\n"));
	}

	// -- Helper methods --

	/** The variables a script's parameters define, as processed. */
	private Map<String, Class<?>> parameters(final String text) {
		return processing.of(text).parameters;
	}
}
