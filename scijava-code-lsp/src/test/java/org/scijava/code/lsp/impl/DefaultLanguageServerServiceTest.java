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

package org.scijava.code.lsp.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.scijava.code.lsp.FakeLanguageServers.SERVERS;

import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.FakeLanguageServers;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * Tests {@link DefaultLanguageServerService}, with fake plugins (see
 * {@link FakeLanguageServers}).
 *
 * @author Gabriel Selzer
 */
public class DefaultLanguageServerServiceTest {

	private Context context;
	private LanguageServerService service;
	private ScriptLanguage fake;

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
	 * Tests that launching a server gets a new one from the highest-priority
	 * plugin serving the language, in the environment given.
	 */
	@Test
	public void testLaunch() {
		final Environment env = new Environment().withInterpreter("/env/bin/fake");
		final LanguageServer server = service.launch("fake", env);
		assertSame(SERVERS.get(0), server);
		assertEquals("high", SERVERS.get(0).name);
		assertEquals("fake", SERVERS.get(0).language);
		assertSame(env, SERVERS.get(0).environment());
	}

	/**
	 * Tests that launching a server of a language no plugin serves gets the
	 * fallback server, of the live interpreter's variables (see
	 * {@link BindingsLanguageServerTest}).
	 */
	@Test
	public void testFallback() {
		// NB: No plugin serves cobol.
		final LanguageServer server = service.launch("cobol", new Environment());
        assertInstanceOf(BindingsLanguageServer.class, server);
		assertTrue(SERVERS.isEmpty());
	}

	/**
	 * Tests that the service supports a script language whose dialect's LSP
	 * language a plugin serves, and an LSP language a plugin serves; and no
	 * others.
	 */
	@Test
	public void testSupports() {
		assertTrue(service.supports(fake));
		assertFalse(service.supports((ScriptLanguage) null));
		// By LSP language.
		assertTrue(service.supports("fake"));
		assertFalse(service.supports("cobol"));
	}
}
