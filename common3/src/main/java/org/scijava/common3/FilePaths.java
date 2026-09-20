/*
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.common3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Useful methods for working with file names and paths.
 * <p>
 * Most of SciJava Common's {@code FileUtils} is not here, because
 * {@link java.nio.file.Files} and {@link java.nio.file.Path} now cover it:
 * reading and writing a file, temporary directories, and modification times.
 * What remains is the filename logic the standard library has no answer for -
 * notably versioned JAR names - plus recursive deletion, which
 * {@link java.nio.file.Files} still lacks.
 * </p>
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public final class FilePaths {

	private static final Pattern VERSION_PATTERN = buildVersionPattern();

	private FilePaths() {
		// NB: prevent instantiation of utility class.
	}

	/**
	 * Gets the file's absolute path with separators normalized to {@code /}, so
	 * that paths compare equal across platforms.
	 */
	public static String normalizeSeparators(final File file) {
		return normalizeSeparators(file.getAbsolutePath(), File.separator);
	}

	/** Replaces the given separator with {@code /} throughout the path. */
	public static String normalizeSeparators(final String path,
		final String separator)
	{
		return path.replaceAll(Pattern.quote(separator), "/");
	}

	/**
	 * Gets the file's extension: the part after the last dot of its name, with
	 * no leading dot, or the empty string if it has none.
	 */
	public static String extension(final File file) {
		final String name = file.getName();
		final int dot = name.lastIndexOf('.');
		return dot < 0 ? "" : name.substring(dot + 1);
	}

	/** Gets the extension of the file at the given path. */
	public static String extension(final String path) {
		return extension(new File(path));
	}

	/**
	 * Strips the version from a versioned file name - for example,
	 * {@code foo-1.2.3.jar} becomes {@code foo.jar}.
	 *
	 * @param filename the file name
	 * @return the file name without its version, or the file name unchanged if
	 *         it is not versioned
	 */
	public static String stripVersion(final String filename) {
		final Matcher matcher = VERSION_PATTERN.matcher(filename);
		if (!matcher.matches()) return filename;
		return matcher.group(1) + matcher.group(5);
	}

	/**
	 * Finds every version of the given file in the given directory - for
	 * example, given {@code foo-1.2.3.jar}, finds {@code foo-1.2.4.jar} too.
	 * Only versions with a matching classifier are returned.
	 *
	 * @param directory the directory to search
	 * @param filename the versioned file name
	 * @return the matching files, or {@code null} if the directory cannot be
	 *         listed, or the file name is unversioned and does not exist
	 */
	public static File[] allVersions(final File directory,
		final String filename)
	{
		final Matcher matcher = VERSION_PATTERN.matcher(filename);
		if (!matcher.matches()) {
			final File file = new File(directory, filename);
			return file.exists() ? new File[] { file } : null;
		}
		final String baseName = matcher.group(1);
		final String classifier = matcher.group(6);
		return directory.listFiles((dir, name) -> {
			if (!name.startsWith(baseName)) return false;
			final Matcher m = VERSION_PATTERN.matcher(name);
			if (!m.matches() || !baseName.equals(m.group(1))) return false;
			final String c = m.group(6);
			return classifier == null ? c == null : classifier.equals(c);
		});
	}

	/**
	 * Matches the given file name against the versioned file name pattern, so
	 * that callers can pick apart its base name, version and classifier.
	 */
	public static Matcher matchVersionedFilename(final String filename) {
		return VERSION_PATTERN.matcher(filename);
	}

	/**
	 * Deletes the given directory and everything in it.
	 *
	 * @param directory the directory to delete; {@code null} is a no-op
	 * @return true if the directory is gone afterward
	 */
	public static boolean deleteRecursively(final File directory) {
		if (directory == null) return true;
		final Path path = directory.toPath();
		if (!Files.exists(path)) return true;
		try (final Stream<Path> paths = Files.walk(path)) {
			// NB: deepest first, so that each directory is empty when deleted.
			return paths.sorted(Comparator.reverseOrder()) //
				.map(Path::toFile) //
				.allMatch(File::delete);
		}
		catch (final IOException exc) {
			return false;
		}
	}

	private static Pattern buildVersionPattern() {
		final String version =
			"\\d+(\\.\\d+|\\d{7})+[a-z]?\\d?(-[A-Za-z0-9.]+?|\\.GA)*?";
		final String suffix = "\\.jar(-[a-z]*)?";
		return Pattern.compile("(.+?)(-" + version + ")?((-(" + classifiers() +
			"))?(" + suffix + "))");
	}

	/** Helper method of {@link #buildVersionPattern()}. */
	private static String classifiers() {
		final String[] classifiers = { //
			"swing", //
			"swt", //
			"shaded", //
			"sources", //
			"javadoc", //
			"natives?-?\\w*", //
			"(natives-)?(android|linux|macosx|macos|solaris|windows)-" +
				"(aarch64|amd64|arm64|armv6hf|armv6|arm|" +
				"i386|i486|i586|i686|universal|x86[_-]32|x86[_-]64|x86)" //
		};
		return "(" + String.join("|", classifiers) + ")";
	}
}
