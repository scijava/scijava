/*-
 * #%L
 * Core API for SciJava code intelligence features.
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

package org.scijava.code.api;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A lazily-built index of the fully qualified class names available on the
 * classpath, together with documentation URLs derived from each containing jar's
 * {@code pom.xml} and the <a href="https://javadoc.scijava.org/">SciJava javadoc
 * aggregator</a>.
 * <p>
 * This is the toolkit-agnostic engine behind import and class-name completion:
 * given a partial name it can enumerate matching classes, locate documentation,
 * and build HTML summaries for fields, methods and constructors. It depends on no
 * widget toolkit, so completers in language adapters (e.g. Jython) can use it
 * without pulling in the Swing script editor.
 * </p>
 * <p>
 * By default the index scans the runtime classpath ({@code java.class.path}), the
 * JRE/JDK home, and (on Java 9+) the runtime image's own modules. Applications that load classes from elsewhere (e.g. an ImageJ
 * {@code jars/} directory) can contribute additional roots via
 * {@link #addRoots(String...)} <em>before</em> the index is first built; this
 * keeps application-specific paths out of this neutral utility.
 * </p>
 *
 * @author Albert Cardona
 * @author Tiago Ferreira
 * @author Curtis Rueden
 */
public final class ClassIndex {

	/** Base URL of the SciJava javadoc aggregator (with trailing slash). */
	public static final String SCIJAVA_JAVADOC_URL =
		"https://javadoc.scijava.org/";

	/** Class name &rarr; properties of its containing jar (URLs, name). */
	private static final Map<String, JarProperties> class_urls = new HashMap<>();

	/** Package name &rarr; properties of a containing jar. */
	private static final Map<String, JarProperties> package_urls =
		new HashMap<>();

	/** Cache of sub-URLs of the SciJava javadoc aggregator. */
	private static final HashMap<String, String> scijava_javadoc_URLs =
		new HashMap<>();

	/** Extra roots (jar files or directories) contributed by applications. */
	private static final Set<String> extraRoots = new LinkedHashSet<>();

	private static boolean ready = false;

	private ClassIndex() {
		// prevent instantiation of static utility class
	}

	/**
	 * Registers additional roots (jar files, or directories scanned recursively
	 * for jars) to include when the index is built. Has no effect once the index
	 * has been built.
	 */
	public static void addRoots(final String... roots) {
		synchronized (class_urls) {
			Collections.addAll(extraRoots, roots);
		}
	}

	/** The default roots: the JRE/JDK home plus each classpath entry. */
	private static List<String> defaultRoots() {
		final List<String> roots = new ArrayList<>();
		roots.add(System.getProperty("java.home"));
		final String cp = System.getProperty("java.class.path");
		if (cp != null) {
			for (final String entry : cp.split(File.pathSeparator)) {
				if (entry != null && !entry.isEmpty()) roots.add(entry);
			}
		}
		return roots;
	}

	/** Builds the class/package index if it has not been built yet. */
	public static void ensureCache() {
		synchronized (class_urls) {
			if (!class_urls.isEmpty()) return;
			final List<String> roots = defaultRoots();
			roots.addAll(extraRoots);
			class_urls.putAll(findAllClasses(roots));
			findRuntimeClasses(class_urls);
			// Soft attempt at getting all packages (will get them wrong if
			// multiple jars share the same packages).
			for (final Map.Entry<String, JarProperties> entry : class_urls
				.entrySet())
			{
				final int idot = entry.getKey().lastIndexOf('.');
				if (-1 == idot) continue; // no package
				final String package_name = entry.getKey().substring(0, idot);
				if (package_urls.containsKey(package_name)) continue;
				package_urls.put(package_name, entry.getValue());
			}
			ready = true;
		}
	}

	/** Whether {@link #ensureCache()} has finished building the index. */
	public static boolean isCacheReady() {
		return ready;
	}

