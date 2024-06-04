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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link POM}.
 * 
 * @author Curtis Rueden
 */
public class POMTest {

	/** Tests {@link POM#compareVersions(String, String)}. */
	@Test
	public void testCompareVersions() {
		// basic checks
		assertTrue(POM.compareVersions("1", "2") < 0);
		assertTrue(POM.compareVersions("1.0", "2.0") < 0);
		assertTrue(POM.compareVersions("1.0", "1.1") < 0);
		assertTrue(POM.compareVersions("1.0.0", "2.0.0") < 0);

		// sanity checks for argument order
		assertTrue(POM.compareVersions("2", "1") > 0);
		assertTrue(POM.compareVersions("2.0", "1.0") > 0);
		assertTrue(POM.compareVersions("1.1", "1.0") > 0);
		assertTrue(POM.compareVersions("2.0.0", "1.0.0") > 0);

		// more complex/unusual checks
		assertTrue(POM.compareVersions("1.0-RC1", "1.0-RC2") < 0);
		assertTrue(POM.compareVersions("1.0-RC-1", "1.0-RC-2") < 0);
		assertTrue(POM.compareVersions("1.0-RC-2", "1.0-RC-10") < 0);
		assertTrue(POM.compareVersions("0.4-alpha", "0.4-beta") < 0);
		assertTrue(POM.compareVersions("foo", "bar") > 0);

		// checks which expose bugs/limitations
		assertTrue(POM.compareVersions("1.0-RC2", "1.0-RC10") > 0);
		assertTrue(POM.compareVersions("1.0-rc1", "1.0-RC2") > 0);

		// check that varying numbers of digits are handled properly
		assertTrue(POM.compareVersions("2.0.0", "2.0.0.1") < 0);
		// check that SemVer pre-release versions are handled properly
		assertTrue(POM.compareVersions("2.0.0", "2.0.0-beta-1") > 0);
	}

	@Test
	public void testAccessors() throws IOException {
		final POM pom = new POM(new File("pom.xml"));
		assertEquals("org.scijava", pom.parentGroupId());
		//assertEquals("pom-scijava", pom.parentArtifactId());
		assertNotNull(pom.parentVersion());
		assertEquals("org.scijava", pom.groupId());
		assertEquals("scijava-meta", pom.artifactId());
		assertNotNull(pom.version());
		assertEquals("GitHub Actions", pom.ciManagementSystem());
		final String ciManagementURL = pom.ciManagementURL();
		assertEquals("https://github.com/scijava/scijava/actions",
			ciManagementURL);
		assertEquals("GitHub Issues", pom.issueManagementSystem());
		final String issueManagementURL = pom.issueManagementURL();
		assertEquals("https://github.com/scijava/scijava/issues",
			issueManagementURL);
		assertEquals("SciJava", pom.organizationName());
		assertEquals("https://scijava.org/", pom.organizationURL());
		assertTrue(pom.path().endsWith("pom.xml"));
		assertTrue(pom.projectDescription().startsWith(
			"Utility functions to introspect metadata of SciJava libraries."));
		assertEquals("2022", pom.projectInceptionYear());
		assertEquals("SciJava Meta", pom.projectName());
		assertEquals("https://github.com/scijava/scijava", //
			pom.projectURL());
		final String scmConnection = pom.scmConnection();
		assertEquals("scm:git:https://github.com/scijava/scijava",
			scmConnection);
		final String scmDeveloperConnection = pom.scmDeveloperConnection();
		assertEquals("scm:git:git@github.com:scijava/scijava",
			scmDeveloperConnection);
		assertNotNull(pom.scmTag()); // won't be HEAD for release tags
		assertEquals("https://github.com/scijava/scijava", pom.scmURL());
	}

	@Test
	public void testCdata() throws IOException {
		final POM pom = new POM(new File("pom.xml"));
		assertEquals("repo", pom.cdata("//project/licenses/license/distribution"));
		assertEquals("https://github.com/scijava/scijava", //
			pom.cdata("//project/url"));
	}
}
