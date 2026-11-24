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

package org.scijava.ops.image.color.convert;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@code color.convert}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class ColorConvertTest extends AbstractOpTest {

	private static final String[] SPACES = { "RGB", "linearRGB", "HSB", "XYZ",
		"Lab", "Luv", "YCbCr" };

	private RandomAccessibleInterval<DoubleType> convert(
		final RandomAccessibleInterval<?> img, final String from, final String to)
	{
		return ops.op("color.convert").input(img, from, to).outType(
			new Nil<RandomAccessibleInterval<DoubleType>>()
			{}).apply();
	}

	/** Converts one colour, stored in a 1 x 3 image. */
	private double[] convert(final double[] c, final String from,
		final String to)
	{
		final var img = ArrayImgs.doubles(c.clone(), 1, 3);
		final var out = convert(img, from, to);
		final var ra = out.randomAccess();
		return new double[] { ra.setPositionAndGet(0, 0).get(), //
			ra.setPositionAndGet(0, 1).get(), ra.setPositionAndGet(0, 2).get() };
	}

	@Test
	public void testPrimariesToXYZ() {
		// The columns of the IEC 61966-2-1 matrix.
		final var img = ArrayImgs.unsignedBytes(new byte[] { (byte) 255, 0, 0, //
			0, (byte) 255, 0, 0, 0, (byte) 255 }, 3, 3);
		final var out = convert(img, "sRGB", "XYZ");
		final double[][] expected = { { 0.4124, 0.2126, 0.0193 }, //
			{ 0.3576, 0.7152, 0.1192 }, { 0.1805, 0.0722, 0.9505 } };
		final var ra = out.randomAccess();
		for (var p = 0; p < 3; p++) {
			for (var c = 0; c < 3; c++) {
				assertEquals(expected[p][c], ra.setPositionAndGet(p, c).get(), 1e-12);
			}
		}
	}

	@Test
	public void testWhiteAndGrey() {
		assertArrayEquals(new double[] { 100, 0, 0 }, convert(new double[] { 1, 1,
			1 }, "RGB", "Lab"), 1e-9);
		assertArrayEquals(new double[] { 100, 0, 0 }, convert(new double[] { 1, 1,
			1 }, "RGB", "Luv"), 1e-9);
		assertArrayEquals(new double[] { 0, 0, 0 }, convert(new double[] { 0, 0,
			0 }, "RGB", "Lab"), 1e-9);
		assertArrayEquals(new double[] { 0, 0, 0 }, convert(new double[] { 0, 0,
			0 }, "RGB", "Luv"), 1e-9);
		assertArrayEquals(new double[] { 1, 0, 0 }, convert(new double[] { 1, 1,
			1 }, "RGB", "YCbCr"), 1e-12);
		// sRGB 50% grey has L* = 53.39.
		final var grey = convert(new double[] { 0.5, 0.5, 0.5 }, "RGB", "Lab");
		assertArrayEquals(new double[] { 53.389, 0, 0 }, grey, 1e-3);
	}

	@Test
	public void testPublishedLabAndLuv() {
		// Commonly published D65 values for the sRGB primaries (e.g. Lindbloom);
		// they differ from ours by less than 0.02 because of the rounding of the
		// D65 white point.
		assertArrayEquals(new double[] { 53.24, 80.09, 67.20 }, convert(
			new double[] { 1, 0, 0 }, "RGB", "Lab"), 0.03);
		assertArrayEquals(new double[] { 87.73, -86.18, 83.18 }, convert(
			new double[] { 0, 1, 0 }, "RGB", "Lab"), 0.03);
		assertArrayEquals(new double[] { 32.30, 79.19, -107.86 }, convert(
			new double[] { 0, 0, 1 }, "RGB", "Lab"), 0.03);
		assertArrayEquals(new double[] { 53.24, 175.05, 37.76 }, convert(
			new double[] { 1, 0, 0 }, "RGB", "Luv"), 0.03);
		assertArrayEquals(new double[] { 32.30, -9.40, -130.35 }, convert(
			new double[] { 0, 0, 1 }, "RGB", "Luv"), 0.03);
	}

	@Test
	public void testHSB() {
		// 8-bit RGB inputs and their HSB values, worked out by hand.
		final int[][] rgb = { { 255, 0, 0 }, { 0, 128, 0 }, { 51, 102, 204 }, //
			{ 255, 255, 0 }, { 255, 0, 255 }, { 128, 128, 128 }, { 0, 255, 255 } };
		final double[][] hsb = { { 0, 1, 1 }, { 120, 1, 128 / 255.0 }, //
			{ 220, 0.75, 0.8 }, { 60, 1, 1 }, { 300, 1, 1 }, { 0, 0, 128 / 255.0 }, //
			{ 180, 1, 1 } };
		final var img = ArrayImgs.unsignedBytes(rgb.length, 3);
		final var ra = img.randomAccess();
		for (var i = 0; i < rgb.length; i++)
			for (var c = 0; c < 3; c++)
				ra.setPositionAndGet(i, c).set(rgb[i][c]);
		final var out = convert(img, "RGB", "HSV");
		final var oa = out.randomAccess();
		for (var i = 0; i < rgb.length; i++)
			for (var c = 0; c < 3; c++)
				assertEquals(hsb[i][c], oa.setPositionAndGet(i, c).get(), 1e-9);
	}

	@Test
	public void testYCbCrJFIF() {
		final var ycc = convert(new double[] { 0.2, 0.6, 0.9 }, "RGB", "YCbCr");
		final var y = 0.299 * 0.2 + 0.587 * 0.6 + 0.114 * 0.9;
		assertArrayEquals(new double[] { y, (0.9 - y) / 1.772, (0.2 - y) /
			1.402 }, ycc, 1e-12);
	}

	@Test
	public void testRoundTrips() {
		// A channel-first float image of random colours.
		final var rnd = new Random(11);
		final var img = ArrayImgs.doubles(3, 6, 5);
		for (final var t : img)
			t.set(rnd.nextDouble());
		for (final var space : SPACES) {
			final var there = convert(img, "RGB", space);
			for (final var other : SPACES) {
				final var via = convert(there, space, other);
				final var back = convert(via, other, "RGB");
				final var a = img.cursor();
				final var b = back.cursor();
				while (a.hasNext()) {
					assertEquals(a.next().get(), b.next().get(), 1e-9, space + " -> " +
						other);
				}
			}
		}
	}

	@Test
	public void testRGBRangeAndChannelDim() {
		// 16-bit input defaults to a range of 65535; output RGB uses the same.
		final var img = ArrayImgs.unsignedShorts(new short[] { (short) 65535, 0,
			(short) 65535, 0, (short) 65535, 0 }, 2, 3);
		final var lab = convert(img, "RGB", "Lab");
		final var ra = lab.randomAccess();
		assertEquals(100, ra.setPositionAndGet(0, 0).get(), 1e-9);
		assertEquals(0, ra.setPositionAndGet(1, 0).get(), 1e-9);

		final RandomAccessibleInterval<DoubleType> rgb = ops.op("color.convert")
			.input(lab, "Lab", "RGB", 1, 255.0).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();
		assertEquals(255, rgb.randomAccess().setPositionAndGet(0, 2).get(), 1e-9);

		assertThrows(IllegalArgumentException.class, () -> convert(img, "RGB",
			"CMYK"));
	}

	@Test
	public void testARGB() {
		final var img = ArrayImgs.argbs(2);
		img.randomAccess().setPositionAndGet(0).set(ARGBType.rgba(255, 0, 0,
			255));
		img.randomAccess().setPositionAndGet(1).set(ARGBType.rgba(128, 128, 128,
			0));
		final RandomAccessibleInterval<DoubleType> lab = ops.op("color.convert")
			.input(img, "CIELAB").outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();
		assertArrayEquals(new long[] { 2, 3 }, lab.dimensionsAsLongArray());
		final var expected = convert(new double[] { 1, 0, 0 }, "RGB", "Lab");
		final var ra = lab.randomAccess();
		for (var c = 0; c < 3; c++)
			assertEquals(expected[c], ra.setPositionAndGet(0, c).get(), 1e-9);
		assertEquals(0, ra.setPositionAndGet(1, 1).get(), 1e-9);
	}
}
