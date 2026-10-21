/*-
 * #%L
 * Python code intelligence for SciJava, via jedi-language-server.
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

package org.scijava.code.lsp.jedi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngine;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.SignatureHelp;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.Positions;
import org.scijava.code.lsp.ScriptDialect;
import org.scijava.code.lsp.ScriptSession;
import org.scijava.plugin.Plugin;
import org.scijava.script.AbstractScriptLanguage;
import org.scijava.script.ScriptLanguage;

/**
 * End-to-end tests of the {@code python} server (see
 * {@link PythonServerPlugin}), with a real jedi-language-server, on plain
 * Python, as editors use it.
 * <p>
 * The first request builds jedi-language-server's environment, which
 * requires network access.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class PythonLanguageServerIntegrationTest {

	/** How long to wait for environments to build. */
	private static final long BUILD_TIMEOUT = TimeUnit.MINUTES.toMillis(15);

	/** A test fixture: a script language whose scripts are plain Python. */
	private static final ScriptLanguage TEST_LANGUAGE =
		new AbstractScriptLanguage()
	{

		@Override
		public ScriptEngine getScriptEngine() {
			return null;
		}
	};

	private static Context context;
	private static LanguageServerService servers;
	private static ScriptSession session;

	@BeforeAll
	public static void setUp() {
		context = new Context(LanguageServerService.class);
		servers = context.service(LanguageServerService.class);
		session = session();
	}

	@AfterAll
	public static void tearDown() {
		if (session != null) session.close();
		if (context != null) context.dispose();
	}

	@Test
	public void testStandardLibrary() throws Exception {
		// The server knows the standard library, with signatures and
		// documentation.
		final List<CompletionItem> completions = completeUntil(
			"import json\njson.du", "dumps");
		final CompletionItem dumps = completions.stream().filter(c -> c.getLabel()
			.equals("dumps") && c.getLabelDetails() != null).findFirst().get();
		assertTrue(dumps.getLabelDetails().getDetail().startsWith(
			"(Any obj, bool skipkeys"), dumps.getLabelDetails().getDetail());
		assertTrue(dumps.getDocumentation() != null || session.resolve(dumps).get(
			1, TimeUnit.MINUTES).getDocumentation() != null);
		report("import json\njson.l", "loads", "o");
	}

	@Test
	public void testSignatureHelp() throws Exception {
		final String code = "import json\njson.dumps(obj, ";
		final long deadline = System.currentTimeMillis() + BUILD_TIMEOUT;
		SignatureHelp help = null;
		while (help == null && System.currentTimeMillis() < deadline) {
			help = session.signatureHelp(code, code.length()).get(1,
				TimeUnit.MINUTES);
		}
		assertTrue(help.getSignatures().get(0).getLabel().contains("dumps("));
		assertFalse(help.getSignatures().get(0).getParameters().isEmpty());
		assertEquals(Integer.valueOf(1), help.getActiveParameter());
	}

	@Test
	public void testHover() throws Exception {
		final String code = "import json\njson.dumps(1)";
		final long deadline = System.currentTimeMillis() + BUILD_TIMEOUT;
		Hover hover = null;
		while (hover == null && System.currentTimeMillis() < deadline) {
			hover = session.hover(code, code.indexOf("dumps") + 2).get(1,
				TimeUnit.MINUTES);
		}
		assertTrue(hover.getContents().getRight().getValue().contains("JSON"),
			String.valueOf(hover));
		// The word's range.
		assertEquals(Positions.position(code, code.indexOf("dumps")), hover
			.getRange().getStart());
		assertEquals(Positions.position(code, code.indexOf("dumps") + 5), hover
			.getRange().getEnd());
	}

	@Test
	public void testDiagnostics() throws Exception {
		// A syntax error, on the second line.
		final String code = "x = 1\ny = (1 +\n";
		final long deadline = System.currentTimeMillis() + BUILD_TIMEOUT;
		List<Diagnostic> problems = Collections.emptyList();
		while (problems.isEmpty() && System.currentTimeMillis() < deadline) {
			problems = problems(code);
		}
		assertFalse(problems.isEmpty());
		assertTrue(problems.get(0).getRange().getStart().getLine() >= 1, problems
			.toString());
		// Fixed: no more problems.
		assertTrue(problems(code.replace("(1 +", "(1 + 2)")).isEmpty());
	}

	// -- Helper methods --

	/** A session about plain Python, with a server of its own. */
	private static ScriptSession session() {
		return new ScriptSession(servers, TEST_LANGUAGE);
	}

	@Plugin(type = ScriptDialect.class)
	public static class TestDialect implements ScriptDialect {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language == TEST_LANGUAGE;
		}

		@Override
		public String languageId() {
			return "python";
		}

		@Override
		public Header header(final Map<String, Class<?>> parameters) {
			return new Header("", Collections.emptySet());
		}
	}

	/** The problems found in a script, in a session of its own. */
	private static List<Diagnostic> problems(final String code)
		throws Exception
	{
		final CompletableFuture<List<Diagnostic>> found =
			new CompletableFuture<>();
		try (ScriptSession s = session()) {
			s.setDiagnosticsListener(found::complete);
			s.update(code);
			return found.get(120, TimeUnit.SECONDS);
		}
	}

	private static List<CompletionItem> completeUntil(final String code,
		final String expected) throws Exception
	{
		return completeUntil(session, code, expected);
	}

	/** Completes until the expected completion appears (environments build). */
	private static List<CompletionItem> completeUntil(final ScriptSession s,
		final String code, final String expected) throws Exception
	{
		final long deadline = System.currentTimeMillis() + BUILD_TIMEOUT;
		while (System.currentTimeMillis() < deadline) {
			final List<CompletionItem> completions = complete(s, code);
			if (completions.stream().anyMatch(c -> c.getLabel().equals(expected))) {
				return completions;
			}
			Thread.sleep(1000);
		}
		fail("No '" + expected + "' completion for: " + code);
		return null;
	}

	/** Completes at the end of the code. */
	private static List<CompletionItem> complete(final ScriptSession s,
		final String code) throws Exception
	{
		return s.completion(code, code.length()).get(1, TimeUnit.MINUTES)
			.getItems();
	}

	/**
	 * Prints how long completions take, once warm: for another keystroke of a
	 * word the server has answered (cached); and for a new word.
	 */
	private static void report(final String code, final String expected,
		final String more) throws Exception
	{
		completeUntil(code, expected);
		long t0 = System.nanoTime();
		final int n = complete(session, code + more).size();
		final long cached = System.nanoTime() - t0;
		final String other = code.substring(0, code.lastIndexOf('.') + 1) + "s";
		t0 = System.nanoTime();
		final CompletionList all = session.completion(other, other.length()).get(
			1, TimeUnit.MINUTES);
		final long arrived = System.nanoTime() - t0;
		System.out.println("[completion timing] same word (cached): " + cached /
			1000000 + " ms, " + n + " completions; new word: " + all.getItems()
				.size() + " completions after " + arrived / 1000000 + " ms");
	}
}
