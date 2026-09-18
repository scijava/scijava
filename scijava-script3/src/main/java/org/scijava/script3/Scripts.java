/*
 * #%L
 * Running scripts as things that declare their inputs and outputs.
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

package org.scijava.script3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Finds the languages available, and makes scripts runnable.
 * <p>
 * Languages come from {@link ServiceLoader} and from the JSR-223 engines on
 * the classpath, so this needs no container and no configuration. For an
 * embedded or tested setting where implicit discovery is unwanted, construct
 * an instance with an explicit list instead.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Scripts {

	private static class DefaultHolder {

		static final Scripts INSTANCE = new Scripts(discoverLanguages());
	}

	private final List<ScriptLanguage> languages;

	/** Creates an instance that uses exactly the given languages. */
	public Scripts(final List<? extends ScriptLanguage> languages) {
		this.languages = new ArrayList<>(languages);
		// NB: highest priority first, so the first claiming an extension wins.
		this.languages.sort(Comparator.comparingDouble(
			(ScriptLanguage l) -> l.priority()).reversed());
	}

	/** Gets the shared instance, backed by discovery. */
	public static Scripts get() {
		return DefaultHolder.INSTANCE;
	}

	/** Gets the languages available, highest priority first. */
	public List<ScriptLanguage> languages() {
		return List.copyOf(languages);
	}

	/**
	 * Finds a language by name or by file extension, either spelled any way.
	 *
	 * @param nameOrExtension "Groovy", "groovy", or ".groovy"
	 * @return the language, or empty if nothing here speaks it
	 */
	public Optional<ScriptLanguage> language(final String nameOrExtension) {
		if (nameOrExtension == null) return Optional.empty();
		final String key = nameOrExtension.toLowerCase(Locale.ROOT) //
			.replaceFirst("^\\.", "");
		return languages.stream().filter(l -> //
		l.name().toLowerCase(Locale.ROOT).equals(key) || l.extensions().stream()
			.anyMatch(e -> e.toLowerCase(Locale.ROOT).equals(key))).findFirst();
	}

	/**
	 * Reads a script file as something that declares its inputs and outputs.
	 * <p>
	 * The language comes from the file's extension.
	 * </p>
	 *
	 * @param path the script to read
	 * @return the script, ready to run through a {@code Runner}
	 * @throws ScriptException if the file cannot be read, or nothing here
	 *           speaks its language
	 */
	public ScriptExecutable of(final Path path) {
		final String fileName = path.getFileName().toString();
		final int dot = fileName.lastIndexOf('.');
		final String extension = dot < 0 ? "" : fileName.substring(dot + 1);
		final ScriptLanguage language = language(extension).orElseThrow(
			() -> new ScriptException("No language for '." + extension +
				"'. Available: " + names()));
		try {
			final String code = new String(Files.readAllBytes(path),
				StandardCharsets.UTF_8);
			return new ScriptExecutable(fileName, code, language);
		}
		catch (final IOException exc) {
			throw new ScriptException("Cannot read script: " + path, exc);
		}
	}

	/**
	 * Takes script source as something that declares its inputs and outputs.
	 *
	 * @param name what to call it
	 * @param code the script
	 * @param nameOrExtension which language it is written in
	 * @return the script, ready to run through a {@code Runner}
	 * @throws ScriptException if nothing here speaks that language
	 */
	public ScriptExecutable of(final String name, final String code,
		final String nameOrExtension)
	{
		final ScriptLanguage language = language(nameOrExtension).orElseThrow(
			() -> new ScriptException("No such language: " + nameOrExtension +
				". Available: " + names()));
		return new ScriptExecutable(name, code, language);
	}

	// -- Helper methods --

	private String names() {
		return languages.stream().map(ScriptLanguage::name).collect(java.util.stream
			.Collectors.joining(", "));
	}

	private static List<ScriptLanguage> discoverLanguages() {
		final List<ScriptLanguage> found = new ArrayList<>();
		// NB: loaded here, in this module, because ServiceLoader resolves `uses`
		// against the calling module.
		ServiceLoader.load(ScriptLanguage.class).forEach(found::add);
		// NB: and every JSR-223 engine, which is how an existing language -
		// Groovy, Jython, Clojure - becomes available by being on the classpath
		// and doing nothing else at all.
		found.addAll(JsrLanguage.discover());
		return found;
	}
}
