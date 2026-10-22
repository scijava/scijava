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
package org.scijava.ops.image.transform.hough;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Hough transform for circles (2D) and, more generally, n-dimensional
 * spheres.
 * <p>
 * Every foreground pixel of the binary input casts one vote for each possible
 * center lying at distance {@code r} from it, for each radius {@code r} in
 * {@code minRadius, minRadius + stepRadius, ...} up to {@code maxRadius}. The
 * result is an (n+1)-dimensional vote image: the first n dimensions match the
 * input (including its minimum), and the last dimension indexes the radii, so
 * that index {@code i} corresponds to radius {@code minRadius + i * stepRadius}.
 * The value at a location is the number of foreground pixels lying on the
 * sphere of that radius centered there. Use {@code segment.detectHoughCircles}
 * to extract circles from the vote image.
 * <p>
 * The discrete sphere of radius {@code r} is the set of integer offsets whose
 * Euclidean norm rounds to {@code r}; in 2D this is a closed ring of
 * approximately {@code 2πr} pixels.
 * <p>
 * Ported from {@code HoughTransformOp} of CircleSkinner by Jean-Yves Tinevez
 * (<a href="https://github.com/tinevez/CircleSkinner">CircleSkinner</a>,
 * BSD-2-Clause). The original votes along 2D midpoint circles; this version
 * generalizes the voting to n dimensions.
 * <p>
 * Reference: R. O. Duda and P. E. Hart, "Use of the Hough transformation to
 * detect lines and curves in pictures", Communications of the ACM 15(1),
 * 11-15, 1972.
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
 * @param <B> the type of the binary input pixels
 * @implNote op names='transform.houghCircle'
 */
public class HoughCircleTransform<B extends BooleanType<B>> implements
	Functions.Arity4<RandomAccessibleInterval<B>, Integer, Integer, Integer, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	/**
	 * Computes the Hough vote image of a binary image.
	 *
	 * @param input the binary image whose foreground pixels vote
	 * @param minRadius the smallest radius to test, in pixels (at least 1)
	 * @param maxRadius the largest radius to test, in pixels (inclusive, at
	 *          least minRadius)
	 * @param stepRadius the increment between tested radii, in pixels (defaults
	 *          to 1)
	 * @return the (n+1)-dimensional vote image, whose last dimension indexes the
	 *         tested radii
	 */
	@Override
	public RandomAccessibleInterval<DoubleType> apply( //
		final RandomAccessibleInterval<B> input, //
		final Integer minRadius, //
		final Integer maxRadius, //
		@Nullable Integer stepRadius //
	) {
		if (stepRadius == null) stepRadius = 1;
		if (minRadius < 1) throw new IllegalArgumentException(
			"Minimum radius must be at least 1: " + minRadius);
		if (maxRadius < minRadius) throw new IllegalArgumentException(
			"Maximum radius " + maxRadius + " is smaller than minimum radius " +
				minRadius);
		if (stepRadius < 1) throw new IllegalArgumentException(
			"Radius step must be at least 1: " + stepRadius);

		final var n = input.numDimensions();
		final var nRadii = (maxRadius - minRadius) / stepRadius + 1;

		final var dims = new long[n + 1];
		for (var d = 0; d < n; d++)
			dims[d] = input.dimension(d);
		dims[n] = nRadii;
		final var votes = createOp.apply(new FinalDimensions(dims),
			new DoubleType());

		final var offsets = new long[nRadii][][];
		for (var i = 0; i < nRadii; i++) {
			offsets[i] = shellOffsets(n, minRadius + i * stepRadius);
		}

		final var min = Intervals.minAsLongArray(input);
		final var pos = new long[n];
		final var target = new long[n + 1];
		final var ra = votes.randomAccess();
		final var cursor = Views.iterable(input).localizingCursor();
		while (cursor.hasNext()) {
			if (!cursor.next().get()) continue;
			cursor.localize(pos);
			for (var d = 0; d < n; d++)
				pos[d] -= min[d];
			for (var i = 0; i < nRadii; i++) {
				target[n] = i;
				OFFSET: for (final var offset : offsets[i]) {
					for (var d = 0; d < n; d++) {
						final var p = pos[d] + offset[d];
						if (p < 0 || p >= dims[d]) continue OFFSET;
						target[d] = p;
					}
					ra.setPosition(target);
					ra.get().inc();
				}
			}
		}

		var zeroMin = true;
		for (var d = 0; d < n; d++)
			zeroMin &= min[d] == 0;
		if (zeroMin) return votes;
		final var translation = new long[n + 1];
		System.arraycopy(min, 0, translation, 0, n);
		return Views.translate(votes, translation);
	}

	/**
	 * Gets the integer offsets whose Euclidean norm rounds to the given radius.
	 */
	private static long[][] shellOffsets(final int n, final int radius) {
		final var lo = (radius - 0.5) * (radius - 0.5);
		final var hi = (radius + 0.5) * (radius + 0.5);
		final List<long[]> offsets = new ArrayList<>();
		final var offset = new long[n];
		for (var d = 0; d < n; d++)
			offset[d] = -radius;
		while (true) {
			var r2 = 0.0;
			for (var d = 0; d < n; d++)
				r2 += offset[d] * offset[d];
			if (r2 >= lo && r2 < hi) offsets.add(offset.clone());
			var d = 0;
			while (d < n && offset[d] == radius) {
				offset[d] = -radius;
				d++;
			}
			if (d == n) break;
			offset[d]++;
		}
		return offsets.toArray(new long[0][]);
	}
}
