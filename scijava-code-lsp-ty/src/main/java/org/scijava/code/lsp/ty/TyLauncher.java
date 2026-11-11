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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apposed.appose.Appose;
import org.apposed.appose.util.Environments;
import org.apposed.appose.util.Platforms;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.server.ServerLauncher;

/**
 * Starts ty's language server, run in an environment of its own (built by
 * Appose the first time it is needed), to analyze scripts against their
 * environment's Python (if known) and extra search paths (e.g. stubs of Java
 * packages). {@link TyLanguageServerPlugin} serves
 * {@code python} with it.
 *
 * @author Gabriel Selzer
 */
final class TyLauncher implements ServerLauncher {

	/** Name of the environment in which the server runs. */
	static final String ENV = "code-lsp-ty";

	/**
	 * A Python script that runs ty's language server, and stops it if this JVM
	 * dies without stopping it (e.g. when killed).
	 * <p>
	 * The server is told that the environment is an active conda environment:
	 * so, for scripts whose environment's Python is not known, it uses its own
	 * environment's Python.
	 * </p>
	 * <p>
	 * The JVM's process ID is passed as the script's argument. A background
	 * thread waits for that process to end, then kills the server and exits:
	 * </p>
	 * <ul>
	 * <li>On Windows, it waits on a handle to the JVM's process (or not at all,
	 * if the process is already gone).</li>
	 * <li>Elsewhere, it checks every 2 seconds whether its parent is still the
	 * JVM: once the JVM is gone, the operating system gives the server a new
	 * parent.</li>
	 * </ul>
	 */
	static final String LAUNCHER = String.join("\n", //
		"import os, subprocess, sys, threading, time", //
		"from ty import find_ty_bin", //
		"parent = int(sys.argv.pop(1))", //
		"server = subprocess.Popen([find_ty_bin(), 'server'],", //
		"    env=dict(os.environ, CONDA_PREFIX=sys.prefix))", //
		"def watch():", //
		"    if os.name == 'nt':", //
		"        import ctypes", //
		"        k = ctypes.windll.kernel32", //
		"        h = k.OpenProcess(0x00100000, False, parent)  # SYNCHRONIZE", //
		"        if h: k.WaitForSingleObject(h, 0xFFFFFFFF)", //
		"    else:", //
		"        while os.getppid() == parent: time.sleep(2)", //
		"    server.kill()", //
		"    os._exit(0)", //
		"threading.Thread(target=watch, daemon=True).start()", //
		"sys.exit(server.wait())");

	/** How large the error log may grow before it is started anew, in bytes. */
	private static final long MAX_LOG = 5 * 1024 * 1024;

	/** The Python of the server's environment, once built. */
	private static String python;

	/** Where interpreters' installed packages are, as they said. */
	private static final Map<String, List<String>> SITE_PACKAGES =
		new ConcurrentHashMap<>();

	/** An (empty) directory to serve as workspace root. */
	private final File workspace;
	/** Where to append the server's error output. */
	private final File log;

	/** Runs the server in its own environment, building it if needed. */
	TyLauncher() {
		final File envs = new File(Environments.apposeEnvsDir());
		workspace = new File(envs, ENV + "-workspace");
		log = new File(envs, ENV + "-server.log");
	}

	@Override
	public String languageId() {
		return "python";
	}

	@Override
	public Process start(final Environment env) throws IOException {
		final String exe;
		try {
			exe = python();
		}
		catch (final IOException exc) {
			throw exc;
		}
		catch (final Exception exc) {
			throw new IOException("No Python for ty", exc);
		}
		log.getParentFile().mkdirs();
		if (log.length() > MAX_LOG) log.delete();
		final ProcessBuilder pb = new ProcessBuilder(exe, "-c", LAUNCHER, String
			.valueOf(ProcessHandle.current().pid()));
		pb.redirectError(ProcessBuilder.Redirect.appendTo(log));
		return pb.start();
	}

	@Override
	public Object initializationOptions(final Environment env) {
		// Environment options: where the scripts run.
		// NB: Without an interpreter, ty uses the server's own Python.
		final Map<String, Object> environment = new LinkedHashMap<>();
		env.interpreter().ifPresent(p -> environment.put("python", p));
		// NB: Also where the interpreter's packages are, as it says: ty finds
		// them itself only where they usually are (see sitePackages).
		final Set<String> paths = new LinkedHashSet<>(env.paths());
		env.interpreter().ifPresent(p -> paths.addAll(sitePackages(p)));
		environment.put("extra-paths", new ArrayList<>(paths));

		// Combine them all. NB: ty reports the diagnostics (syntax errors
		// included) of open scripts by default; stated as what we rely on.
		final Map<String, Object> options = new LinkedHashMap<>();
		options.put("configuration", Map.of("environment", environment));
		options.put("diagnosticMode", "openFilesOnly");
		options.put("showSyntaxErrors", true);
		// NB: Completions of names the script has in scope only, not of names to
		// import from elsewhere.
		options.put("completions", Map.of("autoImport", false));
		return options;
	}

