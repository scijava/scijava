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

package org.scijava.ops.image.image.histogram2D;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.LongType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultHistogram2D}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultHistogram2DTest extends AbstractOpTest {

	@Test
	public void testJointCounts() {
		final var img1 = ArrayImgs.unsignedBytes(new byte[] { 0, 0, 1, 1, 2, 2, 3,
			3 }, 4, 2);
		final var img2 = ArrayImgs.floats(new float[] { 10, 20, 10, 20, 10, 10, 20,
			20 }, 4, 2);
		final Img<LongType> histogram = ops.op("image.histogram2D").input(img1,
			img2, 4, 2).outType(new Nil<Img<LongType>>()
		{}).apply();
		assertArrayEquals(new long[] { 4, 2 }, histogram.dimensionsAsLongArray());
		final var expected = new long[][] { { 1, 1 }, { 1, 1 }, { 2, 0 }, { 0,
			2 } };
		final var ra = histogram.randomAccess();
		long total = 0;
		for (var i = 0; i < 4; i++) {
			for (var j = 0; j < 2; j++) {
				final var count = ra.setPositionAndGet(i, j).get();
				assertEquals(expected[i][j], count, "bin (" + i + ", " + j + ")");
				total += count;
			}
		}
		assertEquals(8, total);
	}

	@Test
	public void testDefaultBins() {
		final var img = ArrayImgs.unsignedBytes(new byte[] { 0, 1, (byte) 254,
			(byte) 255 }, 2, 2);
		final Img<LongType> histogram = ops.op("image.histogram2D").input(img, img)
			.outType(new Nil<Img<LongType>>()
			{}).apply();
		assertArrayEquals(new long[] { 256, 256 }, histogram.dimensionsAsLongArray());
		final var ra = histogram.randomAccess();
		for (final var v : new int[] { 0, 1, 254, 255 })
			assertEquals(1, ra.setPositionAndGet(v, v).get());
	}

	@Test
	public void testConstantImage() {
		final var img1 = ArrayImgs.doubles(new double[] { 5, 5, 5, 5 }, 4);
		final var img2 = ArrayImgs.doubles(new double[] { 0, 1, 2, 3 }, 4);
		final Img<LongType> histogram = ops.op("image.histogram2D").input(img1,
			img2, 3, 4).outType(new Nil<Img<LongType>>()
		{}).apply();
		final var ra = histogram.randomAccess();
		for (var j = 0; j < 4; j++)
			assertEquals(1, ra.setPositionAndGet(0, j).get());
	}
}
