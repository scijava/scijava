/*-
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

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

import org.scijava.function.Computers;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * This algorithm repeatedly executes a colocalization algorithm, computing a
 * p-value. It is based on a new statistical framework published by Wang et al
 * (2017) IEEE Signal Processing "Automated and Robust Quantification of
 * Colocalization in Dual-Color Fluorescence Microscopy: A Nonparametric
 * Statistical Approach".
 */
@Plugin(type = Op.class, name = "coloc.pValue")
public class DefaultPValue<T extends RealType<T>, U extends RealType<U>> implements
		Computers.Arity7<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, Integer, Dimensions, Long, ExecutorService, PValueResult> {

	@Override
	/**
	 * TODO
	 *
	 * @param image1
	 * @param image2
	 * @param op
	 * @param nrRandomizations
	 * @param psfSize
	 * @param seed
	 * @param executorService
	 * @param output
	 */
	public void compute(final RandomAccessibleInterval<T> image1, final RandomAccessibleInterval<U> image2,
			final BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double> op,
			final Integer nrRandomizations, final Dimensions psfSize, final Long seed, final ExecutorService es,
			final PValueResult output) {
		final int[] blockSize = blockSize(image1, psfSize);
		final RandomAccessibleInterval<T> trimmedImage1 = trim(image1, blockSize);
		final RandomAccessibleInterval<U> trimmedImage2 = trim(image2, blockSize);
		final T type1 = Util.getTypeFromInterval(image1);

		final double[] sampleDistribution = new double[nrRandomizations];

		// compute actual coloc value
		final double value = op.apply(trimmedImage1, trimmedImage2);

		// compute shuffled coloc values in parallel
		int threadCount = Runtime.getRuntime().availableProcessors(); // FIXME: conform to Ops threading strategy...
		Random r = new Random(seed);
		long[] seeds = new long[nrRandomizations];
		for (int s = 0; s < nrRandomizations; s++) {
			seeds[s] = r.nextLong();
		}
		ArrayList<Future<?>> future = new ArrayList<>(threadCount);
		for (int t = 0; t < threadCount; t++) {
			int offset = t * nrRandomizations / threadCount;
			int count = (t + 1) * nrRandomizations / threadCount - offset;
			future.add(es.submit(() -> {
				final ShuffledView<T> shuffled = new ShuffledView<>(trimmedImage1, blockSize, seeds[offset]); // a new
																												// one
																												// per
																												// thread
																												// and
																												// each
																												// needs
																												// its
																												// own
																												// seed
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
			}));
		}

		// wait for threads to finish
		try {
			for (int t = 0; t < threadCount; t++) {
				future.get(t).get();
			}
		} catch (final InterruptedException | ExecutionException exc) {
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

@Plugin(type = Op.class, name = "coloc.pValue")
class PValueSimpleWithRandomizations<T extends RealType<T>, U extends RealType<U>> implements
		Computers.Arity5<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, Integer, ExecutorService, PValueResult> {

	@OpDependency(name = "coloc.pValue")
	private Computers.Arity7<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, Integer, Dimensions, Long, ExecutorService, PValueResult> pValueOp;

	@Override
	/**
	 * TODO
	 *
	 * @param image1
	 * @param image2
	 * @param op
	 * @param nrRandomizations
	 * @param executorService
	 * @param output
	 */
	public void compute(RandomAccessibleInterval<T> in1, RandomAccessibleInterval<U> in2,
			BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double> in3, Integer in4,
			ExecutorService in5, PValueResult out) {
		Long defaultSeed = 0x27372034l;
		pValueOp.compute(in1, in2, in3, in4, null, defaultSeed, in5, out);
	}

}

@Plugin(type = Op.class, name = "coloc.pValue")
class PValueSimple<T extends RealType<T>, U extends RealType<U>> implements
		Computers.Arity4<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, ExecutorService, PValueResult> {

	@OpDependency(name = "coloc.pValue")
	private Computers.Arity5<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double>, Integer, ExecutorService, PValueResult> pValueOp;

	@Override
	/**
	 * TODO
	 *
	 * @param image1
	 * @param image2
	 * @param op
	 * @param executorService
	 * @param output
	 */
	public void compute(RandomAccessibleInterval<T> in1, RandomAccessibleInterval<U> in2,
			BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double> in3, ExecutorService in4,
			PValueResult out) {
		Integer defaultNumberRandomizations = 100;
		pValueOp.compute(in1, in2, in3, defaultNumberRandomizations, in4, out);
	}

}
