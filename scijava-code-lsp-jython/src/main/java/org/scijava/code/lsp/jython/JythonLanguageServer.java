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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.script.ScriptContext;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.Positions;
import org.scijava.code.lsp.jvm.AbstractLanguageServer;
import org.scijava.code.lsp.jvm.ClassIndex;

/**
 * A language server for Jython. Leans upon a Python server, with
 * added Python stubs for Java types.
 * <p>
 * FIXME Diagnostics: the Python server reads scripts as Python 3, and so would
 * report Jython's Python 2 (e.g. {@code print "x"}) as problems; it is never
 * asked for them.
 * FIXME Python 2: completions are of Python 3's builtins and standard library
 * (mostly the same as Jython's); only names like {@code __name__} are kept to
 * Jython's.
 * FIXME Overloads: signature help shows only the last of a Java method's
 * overloads, not all of them rated against the arguments typed.
 * FIXME Arguments: in a call, the variables of the parameter's type are not
 * offered first.
 * FIXME Parameter names: Java methods' are named after their types (e.g.
 * {@code long1}), as reflection does not know them.
 *
 * @author Albert Cardona
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
class JythonLanguageServer extends AbstractLanguageServer {

	static {
		// Warm the class index in the background, as completion needs it.
		new Thread(ClassIndex::ensureCache, "Jython-ClassIndex-warmup").start();
	}

	/** Marks the completions made here, rather than by the Python server. */
	private static final String OWN = "jython-class-name";

	private static final Pattern //
	fromImport = Pattern.compile(
		"^((from|import)[ \\t]+)([a-zA-Z_][a-zA-Z0-9._]*)$"), //
		fastImport = Pattern.compile(
			"^(from[ \\t]+)([a-zA-Z_][a-zA-Z0-9._]*)[ \\t]+$"), //
		importStatement = Pattern.compile(
			"^((from[ \\t]+([a-zA-Z0-9._]+)[ \\t]+|[ \\t]*)import[ \\t]+)([a-zA-Z0-9_., \\t]*)$"), //
		simpleClassName = Pattern.compile("^(.*[ \\t]+|)([A-Z_][a-zA-Z0-9_]+)$");

	/**
	 * The names like {@code __name__} that Jython 2.7 has, unqualified: its
	 * module globals, builtins and modules. (The Python server knows Python 3's,
	 * e.g. {@code __build_class__}.)
	 */
	private static final Set<String> DUNDERS = new HashSet<>(Arrays.asList(
		"__builtins__", "__doc__", "__file__", "__name__", "__package__",
		"__debug__", "__import__", "__future__", "__main__", "__builtin__"));

	/** A name like {@code __name__}. */
	private static final Pattern DUNDER = Pattern.compile("__\\w+__");

	/** The Python server. */
	private final LanguageServer python;
	private final JavaStubs stubs;

	/** The scripts' texts, by URI: to know the names they mention. */
	private final Map<String, String> texts = new ConcurrentHashMap<>();

	/**
	 * @param environment Where the scripts run (e.g. a live interpreter).
	 * @param python The Python server, e.g. of {@link #environment(JavaStubs)}.
	 * @param stubs The stubs of Java packages, which the server searches.
	 */
	JythonLanguageServer(final Environment environment,
		final LanguageServer python, final JavaStubs stubs)
	{
		super(environment);
		this.python = python;
		this.stubs = stubs;
		python.initialize(new InitializeParams()).thenRun(() -> python
			.initialized(new InitializedParams()));
	}

	/** Where the Python server runs: its own Python, searching the stubs. */
	static Environment environment(final JavaStubs stubs) {
		return new Environment().withPaths(Collections.singletonList(stubs.dir()
			.getAbsolutePath()));
	}

	// -- LanguageServer, with the Python server --

	@Override
	public void didOpen(final DidOpenTextDocumentParams params) {
		super.didOpen(params);
		final TextDocumentItem item = params.getTextDocument();
		texts.put(item.getUri(), item.getText());
		final String text = prepare(item.getText());
		documents().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(
			item.getUri(), "python", item.getVersion(), text)));
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		super.didChange(params);
		if (params.getContentChanges().isEmpty()) return;
		final String original = params.getContentChanges().get(params
			.getContentChanges().size() - 1).getText();
		texts.put(params.getTextDocument().getUri(), original);
		final String text = prepare(original);
		documents().didChange(new DidChangeTextDocumentParams(params
			.getTextDocument(), Collections.singletonList(
				new TextDocumentContentChangeEvent(text))));
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		super.didClose(params);
		texts.remove(params.getTextDocument().getUri());
		documents().didClose(params);
	}

	/**
	 * The server's completions, then those of Java classes not imported yet,
	 * whose names the server has not completed; without names like
	 * {@code __name__} that Jython does not have.
	 */
	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		final CompletionList own = super.completion(params).join().getRight();
		final Set<String> dunders = dunders(params);
		// TODO: In a call's arguments, with nothing typed, offer the variables of
		// the parameter's type first, as Jython's own analysis did.
		return orNull(documents().completion(params)).thenApply(answer -> {
			final CompletionList merged = merge(list(answer), own);
			if (dunders != null) merged.getItems().removeIf(item -> DUNDER.matcher(
				item.getLabel()).matches() && !dunders.contains(item.getLabel()));
			return Either.forRight(merged);
		});
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item)
	{
		if (OWN.equals(item.getData())) {
			return CompletableFuture.completedFuture(item);
		}
		return orNull(documents().resolveCompletionItem(item)).thenApply(
			resolved -> resolved == null ? item : resolved);
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params)
	{
		// TODO: All of a Java method's overloads, rated against the arguments
		// typed, as Jython's own analysis did: jedi-language-server offers only
		// the last of a stub's typing.overloads.
		return orNull(documents().signatureHelp(params));
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		return orNull(documents().hover(params));
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return python.shutdown().thenCompose(done -> super.shutdown());
	}

	// -- AbstractLanguageServer --

	/** Java classes, and their packages, as the caret's line is typed. */
	@Override
	protected CompletionList complete(final Document doc, final int caret) {
		final String line = doc.lineTo(caret);
		if (line.trim().startsWith("#")) {
			return list(doc, caret, caret, new ArrayList<>());
		}
		final String typed = alreadyEnteredText(line);
		final List<CompletionItem> items = classNames(typed, doc.text());
		for (final CompletionItem item : items) item.setData(OWN);
		return list(doc, caret - typed.length(), caret, items);
	}

	// -- Helper methods --

	/**
	 * The names like {@code __name__} to complete unqualified, at the position
	 * asked about: Jython's, and those the script or the live interpreter has;
	 * or null if completing a member (e.g. after {@code x.}), whose names the
	 * server knows.
	 */
	private Set<String> dunders(final CompletionParams params) {
		final String text = texts.getOrDefault(params.getTextDocument().getUri(),
			"");
		int start = Math.min(Positions.offset(text, params.getPosition()), text
			.length());
		while (start > 0 && (Character.isLetterOrDigit(text.charAt(start - 1)) ||
			text.charAt(start - 1) == '_')) start--;
		if (start > 0 && text.charAt(start - 1) == '.') return null;
		final Set<String> names = new HashSet<>(DUNDERS);
		final Matcher m = DUNDER.matcher(text);
		while (m.find()) names.add(m.group());
		environment().live().map(l -> l.getBindings(ScriptContext.ENGINE_SCOPE))
			.ifPresent(b -> names.addAll(b.keySet()));
		return names;
	}

	private TextDocumentService documents() {
		return python.getTextDocumentService();
	}

	/**
	 * Gets a script ready for the server: writes the stubs of the packages it
	 * imports (in the background). (A live interpreter's variables are
	 * declared, and their packages imported, in the script's header; see
	 * {@code ScriptDialect#header}.)
	 */
	private String prepare(final String text) {
		stubs.ensure(new TreeSet<>(JavaStubs.imports(text)));
		return text;
	}

	/**
	 * The server's completions first; then those made here, whose names it has
	 * not completed. Each keeps its own order.
	 */
	static CompletionList merge(final CompletionList theirs,
		final CompletionList own)
	{
		final List<CompletionItem> out = new ArrayList<>();
		final Set<String> names = new HashSet<>();
		if (theirs != null && theirs.getItems() != null) {
			for (final CompletionItem item : theirs.getItems()) {
				names.add(name(item));
				out.add(item);
			}
		}
		int i = 0;
		for (final CompletionItem item : own.getItems()) {
			if (names.contains(name(item))) continue;
			// NB: After the server's.
			item.setSortText(String.format("~%06d", i++));
			out.add(item);
		}
		return new CompletionList(theirs != null && theirs.isIncomplete(), out);
	}

	/** What a completion completes: its label, without any parameters. */
	private static String name(final CompletionItem item) {
		final String label = item.getLabel();
		final int paren = label.indexOf('(');
		return (paren < 0 ? label : label.substring(0, paren)).trim();
	}

	private static CompletionList list(
		final Either<List<CompletionItem>, CompletionList> answer)
	{
		if (answer == null) return null;
		return answer.isRight() ? answer.getRight() : new CompletionList(answer
			.getLeft());
	}

	/** The answer; or null, if the server failed (e.g. it is not ready). */
	private static <T> CompletableFuture<T> orNull(
		final CompletableFuture<T> answer)
	{
		return answer == null ? CompletableFuture.completedFuture(null) : answer
			.exceptionally(error -> null);
	}

	/**
	 * Java classes and packages, as an import statement or a class's simple
	 * name is typed: classes not imported yet import themselves as they are
	 * accepted.
	 */
	private static List<CompletionItem> classNames(final String typed,
		final String code)
	{
		final List<CompletionItem> items = new ArrayList<>();
		if (!ClassIndex.isCacheReady()) return items; // NB: Don't block.

		final Matcher m1 = fromImport.matcher(typed);
		if (m1.find()) {
			ClassIndex.findClassNamesContaining(m1.group(3)).map(
				JythonImports::importStatement).forEach(s -> items.add(item(s,
					CompletionItemKind.Module, null)));
			return items;
		}
		final Matcher m1f = fastImport.matcher(typed);
		if (m1f.find()) {
			ClassIndex.findClassNamesForPackage(m1f.group(2)).map(
				JythonImports::importStatement).forEach(s -> items.add(item(s,
					CompletionItemKind.Module, null)));
			return items;
		}
		final Matcher m2 = importStatement.matcher(typed);
		if (m2.find()) {
			final String packageName = m2.group(3);
			String className = m2.group(4);
			final String[] bycomma = className.split(",");
			String precomma = "";
			if (bycomma.length > 1) {
				className = bycomma[bycomma.length - 1].trim();
				for (int i = 0; i < bycomma.length - 1; ++i)
					precomma += bycomma[0] + ", ";
			}
			final Stream<String> stream;
			if (className.length() > 0) stream = ClassIndex.findClassNamesStartingWith(
				null == packageName ? className : packageName + "." + className);
			else stream = ClassIndex.findClassNamesForPackage(packageName);
			final String pre = m2.group(1) + precomma;
			stream.map(s -> s.substring(Math.max(0, s.lastIndexOf('.') + 1))).forEach(
				s -> items.add(item(pre + s, CompletionItemKind.Module, null)));
			return items;
		}
		final Matcher m3 = simpleClassName.matcher(typed);
		if (m3.find()) {
			final String pre = m3.group(1);
			final List<String> classNames = ClassIndex
				.findSimpleClassNamesStartingWith(m3.group(2));
			// Closest matches first: shortest simple name, then alphabetical.
			classNames.sort(Comparator.comparingInt((String cn) -> cn.length() - cn
				.lastIndexOf('.')).thenComparing(Comparator.naturalOrder()));
			for (final String className : classNames) {
				final String simpleName = className.substring(className.lastIndexOf(
					'.') + 1);
				final String importStmt = JythonImports.importStatement(className);
				// Side effect on accept: insert the import near the top of the file.
				final TextEdit edit = JythonImports.autoImportEdit(code, className,
					importStmt);
				final CompletionItem c = item(pre + simpleName, CompletionItemKind.Class,
					importStmt);
				if (edit != null) c.setAdditionalTextEdits(Collections.singletonList(
					edit));
				items.add(c);
			}
		}
		return items;
	}

	/** A completion of the given text. */
	private static CompletionItem item(final String text,
		final CompletionItemKind kind, final String detail)
	{
		final CompletionItem item = new CompletionItem(text);
		item.setKind(kind);
		item.setInsertText(text);
		item.setDetail(detail);
		return item;
	}

	/**
	 * The portion of the current line to replace: a maximal suffix of letters,
	 * digits, {@code '.'}, {@code '_'} and spaces (spaces let "from x import Y"
	 * complete as a unit), excluding leading spaces.
	 */
	static String alreadyEnteredText(final String lastLine) {
		int start = lastLine.length();
		while (start > 0) {
			final char c = lastLine.charAt(start - 1);
			if (Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == ' ') //
				start--;
			else break;
		}
		// Leading whitespace is not part of what the user is completing.
		while (start < lastLine.length() && lastLine.charAt(start) == ' ') start++;
		return lastLine.substring(start);
	}
}
