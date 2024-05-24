/*
 * #%L
 * Utility functions to introspect metadata of SciJava libraries.
 * %%
 * Copyright (C) 2022 - 2024 SciJava developers.
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

package org.scijava.meta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.scijava.common3.Apps;
import org.scijava.common3.Classes;
import org.scijava.common3.URLs;
import org.scijava.common3.Versioned;

/**
 * Helper class for working with Maven POMs.
 *
 * @author Curtis Rueden
 */
public class POM extends XML implements Comparable<POM>, Versioned {

	private String version;

	/** Parses a POM from the given file. */
	public POM(final File file) throws IOException {
		super(file);
	}

	/** Parses a POM from the given URL. */
	public POM(final URL url) throws IOException {
		super(url);
	}

	/** Parses a POM from the given input stream. */
	public POM(final InputStream in) throws IOException {
		super(in);
	}

	/** Parses a POM from the given string. */
	public POM(final String s) throws IOException {
		super(s);
	}

	// -- POM methods --

	/** Gets the POM's parent groupId. */
	public String parentGroupId() {
		return cdata("//project/parent/groupId");
	}

	/** Gets the POM's parent artifactId. */
	public String parentArtifactId() {
		return cdata("//project/parent/artifactId");
	}

	/** Gets the POM's parent artifactId. */
	public String parentVersion() {
		return cdata("//project/parent/version");
	}

	/** Gets the POM's groupId. */
	public String groupId() {
		final String groupId = cdata("//project/groupId");
		if (groupId != null) return groupId;
		return parentGroupId();
	}

	/** Gets the POM's artifactId. */
	public String artifactId() {
		return cdata("//project/artifactId");
	}

	/** Gets the project name. */
	public String projectName() {
		return cdata("//project/name");
	}

	/** Gets the project description. */
	public String projectDescription() {
		return cdata("//project/description");
	}

	/** Gets the project URL. */
	public String projectURL() {
		return cdata("//project/url");
	}

	/** Gets the project inception year. */
	public String projectInceptionYear() {
		return cdata("//project/inceptionYear");
	}

	/** Gets the organization name. */
	public String organizationName() {
		return cdata("//project/organization/name");
	}

	/** Gets the organization URL. */
	public String organizationURL() {
		return cdata("//project/organization/url");
	}

	/** Gets the SCM connection string. */
	public String scmConnection() {
		return cdata("//project/scm/connection");
	}

	/** Gets the SCM developerConnection string. */
	public String scmDeveloperConnection() {
		return cdata("//project/scm/developerConnection");
	}

	/** Gets the SCM tag. */
	public String scmTag() {
		return cdata("//project/scm/tag");
	}

	/** Gets the SCM URL. */
	public String scmURL() {
		return cdata("//project/scm/url");
	}

	/** Gets the issue management system. */
	public String issueManagementSystem() {
		return cdata("//project/issueManagement/system");
	}

	/** Gets the issue management URL. */
	public String issueManagementURL() {
		return cdata("//project/issueManagement/url");
	}

	/** Gets the CI management system. */
	public String ciManagementSystem() {
		return cdata("//project/ciManagement/system");
	}

	/** Gets the CI management URL. */
	public String ciManagementURL() {
		return cdata("//project/ciManagement/url");
	}

	// -- Comparable methods --

	private static final Comparator<String> STRING_COMPARATOR = //
		Comparator.nullsFirst(String::compareTo);
	private static final Comparator<POM> POM_COMPARATOR = Comparator//
		// sort by groupId first
		.comparing(POM::groupId, STRING_COMPARATOR)
		// sort by artifactId second
		.thenComparing(POM::artifactId, STRING_COMPARATOR)//
		// finally, sort by version
		.thenComparing(POM::version, POM::compareVersions);

	@Override
	public int compareTo(final POM pom) {
		return POM_COMPARATOR.compare(this, pom);
	}

	// -- Versioned methods --

	/** Gets the POM's version. */
	@Override
	public String version() {
		if (version == null) {
			synchronized (this) {
				if (version == null) {
					version = cdata("//project/version");
					if (version == null) version = parentVersion();
				}
			}
		}
		return version;
	}

	// -- Utility methods --

	/**
	 * Gets the Maven POM associated with the given class.
	 *
	 * @param c The class to use as a base when searching for a pom.xml.
	 * @return {@link POM} object representing the discovered POM, or null if no
	 *         POM could be found.
	 */
	public static POM pom(final Class<?> c) {
		return pom(c, null, null);
	}

	/**
	 * internal cache used for calls to {@link #pom(Class, String, String)}
	 */
	private static final Map<URL, POM> POMS = new HashMap<>();

	/**
	 * Gets the Maven POM associated with the given class.
	 *
	 * @param c The class to use as a base when searching for a pom.xml.
	 * @param groupId The Maven groupId of the desired POM.
	 * @param artifactId The Maven artifactId of the desired POM.
	 * @return {@link POM} object representing the discovered POM, or null if no
	 *         POM could be found.
	 */
	public static POM pom(final Class<?> c, final String groupId,
												final String artifactId)
	{
		final URL location = Classes.location(c);
		return POMS.computeIfAbsent( //
			location, //
			l -> findPOM(l, groupId, artifactId) //
		);
	}

