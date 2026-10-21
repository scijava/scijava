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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Bindings;
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
import org.eclipse.lsp4j.InsertReplaceEdit;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.python.core.PyJavaType;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.Positions;
import org.scijava.code.lsp.jvm.AbstractLanguageServer;
import org.scijava.code.lsp.jvm.ClassIndex;

/**
 * A language server for Jython: a Python language server (e.g.
 * scijava-code-lsp-jedi's), which knows Java classes from Python stubs of the
 * Java packages a script imports (see {@link JavaStubs}, written in the
 * background).
 * <p>
 * The Python server analyzes scripts as CPython (Jython's standard library is
 * Python 2's, but mostly the same), so it is never asked for problems. Beside
 * its completions come those of Java classes not imported yet, which import
 * them as they are accepted (see {@link ClassIndex}). In a live interpreter,
 * its variables are declared to the server, as a module the script's text
 * imports first.
 * </p>
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

	/** Numbers the modules declaring live interpreters' variables. */
	private static final AtomicInteger LIVE = new AtomicInteger();

	private static final Pattern //
	fromImport = Pattern.compile(
		"^((from|import)[ \\t]+)([a-zA-Z_][a-zA-Z0-9._]*)$"), //
		fastImport = Pattern.compile(
			"^(from[ \\t]+)([a-zA-Z_][a-zA-Z0-9._]*)[ \\t]+$"), //
		importStatement = Pattern.compile(
			"^((from[ \\t]+([a-zA-Z0-9._]+)[ \\t]+|[ \\t]*)import[ \\t]+)([a-zA-Z0-9_., \\t]*)$"), //
		simpleClassName = Pattern.compile("^(.*[ \\t]+|)([A-Z_][a-zA-Z0-9_]+)$");

	/** Names Python reserves, which cannot be declared. */
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		"False", "None", "True", "and", "as", "assert", "async", "await", "break",
		"class", "continue", "def", "del", "elif", "else", "except", "exec",
		"finally", "for", "from", "global", "if", "import", "in", "is", "lambda",
		"nonlocal", "not", "or", "pass", "print", "raise", "return", "try",
		"while", "with", "yield"));

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

	/**
	 * The module declaring the live interpreter's variables, if any (see
	 * {@link Environment#live()}).
	 */
	private final File live;

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
		live = environment.live().isPresent() ? new File(stubs.dir(),
			"_scijava_live_" + LIVE.incrementAndGet() + ".pyi") : null;
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
		final CompletionParams theirs = new CompletionParams(params
			.getTextDocument(), toServer(params.getPosition()), params.getContext());
		final Set<String> dunders = dunders(params);
		// TODO: In a call's arguments, with nothing typed, offer the variables of
		// the parameter's type first, as Jython's own analysis did.
		return orNull(documents().completion(theirs)).thenApply(answer -> {
			final CompletionList merged = merge(back(list(answer)), own);
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
			resolved -> resolved == null ? item : back(resolved));
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params)
	{
		// TODO: All of a Java method's overloads, rated against the arguments
		// typed, as Jython's own analysis did: jedi-language-server offers only
		// the last of a stub's typing.overloads.
		return orNull(documents().signatureHelp(new SignatureHelpParams(params
			.getTextDocument(), toServer(params.getPosition()), params
				.getContext())));
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		return orNull(documents().hover(new HoverParams(params.getTextDocument(),
			toServer(params.getPosition())))).thenApply(h -> {
				if (h != null && h.getRange() != null) h.setRange(back(h.getRange()));
				return h;
			});
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		if (live != null) live.delete();
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
	 * imports (in the background), and the declarations of the live
	 * interpreter's variables, which it then imports first.
	 */
	private String prepare(final String text) {
		final Set<String> packages = new TreeSet<>(JavaStubs.imports(text));
		if (live == null) {
			stubs.ensure(packages);
			return text;
		}
		final String declarations = declarations(packages);
		stubs.ensure(packages);
		try {
			final byte[] bytes = declarations.getBytes(StandardCharsets.UTF_8);
			// NB: Unchanged, so that the server need not reload it.
			if (!live.isFile() || !Arrays.equals(Files.readAllBytes(live.toPath()),
				bytes))
			{
				live.getParentFile().mkdirs();
				Files.write(live.toPath(), bytes);
			}
		}
		catch (final IOException exc) {
			// NB: Then, the variables are not known.
		}
		return "from " + module() + " import *\n" + text;
	}

	/** The name of the module declaring the live interpreter's variables. */
	private String module() {
		final String name = live.getName();
		return name.substring(0, name.length() - ".pyi".length());
	}

	/**
	 * Declares the live interpreter's variables, as a stub: Java ones by their
	 * classes' Python types (noting their packages), Python ones by their
	 * builtin types.
	 */
	private String declarations(final Set<String> packages) {
		final Map<String, String> declared = new TreeMap<>();
		final Set<String> imports = new TreeSet<>();
		final Bindings bindings = environment().live().map(l -> l.getBindings(
			ScriptContext.ENGINE_SCOPE)).orElse(null);
		if (bindings != null) {
			for (final Map.Entry<String, Object> entry : bindings.entrySet()) {
				final String name = entry.getKey();
				if (!name.matches("[A-Za-z_][A-Za-z0-9_]*") || name.startsWith("__") ||
					KEYWORDS.contains(name)) continue;
				declared.put(name, declaration(name, entry.getValue(), imports));
			}
		}
		packages.addAll(imports);
		final StringBuilder sb = new StringBuilder(
			"# The variables of a running interpreter. Generated; do not edit.\n");
		sb.append("import typing\n");
		for (final String p : imports) sb.append("import ").append(p).append('\n');
		sb.append("__all__ = [").append(declared.keySet().stream().map(n -> "'" +
			n + "'").collect(Collectors.joining(", "))).append("]\n");
		declared.values().forEach(d -> sb.append(d).append('\n'));
		return sb.toString();
	}

	/** Declares a variable of the live interpreter, as a stub does. */
	private String declaration(final String name, final Object value,
		final Set<String> imports)
	{
		if (value instanceof PyJavaType && ((PyJavaType) value)
			.getProxyType() != null)
		{
			return name + " = " + stubs.type(((PyJavaType) value).getProxyType(),
				"", imports);
		}
		if (value instanceof Class) {
			return name + " = " + stubs.type((Class<?>) value, "", imports);
		}
		if (value instanceof PyModule) {
			final PyObject module = ((PyModule) value).__findattr__("__name__");
			if (module != null && module.toString().matches("[\\w.]+")) {
				return "import " + module + " as " + name;
			}
		}
		if (value instanceof PyObject) {
			return name + ": " + pythonType(((PyObject) value).getType().getName());
		}
		if (value == null) return name + ": typing.Any";
		return name + ": " + stubs.type(value.getClass(), "", imports);
	}

	/** The Python 3 type of a Python 2 builtin one; or {@code typing.Any}. */
	private static String pythonType(final String type) {
		switch (type) {
			case "int":
			case "long":
				return "int";
			case "unicode":
				return "str";
			case "bool":
			case "float":
			case "str":
			case "list":
			case "dict":
			case "tuple":
			case "set":
				return type;
			default:
				return "typing.Any";
		}
	}

	/** Positions in the script, as the server sees it. */
	private Position toServer(final Position p) {
		return new Position(p.getLine() + headerLines(), p.getCharacter());
	}

	/** Ranges as the server sees the script, in the script. */
	private Range back(final Range r) {
		final int n = headerLines();
		return new Range(new Position(r.getStart().getLine() - n, r.getStart()
			.getCharacter()), new Position(r.getEnd().getLine() - n, r.getEnd()
				.getCharacter()));
	}

	private int headerLines() {
		return live == null ? 0 : 1;
	}

	/** Completions as the server sees the script, in the script. */
	private CompletionList back(final CompletionList list) {
		if (list == null || headerLines() == 0) return list;
		for (final CompletionItem item : list.getItems()) back(item);
		return list;
	}

	private CompletionItem back(final CompletionItem item) {
		if (headerLines() == 0) return item;
		final Either<TextEdit, InsertReplaceEdit> edit = item.getTextEdit();
		if (edit != null && edit.isLeft()) {
			edit.getLeft().setRange(back(edit.getLeft().getRange()));
		}
		else if (edit != null) {
			edit.getRight().setInsert(back(edit.getRight().getInsert()));
			edit.getRight().setReplace(back(edit.getRight().getReplace()));
		}
		if (item.getAdditionalTextEdits() != null) {
			item.getAdditionalTextEdits().forEach(e -> e.setRange(back(e
				.getRange())));
		}
		return item;
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