	/** Builds the cache of SciJava javadoc aggregator sub-URLs, if not done. */
	public static void ensureSciJavaSubURLCache() {
		synchronized (scijava_javadoc_URLs) {
			if (!scijava_javadoc_URLs.isEmpty()) return;
			Scanner scanner = null;
			try {
				final Pattern pattern = Pattern.compile(
					"<div class=\"jdbox\"><div><a href=\"(.*?)\">");
				final URLConnection connection =
					new URL(SCIJAVA_JAVADOC_URL).openConnection();
				scanner = new Scanner(connection.getInputStream());
				while (scanner.hasNext()) {
					final Matcher matcher = pattern.matcher(scanner.nextLine());
					if (matcher.find()) {
						String name = matcher.group(1).toLowerCase();
						if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
						scijava_javadoc_URLs.put(name, SCIJAVA_JAVADOC_URL + matcher
							.group(1));
					}
				}
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
			finally {
				if (null != scanner) scanner.close();
			}
		}
	}

	public static HashMap<String, JarProperties> findClassDocumentationURLs(
		final String s)
	{
		ensureCache();
		final HashMap<String, JarProperties> matches = new HashMap<>();
		for (final Map.Entry<String, JarProperties> entry : class_urls
			.entrySet())
		{
			if (entry.getKey().contains(s)) {
				final JarProperties props = entry.getValue();
				matches.put(entry.getKey(), new JarProperties(props.name,
					new ArrayList<>(props.urls)));
			}
		}
		return matches;
	}

	public static HashMap<String, ArrayList<String>> findDocumentationForClass(
		final String s)
	{
		final HashMap<String, JarProperties> matches = findClassDocumentationURLs(
			s);
		ensureSciJavaSubURLCache();

		final Pattern javaPackages = Pattern.compile(
			"^(java|javax|org\\.omg|org\\.w3c|org\\.xml|org\\.ietf\\.jgss)\\..*$");
		final String version = System.getProperty("java.version");
		final String majorVersion = version.startsWith("1.") //
			? version.substring(2, version.indexOf('.', 2)) //
			: version.substring(0, version.indexOf('.'));
		final String javaDoc = "java" + majorVersion;

		final HashMap<String, ArrayList<String>> result = new HashMap<>();

		for (final Map.Entry<String, JarProperties> entry : matches.entrySet()) {
			final String classname = entry.getKey();
			final ArrayList<String> urls = new ArrayList<>();
			result.put(classname, urls);
			if (javaPackages.matcher(classname).matches()) {
				urls.add(scijava_javadoc_URLs.get(javaDoc) + classname.replace('.',
					'/') + ".html");
			}
			else {
				final JarProperties props = entry.getValue();
				// Find the first URL with git in it.
				for (final String url : props.urls) {
					final boolean github = url.contains("/github.com"), gitlab = url
						.contains("/gitlab.com");
					if (github || gitlab) {
						// Find the 5th slash, e.g. https://github.com/imglib/imglib2/
						int count = 0;
						int last = 0;
						while (count < 5) {
							last = url.indexOf('/', last + 1);
							if (-1 == last) break; // less than 5 found
							++count;
						}
						String urlbase = url;
						if (5 == count) urlbase = url.substring(0, last); // no trailing /
						// Assume maven, since these URLs came from a pom.xml.
						urls.add(urlbase + (gitlab ? "/-" : "") +
							"/blob/master/src/main/java/" + classname.replace('.', '/') +
							".java");
						break;
					}
				}
				// Try to find a javadoc in the scijava website.
				if (null != props.name) {
					String scijava_javadoc_url = scijava_javadoc_URLs.get(props.name
						.toLowerCase());
					if (null == scijava_javadoc_url) {
						// Try cropping name at the first whitespace if any (e.g.
						// "ImgLib2 Core Library" to "ImgLib2").
						for (final String word : props.name.split(" ")) {
							scijava_javadoc_url = scijava_javadoc_URLs.get(word
								.toLowerCase());
							if (null != scijava_javadoc_url) break; // found a valid one
						}
					}
					if (null != scijava_javadoc_url) {
						urls.add(scijava_javadoc_url + classname.replace('.', '/') +
							".html");
					}
					else {
						// Try Fiji: could be a plugin.
						Scanner scanner = null;
						try {
							final String url = SCIJAVA_JAVADOC_URL + "Fiji/" + classname
								.replace('.', '/') + ".html";
							final URLConnection c = new URL(url).openConnection();
							scanner = new Scanner(c.getInputStream());
							while (scanner.hasNext()) {
								final String line = scanner.nextLine();
								if (line.contains("<title>")) {
									if (!line.contains("<title>404")) urls.add(url);
									break;
								}
							}
						}
						catch (final Exception e) {
							// Ignore: 404 that wasn't redirected to an error page.
						}
						finally {
							if (null != scanner) scanner.close();
						}
					}
				}
			}
		}

		return result;
	}

	/** Properties of a jar file: its display name and documentation URLs. */
	public static final class JarProperties {

		public final ArrayList<String> urls;
		public String name;

		public JarProperties(final String name, final ArrayList<String> urls) {
			this.name = name;
			this.urls = urls;
		}
	}

	/**
	 * Indexes the classes of the Java runtime image itself. Since Java 9, JDK
	 * classes live in the {@code jrt:} module image rather than in jars, so a jar
	 * scan of the JDK home misses them. On Java 8 this does nothing: the runtime
	 * classes are in {@code rt.jar}, which the jar scan already finds.
	 */
	private static void findRuntimeClasses(
		final Map<String, JarProperties> result)
	{
		final FileSystem jrt;
		try {
			jrt = FileSystems.getFileSystem(URI.create("jrt:/"));
		}
		catch (final RuntimeException exc) {
			return; // no jrt filesystem (Java 8)
		}
		final JarProperties props = new JarProperties("Java Runtime",
			new ArrayList<>());
		final Path modules = jrt.getPath("/modules");
		try (final Stream<Path> paths = Files.walk(modules)) {
			paths.forEach(path -> {
				// Paths look like: /modules/<module>/<package path>/<Class>.class
				if (path.getNameCount() < 3) return;
				final String entry = path.subpath(2, path.getNameCount()).toString();
				if (!entry.endsWith(".class") || entry.endsWith("module-info.class") ||
					entry.endsWith("package-info.class")) return;
				final String classname = className(entry.replace(
					path.getFileSystem().getSeparator(), "/"));
				if (isInternalRuntimeClass(classname)) return;
				result.putIfAbsent(classname, props);
			});
		}
		catch (final IOException | RuntimeException exc) {
			// NB: Best effort; the index simply lacks the runtime classes.
		}
	}

	/** Whether the given JDK class is an implementation detail, not API. */
	private static boolean isInternalRuntimeClass(final String classname) {
		return classname.startsWith("sun.") || classname.startsWith("jdk.internal.") ||
			classname.startsWith("com.sun.") && classname.contains(".internal.");
	}

	/**
	 * Converts a class file entry (e.g. {@code java/util/Map$Entry.class}) into
	 * a class name, truncating nested classes to their outermost class.
	 */
	private static String className(final String entry) {
		final String classname = entry.replace('/', '.');
		final int idollar = classname.indexOf('$');
		return -1 == idollar ? classname.substring(0, classname.length() - 6) //
			: classname.substring(0, idollar);
	}

	/**
	 * Scans the given roots for jar files and indexes every class they contain,
	 * recording each jar's {@code pom.xml} URLs and name where present.
	 *
	 * @param roots jar files, or directories scanned recursively for jars
	 */
	public static HashMap<String, JarProperties> findAllClasses(
		final List<String> roots)
	{
		// Find all jar files.
		final ArrayList<String> jarFilePaths = new ArrayList<>();
		final LinkedList<String> dirs = new LinkedList<>(roots);
		final HashSet<String> seenDirs = new HashSet<>();
		while (!dirs.isEmpty()) {
			final String filepath = dirs.removeFirst();
			if (null == filepath) continue;
			final File file = new File(filepath);
			if (file.isFile() && filepath.endsWith(".jar")) {
				jarFilePaths.add(file.getAbsolutePath());
				continue;
			}
			seenDirs.add(file.getAbsolutePath());
			if (file.exists() && file.isDirectory()) {
				final File[] children = file.listFiles();
				if (children == null) continue;
				for (final File child : children) {
					final String childfilepath = child.getAbsolutePath();
					if (seenDirs.contains(childfilepath)) continue;
					if (child.isDirectory()) dirs.add(childfilepath);
					else if (childfilepath.endsWith(".jar")) jarFilePaths.add(
						childfilepath);
				}
			}
		}
		// Find all classes from all jar files.
		final HashMap<String, JarProperties> result = new HashMap<>();
		final Pattern urlpattern = Pattern.compile(">(http.*?)<");
		final Pattern namepattern = Pattern.compile("<name>(.*?)<");
		for (final String jarpath : jarFilePaths) {
			JarFile jar = null;
			try {
				jar = new JarFile(jarpath);
				final Enumeration<JarEntry> entries = jar.entries();
				final ArrayList<String> urls = new ArrayList<>();
				final JarProperties props = new JarProperties(null, urls);
				// For every filepath in the jar zip archive.
				while (entries.hasMoreElements()) {
					final JarEntry entry = entries.nextElement();
					if (entry.isDirectory()) continue;
					if (entry.getName().endsWith(".class")) {
						result.put(className(entry.getName()), props);
					}
					else if (entry.getName().endsWith("/pom.xml")) {
						final Scanner scanner = new Scanner(jar.getInputStream(entry));
						while (scanner.hasNext()) {
							final String line = scanner.nextLine();
							final Matcher matcher1 = urlpattern.matcher(line);
							if (matcher1.find()) urls.add(matcher1.group(1));
							if (null == props.name) {
								final Matcher matcher2 = namepattern.matcher(line);
								if (matcher2.find()) props.name = matcher2.group(1);
							}
						}
						scanner.close();
					}
				}
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
			finally {
				if (null != jar) try {
					jar.close();
				}
				catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static Stream<String> findPackageNamesStartingWith(
		final String text)
	{
		ensureCache();
		return package_urls.keySet().stream().filter(s -> s.startsWith(text));
	}

	public static Stream<String> findClassNamesForPackage(
		final String packageName)
	{
		ensureCache();
		if (null == packageName || packageName.length() == 0) //
			return class_urls.keySet().stream();
		return class_urls.keySet().stream().filter(s -> s.startsWith(packageName) &&
			-1 == s.indexOf('.', packageName.length() + 2));
	}

	/**
	 * @param text A left-justified substring of a fully qualified class name,
	 *          with the package.
	 */
	public static Stream<String> findClassNamesStartingWith(final String text) {
		ensureCache();
		if (text.length() == 0) return class_urls.keySet().stream();
		return class_urls.keySet().stream().filter(s -> s.startsWith(text));
	}

	/**
	 * @param text A substring of a class's fully qualified name.
	 */
	public static Stream<String> findClassNamesContaining(final String text) {
		ensureCache();
		return class_urls.keySet().stream().filter(s -> s.contains(text));
	}

	/**
	 * Finds simple class names starting with {@code text}, returning the fully
	 * qualified class names.
	 */
	public static ArrayList<String> findSimpleClassNamesStartingWith(
		final String text)
	{
		ensureCache();
		final ArrayList<String> matches = new ArrayList<>();
		if (0 == text.length()) return matches;
		for (final String classname : class_urls.keySet()) {
			final int idot = classname.lastIndexOf('.');
			final String simplename = -1 == idot ? classname : classname.substring(
				idot + 1);
			if (simplename.startsWith(text)) matches.add(classname);
		}
		return matches;
	}

	/** Builds an HTML link to the SciJava javadoc for the given class. */
	public static String getJavaDocLink(final Class<?> c) {
		final String name = c.getCanonicalName();
		final String pkg = getDocPackage(name);
		if (pkg == null) return name;
		final String url = String.format("%s%s/index.html?%s.html",
			SCIJAVA_JAVADOC_URL, pkg, name.replace(".", "/"));
		return String.format("<a href='%s';>%s</a>", url, name);
	}

	private static String getDocPackage(final String classCanonicalName) {
		// TODO: Do this programmatically.
		if (classCanonicalName.startsWith("ij.")) return "ImageJ1";
		else if (classCanonicalName.startsWith("sc.fiji")) return "Fiji";
		else if (classCanonicalName.startsWith("net.imagej")) return "ImageJ";
		else if (classCanonicalName.startsWith("net.imglib2")) return "ImgLib2";
		else if (classCanonicalName.startsWith("org.scijava")) return "SciJava";
		else if (classCanonicalName.startsWith("loci.formats")) return "Bio-Formats";
		if (classCanonicalName.startsWith("java.")) return "Java8";
		else if (classCanonicalName.startsWith("sc.iview")) return "SciView";
		else if (classCanonicalName.startsWith("weka.")) return "Weka";
		else if (classCanonicalName.startsWith("inra.ijpb")) return "MorphoLibJ";
		return null;
	}

	/**
	 * Assembles an HTML-formatted auto-completion summary with functional
	 * hyperlinks for the given field.
	 */
	public static String getSummaryCompletion(final Field field,
		final Class<?> c)
	{
		final StringBuffer summary = new StringBuffer();
		summary.append("<b>").append(field.getName()).append("</b>");
		summary.append(" (").append(field.getType().getName()).append(")");
		summary.append("<DL>");
		summary.append("<DT><b>Defined in:</b>");
		summary.append("<DD>").append(getJavaDocLink(c));
		summary.append("</DL>");
		return summary.toString();
	}

	/**
	 * Assembles an HTML-formatted auto-completion summary with functional
	 * hyperlinks for the given method.
	 */
	public static String getSummaryCompletion(final Method method,
		final Class<?> c)
	{
		final StringBuffer summary = new StringBuffer();
		final StringBuffer replacementHeader = new StringBuffer(method.getName());
		final int bIndex = replacementHeader.length(); // remember '(' position
		replacementHeader.append("(");
		final Parameter[] params = method.getParameters();
		if (params.length > 0) {
			for (final Parameter parameter : params) {
				replacementHeader.append(parameter.getType().getSimpleName()).append(
					" ").append(parameter.getName()).append(", ");
			}
			replacementHeader.setLength(replacementHeader.length() - 2); // trailing
		}
		replacementHeader.append(")");
		replacementHeader.replace(bIndex, bIndex + 1, "</b>(");
		summary.append("<b>").append(replacementHeader);
		summary.append("<DL>");
		summary.append("<DT><b>Returns:</b>");
		summary.append("<DD>").append(method.getReturnType().getSimpleName());
		summary.append("<DT><b>Defined in:</b>");
		summary.append("<DD>").append(getJavaDocLink(c));
		summary.append("</DL>");
		return summary.toString();
	}

	/**
	 * Assembles an HTML-formatted auto-completion summary with functional
	 * hyperlinks for the given constructor.
	 */
	public static String getSummaryCompletion(final Constructor<?> constructor,
		final Class<?> c)
	{
		final StringBuffer summary = new StringBuffer();
		final StringBuffer replacementHeader = new StringBuffer(c.getSimpleName());
		final int bIndex = replacementHeader.length(); // remember '(' position
		replacementHeader.append("(");
		final Parameter[] params = constructor.getParameters();
		if (params.length > 0) {
			for (final Parameter parameter : params) {
				replacementHeader.append(parameter.getType().getSimpleName()).append(
					" ").append(parameter.getName()).append(", ");
			}
			replacementHeader.setLength(replacementHeader.length() - 2); // trailing
		}
		replacementHeader.append(")");
		replacementHeader.replace(bIndex, bIndex + 1, "</b>(");
		summary.append("<b>").append(replacementHeader);
		summary.append("<DL>");
		summary.append("<DT><b>Intantiates:</b>");
		summary.append("<DD>").append(c.getSimpleName());
		summary.append("<DT><b>Defined in:</b>");
		summary.append("<DD>").append(getJavaDocLink(c));
		summary.append("</DL>");
		return summary.toString();
	}
}
