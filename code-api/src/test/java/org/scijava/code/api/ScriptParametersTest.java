/*-
 * #%L
 * Core API for SciJava code intelligence features.
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

package org.scijava.code.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.log.LogService;

/**
 * Tests {@link CodeCompletionService#scriptParameters(String)}.
 *
 * @author Curtis Rueden
 */
public class ScriptParametersTest {

	private Context context;
	private CodeCompletionService service;

	@BeforeEach
	public void setUp() {
		context = new Context();
		service = context.service(CodeCompletionService.class);
	}

	@AfterEach
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testInputsAndOutputs() {
		final Map<String, Class<?>> params = service.scriptParameters("" + //
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
		assertTrue(service.scriptParameters("x = 1\n").isEmpty());
		// The implicit return value output is not a declared variable.
		assertTrue(service.scriptParameters("#@script(name=\"x\")\n").isEmpty());
	}

	@Test
	public void testHalfTypedAndUnresolvable() {
		final List<Object> logged = new ArrayList<>();
		context.service(LogService.class).addLogListener(logged::add);

		final Map<String, Class<?>> params = service.scriptParameters("" + //
			"#@ String name\n" + //
			"#@ NoSuchType thing\n" + //
			"#@ Str\n" + //
			"#@ String (label=\"Unfinished\n");
		assertEquals(Arrays.asList("name", "thing"), //
			new ArrayList<>(params.keySet()));
		assertSame(String.class, params.get("name"));
		assertSame(Object.class, params.get("thing"));
		assertEquals(0, logged.size(), "unexpected log output: " + logged);
	}

	@Test
	public void testCached() {
		final Map<String, Class<?>> first = service.scriptParameters(
			"#@ String name\nx = 1\n");
		// Non-declaration edits reuse the parse.
		assertSame(first, service.scriptParameters("#@ String name\nx = 12\n"));
	}
}
