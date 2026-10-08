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

package org.scijava.code.lsp.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
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
import org.eclipse.lsp4j.InsertReplaceEdit;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.scijava.code.lsp.Positions;

/**
 * Shows a language server SciJava scripts as plain code of its language (see
 * {@link ScriptDocument}): e.g. with their {@code #@} parameters declared.
 * Positions in requests are mapped into the transformed text, and those in
 * answers and diagnostics back into the script; whatever lies in the lines
 * the transformation added is left out.
 *
 * @author Gabriel Selzer
 */
public class TransformingLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	private final LanguageServer inner;
	private final Function<String, ScriptDocument> transform;
	private volatile LanguageClient client;

	/** The open documents, transformed, by URI. */
	private final Map<String, ScriptDocument> docs = new ConcurrentHashMap<>();

	/**
	 * @param inner The server to show transformed scripts.
	 * @param transform Transforms a script's text.
	 */
	public TransformingLanguageServer(final LanguageServer inner,
		final Function<String, ScriptDocument> transform)
	{
		this.inner = inner;
		this.transform = transform;
	}

	@Override
	public void connect(final LanguageClient c) {
		client = c;
		if (inner instanceof LanguageClientAware) {
			((LanguageClientAware) inner).connect(new MappingClient());
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
		final TextDocumentItem item = params.getTextDocument();
		final ScriptDocument doc = transform.apply(item.getText());
		docs.put(item.getUri(), doc);
		inner.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
			new TextDocumentItem(item.getUri(), item.getLanguageId(), item
				.getVersion(), doc.text())));
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		final List<TextDocumentContentChangeEvent> changes = params
			.getContentChanges();
		if (changes.isEmpty()) return;
		final String uri = params.getTextDocument().getUri();
		// NB: Full sync: the last change is the whole text.
		final ScriptDocument doc = transform.apply(changes.get(changes.size() - 1)
			.getText());
		docs.put(uri, doc);
		inner.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
			new VersionedTextDocumentIdentifier(uri, params.getTextDocument()
				.getVersion()), Collections.singletonList(
					new TextDocumentContentChangeEvent(doc.text()))));
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		docs.remove(params.getTextDocument().getUri());
		inner.getTextDocumentService().didClose(params);
	}

	@Override
	public void didSave(final DidSaveTextDocumentParams params) {
		inner.getTextDocumentService().didSave(params);
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		final ScriptDocument doc = docs.get(params.getTextDocument().getUri());
		if (doc == null) return inner.getTextDocumentService().completion(params);
		final CompletionParams p = new CompletionParams(params.getTextDocument(),
			toTransformed(doc, params.getPosition()), params.getContext());
		final CompletableFuture<Either<List<CompletionItem>, CompletionList>> raw =
			inner.getTextDocumentService().completion(p);
		final CompletableFuture<Either<List<CompletionItem>, CompletionList>> out =
			raw.thenApply(answer -> answer == null ? null : Either.forRight(back(doc,
				answer.isRight() ? answer.getRight() : new CompletionList(answer
					.getLeft()))));
		// NB: Cancelling the answer cancels the request.
		out.whenComplete((r, error) -> {
			if (out.isCancelled()) raw.cancel(true);
		});
		return out;
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
		final ScriptDocument doc = docs.get(params.getTextDocument().getUri());
		if (doc == null) return inner.getTextDocumentService().signatureHelp(
			params);
		return inner.getTextDocumentService().signatureHelp(new SignatureHelpParams(
			params.getTextDocument(), toTransformed(doc, params.getPosition()),
			params.getContext()));
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		final ScriptDocument doc = docs.get(params.getTextDocument().getUri());
		if (doc == null) return inner.getTextDocumentService().hover(params);
		return inner.getTextDocumentService().hover(new HoverParams(params
			.getTextDocument(), toTransformed(doc, params.getPosition()))).thenApply(
				h -> {
					if (h == null || h.getRange() == null) return h;
					final Hover out = new Hover();
					out.setContents(h.getContents());
					out.setRange(toOriginal(doc, h.getRange()));
					return out;
				});
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

	// -- Position mapping --

	/** Maps a position in a script to one in its transformed text. */
	static Position toTransformed(final ScriptDocument doc, final Position p) {
		final int offset = Positions.offset(doc.original(), p);
		return Positions.position(doc.text(), doc.map().toTransformed(offset));
	}

	/**
	 * Maps a position in the transformed text to one in the script; or returns
	 * null if it lies in the lines the transformation added.
	 */
	static Position toOriginal(final ScriptDocument doc, final Position p) {
		final PositionMap map = doc.map();
		final int offset = map.toOriginal(Positions.offset(doc.text(), p));
		return offset < 0 ? null : Positions.position(doc.original(), offset);
	}

	/** Maps a range back; or returns null if it starts in the added lines. */
	static Range toOriginal(final ScriptDocument doc, final Range r) {
		final Position start = toOriginal(doc, r.getStart());
		final Position end = toOriginal(doc, r.getEnd());
		return start == null || end == null ? null : new Range(start, end);
	}

	/** Maps completions' edits back, leaving out those in the added lines. */
	private static CompletionList back(final ScriptDocument doc,
		final CompletionList list)
	{
		final List<CompletionItem> items = new ArrayList<>();
		for (final CompletionItem item : list.getItems()) {
			final CompletionItem c = copy(item);
			if (item.getTextEdit() != null) {
				if (item.getTextEdit().isLeft()) {
					final TextEdit e = item.getTextEdit().getLeft();
					final Range r = toOriginal(doc, e.getRange());
					if (r == null) continue;
					c.setTextEdit(Either.forLeft(new TextEdit(r, e.getNewText())));
				}
				else {
					final InsertReplaceEdit e = item.getTextEdit().getRight();
					final Range insert = toOriginal(doc, e.getInsert());
					final Range replace = toOriginal(doc, e.getReplace());
					if (insert == null || replace == null) continue;
					c.setTextEdit(Either.forRight(new InsertReplaceEdit(e.getNewText(),
						insert, replace)));
				}
			}
			if (item.getAdditionalTextEdits() != null) {
				final List<TextEdit> edits = new ArrayList<>();
				for (final TextEdit e : item.getAdditionalTextEdits()) {
					final Range r = toOriginal(doc, e.getRange());
					if (r != null) edits.add(new TextEdit(r, e.getNewText()));
				}
				c.setAdditionalTextEdits(edits);
			}
			items.add(c);
		}
		return new CompletionList(list.isIncomplete(), items);
	}

	/** Passes the inner server's diagnostics on, mapped back. */
	private final class MappingClient implements LanguageClient {

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams p) {
			final LanguageClient c = client;
			final ScriptDocument doc = docs.get(p.getUri());
			if (c == null) return;
			if (doc == null) {
				c.publishDiagnostics(p);
				return;
			}
			final List<Diagnostic> mapped = new ArrayList<>();
			for (final Diagnostic d : p.getDiagnostics()) {
				final Range r = toOriginal(doc, d.getRange());
				if (r == null) continue; // NB: In the added lines.
				final Diagnostic m = new Diagnostic();
				m.setRange(r);
				m.setMessage(d.getMessage());
				m.setSeverity(d.getSeverity());
				m.setSource(d.getSource());
				m.setCode(d.getCode());
				m.setTags(d.getTags());
				m.setData(d.getData());
				mapped.add(m);
			}
			c.publishDiagnostics(new PublishDiagnosticsParams(p.getUri(), mapped, p
				.getVersion()));
		}

		@Override
		public void telemetryEvent(final Object object) {
			final LanguageClient c = client;
			if (c != null) c.telemetryEvent(object);
		}

		@Override
		public void showMessage(final MessageParams m) {
			final LanguageClient c = client;
			if (c != null) c.showMessage(m);
		}

		@Override
		public CompletableFuture<MessageActionItem> showMessageRequest(
			final ShowMessageRequestParams r)
		{
			final LanguageClient c = client;
			return c == null ? CompletableFuture.completedFuture(null) : c
				.showMessageRequest(r);
		}

		@Override
		public void logMessage(final MessageParams m) {
			final LanguageClient c = client;
			if (c != null) c.logMessage(m);
		}
	}

	/** A shallow copy of a completion, to change without changing the original. */
	private static CompletionItem copy(final CompletionItem i) {
		final CompletionItem c = new CompletionItem(i.getLabel());
		c.setLabelDetails(i.getLabelDetails());
		c.setKind(i.getKind());
		c.setTags(i.getTags());
		c.setDetail(i.getDetail());
		c.setDocumentation(i.getDocumentation());
		c.setPreselect(i.getPreselect());
		c.setSortText(i.getSortText());
		c.setFilterText(i.getFilterText());
		c.setInsertText(i.getInsertText());
		c.setInsertTextFormat(i.getInsertTextFormat());
		c.setInsertTextMode(i.getInsertTextMode());
		c.setTextEdit(i.getTextEdit());
		c.setTextEditText(i.getTextEditText());
		c.setAdditionalTextEdits(i.getAdditionalTextEdits());
		c.setCommitCharacters(i.getCommitCharacters());
		c.setCommand(i.getCommand());
		c.setData(i.getData());
		return c;
	}
}
