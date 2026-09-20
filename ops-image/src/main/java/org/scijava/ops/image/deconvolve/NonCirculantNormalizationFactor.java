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

package org.scijava.ops.image.deconvolve;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.OpDependency;

/**
 * Calculate non-circulant normalization factor. This is used as part of the
 * Boundary condition handling scheme described here
 * http://bigwww.epfl.ch/deconvolution/challenge2013/index.html?p=doc_math_rl)
 *
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 * @implNote op names='deconvolve.normalizationFactor', priority='-100.'
 */
public class NonCirculantNormalizationFactor<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, C extends ComplexType<C>>
	implements
	Inplaces.Arity5_1<RandomAccessibleInterval<O>, Dimensions, Dimensions, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>>
{

	/**
	 * k is the size of the measurement window. That is the size of the acquired
	 * image before extension, k is required to calculate the non-circulant
	 * normalization factor
	 */
	private Dimensions k;

	/**
	 * l is the size of the psf, l is required to calculate the non-circulant
	 * normalization factor
	 */
	private Dimensions l;

	private RandomAccessibleInterval<C> fftInput;

	private RandomAccessibleInterval<C> fftKernel;

	// Normalization factor for edge handling (see
	// http://bigwww.epfl.ch/deconvolution/challenge2013/index.html?p=doc_math_rl)
	private Img<O> normalization = null;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, Img<O>> create;

	@OpDependency(name = "filter.correlate")
	private Computers.Arity6<RandomAccessibleInterval<O>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, Boolean, RandomAccessibleInterval<O>> correlater;

//	@OpDependency(name = "math.divide") TODO: match an Op here?
	private BiConsumer<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> divide =
		(numerResult, denom) -> {
			final var tmp = Util.getTypeFromInterval(numerResult).createVariable();
			LoopBuilder.setImages(numerResult, denom).forEachPixel((n, d) -> {
				if (n.getRealFloat() > 0) {
					tmp.set(n);
					tmp.div(d);
					n.set(tmp);
				}
				else n.setZero();
			});
		};

	/**
	 * apply the normalization image needed for semi noncirculant model see
	 * http://bigwww.epfl.ch/deconvolution/challenge2013/index.html?p=doc_math_rl
	 *
	 * @param arg
	 * @param k
	 * @param l
	 * @param fftInput
	 * @param fftKernel
	 */
	@Override
	public void mutate(RandomAccessibleInterval<O> arg, final Dimensions k,
		final Dimensions l, final RandomAccessibleInterval<C> fftInput,
		final RandomAccessibleInterval<C> fftKernel)
	{
		this.k = k;
		this.l = l;
		this.fftInput = fftInput;
		this.fftKernel = fftKernel;

		// if the normalization image hasn't been computed yet, then compute it
		if (normalization == null) {
			this.createNormalizationImageSemiNonCirculant(arg, Util
				.getTypeFromInterval(arg));
		}

		// normalize for non-circulant deconvolution
		// arg = arg / normalization
		divide.accept(arg, normalization);
	}

	protected void createNormalizationImageSemiNonCirculant(
		Interval fastFFTInterval, O type)
	{

		// k is the window size (valid image region)
		final var length = k.numDimensions();

		final var n = new long[length];
		final var nFFT = new long[length];

		// n is the valid image size plus the extended region
		// also referred to as object space size
		for (var d = 0; d < length; d++) {
			n[d] = k.dimension(d) + l.dimension(d) - 1;
		}

		// nFFT is the size of n after (potentially) extending further
		// to a fast FFT size
		for (var d = 0; d < length; d++) {
			nFFT[d] = fastFFTInterval.dimension(d);
		}

        var fd = new FinalDimensions(nFFT);

		// create the normalization image
		normalization = create.apply(fd, type);

		// size of the measurement window
		final var size = new Point(length);
		final var sizel = new long[length];

		for (var d = 0; d < length; d++) {
			size.setPosition(k.dimension(d), d);
			sizel[d] = k.dimension(d);
		}

		// starting point of the measurement window when it is centered in fft space
		final var start = new Point(length);
		final var startl = new long[length];
		final var endl = new long[length];

		for (var d = 0; d < length; d++) {
			start.setPosition((nFFT[d] - k.dimension(d)) / 2, d);
			startl[d] = (nFFT[d] - k.dimension(d)) / 2;
			endl[d] = startl[d] + sizel[d] - 1;
		}

		// size of the object space
		final var maskSize = new Point(length);
		final var maskSizel = new long[length];

		for (var d = 0; d < length; d++) {
			maskSize.setPosition(Math.min(n[d], nFFT[d]), d);
			maskSizel[d] = Math.min(n[d], nFFT[d]);
		}

		// starting point of the object space within the fft space
		final var maskStart = new Point(length);
		final var maskStartl = new long[length];

		for (var d = 0; d < length; d++) {
			maskStart.setPosition((Math.max(0, nFFT[d] - n[d]) / 2), d);
			maskStartl[d] = (Math.max(0, nFFT[d] - n[d]) / 2);
		}

		final RandomAccessibleInterval<O> temp = Views.interval(normalization,
			new FinalInterval(startl, endl));
		final var normCursor = Views.iterable(temp).cursor();

		// draw a cube the size of the measurement space
		while (normCursor.hasNext()) {
			normCursor.fwd();
			normCursor.get().setReal(1.0);
		}

		final var tempImg = create.apply(fd, type);

		// 3. correlate psf with the output of step 2.
		correlater.compute(normalization, null, fftInput, fftKernel, true, false,
			tempImg);

		normalization = tempImg;

		final var cursorN = normalization.cursor();

		while (cursorN.hasNext()) {
			cursorN.fwd();

			if (cursorN.get().getRealFloat() <= 1e-3f) {
				cursorN.get().setReal(1.0f);

			}
		}
	}
}
