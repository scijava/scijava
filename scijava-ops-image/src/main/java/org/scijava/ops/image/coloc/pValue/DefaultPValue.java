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

package org.scijava.ops.image.coloc.pValue;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.scijava.concurrent.Parallelization;
import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

import org.scijava.ops.image.coloc.ShuffledView;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

/**
 * This algorithm repeatedly executes a colocalization algorithm, computing a
 * p-value. It is based on a new statistical framework published by Wang et al
 * (2017) IEEE Signal Processing "Automated and Robust Quantification of
 * Colocalization in Dual-Color Fluorescence Microscopy: A Nonparametric
 * Statistical Approach".
 *
 * @implNote op names='coloc.pValue'
 */
public class DefaultPValue<T extends RealType<T>, U extends RealType<U>>
	implements
	Computers.Arity6<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<? super RandomAccessibleInterval<T>, ? super RandomAccessibleInterval<U>, Double>, Integer, Dimensions, Long, PValueResult>
{

	/**
	 * TODO
	 *
	 * @param image1 the first image
	 * @param image2 the second image
	 * @param op the op
	 * @param nrRandomizations the number of randomizations
	 * @param psfSize Size of blocks for random shufflings.
	 * @param seed the seed
	 * @param output the output
	 */
	@Override
	public void compute( //
		final RandomAccessibleInterval<T> image1, //
		final RandomAccessibleInterval<U> image2, //
		final BiFunction<? super RandomAccessibleInterval<T>, ? super RandomAccessibleInterval<U>, Double> op, //
		@Nullable Integer nrRandomizations, //
		@Nullable Dimensions psfSize, //
		@Nullable Long seed, //
		final PValueResult output //
	) {
		// Check nullable arguments
		if (nrRandomizations == null) {
			nrRandomizations = 100;
		}
		// NB psfSize null-check is in blockSize method
		if (seed == null) {
			seed = 0x27372034L;
		}

		final var blockSize = blockSize(image1, psfSize);
		final var trimmedImage1 = trim(image1, blockSize);
		final var trimmedImage2 = trim(image2, blockSize);
		final var type1 = Util.getTypeFromInterval(image1);

		final var sampleDistribution = new double[nrRandomizations];

		// compute actual coloc value
		final double value = op.apply(trimmedImage1, trimmedImage2);

		// compute shuffled coloc values in parallel
		var executor = Parallelization.getTaskExecutor();
		var numTasks = executor.suggestNumberOfTasks();

        var r = new Random(seed);
        var seeds = new long[nrRandomizations];
		for (var s = 0; s < nrRandomizations; s++) {
			seeds[s] = r.nextLong();
		}

        var params = IntStream.rangeClosed(0, numTasks - 1) //
			.boxed().collect(Collectors.toList());

		// NB final variable needed for use in lambda
		final var nr = nrRandomizations;
		Consumer<Integer> task = (t) -> {
            var offset = t * nr / numTasks;
            var count = (t + 1) * nr / numTasks - offset;
			// a new one per thread and each needs its own seed
			final var shuffled = new ShuffledView<T>(trimmedImage1,
				blockSize, seeds[offset]);
            var buffer = Util.getSuitableImgFactory(shuffled, type1).create(
				shuffled);
			for (var i = 0; i < count; i++) {
                var index = offset + i;
				if (index >= nr) break;
				if (i > 0) shuffled.shuffleBlocks(seeds[index]);
				copy(shuffled, buffer);
				sampleDistribution[index] = op.apply(buffer, trimmedImage2);
			}
		};

		try {
			executor.forEach(params, task);
		}
		catch (final Exception exc) {
			final var cause = exc.getCause();
			if (cause instanceof RuntimeException) throw (RuntimeException) cause;
			throw new RuntimeException(exc);
		}

		output.setColocValue(value);
		output.setColocValuesArray(sampleDistribution);
		output.setPValue(calculatePvalue(value, sampleDistribution));
	}

	private void copy(ShuffledView<T> shuffled, Img<T> buffer) {
        var cursor = buffer.localizingCursor();
        var ra = shuffled.randomAccess();
		while (cursor.hasNext()) {
            var v = cursor.next();
			ra.setPosition(cursor);
			v.set(ra.get());
		}
	}

	private double calculatePvalue(final double input,
		final double[] distribution)
	{
		double count = 0;
		for (var i = 0; i < distribution.length; i++) {
			if (distribution[i] > input) {
				count++;
			}
		}
		final var pval = count / distribution.length;
		return pval;
	}

	private static int[] blockSize(final Dimensions image,
		final Dimensions psfSize)
	{
		if (psfSize != null) return Intervals.dimensionsAsIntArray(psfSize);

		final var blockSize = new int[image.numDimensions()];
		for (var d = 0; d < blockSize.length; d++) {
			final var size = (long) Math.floor(Math.sqrt(image.dimension(d)));
			if (size > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Image dimension #" + d +
					" is too large: " + image.dimension(d));
			}
			blockSize[d] = (int) size;
		}
		return blockSize;
	}

	private static <V> RandomAccessibleInterval<V> trim(
		final RandomAccessibleInterval<V> image, final int[] blockSize)
	{
		final var min = Intervals.minAsLongArray(image);
		final var max = Intervals.maxAsLongArray(image);
		for (var d = 0; d < blockSize.length; d++) {
			final var trimSize = image.dimension(d) % blockSize[d];
			final var half = trimSize / 2;
			min[d] += half;
			max[d] -= trimSize - half;
		}
		return Views.interval(image, min, max);
	}
}
