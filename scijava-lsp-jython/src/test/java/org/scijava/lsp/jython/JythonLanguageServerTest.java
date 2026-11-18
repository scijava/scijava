/*-
 * #%L
 * Jython code intelligence for SciJava, via Jython's own parser.
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
package org.scijava.lsp.jython;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.lsp.Environment;
import org.scijava.lsp.LanguageServerService;
import org.scijava.lsp.Positions;
import org.scijava.lsp.ScriptDialect;
import org.scijava.lsp.ScriptSession;
import org.scijava.lsp.jvm.ClassIndex;
import org.scijava.plugin.Plugin;
import org.scijava.script.AbstractScriptLanguage;
import org.scijava.script.ScriptLanguage;

/**
 * Tests {@link JythonLanguageServer}
 *
 * @author Gabriel Selzer
 */
public class JythonLanguageServerTest {

	private static final ScriptLanguage TEST_LANGUAGE =
		new AbstractScriptLanguage()
	{

		@Override
		public ScriptEngine getScriptEngine() {
			return null;
		}
	};

	/** A language whose dialect prepends a header (see {@link HeaderDialect}). */
	private static final ScriptLanguage HEADER_LANGUAGE =
		new AbstractScriptLanguage()
	{

		@Override
		public ScriptEngine getScriptEngine() {
			return null;
		}
	};

	private static Context context;

	@BeforeAll
	public static void startContext() {
		context = new Context(LanguageServerService.class);
	}

	@AfterAll
	public static void disposeContext() {
		context.dispose();
	}

	@Test
	public void testMemberCompletion() {
		// 's' is a Python str, as in Jython: its members are Python's, not
		// java.lang.String's.
		final List<String> texts = complete("s = \"hello\"\ns.");
		assertTrue(texts.contains("upper"), "expected upper among " + texts);
		assertTrue(texts.contains("find"), "expected find among " + texts);
		assertFalse(texts.contains("length"), "unexpected length in " + texts);
		assertFalse(texts.contains("charAt"), "unexpected charAt in " + texts);
	}

	@Test
	public void testNameAtStartOfLine() {
		final List<String> texts = complete("greeting = \"hi\"\ngree");
		assertTrue(texts.contains("greeting"), "expected greeting among " + texts);
	}

	@Test
	public void testVariableNameNotDuplicated() {
		// A String variable must not also offer String's constructors.
		final List<String> texts = complete("greeting = \"hi\"\nx = gree");
		assertEquals(1, texts.stream().filter("greeting"::equals).count(), texts
			.toString());
		assertFalse(texts.contains(
			" greeting"), "unexpected leading space in " + texts);
	}

	@Test
	public void testKeywordsAndBuiltins() {
		assertTrue(complete("imp").contains("import"));
		final List<String> texts = complete("x = le");
		assertTrue(texts.contains("len"), "expected len among " + texts);
		assertFalse(texts.stream()
			.anyMatch(t -> t.contains(".")), "unexpected builtin member in " + texts);
	}

	@Test
	public void testImportedClassName() {
		ClassIndex.ensureCache();
		final List<CompletionItem> items = completions(
			"from java.util import ArrayList\nArrayList");
		final List<String> texts = labels(items);
		assertTrue(texts.contains(
			"ArrayList"), "expected ArrayList among " + texts);
		// Once as a name; otherwise as its constructors.
		assertEquals(1, items.stream().filter(c -> c.getLabel().equals(
			"ArrayList") && c.getLabelDetails() == null).count(),
			"expected one plain ArrayList among " + texts);
	}

	@Test
	public void testJdkClassImport() {
		ClassIndex.ensureCache();
		final List<String> texts = complete("from java.util import ArrayLi");
		assertTrue(texts.contains(
			"from java.util import ArrayList"), "expected ArrayList import among " + texts);
	}

	/**
	 * Tests that accepting a Java class imports it at the top of the script,
	 * when the script imports nothing yet, even if its dialect prepends a
	 * header (which is not part of the script, so cannot take the import).
	 */
	@Test
	public void testClassImportAfterHeader() {
		ClassIndex.ensureCache();
		final LanguageServerService servers = context.service(
			LanguageServerService.class);
		final String importList = "from java.util import ArrayList";

		try (ScriptSession session = new ScriptSession(servers, HEADER_LANGUAGE)) {
			// Find the CompletionItem that is importing java.util.ArrayList
			List<CompletionItem> items = items(session, "#@String s\n#@Double d\n\nArrayLi");
			CompletionItem arrayList = items.stream()
					.filter(c -> c.getKind() == CompletionItemKind.Class)
					.filter(c -> importList.equals(c.getDetail()))
					.findFirst().orElseThrow();
			// NB: At the top of the script itself, as the header is not part of it.
			final Position top = new Position(0, 0);
			assertEquals(Collections.singletonList(new TextEdit(new Range(top, top),
					importList + "\n")), arrayList.getAdditionalTextEdits());
		}
	}

	/**
	 * Tests that accepting a Java class, when the script's last line is an
	 * import without a line break, puts its import on a line of its own.
	 */
	@Test
	public void testClassImportAtEOF() {
		ClassIndex.ensureCache();
		final String additionalEdit = "from java.util import ArrayList";
		final String code = "x = ArrayLi\nfrom java.lang import Math";
		// Find the CompletionItem that is importing java.util.ArrayList.
		final List<CompletionItem> items = items(session(new Environment()), code,
			code.indexOf('\n'));
		final CompletionItem arrayList = items.stream() //
			.filter(c -> c.getKind() == CompletionItemKind.Class) //
			.filter(c -> additionalEdit.equals(c.getDetail())) //
			.findFirst().orElseThrow();
		// The insertion point should be at the end of the script.
		final Position end = Positions.position(code, code.length());
		assertEquals(Collections.singletonList(new TextEdit(new Range(end, end),
			"\n" + additionalEdit)), arrayList.getAdditionalTextEdits());
	}

