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

package org.scijava.ops.image.filter.phaseCorrelation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultPhaseCorrelate}.
 * <p>
 * The test images sample a texture of random Gaussian blobs, which can be
 * evaluated at any real position, so that subpixel shifts are exact.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultPhaseCorrelateTest extends AbstractOpTest {

	@Test
	public void testIntegerShift2D() {
		final var texture = new Texture(2, 120, 64, 1);
		final double[] shift = { 7, -4 };
		final var result = correlate(texture.sample(new long[] { 64, 64 },
			new double[2], 0.05, 2), texture.sample(new long[] { 64, 64 }, shift,
				0.05, 3), null);
		assertArrayEquals(shift, result.getShift(), 0.1, result.toString());
		assertTrue(result.getCorrelation() > 0.9, result.toString());
		assertEquals((64 - 7) * (64 - 4), result.getOverlap());
	}

	@Test
	public void testIntegerShiftWithoutSubpixel() {
		final var texture = new Texture(2, 120, 64, 4);
		final double[] shift = { -11, 13 };
		final var result = correlate(texture.sample(new long[] { 64, 64 },
			new double[2], 0.05, 5), texture.sample(new long[] { 64, 64 }, shift,
				0.05, 6), false);
		assertArrayEquals(shift, result.getShift(), 0);
	}

	/**
	 * Shifts of more than half the image size: the peak alone cannot tell them
	 * from the opposite shifts, so the cross-correlation must decide.
	 */
	@Test
	public void testLargeShift2D() {
		final var texture = new Texture(2, 400, 100, 7);
		for (final double[] shift : new double[][] { { 40, -10 }, { -42, 6 } }) {
			final var result = correlate(texture.sample(new long[] { 64, 64 },
				new double[2], 0.05, 8), texture.sample(new long[] { 64, 64 }, shift,
					0.05, 9), false);
			assertArrayEquals(shift, result.getShift(), 0, result.toString());
			assertTrue(result.getCorrelation() > 0.9, result.toString());
		}
	}

	@Test
	public void testSubpixelShift2D() {
		final var texture = new Texture(2, 120, 64, 10);
		for (final double[] shift : new double[][] { { 5.3, -2.6 }, { -8.5,
			3.25 }, { 0.4, 0.7 } })
		{
			final var result = correlate(texture.sample(new long[] { 64, 64 },
				new double[2], 0.02, 11), texture.sample(new long[] { 64, 64 },
					shift, 0.02, 12), null);
			assertArrayEquals(shift, result.getShift(), 0.1, result.toString());
		}
	}

	/** Images of different sizes. */
	@Test
	public void testDifferentSizes() {
		final var texture = new Texture(2, 150, 80, 13);
		final double[] shift = { 12, 9 };
		final var result = correlate(texture.sample(new long[] { 70, 60 },
			new double[2], 0.05, 14), texture.sample(new long[] { 40, 50 }, shift,
				0.05, 15), false);
		assertArrayEquals(shift, result.getShift(), 0);
	}

	@Test
	public void testIntegerShift3D() {
		final var texture = new Texture(3, 150, 32, 16);
		final double[] shift = { 3, -5, 2 };
		final var dims = new long[] { 32, 32, 24 };
		final var result = correlate(texture.sample(dims, new double[3], 0.05,
			17), texture.sample(dims, shift, 0.05, 18), null);
		assertArrayEquals(shift, result.getShift(), 0.1, result.toString());
		assertTrue(result.getCorrelation() > 0.9, result.toString());
	}

	@Test
	public void testSubpixelShift3D() {
		final var texture = new Texture(3, 150, 32, 19);
		final double[] shift = { 2.5, -1.25, 3.7 };
		final var dims = new long[] { 32, 32, 24 };
		final var result = correlate(texture.sample(dims, new double[3], 0.02,
			20), texture.sample(dims, shift, 0.02, 21), null);
		assertArrayEquals(shift, result.getShift(), 0.1, result.toString());
	}

	/** The same image is not shifted, wherever it is in space. */
	@Test
	public void testIdentity() {
		final var texture = new Texture(2, 100, 48, 22);
		final var img = texture.sample(new long[] { 48, 40 }, new double[2], 0,
			23);
		final var result = correlate(img, Views.translate(img, 100, -7), null);
		assertArrayEquals(new double[2], result.getShift(), 1e-6);
		assertEquals(1, result.getCorrelation(), 1e-6);
		assertEquals(48 * 40, result.getOverlap());
	}

	/** Constant images have no shift to find. */
	@Test
	public void testConstant() {
		final var img = ArrayImgs.floats(16, 16);
		img.forEach(t -> t.set(3));
		final var result = correlate(img, img, null);
		assertTrue(Double.isNaN(result.getCorrelation()));
		assertArrayEquals(new double[2], result.getShift(), 0);
		assertEquals(0, result.getOverlap());
	}

	private PhaseCorrelationResult correlate(
		final RandomAccessibleInterval<FloatType> image1,
		final RandomAccessibleInterval<FloatType> image2, final Boolean subpixel)
	{
		if (subpixel == null) {
			return ops.op("filter.phaseCorrelate").input(image1, image2).outType(
				PhaseCorrelationResult.class).apply();
		}
		return ops.op("filter.phaseCorrelate").input(image1, image2, 5, subpixel)
			.outType(PhaseCorrelationResult.class).apply();
	}

	/** A sum of random Gaussian blobs. */
	private static class Texture {

		private final double[][] centers;
		private final double[] sigmas;
		private final double[] amplitudes;

		private Texture(final int n, final int count, final double extent,
			final long seed)
		{
			final var rnd = new Random(seed);
			centers = new double[count][n];
			sigmas = new double[count];
			amplitudes = new double[count];
			for (var i = 0; i < count; i++) {
				// Note: blobs also lie around the field, so that it has no edges.
				for (var d = 0; d < n; d++)
					centers[i][d] = (1.5 * rnd.nextDouble() - 0.5) * extent;
				sigmas[i] = 1.5 + 2.5 * rnd.nextDouble();
				amplitudes[i] = 0.5 + rnd.nextDouble();
			}
		}

		private double value(final double[] x) {
			double sum = 0;
			for (var i = 0; i < sigmas.length; i++) {
				double r2 = 0;
				for (var d = 0; d < x.length; d++) {
					final var dx = x[d] - centers[i][d];
					r2 += dx * dx;
				}
				sum += amplitudes[i] * Math.exp(-r2 / (2 * sigmas[i] * sigmas[i]));
			}
			return sum;
		}

		/**
		 * Samples {@code image(x) = texture(x - shift)} with additive Gaussian
		 * noise of the given standard deviation.
		 */
		private RandomAccessibleInterval<FloatType> sample(final long[] dims,
			final double[] shift, final double noise, final long seed)
		{
			final var rnd = new Random(seed);
			final var img = ArrayImgs.floats(dims);
			final var cursor = img.localizingCursor();
			final var x = new double[dims.length];
			while (cursor.hasNext()) {
				cursor.fwd();
				for (var d = 0; d < x.length; d++)
					x[d] = cursor.getDoublePosition(d) - shift[d];
				cursor.get().setReal(value(x) + noise * rnd.nextGaussian());
			}
			return img;
		}
	}
}
