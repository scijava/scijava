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

package org.scijava.ops.image.color.deconvolve;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@code color.deconvolveStains}.
 *
 * @author Curtis Rueden
 */
public class DeconvolveStainsTest extends AbstractOpTest {

	private static final double[][] HED = { { 0.650, 0.704, 0.286 }, //
		{ 0.072, 0.990, 0.105 }, { 0.268, 0.570, 0.776 } };

	/** Test pixels (red, green, blue). */
	private static final int[][] RGB = { { 255, 255, 255 }, { 200, 150, 220 }, //
		{ 120, 60, 160 }, { 230, 120, 180 }, { 100, 90, 150 }, { 50, 40, 100 }, //
		{ 180, 140, 110 }, { 1, 2, 3 } };

	/**
	 * Output of the Fiji Colour_Deconvolution plugin 3.0.3
	 * ({@code StainMatrix.compute}) for {@link #RGB} with the {@link #HED}
	 * vectors: one 8-bit transmittance image per stain.
	 */
	private static final int[][] PLUGIN = { //
		{ 255, 184, 92, 255, 66, 25, 229, 0 }, //
		{ 255, 192, 134, 143, 255, 255, 255, 255 }, //
		{ 255, 248, 224, 173, 213, 183, 90, 10 } };

	private static double[][] normalized(final double[][] m) {
		return ColourDeconvolution.stainMatrix(m);
	}

	@Test
	public void testRecoversKnownAmounts() {
		// Channel-first float image with I0 = 1, synthesized by Beer-Lambert.
		final var m = normalized(HED);
		final var rnd = new Random(42);
		final long w = 5, h = 4;
		final var amounts = new double[(int) (w * h)][3];
		final var img = ArrayImgs.doubles(3, w, h);
		final var ra = img.randomAccess();
		for (var i = 0; i < amounts.length; i++) {
			for (var k = 0; k < 3; k++)
				amounts[i][k] = 1.5 * rnd.nextDouble();
			for (var c = 0; c < 3; c++) {
				var od = 0.0;
				for (var k = 0; k < 3; k++)
					od += amounts[i][k] * m[k][c];
				ra.setPositionAndGet(c, i % w, i / w).set(Math.pow(10, -od));
			}
		}
		final RandomAccessibleInterval<DoubleType> out = ops.op(
			"color.deconvolveStains").input(img, HED, 0, 1.0).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();
		assertArrayEquals(img.dimensionsAsLongArray(), out.dimensionsAsLongArray());
		final var oa = out.randomAccess();
		for (var i = 0; i < amounts.length; i++) {
			for (var k = 0; k < 3; k++) {
				assertEquals(amounts[i][k], oa.setPositionAndGet(k, i % w, i / w)
					.get(), 1e-9);
			}
		}
	}

	@Test
	public void testTwoStainsUseOrthogonalThirdVector() {
		final var m = StainVectors.H_E.vectors();
		final var full = normalized(m);
		assertEquals(0, dot(full[0], full[2]), 1e-12);
		assertEquals(0, dot(full[1], full[2]), 1e-12);
		assertEquals(1, dot(full[2], full[2]), 1e-12);

		// A pure mixture of the first two stains has no third component.
		final var img = ArrayImgs.unsignedShorts(4, 3);
		final var ra = img.randomAccess();
		final double i0 = 65535;
		for (var x = 0; x < 4; x++) {
			for (var c = 0; c < 3; c++) {
				final var od = 0.3 * x * full[0][c] + 0.2 * full[1][c];
				ra.setPositionAndGet(x, c).setReal(Math.round(i0 * Math.pow(10, -od)));
			}
		}
		final var out = deconvolve(img, "H&E");
		final var oa = out.randomAccess();
		for (var x = 0; x < 4; x++) {
			assertEquals(0.3 * x, oa.setPositionAndGet(x, 0).get(), 1e-3);
			assertEquals(0.2, oa.setPositionAndGet(x, 1).get(), 1e-3);
			assertEquals(0, oa.setPositionAndGet(x, 2).get(), 1e-3);
		}
	}

	@Test
	public void testMatchesPlugin() {
		final var img = ArrayImgs.unsignedBytes(RGB.length, 3);
		final var ra = img.randomAccess();
		for (var i = 0; i < RGB.length; i++)
			for (var c = 0; c < 3; c++)
				ra.setPositionAndGet(i, c).set(RGB[i][c]);
		final var out = deconvolve(img, HED);
		final var oa = out.randomAccess();
		for (var k = 0; k < 3; k++) {
			for (var i = 0; i < RGB.length; i++) {
				// The plugin outputs 8-bit transmittance and uses a slightly
				// different zero-intensity offset, so agreement is within 1 grey
				// level.
				final var t = Math.min(255, 255 * Math.pow(10, -oa.setPositionAndGet(
					i, k).get()));
				assertEquals(PLUGIN[k][i], t, 1.5, "stain " + k + ", pixel " + i);
			}
		}
	}

	@Test
	public void testARGBMatchesChannels() {
		final var argb = ArrayImgs.argbs(RGB.length, 2);
		final var channels = ArrayImgs.unsignedBytes(RGB.length, 2, 3);
		final var ar = argb.randomAccess();
		final var cr = channels.randomAccess();
		for (var i = 0; i < RGB.length; i++) {
			for (var y = 0; y < 2; y++) {
				ar.setPositionAndGet(i, y).set(ARGBType.rgba(RGB[i][0], RGB[i][1],
					RGB[i][2], 255));
				for (var c = 0; c < 3; c++)
					cr.setPositionAndGet(i, y, c).set(RGB[i][c]);
			}
		}
		final var expected = deconvolve(channels, "H DAB");
		final RandomAccessibleInterval<DoubleType> actual = ops.op(
			"color.deconvolveStains").input(argb, "H DAB").outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();
		assertArrayEquals(expected.dimensionsAsLongArray(), actual
			.dimensionsAsLongArray());
		LoopBuilder.setImages(expected, actual).forEachPixel((e, a) -> assertEquals(e
			.get(), a.get(), 1e-12));

		final RandomAccessibleInterval<DoubleType> actual2 = ops.op(
			"color.deconvolveStains").input(argb, StainVectors.H_DAB.vectors(), 255.0)
			.outType(new Nil<RandomAccessibleInterval<DoubleType>>()
			{}).apply();
		LoopBuilder.setImages(expected, actual2).forEachPixel((e, a) -> assertEquals(
			e.get(), a.get(), 1e-12));
	}

	@Test
	public void testStainNames() {
		assertEquals(StainVectors.H_E, StainVectors.fromName("h&e"));
		assertEquals(StainVectors.H_E, StainVectors.fromName("H_E"));
		assertEquals(StainVectors.H_E_DAB, StainVectors.fromName("H&E DAB"));
		assertEquals(StainVectors.MASSON_TRICHROME, StainVectors.fromName(
			"masson trichrome"));
		assertThrows(IllegalArgumentException.class, () -> StainVectors.fromName(
			"Nonexistent"));
		for (final var s : StainVectors.values())
			ColourDeconvolution.invert(normalized(s.vectors()));
	}

	private RandomAccessibleInterval<DoubleType> deconvolve(
		final RandomAccessibleInterval<?> img, final Object stains)
	{
		return ops.op("color.deconvolveStains").input(img, stains).outType(
			new Nil<RandomAccessibleInterval<DoubleType>>()
			{}).apply();
	}

	private static double dot(final double[] a, final double[] b) {
		return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
	}
}
