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

package net.imagej.ops2.coloc.pValue;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.scijava.concurrent.Parallelization;
import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

import net.imagej.ops2.coloc.ShuffledView;
import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccess;
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
 *@implNote op names='coloc.pValue'
 */
public class DefaultPValue<T extends RealType<T>, U extends RealType<U>> implements
		Computers.Arity6<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, Integer, Dimensions, Long, PValueResult> {

	/**
	 * TODO
	 *
	 * @param image1 the first image
	 * @param image2 the second image
	 * @param op the op
	 * @param nrRandomizations the number of randomizations
	 * @param psfSize the psf size
	 * @param seed the seed
	 * @param output the output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> image1, final RandomAccessibleInterval<U> image2,
			final BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double> op,
			final Integer nrRandomizations, final Dimensions psfSize, final Long seed,
			final PValueResult output) {
		final int[] blockSize = blockSize(image1, psfSize);
		final RandomAccessibleInterval<T> trimmedImage1 = trim(image1, blockSize);
		final RandomAccessibleInterval<U> trimmedImage2 = trim(image2, blockSize);
		final T type1 = Util.getTypeFromInterval(image1);

		final double[] sampleDistribution = new double[nrRandomizations];

		// compute actual coloc value
		final double value = op.apply(trimmedImage1, trimmedImage2);

		// compute shuffled coloc values in parallel
		var executor = Parallelization.getTaskExecutor();
		var numTasks = executor.suggestNumberOfTasks();

		Random r = new Random(seed);
		long[] seeds = new long[nrRandomizations];
		for (int s = 0; s < nrRandomizations; s++) {
			seeds[s] = r.nextLong();
		}

		List<Integer> params = IntStream.rangeClosed(0, numTasks - 1) //
		 .boxed().collect(Collectors.toList());

		Consumer<Integer> task = (t) -> {
			int offset = t * nrRandomizations / numTasks;
			int count = (t + 1) * nrRandomizations / numTasks - offset;
			// a new one per thread and each needs its own seed
			final ShuffledView<T> shuffled = new ShuffledView<>(trimmedImage1, blockSize, seeds[offset]);
			Img<T> buffer = Util.getSuitableImgFactory(shuffled, type1).create(shuffled);
			for (int i = 0; i < count; i++) {
				int index = offset + i;
				if (index >= nrRandomizations)
					break;
				if (i > 0)
					shuffled.shuffleBlocks(seeds[index]);
				copy(shuffled, buffer);
				sampleDistribution[index] = op.apply(buffer, trimmedImage2);
			}
		};

		try {
			executor.forEach(params, task);
		} catch (final Exception exc) {
			final Throwable cause = exc.getCause();
			if (cause instanceof RuntimeException)
				throw (RuntimeException) cause;
			throw new RuntimeException(exc);
		}

		output.setColocValue(value);
		output.setColocValuesArray(sampleDistribution);
		output.setPValue(calculatePvalue(value, sampleDistribution));
	}

	private void copy(ShuffledView<T> shuffled, Img<T> buffer) {
		Cursor<T> cursor = buffer.localizingCursor();
		RandomAccess<T> ra = shuffled.randomAccess();
		while (cursor.hasNext()) {
			T v = cursor.next();
			ra.setPosition(cursor);
			v.set(ra.get());
		}
	}

	private double calculatePvalue(final double input, final double[] distribution) {
		double count = 0;
		for (int i = 0; i < distribution.length; i++) {
			if (distribution[i] > input) {
				count++;
			}
		}
		final double pval = count / distribution.length;
		return pval;
	}

	private static int[] blockSize(final Dimensions image, final Dimensions psfSize) {
		if (psfSize != null)
			return Intervals.dimensionsAsIntArray(psfSize);

		final int[] blockSize = new int[image.numDimensions()];
		for (int d = 0; d < blockSize.length; d++) {
			final long size = (long) Math.floor(Math.sqrt(image.dimension(d)));
			if (size > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Image dimension #" + d + " is too large: " + image.dimension(d));
			}
			blockSize[d] = (int) size;
		}
		return blockSize;
	}

	private static <V> RandomAccessibleInterval<V> trim(final RandomAccessibleInterval<V> image,
			final int[] blockSize) {
		final long[] min = Intervals.minAsLongArray(image);
		final long[] max = Intervals.maxAsLongArray(image);
		for (int d = 0; d < blockSize.length; d++) {
			final long trimSize = image.dimension(d) % blockSize[d];
			final long half = trimSize / 2;
			min[d] += half;
			max[d] -= trimSize - half;
		}
		return Views.interval(image, min, max);
	}
}

/**
 *@implNote op names='coloc.pValue'
 */
class PValueSimpleWithRandomizations<T extends RealType<T>, U extends RealType<U>> implements
		Computers.Arity4<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, Integer, PValueResult> {

	@OpDependency(name = "coloc.pValue")
	private Computers.Arity6<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, Integer, Dimensions, Long, PValueResult> pValueOp;

	/**
	 * TODO
	 *
	 * @param image1 the first image
	 * @param image2 the second image
	 * @param op the op
	 * @param nrRandomizations the number of randomizations
	 * @param output the result
	 */
	@Override
	public void compute(RandomAccessibleInterval<T> image1, RandomAccessibleInterval<U> image2,
			BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double> op, Integer nrRandomizations, PValueResult output) {
		Long defaultSeed = 0x27372034L;
		pValueOp.compute(image1, image2, op, nrRandomizations, null, defaultSeed, output);
	}

}

/**
 *@implNote op names='coloc.pValue'
 */
class PValueSimple<T extends RealType<T>, U extends RealType<U>> implements
		Computers.Arity3<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, PValueResult> {

	@OpDependency(name = "coloc.pValue")
	private Computers.Arity4<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, Integer, PValueResult> pValueOp;

	/**
	 * TODO
	 *
	 * @param image1 the first image
	 * @param image2 the second image
	 * @param op the op
	 * @param output the result
	 */
	@Override
	public void compute(RandomAccessibleInterval<T> image1, RandomAccessibleInterval<U> image2,
			BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double> op,
			PValueResult output) {
		Integer defaultNumberRandomizations = 100;
		pValueOp.compute(image1, image2, op, defaultNumberRandomizations, output);
	}

}
