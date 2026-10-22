/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.ridge;

import java.util.Collections;
import java.util.List;

/**
 * Result of {@code segment.detectLines}: the detected lines and the junctions
 * between them.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DetectedLines {

	private final List<DetectedLine> lines;
	private final List<LineJunction> junctions;

	DetectedLines(final List<DetectedLine> lines,
		final List<LineJunction> junctions)
	{
		this.lines = Collections.unmodifiableList(lines);
		this.junctions = Collections.unmodifiableList(junctions);
	}

	/** @return the detected lines (unmodifiable) */
	public List<DetectedLine> lines() {
		return lines;
	}

	/**
	 * @return the junctions between the detected lines (unmodifiable), sorted
	 *         as by the plugin
	 */
	public List<LineJunction> junctions() {
		return junctions;
	}
}
