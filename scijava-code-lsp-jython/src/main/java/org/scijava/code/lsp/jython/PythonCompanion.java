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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.lsp4j.CompletionItem;
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
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerService;

/**
 * A Python language server (e.g. scijava-code-lsp-jedi's) beside Jython's
 * own analysis (see {@link JythonLanguageServer}):
 * for Python modules, and for Java classes, from Python stubs of the Java
 * packages a script imports (including those of its parameters' types,
 * declared in its text; see {@link JavaStubs}, written in the background).
 * <p>
 * The server analyzes scripts as CPython (Jython's standard library is Python
 * 2's, but mostly the same): so its completions follow Jython's own, it is
 * asked for signatures and hover only where Jython has none, and never for
 * problems.
 * </p>
 *
 * @author Gabriel Selzer
 */
final class PythonCompanion {

	/** The server. */
	private final LanguageServer server;
	private final JavaStubs stubs;

	private PythonCompanion(final LanguageServer python, final JavaStubs stubs) {
		this.stubs = stubs;
		// NB: No client: the server's problems are not asked for.
		server = python;
		server.initialize(new InitializeParams()).thenRun(() -> server
			.initialized(new InitializedParams()));
	}

	/**
	 * Gets a Python server beside Jython's, or null if none is offered.
	 *
	 * @param servers Offers the server.
	 * @param stubs The stubs of Java packages, for the server to search.
	 */
	static PythonCompanion launch(final LanguageServerService servers,
		final JavaStubs stubs)
	{
		if (servers == null || !servers.supports("python")) return null;
		return new PythonCompanion(servers.launch("python", environment(stubs)),
			stubs);
	}

	/** Where the server runs: its own Python, searching the Java stubs. */
	static Environment environment(final JavaStubs stubs) {
		return new Environment().withPaths(Collections.singletonList(stubs.dir()
			.getAbsolutePath()));
	}

	// -- Documents --

	void didOpen(final DidOpenTextDocumentParams params) {
		ensureStubs(params.getTextDocument().getText());
		documents().didOpen(params);
	}

	void didChange(final DidChangeTextDocumentParams params) {
		if (params.getContentChanges().isEmpty()) return;
		ensureStubs(params.getContentChanges().get(params.getContentChanges()
			.size() - 1).getText());
		documents().didChange(params);
	}

	void didClose(final DidCloseTextDocumentParams params) {
		documents().didClose(params);
	}

	CompletableFuture<Object> shutdown() {
		return server.shutdown();
	}

	// -- Requests: Jython's answers first --

	/**
	 * Jython's completions, then the server's that complete other names. Each
	 * keeps its own order.
	 */
	CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
		final CompletionParams params, final CompletionList own)
	{
		return orNull(documents().completion(params)).thenApply(theirs -> Either
			.forRight(merge(own, list(theirs))));
	}

	/** Resolves a completion: the server's with the server, others with own. */
	CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item,
		final Function<CompletionItem, CompletableFuture<CompletionItem>> own)
	{
		if (!(item.getData() instanceof FromPython)) return own.apply(item);
		final FromPython from = (FromPython) item.getData();
		final String sortText = item.getSortText();
		item.setData(from.data);
		item.setSortText(from.sortText);
		return orNull(documents().resolveCompletionItem(item)).thenApply(
			resolved -> {
				final CompletionItem out = resolved != null ? resolved : item;
				out.setData(from);
				out.setSortText(sortText);
				return out;
			});
	}

	/** Jython's signatures, if any; otherwise the server's. */
	CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params, final SignatureHelp own)
	{
		if (own != null && own.getSignatures() != null && !own.getSignatures()
			.isEmpty()) return CompletableFuture.completedFuture(own);
		return orNull(documents().signatureHelp(params));
	}

	/** Jython's hover, if any; otherwise the server's. */
	CompletableFuture<Hover> hover(final HoverParams params, final Hover own) {
		if (own != null && own.getContents() != null) {
			return CompletableFuture.completedFuture(own);
		}
		return orNull(documents().hover(params));
	}

	// -- Helper methods --

	private TextDocumentService documents() {
		return server.getTextDocumentService();
	}

	/** Writes the stubs of the packages the script imports, in the background. */
	private void ensureStubs(final String text) {
		stubs.ensure(JavaStubs.imports(text));
	}

	/**
	 * Jython's completions, then the server's whose names Jython has not
	 * completed (e.g. Python modules), each in its own order.
	 */
	static CompletionList merge(final CompletionList own,
		final CompletionList theirs)
	{
		if (theirs == null || theirs.getItems() == null || theirs.getItems()
			.isEmpty()) return own;
		final List<CompletionItem> out = new ArrayList<>();
		final Set<String> names = new HashSet<>();
		int i = 0;
		for (final CompletionItem item : sorted(own)) {
			names.add(name(item));
			item.setSortText(String.format("0%06d", i++));
			out.add(item);
		}
		for (final CompletionItem item : sorted(theirs)) {
			if (names.contains(name(item))) continue;
			item.setData(new FromPython(item.getData(), item.getSortText()));
			item.setSortText(String.format("1%06d", i++));
			out.add(item);
		}
		return new CompletionList(own.isIncomplete() || theirs.isIncomplete(),
			out);
	}

	private static List<CompletionItem> sorted(final CompletionList list) {
		final List<CompletionItem> items = new ArrayList<>();
		if (list != null && list.getItems() != null) items.addAll(list.getItems());
		items.sort(Comparator.comparing(c -> c.getSortText() != null ? c
			.getSortText() : c.getLabel()));
		return items;
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

	/** The answer; or null, if the server failed (e.g. it stopped). */
	private static <T> CompletableFuture<T> orNull(
		final CompletableFuture<T> answer)
	{
		return answer == null ? CompletableFuture.completedFuture(null) : answer
			.exceptionally(error -> null);
	}

	/** Marks a completion as the server's, keeping what it came with. */
	private static final class FromPython {

		private final Object data;
		private final String sortText;

		private FromPython(final Object data, final String sortText) {
			this.data = data;
			this.sortText = sortText;
		}
	}
}