	@Override
	public String rootUri(final Environment env) {
		// NB: ty needs a workspace root. We give the server an empty one, so it
		// finds no project (or .venv) there
		workspace.mkdirs();
		return workspace.toURI().toString();
	}

	// -- Helper methods --

	/**
	 * Gets the Python of the server's environment: Python, ty and NumPy; built
	 * first if needed (once per session,
	 * which is quick if nothing changed).
	 */
	static synchronized String python() throws Exception {
		if (python != null && new File(python).isFile()) return python;
		final String found = pythonExecutable(Appose.content(config()).scheme(
			"pixi.toml").build());
		if (found == null) throw new IOException("No Python in " + ENV);
		python = found;
		return python;
	}

	/** The server's environment, as a {@code pixi.toml}. */
	static String config() {
		return "[workspace]\n" + //
			"name = \"" + ENV + "\"\n" + //
			"channels = [\"conda-forge\"]\n" + //
			"platforms = [\"" + condaPlatform() + "\"]\n" + //
			"\n" + //
			"[dependencies]\n" + //
			"python = \"3.12.*\"\n" + //
			// NB: So that the types scientific scripts use most are known before
			// their environment is (e.g. the NumPy arrays Appose scripts get images
			// as).
			"numpy = \"*\"\n" + //
			"\n" + //
			"[pypi-dependencies]\n" + //
			"ty = \"==0.0.100\"\n";
	}

	/**
	 * Gets the directories where an interpreter's installed packages are, as
	 * it says ({@code site.getsitepackages()}: those named
	 * {@code site-packages} or {@code dist-packages}); or none, if it cannot
	 * say (e.g. it is not a Python).
	 * <p>
	 * ty looks for them only where they usually are (e.g.
	 * {@code Lib/site-packages}, on Windows), so it misses those of Pythons
	 * that keep them elsewhere: e.g. conda-forge's Python 3.15 on Windows, in
	 * {@code lib/python/site-packages}. Then, the scripts' imports would be
	 * unknown.
	 * </p>
	 *
	 * @param interpreter Path of the Python executable.
	 */
	static List<String> sitePackages(final String interpreter) {
		final List<String> known = SITE_PACKAGES.get(interpreter);
		if (known != null) return known;
		try {
			final Process p = new ProcessBuilder(interpreter, "-I", "-c",
				"import site; print('\\n'.join(site.getsitepackages()))")
					.redirectError(ProcessBuilder.Redirect.DISCARD).start();
			if (!p.waitFor(30, TimeUnit.SECONDS)) {
				p.destroyForcibly();
				return Collections.emptyList();
			}
			if (p.exitValue() != 0) return Collections.emptyList();
			final List<String> dirs = new ArrayList<>();
			for (final String line : new String(p.getInputStream().readAllBytes(),
				StandardCharsets.UTF_8).split("\\R"))
			{
				// NB: Not e.g. the environment's root, which Windows' Pythons list
				// too: its folders (e.g. lib, Scripts) would become packages.
				final File dir = new File(line.trim());
				final String name = dir.getName();
				if (dir.isDirectory() && (name.equalsIgnoreCase("site-packages") ||
					name.equalsIgnoreCase("dist-packages"))) dirs.add(dir.getPath());
			}
			SITE_PACKAGES.put(interpreter, dirs);
			return dirs;
		}
		catch (final IOException exc) {
			// NB: Not a Python we can run; ty, given it too, then says why.
			return Collections.emptyList();
		}
		catch (final InterruptedException exc) {
			Thread.currentThread().interrupt();
			return Collections.emptyList();
		}
	}

	/** The conda platform (e.g. {@code linux-64}) of this machine. */
	private static String condaPlatform() {
		final String os = Platforms.isWindows() ? "win" : Platforms.isMacOS()
			? "osx" : "linux";
		switch (Platforms.ARCH) {
			case ARM64:
				return os + (Platforms.isLinux() ? "-aarch64" : "-arm64");
			case PPC64LE:
				return os + "-ppc64le";
			default:
				return os + "-64";
		}
	}

	/** Finds the Python executable of a built environment, or null. */
	static String pythonExecutable(final org.apposed.appose.Environment env) {
		// NB: Look in the bin paths, and in their parents: on Windows, conda-style
		// (e.g. pixi) environments keep python.exe in the environment's root,
		// above the bin directory that Appose reports.
		final List<File> dirs = new ArrayList<>();
		for (final String dir : env.binPaths()) {
			dirs.add(new File(dir));
			final File parent = new File(dir).getParentFile();
			if (parent != null) dirs.add(parent);
		}
		if (env.base() != null) dirs.add(new File(env.base()));
		for (final File dir : dirs) {
			for (final String exe : new String[] { "python.exe", "python",
				"python3" })
			{
				final File f = new File(dir, exe);
				if (f.isFile()) return f.getAbsolutePath();
			}
		}
		return null;
	}
}
