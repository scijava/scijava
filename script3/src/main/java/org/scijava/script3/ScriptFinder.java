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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Finds the scripts in a directory, and what each says about itself.
 * <p>
 * A script declares its presentation the way a command does, in a
 * {@code #@script} directive:
 * </p>
 *
 * <pre>
 * #@script(menu = "Process&gt;Filters&gt;Blur It", accelerator = "^B")
 * </pre>
 * <p>
 * and where it says nothing, the directory it sits in speaks for it:
 * {@code scripts/Process/Filters/Blur_It.groovy} means the same thing, with
 * underscores read as spaces and the extension dropped - which is how the tens
 * of thousands of scripts already in the wild are arranged.
 * </p>
 * <p>
 * NB: the metadata comes back as a map rather than as a menu entry, because
 * this component knows nothing about menus. An application turns it into one
 * with {@code CommandInfo.of(script, metadata)}, which is the same call any
 * other source of runnable things makes.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ScriptFinder {

	/** A script that was found, and what it says about itself. */
	public static class Found {

		private final ScriptExecutable script;
		private final Map<String, String> metadata;

		Found(final ScriptExecutable script, final Map<String, String> metadata) {
			this.script = script;
			this.metadata = metadata;
		}

		/** Gets the script, ready to run. */
		public ScriptExecutable script() {
			return script;
		}

		/** Gets its presentation metadata: menu path, label, accelerator. */
		public Map<String, String> metadata() {
			return Map.copyOf(metadata);
		}

		@Override
		public String toString() {
			return script.name() + " " + metadata;
		}
	}

	private final Scripts scripts;

	public ScriptFinder() {
		this(Scripts.get());
	}

	public ScriptFinder(final Scripts scripts) {
		this.scripts = scripts;
	}

	/**
	 * Finds every script beneath the given directory.
	 * <p>
	 * A file whose extension no available language claims is skipped, not
	 * fatal: a scripts directory routinely holds text files, images and
	 * languages this installation happens not to have.
	 * </p>
	 *
	 * @param directory the directory to search
	 * @return what was found, in directory order
	 */
	public List<Found> find(final Path directory) {
		if (!Files.isDirectory(directory)) return List.of();
		try (Stream<Path> paths = Files.walk(directory)) {
			return paths.filter(Files::isRegularFile) //
				.map(path -> found(directory, path)) //
				.filter(found -> found != null) //
				.collect(Collectors.toList());
		}
		catch (final IOException exc) {
			throw new UncheckedIOException("Cannot search for scripts: " + directory,
				exc);
		}
	}

	// -- Helper methods --

	private Found found(final Path root, final Path path) {
		final ScriptExecutable script;
		try {
			script = scripts.of(path);
		}
		catch (final ScriptException exc) {
			return null; // NB: not a script, or not a language we have
		}
		final Map<String, String> metadata = new LinkedHashMap<>(script.header()
			.directives());
		metadata.putIfAbsent("menu", menuPath(root, path));
		return new Found(script, metadata);
	}

	/** Reads a menu path off the directory the script sits in. */
	private static String menuPath(final Path root, final Path path) {
		final List<String> elements = new ArrayList<>();
		for (final Path element : root.relativize(path)) {
			elements.add(element.toString());
		}
		// NB: the file name itself is the last element, without its extension.
		final int last = elements.size() - 1;
		final String fileName = elements.get(last);
		final int dot = fileName.lastIndexOf('.');
		elements.set(last, dot < 0 ? fileName : fileName.substring(0, dot));
		return elements.stream().map(e -> e.replace('_', ' ')) //
			.collect(Collectors.joining(">"));
	}
}
