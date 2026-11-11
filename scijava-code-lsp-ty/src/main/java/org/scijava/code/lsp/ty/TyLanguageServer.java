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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
import org.scijava.code.lsp.CompletionItems;
import org.scijava.code.lsp.Positions;

/**
 * A {@link LanguageServer} wrapping ty's language server, with a few nice
 * additions to its completions:
 * <ul>
 * <li>Only those starting with the word typed (ty's match loosely), and not
 * private ones (starting with {@code _}) unless the word does.</li>
 * <li>Those with signatures (e.g.
 * {@code def dumps(obj, *, skipkeys: bool = False) -> str}) as
 * {@link CompletionItems#methodSnippet method snippets}, with their parameters.
 * (Not in imports: an import names things, rather than calling them.)</li>
 * </ul>
 * <p>
 * Everything else is passed on to the server as is.
 * </p>
 *
 * @author Gabriel Selzer
 */
class TyLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	/** An import statement being typed. */
	private static final Pattern IMPORT = Pattern.compile(
		"^\\s*(from|import)\\s");

	/** The Python language server. */
	private final LanguageServer inner;

	/** The open documents' texts, by URI. */
	private final Map<String, String> texts = new ConcurrentHashMap<>();

	/** @param inner The Python language server. */
	TyLanguageServer(final LanguageServer inner) {
		this.inner = inner;
	}

	// -- TextDocumentService --

	// NB: The documents' texts are kept, as a completion request gives only a
	// position: completions are filtered by the word typed there.

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
		inner.getTextDocumentService().didClose(params);
	}

	/**
	 * Gets ty's completions, filtered and made callables where they can be.
	 * <p>
	 * ty matches the word typed loosely, ranks its own way, and offers private
	 * names too: for {@code x = le}, {@code False} and {@code else} come before
	 * {@code len}; for {@code json.du}, {@code __reduce__} comes too. And it
	 * makes no snippets of functions: their signatures come only as text (in
	 * each completion's detail). Editors may show completions as they come, so
	 * this is done here.
	 * </p>
	 */
	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		final CompletableFuture<Either<List<CompletionItem>, CompletionList>> raw =
			inner.getTextDocumentService().completion(params);
		final String text = texts.get(params.getTextDocument().getUri());
		if (text == null) return raw;
		// The word typed so far, up to the caret.
		final int caret = Math.min(Positions.offset(text, params.getPosition()),
			text.length());
		int start = caret;
		while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start -
			1))) start--;
		final String word = text.substring(start, caret);
		// NB: No snippets in imports: an import names things, rather than calling
		// them.
		final String line = text.substring(text.lastIndexOf('\n', start - 1) + 1,
			start);
		final boolean importing = IMPORT.matcher(line).find();
		return raw.thenApply(answer -> {
			final List<CompletionItem> items = new ArrayList<>();
			for (final CompletionItem item : answer == null ? new ArrayList<
				CompletionItem>() : answer.isRight() ? answer.getRight().getItems()
					: answer.getLeft())
			{
				if (!matches(item, word)) continue;
				final CompletionItem callable = importing ? null : callable(item);
				items.add(callable != null ? callable : item);
			}
			return Either.forRight(new CompletionList(false, items));
		});
	}

	// -- Helper methods --

	/**
	 * Whether a completion is of the word typed so far: starts with it (ty's
	 * may merely contain its letters, in order), and is not private (starting
	 * with {@code _}) unless the word is.
	 */
	private static boolean matches(final CompletionItem item,
		final String word)
	{
		final String name = item.getFilterText() != null ? item.getFilterText()
			: item.getLabel();
		if (name.startsWith("_") && !word.startsWith("_")) return false;
		// NB: The word typed in full is not offered again.
		return name.toLowerCase(Locale.ROOT).startsWith(word.toLowerCase(
			Locale.ROOT)) && !name.equals(word);
	}

	/**
	 * The completion as a snippet with its parameters, from its signature (see
	 * {@link #parseCallable}); or null if it has none.
	 * <p>
	 * ty gives a function's signature only as text (e.g.
	 * {@code def dumps(obj: Any, *, indent: int | None = None) -> str}), so an
	 * editor would insert just its name. As a callable, it is a snippet with
	 * its parameters (see {@link CompletionItems#methodSnippet}). NB: The
	 * detail comes with the completion: ty does not resolve completions.
	 * </p>
	 */
	private static CompletionItem callable(final CompletionItem item) {
		final CompletionItemKind kind = item.getKind();
		if (kind != CompletionItemKind.Function && kind != CompletionItemKind.Method &&
			kind != CompletionItemKind.Class && kind != CompletionItemKind.Constructor)
		{
			return null;
		}
		if (item.getDetail() == null) return null;
		final String name = item.getInsertText() != null ? item.getInsertText()
			: item.getLabel();
		final CompletionItem out = parseCallable(name, item.getDetail());
		if (out == null) return null;
		out.setDetail(item.getDetail());
		out.setSortText(item.getSortText());
		out.setDocumentation(item.getDocumentation());
		out.setData(item.getData());
		return out;
	}

	/**
	 * Parses a signature, as ty writes it in a completion's detail, into a
	 * {@link CompletionItems#methodSnippet method snippet}: its parameters'
	 * names and its parameter list (as ty writes it). E.g.
	 * {@code def dumps(obj: Any, *, indent: int | None = None) -> str} becomes
	 * {@code dumps}, with parameters {@code obj} and {@code indent}, listed as
	 * {@code (obj: Any, indent: int | None = None)}.
	 * (Bare {@code *} and {@code /} are left out: they are not parameters, but
	 * mark keyword-only and positional-only ones.) Also reads methods
	 * ({@code bound method str.upper() -> str}) and overloads
	 * ({@code Overload[(...) -> A, (...) -> B]}, as the first). Returns null if
	 * it is not a signature (e.g. {@code <class 'Path'>}, or {@code str}).
	 *
	 * @param name The callable's name (the signature's may be qualified).
	 * @param signature The signature, as ty writes it.
	 */
	private static CompletionItem parseCallable(final String name,
		String signature)
	{
		if (signature.startsWith("Overload[")) signature = signature.substring(9);
		final int open = signature.indexOf('(');
		if (open < 0) return null;
		final int close = closing(signature, open);
		if (close < 0) return null;
		final List<String> names = new ArrayList<>();
		final List<String> params = new ArrayList<>();
		for (final String part : split(signature.substring(open + 1, close))) {
			final String p = part.trim();
			if (p.isEmpty() || p.equals("*") || p.equals("/")) continue;
			// NB: The name is up to its type or default, without * or **.
			final int colon = topLevel(p, ':'), eq = topLevel(p, '=');
			final int end = colon < 0 ? eq : eq < 0 ? colon : Math.min(colon, eq);
			names.add((end < 0 ? p : p.substring(0, end)).trim().replaceFirst(
				"^\\*{1,2}", ""));
			params.add(p);
		}
		return CompletionItems.methodSnippet(name, names, "(" + String.join(
			", ", params) + ")");
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

	// -- LanguageClientAware, passed on as is --

	@Override
	public void connect(final LanguageClient client) {
		if (inner instanceof LanguageClientAware) {
			((LanguageClientAware) inner).connect(client);
		}
	}

	// -- LanguageServer, passed on as is --

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

	// -- TextDocumentService, passed on as is --

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

	// -- WorkspaceService, passed on as is --

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams p) {
		inner.getWorkspaceService().didChangeConfiguration(p);
	}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {
		inner.getWorkspaceService().didChangeWatchedFiles(p);
	}
}
