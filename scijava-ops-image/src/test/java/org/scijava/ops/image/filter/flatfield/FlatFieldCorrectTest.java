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

package org.scijava.ops.image.filter.flatfield;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link FlatFieldCorrect}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class FlatFieldCorrectTest extends AbstractOpTest {

	private static final long[] DIMS = { 12, 10, 3 };

	/**
	 * Builds a specimen S, an illumination profile P, and simulates
	 * {@code in = S·P + dark} and {@code flat = c·P + dark}. The corrected image
	 * must then be {@code S·mean(P)}.
	 */
	private void check(final double darkValue, final boolean passDark) {
		final var random = new Random(0xF1A7);
		final var specimen = ArrayImgs.doubles(DIMS);
		final var profile = ArrayImgs.doubles(DIMS);
		final var c = profile.localizingCursor();
		final var s = specimen.cursor();
		double profileSum = 0;
		while (c.hasNext()) {
			c.fwd();
			final double x = c.getDoublePosition(0) / DIMS[0];
			final double y = c.getDoublePosition(1) / DIMS[1];
			final double p = 0.5 + x * (1 - y) + 0.1 * c.getDoublePosition(2);
			c.get().set(p);
			profileSum += p;
			s.next().set(10 + 90 * random.nextDouble());
		}
		final double meanProfile = profileSum / profile.size();

		final ArrayImg<DoubleType, DoubleArray> in = ArrayImgs.doubles(DIMS);
		final ArrayImg<DoubleType, DoubleArray> flat = ArrayImgs.doubles(DIMS);
		final ArrayImg<DoubleType, DoubleArray> dark = ArrayImgs.doubles(DIMS);
		dark.forEach(t -> t.set(darkValue));
		LoopBuilder.setImages(specimen, profile, in, flat).forEachPixel((sp, p, i,
			f) -> {
			i.set(sp.get() * p.get() + darkValue);
			f.set(1234 * p.get() + darkValue);
		});

		final var out = ArrayImgs.floats(DIMS);
		if (passDark) {
			ops.op("filter.flatFieldCorrect").input(in, flat, dark).output(out)
				.compute();
		}
		else {
			ops.op("filter.flatFieldCorrect").input(in, flat).output(out).compute();
		}
		LoopBuilder.setImages(specimen, out).forEachPixel((sp, o) -> assertEquals(
			sp.get() * meanProfile, o.get(), 1e-3));
	}

	@Test
	public void testWithoutDark() {
		check(0, false);
	}

	@Test
	public void testWithDark() {
		check(100, true);
	}

	@Test
	public void testZeroFlatPixel() {
		final var in = ArrayImgs.doubles(new double[] { 5, 6, 7, 8 }, 4);
		final var flat = ArrayImgs.doubles(new double[] { 2, 0, 4, 2 }, 4);
		final var out = ArrayImgs.doubles(4);
		ops.op("filter.flatFieldCorrect").input(in, flat).output(out).compute();
		// The mean of the flat-field image is 2.
		final double[] expected = { 5, 6, 3.5, 8 };
		final var o = out.cursor();
		for (final double e : expected)
			assertEquals(e, o.next().get(), 1e-12);
	}

	/**
	 * Integer outputs must saturate rather than wrap around: values above 255
	 * become 255, and {@code in < dark} becomes 0.
	 */
	@Test
	public void testUnsignedByteOutputIsClamped() {
		final var in = ArrayImgs.unsignedBytes(new byte[] { 10, (byte) 200,
			(byte) 250, 5 }, 4);
		final var flat = ArrayImgs.unsignedBytes(new byte[] { 100, 50, 100, 100 },
			4);
		final var dark = ArrayImgs.unsignedBytes(new byte[] { 8, 8, 8, 8 }, 4);

		// Without dark: mean(flat) = 87.5, so the values are 8.75, 350 (too
		// bright), 218.75 and 4.375.
		final var out = ArrayImgs.unsignedBytes(4);
		ops.op("filter.flatFieldCorrect").input(in, flat).output(out).compute();
		assertValues(new int[] { 9, 255, 219, 4 }, out);

		// With dark: mean(flat - dark) = 79.5, so the values are 1.73, 363.4 (too
		// bright), 209.1 and -2.59 (in < dark).
		final var outDark = ArrayImgs.unsignedBytes(4);
		ops.op("filter.flatFieldCorrect").input(in, flat, dark).output(outDark)
			.compute();
		assertValues(new int[] { 2, 255, 209, 0 }, outDark);
	}

	private static void assertValues(final int[] expected,
		final Iterable<UnsignedByteType> actual)
	{
		final var c = actual.iterator();
		for (final int e : expected)
			assertEquals(e, c.next().get());
	}
}
