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

package org.scijava.ops.image.morphology.directional;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.view.Views;

/**
 * A {@link Shape} consisting of a digital straight line segment through the
 * center pixel, at an arbitrary orientation in the plane of the first two
 * dimensions.
 * <p>
 * The segment is symmetric about the center, so it equals its own reflection
 * (which makes an erosion followed by a dilation with it a morphological
 * opening). For an orientation {@code θ}, the dominant axis is the one of
 * {@code (cos θ, sin θ)} with the larger magnitude. The segment contains one
 * pixel for each integer step {@code k} in {@code [-K, K]} along the dominant
 * axis, the other coordinate being {@code k} times the slope, rounded half
 * away from zero, as in a Bresenham line (Hendriks &amp; van Vliet 2005).
 * {@code K} is chosen so that the Euclidean length of the segment is close to
 * the requested length: {@code K = round(((length - 1) / 2) · max(|cos θ|,
 * |sin θ|))}. The angle is measured from the positive x axis (dimension 0)
 * towards the positive y axis (dimension 1). Offsets in further dimensions
 * are zero, so on an n-D image the line acts plane by plane.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @see <a href="https://doi.org/10.1016/j.sigpro.2004.09.005">C. L. L. Hendriks
 *      and L. J. van Vliet, "Using line segments as structuring elements for
 *      sampling-invariant measurements", IEEE Trans. Pattern Anal. Mach.
 *      Intell. 27(11):1826-1831, 2005</a>
 */
public class OrientedLineShape implements Shape {

	private final double length;
	private final double angle;

	/** The offsets of the line pixels, as {@code [x, y]} pairs. */
	private final long[][] offsets;

	/**
	 * Creates an oriented line shape.
	 *
	 * @param length the approximate Euclidean length of the segment, in pixels
	 * @param angle the orientation of the segment, in radians
	 */
	public OrientedLineShape(final double length, final double angle) {
		if (!(length >= 1)) {
			throw new IllegalArgumentException("Length must be at least 1: " +
				length);
		}
		this.length = length;
		this.angle = angle;
		final var cos = Math.cos(angle);
		final var sin = Math.sin(angle);
		final var horizontal = Math.abs(cos) >= Math.abs(sin);
		final var major = horizontal ? cos : sin;
		final var slope = (horizontal ? sin : cos) / major;
		final var k = Math.round((length - 1) / 2 * Math.abs(major));
		offsets = new long[(int) (2 * k + 1)][];
		for (var i = -k; i <= k; i++) {
			final var minor = Math.signum(i * slope) * Math.round(Math.abs(i *
				slope));
			final var step = (long) Math.signum(major) * i;
			offsets[(int) (i + k)] = horizontal ? new long[] { step, (long) minor }
				: new long[] { (long) minor, step };
		}
	}

	/** @return the approximate Euclidean length of the segment */
	public double getLength() {
		return length;
	}

	/** @return the orientation of the segment, in radians */
	public double getAngle() {
		return angle;
	}

	/**
	 * @return a copy of the offsets of the line pixels relative to the center,
	 *         as {@code [x, y]} pairs
	 */
	public long[][] getOffsets() {
		final var copy = new long[offsets.length][];
		for (var i = 0; i < offsets.length; i++)
			copy[i] = offsets[i].clone();
		return copy;
	}

	@Override
	public <T> RandomAccessibleInterval<Neighborhood<T>> neighborhoods(
		final RandomAccessibleInterval<T> source)
	{
		return Views.interval(neighborhoodsRandomAccessible(source), source);
	}

	@Override
	public <T> RandomAccessible<Neighborhood<T>> neighborhoodsRandomAccessible(
		final RandomAccessible<T> source)
	{
		checkDimensions(source.numDimensions());
		return new LineNeighborhoods<>(source);
	}

	@Override
	public <T> RandomAccessibleInterval<Neighborhood<T>> neighborhoodsSafe(
		final RandomAccessibleInterval<T> source)
	{
		return neighborhoods(source);
	}

	@Override
	public <T> RandomAccessible<Neighborhood<T>>
		neighborhoodsRandomAccessibleSafe(final RandomAccessible<T> source)
	{
		return neighborhoodsRandomAccessible(source);
	}

	@Override
	public Interval getStructuringElementBoundingBox(final int numDimensions) {
		checkDimensions(numDimensions);
		final var min = new long[numDimensions];
		final var max = new long[numDimensions];
		for (final var o : offsets) {
			for (var d = 0; d < 2; d++) {
				min[d] = Math.min(min[d], o[d]);
				max[d] = Math.max(max[d], o[d]);
			}
		}
		return new FinalInterval(min, max);
	}

