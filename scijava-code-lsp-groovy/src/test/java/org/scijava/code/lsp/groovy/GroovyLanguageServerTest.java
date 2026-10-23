/*-
 * #%L
 * Groovy code intelligence for SciJava, via Groovy's own compiler.
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

package org.scijava.code.lsp.groovy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
 * Tests {@link GroovyLanguageServer}
 *
 * @author Gabriel Selzer
 */
public class GroovyLanguageServerTest {

	/** A test fixture: a script language whose scripts are plain Groovy. */
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
	public void testMembersOfInferredTypes() {
		assertTrue(complete("def s = 'hello'\ns.toUpp").contains("toUpperCase"));
		// Types flow through method calls...
		assertTrue(complete("def x = new ArrayList()\n" +
			"def y = x.subList(0, 1)\ny.isEm").contains("isEmpty"));
		assertTrue(complete("'abc'.toUpperCase().trim().len").contains("length"));
		// ...and through closures.
		assertTrue(complete("[1, 2].collect { it * 2 }.isEm").contains(
			"isEmpty"));
		// Declared types count too.
		assertTrue(complete("List items = null\nitems.isEm").contains("isEmpty"));
		// Inherited members are included.
		assertTrue(complete("def x = new ArrayList()\nx.containsA").contains(
			"containsAll"));
	}

	/**
	 * Variables assigned without a declaration ({@code x = ...}) are not local
	 * variables but script bindings. The static type checker sees as
	 * dynamic (of unknown type) so {@link TypedAST} types them itself.
	 */
	@Test
	public void testMembersOfBindings() {
		// Variables assigned without a declaration are typed as assigned...
		assertTrue(complete("z = \"abc\"\nz.toU").contains("toUpperCase"));
		assertTrue(complete("z = new ArrayList()\ny = z.subList(0, 1)\ny.isEm")
			.contains("isEmpty"));
		// ...as last assigned.
		final List<String> reassigned = complete(
			"z = 'abc'\nz = new File('a')\nz.getN");
		assertTrue(reassigned.contains("getName"));
		// ...and offered with their type.
		final List<CompletionItem> items = items("zed = 'abc'\nx = ze");
		final CompletionItem zed = items.stream().filter(c -> "zed".equals(c
			.getLabel())).findFirst().orElse(null);
		assertTrue(zed != null, String.valueOf(labels(items)));
		assertEquals("java.lang.String zed", zed.getDetail());
	}

	@Test
	public void testDiagnostics() throws Exception {
		// Syntax errors, and unresolvable classes: errors, where they are.
		assertEquals(Collections.singletonList("Error@1:11-12 Unexpected input: '+'"),
			problems("sigma = 1.5\ndef x = (1 +\n"));
		assertEquals(Collections.singletonList(
			"Error@0:0-14 unable to resolve class foo.Bar"), problems(
				"import foo.Bar\nx = 1\n"));
		assertEquals(Collections.singletonList(
			"Error@0:0-9 unable to resolve class Foo"), problems("new Foo()\n"));
		// Members a known type lacks: warnings, also through calls and bindings.
		assertEquals(Collections.singletonList(
			"Warning@1:0-24 Cannot find matching method java.lang.String#fooBar()."),
			problems("def s = 'abc'\ns.toUpperCase().fooBar()\n"));
		assertEquals(Collections.singletonList(
			"Warning@1:0-10 Cannot find matching method java.lang.String#fooBar()."),
			problems("z = 'abc'\nz.fooBar()\n"));
		assertEquals(Collections.singletonList(
			"Warning@1:0-5 No such property: nme for class: java.io.File"), problems(
				"def f = new File('a')\nf.nme\n"));
		// Dynamic code, which Groovy resolves when the script runs: nothing.
		for (final String fine : new String[] { "x = 1\nprintln x\n",
			"def m = [:]\nm.foo = 1\nprintln m.foo\n",
			"def o = something()\no.foo()\n", "def c = { it * 2 }\nc(3)\n",
			"def x\nx = 'a'\nx.size()\n", "[1, 2].collect { it }.sum()\n",
			"println undefinedThing\n", "class A { def go() { bar() } }\n" })
		{
			assertEquals(Collections.emptyList(), problems(fine), fine);
		}
	}

	@Test
	public void testCallablesAreSnippets() {
		final CompletionItem upper = items("def s = 'x'\ns.toUpp").stream().filter(
			c -> c.getLabelDetails() != null && "()".equals(c.getLabelDetails()
				.getDetail())).findFirst().orElse(null);
		assertTrue(upper != null);
		assertEquals("toUpperCase()", upper.getTextEdit().getLeft().getNewText());
		final CompletionItem locale = items("def s = 'x'\ns.toUpp").stream().filter(
			c -> "(java.util.Locale arg0)".equals(c.getLabelDetails().getDetail()) ||
				c.getLabelDetails().getDetail().startsWith("(java.util.Locale "))
			.findFirst().orElse(null);
		assertTrue(locale != null);
		assertTrue(locale.getTextEdit().getLeft().getNewText().startsWith(
			"toUpperCase(${1:"));
		assertEquals("java.lang.String", locale.getLabelDetails()
			.getDescription());
	}

