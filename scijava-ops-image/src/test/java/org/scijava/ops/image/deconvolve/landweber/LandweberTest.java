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

package org.scijava.ops.image.deconvolve.landweber;

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
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsPeriodicFactory;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link Landweber}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LandweberTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<FloatType>> FLOAT_RAI =
		new Nil<>()
		{};

	/**
	 * On noise-free data in the circulant model, each frequency component of
	 * the error is multiplied by {@code 1 - tau |H|^2} per iteration, so for a
	 * valid step size the error and the residual decrease monotonically, while
	 * a step size beyond {@code 2 / max|H|^2} diverges.
	 */
	@Test
	public void testMonotoneConvergence() {
		final var size = fastSize(40, 36);
		final var object = randomImage(size, 5);
		final var kernel = gaussKernel(1.5, 2);
		final var blurred = blurCirculant(object, kernel);

		var previousError = Double.POSITIVE_INFINITY;
		var previousResidual = Double.POSITIVE_INFINITY;
		for (var k = 0; k <= 8; k++) {
			final var x = landweberCirculant(blurred, kernel, k, null, false);
			assertArrayEquals(size, x.dimensionsAsLongArray());
			final var error = mse(x, object);
			final var residual = mse(blurCirculant(x, kernel), blurred);
			assertTrue(error < previousError, "error at iteration " + k + ": " +
				error + " >= " + previousError);
			assertTrue(residual < previousResidual, "residual at iteration " + k +
				": " + residual + " >= " + previousResidual);
			previousError = error;
			previousResidual = residual;
		}
		assertEquals(mse(blurred, object), mse(landweberCirculant(blurred, kernel,
			0, null, false), object), 0);

		// Projected Landweber also decreases the residual for tau = 1 / max|H|^2.
		previousResidual = Double.POSITIVE_INFINITY;
		for (var k = 0; k <= 8; k++) {
			final var x = landweberCirculant(blurred, kernel, k, 1.0, true);
			final var residual = mse(blurCirculant(x, kernel), blurred);
			assertTrue(residual < previousResidual, "projected residual at " +
				"iteration " + k + ": " + residual + " >= " + previousResidual);
			previousResidual = residual;
		}

		// Beyond the stability bound tau < 2, the iteration diverges.
		final var initialError = mse(blurred, object);
		final var diverged = mse(landweberCirculant(blurred, kernel, 20, 3.0,
			false), object);
		assertTrue(diverged > 100 * initialError, "diverged: " + diverged);
	}

	/**
	 * The default step size must be stable for a kernel that does not sum to
	 * one, where {@code max|H|} and {@code max|H|^2} differ.
	 */
	@Test
	public void testDefaultStepUnnormalizedKernel() {
		final var size = fastSize(40, 36);
		final var object = randomImage(size, 5);
		final var kernel = gaussKernel(1.5, 2);
		for (final var v : Views.iterable(kernel))
			v.mul(3);
		final var blurred = blurCirculant(object, kernel);

		var previousResidual = Double.POSITIVE_INFINITY;
		for (var k = 0; k <= 8; k++) {
			final var x = landweberCirculant(blurred, kernel, k, null, false);
			final var residual = mse(blurCirculant(x, kernel), blurred);
			assertTrue(residual < previousResidual, "residual at iteration " + k +
				": " + residual + " >= " + previousResidual);
			previousResidual = residual;
		}
	}

	/** Restoration of a noisy, blurred 3-D phantom must reduce the error. */
	@Test
	public void testNoisy3D() {
		final var object = phantom3D();
		final var kernel = gaussKernel(1.5, 3);
		final var blurred = blur(object, kernel);
		addNoise(blurred, 0.01, 17);
		final var blurredError = mse(blurred, object);

		final var restored = ops.op("deconvolve.landweber").input(blurred, kernel,
			30).outType(FLOAT_RAI).apply();
		assertArrayEquals(blurred.dimensionsAsLongArray(), restored
			.dimensionsAsLongArray());
		final var error = mse(restored, object);
		assertTrue(error < 0.8 * blurredError, "landweber: " + error +
			" vs blurred: " + blurredError);
		for (final var v : Views.iterable(restored))
			assertTrue(v.getRealDouble() >= 0);

		// Without the constraint, negative values appear.
		final var unconstrained = ops.op("deconvolve.landweber").input(blurred,
			kernel, 30, null, false).outType(FLOAT_RAI).apply();
		var min = Double.POSITIVE_INFINITY;
		for (final var v : Views.iterable(unconstrained))
			min = Math.min(min, v.getRealDouble());
		assertTrue(min < 0);
		assertTrue(mse(unconstrained, object) < blurredError);
	}

	/**
	 * An object truncated by the image border: non-circulant mode must reduce
	 * the error, and avoid the artifacts of circulant mode with zero padding,
	 * which worsen with the iteration count.
	 */
	@Test
	public void testNonCirculant() {
		final var full = phantom3D();
		final var kernel = gaussKernel(1.5, 3);
		final var fullBlurred = blur(full, kernel);
		// Crop so that the boxes are cut by the border of the measured window.
		final long[] min = { 12, 10, 0 };
		final long[] max = { 31, 29, 19 };
		final RandomAccessibleInterval<FloatType> object = Views.zeroMin(Views
			.interval(full, min, max));
		final var blurred = copy(Views.zeroMin(Views.interval(fullBlurred, min,
			max)));
		final var blurredError = mse(blurred, object);

		final var nc30 = mse(landweber(blurred, kernel, 30, true, null), object);
		final var nc100 = mse(landweber(blurred, kernel, 100, true, null), object);
		final OutOfBoundsFactory<FloatType, RandomAccessibleInterval<FloatType>> zero =
			new OutOfBoundsConstantValueFactory<>(new FloatType());
		final var zero30 = mse(landweber(blurred, kernel, 30, false, zero), object);
		final var zero100 = mse(landweber(blurred, kernel, 100, false, zero),
			object);
		assertTrue(nc30 < 0.8 * blurredError, "non-circulant: " + nc30 +
			" vs blurred: " + blurredError);
		assertTrue(nc100 < nc30, "non-circulant: " + nc100 + " vs " + nc30);
		assertTrue(zero100 > zero30, "zero-padded: " + zero100 + " vs " + zero30);
		assertTrue(nc100 < 0.5 * zero100, "non-circulant: " + nc100 +
			" vs zero-padded: " + zero100);
	}

	/**
	 * With an integer output type, the residual {@code y - h * x} is negative
	 * in places, so it must not be stored in the output type. The result must
	 * match the floating-point result, rounded.
	 */
	@Test
	public void testUnsignedShortOutput() {
		final var object = phantom3D();
		for (final var v : object)
			v.mul(1000);
		final var kernel = gaussKernel(1.5, 3);
		final var blurred = blur(object, kernel);
		final var in = ArrayImgs.unsignedShorts(blurred.dimensionsAsLongArray());
		final var bc = Views.flatIterable(blurred).cursor();
		for (final var v : in)
			v.setReal(Math.round(bc.next().get()));

		final var expected = ops.op("deconvolve.landweber").input(in, kernel,
			new FloatType(), new ComplexFloatType(), 10, null, true, false, null,
			null).outType(FLOAT_RAI).apply();
		final var actual = ops.op("deconvolve.landweber").input(in, kernel,
			new UnsignedShortType(), new ComplexFloatType(), 10, null, true, false,
			null, null).outType(new Nil<RandomAccessibleInterval<UnsignedShortType>>()
			{}).apply();
		final var ec = Views.flatIterable(expected).cursor();
		for (final var v : Views.flatIterable(actual))
			assertEquals(Math.min(65535, ec.next().get()), v.get(), 0.5 + 1e-3);
	}

	@Test
	public void testInvalidArguments() {
		final var img = randomImage(new long[] { 10, 10 }, 1);
		final var kernel = gaussKernel(1.0, 2);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"deconvolve.landweber").input(img, kernel, 5, -1.0).outType(FLOAT_RAI)
			.apply());
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"deconvolve.landweber").input(img, kernel, -1).outType(FLOAT_RAI)
			.apply());
	}

	// -- Helper methods --

	private RandomAccessibleInterval<FloatType> landweber(
		final RandomAccessibleInterval<FloatType> img,
		final RandomAccessibleInterval<FloatType> kernel, final int iterations,
		final boolean nonCirculant,
		final OutOfBoundsFactory<FloatType, RandomAccessibleInterval<FloatType>> obf)
	{
		return ops.op("deconvolve.landweber").input(img, kernel, new FloatType(),
			new ComplexFloatType(), iterations, null, true, nonCirculant, null, obf)
			.outType(FLOAT_RAI).apply();
	}

	private RandomAccessibleInterval<FloatType> landweberCirculant(
		final RandomAccessibleInterval<FloatType> img,
		final RandomAccessibleInterval<FloatType> kernel, final int iterations,
		final Double stepSize, final boolean nonNegative)
	{
		final OutOfBoundsFactory<FloatType, RandomAccessibleInterval<FloatType>> periodic =
			new OutOfBoundsPeriodicFactory<>();
		return ops.op("deconvolve.landweber").input(img, kernel, new FloatType(),
			new ComplexFloatType(), iterations, stepSize, nonNegative, false,
			new long[img.numDimensions()], periodic).outType(FLOAT_RAI).apply();
	}

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

	/** Two overlapping boxes and a bright point. */
	private static Img<FloatType> phantom3D() {
		final var img = ArrayImgs.floats(32, 30, 20);
		final var cursor = img.localizingCursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			final var x = cursor.getIntPosition(0);
			final var y = cursor.getIntPosition(1);
			final var z = cursor.getIntPosition(2);
			var v = 0.0;
			if (x >= 8 && x < 18 && y >= 8 && y < 20 && z >= 6 && z < 12) v += 0.6;
			if (x >= 14 && x < 24 && y >= 12 && y < 22 && z >= 9 && z < 15) v += 0.4;
			if (x == 22 && y == 8 && z == 5) v = 1;
			cursor.get().setReal(v);
		}
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

	private static Img<FloatType> copy(
		final RandomAccessibleInterval<FloatType> img)
	{
		final var result = ArrayImgs.floats(img.dimensionsAsLongArray());
		final var c = Views.flatIterable(img).cursor();
		for (final var v : result)
			v.set(c.next());
		return result;
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
