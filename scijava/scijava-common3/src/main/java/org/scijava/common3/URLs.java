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
		final String protocol = directory.getProtocol();
		if (protocol.equals("file")) {
			final File dir = toFile(directory);
			final File[] list = dir.listFiles();
			if (list != null) {
				for (final File file : list) {
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
				final String url = directory.toString();
				final int bang = url.indexOf("!/");
				if (bang < 0) return result;
				final String prefix = url.substring(bang + 2);
				final String baseURL = url.substring(0, bang + 2);

				final JarURLConnection connection =
					(JarURLConnection) new URL(baseURL).openConnection();
				try (final JarFile jar = connection.getJarFile()) {
					final Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						final JarEntry entry = entries.nextElement();
						final String urlEncoded =
							new URI(null, null, entry.getName(), null).toString();
						if (urlEncoded.length() > prefix.length() && // omit directory itself
							urlEncoded.startsWith(prefix))
						{
							if (filesOnly && urlEncoded.endsWith("/")) {
								// URL is directory; exclude it
								continue;
							}
							if (!recurse) {
								// check whether this URL is a *direct* child of the directory
								final int slash = urlEncoded.indexOf("/", prefix.length());
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
		String path = url;
		if (path.startsWith("jar:")) {
			// remove "jar:" prefix and "!/" suffix
			final int index = path.indexOf("!/");
			path = path.substring(4, index);
		}
		try {
			if (Platforms.isWindows() && path.matches("file:[A-Za-z]:.*")) {
				path = "file:/" + path.substring(5);
			}
			return new File(new URL(path).toURI());
		}
		catch (final MalformedURLException e) {
			// NB: URL is not completely well-formed.
		}
		catch (final URISyntaxException e) {
			// NB: URL is not completely well-formed.
		}
		if (path.startsWith("file:")) {
			// pass through the URL as-is, minus "file:" prefix
			path = path.substring(5);
			return new File(path);
		}
		throw new IllegalArgumentException("Invalid URL: " + url);
	}
}
