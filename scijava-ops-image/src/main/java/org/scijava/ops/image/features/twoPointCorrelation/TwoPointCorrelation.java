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

package org.scijava.ops.image.features.twoPointCorrelation;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Computes the radially averaged two-point correlation function
 * {@code S2(r)} of a binary n-D image (Berryman &amp; Blair 1986; Torquato
 * 2002).
 * <p>
 * {@code S2(r)} is the probability that two points a distance {@code r}
 * apart both lie in the foreground phase. For a displacement vector
 * {@code v}, the autocorrelation {@code C(v) = Σ f(x) f(x + v)} of the
 * indicator image {@code f} counts the foreground pairs, and
 * {@code N(v) = Π (d_i - |v_i|)} counts all pixel pairs with that
 * displacement inside the image, so no periodicity is assumed. {@code C} is
 * computed with the FFT on a zero-padded image. For each integer radius
 * {@code r}, the result pools all displacements with {@code round(|v|) = r}:
 * {@code S2(r) = Σ C(v) / Σ N(v)}.
 * </p>
 * <p>
 * {@code S2(0)} is the volume fraction {@code φ} of the foreground, and for
 * a statistically homogeneous medium without long-range order,
 * {@code S2(r)} tends to {@code φ²} for large {@code r}. Distances are in
 * pixels.
 * </p>
 * <p>
 * Implemented from the papers.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <B> the binary pixel type
 * @see <a href="https://doi.org/10.1063/1.337244">J. G. Berryman and S. C.
 *      Blair, "Use of digital image analysis to estimate fluid permeability of
 *      porous materials: Application of two-point correlation functions", J.
 *      Appl. Phys. 60(6):1930-1938, 1986</a>
 * @see <a href="https://doi.org/10.1007/978-1-4757-6355-3">S. Torquato,
 *      "Random Heterogeneous Materials", Springer, 2002</a>
 * @implNote op names='features.twoPointCorrelation'
 */
public class TwoPointCorrelation<B extends BooleanType<B>> implements
	BiFunction<RandomAccessibleInterval<B>, Integer, double[]>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createImg;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, ComplexDoubleType, Boolean, RandomAccessibleInterval<ComplexDoubleType>> createFFTOutput;

	@OpDependency(name = "filter.fft")
	private Computers.Arity1<RandomAccessibleInterval<DoubleType>, RandomAccessibleInterval<ComplexDoubleType>> fftOp;

	@OpDependency(name = "filter.ifft")
	private Computers.Arity1<RandomAccessibleInterval<ComplexDoubleType>, RandomAccessibleInterval<DoubleType>> ifftOp;

	/**
	 * Computes {@code S2(r)}.
	 *
	 * @param input the binary image
	 * @param maxRadius the largest radius, in pixels (default: half the smallest
	 *          image dimension)
	 * @return the values {@code S2(0), S2(1), ..., S2(maxRadius)}; radii without
	 *         any displacement inside the image yield NaN
	 */
	@Override
	public double[] apply(final RandomAccessibleInterval<B> input,
		@Nullable final Integer maxRadius)
	{
		final var image = Views.zeroMin(input);
		final var n = image.numDimensions();
		final var dims = image.dimensionsAsLongArray();
		var smallest = Long.MAX_VALUE;
		for (final var d : dims)
			smallest = Math.min(smallest, d);
		final int rMax = maxRadius == null ? (int) (smallest / 2) : maxRadius;
		if (rMax < 0) {
			throw new IllegalArgumentException("Maximum radius must be " +
				"non-negative: " + rMax);
		}

		// Zero-pad to at least 2d - 1 per dimension, so that the circular
		// autocorrelation equals the linear one.
		final var minPadded = new long[n];
		for (var d = 0; d < n; d++)
			minPadded[d] = 2 * dims[d] - 1;
		final var padded = new long[n];
		FFTMethods.dimensionsRealToComplexFast(new FinalDimensions(minPadded),
			padded, new long[n]);
		final var paddedDims = new FinalDimensions(padded);
		final var f = createImg.apply(paddedDims, new DoubleType());
		LoopBuilder.setImages(image, Views.interval(f, image)).forEachPixel((in,
			out) -> out.set(in.get() ? 1 : 0));
		final var fft = createFFTOutput.apply(paddedDims, new ComplexDoubleType(),
			true);
		fftOp.compute(f, fft);
		for (final var c : Views.iterable(fft)) {
			final var re = c.getRealDouble();
			final var im = c.getImaginaryDouble();
			c.set(re * re + im * im, 0);
		}
		final var autocorrelation = createImg.apply(paddedDims, new DoubleType());
		ifftOp.compute(fft, autocorrelation);

		final var pairs = new double[rMax + 1];
		final var total = new double[rMax + 1];
		final var cursor = Views.iterable(autocorrelation).localizingCursor();
		final var pos = new long[n];
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.localize(pos);
			var r2 = 0.0;
			var overlap = 1.0;
			for (var d = 0; d < n; d++) {
				// Note: Index p holds displacement p, or p - padded for the upper
				// half (negative displacements).
				final var v = pos[d] < dims[d] ? pos[d] : pos[d] - padded[d];
				final var a = Math.abs(v);
				if (a >= dims[d]) {
					overlap = 0;
					break;
				}
				overlap *= dims[d] - a;
				r2 += (double) v * v;
			}
			if (overlap == 0) continue;
			final var r = Math.round(Math.sqrt(r2));
			if (r > rMax) continue;
			pairs[(int) r] += cursor.get().get();
			total[(int) r] += overlap;
		}
		final var s2 = new double[rMax + 1];
		for (var r = 0; r <= rMax; r++)
			s2[r] = total[r] > 0 ? Math.max(0, pairs[r]) / total[r] : Double.NaN;
		return s2;
	}
}
