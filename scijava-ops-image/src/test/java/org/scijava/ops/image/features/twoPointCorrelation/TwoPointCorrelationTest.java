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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.LongArray;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link TwoPointCorrelation}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class TwoPointCorrelationTest extends AbstractOpTest {

	/** The FFT result matches direct pair counting. */
	@Test
	public void testAgainstNaive() {
		final var rnd = new Random(1);
		final long w = 12, h = 9;
		final var img = random(rnd, 0.4, w, h);
		final var s2 = (double[]) ops.op("features.twoPointCorrelation").input(img,
			6).apply();
		assertEquals(7, s2.length);

		final var ra = img.randomAccess();
		final var pairs = new double[7];
		final var total = new double[7];
		for (var x = 0; x < w; x++) {
			for (var y = 0; y < h; y++) {
				for (var vx = -x; vx < w - x; vx++) {
					for (var vy = -y; vy < h - y; vy++) {
						final var r = Math.round(Math.sqrt(vx * vx + vy * vy));
						if (r > 6) continue;
						total[(int) r]++;
						if (ra.setPositionAndGet(x, y).get() && ra.setPositionAndGet(x +
							vx, y + vy).get()) pairs[(int) r]++;
					}
				}
			}
		}
		for (var r = 0; r <= 6; r++)
			assertEquals(pairs[r] / total[r], s2[r], 1e-6, "r = " + r);
	}

	/** S2(0) is the volume fraction, and S2 tends to its square. */
	@Test
	public void testRandomMedium() {
		final var rnd = new Random(2);
		final var img = random(rnd, 0.3, 64, 64);
		var count = 0;
		for (final var b : img)
			if (b.get()) count++;
		final var phi = count / (64.0 * 64);
		final var s2 = (double[]) ops.op("features.twoPointCorrelation").input(img)
			.apply();
		assertEquals(33, s2.length);
		assertEquals(phi, s2[0], 1e-6);
		for (var r = 1; r < s2.length; r++)
			assertEquals(phi * phi, s2[r], 0.01, "r = " + r);
	}

	/** For a fully foreground image S2 is 1; in 3D too. */
	@Test
	public void testFull3D() {
		final var img = ArrayImgs.bits(6, 5, 4);
		img.forEach(b -> b.set(true));
		final var s2 = (double[]) ops.op("features.twoPointCorrelation").input(img,
			3).apply();
		for (final var v : s2)
			assertEquals(1, v, 1e-6);
	}

	private static ArrayImg<BitType, LongArray> random(final Random rnd,
		final double p, final long... dims)
	{
		final var img = ArrayImgs.bits(dims);
		img.forEach(b -> b.set(rnd.nextDouble() < p));
		return img;
	}
}