	@Test
	public void testStaticMembers() {
		final List<String> texts = complete("Math.ma");
		assertTrue(texts.contains("max"));
		// Only static members of a class.
		assertFalse(complete("Integer.intV").contains("intValue"));
		assertTrue(complete("Integer.parseI").contains("parseInt"));
	}

	@Test
	public void testGroovyMethods() {
		// The GDK adds methods to Java classes.
		assertTrue(complete("[1, 2, 3].coll").contains("collect"));
		assertTrue(complete("def s = 'abc'\ns.rev").contains("reverse"));
		assertTrue(complete("new File('x').eachL").contains("eachLine"));
		final CompletionItem each = items("[1, 2, 3].eac").stream().filter(
			c -> c.getLabel().equals("each")).findFirst().orElse(null);
		assertTrue(each != null && each.getDetail().startsWith("GDK"));
	}

	@Test
	public void testScriptClasses() {
		final List<String> texts = complete("class Foo {\n" + //
			"  int bar\n" + //
			"  String baz() { 'x' }\n" + //
			"}\n" + //
			"def f = new Foo()\n" + //
			"f.ba");
		assertTrue(texts.contains("bar"), texts.toString());
		assertTrue(texts.contains("baz"), texts.toString());
	}

	@Test
	public void testVariableScopes() {
		assertTrue(complete("def greeting = 'hi'\ndef x = gree").contains(
			"greeting"));
		// Closure parameters are visible inside the closure...
		assertTrue(complete("[1].each { item ->\n  ite").contains("item"));
		// ...but not variables declared in a closure that has ended.
		assertFalse(complete("[1].each { def inner = it }\ninn").contains(
			"inner"));
		// Nor variables declared after the caret.
		assertFalse(complete("def a = lat").contains("later"));
		// For loop variables are visible in the loop.
		assertTrue(complete("for (element in [1, 2]) {\n  elem").contains(
			"element"));
	}

	@Test
	public void testIncompleteCode() {
		// Unclosed brackets before the caret.
		assertTrue(complete("def s = 'x'\nif (true) {\n  s.toUpp").contains(
			"toUpperCase"));
		assertTrue(complete("def s = 'x'\nprintln(s.toUpp").contains(
			"toUpperCase"));
		// Unresolvable classes do not prevent type inference.
		assertTrue(complete("def z = new NoSuchClass()\ndef s = 'x'\ns.toUpp")
			.contains("toUpperCase"));
		// A syntax error above the caret: the caret's line is still analyzed.
		assertTrue(complete("def a = (]\n'abc'.toUpp").contains("toUpperCase"));
	}

	@Test
	public void testKeywords() {
		// keywords get completed
		assertTrue(complete("ret").contains("return"));
		// primitives get completed
		assertTrue(complete("tr").contains("true"));
		assertTrue(complete("do").contains("double"));
	}

	@Test
	public void testNumbers() {
		// A decimal being typed is not completed.
		assertTrue(complete("def x = 1.").isEmpty());
		assertTrue(complete("def x = 1.5").isEmpty());
		// But numbers are objects, whose members are.
		assertTrue(complete("5.ti").contains("times"));
		assertTrue(complete("def x = 1.5.ro").contains("round"));
	}

	@Test
	public void testClassNamesWithAutoImport() {
		ClassIndex.ensureCache();
		final CompletionItem atomic = items("def a = new AtomicInt").stream()
			.filter(c -> c.getLabel().equals("AtomicInteger")).findFirst().orElse(
				null);
		assertTrue(atomic != null);
		assertEquals(1, atomic.getAdditionalTextEdits().size());
		assertEquals("import java.util.concurrent.atomic.AtomicInteger\n", atomic
			.getAdditionalTextEdits().get(0).getNewText());
		// Already imported (or imported by default): no edit.
		final CompletionItem list = items("def a = new ArrayLis").stream().filter(
			c -> c.getLabel().equals("ArrayList")).findFirst().orElse(null);
		assertTrue(list != null && list.getAdditionalTextEdits() == null);
	}

	@Test
	public void testImports() {
		ClassIndex.ensureCache();
		assertTrue(complete("import java.util.concurrent.atomic.AtomicInt")
			.contains("java.util.concurrent.atomic.AtomicInteger"));
		assertTrue(complete("java.util.concurrent.atomic.AtomicInt").contains(
			"AtomicInteger"));
	}

	@Test
	public void testStringsAndComments() {
		assertTrue(complete("def s = 'a.b.").isEmpty());
		assertTrue(complete("// s.toUpp").isEmpty());
		assertTrue(complete("/* x.").isEmpty());
	}

