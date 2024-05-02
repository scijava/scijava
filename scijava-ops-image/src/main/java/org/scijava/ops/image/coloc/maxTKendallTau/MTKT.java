/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.coloc.maxTKendallTau;

import java.util.Collections;
import java.util.Random;
import java.util.function.Function;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.collections.IntArray;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.image.coloc.ColocUtil;
import org.scijava.ops.image.coloc.IntArraySorter;
import org.scijava.ops.image.coloc.MergeSort;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * This algorithm calculates Maximum Truncated Kendall Tau (MTKT) from Wang et
 * al. (2017); computes thresholds using Otsu method.
 *
 * @param <T> Type of the first image
 * @param <U> Type of the second image
 * @author Ellen T Arena
 * @author Shulei Wang
 * @author Curtis Rueden
 * @implNote op names='coloc.maxTKendallTau'
 */
public class MTKT<T extends RealType<T>, U extends RealType<U>> implements
	Functions.Arity3<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Long, Double>
{

	@OpDependency(name = "image.histogram")
	private Function<Iterable<T>, Histogram1d<T>> histogramOpT;
	@OpDependency(name = "image.histogram")
	private Function<Iterable<U>, Histogram1d<U>> histogramOpU;

	@OpDependency(name = "threshold.otsu")
	private Computers.Arity1<Histogram1d<T>, T> thresholdOpT;
	@OpDependency(name = "threshold.otsu")
	private Computers.Arity1<Histogram1d<U>, U> thresholdOpU;

	/**
	 * TODO
	 *
	 * @param image1
	 * @param image2
	 * @param seed
	 * @return the output
	 */
	@Override
	public Double apply(final RandomAccessibleInterval<T> image1,
		final RandomAccessibleInterval<U> image2, @Nullable Long seed)
	{
		// check image sizes
		// TODO: Add these checks to conforms().
		if (!Intervals.equalDimensions(image1, image2)) {
			throw new IllegalArgumentException("Image dimensions do not match");
		}
		final long n1 = Intervals.numElements(image1);
		if (n1 > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image dimensions too large: " + n1);
		}
		final int n = (int) n1;

		// Check nullable seed
		if (seed == null) {
			// NB the original seed used in ImageJ Ops was an integer, 0x893023421.
			// To preserve the same value without casting, we need this value.
			seed = 0xffff_ffff_8930_2341L;
		}

		// compute thresholds
		final double thresh1 = thresholdT(image1);
		final double thresh2 = thresholdU(image2);

		double[][] rank = rankTransformation(image1, image2, thresh1, thresh2, n,
			seed);

		double maxtau = calculateMaxKendallTau(rank, thresh1, thresh2, n);
		return maxtau;
	}

	double thresholdT(final RandomAccessibleInterval<T> image) {
		final Histogram1d<T> histogram = histogramOpT.apply(Views.iterable(image));
		final T result = Util.getTypeFromInterval(image).createVariable();
		thresholdOpT.compute(histogram, result);
		return result.getRealDouble();
	}

	double thresholdU(final RandomAccessibleInterval<U> image) {
		final Histogram1d<U> histogram = histogramOpU.apply(Views.iterable(image));
		final U result = Util.getTypeFromInterval(image).createVariable();
		thresholdOpU.compute(histogram, result);
		return result.getRealDouble();
	}

	static <T extends RealType<T>, U extends RealType<U>> double[][]
		rankTransformation(final RandomAccessibleInterval<T> image1,
			final RandomAccessibleInterval<U> image2, final double thres1,
			final double thres2, final int n, long seed)
	{
		// FIRST...
		final int[] rankIndex1 = rankSamples(image1, seed);
		final int[] rankIndex2 = rankSamples(image2, seed);

		IntArray validIndex = new IntArray(new int[n]);
		validIndex.setSize(0);
		for (int i = 0; i < n; i++) {
			if (rankIndex1[i] >= thres1 && rankIndex2[i] >= thres2) {
				validIndex.addValue(i);
			}
		}
		int rn = validIndex.size();
		double[][] finalRanks = new double[rn][2];
		for (int i = 0; i < rn; i++) {
			final int index = validIndex.getValue(i);
			finalRanks[i][0] = rankIndex1[index];
			finalRanks[i][1] = rankIndex2[index];
		}
		return finalRanks;
	}

	private static <V extends RealType<V>> int[] rankSamples(
		RandomAccessibleInterval<V> image, long seed)
	{
		final long elementCount = Intervals.numElements(image);
		if (elementCount > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image dimensions too large: " +
				elementCount);
		}
		final int n = (int) elementCount;

		// NB: Initialize rank index in random order, to ensure random tie-breaking.
		final int[] rankIndex = new int[n];
		for (int i = 0; i < n; i++) {
			rankIndex[i] = i;
		}
		Random r = new Random(seed);
		ColocUtil.shuffle(rankIndex, r);

		final V a = Util.getTypeFromInterval(image).createVariable();
		final RandomAccess<V> ra = image.randomAccess();
		Collections.sort(new IntArray(rankIndex), (indexA, indexB) -> {
			IntervalIndexer.indexToPosition(indexA, image, ra);
			a.set(ra.get());
			IntervalIndexer.indexToPosition(indexB, image, ra);
			final V b = ra.get();
			return a.compareTo(b);
		});
		return rankIndex;
	}

	static double calculateMaxKendallTau(final double[][] rank,
		final double thresholdRank1, final double thresholdRank2, final int n)
	{
		final int rn = rank.length;
		int an;
		final double step = 1 + 1.0 / Math.log(Math.log(n)); /// ONE PROBLEM IS HERE
																													/// - STEP SIZE IS
																													/// PERHAPS TOO
																													/// SMALL??
		double tempOff1 = 1;
		double tempOff2;
		IntArray activeIndex = new IntArray();
		double sdTau;
		double kendallTau;
		double normalTau;
		double maxNormalTau = Double.MIN_VALUE;

		while (tempOff1 * step + thresholdRank1 < n) {
			tempOff1 *= step;
			tempOff2 = 1;
			while (tempOff2 * step + thresholdRank2 < n) {
				tempOff2 *= step;

				activeIndex.setSize(0);
				for (int i = 0; i < rn; i++) {
					if (rank[i][0] >= n - tempOff1 && rank[i][1] >= n - tempOff2) {
						activeIndex.addValue(i);
					}
				}
				an = activeIndex.size();
				if (an > 1) {
					kendallTau = calculateKendallTau(rank, activeIndex);
					sdTau = Math.sqrt(2.0 * (2 * an + 5) / 9 / an / (an - 1));
					normalTau = kendallTau / sdTau;
				}
				else {
					normalTau = Double.MIN_VALUE;
				}
				if (normalTau > maxNormalTau) maxNormalTau = normalTau;
			}
		}
		return maxNormalTau;
	}

	static double calculateKendallTau(final double[][] rank,
		final IntArray activeIndex)
	{
		final int an = activeIndex.size();

		int indicator = 0;
		final double[][] partRank = new double[2][an];
		for (final Integer i : activeIndex) {
			partRank[0][indicator] = rank[i][0];
			partRank[1][indicator] = rank[i][1];
			indicator++;
		}
		final double[] partRank1 = partRank[0];
		final double[] partRank2 = partRank[1];

		final int[] index = new int[an];
		for (int i = 0; i < an; i++) {
			index[i] = i;
		}

		IntArraySorter.sort(index, (a, b) -> Double.compare(partRank1[a],
			partRank1[b]));

		final MergeSort mergeSort = new MergeSort(index, (a, b) -> Double.compare(
			partRank2[a], partRank2[b]));

		final long n0 = an * (long) (an - 1) / 2;
		final long S = mergeSort.sort();
		return (n0 - 2 * S) / (double) n0;
	}

}
