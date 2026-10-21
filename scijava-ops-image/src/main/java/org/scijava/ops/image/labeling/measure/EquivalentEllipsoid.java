/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.labeling.measure;

import java.util.Arrays;

/**
 * An n-dimensional ellipsoid with arbitrary orientation, as computed by
 * {@code labeling.equivalentEllipsoid}: a center, the semi-axis lengths
 * (radii) in decreasing order, and the unit direction of each semi-axis.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class EquivalentEllipsoid {

	private final double[] center;
	private final double[] radii;
	private final double[][] axes;

	/**
	 * Creates an ellipsoid.
	 *
	 * @param center the center
	 * @param radii the semi-axis lengths, in decreasing order
	 * @param axes the unit direction of each semi-axis, {@code axes[k]} being
	 *          that of {@code radii[k]}
	 */
	public EquivalentEllipsoid(final double[] center, final double[] radii,
		final double[][] axes)
	{
		this.center = center.clone();
		this.radii = radii.clone();
		this.axes = new double[axes.length][];
		for (var k = 0; k < axes.length; k++)
			this.axes[k] = axes[k].clone();
	}

	/** @return the number of dimensions */
	public int numDimensions() {
		return center.length;
	}

	/** @return a copy of the center */
	public double[] center() {
		return center.clone();
	}

	/** @return a copy of the semi-axis lengths, in decreasing order */
	public double[] radii() {
		return radii.clone();
	}

	/**
	 * @param k the index of the semi-axis
	 * @return a copy of the unit direction of the {@code k}-th semi-axis
	 */
	public double[] axis(final int k) {
		return axes[k].clone();
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder("EquivalentEllipsoid[center=");
		sb.append(Arrays.toString(center)).append(", radii=").append(Arrays
			.toString(radii)).append(", axes=");
		for (final var a : axes)
			sb.append(Arrays.toString(a));
		return sb.append("]").toString();
	}
}