	@Test
	public void testInterpreterBindings() throws Exception {
		// Code typed into a running interpreter: its variables, by their values'
		// types.
		final ScriptContext live = new SimpleScriptContext();
		live.setAttribute("injected", new ArrayList<String>(),
			ScriptContext.ENGINE_SCOPE);
		final List<String> texts = labels(items(session(new Environment().withLive(
			live)), "injected.si"));
		assertTrue(texts.contains("size"), texts.toString());
		// Variables it gains later, though the code is the same.
		try (ScriptSession session = session(new Environment().withLive(live))) {
			final String code = "later.si";
			assertFalse(labels(session.completion(code, code.length()).get(30,
				TimeUnit.SECONDS).getItems()).contains("size"));
			live.setAttribute("later", new ArrayList<String>(),
				ScriptContext.ENGINE_SCOPE);
			assertTrue(labels(session.completion(code, code.length()).get(30,
				TimeUnit.SECONDS).getItems()).contains("size"));
		}
	}

	@Test
	public void testSignatureHelp() {
		// A decimal (a BigDecimal) fits the floating-point overloads, which come
		// first; the integral ones do not. The caret is in the second argument.
		final SignatureHelp max = help("Math.max(1.5, ");
		assertEquals(Integer.valueOf(1), max.getActiveParameter());
		assertEquals("[double, float, int, long]", describe(max));
		// An integer fits the integral overloads, and converts to the others.
		assertEquals("[int, long, double, float]", describe(help(
			"Math.max(1, 2")));
		// Nothing typed yet: all overloads.
		assertEquals(4, help("Math.max(").getSignatures().size());
		// Arguments' types come from the analysis, e.g. of variables.
		final SignatureHelp indexOf = help("def s = 'abc'\ndef c = 'b'\n" +
			"s.indexOf(c, ");
		assertEquals("java.lang.String", describe(indexOf).replaceAll(
			"^\\[([^,]*),.*", "$1"));
		// Constructors, and the script's own functions.
		assertTrue(describe(help("new StringBuilder(")).contains(
			"java.lang.String"));
		final SignatureHelp area = help("def area(int w, int h) { w * h }\n" +
			"area(3, ");
		assertEquals("[int]", describe(area));
		// The innermost call, and none outside calls.
		assertEquals(Integer.valueOf(0), help("Math.max(Math.abs(")
			.getActiveParameter());
		assertNull(help("def x = 1"));
	}

	@Test
	public void testArgumentChoices() {
		// In a call's arguments: the variables fitting the parameter first.
		final List<CompletionItem> items = items(
			"def a = new ArrayList()\ndef s = 'x'\nCollections.sort(");
		assertEquals("a", items.get(0).getLabel());
		assertEquals(Boolean.TRUE, items.get(0).getPreselect());
		assertTrue(labels(items).contains("s"));
	}

	@Test
	public void testImportOffset() {
		// Already imported: no import needed.
		assertEquals(-1, GroovyLanguageServer.importOffset(
			"import java.util.concurrent.atomic.*\n",
			"java.util.concurrent.atomic.AtomicInteger"));
		// Inserted after the last import.
		assertEquals(20, GroovyLanguageServer.importOffset("import java.io.File\n" +
			"x = 1\n", "java.util.concurrent.atomic.AtomicInteger"));
	}

	// -- Helper methods --

	/** A session about plain Groovy, with a server of its own. */
	private static ScriptSession session(final Environment env) {
		final LanguageServerService servers = context.service(
			LanguageServerService.class);
		return env.live().map(live -> new ScriptSession(servers, TEST_LANGUAGE,
			live)).orElseGet(() -> new ScriptSession(servers, TEST_LANGUAGE));
	}

	/** The problems found in a script, e.g. {@code Error@0:4-6 message}. */
	private static List<String> problems(final String code) throws Exception {
		final CompletableFuture<List<Diagnostic>> heard =
			new CompletableFuture<>();
		final List<Diagnostic> found;
		try (ScriptSession session = session(new Environment())) {
			session.setDiagnosticsListener(heard::complete);
			session.update(code);
			found = heard.get(20, TimeUnit.SECONDS);
		}
		return found.stream().map(d -> d.getSeverity() + "@" + d.getRange().getStart()
				.getLine() + ":" + d.getRange().getStart().getCharacter() + "-" + d
					.getRange().getEnd().getCharacter() + " " + d.getMessage().getLeft())
			.collect(Collectors.toList());
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

	private static List<CompletionItem> items(final String code) {
		return items(session(new Environment()), code);
	}

	private static List<CompletionItem> items(final ScriptSession session,
		final String code)
	{
		try (ScriptSession s = session) {
			final List<CompletionItem> items = new ArrayList<>(s.completion(
				code, code.length()).get(30, TimeUnit.SECONDS).getItems());
			items.sort((a, b) -> a.getSortText().compareTo(b.getSortText()));
			return items;
		}
		catch (final Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	private static List<String> complete(final String code) {
		return labels(items(code));
	}

	private static List<String> labels(final List<CompletionItem> items) {
		return items.stream().map(CompletionItem::getLabel).collect(Collectors
			.toList());
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
		public Header header(final Map<String, Variable> variables) {
			// NB: As scripting-groovy's dialect does: full names need no imports.
			final List<String> declarations = new ArrayList<>();
			variables.forEach((name, v) -> declarations.add(GroovyLanguageServer
				.typeName(v.type()) + " " + name));
			return new Header(String.join("; ", declarations), Collections
				.emptySet());
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
			return new GroovyLanguageServer(environment);
		}
	}
}
