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

package org.scijava.ops.image.filter.localCoefficientOfVariation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.segment.phantast.PhantastTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultLocalCoefficientOfVariation}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LocalCoefficientOfVariationTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<FloatType>> FLOATS =
		new Nil<>()
		{};

	/**
	 * Compares the local contrast to the values of the original PHANTAST
	 * plugin. The expected values were produced by calling
	 * {@code PHANTAST_.getLocalContrastImage(img, 1.2)} of PHANTAST-FIJI on
	 * {@link PhantastTest#synth()}. The plugin uses the older imglib2 {@code Gauss}, whose
	 * kernel differs slightly from that of {@code filter.gauss}.
	 */
	@Test
	public void testLocalCoefficientOfVariationRegression() {
		final var lc = ops.op("filter.localCoefficientOfVariation").input(PhantastTest.synth(), 1.2)
			.outType(FLOATS).apply();
		final var ra = lc.randomAccess();
		final int[][] pos = { { 0, 0 }, { 20, 22 }, { 10, 20 }, { 33, 22 }, { 40,
			5 }, { 46, 40 }, { 63, 63 } };
		final double[] expected = { 0.014355456, 0.023312092, 0.14764106,
			0.23673302, 0.01423413, 0.09012429, 0.015692648 };
		for (var i = 0; i < pos.length; i++) {
			final var actual = ra.setPositionAndGet(pos[i]).getRealDouble();
			assertEquals(expected[i], actual, 0.02 * expected[i], "at " + pos[i][0] +
				"," + pos[i][1]);
		}
	}

	/** The local contrast is 0 on flat images and invariant to scaling. */
	@Test
	public void testLocalCoefficientOfVariationInvariants() {
		final var flat = ArrayImgs.floats(16, 16, 4);
		for (final var t : flat)
			t.set(42);
		final var lcFlat = ops.op("filter.localCoefficientOfVariation").input(flat, 2.0)
			.outType(FLOATS).apply();
		for (final var t : Views.iterable(lcFlat))
			assertEquals(0, t.get(), 1e-5);

		final var img = PhantastTest.synth();
		final var scaled = ArrayImgs.floats(64, 64);
		final var c = img.cursor();
		final var sc = scaled.cursor();
		while (c.hasNext())
			sc.next().set(7 * c.next().get());
		final var lc = ops.op("filter.localCoefficientOfVariation").input(img, 1.5).outType(
			FLOATS).apply();
		final var lcScaled = ops.op("filter.localCoefficientOfVariation").input(scaled, 1.5)
			.outType(FLOATS).apply();
		final var a = Views.flatIterable(lc).cursor();
		final var b = Views.flatIterable(lcScaled).cursor();
		while (a.hasNext()) {
			final var expected = a.next().get();
			assertEquals(expected, b.next().get(), 1e-4 * Math.max(1, expected));
		}
	}

	/**
	 * A 16-bit checkerboard of {@code 40000 ± 5} has a local mean of 40000 and a
	 * local standard deviation of 5 wherever the Gaussian window averages over
	 * many squares, so {@code C = 5 / 40000}. Computing {@code E[I²] - E[I]²} in
	 * single precision loses this entirely, since {@code E[I²] ≈ 1.6e9} has a
	 * float ulp of 128.
	 */
	@Test
	public void testSixteenBitWithLargeOffset() {
		final int n = 32;
		final var img = ArrayImgs.unsignedShorts(n, n);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final var odd = (c.getIntPosition(0) + c.getIntPosition(1)) % 2 != 0;
			c.get().set(odd ? 40005 : 39995);
		}
		final var lc = ops.op("filter.localCoefficientOfVariation").input(img, 3.0).outType(
			FLOATS).apply();
		final var ra = lc.randomAccess();
		for (int y = 10; y < n - 10; y++) {
			for (int x = 10; x < n - 10; x++) {
				assertEquals(5.0 / 40000, ra.setPositionAndGet(x, y).get(), 1e-2 * 5.0 /
					40000, "at " + x + "," + y);
			}
		}
	}

}
