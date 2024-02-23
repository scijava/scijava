/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image.coloc.saca;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.Localizable;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.util.Localizables;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.GenericComposite;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Helper class for Spatially Adaptive Colocalization Analysis (SACA) op.
 *
 * @author Shulei Wang
 * @author Ellen TA Dobson
 */

public final class AdaptiveSmoothedKendallTau {

	private AdaptiveSmoothedKendallTau() {}

	// TODO: check that output float type is actually what we want here.
	public static <I extends RealType<I>> RandomAccessibleInterval<DoubleType>
		execute(final RandomAccessibleInterval<I> image1,
			final RandomAccessibleInterval<I> image2, final I thres1, final I thres2,
			final long seed)
	{
		final long nr = image1.dimension(1);
		final long nc = image1.dimension(0);
		final ImgFactory<DoubleType> factory = Util.getSuitableImgFactory(image1,
			new DoubleType());
		final RandomAccessibleInterval<DoubleType> oldtau = factory.create(image1);
		final RandomAccessibleInterval<DoubleType> newtau = factory.create(image1);
		final RandomAccessibleInterval<DoubleType> oldsqrtN = factory.create(
			image1);
		final RandomAccessibleInterval<DoubleType> newsqrtN = factory.create(
			image1);
		final RandomAccessibleInterval<DoubleType> result = factory.create(image1);
		final List<RandomAccessibleInterval<DoubleType>> stop = new ArrayList<>();
		final double Dn = Math.sqrt(Math.log(nr * nc)) * 2;
		final int TU = 15;
		final int TL = 8;
		final double Lambda = Dn;
		final double stepsize = 1.15;
		final Random rng = new Random(seed);
		boolean isCheck = false;
		double size = 1;
		int intSize;

		for (int s = 0; s < 3; s++)
			stop.add(factory.create(image1));

		LoopBuilder.setImages(oldsqrtN).forEachPixel(t -> t.setOne());

		for (int s = 0; s < TU; s++) {
			intSize = (int) Math.floor(size);
			singleiteration(image1, image2, thres1, thres2, stop, oldtau, oldsqrtN,
				newtau, newsqrtN, result, Lambda, Dn, intSize, isCheck, rng);
			size *= stepsize;
			if (s == TL) {
				isCheck = true;
				LoopBuilder.setImages(stop.get(1), stop.get(2), newtau, newsqrtN)
					.forEachPixel((ts1, ts2, tTau, tSqrtN) -> {
						ts1.set(tTau);
						ts2.set(tSqrtN);
					});
			}
		}
		// get factory and create imgs
		return result;
	}

	private static <I extends RealType<I>> void singleiteration(
		final RandomAccessibleInterval<I> image1,
		final RandomAccessibleInterval<I> image2, final I thres1, final I thres2,
		final List<RandomAccessibleInterval<DoubleType>> stop,
		final RandomAccessibleInterval<DoubleType> oldtau,
		final RandomAccessibleInterval<DoubleType> oldsqrtN,
		final RandomAccessibleInterval<DoubleType> newtau,
		final RandomAccessibleInterval<DoubleType> newsqrtN,
		final RandomAccessibleInterval<DoubleType> result, final double Lambda,
		final double Dn, final int Bsize, final boolean isCheck, final Random rng)
	{
		final double[][] kernel = kernelGenerate(Bsize);

		final long[] rowrange = new long[4];
		final long[] colrange = new long[4];
		final int totnum = (2 * Bsize + 1) * (2 * Bsize + 1);
		final double[] LocX = new double[totnum];
		final double[] LocY = new double[totnum];
		final double[] LocW = new double[totnum];
		final double[][] combinedData = new double[totnum][3];
		final int[] rankedindex = new int[totnum];
		final double[] rankedw = new double[totnum];
		final int[] index1 = new int[totnum];
		final int[] index2 = new int[totnum];
		final double[] w1 = new double[totnum];
		final double[] w2 = new double[totnum];
		final double[] cumw = new double[totnum];

		RandomAccessibleInterval<DoubleType> workingImageStack = Views.stack(oldtau,
			newtau, oldsqrtN, newsqrtN, stop.get(0), stop.get(1), stop.get(2));
		CompositeIntervalView<DoubleType, ? extends GenericComposite<DoubleType>> workingImage =
			Views.collapse(workingImageStack);

		IntervalView<Localizable> positions = Views.interval(Localizables
			.randomAccessible(result.numDimensions()), result);
		final long nr = result.dimension(1);
		final long nc = result.dimension(0);
		final RandomAccess<I> gdImage1 = image1.randomAccess();
		final RandomAccess<I> gdImage2 = image2.randomAccess();
		final RandomAccess<DoubleType> gdTau = oldtau.randomAccess();
		final RandomAccess<DoubleType> gdSqrtN = oldsqrtN.randomAccess();
		LoopBuilder.setImages(positions, result, workingImage).forEachPixel((pos,
			resPixel, workingPixel) -> {
			DoubleType oldtauPix = workingPixel.get(0);
			DoubleType newtauPix = workingPixel.get(1);
			DoubleType oldsqrtNPix = workingPixel.get(2);
			DoubleType newsqrtNPix = workingPixel.get(3);
			DoubleType stop0Pix = workingPixel.get(4);
			DoubleType stop1Pix = workingPixel.get(5);
			DoubleType stop2Pix = workingPixel.get(6);
			final long row = pos.getLongPosition(1);
			updateRange(row, Bsize, nr, rowrange);
			if (isCheck) {
				if (stop0Pix.getRealDouble() != 0) {
					return;
				}
			}
			final long col = pos.getLongPosition(0);
			updateRange(col, Bsize, nc, colrange);
			getData(Dn, kernel, gdImage1, gdImage2, gdTau, gdSqrtN, LocX, LocY, LocW,
				rowrange, colrange, totnum);
			newsqrtNPix.setReal(Math.sqrt(NTau(thres1, thres2, LocW, LocX, LocY)));
			if (newsqrtNPix.getRealDouble() <= 0) {
				newtauPix.setZero();
				resPixel.setZero();
			}
			else {
				final double tau = WtKendallTau.calculate(LocX, LocY, LocW,
					combinedData, rankedindex, rankedw, index1, index2, w1, w2, cumw,
					rng);
				newtauPix.setReal(tau);
				resPixel.setReal(tau * newsqrtNPix.getRealDouble() * 1.5);
			}

			if (isCheck) {
				final double taudiff = Math.abs(stop1Pix.getRealDouble() - newtauPix
					.getRealDouble()) * stop2Pix.getRealDouble();
				if (taudiff > Lambda) {
					stop0Pix.setOne();
					newtauPix.set(oldtauPix);
					newsqrtNPix.set(oldsqrtNPix);
				}
			}
		});

		// TODO: instead of copying pixels here, swap oldTau and newTau every time.
		// :-)
		LoopBuilder.setImages(oldtau, newtau, oldsqrtN, newsqrtN).forEachPixel((
			tOldTau, tNewTau, tOldSqrtN, tNewSqrtN) -> {
			tOldTau.set(tNewTau);
			tOldSqrtN.set(tNewSqrtN);
		});
	}

