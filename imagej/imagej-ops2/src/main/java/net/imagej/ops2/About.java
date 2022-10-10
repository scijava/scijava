/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

package net.imagej.ops2;

import org.scijava.util.POM;
import org.scijava.util.VersionUtils;

/**
 * Main entry point which displays information about the component.
 * 
 * @author Curtis Rueden
 */
public class About {

	private static String about(final Class<?> c, final String groupId,
		final String artifactId)
	{
		final POM pom = POM.getPOM(c, groupId, artifactId);
		final String version = VersionUtils.getVersion(c, groupId, artifactId);
		final StringBuilder sb = new StringBuilder();

		final String projectName = pom.getProjectName();
		if (projectName != null && !projectName.isEmpty()) sb.append(projectName);
		else sb.append(groupId + ":" + artifactId);
		if (version != null && !version.isEmpty()) sb.append(" v" + version);
		sb.append("\n");

		final String licenseName = pom.cdata("//project/licenses/license/name");
		if (licenseName != null && !licenseName.isEmpty()) {
			sb.append("Project license: " + licenseName + "\n");
		}

		final String projectURL = pom.getProjectURL();
		if (projectURL != null && !projectURL.isEmpty()) {
			sb.append("Project website: " + projectURL);
		}

		return sb.toString();
	}

	public static void main(String... args) {
		System.out.println(about(About.class, "net.imagej", "imagej-ops2"));
	}
}
