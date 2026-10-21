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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.python.jsr223.PyScriptEngineFactory;
import org.scijava.Context;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.ScriptDialect;
import org.scijava.code.lsp.ScriptSession;
import org.scijava.code.lsp.jvm.ClassIndex;
import org.scijava.plugin.Plugin;
import org.scijava.script.AbstractScriptLanguage;
import org.scijava.script.ScriptLanguage;

/**
 * Tests {@link JythonLanguageServer}: what it makes of plain Jython, as its
 * clients send it.
 *
 * @author Curtis Rueden
 */
public class JythonLanguageServerTest {

	/** A test fixture: a script language whose scripts are plain Jython. */
	private static final ScriptLanguage TEST_LANGUAGE =
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
		// Static analysis: 's' is a String, so 's.' completes to String members.
		final List<String> texts = complete("s = \"hello\"\ns.");
		assertTrue(texts.contains("s.length"), "expected s.length among " + texts);
		assertTrue(texts.contains("s.charAt"), "expected s.charAt among " + texts);
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

	@Test
	public void testInheritedMembers() {
		final List<String> texts = complete(
				"from java.util import ArrayList\nlist = ArrayList()\nlist.");
		// members come from superinterfaces
		assertTrue(texts.contains("list.add"));
		// members come from superclasses
		assertTrue(texts.contains("list.toString"));
	}

	@Test
	public void testCallablesAreSnippets() {
		final CompletionItem charAt = completions("s = 'x'\ns.charA").stream()
			.filter(c -> c.getLabel().equals("s.charAt")).findFirst().orElse(null);
		assertNotNull(charAt);
		assertEquals("(int arg0)", charAt.getLabelDetails().getDetail().replaceAll(
			" \\w+\\)$", " arg0)"));
		assertTrue(charAt.getTextEdit().getLeft().getNewText().startsWith(
			"s.charAt(${1:"));
	}

	@Test
	public void testArgumentChoices() {
		// In a call's arguments: the variables of the parameter's type first.
		final List<CompletionItem> items = completions(
			"from java.lang import Math\nx = 1.5\ny = 'a'\nMath.abs(");
		assertTrue(!items.isEmpty(), items.toString());
		assertEquals("x", items.get(0).getLabel());
		assertEquals(Boolean.TRUE, items.get(0).getPreselect());
	}

	@Test
	public void testInterpreterBindings() throws Exception {
		// Code typed into a running interpreter: its variables.
		final ScriptEngine engine = new PyScriptEngineFactory().getScriptEngine();
		engine.put("injected", new java.util.ArrayList<String>());
		engine.eval("from java.util import Collections, HashMap\n" +
			"m = HashMap()\n" +
			"def helper(): pass\n");
		final Environment env = new Environment().withLive(engine.getContext());
		final Supplier<ScriptSession> live = () -> session(env);

		assertTrue(labels(items(live.get(), "m.putIfAb")).contains(
			"m.putIfAbsent"));
		assertTrue(labels(items(live.get(), "injected.si")).contains(
			"injected.size"));
		assertTrue(labels(items(live.get(), "x = hel")).contains("helper"));
		// An imported Java class offers its static members.
		assertTrue(labels(items(live.get(), "Collections.emptyL")).contains(
			"Collections.emptyList"));
		// Python internals are not offered.
		assertFalse(labels(items(live.get(), "x = __")).contains("__name__"));
	}

	@Test
	public void testSignatureHelp() {
		// A Python float fits the floating-point overloads, which come first.
		final SignatureHelp max = help("from java.lang import Math\n" +
			"Math.max(1.5, ");
		assertEquals(Integer.valueOf(1), max.getActiveParameter());
		assertEquals("[double, float, int, long]", describe(max));
		// A Python int fits the integral ones, and converts to the others.
		assertEquals("[int, long, double, float]", describe(help(
			"from java.lang import Math\nMath.max(1, 2")));
		// Arguments' types come from the analysis, e.g. of variables.
		final SignatureHelp indexOf = help("s = 'abc'\nc = 'b'\n" +
			"s.indexOf(c, ");
		assertTrue(describe(indexOf).startsWith(
			"[java.lang.String"), describe(indexOf));
		// Constructors.
		assertTrue(describe(help("from java.util import ArrayList\n" +
			"ArrayList(")).contains("java.util.Collection"));
		// None outside calls.
		assertNull(help("x = 1"));
	}

	// -- Helper methods --

	/** A session about plain Jython, with a server of its own. */
	private static ScriptSession session(final Environment env) {
		final LanguageServerService servers = context.service(
			LanguageServerService.class);
		return env.live().map(live -> new ScriptSession(servers, TEST_LANGUAGE,
			live)).orElseGet(() -> new ScriptSession(servers, TEST_LANGUAGE));
	}

	@Plugin(type = ScriptDialect.class)
	public static class TestDialect implements ScriptDialect {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language == TEST_LANGUAGE;
		}

		@Override
		public String languageId() {
			return "test";
		}

		@Override
		public Header header(final Map<String, Class<?>> parameters) {
			return new Header("", Collections.emptySet());
		}
	}

	@Plugin(type = LanguageServerPlugin.class)
	public static class TestServers implements LanguageServerPlugin {

		@Override
		public boolean supports(final String languageId) {
			return "test".equals(languageId);
		}

		@Override
		public LanguageServer create(final String languageId,
			final Environment environment)
		{
			return new JythonLanguageServer(environment);
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

	/** Each signature's first parameter type, in order. */
	private static String describe(final SignatureHelp help) {
		return help.getSignatures().stream().map(s -> {
			final String first = s.getParameters().isEmpty() ? "()" : s
				.getParameters().get(0).getLabel().getLeft();
			final String type = first.contains(" ") ? first.substring(0, first
				.lastIndexOf(' ')) : first;
			return type;
		}).collect(Collectors.toList()).toString();
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
		try (ScriptSession s = session) {
			final List<CompletionItem> items = new java.util.ArrayList<>(s.completion(
				code, code.length()).get(30, TimeUnit.SECONDS).getItems());
			items.sort((a, b) -> a.getSortText().compareTo(b.getSortText()));
			return items;
		}
		catch (final Exception exc) {
			throw new RuntimeException(exc);
		}
	}
}
