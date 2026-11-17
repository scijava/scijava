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

package org.scijava.lsp;

import static org.junit.jupiter.api.Assertions.*;
import static org.scijava.lsp.FakeLanguageServers.SERVERS;
import static org.scijava.lsp.FakeLanguageServers.labels;
import static org.scijava.lsp.FakeLanguageServers.live;
import static org.scijava.lsp.FakeLanguageServers.server;
import static org.scijava.lsp.FakeLanguageServers.unserved;
import static org.scijava.lsp.FakeLanguageServers.waitFor;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.lsp.FakeLanguageServers.FakeServer;
import org.scijava.lsp.FakeLanguageServers.HighPlugin;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * Tests {@link ScriptSession}, with fake in-process servers (see
 * {@link FakeLanguageServers}).
 *
 * @author Gabriel Selzer
 */
public class ScriptSessionTest {

	/** A script with a parameter, which its dialect declares in a header. */
	private static final String SCIJAVA_PARAMETER = "#@ double sigma\nx.ma";

	private Context context;
	private LanguageServerService service;
	private ScriptLanguage fake;
	/** The problems sessions' listeners heard, in order. */
	private final List<List<Diagnostic>> heard = new CopyOnWriteArrayList<>();

	@BeforeEach
	public void setUp() {
		SERVERS.clear();
		context = new Context(ScriptService.class, LanguageServerService.class);
		service = context.service(LanguageServerService.class);
		fake = context.service(ScriptService.class).getLanguageByName("Fake");
	}

	@AfterEach
	public void tearDown() {
		context.dispose();
	}

