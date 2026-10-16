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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.LongSupplier;

import org.eclipse.lsp4j.TextDocumentItem;
import org.scijava.code.lsp.Environment;

/**
 * Makes packages a script declares (in its PEP 723 {@code # /// script}
 * block), but its environment lacks, known to code completion before the
 * environment is (re)built: e.g. a dependency just added to the script.
 * <p>
 * The missing packages are installed, in the background, into a separate
 * directory per script (an "overlay"), which Jedi adds to its search path.
 * The environment itself is never touched, so this never disturbs whatever
 * runs in it (e.g. an Appose worker). Installs start only once the
 * dependencies have stopped changing for a moment, so that half-typed names
 * do not cause installs; and only if the missing packages differ from the
 * overlay's.
 * </p>
 * <p>
 * Alternatives considered, which may be worth revisiting:
 * </p>
 * <ul>
 * <li><em>Rebuilding the script's environment</em> as its dependencies
 * change. Jedi would then see exactly what will run, but builds can take
 * minutes and gigabytes, half-finished edits would trigger builds, and e.g. Appose
 * rebuilds environments in place, which can interfere with (or, by changing
 * its configuration, release) a resident worker using it.</li>
 * <li><em>Installing nothing</em>, but hinting that new dependencies are
 * known only once the script has run. Cheap and honest, but the user must
 * run the script first. It may still be worthwhile for dependencies an
 * overlay cannot provide: conda packages (e.g.
 * {@code [tool.pixi.dependencies]}), and environments declared in files via
 * {@code #@script(env=...)} in Appose-Python scripts, which overlays do not
 * handle.</li>
 * </ul>
 *
 * @author Gabriel Selzer
 */
final class DependencyOverlays {

	/** Installs the missing packages of a script's environment. */
	@FunctionalInterface
	interface Installer {

		/**
		 * Installs the packages among {@code dependencies} that the environment
		 * with the given Python lacks into the given overlay directory.
		 *
		 * @param python The environment's Python executable, or null if the
		 *          environment has not been built (all packages are missing).
		 * @param dependencies The script's dependencies, as PEP 508 strings.
		 * @param overlay The directory to install into.
		 * @return The overlay directory, or null if nothing is missing.
		 */
		String install(String python, List<String> dependencies, File overlay);
	}

	private final Installer installer;
	private final File root;
	private final long settleMillis;
	private final LongSupplier clock;
	private final ExecutorService executor = Executors.newSingleThreadExecutor(
		r -> {
			final Thread t = new Thread(r, "code-lsp-jedi-overlays");
			t.setDaemon(true);
			return t;
		});

	/** Installs, by environment name, Python and dependencies. */
	private final Map<String, CompletableFuture<String>> installs =
		new HashMap<>();

	/** The dependencies last seen for each environment, and since when. */
	private final Map<String, Seen> seen = new HashMap<>();

	/**
	 * @param installer Does the installing, on a background thread.
	 * @param root Directory holding the overlays (one per script).
	 * @param settleMillis How long dependencies must stay unchanged before
	 *          their missing packages are installed.
	 * @param clock The current time, in milliseconds.
	 */
	DependencyOverlays(final Installer installer, final File root,
		final long settleMillis, final LongSupplier clock)
	{
		this.installer = installer;
		this.root = root;
		this.settleMillis = settleMillis;
		this.clock = clock;
	}

	/**
	 * Gets the environment to analyze a document in: the given one, searching
	 * the overlay of the packages its script declares but the environment
	 * lacks too, once ready (see {@link #overlay}). Never blocks.
	 *
	 * @param doc The document; just opened, if its version is 1, so its
	 *          dependencies need no time to settle.
	 * @param env Where its script runs.
	 */
	Environment environment(final TextDocumentItem doc, final Environment env) {
		final String overlay;
		try {
			final String metadata = InlineMetadata.extract(doc.getText());
			if (metadata == null) return env;
			overlay = overlay(name(doc.getUri()), env.interpreter(), InlineMetadata
				.dependencies(metadata), doc.getVersion() <= 1);
		}
		catch (final RuntimeException exc) {
			return env; // NB: E.g. half-typed metadata.
		}
		if (overlay == null) return env;
		final List<String> paths = new ArrayList<>(env.paths());
		paths.add(overlay);
		return new Environment(env.interpreter(), paths, env.live());
	}

	/**
	 * Gets the overlay providing the packages the environment lacks, if it is
	 * ready; starts preparing it once the dependencies have settled. Never
	 * blocks.
	 *
	 * @param envName The overlay's name (e.g. of its script).
	 * @param python The environment's Python executable, or null if not built.
	 * @param dependencies The script's dependencies (PEP 508 strings).
	 * @param settled Whether the dependencies are known not to be changing
	 *          (e.g. the script was just opened), so need no time to settle.
	 * @return The overlay directory, or null if none is needed or ready.
	 */
	String overlay(final String envName, final String python,
		final List<String> dependencies, final boolean settled)
	{
		if (dependencies == null || dependencies.isEmpty()) return null;
		final String key = envName + "\n" + python + "\n" + dependencies;
		synchronized (installs) {
			final CompletableFuture<String> install = installs.get(key);
			if (install != null) {
				return install.isDone() && !install.isCompletedExceptionally()
					? install.join() : null;
			}
			// Wait for the dependencies to settle, e.g. a name to be typed fully.
			final long now = clock.getAsLong();
			final Seen last = seen.get(envName);
			if (last == null || !last.key.equals(key)) {
				seen.put(envName, new Seen(key, now));
				if (!settled) return null;
			}
			else if (!settled && now - last.since < settleMillis) return null;
			final File overlay = new File(root, envName);
			final CompletableFuture<String> started = CompletableFuture.supplyAsync(
				() -> installer.install(python, dependencies, overlay), executor);
			// NB: Once done, asking again gets the overlay.
			installs.put(key, started);
			return null;
		}
	}

	/**
	 * The name of a document's overlay: after its file, if any; otherwise
	 * {@code unsaved}, as untitled scripts share one.
	 */
	static String name(final String uri) {
		try {
			final URI u = URI.create(uri);
			if ("file".equals(u.getScheme())) {
				final File f = new File(u).getAbsoluteFile();
				return f.getName().replaceAll("[^A-Za-z0-9_.-]", "_") + "-" + Integer
					.toHexString(f.getPath().hashCode());
			}
		}
		catch (final IllegalArgumentException exc) {
			// NB: Not a URI: as unsaved.
		}
		return "unsaved";
	}

	private static final class Seen {

		private final String key;
		private final long since;

		private Seen(final String key, final long since) {
			this.key = key;
			this.since = since;
		}
	}
}
