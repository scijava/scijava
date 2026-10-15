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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.Positions;


/**
 * Integration tests for {@link JediServerProcessPlugin}.
 * Additionally useful for understanding usage.
 *
 * @author Gabriel Selzer
 */
public class JediLifecycleIntegrationTest {

	@Test
	public void testLifecycle() throws Exception {
		try (final Context context = new Context(LanguageServerService.class)){
			// Get a LanguageServer Object
			final LanguageServerService service = context.service(LanguageServerService.class);
			final LanguageServer server = service.launch("python", Environment.NONE);
			// Start it up
			server.initialize(new InitializeParams()).get();
			server.initialized(new InitializedParams());

			// Open a script...
			final String uri = "untitled:/example.py";
			final String script = "import json\n" + //
				"data = json.loads('[1, 2]')\n" + //
				"json.du";
			server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
				new TextDocumentItem(uri, "python", 1, script)));

			// ...ask for completions at its end ("json.du|")...
			final CompletionParams params = new CompletionParams(
					new TextDocumentIdentifier(uri), Positions.position(script, script.length())
			);
			final List<CompletionItem> items = server.getTextDocumentService()
				.completion(params)
				.get(15, TimeUnit.MINUTES)
				.map(list -> list, CompletionList::getItems);
			final List<String> labels = items.stream().map(CompletionItem::getLabel)
				.collect(Collectors.toList());
			assertTrue(labels.contains("dumps"), labels.toString());
			assertTrue(labels.contains("dump"), labels.toString());

			// ...and what "loads" is, by hovering over it: its signature, then its
			// documentation (in Markdown).
			final Hover hover = server.getTextDocumentService().hover(
				new HoverParams(new TextDocumentIdentifier(uri), Positions.position(
					script, script.indexOf("loads")))).get(1, TimeUnit.MINUTES);
			assertNotNull(hover);
			final String contents = hover.getContents().getRight().getValue();
			assertTrue(contents.startsWith("def loads("), contents);

			// The server runs as a process of this JVM's.
			assertFalse(servers().isEmpty());

			// Shut it down: the server, then the service, which stops the
			// processes.
			server.shutdown().get();

		}
		await().atMost(30, TimeUnit.SECONDS)
				.pollInterval(100, TimeUnit.MILLISECONDS)
				.until(() -> servers().isEmpty());
	}

	/** The processes this JVM has started that are still running. */
	private static List<ProcessHandle> servers() {
		return ProcessHandle.current().descendants().filter(ProcessHandle::isAlive)
			.collect(Collectors.toList());
	}
}
