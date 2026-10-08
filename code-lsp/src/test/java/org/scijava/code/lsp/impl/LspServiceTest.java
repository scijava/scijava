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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.launch.LspConnection;
import org.scijava.code.lsp.launch.LspServerPlugin;
import org.scijava.code.lsp.launch.LspServerSpec;
import org.scijava.plugin.Plugin;

/**
 * Tests {@link DefaultLspService}.
 *
 * @author Gabriel Selzer
 */
public class LspServiceTest {

	@Test
	public void testServersFromPlugins() {
		final Context context = new Context(LspService.class);
		try {
			final LspService service = context.service(LspService.class);
			final LspServers fake = service.servers("fake");
			assertNotNull(fake);
			// The same, for every completer asking.
			assertSame(fake, service.servers("fake"));
			// No plugin, no servers.
			assertNull(service.servers("cobol"));
		}
		finally {
			context.dispose();
		}
	}

	/** Offers a server that cannot start. */
	@Plugin(type = LspServerPlugin.class)
	public static class FakeServerPlugin implements LspServerPlugin {

		@Override
		public boolean supports(final String languageId) {
			return "fake".equals(languageId);
		}

		@Override
		public LspServerSpec spec() {
			return new LspServerSpec() {

				@Override
				public String languageId() {
					return "fake";
				}

				@Override
				public LspConnection start(final Environment env)
					throws IOException
				{
					throw new IOException("Not a real server");
				}
			};
		}
	}
}