	/**
	 * Gets the Maven POM associated with the given class, always returning a new
	 * {@link POM} object.
	 *
	 * @param location The {@link URL} to use as a base when searching for a
	 *          pom.xml.
	 * @param groupId The Maven groupId of the desired POM.
	 * @param artifactId The Maven artifactId of the desired POM.
	 * @return {@link POM} object representing the discovered POM, or null if no
	 *         POM could be found.
	 * @see #pom(Class, String, String), which does the same thing, but with an
	 *      internal cache that makes repeated calls trivial
	 */
	private static POM findPOM(final URL location, final String groupId,
		final String artifactId)
	{
		try {
			if (!location.getProtocol().equals("file") || location.toString()
				.endsWith(".jar"))
			{
				// look for pom.xml in JAR's META-INF/maven subdirectory
				if (groupId == null || artifactId == null) {
					// groupId and/or artifactId is unknown; scan for the POM
					final URL pomBase = new URL("jar:" + //
						location + "!/META-INF/maven");
					for (final URL url : URLs.listContents(pomBase, true, true)) {
						if (url.toExternalForm().endsWith("/pom.xml")) {
							return new POM(url);
						}
					}
				}
				else {
					// known groupId and artifactId; grab it directly
					final String pomPath = "META-INF/maven/" + groupId + "/" +
						artifactId + "/pom.xml";
					final URL pomURL = new URL("jar:" + location.toString() + "!/" +
						pomPath);
					return new POM(pomURL);
				}
			}
			// look for the POM in the class's base directory
			final File file = URLs.toFile(location);
			final File baseDir = Apps.baseDirectory(file, null);
			final File pomFile = new File(baseDir, "pom.xml");
			return new POM(pomFile);
		}
		catch (final IOException e) {
			return null;
		}
	}

	/** Gets all available Maven POMs on the class path. */
	public static List<POM> allPOMs() {
		// find all META-INF/maven/ folders on the classpath
		final String pomPrefix = "META-INF/maven/";
		final ClassLoader classLoader = Classes.classLoader();
		final Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(pomPrefix);
		}
		catch (final IOException exc) {
			return null;
		}

		final ArrayList<POM> poms = new ArrayList<>();

		// recursively list contents of META-INF/maven/ directories
		while (resources.hasMoreElements()) {
			final URL resource = resources.nextElement();
			for (final URL url : URLs.listContents(resource)) {
				// look for pom.xml files amongst the contents
				if (url.getPath().endsWith("/pom.xml")) {
					try {
						poms.add(new POM(url));
					}
					catch (final IOException exc) {
						// NB: empty catch block
						// ignore and continue
					}
				}
			}
		}

		return poms;
	}

	/**
	 * Compares two version strings.
	 * <p>
	 * Given the variation between versioning styles, there is no single
	 * comparison method that can possibly be correct 100% of the time. So this
	 * method works on a "best effort" basis; YMMV.
	 * </p>
	 * <p>
	 * The algorithm is as follows:
	 * </p>
	 * <ul>
	 * <li>Split on non-alphameric characters.</li>
	 * <li>Compare each token one by one.</li>
	 * <li>Comparison is numerical when possible (i.e., when an integer can be
	 * parsed from the token), and lexicographic otherwise.</li>
	 * <li>If one version string runs out of tokens, the version with additional
	 * tokens remaining is considered <em>greater than</em> the version without
	 * additional tokens.</li>
	 * <li>There is one exception: if two version strings are identical except
	 * that one has a suffix beginning with a dash ({@code -}), the version with
	 * suffix will be considered <em>less than</em> the one without a suffix. The
	 * reason for this is to accommodate the
	 * <a href="http://semver.org/">SemVer</a> versioning scheme's usage of
	 * "prerelease" version suffixes. For example, {@code 2.0.0} will compare
	 * greater than {@code 2.0.0-beta-1}, whereas {@code 2.0.0} will compare less
	 * than {@code 2.0.0.1}.</li>
	 * </ul>
	 *
	 * @return a negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 * @see Comparator#compare(Object, Object)
	 */
	public static int compareVersions(final String v1, final String v2) {
		final String[] t1 = v1.split("[^\\w]");
		final String[] t2 = v2.split("[^\\w]");
		final int size = Math.min(t1.length, t2.length);
		for (int i = 0; i < size; i++) {
			try {
				final long n1 = Long.parseLong(t1[i]);
				final long n2 = Long.parseLong(t2[i]);
				if (n1 < n2) return -1;
				if (n1 > n2) return 1;
			}
			catch (final NumberFormatException exc) {
				final int result = t1[i].compareTo(t2[i]);
				if (result != 0) return result;
			}
		}
		if (t1.length == t2.length) return 0; // versions match

		// check for SemVer prerelease versions
		if (v1.startsWith(v2) && v1.charAt(v2.length()) == '-') {
			// v1 is a prerelease version of v2
			return -1;
		}
		if (v2.startsWith(v1) && v2.charAt(v1.length()) == '-') {
			// v2 is a prerelease version of v1
			return 1;
		}

		return t1.length < t2.length ? -1 : 1;
	}

}
