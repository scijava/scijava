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

package org.scijava.lsp.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.jupiter.api.Test;
import org.scijava.lsp.Environment;

/**
 * Tests {@link BindingsLanguageServer}.
 *
 * @author Gabriel Selzer
 */
public class BindingsLanguageServerTest {

	/**
	 * Tests that the variables of the live interpreter are completed: those
	 * starting with the word before the caret (ignoring case), by name.
	 */
	@Test
	public void testVariables() throws Exception {
		final ScriptContext live = new SimpleScriptContext();
		live.setAttribute("text", "hello", ScriptContext.ENGINE_SCOPE);
		live.setAttribute("count", 3, ScriptContext.ENGINE_SCOPE);
		final BindingsLanguageServer server = new BindingsLanguageServer(
				new Environment().withLive(live)
		);
		assertEquals(Collections.singletonList("count"), labels(complete(
			server, "x = co")));
		assertEquals(Collections.singletonList("count"), labels(complete(
			server, "x = CO")));
		assertEquals(Arrays.asList("count", "text"), labels(complete(server, "")));
	}

	/**
	 * Tests that the members of a variable's value are completed, replacing
	 * the prefix typed after the dot.
	 */
	@Test
	public void testMembers() throws Exception {
		final ScriptContext live = new SimpleScriptContext();
		live.setAttribute("text", "hello", ScriptContext.ENGINE_SCOPE);
		final BindingsLanguageServer server = new BindingsLanguageServer(
				new Environment().withLive(live)
		);
		final CompletionList members = complete(server, "text.toUpp");
		// NB: Two overloads, of different arity.
		assertEquals(Arrays.asList("toUpperCase", "toUpperCase"), labels(
			members));
		final CompletionItem upper = members.getItems().get(0);
		assertTrue(upper.getTextEdit().getLeft().getNewText().startsWith(
			"toUpperCase("));
		assertEquals(new Range(new Position(0, 5), new Position(0, 10)), upper
			.getTextEdit().getLeft().getRange());
	}

	/** Tests that nothing is completed after an unknown variable. */
	@Test
	public void testUnknownVariable() throws Exception {
		final BindingsLanguageServer server = new BindingsLanguageServer(new Environment());
		assertTrue(complete(server, "nothing.x").getItems().isEmpty());
	}

	/** Tests that nothing is completed without a live interpreter. */
	@Test
	public void testNoInterpreter() throws Exception {
		assertTrue(complete(new BindingsLanguageServer(new Environment()), "co")
			.getItems().isEmpty());
	}

	// -- Helper methods --

	private static CompletionList complete(final BindingsLanguageServer server,
		final String text) throws Exception
	{
		final String uri = "untitled:/live";
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri,
			"live", 1, text)));
		final Position end = new Position(0, text.length());
		return server.completion(new CompletionParams(new TextDocumentIdentifier(
			uri), end)).get(10, TimeUnit.SECONDS).getRight();
	}

	private static List<String> labels(final CompletionList list) {
		return list.getItems().stream().map(CompletionItem::getLabel).collect(
			Collectors.toList());
	}
}
