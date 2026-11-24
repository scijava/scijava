/*-
 * #%L
 * LGPL-licensed image processing operations for SciJava Ops, translated from LGPL ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.lgpl;

import org.scijava.meta.POM;
import org.scijava.meta.Versions;

/**
 * Main entry point which displays information about the component.
 *
 * @author Curtis Rueden
 */
public class About {

	private static String about(final Class<?> c, final String groupId,
		final String artifactId)
	{
		final var pom = POM.pom(c, groupId, artifactId);
		final var version = Versions.of(c, groupId, artifactId);
		final var sb = new StringBuilder();

		final var projectName = pom.projectName();
		if (projectName != null && !projectName.isEmpty()) sb.append(projectName);
		else sb.append(groupId + ":" + artifactId);
		if (version != null && !version.isEmpty()) sb.append(" v" + version);
		sb.append("\n");

		final var licenseName = pom.cdata("//project/licenses/license/name");
		if (licenseName != null && !licenseName.isEmpty()) {
			sb.append("Project license: " + licenseName + "\n");
		}

		final var projectURL = pom.projectURL();
		if (projectURL != null && !projectURL.isEmpty()) {
			sb.append("Project website: " + projectURL);
		}

		return sb.toString();
	}

	public static void main(String... args) {
		System.out.println(about(About.class, "org.scijava",
			"scijava-ops-image-lgpl"));
	}
}
