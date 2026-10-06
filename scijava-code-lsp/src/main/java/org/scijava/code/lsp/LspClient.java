/*-
 * #%L
 * Language Server Protocol bridge for SciJava code intelligence.
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

package org.scijava.code.lsp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple;
import org.scijava.code.api.Completion;
import org.scijava.code.api.Hover;
import org.scijava.code.api.ScriptDocument;
import org.scijava.code.api.SignatureHelp;
import org.scijava.code.api.SignatureHelp.Fit;

/**
 * Asks language servers about {@link ScriptDocument scripts}, in the terms of
 * {@code org.scijava.code.api}: completions and signature help, at positions
 * of the original script.
 * <p>
 * Questions are answered asynchronously. An answer is reused while the same
 * question (the same text before the same position) is asked again, for a
 * while; and a question still pending is cancelled once a different one is
 * asked about the same document (e.g. as the user types on).
 * </p>
 * <p>
 * Converting the servers' answers can be refined per language by overriding
 * {@link #callable}, e.g. to parse a signature's parameters.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class LspClient {

	/** How long an answer may be reused, in milliseconds. */
	private static final long ANSWER_LIFETIME = 20_000;

	/** How long to wait for documentation, in milliseconds. */
	private static final long DESCRIBE_TIMEOUT = 1000;

	private final LspServers servers;
	private final LongSupplier clock;

	/** Answers, by question; least recently used first. */
	private final Map<String, Answer> answers = new LinkedHashMap<String, Answer>(
		16, 0.75f, true)
	{

		@Override
		protected boolean removeEldestEntry(final Map.Entry<String, Answer> e) {
			return size() > 32;
		}
	};

	/** The latest question about each document (by URI), and its request. */
	private final Map<String, Pending> pending = new HashMap<>();

	public LspClient(final LspServers servers) {
		this(servers, System::currentTimeMillis);
	}

	LspClient(final LspServers servers, final LongSupplier clock) {
		this.servers = servers;
		this.clock = clock;
	}

	/**
	 * Gets a URI for a script: its file's, if saved; otherwise an
	 * {@code untitled:} one.
	 *
	 * @param path The script's path, or null if unsaved.
	 * @param unsaved The name of an unsaved script, e.g. {@code script.py}.
	 */
	public static String uri(final String path, final String unsaved) {
		return path == null ? "untitled:/" + unsaved : new File(path).toURI()
			.toString();
	}

	/**
	 * Brings a script to the attention of its server (starting the server, if
	 * needed), and has the server analyze it, so that the first questions
	 * about it are answered quickly.
	 */
	public void prepare(final ScriptDocument doc, final String uri) {
		final Position end = position(doc.text(), doc.text().length());
		servers.server(doc.environment()).thenCompose(server -> server.request(
			uri, doc.text(), s -> s.completion(new CompletionParams(
				new TextDocumentIdentifier(uri), end))));
	}

	/** Forgets a script (e.g. once its editor is closed). */
	public void close(final String uri) {
		synchronized (pending) {
			pending.keySet().removeIf(k -> k.endsWith("\n" + uri));
		}
		servers.forEach(s -> s.close(uri));
	}

	/**
	 * Gets the completions at an offset of a script, in the server's order.
	 *
	 * @param doc The script.
	 * @param uri Its URI.
	 * @param offset The offset, in the original script.
	 * @param resolve How many of the first completions to resolve (e.g. to get
	 *          their signatures) before answering; the others are resolved
	 *          only for their documentation, when needed.
	 */
	public CompletableFuture<List<Completion>> complete(final ScriptDocument doc,
		final String uri, final int offset, final int resolve)
	{
		final int at = doc.map().toTransformed(offset);
		final String key = "complete\n" + resolve + "\n" + question(doc, uri, at);
		return ask(uri, key, () -> {
			final Position position = position(doc.text(), at);
			final List<CompletableFuture<?>> requests = new CopyOnWriteArrayList<>();
			final CompletableFuture<List<Completion>> answer = servers.server(doc
				.environment()).thenCompose(server -> {
					final CompletableFuture<Either<List<CompletionItem>, CompletionList>> r =
						server.request(uri, doc.text(), s -> s.completion(
							new CompletionParams(new TextDocumentIdentifier(uri), position)));
					requests.add(r);
					return r.thenCompose(items -> completions(server, items(items),
						resolve));
				});
			return new Pending(answer, requests);
		});
	}

	/**
	 * Gets the signatures of the call around an offset of a script, in the
	 * server's order, unrated ({@link Fit#UNKNOWN}).
	 *
	 * @param doc The script.
	 * @param uri Its URI.
	 * @param offset The offset, in the original script.
	 * @param callStart The offset of the call's opening parenthesis.
	 * @param active The argument the offset is in, should the server not
	 *          tell.
	 * @return The signatures, or {@link SignatureHelp#NONE}.
	 */
	public CompletableFuture<SignatureHelp> signatureHelp(
		final ScriptDocument doc, final String uri, final int offset,
		final int callStart, final int active)
	{
		final int at = doc.map().toTransformed(offset);
		final String key = "signatures\n" + callStart + "\n" + active + "\n" +
			question(doc, uri, at);
		return ask(uri, key, () -> {
			final Position position = position(doc.text(), at);
			final List<CompletableFuture<?>> requests = new CopyOnWriteArrayList<>();
			final CompletableFuture<SignatureHelp> answer = servers.server(doc
				.environment()).thenCompose(server -> {
					final CompletableFuture<org.eclipse.lsp4j.SignatureHelp> r = server
						.request(uri, doc.text(), s -> s.signatureHelp(
							new SignatureHelpParams(new TextDocumentIdentifier(uri),
								position)));
					requests.add(r);
					return r;
				}).thenApply(help -> signatureHelp(help, callStart, active));
			return new Pending(answer, requests);
		});
	}

	/**
	 * Gets what is known about the code at an offset of a script, e.g. the
	 * documentation of the function named there.
	 *
	 * @param doc The script.
	 * @param uri Its URI.
	 * @param offset The offset, in the original script.
	 * @return What is known, or {@link Hover#NONE}.
	 */
	public CompletableFuture<Hover> hover(final ScriptDocument doc,
		final String uri, final int offset)
	{
		final int at = doc.map().toTransformed(offset);
		final String key = "hover\n" + at + "\n" + doc.environment() + "\n" +
			uri + "\n" + doc.text();
		return ask(uri, key, () -> {
			final Position position = position(doc.text(), at);
			final List<CompletableFuture<?>> requests = new CopyOnWriteArrayList<>();
			final CompletableFuture<Hover> answer = servers.server(doc.environment())
				.thenCompose(server -> {
					final CompletableFuture<org.eclipse.lsp4j.Hover> r = server.request(
						uri, doc.text(), s -> s.hover(new HoverParams(
							new TextDocumentIdentifier(uri), position)));
					requests.add(r);
					return r;
				}).thenApply(h -> hover(h, doc, offset));
			return new Pending(answer, requests);
		});
	}

	/**
	 * Gets the problems in a script (e.g. syntax errors), once the server has
	 * found them; at offsets of the original script. Problems in the lines
	 * the script's transformation added are left out.
	 *
	 * @param doc The script.
	 * @param uri Its URI.
	 * @param timeout How long to wait for the server, in milliseconds; without
	 *          an answer by then, no problems.
	 */
	public CompletableFuture<List<org.scijava.code.api.Diagnostic>> diagnose(
		final ScriptDocument doc, final String uri, final long timeout)
	{
		return servers.server(doc.environment()).thenCompose(server -> server
			.diagnostics(uri, doc.text())).completeOnTimeout(Collections
				.emptyList(), timeout, TimeUnit.MILLISECONDS).thenApply(
					list -> diagnostics(list, doc));
	}

	// -- Conversions; override to refine --

	/**
	 * Builds a completion for a callable from its signature, as the server
	 * writes it (e.g. {@code def dumps(obj, *, skipkeys=False) -> str}), with
	 * its parameters and return type; or returns null if the signature is not
	 * understood. (Its kind becomes {@link Completion.Kind#METHOD}.) The default
	 * understands none.
	 *
	 * @param name The callable's name.
	 * @param signature Its signature.
	 */
	protected Completion.Builder callable(final String name,
		final String signature)
	{
		return null;
	}

	/** Converts documentation (e.g. a docstring) to HTML, or null if empty. */
	protected String html(final Either<String, MarkupContent> doc) {
		if (doc == null) return null;
		final String text = doc.isLeft() ? doc.getLeft() : doc.getRight()
			.getValue();
		if (text == null || text.trim().isEmpty()) return null;
		return "<pre>" + text.replace("&", "&amp;").replace("<", "&lt;").replace(
			">", "&gt;") + "</pre>";
	}

	/** Maps the server's severities to SciJava's. */
	protected org.scijava.code.api.Diagnostic.Severity severity(
		final DiagnosticSeverity severity)
	{
		if (severity == null) return org.scijava.code.api.Diagnostic.Severity.ERROR;
		switch (severity) {
			case Warning:
				return org.scijava.code.api.Diagnostic.Severity.WARNING;
			case Information:
				return org.scijava.code.api.Diagnostic.Severity.INFO;
			case Hint:
				return org.scijava.code.api.Diagnostic.Severity.HINT;
			default:
				return org.scijava.code.api.Diagnostic.Severity.ERROR;
		}
	}

	/** Maps the server's completion kinds to SciJava's. */
	protected Completion.Kind kind(final CompletionItemKind kind) {
		if (kind == null) return Completion.Kind.OTHER;
		switch (kind) {
			case Method:
			case Function:
			case Constructor:
				return Completion.Kind.METHOD;
			case Class:
			case Interface:
			case Enum:
			case Struct:
				return Completion.Kind.CLASS;
			case Module:
				return Completion.Kind.PACKAGE;
			case Field:
			case Property:
			case EnumMember:
				return Completion.Kind.FIELD;
			case Keyword:
				return Completion.Kind.KEYWORD;
			case Variable:
			case Constant:
			case Value:
				return Completion.Kind.VARIABLE;
			case Snippet:
				return Completion.Kind.SNIPPET;
			default:
				return Completion.Kind.OTHER;
		}
	}

	// -- Helper methods --

	/**
	 * Gets the answer to a question: a reusable one, if any; otherwise asks,
	 * cancelling the document's previous question, if still pending.
	 */
	@SuppressWarnings("unchecked")
	private <T> CompletableFuture<T> ask(final String uri, final String key,
		final Supplier<Pending> asker)
	{
		// NB: Questions of one kind (e.g. completion) supersede each other only.
		final String kind = key.substring(0, key.indexOf('\n')) + "\n" + uri;
		final long now = clock.getAsLong();
		synchronized (answers) {
			final Answer known = answers.get(key);
			if (known != null && now - known.time < ANSWER_LIFETIME && !known.pending
				.answer.isCompletedExceptionally())
			{
				return (CompletableFuture<T>) known.pending.answer;
			}
		}
		final Pending p = asker.get();
		synchronized (answers) {
			answers.put(key, new Answer(p, now));
		}
		final Pending previous;
		synchronized (pending) {
			previous = pending.put(kind, p);
		}
		if (previous != null && !previous.answer.isDone()) {
			// NB: Cancelling a request tells the server ($/cancelRequest). Cancel
			// the answer first, lest it fail with the request.
			previous.answer.cancel(true);
			for (final CompletableFuture<?> r : previous.requests) r.cancel(true);
		}
		return (CompletableFuture<T>) p.answer;
	}

	/** Identifies a question: where it is asked, and the text before. */
	private static String question(final ScriptDocument doc, final String uri,
		final int at)
	{
		return doc.environment() + "\n" + uri + "\n" + doc.text().substring(0, at);
	}

	/** Converts items to completions, resolving the first few first. */
	private CompletableFuture<List<Completion>> completions(
		final LspServer server, final List<CompletionItem> items,
		final int resolve)
	{
		final List<CompletionItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator.comparing(i -> i.getSortText() != null ? i
			.getSortText() : i.getLabel()));
		final int n = Math.min(resolve, sorted.size());
		final List<CompletableFuture<CompletionItem>> resolved = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			// NB: Unresolved, should resolving fail.
			resolved.add(server.remote().getTextDocumentService()
				.resolveCompletionItem(sorted.get(i)).handle((r, error) -> r));
		}
		return CompletableFuture.allOf(resolved.toArray(new CompletableFuture[0]))
			.thenApply(done -> {
				final List<Completion> out = new ArrayList<>();
				for (int i = 0; i < sorted.size(); i++) {
					out.add(completion(server, sorted.get(i), i < n ? resolved.get(i)
						.join() : null));
				}
				return out;
			});
	}

	/** Converts an item, as resolved (if it was). */
	private Completion completion(final LspServer server,
		final CompletionItem item, final CompletionItem resolved)
	{
		final String text = item.getInsertText() != null ? item.getInsertText()
			: item.getTextEdit() != null && item.getTextEdit().isLeft() ? item
				.getTextEdit().getLeft().getNewText() : item.getLabel();
		final Completion.Kind kind = kind(item.getKind());
		final String detail = resolved != null ? resolved.getDetail() : item
			.getDetail();
		// NB: Classes are callable, too: their constructors.
		Completion.Builder b = (kind == Completion.Kind.METHOD ||
			kind == Completion.Kind.CLASS) && detail != null ? callable(text, detail)
				: null;
		if (b != null) b.kind(Completion.Kind.METHOD);
		else {
			// NB: Without a signature, even a function is offered by name only.
			b = Completion.builder(text).kind(kind == Completion.Kind.METHOD
				? Completion.Kind.OTHER : kind).summary(detail);
		}
		if (resolved != null) {
			final String doc = html(resolved.getDocumentation());
			b.lazyDescription(() -> doc);
		}
		else {
			b.lazyDescription(() -> {
				try {
					return html(server.remote().getTextDocumentService()
						.resolveCompletionItem(item).get(DESCRIBE_TIMEOUT,
							TimeUnit.MILLISECONDS).getDocumentation());
				}
				catch (final Exception exc) {
					return null; // NB: E.g. resolving after a newer completion.
				}
			});
		}
		return b.build();
	}

	private SignatureHelp signatureHelp(final org.eclipse.lsp4j.SignatureHelp help,
		final int callStart, final int active)
	{
		if (help == null || help.getSignatures() == null || help.getSignatures()
			.isEmpty()) return SignatureHelp.NONE;
		final List<SignatureHelp.Signature> signatures = new ArrayList<>();
		for (final SignatureInformation s : help.getSignatures()) {
			final String label = s.getLabel();
			final int paren = label.indexOf('(');
			final String head = (paren < 0 ? label : label.substring(0, paren))
				.trim();
			final String name = head.substring(head.lastIndexOf(' ') + 1);
			Completion.Builder b = callable(name, label);
			if (b == null) {
				final List<Completion.Parameter> params = new ArrayList<>();
				if (s.getParameters() != null) {
					for (final ParameterInformation p : s.getParameters()) {
						params.add(new Completion.Parameter(parameterLabel(label, p
							.getLabel()), null));
					}
				}
				b = Completion.builder(name).kind(Completion.Kind.METHOD).parameters(
					params);
			}
			final String doc = html(s.getDocumentation());
			signatures.add(new SignatureHelp.Signature(b.lazyDescription(() -> doc)
				.build(), Fit.UNKNOWN));
		}
		final Integer index = help.getActiveParameter();
		return new SignatureHelp(signatures, index != null && index >= 0 ? index
			: active, callStart);
	}

	private static String parameterLabel(final String signature,
		final Either<String, Tuple.Two<Integer, Integer>> label)
	{
		if (label.isLeft()) return label.getLeft();
		final Tuple.Two<Integer, Integer> range = label.getRight();
		return signature.substring(range.getFirst(), range.getSecond());
	}

	private Hover hover(final org.eclipse.lsp4j.Hover h,
		final ScriptDocument doc, final int offset)
	{
		if (h == null || h.getContents() == null) return Hover.NONE;
		final StringBuilder text = new StringBuilder();
		final Either<List<Either<String, MarkedString>>, MarkupContent> contents = h
			.getContents();
		if (contents.isRight()) text.append(contents.getRight().getValue());
		else {
			for (final Either<String, MarkedString> part : contents.getLeft()) {
				if (text.length() > 0) text.append("\n\n");
				text.append(part.isLeft() ? part.getLeft() : part.getRight()
					.getValue());
			}
		}
		final String html = html(Either.forLeft(text.toString()));
		if (html == null) return Hover.NONE;
		int start = offset, end = offset;
		if (h.getRange() != null) {
			final int s = doc.map().toOriginal(offset(doc.text(), h.getRange()
				.getStart()));
			final int e = doc.map().toOriginal(offset(doc.text(), h.getRange()
				.getEnd()));
			if (s >= 0 && e >= s) {
				start = s;
				end = e;
			}
		}
		return new Hover(html, start, end);
	}

	private List<org.scijava.code.api.Diagnostic> diagnostics(
		final List<Diagnostic> list, final ScriptDocument doc)
	{
		final List<org.scijava.code.api.Diagnostic> out = new ArrayList<>();
		if (list == null) return out;
		for (final Diagnostic d : list) {
			final int start = doc.map().toOriginal(offset(doc.text(), d.getRange()
				.getStart()));
			if (start < 0) continue; // NB: In the added lines.
			final int end = Math.max(start, doc.map().toOriginal(offset(doc.text(), d
				.getRange().getEnd())));
			out.add(new org.scijava.code.api.Diagnostic(start, end, severity(d
				.getSeverity()), d.getMessage() == null ? null : d.getMessage()
					.isLeft() ? d.getMessage().getLeft() : d.getMessage().getRight()
						.getValue(), d.getSource()));
		}
		return out;
	}

	private static List<CompletionItem> items(
		final Either<List<CompletionItem>, CompletionList> items)
	{
		if (items == null) return Collections.emptyList();
		final List<CompletionItem> list = items.isLeft() ? items.getLeft()
			: items.getRight().getItems();
		return list == null ? Collections.emptyList() : list;
	}

	/**
	 * Converts an offset to an LSP position: a line, and a character in that
	 * line, counted in UTF-16 code units (as Java does).
	 */
	static Position position(final String text, final int offset) {
		int line = 0, start = 0;
		for (int i = 0; i < offset; i++) {
			if (text.charAt(i) == '\n') {
				line++;
				start = i + 1;
			}
		}
		return new Position(line, offset - start);
	}

	/** Converts an LSP position to an offset (clamped to the text). */
	static int offset(final String text, final Position position) {
		int start = 0;
		for (int line = 0; line < position.getLine(); line++) {
			final int nl = text.indexOf('\n', start);
			if (nl < 0) return text.length();
			start = nl + 1;
		}
		int end = text.indexOf('\n', start);
		if (end < 0) end = text.length();
		return Math.min(start + position.getCharacter(), end);
	}

	/** A question asked: the answer to come, and the requests it awaits. */
	private static final class Pending {

		private final CompletableFuture<?> answer;
		private final List<CompletableFuture<?>> requests;

		private Pending(final CompletableFuture<?> answer,
			final List<CompletableFuture<?>> requests)
		{
			this.answer = answer;
			this.requests = requests;
		}
	}

	private static final class Answer {

		private final Pending pending;
		private final long time;

		private Answer(final Pending pending, final long time) {
			this.pending = pending;
			this.time = time;
		}
	}
}
