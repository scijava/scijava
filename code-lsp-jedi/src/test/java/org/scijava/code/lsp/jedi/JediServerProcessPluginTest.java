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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apposed.appose.util.Environments;
import org.junit.jupiter.api.Test;
import org.scijava.code.lsp.Environment;

/**
 * Unit tests for {@link JediServerProcessPlugin}.
 *
 * @author Gabriel Selzer
 */
public class JediServerProcessPluginTest {

	/**
	 * Tests that the plugin offers servers for {@code python} documents
	 */
	@Test
	public void testLanguageId() {
		final JediServerProcessPlugin plugin = new JediServerProcessPlugin();
		// Assert this plugin provides a "jedi" LSP (PythonServerPlugin serves "python")
		assertEquals("jedi", plugin.languageId());
	}

	/**
	 * Tests the initialization options the plugin gives a server.
	 * <p>
	 * NB This map populates the "initializationOptions" key in the first
	 * message to the LSP server
	 * </p>
	 */
	@Test
	public void testLanguageInitializationOptions() {
		final JediServerProcessPlugin plugin = new JediServerProcessPlugin();
		// Whatever the environment, Jedi reports syntax errors as a script is
		// opened and edited (rather than only when it is saved).
		final Map<String, Object> diagnostics = Map.of("enable", true, //
			"didOpen", true, "didChange", true, "didSave", false);

		// For a script running in some environment, the options tell Jedi to
		// analyze it with that environment's Python, also searching its extra
		// paths (e.g. a dependency overlay)...
		final Environment env = new Environment("/env/bin/python", List.of(
			"/overlay"), null);
		assertEquals(Map.of( //
			"workspace", Map.of("environmentPath", "/env/bin/python", //
				"extraPaths", List.of("/overlay")), //
			"diagnostics", diagnostics), plugin.initializationOptions(env));

		// ...and for one in no particular environment, they name no Python, so
		// Jedi uses the server's own.
		assertEquals(Map.of( //
			"workspace", Map.of("extraPaths", List.of()), //
			"diagnostics", diagnostics), plugin.initializationOptions(
				Environment.NONE));
	}

	/**
	 * Tests a workspace root directory is always given.
	 */
	@Test
	public void testRootUri() {
		final JediServerProcessPlugin plugin = new JediServerProcessPlugin();
		final String root = plugin.rootUri(Environment.NONE);
		assertTrue(new File(URI.create(root)).isDirectory(), root);
	}

	/**
	 * Ensures the server closes (eventually) when the parent is killed before
	 * it can shut down the server (e.g. a crash): otherwise, each would leave a
	 * Python process behind. The server runs under the
	 * {@link JediServerProcessPlugin#LAUNCHER}, which watches its parent.
	 * Skipped unless the server's environment is already built.
	 * <p>
	 * This JVM cannot be killed here, so the launcher is told to watch a
	 * process that has already exited instead. NB: Only on Windows does it then
	 * really wait on that process; elsewhere, it exits at its first check, as
	 * its actual parent (this JVM) was never the one given.
	 * </p>
	 */
	@Test
	public void testServerExitsWithoutParent() throws Exception {
		// NB: Only once the server's environment is built (e.g. by using it):
		// building it here would need network access.
		final String python = python(new File(Environments.apposeEnvsDir(),
			JediServerProcessPlugin.ENV));
		assumeTrue(python != null, "The server's environment is not built");

		// A "parent" that is already gone: a process that did nothing, and exited.
		final Process gone = new ProcessBuilder(python, "-c", "pass").start();
		gone.waitFor();

		// The server, as the plugin starts it, but told to watch that process.
		final Process process = new ProcessBuilder(python, "-c",
			JediServerProcessPlugin.LAUNCHER, String.valueOf(gone.pid()))
				.redirectErrorStream(true).start();

		// It must exit soon after, rather than run on.
		final boolean exited = process.waitFor(30, TimeUnit.SECONDS);
		if (!exited) process.destroyForcibly();
		assertTrue(exited);

		// And it must be the watcher that ended it (with exit code 0), not a
		// failure (e.g. to import jedi-language-server), whose output (e.g. a
		// traceback) then says why. NB: A failure slower than the watcher may
		// still go unnoticed; JediLifecycleIntegrationTest uses the server.
		final String output = new String(process.getInputStream().readAllBytes());
		assertEquals(0, process.exitValue(), output);
	}

	/** The Python of a pixi environment, as Appose builds them; or null. */
	private static String python(final File envDir) {
		final File env = new File(envDir, ".pixi/envs/default");
		for (final File f : new File[] { new File(env, "python.exe"), new File(env,
			"bin/python") })
		{
			if (f.isFile()) return f.getAbsolutePath();
		}
		return null;
	}
}
