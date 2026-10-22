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

package org.scijava.ops.image.filter.helmholtz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link HelmholtzAnalysis}.
 * <p>
 * The plugin values below were produced by replicating
 * {@code Helmholtz_Analysis.run} (with ImageJ 1.54p "Gaussian Blur...",
 * "Bandpass Filter..." and image subtraction, then the plugin's
 * {@code average} methods) on {@link #grating}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class HelmholtzAnalysisTest extends AbstractOpTest {

	private static final int W = 64, H = 64;

	private static final Nil<RandomAccessibleInterval<FloatType>> FLOATS =
		new Nil<>()
		{};

	/**
	 * A sinusoidal grating whose wave vector points at angle {@code thetaDeg}
	 * from the x axis towards the y axis, plus a linear trend.
	 */
	private static Img<FloatType> grating(final double lambda,
		final double thetaDeg)
	{
		final var p = new float[W * H];
		final double t = Math.toRadians(thetaDeg), k = 2 * Math.PI / lambda;
		for (var y = 0; y < H; y++)
			for (var x = 0; x < W; x++)
				p[x + W * y] = (float) (100 + 50 * Math.cos(k * (x * Math.cos(t) + y *
					Math.sin(t))) + 0.2 * x);
		return ArrayImgs.floats(p, W, H);
	}

	@Test
	public void testGratings() {
		// lambda, theta, lambdaMin, lambdaMax, radius, plugin lambda and phi
		// medians (raw), plugin lambda and phi medians (averaged).
		final double[][] cases = { //
			{ 8, 30, 4, 16, 5, 8.132935, 119.34082, 8.145168, 119.33787 }, //
			{ 10, 120, 4, 20, 6, 10.122702, 29.580921, 10.175095, 29.583069 }, //
			{ 6, 0, 3, 12, 4, 6.2832785, 90.0, 6.283233, 90.0 } };
		for (final var c : cases) {
			final var in = grating(c[0], c[1]);
			final var expectedPhi = (c[1] + 90) % 180;
			for (final var r : new int[] { 0, (int) c[4] }) {
				final var lambda = ops.op("filter.helmholtzWavelength").input(in, c[2],
					c[3], r).outType(FLOATS).apply();
				final var phi = ops.op("filter.helmholtzOrientation").input(in, c[2],
					c[3], r).outType(FLOATS).apply();
				final var lambdaMedian = median(lambda);
				final var phiMedian = median(phi);
				final var pluginLambda = r == 0 ? c[5] : c[7];
				final var pluginPhi = r == 0 ? c[6] : c[8];
				// Note: the discrete Laplacian biases short wavelengths upwards.
				assertEquals(c[0], lambdaMedian, 0.06 * c[0]);
				assertEquals(pluginLambda, lambdaMedian, 0.01 * c[0]);
				assertEquals(expectedPhi, phiMedian, 1.5);
				assertEquals(pluginPhi, phiMedian, 0.5);
				assertTrue(validFraction(lambda) > 0.8);
				assertTrue(validFraction(phi) > 0.7);
			}
		}
	}

	/**
	 * Exactly horizontal fringes, whose gradient has no x component, must have
	 * orientation 0 everywhere, never 180, which is outside {@code [0, 180)}.
	 */
	@Test
	public void testHorizontalFringes() {
		final var p = new float[W * H];
		for (var y = 0; y < H; y++)
			for (var x = 0; x < W; x++)
				p[x + W * y] = (float) (100 + 50 * Math.cos(2 * Math.PI * y / 8));
		final var in = ArrayImgs.floats(p, W, H);
		final var phi = ops.op("filter.helmholtzOrientation").input(in, 4.0, 16.0)
			.outType(FLOATS).apply();
		assertTrue(validFraction(phi) > 0.5);
		for (final var v : valid(phi))
			assertEquals(0, v, 1e-3);
	}

	@Test
	public void testOutOfRange() {
		final var in = grating(6, 45);
		final var lambda = ops.op("filter.helmholtzWavelength").input(in, 12.0,
			24.0).outType(FLOATS).apply();
		assertTrue(validFraction(lambda) < 0.25);
		for (final var t : Views.iterable(lambda)) {
			final var v = t.get();
			assertTrue(Float.isNaN(v) || v >= 12 && v <= 24);
		}
	}

	@Test
	public void test3DUnsupported() {
		final var in = ArrayImgs.floats(8, 8, 2);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"filter.helmholtzWavelength").input(in, 2.0, 6.0).outType(FLOATS)
			.apply());
	}

	private static double median(final RandomAccessibleInterval<FloatType> img) {
		final var values = valid(img);
		Arrays.sort(values);
		return values[values.length / 2];
	}

	private static double validFraction(
		final RandomAccessibleInterval<FloatType> img)
	{
		return (double) valid(img).length / img.size();
	}

	private static float[] valid(final RandomAccessibleInterval<FloatType> img) {
		final var values = new float[(int) img.size()];
		var n = 0;
		for (final var t : Views.iterable(img))
			if (!Float.isNaN(t.get())) values[n++] = t.get();
		return Arrays.copyOf(values, n);
	}
}
