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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.script.AbstractScriptEngine;
import org.scijava.script.AbstractScriptLanguage;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * Tests {@link DefaultCodeCompleter} and {@link CodeCompletionService}.
 *
 * @author Curtis Rueden
 */
public class CodeCompleterTest {

	private Context context;
	private ScriptLanguage hello;

	@BeforeEach
	public void setUp() {
		context = new Context(ScriptService.class, CodeCompletionService.class);
		hello = context.service(ScriptService.class).getLanguageByName("Hello");
	}

	@AfterEach
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testCodeCompleter() {
		final ScriptEngine engine = hello.getScriptEngine();
		final CodeCompleter ac = new DefaultCodeCompleter();

		// test all members of a bound object
		engine.put("thing", new Object());
		final String code1 = "thing.";
		final CompletionResult result = ac.complete(new CompletionRequest(code1,
			hello, engine));
		// "thing." -> caret 6, prefix "" -> replace span starts at 6
		assertEquals(code1.length(), result.replaceStart());
		assertEquals(Arrays.asList("equals", "getClass", "hashCode", "notify",
			"notifyAll", "toString", "wait"), insertionTexts(result));

		// test member prefix filtering
		engine.put("hello", "world");
		final String code2 = "hello.c";
		final CompletionResult cWords = ac.complete(new CompletionRequest(code2,
			hello, engine));
		// "hello.c" -> caret 7, prefix "c" -> replace span starts at 6
		assertEquals(code2.length() - 1, cWords.replaceStart());
		assertEquals(Arrays.asList("CASE_INSENSITIVE_ORDER", "charAt", "chars",
			"codePointAt", "codePointBefore", "codePointCount", "codePoints",
			"compareTo", "compareToIgnoreCase", "concat", "contains",
			"contentEquals", "copyValueOf"), insertionTexts(cWords));
	}

	@Test
	public void testCodeCompletionService() {
		final CodeCompletionService completion = context.service(
			CodeCompletionService.class);
		final ScriptEngine engine = hello.getScriptEngine();
		engine.put("greeting", "world");

		// No language-specific plugin is registered for "Hello", so the
		// service must fall back to the default reflective completer.
		assertNull(completion.getCompleterPlugin(hello));
		final CompletionResult result = completion.complete(new CompletionRequest(
			"greet", hello, engine));
		assertTrue(insertionTexts(result).contains("greeting"));
	}

	private static List<String> insertionTexts(final CompletionResult result) {
		return result.completions().stream().map(Completion::insertionText)
			.collect(Collectors.toList());
	}

	/** A trivial script language, for testing. */
	@Plugin(type = ScriptLanguage.class)
	public static class HelloLanguage extends AbstractScriptLanguage {

		@Override
		public ScriptEngine getScriptEngine() {
			return new HelloEngine();
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("Hello");
		}

		@Override
		public List<String> getExtensions() {
			return Arrays.asList("hello");
		}
	}

	private static class HelloEngine extends AbstractScriptEngine {

		{
			engineScopeBindings = new HelloBindings();
		}

		@Override
		public Object eval(final String script) {
			return script;
		}

		@Override
		public Object eval(final Reader reader) {
			final StringBuilder sb = new StringBuilder();
			try {
				for (int c = reader.read(); c >= 0; c = reader.read()) {
					sb.append((char) c);
				}
			}
			catch (final IOException exc) {
				throw new RuntimeException(exc);
			}
			return sb.toString();
		}
	}

	private static class HelloBindings extends HashMap<String, Object> implements
		Bindings
	{

		private static final long serialVersionUID = 1L;
	}
}
