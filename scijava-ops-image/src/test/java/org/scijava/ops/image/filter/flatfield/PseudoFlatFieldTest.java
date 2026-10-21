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
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link PseudoFlatField}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class PseudoFlatFieldTest extends AbstractOpTest {

	private static final int W = 64, H = 48;

	/** Computes a smooth vignetting profile: 1 at the center, 0.5 in corners. */
	private static double vignette(final long x, final long y) {
		final double dx = (x - (W - 1) / 2.0) / (W / 2.0);
		final double dy = (y - (H - 1) / 2.0) / (H / 2.0);
		return 1 - 0.25 * (dx * dx + dy * dy);
	}

	/** Creates a vignetted image of a few bright spots on a flat background. */
	private static Img<UnsignedShortType> vignettedSpots() {
		final var img = ArrayImgs.unsignedShorts(W, H);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final long x = c.getLongPosition(0), y = c.getLongPosition(1);
			final boolean spot = x % 16 == 8 && y % 16 == 8;
			c.get().setReal(Math.round((spot ? 3000 : 1000) * vignette(x, y)));
		}
		return img;
	}

	/** Computes the coefficient of variation of the non-spot pixels. */
	private static double backgroundCV(final RandomAccessibleInterval<
		? extends RealType<?>> img)
	{
		double sum = 0, sumSq = 0;
		long n = 0;
		final var c = Views.flatIterable(img).localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final long x = c.getLongPosition(0), y = c.getLongPosition(1);
			if (Math.abs(x % 16 - 8) <= 1 && Math.abs(y % 16 - 8) <= 1) continue;
			final double v = c.get().getRealDouble();
			sum += v;
			sumSq += v * v;
			n++;
		}
		final double mean = sum / n;
		return Math.sqrt(sumSq / n - mean * mean) / mean;
	}

	@Test
	public void testConstantImageUnchanged() {
		final var in = ArrayImgs.floats(20, 15, 4);
		in.forEach(t -> t.set(42));
		final var out = ArrayImgs.floats(20, 15, 4);
		ops.op("filter.pseudoFlatField").input(in, 5.0).output(out).compute();
		for (final var t : out)
			assertEquals(42, t.get(), 1e-3);
	}

	@Test
	public void testVignettingIsReduced() {
		final var in = vignettedSpots();
		final var out = ArrayImgs.floats(W, H);
		ops.op("filter.pseudoFlatField").input(in, 4.0).output(out).compute();
		final double before = backgroundCV(in);
		final double after = backgroundCV(out);
		// Before correction, the background varies by ~15%.
		assertTrue(before > 0.1, "before: " + before);
		assertTrue(after < before / 3, "before: " + before + ", after: " + after);
	}

	@Test
	public void testFormula() {
		final var in = vignettedSpots();
		final double[] sigmas = { 10, 6 };
		final var out = ArrayImgs.floats(W, H);
		ops.op("filter.pseudoFlatField").input(in, sigmas).output(out).compute();

		// Compute the background independently, then check out * G = in * <G>.
		final Img<FloatType> bg = ArrayImgs.floats(W, H);
		ops.op("filter.gauss").input(in, sigmas,
			new OutOfBoundsBorderFactory<>()).output(bg).compute();
		double sum = 0;
		for (final var t : bg)
			sum += t.get();
		final double meanBg = sum / (W * H);
		LoopBuilder.setImages(in, bg, out).forEachPixel((i, g, o) -> assertEquals(
			i.getRealDouble() * meanBg, o.getRealDouble() * g.getRealDouble(),
			1e-2 * i.getRealDouble()));
	}
}
