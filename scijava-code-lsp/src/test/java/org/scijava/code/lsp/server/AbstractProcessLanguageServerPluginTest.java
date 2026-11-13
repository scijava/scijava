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

package org.scijava.code.lsp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.scijava.code.lsp.server.FakeServers.ENV;
import static org.scijava.code.lsp.server.FakeServers.complete;
import static org.scijava.code.lsp.server.FakeServers.open;
import static org.scijava.code.lsp.server.FakeServers.waitFor;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.log.LogService;

/**
 * Tests {@link AbstractProcessLanguageServerPlugin}.
 *
 * @author Gabriel Selzer
 */
public class AbstractProcessLanguageServerPluginTest {

	private final FakeServers.Launcher launcher = new FakeServers.Launcher();
	private final List<FakeServers.Server> started = launcher.started;
	private Context context;
	private AbstractProcessLanguageServerPlugin plugin;

	@BeforeEach
	public void setUp() {
		context = new Context(LogService.class);
		// An AbstractProcessLanguageServerPlugin using the fake launcher
		plugin = new AbstractProcessLanguageServerPlugin() {

			@Override
			protected ServerLauncher launcher() {
				return launcher;
			}
		};
		context.inject(plugin);
	}

	@AfterEach
	public void tearDown() {
		plugin.dispose();
		context.dispose();
	}

	/** Tests that the plugin supports its launcher's language, and no other. */
	@Test
	public void testSupports() {
		assertTrue(plugin.supports(launcher.language()));
		final String unsupportedLanguage = "not" + launcher.language();
		assertFalse(plugin.supports(unsupportedLanguage));
	}

	/**
	 * Tests that a process is not started when a server is created, only once
	 * the server is used.
	 */
	@Test
	public void testLazyStart() throws Exception {
		final ProcessLanguageServer s = (ProcessLanguageServer) plugin.create(
			"python", ENV);
		assertEquals(0, started.size());
		// Using it starts it
		final String uri = "untitled:/p.py";
		open(s, uri, "x");
		assertEquals(3, complete(s, uri).size());
		assertEquals(1, started.size());
	}

	/** Tests that disposing of the plugin stops its processes. */
	@Test
	public void testDispose() throws Exception {
		final ProcessLanguageServer s = (ProcessLanguageServer) plugin.create(
			"python", ENV);
		final String uri = "untitled:/p.py";
		open(s, uri, "x");
		complete(s, uri);
		plugin.dispose();
		waitFor(() -> started.get(0).shutdown);
	}
}
