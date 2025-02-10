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

import org.scijava.progress.Progress;

/**
 * Helper class for Spatially Adaptive Colocalization Analysis (SACA) framework.
 * This class is used by the "coloc.saca.heatmapZScore" Op to produce the
 * Z-score heatmap of pixel colocalization strength.
 *
 * @author Shulei Wang
 * @author Ellen TA Dobson
 */

public final class AdaptiveSmoothedKendallTau {

	private AdaptiveSmoothedKendallTau() {}

	public static <I extends RealType<I>> void execute(
		final RandomAccessibleInterval<I> image1,
		final RandomAccessibleInterval<I> image2,
		final RandomAccessibleInterval<DoubleType> result, final I thres1,
		final I thres2, final long seed)
	{
		final var nr = image1.dimension(1);
		final var nc = image1.dimension(0);
		final var factory = Util.getSuitableImgFactory(image1,
			new DoubleType());
		final RandomAccessibleInterval<DoubleType> oldtau = factory.create(image1);
		final RandomAccessibleInterval<DoubleType> newtau = factory.create(image1);
		final RandomAccessibleInterval<DoubleType> oldsqrtN = factory.create(
			image1);
		final RandomAccessibleInterval<DoubleType> newsqrtN = factory.create(
			image1);
		final List<RandomAccessibleInterval<DoubleType>> stop = new ArrayList<>();
		final var Dn = Math.sqrt(Math.log(nr * nc)) * 2;
		final var TU = 15;
		final var TL = 8;
		final var Lambda = Dn;
		final var stepsize = 1.15;
		final var rng = new Random(seed);
        var isCheck = false;
		double size = 1;
		int intSize;

		for (var s = 0; s < 3; s++)
			stop.add(factory.create(image1));

		LoopBuilder.setImages(oldsqrtN).multiThreaded().forEachPixel(t -> t
			.setOne());
		Progress.defineTotal(TU);
		for (var s = 0; s < TU; s++) {
			intSize = (int) Math.floor(size);
			singleiteration(image1, image2, thres1, thres2, stop, oldtau, oldsqrtN,
				newtau, newsqrtN, result, Lambda, Dn, intSize, isCheck, rng);
			size *= stepsize;
			if (s == TL) {
				isCheck = true;
				LoopBuilder.setImages(stop.get(1), stop.get(2), newtau, newsqrtN)
					.multiThreaded().forEachPixel((ts1, ts2, tTau, tSqrtN) -> {
						ts1.set(tTau);
						ts2.set(tSqrtN);
					});
			}
			Progress.update();
		}
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
		final var kernel = kernelGenerate(Bsize);

        var workingImageStack = Views.stack(oldtau,
			newtau, oldsqrtN, newsqrtN, stop.get(0), stop.get(1), stop.get(2));
        var workingImage =
			Views.collapse(workingImageStack);

        var positions = Views.interval(Localizables
			.randomAccessible(result.numDimensions()), result);
		LoopBuilder.setImages(positions, result, workingImage).forEachChunk(
			chunk -> {
				final var rowrange = new long[4];
				final var colrange = new long[4];
				final var totnum = (2 * Bsize + 1) * (2 * Bsize + 1);
				final var LocX = new double[totnum];
				final var LocY = new double[totnum];
				final var LocW = new double[totnum];
				final var combinedData = new double[totnum][3];
				final var rankedindex = new int[totnum];
				final var rankedw = new double[totnum];
				final var index1 = new int[totnum];
				final var index2 = new int[totnum];
				final var w1 = new double[totnum];
				final var w2 = new double[totnum];
				final var cumw = new double[totnum];
				final var nr = result.dimension(1);
				final var nc = result.dimension(0);
				final var gdImage1 = image1.randomAccess();
				final var gdImage2 = image2.randomAccess();
				final var gdTau = oldtau.randomAccess();
				final var gdSqrtN = oldsqrtN.randomAccess();
				chunk.forEachPixel((pos, resPixel, workingPixel) -> {
                    var oldtauPix = workingPixel.get(0);
                    var newtauPix = workingPixel.get(1);
                    var oldsqrtNPix = workingPixel.get(2);
                    var newsqrtNPix = workingPixel.get(3);
                    var stop0Pix = workingPixel.get(4);
                    var stop1Pix = workingPixel.get(5);
                    var stop2Pix = workingPixel.get(6);
					final var row = pos.getLongPosition(1);
					updateRange(row, Bsize, nr, rowrange);
					if (isCheck) {
						if (stop0Pix.getRealDouble() != 0) {
							return;
						}
					}
					final var col = pos.getLongPosition(0);
					updateRange(col, Bsize, nc, colrange);
					getData(Dn, kernel, gdImage1, gdImage2, gdTau, gdSqrtN, LocX, LocY,
						LocW, rowrange, colrange, totnum);
					newsqrtNPix.setReal(Math.sqrt(NTau(thres1, thres2, LocW, LocX,
						LocY)));
					if (newsqrtNPix.getRealDouble() <= 0) {
						newtauPix.setZero();
						resPixel.setZero();
					}
					else {
						final var tau = WtKendallTau.calculate(LocX, LocY, LocW,
							combinedData, rankedindex, rankedw, index1, index2, w1, w2, cumw,
							rng);
						newtauPix.setReal(tau);
						resPixel.setReal(tau * newsqrtNPix.getRealDouble() * 1.5);
					}

					if (isCheck) {
						final var taudiff = Math.abs(stop1Pix.getRealDouble() - newtauPix
							.getRealDouble()) * stop2Pix.getRealDouble();
						if (taudiff > Lambda) {
							stop0Pix.setOne();
							newtauPix.set(oldtauPix);
							newsqrtNPix.set(oldsqrtNPix);
						}
					}
				});
				return null;
			});

		// TODO: instead of copying pixels here, swap oldTau and newTau every time.
		LoopBuilder.setImages(oldtau, newtau, oldsqrtN, newsqrtN).multiThreaded()
			.forEachPixel((tOldTau, tNewTau, tOldSqrtN, tNewSqrtN) -> {
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
        var kernelk = (int) (rowrange[0] - rowrange[2] + rowrange[3]);
		int kernell;
        var index = 0;
		double taudiffabs;

		sqrtN.setPosition(colrange[2], 0);
		sqrtN.setPosition(rowrange[2], 1);
		final var sqrtNValue = sqrtN.get().getRealDouble();

		for (var k = rowrange[0]; k <= rowrange[1]; k++) {
			i1RA.setPosition(k, 1);
			i2RA.setPosition(k, 1);
			sqrtN.setPosition(k, 1);
			kernell = (int) (colrange[0] - colrange[2] + colrange[3]);
			for (var l = colrange[0]; l <= colrange[1]; l++) {
				i1RA.setPosition(l, 0);
				i2RA.setPosition(l, 0);
				sqrtN.setPosition(l, 0);
				sx[index] = i1RA.get().getRealDouble();
				sy[index] = i2RA.get().getRealDouble();
				sw[index] = w[kernelk][kernell];

				tau.setPosition(l, 0);
				tau.setPosition(k, 1);
				final var tau1 = tau.get().getRealDouble();

				tau.setPosition(colrange[2], 0);
				tau.setPosition(rowrange[2], 1);
				final var tau2 = tau.get().getRealDouble();

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

		for (var index = 0; index < w.length; index++) {
			if (x[index] < thres1.getRealDouble() || y[index] < thres2
				.getRealDouble()) w[index] = 0;
			tempW = w[index];
			sumW += tempW;
			tempW = tempW * w[index];
			sumsqrtW += tempW;
		}
		double NW;
		final var Denomi = sumW * sumW;
		if (Denomi <= 0) {
			NW = 0;
		}
		else {
			NW = Denomi / sumsqrtW;
		}
		return NW;
	}

	private static double[][] kernelGenerate(final int size) {
		final var L = size * 2 + 1;
		final var kernel = new double[L][L];
		final var center = size;
		double temp;
		final var Rsize = size * Math.sqrt(2.5);

		for (var i = 0; i <= size; i++) {
			for (var j = 0; j <= size; j++) {
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
