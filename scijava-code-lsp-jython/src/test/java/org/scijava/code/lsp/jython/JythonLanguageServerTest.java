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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
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
import org.scijava.plugin.Parameter;
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

	/** The stubs of Java packages, written where the tests run. */
	private static final JavaStubs STUBS = new JavaStubs(new File("target",
		"jython-stubs").getAbsoluteFile(), JythonLanguageServerTest.class
			.getClassLoader());

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

	@Test
	public void testInheritedMembers() {
		final List<String> texts = complete(
				"from java.util import ArrayList\nlist = ArrayList()\nlist.");
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
		assertEquals("(int arg0)", ensure.getLabelDetails().getDetail().replaceAll(
			" \\w+\\)$", " arg0)"));
		assertTrue(ensure.getInsertText().startsWith("ensureCapacity(${1:"),
			ensure.getInsertText());
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
			"putIfAbsent"));
		assertTrue(labels(items(live.get(), "injected.si")).contains("size"));
		assertTrue(labels(items(live.get(), "x = hel")).contains("helper"));
		// An imported Java class offers its static members.
		assertTrue(labels(items(live.get(), "Collections.emptyL")).contains(
			"emptyList"));
		// Names like __name__: Jython's, not Python 3's.
		final List<String> dunders = labels(items(live.get(), "x = __"));
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
		// Constructors.
		assertTrue(help("from java.util import ArrayList\nArrayList(")
			.getSignatures().get(0).getLabel().contains("Collection"));
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

		@Parameter
		private LanguageServerService servers;

		@Override
		public boolean supports(final String languageId) {
			return "test".equals(languageId);
		}

		@Override
		public LanguageServer create(final String languageId,
			final Environment environment)
		{
			return new JythonLanguageServer(environment, servers.launch("python",
				JythonLanguageServer.environment(STUBS)), STUBS);
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
