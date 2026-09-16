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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FilePaths}.
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class FilePathsTest {

	@Test
	public void testNormalizeSeparators() {
		// Windows-style paths get standardized.
		assertEquals("C:/path/to/my-windows-file", //
			FilePaths.normalizeSeparators("C:\\path\\to\\my-windows-file", "\\"));
		// *nix-style paths are unchanged.
		assertEquals("/path/to/my-nix-file", //
			FilePaths.normalizeSeparators("/path/to/my-nix-file", "/"));
		// An already-standardized path stays good on Windows.
		assertEquals("/path/to/my-nix-file", //
			FilePaths.normalizeSeparators("/path/to/my-nix-file", "\\"));
	}

	@Test
	public void testExtension() {
		assertEquals("ext", FilePaths.extension("/path/to/file.ext"));
		assertEquals("", FilePaths.extension("/path/to/file"));
		assertEquals("a", FilePaths.extension("/etc/init.d/xyz/file.a"));
		assertEquals("", FilePaths.extension("/etc/init.d/xyz/file"));
	}

	@Test
	public void testStripVersion() {
		assertEquals("jars/bio-formats.jar", //
			FilePaths.stripVersion("jars/bio-formats-4.4-imagej-2.0.0-beta1.jar"));
		assertEquals(FilePaths.stripVersion("jars/ij-data-2.0.0.1-beta1.jar"), //
			FilePaths.stripVersion("jars/ij-data-2.0.0.1-SNAPSHOT.jar"));
		assertEquals(FilePaths.stripVersion("jars/ij-1.44.jar"), //
			FilePaths.stripVersion("jars/ij-1.46b.jar"));
		assertEquals(FilePaths.stripVersion("jars/javassist.jar"), //
			FilePaths.stripVersion("jars/javassist-3.9.0.GA.jar"));
		assertEquals(FilePaths.stripVersion("jars/javassist.jar"), //
			FilePaths.stripVersion("jars/javassist-3.16.1-GA.jar"));
		assertEquals(FilePaths.stripVersion("jars/bsh.jar"), //
			FilePaths.stripVersion("jars/bsh-2.0b4.jar"));
		assertEquals(FilePaths.stripVersion("jars/mpicbg.jar"), //
			FilePaths.stripVersion("jars/mpicbg-20111128.jar"));
		assertEquals(FilePaths.stripVersion("jars/miglayout-swing.jar"), //
			FilePaths.stripVersion("jars/miglayout-3.7.3.1-swing.jar"));
	}

	/** Native binary JARs must not clash with their partner hook JAR. */
	@Test
	public void testStripVersionNativeBinaries() {
		assertEquals("jars/ffmpeg-windows-x86.jar", //
			FilePaths.stripVersion("jars/ffmpeg-2.6.1-0.11-windows-x86.jar"));
		assertEquals("jars/ffmpeg-windows-x86_64.jar", //
			FilePaths.stripVersion("jars/ffmpeg-2.6.1-0.11-windows-x86_64.jar"));
		assertEquals("jars/ffmpeg-macosx-x86_64.jar", //
			FilePaths.stripVersion("jars/ffmpeg-2.6.1-0.11-macosx-x86_64.jar"));
		assertEquals("jars/ffmpeg-linux-x86_64.jar", //
			FilePaths.stripVersion("jars/ffmpeg-2.6.1-0.11-linux-x86_64.jar"));
		assertEquals("jars/ffmpeg-android-arm.jar", //
			FilePaths.stripVersion("jars/ffmpeg-2.6.1-0.11-android-arm.jar"));
	}

	/** The jogamp style of native binary JARs. */
	@Test
	public void testStripVersionJogamp() {
		assertEquals("jars/jogl-all-natives-android-aarch64.jar", FilePaths
			.stripVersion("jars/jogl-all-2.3.0-natives-android-aarch64.jar"));
		assertEquals("jars/jogl-all-natives-linux-armv6hf.jar", FilePaths
			.stripVersion("jars/jogl-all-2.3.0-natives-linux-armv6hf.jar"));
		assertEquals("jars/jogl-all-natives-macosx-universal.jar", FilePaths
			.stripVersion("jars/jogl-all-2.3.0-natives-macosx-universal.jar"));
		assertEquals("jars/jogl-all-natives-windows-i586.jar", FilePaths
			.stripVersion("jars/jogl-all-2.3.0-natives-windows-i586.jar"));
		// The jinput style.
		assertEquals("jars/jinput-natives-all.jar", //
			FilePaths.stripVersion("jars/jinput-2.0.9-natives-all.jar"));
		// native-lib-loader must not be mistaken for a native classifier.
		assertEquals("jars/native-lib-loader.jar", //
			FilePaths.stripVersion("jars/native-lib-loader-2.3.2.jar"));
	}

	@Test
	public void testAllVersions() throws IOException {
		final String withClassifier = "miglayout-3.7.3.1-swing.jar";
		final String withoutClassifier = "miglayout-3.7.3.1.jar";
		final File tmp = Files.createTempDirectory("all-versions-").toFile();
		try {
			assertTrue(new File(tmp, withClassifier).createNewFile());
			assertTrue(new File(tmp, withoutClassifier).createNewFile());

			// A classifier matches only files with that same classifier.
			assertArrayEquals(new File[] { new File(tmp, withClassifier) }, //
				FilePaths.allVersions(tmp, withClassifier));
			assertArrayEquals(new File[] { new File(tmp, withoutClassifier) }, //
				FilePaths.allVersions(tmp, withoutClassifier));
		}
		finally {
			FilePaths.deleteRecursively(tmp);
		}
	}

	@Test
	public void testDeleteRecursively() throws IOException {
		final File tmp = Files.createTempDirectory("delete-recursively-").toFile();
		final File nested = new File(new File(tmp, "a"), "b");
		assertTrue(nested.mkdirs());
		assertTrue(new File(nested, "file.txt").createNewFile());
		assertTrue(new File(tmp, "top.txt").createNewFile());

		assertTrue(FilePaths.deleteRecursively(tmp));
		assertFalse(tmp.exists());
	}

	@Test
	public void testDeleteRecursivelyNullOrMissing() {
		assertTrue(FilePaths.deleteRecursively(null));
		assertTrue(FilePaths.deleteRecursively(new File("no-such-directory-xyz")));
	}
}
