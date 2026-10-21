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

package org.scijava.ops.image.deconvolve.wiener;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsPeriodicFactory;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link WienerFilter} and {@link TikhonovFilter}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class WienerFilterTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<FloatType>> FLOAT_RAI =
		new Nil<>()
		{};

	private static final Nil<RandomAccessibleInterval<ComplexFloatType>> COMPLEX_RAI =
		new Nil<>()
		{};

	/**
	 * On noise-free data blurred with the same circulant model the op uses,
	 * Wiener with zero regularization is the inverse filter, so the spectrum of
	 * the result must match the spectrum of the object wherever the PSF
	 * transfers signal.
	 */
	@Test
	public void testExactInverseWithinPassband() {
		final var size = fastSize(50, 40);
		final var object = randomImage(size, 1);
		final var kernel = gaussKernel(1.0, 2);
		final var blurred = blurCirculant(object, kernel);

		final var restored = ops.op("deconvolve.wiener").input(blurred, kernel,
			new FloatType(), new ComplexFloatType(), 0.0, new long[2],
			new OutOfBoundsPeriodicFactory<FloatType, RandomAccessibleInterval<FloatType>>())
			.outType(FLOAT_RAI).apply();
		assertArrayEquals(size, restored.dimensionsAsLongArray());

		final var fftObject = fft(object);
		final var fftRestored = fft(restored);
		final RandomAccessibleInterval<FloatType> paddedKernel = ops.op(
			"filter.padShiftKernelFFTMethods").input(kernel, new FinalDimensions(
				size)).outType(FLOAT_RAI).apply();
		final var fftKernel = fft(paddedKernel);

		var maxAbs = 0.0;
		for (final var c : fftObject)
			maxAbs = Math.max(maxAbs, c.getPowerDouble());
		final var tolerance = 1e-4 * maxAbs;
		final var inBand = new long[1];
		final var outOfBand = new long[1];
		final var maxError = new double[1];
		LoopBuilder.setImages(fftObject, fftRestored, fftKernel).forEachPixel((
			o, r, h) -> {
			if (h.getPowerDouble() > 0.1) {
				inBand[0]++;
				final var dr = o.getRealDouble() - r.getRealDouble();
				final var di = o.getImaginaryDouble() - r.getImaginaryDouble();
				maxError[0] = Math.max(maxError[0], Math.sqrt(dr * dr + di * di));
			}
			else outOfBand[0]++;
		});
		assertTrue(inBand[0] > 100, "passband samples: " + inBand[0]);
		assertTrue(outOfBand[0] > 100, "stopband samples: " + outOfBand[0]);
		assertTrue(maxError[0] < tolerance, "max spectral error " + maxError[0] +
			" exceeds " + tolerance);
	}

	/** Restoration of a noisy, blurred 3-D phantom must reduce the error. */
	@Test
	public void testNoisy3D() {
		final var object = phantom3D();
		final var kernel = gaussKernel(1.5, 3);
		final var blurred = blur(object, kernel);
		addNoise(blurred, 0.01, 42);
		final var blurredError = mse(blurred, object);

		final var wiener = ops.op("deconvolve.wiener").input(blurred, kernel, 1e-2)
			.outType(FLOAT_RAI).apply();
		final var wienerError = mse(wiener, object);
		assertTrue(wienerError < 0.8 * blurredError, "wiener: " + wienerError +
			" vs blurred: " + blurredError);

		final var tikhonov = ops.op("deconvolve.tikhonov").input(blurred, kernel,
			1e-3, 2).outType(FLOAT_RAI).apply();
		final var tikhonovError = mse(tikhonov, object);
		assertTrue(tikhonovError < 0.8 * blurredError, "tikhonov: " +
			tikhonovError + " vs blurred: " + blurredError);

		// Note: Without regularization, the inverse filter amplifies the noise.
		final var inverse = ops.op("deconvolve.wiener").input(blurred, kernel, 0.0)
			.outType(FLOAT_RAI).apply();
		assertTrue(mse(inverse, object) > blurredError);
	}

	/** Zeroth-order Tikhonov regularization is the constant-NSR Wiener filter. */
	@Test
	public void testTikhonovOrderZeroIsWiener() {
		final var object = randomImage(new long[] { 30, 24 }, 7);
		final var kernel = gaussKernel(2.0, 2);
		final var blurred = blur(object, kernel);
		final var wiener = ops.op("deconvolve.wiener").input(blurred, kernel,
			5e-3).outType(FLOAT_RAI).apply();
		final var tikhonov0 = ops.op("deconvolve.tikhonov").input(blurred, kernel,
			5e-3, 0).outType(FLOAT_RAI).apply();
		final var tikhonov2 = ops.op("deconvolve.tikhonov").input(blurred, kernel,
			5e-3, 2).outType(FLOAT_RAI).apply();
		assertEquals(0, mse(wiener, tikhonov0), 0);
		assertTrue(mse(wiener, tikhonov2) > 1e-6);
	}

	/** Defaults of the simple and full signatures agree. */
	@Test
	public void testDefaults() {
		final var object = randomImage(new long[] { 20, 22 }, 3);
		final var kernel = gaussKernel(1.5, 2);
		final var blurred = blur(object, kernel);
		final var simple = ops.op("deconvolve.wiener").input(blurred, kernel)
			.outType(FLOAT_RAI).apply();
		final var full = ops.op("deconvolve.wiener").input(blurred, kernel,
			new FloatType(), new ComplexFloatType()).outType(FLOAT_RAI).apply();
		final var explicit = ops.op("deconvolve.wiener").input(blurred, kernel,
			WienerFilter.DEFAULT_NSR).outType(FLOAT_RAI).apply();
		assertEquals(0, mse(simple, full), 0);
		assertEquals(0, mse(simple, explicit), 0);

		final var tSimple = ops.op("deconvolve.tikhonov").input(blurred, kernel)
			.outType(FLOAT_RAI).apply();
		final var tExplicit = ops.op("deconvolve.tikhonov").input(blurred, kernel,
			TikhonovFilter.DEFAULT_LAMBDA, TikhonovFilter.DEFAULT_ORDER).outType(
				FLOAT_RAI).apply();
		assertEquals(0, mse(tSimple, tExplicit), 0);

		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"deconvolve.tikhonov").input(blurred, kernel, 1e-3, 3).outType(FLOAT_RAI)
			.apply());
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"deconvolve.wiener").input(blurred, kernel, -1.0).outType(FLOAT_RAI)
			.apply());
	}

	// -- Helper methods --

	/** Rounds each size up to a size the FFT ops use without further padding. */
	private static long[] fastSize(final long... size) {
		final var padded = new long[size.length];
		FFTMethods.dimensionsRealToComplexFast(new FinalDimensions(size), padded,
			new long[size.length]);
		return padded;
	}

	private static Img<FloatType> randomImage(final long[] size,
		final long seed)
	{
		final var img = ArrayImgs.floats(size);
		final var random = new Random(seed);
		for (final var v : img)
			v.setReal(random.nextDouble());
		return img;
	}

	/** Two overlapping boxes and a bright point, away from the borders. */
	private static Img<FloatType> phantom3D() {
		final var img = ArrayImgs.floats(32, 30, 20);
		final var ra = img.randomAccess();
		final var cursor = img.localizingCursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			final var x = cursor.getIntPosition(0);
			final var y = cursor.getIntPosition(1);
			final var z = cursor.getIntPosition(2);
			var v = 0.0;
			if (x >= 8 && x < 18 && y >= 8 && y < 20 && z >= 6 && z < 12) v += 0.6;
			if (x >= 14 && x < 24 && y >= 12 && y < 22 && z >= 9 && z < 15) v += 0.4;
			cursor.get().setReal(v);
		}
		ra.setPosition(new int[] { 22, 8, 5 });
		ra.get().setReal(1);
		return img;
	}

	private RandomAccessibleInterval<FloatType> gaussKernel(final double sigma,
		final int numDims)
	{
		return ops.op("create.kernelGauss").input(sigma, numDims, new FloatType())
			.outType(FLOAT_RAI).apply();
	}

	/** Linear convolution, with zero outside the image. */
	private RandomAccessibleInterval<FloatType> blur(
		final RandomAccessibleInterval<FloatType> img,
		final RandomAccessibleInterval<FloatType> kernel)
	{
		return ops.op("filter.convolve").input(img, kernel, new FloatType(),
			new ComplexFloatType(), null,
			new OutOfBoundsConstantValueFactory<FloatType, RandomAccessibleInterval<FloatType>>(
				new FloatType())).outType(FLOAT_RAI).apply();
	}

	/** Circular convolution, for images of a fast FFT size. */
	private RandomAccessibleInterval<FloatType> blurCirculant(
		final RandomAccessibleInterval<FloatType> img,
		final RandomAccessibleInterval<FloatType> kernel)
	{
		return ops.op("filter.convolve").input(img, kernel, new FloatType(),
			new ComplexFloatType(), new long[img.numDimensions()],
			new OutOfBoundsPeriodicFactory<FloatType, RandomAccessibleInterval<FloatType>>())
			.outType(FLOAT_RAI).apply();
	}

	private RandomAccessibleInterval<ComplexFloatType> fft(
		final RandomAccessibleInterval<FloatType> img)
	{
		return ops.op("filter.fft").input(img, new ComplexFloatType(), null, true)
			.outType(COMPLEX_RAI).apply();
	}

	private static void addNoise(final RandomAccessibleInterval<FloatType> img,
		final double sigma, final long seed)
	{
		final var random = new Random(seed);
		for (final var v : Views.flatIterable(img))
			v.setReal(v.getRealDouble() + sigma * random.nextGaussian());
	}

	private static double mse(final RandomAccessibleInterval<FloatType> a,
		final RandomAccessibleInterval<FloatType> b)
	{
		final var ca = Views.flatIterable(a).cursor();
		final var cb = Views.flatIterable(b).cursor();
		var sum = 0.0;
		long count = 0;
		while (ca.hasNext()) {
			final var d = ca.next().getRealDouble() - cb.next().getRealDouble();
			sum += d * d;
			count++;
		}
		return sum / count;
	}
}