	@Override
	public String toString() {
		return "OrientedLineShape, length = " + length + ", angle = " + angle;
	}

	private static void checkDimensions(final int numDimensions) {
		if (numDimensions < 2) {
			throw new IllegalArgumentException(
				"An oriented line needs at least 2 dimensions");
		}
	}

	/** The random-accessible field of line neighborhoods over a source. */
	private final class LineNeighborhoods<T> implements
		RandomAccessible<Neighborhood<T>>
	{

		private final RandomAccessible<T> source;

		private LineNeighborhoods(final RandomAccessible<T> source) {
			this.source = source;
		}

		@Override
		public int numDimensions() {
			return source.numDimensions();
		}

		@Override
		public RandomAccess<Neighborhood<T>> randomAccess() {
			return new LineNeighborhoodAccess<>(source);
		}

		@Override
		public RandomAccess<Neighborhood<T>> randomAccess(final Interval interval) {
			return randomAccess();
		}

		@Override
		public Neighborhood<T> getType() {
			return randomAccess().get();
		}
	}

	/** A random access whose samples are line neighborhoods. */
	private final class LineNeighborhoodAccess<T> extends Point implements
		RandomAccess<Neighborhood<T>>
	{

		private final RandomAccessible<T> source;

		private LineNeighborhoodAccess(final RandomAccessible<T> source) {
			super(source.numDimensions());
			this.source = source;
		}

		private LineNeighborhoodAccess(final LineNeighborhoodAccess<T> other) {
			super(other);
			this.source = other.source;
		}

		@Override
		public Neighborhood<T> get() {
			return new LineNeighborhood<>(this, source);
		}

		@Override
		public LineNeighborhoodAccess<T> copy() {
			return new LineNeighborhoodAccess<>(this);
		}

		@Override
		public Neighborhood<T> getType() {
			return get();
		}
	}

	/** The line neighborhood of one center pixel. */
	private final class LineNeighborhood<T> extends Point implements
		Neighborhood<T>
	{

		private final RandomAccessible<T> source;
		private final Interval bounds;

		private LineNeighborhood(final Localizable center,
			final RandomAccessible<T> source)
		{
			super(center);
			this.source = source;
			final var box = getStructuringElementBoundingBox();
			final var min = box.minAsLongArray();
			final var max = box.maxAsLongArray();
			for (var d = 0; d < min.length; d++) {
				min[d] += position[d];
				max[d] += position[d];
			}
			bounds = new FinalInterval(min, max);
		}

		@Override
		public Interval getStructuringElementBoundingBox() {
			return OrientedLineShape.this.getStructuringElementBoundingBox(
				numDimensions());
		}

		@Override
		public long min(final int d) {
			return bounds.min(d);
		}

		@Override
		public long max(final int d) {
			return bounds.max(d);
		}

		@Override
		public long size() {
			return offsets.length;
		}

		@Override
		public Object iterationOrder() {
			return this;
		}

		@Override
		public Cursor<T> cursor() {
			return new LineCursor<>(position, source.randomAccess());
		}

		@Override
		public Cursor<T> localizingCursor() {
			return cursor();
		}

		@Override
		public T getType() {
			return source.getType();
		}
	}

	/** A cursor over the pixels of one line neighborhood. */
	private final class LineCursor<T> extends AbstractCursor<T> {

		private final long[] center;
		private final RandomAccess<T> access;
		private int index = -1;

		private LineCursor(final long[] center, final RandomAccess<T> access) {
			super(center.length);
			this.center = center.clone();
			this.access = access;
		}

		@Override
		public T get() {
			return access.get();
		}

		@Override
		public void fwd() {
			index++;
			final var o = offsets[index];
			access.setPosition(center);
			access.move(o[0], 0);
			access.move(o[1], 1);
		}

		@Override
		public void reset() {
			index = -1;
		}

		@Override
		public boolean hasNext() {
			return index < offsets.length - 1;
		}

		@Override
		public void localize(final long[] pos) {
			access.localize(pos);
		}

		@Override
		public long getLongPosition(final int d) {
			return access.getLongPosition(d);
		}

		@Override
		public LineCursor<T> copy() {
			final var c = new LineCursor<>(center, access.copy());
			c.index = index;
			return c;
		}

		@Override
		public void jumpFwd(final long steps) {
			for (var i = 0; i < steps; i++)
				fwd();
		}
	}
}
