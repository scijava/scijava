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
package org.scijava.ops.image.segment.hough;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;

/**
 * A circle (or n-dimensional sphere) detected by
 * {@code segment.detectHoughCircles}: a center position, a radius and a
 * sensitivity.
 * <p>
 * The sensitivity is the surface area of the sphere divided by the number of
 * votes it received in the Hough transform. It is approximately 1 for a
 * complete, one-pixel-thick circle, and grows as the circle gets fainter or
 * more incomplete: lower values denote stronger detections.
 * <p>
 * Adapted from {@code HoughCircle} of CircleSkinner by Jean-Yves Tinevez
 * (<a href="https://github.com/tinevez/CircleSkinner">CircleSkinner</a>,
 * BSD-2-Clause).
 * <p>
 * The original CircleSkinner license notice follows, with the copyright line
 * as it appears in the CircleSkinner sources.
 * </p>
 *
 * <pre>
 * Copyright (C) 2016 - 2022 My Company, Inc.
 *
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
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jean-Yves Tinevez
 */
public class HoughCircle extends RealPoint implements Comparable<HoughCircle> {

	private final double radius;

	private final double sensitivity;

	/**
	 * Creates a new circle.
	 *
	 * @param center the center of the circle
	 * @param radius the radius of the circle
	 * @param sensitivity the sensitivity of the detection (lower is stronger)
	 */
	public HoughCircle(final RealLocalizable center, final double radius,
		final double sensitivity)
	{
		super(center);
		this.radius = radius;
		this.sensitivity = sensitivity;
	}

	/**
	 * Gets the radius of this circle.
	 *
	 * @return the radius, in pixels
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Gets the sensitivity of this circle: the surface area of the circle
	 * divided by its number of votes. Lower values denote stronger detections.
	 *
	 * @return the sensitivity
	 */
	public double getSensitivity() {
		return sensitivity;
	}

	/**
	 * Tests whether a point lies inside (or on) this circle. Only the first
	 * {@link #numDimensions()} coordinates of the point are considered.
	 *
	 * @param point the point to test
	 * @return true iff the point lies within {@link #getRadius()} of the center
	 */
	public boolean contains(final RealLocalizable point) {
		var dr2 = 0.0;
		for (var d = 0; d < numDimensions(); d++) {
			final var dx = getDoublePosition(d) - point.getDoublePosition(d);
			dr2 += dx * dx;
		}
		return dr2 <= radius * radius;
	}

	/** Orders circles by ascending sensitivity, i.e. strongest first. */
	@Override
	public int compareTo(final HoughCircle o) {
		return Double.compare(sensitivity, o.sensitivity);
	}

	@Override
	public String toString() {
		return String.format("%s R=%.2f sensitivity=%.3f", super.toString(),
			radius, sensitivity);
	}
}
