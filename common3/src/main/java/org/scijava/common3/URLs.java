/*-
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class URLs {

	private URLs() {
		// Prevent instantiation of static utility class
	}

	/**
	 * Recursively lists the contents of the referenced directory. Directories are
	 * excluded from the result. Supported protocols include {@code file} and
	 * {@code jar}.
	 *
	 * @param directory The directory whose contents should be listed.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 * @see #listContents(URL, boolean, boolean)
	 */
	public static Collection<URL> listContents(final URL directory) {
		return listContents(directory, true, true);
	}

	/**
	 * Lists all contents of the referenced directory. Supported protocols include
	 * {@code file} and {@code jar}.
	 *
	 * @param directory The directory whose contents should be listed.
	 * @param recurse Whether to list contents recursively, as opposed to only the
	 *          directory's direct contents.
	 * @param filesOnly Whether to exclude directories in the resulting collection
	 *          of contents.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 */
	public static Collection<URL> listContents(final URL directory,
		final boolean recurse, final boolean filesOnly)
	{
		return appendContents(new ArrayList<URL>(), directory, recurse, filesOnly);
	}

	/**
	 * Recursively adds contents from the referenced directory to an existing
	 * collection. Directories are excluded from the result. Supported protocols
	 * include {@code file} and {@code jar}.
	 *
	 * @param result The collection to which contents should be added.
	 * @param directory The directory whose contents should be listed.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 * @see #appendContents(Collection, URL, boolean, boolean)
	 */
	public static Collection<URL> appendContents(final Collection<URL> result,
		final URL directory)
	{
		return appendContents(result, directory, true, true);
	}

	/**
	 * Add contents from the referenced directory to an existing collection.
	 * Supported protocols include {@code file} and {@code jar}.
	 *
	 * @param result The collection to which contents should be added.
	 * @param directory The directory whose contents should be listed.
	 * @param recurse Whether to append contents recursively, as opposed to only
	 *          the directory's direct contents.
	 * @param filesOnly Whether to exclude directories in the resulting collection
	 *          of contents.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 */
	public static Collection<URL> appendContents(final Collection<URL> result,
		final URL directory, final boolean recurse, final boolean filesOnly)
	{
		if (directory == null) return result; // nothing to append
		final var protocol = directory.getProtocol();
		if (protocol.equals("file")) {
			final var dir = toFile(directory);
			final var list = dir.listFiles();
			if (list != null) {
				for (final var file : list) {
					try {
						if (!filesOnly || file.isFile()) {
							result.add(file.toURI().toURL());
						}
						if (recurse && file.isDirectory()) {
							appendContents(result, file.toURI().toURL(), recurse, filesOnly);
						}
					}
					catch (final MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else if (protocol.equals("jar")) {
			try {
				final var url = directory.toString();
				final var bang = url.indexOf("!/");
				if (bang < 0) return result;
				final var prefix = url.substring(bang + 2);
				final var baseURL = url.substring(0, bang + 2);

				final var connection = (JarURLConnection) new URL(baseURL)
					.openConnection();
				try (final var jar = connection.getJarFile()) {
					final var entries = jar.entries();
					while (entries.hasMoreElements()) {
						final var entry = entries.nextElement();
						final var urlEncoded = new URI(null, null, entry.getName(), null)
							.toString();
						if (urlEncoded.length() > prefix.length() && // omit directory
																													// itself
							urlEncoded.startsWith(prefix))
						{
							if (filesOnly && urlEncoded.endsWith("/")) {
								// URL is directory; exclude it
								continue;
							}
							if (!recurse) {
								// check whether this URL is a *direct* child of the directory
								final var slash = urlEncoded.indexOf("/", prefix.length());
								if (slash >= 0 && slash != urlEncoded.length() - 1) {
									// not a direct child
									continue;
								}
							}
							result.add(new URL(baseURL + urlEncoded));
						}
					}
				}
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
			catch (final URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return result;
	}

	/**
	 * Converts the given {@link URL} to its corresponding {@link File}.
	 * <p>
	 * This method is similar to calling {@code new File(url.toURI())} except that
	 * it also handles "jar:file:" URLs, returning the path to the JAR file.
	 * </p>
	 *
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	public static File toFile(final URL url) {
		return url == null ? null : toFile(url.toString());
	}

	/**
	 * Converts the given URL string to its corresponding {@link File}.
	 *
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	public static File toFile(final String url) {
        var path = url;
		if (path.startsWith("jar:")) {
			// remove "jar:" prefix and "!/" suffix
			final var index = path.indexOf("!/");
			path = path.substring(4, index);
		}
		try {
			if (Platforms.isWindows() && path.matches("file:[A-Za-z]:.*")) {
				path = "file:/" + path.substring(5);
			}
			return new File(new URL(path).toURI());
		}
		catch (final MalformedURLException | URISyntaxException e) {
			// NB: empty catch block
			// This is OK if it's a file. Otherwise we'll throw IAE below
		}
		// If it's a File we can still try to construct it
		if (path.startsWith("file:")) {
			// pass through the URL as-is, minus "file:" prefix
			path = path.substring(5);
			return new File(path);
		}
		throw new IllegalArgumentException("Invalid URL: " + url);
	}
}
