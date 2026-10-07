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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link PythonCompletionServer}, around a fake Python server.
 *
 * @author Gabriel Selzer
 */
public class PythonCompletionServerTest {

	private static final String URI = "untitled:/a.py";

	/** Knows json's dumps (a function), dump, decoder (a module), __doc__. */
	private static final class Jedi extends LanguageServersTest.FakeServer {

		final List<Position> asked = new CopyOnWriteArrayList<>();
		final List<String> resolved = new CopyOnWriteArrayList<>();
		CompletableFuture<Void> hold;
		final List<CompletableFuture<?>> held = new CopyOnWriteArrayList<>();

		Jedi() {
			super("jedi", Environment.NONE, false);
			silent = true;
		}

		@Override
		public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
			completion(final CompletionParams p)
		{
			asked.add(p.getPosition());
			final List<CompletionItem> items = new ArrayList<>();
			int i = 0;
			for (final String name : new String[] { "dumps", "dump", "decoder",
				"__doc__" })
			{
				final CompletionItem item = new CompletionItem(name);
				item.setKind(name.equals("decoder") ? CompletionItemKind.Module
					: CompletionItemKind.Function);
				item.setInsertText(name);
				item.setSortText("z" + i++);
				items.add(item);
			}
			final Either<List<CompletionItem>, CompletionList> answer = Either
				.forRight(new CompletionList(items));
			if (hold == null) return CompletableFuture.completedFuture(answer);
			final CompletableFuture<Either<List<CompletionItem>, CompletionList>> f =
				new CompletableFuture<>();
			held.add(f);
			hold.thenRun(() -> f.complete(answer));
			return f;
		}

		@Override
		public CompletableFuture<CompletionItem> resolveCompletionItem(
			final CompletionItem item)
		{
			resolved.add(item.getLabel());
			item.setDetail(item.getLabel().equals("decoder") ? "module decoder"
				: "def " + item.getLabel() + "(obj, *, indent: int=None) -> str");
			return CompletableFuture.completedFuture(item);
		}
	}

	@Test
	public void testAskOnceAndFilter() throws Exception {
		final Jedi jedi = new Jedi();
		final PythonCompletionServer server = new PythonCompletionServer(jedi, 30);
		open(server, "import json\njson.du");
		List<CompletionItem> items = complete(server, "import json\njson.du");
		// Asked about the word's first character.
		assertEquals(new Position(1, 6), jedi.asked.get(0));
		// The word's completions, without dunders.
		assertEquals(Arrays.asList("dumps", "dump"), labels(items));
		// Once a word is begun: callables, from the resolved signatures.
		final CompletionItem dumps = items.get(0);
		assertEquals("(obj, int indent)", dumps.getLabelDetails().getDetail());
		assertEquals("str", dumps.getLabelDetails().getDescription());
		assertEquals("dumps(${1:obj}, ${2:indent})", dumps.getInsertText());

		// Typing on: the same answer, filtered; not asked again.
		change(server, "import json\njson.dum", 2);
		items = complete(server, "import json\njson.dum");
		assertEquals(Arrays.asList("dumps", "dump"), labels(items));
		// (The word typed in full is not offered again.)
		change(server, "import json\njson.dumps", 3);
		assertTrue(complete(server, "import json\njson.dumps").isEmpty());
		assertEquals(1, jedi.asked.size());

		// Nothing typed: names only (nothing resolved), dunders left out.
		change(server, "import json\njson.", 4);
		final int resolved = jedi.resolved.size();
		items = complete(server, "import json\njson.");
		assertEquals(Arrays.asList("dumps", "dump", "decoder"), labels(items));
		assertNull(items.get(0).getLabelDetails());
		assertEquals(resolved, jedi.resolved.size());
	}

	@Test
	public void testImportsAreNames() throws Exception {
		final Jedi jedi = new Jedi();
		final PythonCompletionServer server = new PythonCompletionServer(jedi, 30);
		open(server, "from json import du");
		final List<CompletionItem> items = complete(server, "from json import du");
		assertEquals(Arrays.asList("dumps", "dump"), labels(items));
		assertNull(items.get(0).getLabelDetails());
	}

	@Test
	public void testSuperseded() throws Exception {
		final Jedi jedi = new Jedi();
		jedi.hold = new CompletableFuture<>();
		final PythonCompletionServer server = new PythonCompletionServer(jedi, 0);
		open(server, "x.a");
		final CompletableFuture<Either<List<CompletionItem>, CompletionList>> slow =
			server.completion(params("x.a"));
		change(server, "x.b", 2);
		final CompletableFuture<Either<List<CompletionItem>, CompletionList>> next =
			server.completion(params("x.b"));
		// The first question is cancelled, also on the server.
		assertThrows(Exception.class, () -> slow.get(10, TimeUnit.SECONDS));
		assertTrue(jedi.held.get(0).isCancelled());
		jedi.hold.complete(null);
		assertTrue(next.get(10, TimeUnit.SECONDS).getRight().getItems().isEmpty());
	}

	// -- Helper methods --

	private static CompletionParams params(final String text) {
		return new CompletionParams(new TextDocumentIdentifier(URI), Positions
			.position(text, text.length()));
	}

	private static List<CompletionItem> complete(
		final PythonCompletionServer server, final String text) throws Exception
	{
		return server.completion(params(text)).get(10, TimeUnit.SECONDS)
			.getRight().getItems();
	}

	private static void open(final PythonCompletionServer server,
		final String text)
	{
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(URI,
			"python", 1, text)));
	}

	private static void change(final PythonCompletionServer server,
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

	@Test
	public void testParse() {
		Callable c = PythonCompletionServer.parse("dumps",
			"def dumps(obj, *, skipkeys: bool=False, cls=None, **kw) -> str");
		assertEquals("[obj, bool skipkeys, cls, kw]", String.valueOf(c
			.params()));
		assertEquals("str", c.returnType());

		// Brackets and strings in annotations and defaults; positional-only.
		c = PythonCompletionServer.parse("zeros", "def zeros(shape: tuple[int, ...], dtype: str=\"f,8\"" +
			", /, order: Literal['C', 'F']='C') -> NDArray[float64]");
		assertEquals("[tuple[int, ...] shape, str dtype, " +
			"Literal['C', 'F'] order]", String.valueOf(c.params()));
		assertEquals("NDArray[float64]", c.returnType());

		// Classes (their constructors), without parameters or return type.
		c = PythonCompletionServer.parse("Path", "class Path()");
		assertEquals(0, c.params().size());
		assertNull(c.returnType());
		c = PythonCompletionServer.parse("f", "def f(*args)");
		assertEquals("args", c.params().get(0).name());

		// Not a signature.
		assertNull(PythonCompletionServer.parse("x", "instance x"));
	}
}
