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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.scijava.code.lsp.jvm.Callable;
import org.scijava.code.lsp.Positions;

/**
 * Tests {@link TyLanguageServer}, around a fake Python server.
 *
 * @author Gabriel Selzer
 */
public class TyLanguageServerTest {

	private static final String URI = "untitled:/a.py";

	/**
	 * Knows json's dumps and dump (functions), decoder (a module), and
	 * __doc__, with their signatures as ty gives them; whatever is typed, as
	 * ty matches loosely.
	 */
	private static final class Ty implements LanguageServer,
		TextDocumentService, WorkspaceService
	{

		final List<Position> asked = new CopyOnWriteArrayList<>();

		@Override
		public CompletableFuture<InitializeResult> initialize(
			final InitializeParams params)
		{
			return CompletableFuture.completedFuture(new InitializeResult(
				new ServerCapabilities()));
		}

		@Override
		public CompletableFuture<Object> shutdown() {
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void exit() {}

		@Override
		public TextDocumentService getTextDocumentService() {
			return this;
		}

		@Override
		public WorkspaceService getWorkspaceService() {
			return this;
		}

		@Override
		public void didOpen(final DidOpenTextDocumentParams p) {}

		@Override
		public void didChange(final DidChangeTextDocumentParams p) {}

		@Override
		public void didClose(final DidCloseTextDocumentParams p) {}

		@Override
		public void didSave(final DidSaveTextDocumentParams p) {}

		@Override
		public void didChangeConfiguration(final DidChangeConfigurationParams p) {}

		@Override
		public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {}

		@Override
		public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
			completion(final CompletionParams p)
		{
			asked.add(p.getPosition());
			final List<CompletionItem> items = new ArrayList<>();
			items.add(item("dumps", CompletionItemKind.Function,
				"def dumps(obj: Any, *, indent: int | None = None) -> str"));
			items.add(item("dump", CompletionItemKind.Function,
				"def dump(obj: Any, fp: SupportsWrite[str]) -> None"));
			items.add(item("decoder", CompletionItemKind.Module,
				"<module 'json.decoder'>"));
			items.add(item("__doc__", CompletionItemKind.Variable, "str | None"));
			return CompletableFuture.completedFuture(Either.forRight(
				new CompletionList(items)));
		}

		private static CompletionItem item(final String name,
			final CompletionItemKind kind, final String detail)
		{
			final CompletionItem item = new CompletionItem(name);
			item.setKind(kind);
			item.setDetail(detail);
			return item;
		}
	}

	@Test
	public void testFilter() throws Exception {
		final Ty ty = new Ty();
		final TyLanguageServer server = new TyLanguageServer(ty);
		open(server, "import json\njson.du");
		// Asked at the caret; only the completions of the word typed so far.
		assertEquals(Arrays.asList("dumps", "dump"), labels(complete(server,
			"import json\njson.du")));
		assertEquals(new Position(1, 7), ty.asked.get(0));
		// The word typed in full is not offered again.
		change(server, "import json\njson.dumps", 2);
		assertTrue(complete(server, "import json\njson.dumps").isEmpty());
		// Nothing typed: all, but private ones.
		change(server, "import json\njson.", 3);
		assertEquals(Arrays.asList("dumps", "dump", "decoder"), labels(complete(
			server, "import json\njson.")));
		// Private ones, once asked for.
		change(server, "import json\njson._", 4);
		assertEquals(Arrays.asList("__doc__"), labels(complete(server,
			"import json\njson._")));
	}

	@Test
	public void testCallables() throws Exception {
		final TyLanguageServer server = new TyLanguageServer(new Ty());
		open(server, "import json\njson.du");
		final List<CompletionItem> items = complete(server,
			"import json\njson.du");
		// From their signatures.
		final CompletionItem dumps = items.get(0);
		assertEquals("(Any obj, int | None indent)", dumps.getLabelDetails()
			.getDetail());
		assertEquals("str", dumps.getLabelDetails().getDescription());
		assertEquals("dumps(${1:obj}, ${2:indent})", dumps.getInsertText());
		// Not those without.
		change(server, "import json\njson.dec", 2);
		assertNull(complete(server, "import json\njson.dec").get(0)
			.getLabelDetails());
	}

	@Test
	public void testImportsAreNames() throws Exception {
		final TyLanguageServer server = new TyLanguageServer(new Ty());
		open(server, "from json import du");
		final List<CompletionItem> items = complete(server, "from json import du");
		assertEquals(Arrays.asList("dumps", "dump"), labels(items));
		assertNull(items.get(0).getLabelDetails());
	}

	@Test
	public void testParse() {
		Callable c = TyLanguageServer.parse("dumps",
			"def dumps(obj, *, skipkeys: bool=False, cls=None, **kw) -> str");
		assertEquals("[obj, bool skipkeys, cls, kw]", String.valueOf(c
			.params()));
		assertEquals("str", c.returnType());

		// Brackets and strings in annotations and defaults; positional-only.
		c = TyLanguageServer.parse("zeros", "def zeros(shape: tuple[int, ...], dtype: str=\"f,8\"" +
			", /, order: Literal['C', 'F']='C') -> NDArray[float64]");
		assertEquals("[tuple[int, ...] shape, str dtype, " +
			"Literal['C', 'F'] order]", String.valueOf(c.params()));
		assertEquals("NDArray[float64]", c.returnType());

		// Methods.
		c = TyLanguageServer.parse("isupper",
			"bound method Literal[\"x\"].isupper() -> bool");
		assertEquals(0, c.params().size());
		assertEquals("bool", c.returnType());

		// Overloads: the first.
		c = TyLanguageServer.parse("join", "Overload[(path: LiteralString, " +
			"/, *paths: LiteralString) -> LiteralString, (path: str, /, " +
			"*paths: str) -> str]");
		assertEquals("[LiteralString path, LiteralString paths]", String.valueOf(c
			.params()));
		assertEquals("LiteralString", c.returnType());

		// Without return types.
		c = TyLanguageServer.parse("f", "def f(*args)");
		assertEquals("args", c.params().get(0).name());
		assertNull(c.returnType());

		// Not signatures.
		assertNull(TyLanguageServer.parse("x", "str"));
		assertNull(TyLanguageServer.parse("Path", "<class 'Path'>"));
	}

	// -- Helper methods --

	private static List<CompletionItem> complete(
		final TyLanguageServer server, final String text) throws Exception
	{
		return server.completion(new CompletionParams(new TextDocumentIdentifier(
			URI), Positions.position(text, text.length()))).get(10,
				TimeUnit.SECONDS).getRight().getItems();
	}

	private static void open(final TyLanguageServer server,
		final String text)
	{
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(URI,
			"python", 1, text)));
	}

	private static void change(final TyLanguageServer server,
		final String text, final int version)
	{
		server.didChange(new DidChangeTextDocumentParams(
			new VersionedTextDocumentIdentifier(URI, version), Collections
				.singletonList(new TextDocumentContentChangeEvent(text))));
	}

	private static List<String> labels(final List<CompletionItem> items) {
		return items.stream().map(CompletionItem::getLabel).collect(Collectors
			.toList());
	}
}
