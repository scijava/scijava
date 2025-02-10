/*
 * #%L
 * Utility functions to introspect metadata of SciJava libraries.
 * %%
 * Copyright (C) 2022 - 2025 SciJava developers.
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Attributes;

import org.scijava.common3.Classes;
import org.scijava.common3.Versioned;

/**
 * Helper class for working with JAR manifests.
 *
 * @author Curtis Rueden
 */
public class Manifest implements Versioned {

	/** The JAR manifest backing this object. */
	private final java.util.jar.Manifest manifest;

	/** Creates a new instance wrapping the given JAR manifest. */
	private Manifest(final java.util.jar.Manifest manifest) {
		this.manifest = manifest;
	}

	// -- Manifest methods --

	public String archiverVersion() {
		return get("Archiver-Version");
	}

	public String buildJdk() {
		return get("Build-Jdk");
	}

	public String builtBy() {
		return get("Built-By");
	}

	public String createdBy() {
		return get("Created-By");
	}

	public String implementationBuild() {
		return get("Implementation-Build");
	}

	public String implementationDate() {
		return get("Implementation-Date");
	}

	public String implementationTitle() {
		return get("Implementation-Title");
	}

	public String implementationVendor() {
		return get("Implementation-Vendor");
	}

	public String implementationVendorId() {
		return get("Implementation-Vendor-Id");
	}

	public String implementationVersion() {
		return get("Implementation-Version");
	}

	public String manifestVersion() {
		return get("Manifest-Version");
	}

	public String package_() {
		return get("Package");
	}

	public String specificationTitle() {
		return get("Specification-Title");
	}

	public String specificationVendor() {
		return get("Specification-Vendor");
	}

	public String specificationVersion() {
		return get("Specification-Version");
	}

	public String get(final String key) {
		if (manifest == null) return null;
		final var mainAttrs = manifest.getMainAttributes();
		if (mainAttrs == null) return null;
		return mainAttrs.getValue(key);
	}

	public Map<Object, Object> getAll() {
		if (manifest == null) return null;
		final var mainAttrs = manifest.getMainAttributes();
		if (mainAttrs == null) return null;
		return Collections.unmodifiableMap(mainAttrs);
	}

	// -- Utility methods --

	/** Gets the JAR manifest associated with the given class. */
	public static Manifest manifest(final Class<?> c) {
		try {
			return manifest(new URL("jar:" + Classes.location(c) + "!/"));
		}
		catch (final IOException e) {
			return null;
		}
	}

	/**
	 * Gets the JAR manifest associated with the given XML document. Assumes the
	 * XML document was loaded as a resource from inside a JAR.
	 */
	public static Manifest manifest(final XML xml) throws IOException {
		final var path = xml.path();
		if (path == null || !path.startsWith("file:")) return null;
		final var dotJAR = path.indexOf(".jar!/");
		return manifest(new File(path.substring(5, dotJAR + 4)));
	}

	/** Gets the JAR manifest associated with the given JAR file. */
	public static Manifest manifest(final File jarFile) throws IOException {
		if (!jarFile.exists()) throw new FileNotFoundException();
		return manifest(new URL("jar:file:" + jarFile.getAbsolutePath() + "!/"));
	}

	private static Manifest manifest(final URL jarURL) throws IOException {
		final var conn = (JarURLConnection) jarURL.openConnection();
		return new Manifest(conn.getManifest());
	}

	// -- Versioned methods --

	@Override
	public String version() {
		final var v = baseVersion();
		if (v == null || !v.endsWith("-SNAPSHOT")) return v;

		// append commit hash to differentiate between development versions
		final var buildNumber = implementationBuild();
		return buildNumber == null ? v : v + "-" + buildNumber;
	}

	// -- Helper methods --

	private String baseVersion() {
		final var manifestVersion = implementationVersion();
		if (manifestVersion != null) return manifestVersion;
		return specificationVersion();
	}
}
