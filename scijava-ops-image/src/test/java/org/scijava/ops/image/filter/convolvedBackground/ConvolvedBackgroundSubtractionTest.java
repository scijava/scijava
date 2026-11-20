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

package org.scijava.ops.image.filter.convolvedBackground;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link ConvolvedBackgroundSubtraction}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class ConvolvedBackgroundSubtractionTest extends AbstractOpTest {

	/** Creates sparse single-pixel spots of 200 on a background of 50. */
	private static Img<UnsignedByteType> spots() {
		final var img = ArrayImgs.unsignedBytes(40, 30);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final boolean spot = c.getLongPosition(0) % 7 == 3 && c.getLongPosition(
				1) % 7 == 3;
			c.get().set(spot ? 200 : 50);
		}
		return img;
	}

	private static void assertSpotsOnly(final Img<UnsignedByteType> in,
		final Img<? extends net.imglib2.type.numeric.RealType<?>> out)
	{
		LoopBuilder.setImages(in, out).forEachPixel((i, o) -> assertEquals(i
			.get() == 200 ? 150 : 0, o.getRealDouble(), 1e-4));
	}

	@Test
	public void testMedian() {
		// The median ignores the sparse spots, so the background is exactly 50;
		// with radius 10 the subsequent maximum filter (radius 1) keeps it so.
		final var in = spots();
		final var out = ArrayImgs.unsignedBytes(40, 30);
		ops.op("filter.convolvedBackgroundSubtraction").input(in, 10.0, "median")
			.output(out).compute();
		assertSpotsOnly(in, out);
	}

	@Test
	public void testGaussianDefault() {
		// A constant image has a constant Gaussian background.
		final var in = ArrayImgs.unsignedBytes(20, 20, 5);
		in.forEach(t -> t.set(77));
		final var out = ArrayImgs.floats(20, 20, 5);
		ops.op("filter.convolvedBackgroundSubtraction").input(in, 12.0).output(out)
			.compute();
		for (final var t : out)
			assertEquals(0, t.get(), 1e-3);
	}

	@Test
	public void testMeanOnRamp() {
		// The mean over a symmetric neighborhood of a linear ramp is the center
		// value, so the result is zero away from the border.
		final var in = ArrayImgs.floats(30, 30);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(2 * c.getFloatPosition(0) + 3 * c.getFloatPosition(1));
		}
		final var out = ArrayImgs.floats(30, 30);
		ops.op("filter.convolvedBackgroundSubtraction").input(in, 3.0, "mean")
			.output(out).compute();
		final var oc = out.localizingCursor();
		while (oc.hasNext()) {
			oc.fwd();
			final long x = oc.getLongPosition(0), y = oc.getLongPosition(1);
			if (x >= 3 && x < 27 && y >= 3 && y < 27) {
				assertEquals(0, oc.get().get(), 1e-3);
			}
		}
	}

	@Test
	public void testClamping() {
		final var in = ArrayImgs.unsignedBytes(15, 15);
		in.forEach(t -> t.set(100));
		in.randomAccess().setPositionAndGet(7, 7).set(0);
		final var outByte = ArrayImgs.unsignedBytes(15, 15);
		ops.op("filter.convolvedBackgroundSubtraction").input(in, 2.0, "median")
			.output(outByte).compute();
		assertEquals(0, outByte.randomAccess().setPositionAndGet(7, 7).get());
		final var outFloat = ArrayImgs.floats(15, 15);
		ops.op("filter.convolvedBackgroundSubtraction").input(in, 2.0, "median")
			.output(outFloat).compute();
		assertEquals(-100, outFloat.randomAccess().setPositionAndGet(7, 7).get(),
			1e-4);
	}

	@Test
	public void testUnknownFilter() {
		final var in = spots();
		final var out = ArrayImgs.unsignedBytes(40, 30);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"filter.convolvedBackgroundSubtraction").input(in, 10.0, "bogus").output(
				out).compute());
	}
}
