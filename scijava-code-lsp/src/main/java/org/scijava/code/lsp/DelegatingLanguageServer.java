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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
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

/**
 * A language server passing everything on to another: a base for decorators
 * changing some of its answers.
 *
 * @author Gabriel Selzer
 */
public class DelegatingLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	protected final LanguageServer inner;

	public DelegatingLanguageServer(final LanguageServer inner) {
		this.inner = inner;
	}

	@Override
	public void connect(final LanguageClient client) {
		if (inner instanceof LanguageClientAware) {
			((LanguageClientAware) inner).connect(client);
		}
	}

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

	@Override
	public void didOpen(final DidOpenTextDocumentParams params) {
		inner.getTextDocumentService().didOpen(params);
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		inner.getTextDocumentService().didChange(params);
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
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
		return inner.getTextDocumentService().completion(params);
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

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams p) {
		inner.getWorkspaceService().didChangeConfiguration(p);
	}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {
		inner.getWorkspaceService().didChangeWatchedFiles(p);
	}
}
