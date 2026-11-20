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

import net.imglib2.RealLocalizable;

/**
 * A junction between two lines detected by {@code segment.detectLines}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 */
public class LineJunction implements RealLocalizable {

	private final double x;
	private final double y;
	private final DetectedLine line1;
	private final DetectedLine line2;
	private final int position;
	private final boolean nonTerminal;

	LineJunction(final double x, final double y, final DetectedLine line1,
		final DetectedLine line2, final int position, final boolean nonTerminal)
	{
		this.x = x;
		this.y = y;
		this.line1 = line1;
		this.line2 = line2;
		this.position = position;
		this.nonTerminal = nonTerminal;
	}

	/** @return the X coordinate of the junction */
	public double x() {
		return x;
	}

	/** @return the Y coordinate of the junction */
	public double y() {
		return y;
	}

	/** @return the first line of the junction */
	public DetectedLine line1() {
		return line1;
	}

	/** @return the second line of the junction, which runs into the first */
	public DetectedLine line2() {
		return line2;
	}

	/**
	 * @return the index of the junction point among the vertices of
	 *         {@link #line1()}, as reported by the plugin
	 */
	public int position() {
		return position;
	}

	/**
	 * @return true if the overlap resolution found that the junction lies on
	 *         neither line end (always false without overlap resolution)
	 */
	public boolean isNonTerminal() {
		return nonTerminal;
	}

	@Override
	public int numDimensions() {
		return 2;
	}

	@Override
	public double getDoublePosition(final int d) {
		return d == 0 ? x : y;
	}
}
