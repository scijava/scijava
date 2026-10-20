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

package org.scijava.ops.image.transform.bin;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultBin} and {@link DefaultBinFunction}.
 *
 * @author Curtis Rueden
 */
public class BinTest extends AbstractOpTest {

	// This is a 5x4 image whose last column is dropped when binning by 2 in x.
	private static final double[] DATA = { //
		1, 2, 3, 4, 99, //
		5, 6, 7, 1, 99, //
		1, 1, 9, 9, 99, //
		1, 3, 9, 2, 99 //
	};

	private RandomAccessibleInterval<FloatType> binned(final String method) {
		final var img = ArrayImgs.floats(toFloats(DATA), 5, 4);
		return ops.op("transform.bin").input(img, new long[] { 2, 2 }, method)
			.outType(new Nil<RandomAccessibleInterval<FloatType>>()
			{}).apply();
	}

	@Test
	public void testMethods() {
		// The blocks are {1,2,5,6}, {3,4,7,1}, {1,1,1,3} and {9,9,9,2}.
		assertArrayEquals(new double[] { 3.5, 3.75, 1.5, 7.25 }, asArray(binned(
			"mean")), 0);
		assertArrayEquals(new double[] { 3.5, 3.75, 1.5, 7.25 }, asArray(binned(
			null)), 0);
		assertArrayEquals(new double[] { 14, 15, 6, 29 }, asArray(binned("sum")),
			0);
		assertArrayEquals(new double[] { 1, 1, 1, 2 }, asArray(binned("min")), 0);
		assertArrayEquals(new double[] { 6, 7, 3, 9 }, asArray(binned("max")), 0);
		assertArrayEquals(new double[] { 3.5, 3.5, 1, 9 }, asArray(binned(
			"median")), 0);
		assertArrayEquals(new double[] { 1, 1, 1, 9 }, asArray(binned("mode")), 0);
	}

	@Test
	public void testDimensions() {
		final var out = binned("max");
		assertArrayEquals(new long[] { 2, 2 }, out.dimensionsAsLongArray());
		assertArrayEquals(new long[] { 0, 0 }, out.minAsLongArray());
	}

	@Test
	public void testPartialFactorsAndOffsetInput() {
		// A 3D image with non-zero min is binned by 3 along x only.
		final var img = ArrayImgs.doubles(6, 2, 2);
		var i = 0;
		for (final var t : img)
			t.set(i++);
		final var translated = Views.translate(img, 10, -4, 7);
		final RandomAccessibleInterval<DoubleType> out = ops.op("transform.bin")
			.input(translated, new long[] { 3 }, "sum").outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();
		assertArrayEquals(new long[] { 2, 2, 2 }, out.dimensionsAsLongArray());
		// Row k holds 6k..6k+5, so the sums are 18k+3 and 18k+12.
		assertArrayEquals(new double[] { 3, 12, 21, 30, 39, 48, 57, 66 }, asArray(
			out), 0);
	}

	@Test
	public void testIntegerRoundingAndClamping() {
		final var img = ArrayImgs.unsignedBytes(new byte[] { 1, 2, (byte) 200,
			(byte) 100 }, 4, 1);
		final RandomAccessibleInterval<UnsignedByteType> mean = ops.op(
			"transform.bin").input(img, new long[] { 2, 1 }, "mean").outType(
				new Nil<RandomAccessibleInterval<UnsignedByteType>>()
				{}).apply();
		// 1.5 rounds half up to 2.
		assertArrayEquals(new double[] { 2, 150 }, asArray(mean), 0);
		final RandomAccessibleInterval<UnsignedByteType> sum = ops.op(
			"transform.bin").input(img, new long[] { 2, 1 }, "sum").outType(
				new Nil<RandomAccessibleInterval<UnsignedByteType>>()
				{}).apply();
		// 300 saturates at 255.
		assertArrayEquals(new double[] { 3, 255 }, asArray(sum), 0);

		// The computer allows a wider output type.
		final var wide = ArrayImgs.doubles(2, 1);
		ops.op("transform.bin").input(img, new long[] { 2, 1 }, "sum").output(wide)
			.compute();
		assertArrayEquals(new double[] { 3, 300 }, asArray(wide), 0);
	}

	@Test
	public void testMeanPreservesTotal() {
		// Binning by mean then multiplying by block size preserves the total.
		final var img = ArrayImgs.doubles(8, 6);
		var i = 0;
		for (final var t : img)
			t.set(Math.sin(i++));
		final RandomAccessibleInterval<DoubleType> out = ops.op("transform.bin")
			.input(img, new long[] { 4, 3 }, "mean").outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();
		double inSum = 0, outSum = 0;
		for (final var t : img)
			inSum += t.get();
		for (final var t : asArray(out))
			outSum += t;
		assertEquals(inSum, outSum * 12, 1e-10);
	}

	@Test
	public void testInvalidArguments() {
		final var img = ArrayImgs.floats(4, 4);
		assertThrows(IllegalArgumentException.class, () -> ops.op("transform.bin")
			.input(img, new long[] { 0, 1 }, "mean").outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply());
		assertThrows(IllegalArgumentException.class, () -> ops.op("transform.bin")
			.input(img, new long[] { 5, 1 }, "mean").outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply());
		assertThrows(IllegalArgumentException.class, () -> ops.op("transform.bin")
			.input(img, new long[] { 2, 2 }, "average!").outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply());
	}

	private static float[] toFloats(final double[] values) {
		final var f = new float[values.length];
		for (var i = 0; i < f.length; i++)
			f[i] = (float) values[i];
		return f;
	}
}
