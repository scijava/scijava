/*-
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

package org.scijava.ops.image.image.extrema;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.LocalExtrema;
import net.imglib2.algorithm.localextrema.LocalExtrema.LocalNeighborhoodCheck;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.concurrent.Parallelization;

/**
 * Utility methods backing the {@code image.localExtrema} ops.
 *
 * @author Curtis Rueden
 */
final class LocalExtremaFinder {

	private LocalExtremaFinder() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Finds the local extrema of {@code input}, sorted in flat iteration order
	 * (first dimension varying fastest).
	 */
	static <T extends RealType<T>> List<Point> find(
		final RandomAccessibleInterval<T> input, Shape shape,
		final Double threshold, Boolean maxima)
	{
		if (shape == null) shape = new RectangleShape(1, true);
		if (maxima == null) maxima = true;

		// Build the threshold value, clamped to the range of the pixel type.
		final var t = input.getType().createVariable();
		double bound;
		if (threshold == null) bound = maxima ? t.getMinValue() : t.getMaxValue();
		else bound = Math.max(t.getMinValue(), Math.min(t.getMaxValue(),
			threshold));
		if (t instanceof IntegerType) {
			// Note: Rounding inward keeps the comparison exact for integer pixels.
			bound = maxima ? Math.ceil(bound) : Math.floor(bound);
		}
		t.setReal(bound);

		final LocalNeighborhoodCheck<Point, T> check = maxima ? //
			new LocalExtrema.MaximumCheck<>(t) : new LocalExtrema.MinimumCheck<>(t);

		// Note: Extending by border replication lets edge pixels be extrema.
		// Since the checks are non-strict, replicated values never disqualify a
		// candidate unless a pixel within the image would also do so.
		final var source = Views.extendBorder(input);

		final var es = Parallelization.getExecutorService();
		final var splitDim = LocalExtrema.getBiggestDimension(input);
		final var numTasks = (int) Math.max(1, Math.min(input.dimension(
			splitDim), 4L * Runtime.getRuntime().availableProcessors()));
		final List<Point> extrema;
		try {
			extrema = new ArrayList<>(LocalExtrema.findLocalExtrema(source, input,
				check, shape, es, numTasks, splitDim));
		}
		catch (final InterruptedException exc) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(exc);
		}
		catch (final ExecutionException exc) {
			throw new RuntimeException(exc);
		}

		// Sort for deterministic output regardless of the parallel split.
		extrema.sort(FLAT_ORDER);
		return extrema;
	}

	private static final Comparator<Point> FLAT_ORDER = (p1, p2) -> {
		for (var d = p1.numDimensions() - 1; d >= 0; d--) {
			final var c = Long.compare(p1.getLongPosition(d), p2.getLongPosition(d));
			if (c != 0) return c;
		}
		return 0;
	};
}
