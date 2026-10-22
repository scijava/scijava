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

package org.scijava.ops.image.filter.kalman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@code filter.kalman}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class KalmanFilterTest extends AbstractOpTest {

	@Test
	public void testNoiseDecreases() {
		final int w = 8, h = 8, frames = 60;
		final var rnd = new Random(4);
		final var img = ArrayImgs.doubles(w, h, frames);
		for (final var t : img)
			t.set(100 + 10 * rnd.nextGaussian());
		final var out = ArrayImgs.doubles(w, h, frames);
		ops.op("filter.kalman").input(img).output(out).compute();
		final var first = rms(Views.hyperSlice(out, 2, 0));
		final var last = rms(Views.hyperSlice(out, 2, frames - 1));
		assertEquals(10, first, 3);
		// The steady state is an exponential average with weight 0.2, whose
		// noise is 10 * sqrt(0.2 / 1.8) = 3.3.
		assertTrue(last < 5, "final noise " + last);
	}

	@Test
	public void testRunningMean() {
		// With g = 1, the update is x += K (z - x) with K_t = 1 / (t + 1), so the
		// estimate is the mean of all frames so far.
		final var rnd = new Random(8);
		final var img = ArrayImgs.unsignedShorts(3, 2, 20);
		for (final var t : img)
			t.set(rnd.nextInt(1000));
		final var out = ArrayImgs.doubles(3, 2, 20);
		ops.op("filter.kalman").input(img, 2, 1.0).output(out).compute();
		final var ia = img.randomAccess();
		final var oa = out.randomAccess();
		for (var x = 0; x < 3; x++) {
			for (var y = 0; y < 2; y++) {
				var sum = 0.0;
				for (var t = 0; t < 20; t++) {
					sum += ia.setPositionAndGet(x, y, t).get();
					assertEquals(sum / (t + 1), oa.setPositionAndGet(x, y, t).get(),
						1e-9);
				}
			}
		}
	}

	/**
	 * A step from 0 to 100 at frame 1, filtered along dimension 0 with the
	 * default gain g = 0.8. By the documented update of the Kalman stack
	 * filter, seeded with {@code x_0 = z_0} and {@code P_0 = R}, the Kalman gain
	 * is {@code K_t = 1 / (t + 1)}:
	 * <ul>
	 * <li>t = 1: K = 1/2, x = 0.8·0 + 0.2·100 + 0.5·(100 - 0) = 70;</li>
	 * <li>t = 2: K = 1/3, x = 0.8·70 + 0.2·100 + (100 - 70)/3 = 86;</li>
	 * <li>t = 3: K = 1/4, x = 0.8·86 + 0.2·100 + (100 - 86)/4 = 92.3;</li>
	 * <li>t = 4: K = 1/5, x = 0.8·92.3 + 0.2·100 + (100 - 92.3)/5 = 95.38.</li>
	 * </ul>
	 */
	@Test
	public void testStepResponse() {
		final var img = ArrayImgs.doubles(new double[] { 0, 100, 100, 100, 100, 0,
			100, 100, 100, 100 }, 5, 2);
		final var out = ArrayImgs.doubles(5, 2);
		ops.op("filter.kalman").input(img, 0).output(out).compute();
		final double[] expected = { 0, 70, 86, 92.3, 95.38 };
		final var oa = out.randomAccess();
		for (var y = 0; y < 2; y++)
			for (var t = 0; t < 5; t++)
				assertEquals(expected[t], oa.setPositionAndGet(t, y).get(), 1e-9);
	}

	/**
	 * With {@code g = 0}, the Kalman term is not scaled down, so the estimate
	 * overshoots: for frames (0, 200), {@code x_1 = 200 + (200 - 0) / 2 = 300}.
	 * An 8-bit output saturates at 255.
	 */
	@Test
	public void testNoGainOvershoots() {
		final var img = ArrayImgs.doubles(new double[] { 0, 200 }, 1, 2);
		final var out = ArrayImgs.doubles(1, 2);
		ops.op("filter.kalman").input(img, 1, 0.0).output(out).compute();
		assertEquals(0, out.getAt(0, 0).get(), 0);
		assertEquals(300, out.getAt(0, 1).get(), 1e-9);

		final var outByte = ArrayImgs.unsignedBytes(1, 2);
		ops.op("filter.kalman").input(img, 1, 0.0).output(outByte).compute();
		assertEquals(255, outByte.getAt(0, 1).get());
	}

	@Test
	public void testInvalidGain() {
		final var img = ArrayImgs.doubles(4, 5);
		assertThrows(RuntimeException.class, () -> ops.op("filter.kalman").input(
			img, 1, 1.5).output(ArrayImgs.doubles(4, 5)).compute());
	}

	private static double rms(
		final net.imglib2.RandomAccessibleInterval<net.imglib2.type.numeric.real.DoubleType> img)
	{
		var sum = 0.0;
		long n = 0;
		for (final var t : Views.iterable(img)) {
			sum += Math.pow(t.get() - 100, 2);
			n++;
		}
		return Math.sqrt(sum / n);
	}
}
