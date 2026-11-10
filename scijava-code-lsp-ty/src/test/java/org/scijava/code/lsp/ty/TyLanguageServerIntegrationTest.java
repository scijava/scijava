/*-
 * #%L
 * Python code intelligence for SciJava, via ty.
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

package org.scijava.code.lsp.ty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InsertTextFormat;
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
 * {@link TyLanguageServerPlugin}), with a real ty server, on plain
 * Python, as editors use it.
 * <p>
 * ty's environment is built first, which requires network access (unless
 * built already); if it cannot be, the tests are skipped.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class TyLanguageServerIntegrationTest {

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
		// NB: Skipped if ty's environment cannot be built (e.g. offline), rather
		// than waiting for servers that cannot start.
		try {
			TyLauncher.python();
		}
		catch (final Exception exc) {
			assumeTrue(false, "ty's environment could not be built: " + exc);
		}
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
		assertNotNull(dumps.getDocumentation());
	}

	/**
	 * ty returns some nonsensical completions. For example, completions include
	 * everything that contains the typed letters; e.g. "False" and
	 * "else", for "le". This test ensures such completions are filtered out.
	 */
	@Test
	public void testCompletionFiltering() throws Exception {
		// Completions not starting with the typed characters are not present.
		final List<String> le = labels(completeUntil("x = le", "len"));
		assertTrue(le.stream().allMatch(l -> l.toLowerCase().startsWith("le")));
		// Underscore names are not returned unless asked for.
		final List<String> du = labels(completeUntil("import json\njson.du",
			"dumps"));
		assertFalse(du.stream().anyMatch(l -> l.startsWith("_")));
		completeUntil("import json\njson._", "__name__");
	}

	/**
	 * ty returns functions' signatures only as text, in each completion's
	 * detail; e.g. "def len(obj: Sized, /) -> int". This test ensures such
	 * completions are converted to snippets, with their parameters and return
	 * types parsed correctly from the many forms of signatures.
	 */
	@Test
	public void testCallables() throws Exception {
		// Brackets in types (their commas do not separate parameters).
		final String dumps = detail("import json\njson.dum", "dumps");
		assertTrue(dumps.contains(", tuple[str, str] | None separators, " +
			"((Any, /) -> Any) | None default, "), dumps);
		// Positional-only ones; and the return type.
		final CompletionItem len = callable("x = le", "len");
		assertEquals("(Sized obj)", len.getLabelDetails().getDetail());
		assertEquals("int", len.getLabelDetails().getDescription());
		// Methods.
		assertEquals("()", detail("s = 'x'\ns.isu", "isupper"));
		// Strings in types; keyword-only ones; and **kwargs.
		assertEquals("(tuple[int, str] a, Literal[\"x\", \"y\"] b, kw)", detail(
			"from typing import Literal\ndef func(a: tuple[int, str] = (1, \",\"), " +
				"/, *, b: Literal['x', 'y'] = 'x', **kw):\n    pass\nfu", "func"));
		// Not classes, which ty gives no signatures of.
		assertNotEquals(InsertTextFormat.Snippet, item("import json\njson.JSONDec",
			"JSONDecoder").getInsertTextFormat());
	}

	/**
	 * ty returns overloaded functions' signatures all together; e.g.
	 * "Overload[[_T](i: SupportsNext[_T], /) -> _T, [_T, _VT](i:
	 * SupportsNext[_T], default: _VT, /) -> _T | _VT]", for next. This test
	 * ensures such completions are returned as snippets, with the first
	 * overload's parameters.
	 */
	@Test
	public void testOverloads() throws Exception {
		// The first overload's signature (next(i), not next(i, default)); after
		// its type parameters ([_T]).
		final CompletionItem next = callable("x = nex", "next");
		assertEquals("(SupportsNext[_T] i)", next.getLabelDetails().getDetail());
		assertEquals("_T", next.getLabelDetails().getDescription());
	}

	/**
	 * ty returns the same completions in imports as elsewhere, signatures
	 * included; e.g. "dumps", for "from json import du". This test ensures
	 * such completions are not returned as snippets: an import names things,
	 * rather than calling them.
	 */
	@Test
	public void testImportsAreNames() throws Exception {
		// An import names things, rather than calling them.
		final CompletionItem dumps = completeUntil("from json import du", "dumps")
			.stream().filter(c -> c.getLabel().equals("dumps")).findFirst().get();
		assertNotEquals(InsertTextFormat.Snippet, dumps.getInsertTextFormat());
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
		// NB: ty labels a signature with its parameters only, without its name.
		assertTrue(help.getSignatures().get(0).getLabel().startsWith("(obj"));
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
		public Header header(final Map<String, Variable> variables) {
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

	/** The completion of the given name, once it appears. */
	private static CompletionItem item(final String code, final String name)
		throws Exception
	{
		return completeUntil(code, name).stream().filter(c -> c.getLabel().equals(
			name)).findFirst().get();
	}

	/** The completion of the given name, as a callable. */
	private static CompletionItem callable(final String code, final String name)
		throws Exception
	{
		final CompletionItem item = item(code, name);
		assertEquals(InsertTextFormat.Snippet, item.getInsertTextFormat(), name);
		return item;
	}

	/** The parameters of the callable completion of the given name. */
	private static String detail(final String code, final String name)
		throws Exception
	{
		return callable(code, name).getLabelDetails().getDetail();
	}

	private static List<String> labels(final List<CompletionItem> items) {
		return items.stream().map(CompletionItem::getLabel).collect(Collectors
			.toList());
	}
}