	private static <I extends RealType<I>, T extends RealType<T>> void getData(
		final double Dn, final double[][] w, final RandomAccess<I> i1RA,
		final RandomAccess<I> i2RA, final RandomAccess<T> tau,
		final RandomAccess<T> sqrtN, final double[] sx, final double[] sy,
		final double[] sw, final long[] rowrange, final long[] colrange,
		final int totnum)
	{
		// TODO: Decide if this cast is OK.
		int kernelk = (int) (rowrange[0] - rowrange[2] + rowrange[3]);
		int kernell;
		int index = 0;
		double taudiffabs;

		sqrtN.setPosition(colrange[2], 0);
		sqrtN.setPosition(rowrange[2], 1);
		final double sqrtNValue = sqrtN.get().getRealDouble();

		for (long k = rowrange[0]; k <= rowrange[1]; k++) {
			i1RA.setPosition(k, 1);
			i2RA.setPosition(k, 1);
			sqrtN.setPosition(k, 1);
			// TODO: Double check cast.
			kernell = (int) (colrange[0] - colrange[2] + colrange[3]);
			for (long l = colrange[0]; l <= colrange[1]; l++) {
				i1RA.setPosition(l, 0);
				i2RA.setPosition(l, 0);
				sqrtN.setPosition(l, 0);
				sx[index] = i1RA.get().getRealDouble();
				sy[index] = i2RA.get().getRealDouble();
				sw[index] = w[kernelk][kernell];

				tau.setPosition(l, 0);
				tau.setPosition(k, 1);
				final double tau1 = tau.get().getRealDouble();

				tau.setPosition(colrange[2], 0);
				tau.setPosition(rowrange[2], 1);
				final double tau2 = tau.get().getRealDouble();

				taudiffabs = Math.abs(tau1 - tau2) * sqrtNValue;
				taudiffabs = taudiffabs / Dn;
				if (taudiffabs < 1) sw[index] = sw[index] * (1 - taudiffabs) * (1 -
					taudiffabs);
				else sw[index] = sw[index] * 0;
				kernell++;
				index++;
			}
			kernelk++;
		}
		while (index < totnum) {
			sx[index] = 0;
			sy[index] = 0;
			sw[index] = 0;
			index++;
		}
	}

	private static void updateRange(final long location, final int radius,
		final long boundary, final long[] range)
	{
		range[0] = location - radius;
		if (range[0] < 0) range[0] = 0;
		range[1] = location + radius;
		if (range[1] >= boundary) range[1] = boundary - 1;
		range[2] = location;
		range[3] = radius;
	}

	private static <I extends RealType<I>> double NTau(final I thres1,
		final I thres2, final double[] w, final double[] x, final double[] y)
	{
		double sumW = 0;
		double sumsqrtW = 0;
		double tempW;

		for (int index = 0; index < w.length; index++) {
			if (x[index] < thres1.getRealDouble() || y[index] < thres2
				.getRealDouble()) w[index] = 0;
			tempW = w[index];
			sumW += tempW;
			tempW = tempW * w[index];
			sumsqrtW += tempW;
		}
		double NW;
		final double Denomi = sumW * sumW;
		if (Denomi <= 0) {
			NW = 0;
		}
		else {
			NW = Denomi / sumsqrtW;
		}
		return NW;
	}

	private static double[][] kernelGenerate(final int size) {
		final int L = size * 2 + 1;
		final double[][] kernel = new double[L][L];
		final int center = size;
		double temp;
		final double Rsize = size * Math.sqrt(2.5);

		for (int i = 0; i <= size; i++) {
			for (int j = 0; j <= size; j++) {
				temp = Math.sqrt(i * i + j * j) / Rsize;
				if (temp >= 1) temp = 0;
				else temp = 1 - temp;
				kernel[center + i][center + j] = temp;
				kernel[center - i][center + j] = temp;
				kernel[center + i][center - j] = temp;
				kernel[center - i][center - j] = temp;
			}
		}
		return kernel;
	}
}
