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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apposed.appose.util.Environments;
import org.junit.jupiter.api.Test;
import org.scijava.code.lsp.Environment;

/**
 * Unit tests for {@link TyLauncher}.
 *
 * @author Gabriel Selzer
 */
public class TyLauncherTest {

	/**
	 * Tests that the plugin offers servers for {@code python} documents
	 */
	@Test
	public void testLanguage() {
		final TyLauncher plugin = new TyLauncher();
		assertEquals("python", plugin.language());
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
		final TyLauncher plugin = new TyLauncher();
		// For a script running in some environment, the options tell ty to
		// analyze it with that environment's Python, also searching its extra
		// paths (e.g. a dependency overlay); and to report the diagnostics (syntax
		// errors included) of open scripts...
		final Environment env = new Environment().withInterpreter("/env/bin/python")
			.withPaths(List.of("/overlay"));
		assertEquals(Map.of( //
			"configuration", Map.of("environment", Map.of( //
				"python", "/env/bin/python", //
				"extra-paths", List.of("/overlay"))), //
			"diagnosticMode", "openFilesOnly", //
			"showSyntaxErrors", true, //
			"completions", Map.of("autoImport", false)), plugin
				.initializationOptions(env));

		// ...and for one in no particular environment, they name no Python, so
		// ty uses the server's own.
		assertEquals(Map.of( //
			"configuration", Map.of("environment", Map.of( //
				"extra-paths", List.of())), //
			"diagnosticMode", "openFilesOnly", //
			"showSyntaxErrors", true, //
			"completions", Map.of("autoImport", false)), plugin
				.initializationOptions(new Environment()));
	}

	/**
	 * Tests that an interpreter says where its installed packages are, so
	 * that ty is told even of those it would not find itself. Skipped unless
	 * the server's environment is already built.
	 */
	@Test
	public void testSitePackages() {
		final String python = python(new File(Environments.apposeEnvsDir(),
			TyLauncher.ENV));
		assumeTrue(python != null, "The server's environment is not built");
		// The server's environment has ty installed: in its site-packages.
		final List<String> dirs = TyLauncher.sitePackages(python);
		assertTrue(dirs.stream().anyMatch(d -> new File(d, "ty").isDirectory()),
			dirs.toString());
		// Not a Python: nothing.
		assertTrue(TyLauncher.sitePackages("/no/such/python").isEmpty());
	}

	/**
	 * Tests a workspace root directory is always given.
	 */
	@Test
	public void testRootUri() {
		final TyLauncher plugin = new TyLauncher();
		final String root = plugin.rootUri(new Environment());
		assertTrue(new File(URI.create(root)).isDirectory(), root);
	}

	/**
	 * Ensures the server closes (eventually) when the parent is killed before
	 * it can shut down the server (e.g. a crash): otherwise, each would leave a
	 * server process behind. The server runs under the
	 * {@link TyLauncher#LAUNCHER}, which watches its parent.
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
			TyLauncher.ENV));
		assumeTrue(python != null, "The server's environment is not built");

		// A "parent" that is already gone: a process that did nothing, and exited.
		final Process gone = new ProcessBuilder(python, "-c", "pass").start();
		gone.waitFor();

		// The server, as the plugin starts it, but told to watch that process.
		final Process process = new ProcessBuilder(python, "-c",
			TyLauncher.LAUNCHER, String.valueOf(gone.pid()))
				.redirectErrorStream(true).start();

		// It must exit soon after, rather than run on.
		final boolean exited = process.waitFor(30, TimeUnit.SECONDS);
		if (!exited) process.destroyForcibly();
		assertTrue(exited);

		// And it must be the watcher that ended it (with exit code 0), not a
		// failure (e.g. to find ty), whose output (e.g. a
		// traceback) then says why. NB: A failure slower than the watcher may
		// still go unnoticed; TyLifecycleIntegrationTest uses the server.
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