	/**
	 * Tests that a session's server comes from the highest-priority plugin
	 * serving the LSP language its script language's dialect names.
	 */
	@Test
	public void testDialectServer() throws Exception {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			// NB: Answered once its server is created.
			session.completion("x", 1).get(10, TimeUnit.SECONDS);
			assertEquals(1, SERVERS.size());
			assertEquals("high", SERVERS.get(0).name);
			assertEquals("fake", SERVERS.get(0).language);
		}
	}

	/** Tests that each session gets a server of its own. */
	@Test
	public void testServerPerSession() {
		// NB: Sessions do not expose their servers, so this checks it in a
		// roundabout way: each session's script is distinct, and each server is
		// told only one of them. A server is created when its session first has
		// a script, so the first is the first session's.
		try (ScriptSession session = new ScriptSession(service, fake);
			ScriptSession other = new ScriptSession(service, fake))
		{
			session.update("a");
			final FakeServer first = server(0);
			other.update("b");
			final FakeServer second = server(1);
			waitFor(() -> first.events.size() >= 2 && second.events.size() >= 2);
			assertEquals(Arrays.asList("initialized", "open 1 a"), first.events);
			assertEquals(Arrays.asList("initialized", "open 1 b"), second.events);
		}
	}

	/** Tests that a session's server is not created until the session has text. */
	@Test
	public void testCreatedForScript() {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			assertTrue(SERVERS.isEmpty());
			session.update("x = 1");
			waitFor(() -> !SERVERS.isEmpty());
		}
	}

	/**
	 * Tests that a session's server is initialized, then opens the script with
	 * its first text, and is told each change as the next version.
	 */
	@Test
	public void testOpenAndChange() {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			session.update("x = 1");
			session.update("x = 2");
			final FakeServer server = server(0);
			waitFor(() -> server.events.size() >= 3);
			assertEquals(Arrays.asList("initialized", "open 1 x = 1",
				"change 2 x = 2"), server.events);
		}
	}

	/** Tests that updating a session with an unchanged text sends nothing. */
	@Test
	public void testUnchanged() {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			session.update("x = 1");
			session.update("x = 1");
			session.update("x = 2");
			final FakeServer server = server(0);
			// NB: the repeat was not sent.
			waitFor(() -> server.events.size() >= 3);
			assertEquals(Arrays.asList("initialized", "open 1 x = 1",
					"change 2 x = 2"), server.events);
		}
	}

	/**
	 * Tests that closing a session closes its script, then shuts down and
	 * exits its server; and that it then answers nothing, with no new server.
	 */
	@Test
	public void testClose() throws Exception {
		final ScriptSession session = new ScriptSession(service, fake);
		session.update("x = 1");
		final FakeServer server = server(0);
		waitFor(() -> server.events.size() >= 2);
		assertEquals(Arrays.asList("initialized", "open 1 x = 1"), server.events);

		session.close();
		waitFor(() -> server.events.size() >= 5);
		assertEquals(Arrays.asList("close", "shutdown", "exit"), server.events
			.subList(2, 5));

		// Once closed: no answers, and no new server.
		assertTrue(session.completion("x = 3", 5).get(10, TimeUnit.SECONDS)
			.getItems().isEmpty());
		assertEquals(1, SERVERS.size());
		assertEquals(5, server.events.size());
	}

	/**
	 * Tests that a session is not sent script text until it has reported
	 * initialization.
	 */
	@Test
	public void testAwaitsInitialization() {
        class DelayedFakeServer extends FakeServer {

			private final CompletableFuture<InitializeResult> initFuture =
				new CompletableFuture<>();

			DelayedFakeServer(final String name, final Environment environment) {
				super(name, environment);
			}

			@Override
			public CompletableFuture<InitializeResult> initialize(
				final InitializeParams params)
			{
				events.add("initialize");
				return initFuture;
			}

			/** Finishes the initialize request. */
			void finishInitializing() {
				initFuture.complete(new InitializeResult(new ServerCapabilities()));
			}
		}
		// A server answering initialize later (e.g. a process starting).
		final DelayedFakeServer late = new DelayedFakeServer("late",
			new Environment());
		HighPlugin.next = late;
		try (ScriptSession session = new ScriptSession(service, fake)) {
			// Send text - initialization will begin...
			session.update("x");
			// ...but will not complete..
			waitFor(() -> Collections.singletonList("initialize").equals(late.events));
			// ...until we let it finish.
			late.finishInitializing();
			// Then, after initialization, we get the text sent.
			waitFor(() -> late.events.size() >= 3);
			assertEquals(Arrays.asList("initialize", "initialized",
				"open 1 x"), late.events);
		}
		finally {
			HighPlugin.next = null;
		}
	}

	/**
	 * Tests that a request about the text the server already has asks the
	 * server at the given offset, without calling {@code didChange} first.
	 */
	@Test
	public void testRequest() throws Exception {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			session.update("x.ma");
			final FakeServer server = server(0);
			waitFor(() -> server.events.size() >= 2);
			session.completion("x.ma", 4).get(10, TimeUnit.SECONDS);
			assertEquals(Collections.singletonList(new Position(0, 4)),
				server.asked);
			assertEquals(Arrays.asList("initialized", "open 1 x.ma"),
				server.events);
		}
	}

	/**
	 * Tests that a script is opened as an untitled document; and, once saved
	 * as a file, closed and opened anew as the file.
	 */
	@Test
	public void testSavedAsFile() {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			session.update("x");
			final FakeServer server = server(0);
			waitFor(() -> server.events.size() >= 2);
			assertTrue(server.opened.get(0).startsWith("untitled:/script-"));
			final File file = new File("scripts", "a.fake");
			session.setFile(() -> file);
			session.update("x");
			waitFor(() -> server.events.size() >= 4);
			assertEquals(Arrays.asList("close", "open 1 x"), server.events.subList(
				2, 4));
			assertEquals(file.getAbsoluteFile().toURI().toString(), server.opened
				.get(1));
		}
	}

	/** Tests that the problems a server publishes are heard. */
	@Test
	public void testDiagnostics() {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			session.setDiagnosticsListener(heard::add);
			// NB: The fake server complains about any text but "fine".
			session.update("oops");
			awaitHeard("high: oops");
		}
	}

	/** Tests that problems of an earlier version of a script are not heard. */
	@Test
	public void testStaleDiagnostics() {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			session.setDiagnosticsListener(heard::add);
			// Sends the script as version 1, then version 2.
			session.update("x");
			session.update("y");
			// Waits until the server has both, so that what it publishes about
			// them on its own is heard first.
			final FakeServer server = server(0);
			waitFor(() -> server.events.size() >= 3);
			final String uri = server.opened.get(0);
			// Problems of version 2 are heard...
			server.publish(uri, 2, "current");
			awaitHeard("high: current");
			// ...but those of version 1, published after, are not: the last
			// problems heard are still those of version 2.
			server.publish(uri, 1, "stale");
			assertEquals(Collections.singletonList("high: current"), messages(heard
				.get(heard.size() - 1)));
		}
	}

	/** Tests that a session passes on its server's signature help and hover. */
	@Test
	public void testSignatureHelpAndHover() throws Exception {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			final SignatureHelp help = session.signatureHelp("max(1, ", 7).get(10,
				TimeUnit.SECONDS);
			assertEquals("max(int a, int b)", help.getSignatures().get(0)
				.getLabel());
			final Hover hover = session.hover("max(1, ", 1).get(10,
				TimeUnit.SECONDS);
			assertEquals("high hover", hover.getContents().getRight().getValue());
		}
	}

	/**
	 * Tests that a session transforms SciJava scripts (see
	 * {@link ScriptDocument}) before sending them to the server.
	 */
	@Test
	public void testTransformedText() {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			session.update(SCIJAVA_PARAMETER);
			final FakeServer server = server(0);
			waitFor(() -> server.events.size() >= 2);
			assertEquals("open 1 import mass; sigma: double\n" +
				SCIJAVA_PARAMETER.replace("#@ double sigma", "               "),
				server.events.get(1));
		}
	}

	/**
	 * Tests that requests are asked of the server one line down, past the
	 * header, and that the edits of its completions are mapped back up.
	 */
	@Test
	public void testCompletionMapped() throws Exception {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			final String script = "#@ double sigma\nx.ma";
			// Asks for completions at the end of line 1, after "x.ma".
			final CompletionList list = session.completion(script, script.length())
				.get(10, TimeUnit.SECONDS);
			// The server sees the header on line 0 and the blanked parameter on
			// line 1, so "x.ma" is on line 2.
			assertEquals(new Position(2, 4), server(0).asked.get(0));
			// The completion's edit, of "ma", is mapped back to line 1.
			final Range ma = new Range(new Position(1, 2), new Position(1, 4));
			assertEquals(ma, list.getItems().get(0).getTextEdit().getLeft()
				.getRange());
		}
	}

	/** Tests that names only the header binds are not completed. */
	@Test
	public void testHiddenNames() throws Exception {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			final CompletionList list = session.completion(SCIJAVA_PARAMETER,
				SCIJAVA_PARAMETER.length()).get(10, TimeUnit.SECONDS);
			// NB: The fake server offers "max" and "mass"; the header imports mass.
			assertEquals(Collections.singletonList("max"), labels(list));
		}
	}

	/**
	 * Tests that the server's problems are mapped back up, and those in the
	 * header left out.
	 */
	@Test
	public void testDiagnosticsMapped() throws Exception {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			session.setDiagnosticsListener(heard::add);
			// Sends a script with a parameter, which the server sees as the header
			// on line 0, the blanked parameter on line 1, and "x.ma" on line 2.
			session.update("#@ double sigma\nx.ma");
			// Waits until the server has it, so that what it publishes about it on
			// its own is heard first.
			final FakeServer server = server(0);
			waitFor(() -> server.events.size() >= 2);
			final String uri = server.opened.get(0);
			// A problem with "x" is heard mapped back to line 1...
			server.publish(uri, 1, "oops", new Range(new Position(2, 0),
				new Position(2, 1)));
			final List<Diagnostic> problems = heard.get(heard.size() - 1);
			assertEquals(1, problems.size());
			assertEquals(new Range(new Position(1, 0), new Position(1, 1)), problems
				.get(0).getRange());
			// ...but one in the header is left out.
			server.publish(uri, 1, "oops", new Range(new Position(0, 0),
				new Position(0, 5)));
			assertTrue(heard.get(heard.size() - 1).isEmpty());
		}
	}

	/**
	 * Tests that a session about a language with no dialect, typed into a live
	 * interpreter, gets that interpreter's variables completed.
	 */
	@Test
	public void testFallbackSession() throws Exception {
		try (ScriptSession session = new ScriptSession(
			service,
			unserved(),
			live()
		))
		{
			// NB The "count" completion comes from live()
			assertEquals(
				Collections.singletonList("count"),
				labels(session .completion("x = co", 6).get(10, TimeUnit.SECONDS))
			);
		}
	}

	/**
	 * Tests that when where a script runs changes as it is edited, its session
	 * moves to a server of where it runs now, which is told the script anew,
	 * and gets later edits.
	 */
	@Test
	public void testMoving() {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			// NB: The fake dialect says scripts run where #@script(env=...) says.
			// Sends a script that runs in /a
			session.update("#@script(env=/a)\nx.ma");
			assertEquals(Optional.of("/a"), server(0).environment().interpreter());
			// Edit it to run in /b. This will start a second server.
			session.update("#@script(env=/b)\nx.ma");
			final FakeServer second = server(1);
			assertEquals(Optional.of("/b"), second.environment().interpreter());
			// Sends a further edit, which still runs in /b.
			session.update("#@script(env=/b)\nx.max");
			// The second server is opened with the script at its current version,
			// then told the edit, with the (blanked) directive in each.
			waitFor(() -> second.events.size() >= 3);
			final String blanked = "                ";
			assertEquals(Arrays.asList("initialized", "open 2 " + blanked +
				"\nx.ma", "change 3 " + blanked + "\nx.max"), second.events);
		}
	}

	/**
	 * Tests that the server a session moved away from is let go, and that what
	 * it made (completion items to resolve, problems) is not used.
	 */
	@Test
	public void testMovedAway() throws Exception {
		try (ScriptSession session = new ScriptSession(service, fake)) {
			session.setDiagnosticsListener(heard::add);
			// NB: The fake dialect says scripts run where #@script(env=...) says.
			final String atA = "#@script(env=/a)\nx.ma";
			final String atB = "#@script(env=/b)\nx.ma";
			session.completion(atA, atA.length()).get(10, TimeUnit.SECONDS);
			final FakeServer first = server(0);
			// Its completion items: not resolved by the next server.
			session.update(atB);
			assertNull(session.resolve(new CompletionItem("max")).get(10,
				TimeUnit.SECONDS));
			session.completion(atB, atB.length()).get(10, TimeUnit.SECONDS);
			// It is let go...
			waitFor(() -> first.events.contains("shutdown"));
			// ...and what it still says, not heard.
			first.publish(first.opened.get(0), 2, "stale");
			assertTrue(heard.stream().flatMap(List::stream).noneMatch(d -> d
				.getMessage().getLeft().contains("stale")));
		}
	}

	// -- Helper methods --

	/** Waits until the problems heard last are those given. */
	private void awaitHeard(final String... expected) {
		waitFor(() -> !heard.isEmpty() && messages(heard.get(heard.size() - 1))
			.equals(Arrays.asList(expected)));
	}

	private static List<String> messages(final List<Diagnostic> diagnostics) {
		return diagnostics.stream().map(d -> d.getMessage().getLeft()).collect(
			Collectors.toList());
	}
}
