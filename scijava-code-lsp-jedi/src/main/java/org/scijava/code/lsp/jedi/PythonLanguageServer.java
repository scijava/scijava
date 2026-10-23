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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.scijava.code.lsp.jvm.Callable;
import org.scijava.code.lsp.Positions;

/**
 * A {@link LanguageServer} wrapping jedi-language-server, with a few nice
 * additions:
 * <ul>
 * <li>Asks about a word's first character only, and reuses the answer
 * (filtered) as the word is typed on, for a while.</li>
 * <li>Cancels a question still pending once another is asked about the same
 * document.</li>
 * <li>Once a word is begun, resolves the first completions, and makes those
 * with signatures (e.g. {@code def dumps(obj, *, skipkeys=False) -> str})
 * {@link Callable}s: snippets with their parameters and types. (Not in
 * imports: an import names things, rather than calling them.)</li>
 * </ul>
 * <p>
 * Everything else is passed on to the server as is.
 * </p>
 *
 * @author Gabriel Selzer
 */
class PythonLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	/** How long an answer may be reused, in milliseconds. */
	private static final long ANSWER_LIFETIME = 20_000;

	/** An import statement being typed. */
	private static final Pattern IMPORT = Pattern.compile(
		"^\\s*(from|import)\\s");

	/** The Python language server. */
	private final LanguageServer inner;

	private final int signatures;
	private final LongSupplier clock;

	/** The open documents' texts, by URI. */
	private final Map<String, String> texts = new ConcurrentHashMap<>();

	/** Answers, by question; least recently used first. */
	private final Map<String, Answer> answers = new LinkedHashMap<String, Answer>(
		16, 0.75f, true)
	{

		@Override
		protected boolean removeEldestEntry(final Map.Entry<String, Answer> e) {
			return size() > 32;
		}
	};

	/** The latest question about each document, by URI. */
	private final Map<String, Answer> pending = new ConcurrentHashMap<>();

	/**
	 * @param inner The Python language server.
	 * @param signatures How many completions to resolve for their signatures,
	 *          once a word is begun.
	 */
	PythonLanguageServer(final LanguageServer inner,
		final int signatures)
	{
		this(inner, signatures, System::currentTimeMillis);
	}

	PythonLanguageServer(final LanguageServer inner, final int signatures,
		final LongSupplier clock)
	{
		this.inner = inner;
		this.signatures = signatures;
		this.clock = clock;
	}

	// -- LanguageClientAware --

	@Override
	public void connect(final LanguageClient client) {
		if (inner instanceof LanguageClientAware) {
			((LanguageClientAware) inner).connect(client);
		}
	}

	// -- LanguageServer --

	@Override
	public CompletableFuture<InitializeResult> initialize(
		final InitializeParams params)
	{
		return inner.initialize(params);
	}

	@Override
	public void initialized(final InitializedParams params) {
		inner.initialized(params);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return inner.shutdown();
	}

	@Override
	public void exit() {
		inner.exit();
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return this;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		return this;
	}

	// -- TextDocumentService --

	@Override
	public void didOpen(final DidOpenTextDocumentParams params) {
		texts.put(params.getTextDocument().getUri(), params.getTextDocument()
			.getText());
		inner.getTextDocumentService().didOpen(params);
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		if (!params.getContentChanges().isEmpty()) {
			texts.put(params.getTextDocument().getUri(), params.getContentChanges()
				.get(params.getContentChanges().size() - 1).getText());
		}
		inner.getTextDocumentService().didChange(params);
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		texts.remove(params.getTextDocument().getUri());
		pending.remove(params.getTextDocument().getUri());
		inner.getTextDocumentService().didClose(params);
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		final String uri = params.getTextDocument().getUri();
		final String text = texts.get(uri);
		if (text == null) return inner.getTextDocumentService().completion(
			params);
		final int caret = Math.min(Positions.offset(text, params.getPosition()),
			text.length());
		int start = caret;
		while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start -
			1))) start--;
		final String word = text.substring(start, caret);
		// Ask about the first character typed, if any: later keystrokes of the
		// same word then reuse the answer (filtered).
		final int ask = start + Math.min(1, word.length());
		final String line = text.substring(text.lastIndexOf('\n', start - 1) + 1,
			start);
		final boolean importing = IMPORT.matcher(line).find();
		final String key = uri + "\n" + text.substring(0, ask);
		final long now = clock.getAsLong();
		Answer answer;
		synchronized (answers) {
			answer = answers.get(key);
			// NB: Not an empty answer either: the server may know more by now (e.g.
			// once the script's packages are, or its environment is built).
			if (answer != null && (now - answer.time > ANSWER_LIFETIME ||
				answer.future.isCompletedExceptionally() || answer.isEmpty()))
			{
				answer = null;
			}
		}
		if (answer == null) {
			final CompletionParams p = new CompletionParams(params.getTextDocument(),
				Positions.position(text, ask), params.getContext());
			final CompletableFuture<Either<List<CompletionItem>, CompletionList>> raw =
				inner.getTextDocumentService().completion(p);
			final int n = word.isEmpty() || importing ? 0 : signatures;
			answer = new Answer(raw, raw.thenCompose(list -> resolve(list, n)), now);
			synchronized (answers) {
				answers.put(key, answer);
			}
			final Answer previous = pending.put(uri, answer);
			if (previous != null && previous != answer && !previous.future
				.isDone())
			{
				// NB: Cancelling the request tells the server ($/cancelRequest).
				previous.future.cancel(true);
				previous.raw.cancel(true);
			}
		}
		return answer.future.thenApply(list -> Either.forRight(filter(list,
			word)));
	}

	@Override
	public void didSave(final DidSaveTextDocumentParams params) {
		inner.getTextDocumentService().didSave(params);
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item)
	{
		return inner.getTextDocumentService().resolveCompletionItem(item);
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params)
	{
		return inner.getTextDocumentService().signatureHelp(params);
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		return inner.getTextDocumentService().hover(params);
	}

	// -- WorkspaceService --

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams p) {
		inner.getWorkspaceService().didChangeConfiguration(p);
	}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {
		inner.getWorkspaceService().didChangeWatchedFiles(p);
	}

	// -- Helper methods --

	/** Resolves the first completions, making those with signatures callables. */
	private CompletableFuture<CompletionList> resolve(
		final Either<List<CompletionItem>, CompletionList> answer, final int n)
	{
		final List<CompletionItem> items = new ArrayList<>(answer == null
			? new ArrayList<>() : answer.isRight() ? answer.getRight().getItems()
				: answer.getLeft());
		items.sort(Comparator.comparing(i -> i.getSortText() != null ? i
			.getSortText() : i.getLabel()));
		final int count = Math.min(n, items.size());
		final List<CompletableFuture<CompletionItem>> resolved = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			// NB: Unresolved, should resolving fail.
			resolved.add(inner.getTextDocumentService().resolveCompletionItem(items
				.get(i)).handle((r, error) -> r));
		}
		return CompletableFuture.allOf(resolved.toArray(new CompletableFuture[0]))
			.thenApply(x -> {
				for (int i = 0; i < count; i++) {
					final CompletionItem r = resolved.get(i).join();
					final CompletionItem callable = r == null ? null : callable(items
						.get(i), r);
					if (callable != null) items.set(i, callable);
				}
				return new CompletionList(false, items);
			});
	}

	/**
	 * A completion as a callable, from its resolved signature (its detail);
	 * or null if it has none.
	 */
	private static CompletionItem callable(final CompletionItem item,
		final CompletionItem resolved)
	{
		final CompletionItemKind kind = item.getKind();
		if (kind != CompletionItemKind.Function && kind != CompletionItemKind.Method &&
			kind != CompletionItemKind.Class && kind != CompletionItemKind.Constructor)
		{
			return null;
		}
		final String name = item.getInsertText() != null ? item.getInsertText()
			: item.getLabel();
		if (resolved.getDetail() == null) return null;
		final Callable parsed = parse(name, resolved.getDetail());
		if (parsed == null) return null;
		final CompletionItem out = new Callable(name, parsed.params(), parsed
			.returnType()).kind(CompletionItemKind.Method).detail(resolved
				.getDetail()).toItem();
		out.setSortText(item.getSortText());
		out.setDocumentation(resolved.getDocumentation());
		out.setData(item.getData());
		return out;
	}

	/** The completions of the word typed so far. */
	private static CompletionList filter(final CompletionList list,
		final String word)
	{
		final String w = word.toLowerCase(Locale.ROOT);
		final List<CompletionItem> out = new ArrayList<>();
		for (final CompletionItem item : list.getItems()) {
			final String name = item.getFilterText() != null ? item.getFilterText()
				: item.getLabel();
			if (name.startsWith("_") && !word.startsWith("_")) continue;
			if (!name.toLowerCase(Locale.ROOT).startsWith(w) || name.equals(word)) {
				continue;
			}
			out.add(item);
		}
		return new CompletionList(false, out);
	}

	private static final class Answer {

		private final CompletableFuture<?> raw;
		private final CompletableFuture<CompletionList> future;
		private final long time;

		private Answer(final CompletableFuture<?> raw,
			final CompletableFuture<CompletionList> future, final long time)
		{
			this.raw = raw;
			this.future = future;
			this.time = time;
		}

		/** Whether the server answered, with no completions. */
		private boolean isEmpty() {
			final CompletionList list = future.getNow(null);
			return list != null && list.getItems().isEmpty();
		}
	}

	/**
	 * Parses a Python signature (as Jedi writes them): the parameters, with
	 * their annotations as types; and the return annotation. Returns null if it
	 * is not a signature.
	 */
	static Callable parse(final String name,
		final String signature)
	{
		final int open = signature.indexOf('(');
		if (open < 0) return null;
		final int close = closing(signature, open);
		if (close < 0) return null;
		final List<Callable.Param> params = new ArrayList<>();
		for (final String part : split(signature.substring(open + 1, close))) {
			final String p = part.trim();
			// NB: Bare * and / only mark keyword-only and positional-only ones.
			if (p.isEmpty() || p.equals("*") || p.equals("/")) continue;
			final int colon = topLevel(p, ':'), eq = topLevel(p, '=');
			String paramName = p, type = null;
			if (colon >= 0 && (eq < 0 || colon < eq)) {
				paramName = p.substring(0, colon);
				type = p.substring(colon + 1, eq < 0 ? p.length() : eq).trim();
			}
			else if (eq >= 0) paramName = p.substring(0, eq);
			paramName = paramName.trim().replaceFirst("^\\*{1,2}", "");
			params.add(new Callable.Param(paramName, type == null || type
				.isEmpty() ? null : type));
		}
		final String rest = signature.substring(close + 1).trim();
		final String returns = rest.startsWith("->") ? rest.substring(2).trim()
			: null;
		return new Callable(name, params, returns == null || returns.isEmpty()
			? null : returns);
	}

	/** The index of the bracket closing the one at {@code open}, or -1. */
	private static int closing(final String s, final int open) {
		int depth = 0;
		char quote = 0;
		for (int i = open; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
			}
			else if (c == '\'' || c == '"') quote = c;
			else if (c == '(' || c == '[' || c == '{') depth++;
			else if (c == ')' || c == ']' || c == '}') {
				if (--depth == 0) return i;
			}
		}
		return -1;
	}

	/** Splits at the commas outside brackets and strings. */
	private static List<String> split(final String s) {
		final List<String> parts = new ArrayList<>();
		int start = 0;
		while (true) {
			final int comma = topLevel(s.substring(start), ',');
			if (comma < 0) break;
			parts.add(s.substring(start, start + comma));
			start += comma + 1;
		}
		parts.add(s.substring(start));
		return parts;
	}

	/** The index of the first {@code target} outside brackets and strings. */
	private static int topLevel(final String s, final char target) {
		int depth = 0;
		char quote = 0;
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
			}
			else if (c == '\'' || c == '"') quote = c;
			else if (c == '(' || c == '[' || c == '{') depth++;
			else if (c == ')' || c == ']' || c == '}') depth--;
			else if (c == target && depth == 0) return i;
		}
		return -1;
	}
}
