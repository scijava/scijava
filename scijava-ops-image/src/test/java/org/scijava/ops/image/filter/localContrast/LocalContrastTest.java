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

package org.scijava.ops.image.filter.localContrast;

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
 * Tests {@link DefaultLocalContrast}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LocalContrastTest extends AbstractOpTest {

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
	public void testLocalContrastRegression() {
		final var lc = ops.op("filter.localContrast").input(PhantastTest.synth(), 1.2)
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
	public void testLocalContrastInvariants() {
		final var flat = ArrayImgs.floats(16, 16, 4);
		for (final var t : flat)
			t.set(42);
		final var lcFlat = ops.op("filter.localContrast").input(flat, 2.0)
			.outType(FLOATS).apply();
		for (final var t : Views.iterable(lcFlat))
			assertEquals(0, t.get(), 1e-5);

		final var img = PhantastTest.synth();
		final var scaled = ArrayImgs.floats(64, 64);
		final var c = img.cursor();
		final var sc = scaled.cursor();
		while (c.hasNext())
			sc.next().set(7 * c.next().get());
		final var lc = ops.op("filter.localContrast").input(img, 1.5).outType(
			FLOATS).apply();
		final var lcScaled = ops.op("filter.localContrast").input(scaled, 1.5)
			.outType(FLOATS).apply();
		final var a = Views.flatIterable(lc).cursor();
		final var b = Views.flatIterable(lcScaled).cursor();
		while (a.hasNext()) {
			final var expected = a.next().get();
			assertEquals(expected, b.next().get(), 1e-4 * Math.max(1, expected));
		}
	}

}
