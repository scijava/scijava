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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apposed.appose.util.Environments;
import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.RoutingLanguageServer;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Serves {@code python}: jedi-language-server (see
 * {@link JediServerProcessPlugin}, which offers it as {@code jedi}), made to
 * work well in an editor (see {@link PythonLanguageServer}); analyzing each
 * script with the packages it declares, but its environment lacks, too (see
 * {@link DependencyOverlays}).
 *
 * @author Gabriel Selzer
 */
@Plugin(type = LanguageServerPlugin.class, name = "Python")
public class PythonServerPlugin implements LanguageServerPlugin {

	/** How many completions get signatures, once a word is begun. */
	private static final int SIGNATURES = 30;

	/**
	 * How long a script's dependencies must stay unchanged before packages
	 * missing from its environment are installed for completion, in ms.
	 */
	private static final long OVERLAY_SETTLE = 1500;

	/** How long installing an overlay may take, in minutes. */
	private static final long INSTALL_TIMEOUT = 15;

	@Parameter
	private LanguageServerService servers;

	private DependencyOverlays overlays;

	@Override
	public boolean supports(final String languageId) {
		// NB: Not "jedi", which this builds on.
		return "python".equals(languageId);
	}

	@Override
	public LanguageServer create(final String languageId,
		final Environment environment)
	{
		// NB: As a script's dependencies change, so does the overlay it is
		// analyzed with, moving it to another jedi server.
		return new PythonLanguageServer(new RoutingLanguageServer(env -> servers
			.launch("jedi", env), doc -> overlays().environment(doc, environment)),
			SIGNATURES);
	}

	/** Sets the dependency overlays, e.g. to use fake ones in tests. */
	synchronized void setOverlays(final DependencyOverlays overlays) {
		this.overlays = overlays;
	}

	// -- Helper methods --

	/** Gets the dependency overlays, creating them on first use. */
	private synchronized DependencyOverlays overlays() {
		if (overlays == null) {
			overlays = new DependencyOverlays(PythonServerPlugin::install, root(),
				OVERLAY_SETTLE, System::currentTimeMillis);
		}
		return overlays;
	}

	/** The directory holding the overlays. */
	private static File root() {
		return new File(Environments.apposeEnvsDir(), JediServerProcessPlugin.ENV +
			"-overlays");
	}

	/**
	 * Installs an overlay by running overlay.py on jedi-language-server's own
	 * Python, whose environment includes uv. Called on the overlays' background
	 * thread.
	 */
	private static String install(final String python,
		final List<String> dependencies, final File overlay)
	{
		try {
			// NB: The script is run from a file, and given its input on stdin: on
			// Windows, arguments with quotes do not reach processes intact.
			final File script = new File(root(), "overlay.py");
			script.getParentFile().mkdirs();
			try (final InputStream in = PythonServerPlugin.class.getResourceAsStream(
				"overlay.py"))
			{
				if (in == null) throw new IOException("overlay.py not found");
				Files.copy(in, script.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			final Map<String, Object> input = new LinkedHashMap<>();
			input.put("python", python);
			input.put("dependencies", dependencies);
			input.put("overlay", overlay.getAbsolutePath());
			final Process p = new ProcessBuilder(JediServerProcessPlugin.python(),
				script.getAbsolutePath()).redirectErrorStream(true).start();
			try (final OutputStream out = p.getOutputStream()) {
				out.write(new ObjectMapper().writeValueAsBytes(input));
			}
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			try (final InputStream in = p.getInputStream()) {
				final byte[] buf = new byte[8192];
				int n;
				while ((n = in.read(buf)) > 0) output.write(buf, 0, n);
			}
			if (!p.waitFor(INSTALL_TIMEOUT, TimeUnit.MINUTES)) {
				p.destroyForcibly();
				throw new IOException("Timed out");
			}
			final String text = new String(output.toByteArray(),
				StandardCharsets.UTF_8).trim();
			if (p.exitValue() != 0) throw new IOException(text);
			// NB: The overlay, on the last line; or nothing, if none is needed.
			final String path = text.substring(text.lastIndexOf('\n') + 1).trim();
			return path.isEmpty() ? null : path;
		}
		catch (final Exception exc) {
			throw new IllegalStateException("Overlay installation failed", exc);
		}
	}
}
