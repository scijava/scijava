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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link Versions}.
 * 
 * @author Curtis Rueden
 */
public class VersionsTest {

	/** Tests {@link Versions#compare(String, String)}. */
	@Test
	public void testCompare() {
		// SemVer PATCH version.
		assertTrue(Versions.compare("1.5.1", "1.5.2") < 0);
		assertTrue(Versions.compare("1.5.2", "1.5.1") > 0);

		// SemVer MINOR version.
		assertTrue(Versions.compare("1.5.2", "1.6.2") < 0);
		assertTrue(Versions.compare("1.6.2", "1.5.2") > 0);

		// SemVer MAJOR version.
		assertTrue(Versions.compare("1.7.3", "2.7.3") < 0);
		assertTrue(Versions.compare("2.7.3", "1.7.3") > 0);

		// Suffix indicates version is older than final release.
		assertTrue(Versions.compare("1.5.2", "1.5.2-beta-1") > 0);

		// Check when number of version tokens does not match.
		assertTrue(Versions.compare("1.5", "1.5.1") < 0);
		assertTrue(Versions.compare("1.5.1", "1.5") > 0);

		// Check equality.
		assertEquals(Versions.compare("1.5", "1.5"), 0);

		// Check ImageJ 1.x style versions.
		assertTrue(Versions.compare("1.50a", "1.50b") < 0);

		// Check four version tokens.
		assertTrue(Versions.compare("1.5.1.3", "1.5.1.4") < 0);
		assertTrue(Versions.compare("1.5.1.6", "1.5.1.5") > 0);
		assertEquals(Versions.compare("10.4.9.8", "10.4.9.8"), 0);

		// Check non-numeric tokens.
		assertTrue(Versions.compare("a.b.c", "a.b.d") < 0);

		// Check for numerical (not lexicographic) comparison.
		assertTrue(Versions.compare("2.0", "23.0") < 0);
		assertTrue(Versions.compare("23.0", "2.0") > 0);
		assertTrue(Versions.compare("3.0", "23.0") < 0);
		assertTrue(Versions.compare("23.0", "3.0") > 0);

		// Check weird stuff.
		assertTrue(Versions.compare("1", "a") < 0);
		assertTrue(Versions.compare("1", "%") > 0);
		assertTrue(Versions.compare("", "1") < 0);
		assertTrue(Versions.compare("1", "1.") < 0);
		assertTrue(Versions.compare("", ".") < 0);
		assertTrue(Versions.compare("", "..") < 0);
		assertTrue(Versions.compare(".", "..") < 0);
	}
}
