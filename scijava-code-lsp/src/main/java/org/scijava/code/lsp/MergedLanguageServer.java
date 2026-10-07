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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.script.ScriptContext;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
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
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.scijava.script.ScriptLanguage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * One language server standing for all the servers of a script language (see
 * {@link LanguageServerService#server}): it keeps each open document, opens,
 * changes and closes it on every server for that script, and merges their
 * answers, in the plugins' priority order.
 * <ul>
 * <li>Completions: all servers', in order, without duplicates (the same text
 * after inserting). Those not answered within a moment follow as an
 * {@link UpdatingCompletionList#update() update}.</li>
 * <li>Signature help and hover: the first server's that has some.</li>
 * <li>Diagnostics: all servers', published together to this server's
 * client.</li>
 * </ul>
 * <p>
 * Each document's servers depend on where the script runs (see
 * {@link LanguageServerPlugin#environment}): as that changes, the document
 * moves to other servers.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class MergedLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	/** How long to wait for all servers' completions, in milliseconds. */
	private static final long BUDGET = Long.getLong("scijava.lsp.budget", 25);

	private final DefaultLanguageServerService service;
	private final ScriptLanguage language;
	private final List<LanguageServerPlugin> plugins;
	private final ScriptContext live;
	private volatile LanguageClient client;

	/** The open documents, by URI. */
	private final Map<String, Doc> docs = new ConcurrentHashMap<>();

	MergedLanguageServer(final DefaultLanguageServerService service,
		final ScriptLanguage language, final List<LanguageServerPlugin> plugins,
		final ScriptContext live)
	{
		this.service = service;
		this.language = language;
		this.plugins = plugins;
		this.live = live;
	}

	/** The language this serves. */
	public ScriptLanguage language() {
		return language;
	}

	@Override
	public void connect(final LanguageClient c) {
		client = c;
	}

	// -- LanguageServer --

	@Override
	public CompletableFuture<InitializeResult> initialize(
		final InitializeParams params)
	{
		final ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
		capabilities.setCompletionProvider(new CompletionOptions(true,
			Collections.singletonList(".")));
		capabilities.setSignatureHelpProvider(new SignatureHelpOptions(Arrays
			.asList("(", ",")));
		capabilities.setHoverProvider(true);
		return CompletableFuture.completedFuture(new InitializeResult(
			capabilities));
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		for (final String uri : new ArrayList<>(docs.keySet())) {
			didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(
				uri)));
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void exit() {
		// NB: The servers behind belong to the service.
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return this;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		return this;
	}

	// -- TextDocumentService: documents --

	@Override
	public void didOpen(final DidOpenTextDocumentParams params) {
		final TextDocumentItem given = params.getTextDocument();
		final TextDocumentItem item = new TextDocumentItem(given.getUri(), language
			.getLanguageName(), given.getVersion(), given.getText());
		final Doc doc = new Doc(item);
		docs.put(item.getUri(), doc);
		service.own(item.getUri(), this);
		synchronized (doc) {
			for (final Member m : doc.members) open(m, item);
		}
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		final Doc doc = docs.get(params.getTextDocument().getUri());
		final List<TextDocumentContentChangeEvent> changes = params
			.getContentChanges();
		if (doc == null || changes.isEmpty()) return;
		synchronized (doc) {
			// NB: Full sync: the last change is the whole text.
			doc.item = new TextDocumentItem(doc.item.getUri(), doc.item
				.getLanguageId(), params.getTextDocument().getVersion(), changes.get(
					changes.size() - 1).getText());
			doc.reported = new CompletableFuture<>();
			for (final Member m : doc.members) {
				final Environment env = environment(m.plugin, doc.item);
				if (!env.equals(m.environment)) {
					// The script runs elsewhere now: move it to that server.
					// TODO: Settle how often this may happen while dependencies are
					// edited (debounce?).
					close(m, doc.item.getUri());
					m.environment = env;
					m.server = service.server(m.plugin, env);
					m.diagnostics = Collections.emptyList();
					open(m, doc.item);
				}
				else {
					final DidChangeTextDocumentParams p = new DidChangeTextDocumentParams(
						new VersionedTextDocumentIdentifier(doc.item.getUri(), doc.item
							.getVersion()), Collections.singletonList(
								new TextDocumentContentChangeEvent(doc.item.getText())));
					notify(m, s -> s.didChange(p));
				}
			}
		}
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		final String uri = params.getTextDocument().getUri();
		final Doc doc = docs.remove(uri);
		if (doc == null) return;
		service.own(uri, null);
		synchronized (doc) {
			for (final Member m : doc.members) close(m, uri);
		}
	}

	@Override
	public void didSave(final DidSaveTextDocumentParams params) {
		// NB: Changes come with didChange.
	}

	// -- TextDocumentService: requests --

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		final Doc doc = docs.get(params.getTextDocument().getUri());
		if (doc == null) return CompletableFuture.completedFuture(Either.forRight(
			new CompletionList()));
		final String uri = doc.item.getUri();
		final String text = doc.item.getText();
		final int caret = LspClient.offset(text, params.getPosition());
		final List<CompletableFuture<CompletionList>> first = new ArrayList<>();
		final List<CompletableFuture<CompletionList>> last = new ArrayList<>();
		for (final Member m : doc.members) {
			final CompletableFuture<CompletionList> f = ask(m, s -> s.completion(
				params)).thenApply(MergedLanguageServer::list);
			first.add(f);
			last.add(f.thenCompose(MergedLanguageServer::settled));
		}
		final CompletableFuture<Void> all = CompletableFuture.allOf(first.toArray(
			new CompletableFuture[0]));
		return all.completeOnTimeout(null, BUDGET, TimeUnit.MILLISECONDS)
			.thenApply(x -> {
				final boolean settled = last.stream().allMatch(
					CompletableFuture::isDone);
				final List<CompletionList> now = new ArrayList<>();
				for (int i = 0; i < first.size(); i++) {
					now.add(last.get(i).isDone() ? last.get(i).getNow(null) : first.get(
						i).getNow(null));
				}
				final CompletionList merged = merge(now, uri, text, caret);
				if (settled) return merged;
				final CompletableFuture<CompletionList> update = CompletableFuture
					.allOf(last.toArray(new CompletableFuture[0])).thenApply(
						y -> merge(joinAll(last), uri, text, caret));
				return (CompletionList) new UpdatingCompletionList(merged.getItems(),
					update);
			}).thenApply(Either::forRight);
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item)
	{
		final MergedData data = MergedData.of(item.getData());
		final Doc doc = data == null ? null : docs.get(data.uri);
		if (doc == null || data.member < 0 || data.member >= doc.members.length) {
			return CompletableFuture.completedFuture(item);
		}
		final CompletionItem original = copy(item);
		original.setData(data.data);
		original.setSortText(data.sortText);
		return ask(doc.members[data.member], s -> s.resolveCompletionItem(
			original)).thenApply(resolved -> {
				if (resolved == null) return item;
				final CompletionItem out = copy(resolved);
				out.setData(item.getData());
				out.setSortText(item.getSortText());
				return out;
			});
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params)
	{
		return first(params.getTextDocument().getUri(), s -> s.signatureHelp(
			params), h -> h != null && h.getSignatures() != null && !h
				.getSignatures().isEmpty());
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		return first(params.getTextDocument().getUri(), s -> s.hover(params),
			h -> h != null && h.getContents() != null);
	}

	// -- WorkspaceService --

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams p) {
		// NB: Nothing to configure.
	}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {
		// NB: Nothing watched.
	}

	// -- Diagnostics --

	/**
	 * Gets the problems the servers found in a document so far, all together.
	 */
	public List<Diagnostic> diagnostics(final String uri) {
		final Doc doc = docs.get(uri);
		if (doc == null) return Collections.emptyList();
		synchronized (doc) {
			return union(doc);
		}
	}

	/**
	 * Completes once every server has reported the problems of the document's
	 * current version.
	 */
	public CompletableFuture<Void> awaitDiagnostics(final String uri) {
		final Doc doc = docs.get(uri);
		if (doc == null) return CompletableFuture.completedFuture(null);
		synchronized (doc) {
			return doc.reported;
		}
	}

	/** Receives a server's diagnostics (see the service's dispatcher). */
	void received(final LanguageServer server,
		final PublishDiagnosticsParams p)
	{
		final Doc doc = docs.get(p.getUri());
		if (doc == null) return;
		final List<Diagnostic> union;
		final int version;
		synchronized (doc) {
			version = doc.item.getVersion();
			// NB: Those of an earlier version are out of date.
			if (p.getVersion() != null && p.getVersion() < version) return;
			for (final Member m : doc.members) {
				if (m.server.getNow(null) != server) continue;
				m.diagnostics = p.getDiagnostics() == null ? Collections.emptyList()
					: p.getDiagnostics();
				m.reportedVersion = version;
			}
			union = union(doc);
			if (Arrays.stream(doc.members).allMatch(m -> m.reportedVersion >=
				version)) doc.reported.complete(null);
		}
		final LanguageClient c = client;
		if (c != null) c.publishDiagnostics(new PublishDiagnosticsParams(p
			.getUri(), union, version));
	}

	// -- Helper methods --

	private Environment environment(final LanguageServerPlugin plugin,
		final TextDocumentItem item)
	{
		Environment env;
		try {
			env = plugin.environment(item);
		}
		catch (final RuntimeException | LinkageError exc) {
			if (service.logger() != null) service.logger().debug(exc);
			env = null;
		}
		return (env == null ? Environment.NONE : env).withLive(live);
	}

	private void open(final Member m, final TextDocumentItem item) {
		final DidOpenTextDocumentParams p = new DidOpenTextDocumentParams(
			new TextDocumentItem(item.getUri(), item.getLanguageId(), item
				.getVersion(), item.getText()));
		notify(m, s -> s.didOpen(p));
	}

	private void close(final Member m, final String uri) {
		final DidCloseTextDocumentParams p = new DidCloseTextDocumentParams(
			new TextDocumentIdentifier(uri));
		notify(m, s -> s.didClose(p));
	}

	/**
	 * Sends a server a notification, after those sent before. (Callbacks on an
	 * unfinished future need not run in the order they were added.)
	 */
	private void notify(final Member m,
		final Consumer<TextDocumentService> notification)
	{
		final CompletableFuture<LanguageServer> server = m.server;
		m.tail = m.tail.thenCompose(x -> server).thenAccept(s -> notification
			.accept(s.getTextDocumentService())).exceptionally(error -> {
				if (service.logger() != null) service.logger().debug(error);
				return null;
			});
	}

	/** Asks a server, after the notifications sent before; null on failure. */
	private <T> CompletableFuture<T> ask(final Member m,
		final Function<TextDocumentService, CompletableFuture<T>> request)
	{
		final CompletableFuture<LanguageServer> server = m.server;
		return m.tail.thenCompose(x -> server).thenCompose(s -> request.apply(s
			.getTextDocumentService())).exceptionally(error -> {
				if (service.logger() != null) service.logger().debug(error);
				return null;
			});
	}

	/** Asks every server; answers with the first (by priority) accepted. */
	private <T> CompletableFuture<T> first(final String uri,
		final Function<TextDocumentService, CompletableFuture<T>> request,
		final java.util.function.Predicate<T> accepted)
	{
		final Doc doc = docs.get(uri);
		if (doc == null) return CompletableFuture.completedFuture(null);
		final List<CompletableFuture<T>> answers = new ArrayList<>();
		for (final Member m : doc.members) answers.add(ask(m, request));
		return CompletableFuture.allOf(answers.toArray(new CompletableFuture[0]))
			.thenApply(x -> {
				for (final CompletableFuture<T> a : answers) {
					final T t = a.getNow(null);
					if (accepted.test(t)) return t;
				}
				return null;
			});
	}

	private static CompletionList list(
		final Either<List<CompletionItem>, CompletionList> answer)
	{
		if (answer == null) return null;
		return answer.isRight() ? answer.getRight() : new CompletionList(answer
			.getLeft());
	}

	/** The final completions of a server: its update's, if announced. */
	private static CompletableFuture<CompletionList> settled(
		final CompletionList list)
	{
		if (!(list instanceof UpdatingCompletionList)) {
			return CompletableFuture.completedFuture(list);
		}
		final CompletableFuture<CompletionList> update =
			((UpdatingCompletionList) list).update();
		if (update == null) return CompletableFuture.completedFuture(list);
		return update.handle((better, error) -> better != null ? better : list);
	}

	private static List<CompletionList> joinAll(
		final List<CompletableFuture<CompletionList>> lists)
	{
		final List<CompletionList> out = new ArrayList<>();
		for (final CompletableFuture<CompletionList> f : lists) {
			out.add(f.getNow(null));
		}
		return out;
	}

	/**
	 * Merges the servers' completions, in order: each server's in its own
	 * (sortText) order, leaving out those inserting what an earlier one does.
	 */
	private static CompletionList merge(final List<CompletionList> lists,
		final String uri, final String text, final int caret)
	{
		final List<CompletionItem> out = new ArrayList<>();
		// What each inserted name was seen with: its overloads' parameter lists
		// (label details), or null for an item without any.
		final Map<String, Set<String>> seen = new java.util.HashMap<>();
		boolean incomplete = false;
		for (int i = 0; i < lists.size(); i++) {
			final CompletionList list = lists.get(i);
			if (list == null || list.getItems() == null) continue;
			incomplete |= list.isIncomplete() &&
				!(list instanceof UpdatingCompletionList);
			final List<CompletionItem> items = new ArrayList<>(list.getItems());
			items.sort(Comparator.comparing(c -> c.getSortText() != null ? c
				.getSortText() : c.getLabel()));
			int j = 0;
			// NB: A server's own completions are all kept (e.g. the same method
			// inherited twice): only those of earlier servers make duplicates.
			final Map<String, Set<String>> before = new java.util.HashMap<>();
			for (final Map.Entry<String, Set<String>> e : seen.entrySet()) {
				before.put(e.getKey(), new HashSet<>(e.getValue()));
			}
			for (final CompletionItem item : items) {
				final String inserted = insertedLine(item, text, caret);
				if (duplicate(before, inserted, item)) continue;
				final String params = item.getLabelDetails() == null ? null : item
					.getLabelDetails().getDetail();
				seen.computeIfAbsent(inserted, k -> new HashSet<>()).add(params);
				final CompletionItem c = copy(item);
				c.setData(new MergedData(uri, i, item.getData(), item.getSortText()));
				c.setSortText(String.format("%02d%06d", i, j++));
				out.add(c);
			}
		}
		return new CompletionList(incomplete, out);
	}

	/**
	 * Whether a completion inserts what an earlier server's does: the same
	 * name, and the same parameters (overloads differ in their label details),
	 * or either without any (e.g. a server knowing only names).
	 */
	private static boolean duplicate(final Map<String, Set<String>> seen,
		final String name, final CompletionItem item)
	{
		final Set<String> known = seen.get(name);
		if (known == null) return false;
		final String params = item.getLabelDetails() == null ? null : item
			.getLabelDetails().getDetail();
		return params == null || known.contains(null) || known.contains(params);
	}

	/**
	 * What a completion leaves on its line before the caret, roughly: the line
	 * up to the edit (or, without an edit, up to the word before the caret),
	 * then the inserted name (without a parameter snippet). Two servers'
	 * completions doing the same have the same: e.g. one replacing "s.len" with
	 * "s.length", another "len" with "length".
	 */
	private static String insertedLine(final CompletionItem item,
		final String text, final int caret)
	{
		String inserted;
		String before = "";
		if (item.getTextEdit() != null) {
			final Range range = item.getTextEdit().isLeft() ? item.getTextEdit()
				.getLeft().getRange() : item.getTextEdit().getRight().getInsert();
			inserted = item.getTextEdit().isLeft() ? item.getTextEdit().getLeft()
				.getNewText() : item.getTextEdit().getRight().getNewText();
			final Position start = range.getStart();
			final int offset = LspClient.offset(text, start);
			before = text.substring(offset - start.getCharacter(), offset);
		}
		else {
			inserted = item.getInsertText() != null ? item.getInsertText() : item
				.getLabel();
			int word = Math.min(caret, text.length());
			while (word > 0 && Character.isJavaIdentifierPart(text.charAt(word -
				1))) word--;
			before = text.substring(text.lastIndexOf('\n', word - 1) + 1, word);
		}
		final int cut = indexOfAny(inserted, "($");
		return before + (cut < 0 ? inserted : inserted.substring(0, cut));
	}

	private static int indexOfAny(final String s, final String chars) {
		for (int i = 0; i < s.length(); i++) {
			if (chars.indexOf(s.charAt(i)) >= 0) return i;
		}
		return -1;
	}

	private static List<Diagnostic> union(final Doc doc) {
		final List<Diagnostic> all = new ArrayList<>();
		for (final Member m : doc.members) all.addAll(m.diagnostics);
		return all;
	}

	/** A shallow copy of a completion item. */
	static CompletionItem copy(final CompletionItem i) {
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

	/** An open document, and its servers (one per plugin). */
	private final class Doc {

		private TextDocumentItem item;
		private final Member[] members;
		private CompletableFuture<Void> reported = new CompletableFuture<>();

		private Doc(final TextDocumentItem item) {
			this.item = item;
			members = new Member[plugins.size()];
			for (int i = 0; i < members.length; i++) {
				final LanguageServerPlugin p = plugins.get(i);
				final Environment env = environment(p, item);
				members[i] = new Member(p, env, service.server(p, env));
			}
			if (members.length == 0) reported.complete(null);
		}
	}

	/** A plugin's server for a document. */
	private static final class Member {

		private final LanguageServerPlugin plugin;
		private Environment environment;
		private CompletableFuture<LanguageServer> server;
		private CompletableFuture<?> tail = CompletableFuture.completedFuture(null);
		private List<Diagnostic> diagnostics = Collections.emptyList();
		private int reportedVersion = Integer.MIN_VALUE;

		private Member(final LanguageServerPlugin plugin,
			final Environment environment,
			final CompletableFuture<LanguageServer> server)
		{
			this.plugin = plugin;
			this.environment = environment;
			this.server = server;
		}
	}

	/** Remembers which server a merged completion came from. */
	public static final class MergedData {

		private final String uri;
		private final int member;
		private final Object data;
		private final String sortText;

		MergedData(final String uri, final int member, final Object data,
			final String sortText)
		{
			this.uri = uri;
			this.member = member;
			this.data = data;
			this.sortText = sortText;
		}

		/** Reads it back, also as JSON (from a client over JSON-RPC). */
		static MergedData of(final Object o) {
			if (o instanceof MergedData) return (MergedData) o;
			if (!(o instanceof JsonObject)) return null;
			final JsonObject j = (JsonObject) o;
			if (!j.has("uri") || !j.has("member")) return null;
			final JsonElement sort = j.get("sortText");
			return new MergedData(j.get("uri").getAsString(), j.get("member")
				.getAsInt(), j.get("data"), sort == null || sort.isJsonNull() ? null
					: sort.getAsString());
		}
	}
}