	@Test
	public void testInheritedMembers() {
		// NB: The stubs of the packages java.util refers to (e.g. java.lang, of
		// Object) are written in the background, after the script is opened: so
		// ask until the server knows them, as an editor would as typing goes on.
		// (The server keeps running, and must be told of the stubs written.)
		final String code = "from java.util import ArrayList\nlist = ArrayList()\nlist.";
		final long deadline = System.currentTimeMillis() + 30_000;
		List<String> texts = complete(code);
		while (!texts.contains("toString") && System.currentTimeMillis() < deadline) {
			texts = complete(code);
		}
		// members come from superinterfaces
		assertTrue(texts.contains("add"), "expected add among " + texts);
		// members come from superclasses
		assertTrue(texts.contains("toString"), "expected toString among " + texts);
	}

	@Test
	public void testMerge() {
		// The Python server's first, in its order; then Java classes, only for
		// names it has not completed.
		final CompletionList merged = JythonLanguageServer.merge(
			new CompletionList(Arrays.asList(new CompletionItem("max"),
				new CompletionItem("min"))), new CompletionList(Arrays.asList(
					new CompletionItem("Math"), new CompletionItem("max"))));
		assertEquals(Arrays.asList("max", "min", "Math"), labels(merged
			.getItems()));
	}

	@Test
	public void testCallablesAreSnippets() {
		final CompletionItem ensure = completions("from java.util import ArrayList\n" +
			"a = ArrayList()\na.ensureCap").stream().filter(c -> c.getLabel().equals(
				"ensureCapacity")).findFirst().orElse(null);
		assertNotNull(ensure);
		final String params = ensure.getLabelDetails().getDetail();
		assertTrue(params.matches("\\(\\w+: int\\)"), params);
		assertTrue(ensure.getInsertText().startsWith("ensureCapacity(${1:"),
			ensure.getInsertText());
	}

	@Test
	public void testDunders() {
		// Names like __name__: Jython's, not Python 3's.
		final List<String> dunders = labels(completions("x = __"));
		assertTrue(dunders.contains("__name__"), dunders.toString());
		assertFalse(dunders.contains("__build_class__"), dunders.toString());
	}

	@Test
	public void testSignatureHelp() {
		// A Java method's signature, and the argument typed.
		final SignatureHelp max = help("from java.lang import Math\n" +
			"Math.max(1.5, ");
		assertFalse(max.getSignatures().isEmpty());
		assertEquals(Integer.valueOf(1), max.getActiveParameter());
		// Constructors (one signature each).
		final SignatureHelp list = help("from java.util import ArrayList\nArrayList(");
		assertTrue(list.getSignatures().stream().anyMatch(s -> s.getLabel()
			.contains("Collection")), list.toString());
		// None outside calls.
		final SignatureHelp none = help("x = 1");
		assertTrue(none == null || none.getSignatures().isEmpty());
	}

	// -- Helper methods --

	/** A session about plain Jython, with a server of its own. */
	private static ScriptSession session(final Environment env) {
		final LanguageServerService servers = context.service(
			LanguageServerService.class);
		return env.live().map(live -> new ScriptSession(servers, TEST_LANGUAGE,
			live)).orElseGet(() -> new ScriptSession(servers, TEST_LANGUAGE));
	}

	/**
	 * Makes {@code TEST_LANGUAGE} Jython, with no header, so that the tests
	 * reach the real {@link JythonLanguageServerPlugin} through a
	 * {@link ScriptSession}. (The real Jython dialect lives elsewhere.)
	 */
	@Plugin(type = ScriptDialect.class)
	public static class TestDialect implements ScriptDialect {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language == TEST_LANGUAGE;
		}

		@Override
		public String outputLanguage() {
			return "jython";
		}

		@Override
		public Header header(final Map<String, Variable> variables) {
			return new Header("", Collections.emptySet());
		}
	}

	/**
	 * Makes {@code HEADER_LANGUAGE} Jython, with a header that imports nothing
	 * (as one declaring a parameter might).
	 */
	@Plugin(type = ScriptDialect.class)
	public static class HeaderDialect implements ScriptDialect {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language == HEADER_LANGUAGE;
		}

		@Override
		public String outputLanguage() {
			return "jython";
		}

		@Override
		public Header header(final Map<String, Variable> variables) {
			return new Header("name = None", Collections.emptySet());
		}
	}

	private static SignatureHelp help(final String code) {
		try (ScriptSession session = session(new Environment())) {
			return session.signatureHelp(code, code.length()).get(30,
				TimeUnit.SECONDS);
		}
		catch (final Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	private static List<String> complete(final String code) {
		return labels(completions(code));
	}

	private static List<String> labels(final List<CompletionItem> items) {
		return items.stream().map(CompletionItem::getLabel).collect(Collectors
			.toList());
	}

	private static List<CompletionItem> completions(final String code) {
		return items(session(new Environment()), code);
	}

	private static List<CompletionItem> items(final ScriptSession session,
		final String code)
	{
		return items(session, code, code.length());
	}

	private static List<CompletionItem> items(final ScriptSession session,
		final String code, final int offset)
	{
		try (ScriptSession s = session) {
			final List<CompletionItem> items = new java.util.ArrayList<>(s.completion(
				code, offset).get(30, TimeUnit.SECONDS).getItems());
			items.sort(Comparator.comparing(CompletionItem::getSortText));
			return items;
		}
		catch (final Exception exc) {
			throw new RuntimeException(exc);
		}
	}
}
