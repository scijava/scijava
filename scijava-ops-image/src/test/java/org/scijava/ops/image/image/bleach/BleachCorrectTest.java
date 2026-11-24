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

package org.scijava.ops.image.image.bleach;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link BleachCorrect}, {@link BleachFit} and {@link HistogramMatch}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class BleachCorrectTest extends AbstractOpTest {

	private static final double A = 80, B = 0.15, C = 20;

	/** A textured 2D+t series (time in dimension 1) decaying as A e^-Bt + C. */
	private static RandomAccessibleInterval<DoubleType> decaying(
		final boolean noisy)
	{
		final var rnd = new Random(3);
		final var img = ArrayImgs.doubles(16, 20, 12);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final var t = c.getIntPosition(1);
			final var texture = 1 + 0.5 * Math.sin(c.getIntPosition(0) + 2 * c
				.getIntPosition(2));
			final var f = A * Math.exp(-B * t) + C;
			c.get().set(texture * f * (noisy ? 1 + 0.02 * rnd.nextGaussian() : 1));
		}
		return img;
	}

	@Test
	public void testFit() {
		final var p = ops.op("image.bleachFit").input(decaying(false), 1).outType(
			double[].class).apply();
		// The mean of the texture is not exactly 1, so only b and a / c are known.
		assertEquals(B, p[1], 1e-6);
		assertEquals(A / C, p[0] / p[2], 1e-6);
	}

	@Test
	public void testRatio() {
		final var in = decaying(true);
		final var out = ArrayImgs.doubles(16, 20, 12);
		ops.op("image.bleachCorrect").input(in, 1, "ratio").output(out).compute();
		final var means = Bleach.frameMeans(out, 1);
		final var in0 = Bleach.frameMeans(in, 1)[0];
		for (final var m : means)
			assertEquals(in0, m, 1e-9);
		// The default method is ratio.
		final var out2 = ArrayImgs.doubles(16, 20, 12);
		ops.op("image.bleachCorrect").input(in, 1).output(out2).compute();
		assertImagesEqual(out, out2, 0);
	}

	@Test
	public void testExponential() {
		final var in = decaying(true);
		final var out = ArrayImgs.doubles(16, 20, 12);
		ops.op("image.bleachCorrect").input(in, 1, "exponential").output(out)
			.compute();
		final var means = Bleach.frameMeans(out, 1);
		for (final var m : means)
			assertEquals(means[0], m, 0.01 * means[0]);
		// Without noise, the corrected image is exactly the first frame.
		final var clean = decaying(false);
		ops.op("image.bleachCorrect").input(clean, 1, "exponential").output(out)
			.compute();
		for (var t = 0; t < 20; t++)
			assertImagesEqual(Views.hyperSlice(clean, 1, 0), Views.hyperSlice(out, 1,
				t), 1e-6);
	}

	@Test
	public void testClampsToOutputRange() {
		final var in = ArrayImgs.unsignedBytes(new byte[] { (byte) 200, 100,
			(byte) 200, 0 }, 2, 2);
		final var out = ArrayImgs.unsignedBytes(2, 2);
		ops.op("image.bleachCorrect").input(in, 1, "ratio").output(out).compute();
		final var ra = out.randomAccess();
		assertEquals(255, ra.setPositionAndGet(0, 1).get());
		assertEquals(0, ra.setPositionAndGet(1, 1).get());
		assertEquals(100, ra.setPositionAndGet(1, 0).get());
	}

	@Test
	public void testHistogramMatchIdentity() {
		final var rnd = new Random(9);
		final var in = ArrayImgs.unsignedBytes(13, 11);
		in.forEach(t -> t.set(rnd.nextInt(40)));
		final var out = ArrayImgs.unsignedBytes(13, 11);
		ops.op("image.histogramMatch").input(in, in).output(out).compute();
		assertImagesEqual(in, out, 0);
	}

	@Test
	public void testHistogramMatchMonotoneMap() {
		// The reference is a monotone transform of the input, so matching
		// recovers the transform exactly.
		final var rnd = new Random(10);
		final var in = ArrayImgs.unsignedBytes(20, 10);
		in.forEach(t -> t.set(rnd.nextInt(256)));
		final var expected = ArrayImgs.doubles(20, 10);
		final var c = in.cursor();
		final var e = expected.cursor();
		while (c.hasNext())
			e.next().set(Math.sqrt(c.next().get()) * 3 + 1);
		// Same distribution as expected, different size: every value twice and
		// in another order.
		final var big = ArrayImgs.doubles(20, 10, 2);
		final var bc = big.cursor();
		for (var k = 0; k < 2; k++)
			for (final var v : Views.flatIterable(Views.invertAxis(expected, 0)))
				bc.next().set(v.get());
		final var out = ArrayImgs.doubles(20, 10);
		ops.op("image.histogramMatch").input(in, big).output(out).compute();
		assertImagesEqual(expected, out, 1e-12);
	}

	@Test
	public void testHistogramFrames() {
		final var in = decaying(true);
		final var out = ArrayImgs.doubles(16, 20, 12);
		ops.op("image.histogramMatch").input(in, 1, 0).output(out).compute();
		final var out2 = ArrayImgs.doubles(16, 20, 12);
		ops.op("image.bleachCorrect").input(in, 1, "histogram").output(out2)
			.compute();
		assertImagesEqual(out, out2, 0);
		// The noise makes the sort order of each frame differ from frame 0, but
		// every frame now has exactly the values of frame 0.
		final var ref = sortedValues(Views.hyperSlice(in, 1, 0));
		for (var t = 0; t < 20; t++)
			assertArrayEquals(ref, sortedValues(Views.hyperSlice(out, 1, t)), 0);
	}

	private static double[] sortedValues(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		final var v = new double[(int) img.size()];
		var i = 0;
		for (final var t : img)
			v[i++] = t.getRealDouble();
		java.util.Arrays.sort(v);
		return v;
	}

	private static void assertImagesEqual(
		final RandomAccessibleInterval<? extends RealType<?>> expected,
		final RandomAccessibleInterval<? extends RealType<?>> actual,
		final double tol)
	{
		final var e = Views.flatIterable(expected).cursor();
		final var a = Views.flatIterable(actual).cursor();
		while (e.hasNext())
			assertEquals(e.next().getRealDouble(), a.next().getRealDouble(), tol);
	}
}
