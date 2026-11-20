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

package org.scijava.ops.image.transform.focus;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link FocusStack} and {@link FocusStackIndex}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class FocusStackTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<FloatType>> FLOATS =
		new Nil<>()
		{};

	private static final Nil<RandomAccessibleInterval<IntType>> INTS =
		new Nil<>()
		{};

	private static final int W = 48, H = 24;

	/** A checkerboard texture of 2x2-pixel squares. */
	private static double sharp(final long x, final long y) {
		return ((x / 2 + y / 2) % 2 == 0) ? 200 : 50;
	}

	/**
	 * Two slices: slice 0 is sharp in the left half and blurred in the right
	 * half, slice 1 the other way around. The stack is laid out as (z, x, y)
	 * to test a focus dimension other than the last.
	 */
	private static RandomAccessibleInterval<FloatType> stack() {
		final var texture = ArrayImgs.floats(W, H);
		final var c = texture.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().setReal(sharp(c.getLongPosition(0), c.getLongPosition(1)));
		}
		final var blurred = ArrayImgs.floats(W, H);
		Gauss3.gauss(3, Views.extendBorder(texture), blurred);
		final var stack = ArrayImgs.floats(2, W, H);
		final var ra = stack.randomAccess();
		final var tr = texture.randomAccess();
		final var br = blurred.randomAccess();
		for (var y = 0; y < H; y++)
			for (var x = 0; x < W; x++) {
				final var s = tr.setPositionAndGet(x, y).get();
				final var b = br.setPositionAndGet(x, y).get();
				ra.setPositionAndGet(0, x, y).set(x < W / 2 ? s : b);
				ra.setPositionAndGet(1, x, y).set(x < W / 2 ? b : s);
			}
		return stack;
	}

	@Test
	public void testMax() {
		final var out = ops.op("transform.focusStack").input(stack(), 0).outType(
			FLOATS).apply();
		assertArrayEquals(new long[] { W, H }, out.dimensionsAsLongArray());
		final var ra = out.randomAccess();
		for (var y = 0; y < H; y++)
			for (var x = 0; x < W; x++) {
				if (Math.abs(x - W / 2) < 4) continue;
				assertEquals(sharp(x, y), ra.setPositionAndGet(x, y).get(), 1e-4,
					"at " + x + "," + y);
			}
		final var index = ops.op("transform.focusStackIndex").input(stack(), 0)
			.outType(INTS).apply();
		final var ir = index.randomAccess();
		for (var y = 0; y < H; y++)
			for (var x = 0; x < W; x++) {
				if (Math.abs(x - W / 2) < 4) continue;
				assertEquals(x < W / 2 ? 0 : 1, ir.setPositionAndGet(x, y).get());
			}
	}

	@Test
	public void testWeighted() {
		final var out = ops.op("transform.focusStack").input(stack(), 0, 2.0,
			"weighted").outType(FLOATS).apply();
		final var ra = out.randomAccess();
		double err = 0, errBlur = 0;
		final var st = stack().randomAccess();
		for (var y = 0; y < H; y++)
			for (var x = 0; x < W; x++) {
				if (Math.abs(x - W / 2) < 4) continue;
				final var s = sharp(x, y);
				err += Math.abs(ra.setPositionAndGet(x, y).get() - s);
				errBlur += Math.abs(st.setPositionAndGet(x < W / 2 ? 1 : 0, x, y)
					.get() - s);
			}
		assertTrue(err < 0.05 * errBlur, "error " + err + " vs " + errBlur);
	}

	@Test
	public void testConstantStack() {
		final var in = ArrayImgs.unsignedBytes(5, 6, 3);
		in.forEach(t -> t.set(77));
		for (final var method : new String[] { "max", "weighted" }) {
			final var out = ops.op("transform.focusStack").input(in, 2, 1.0, method)
				.outType(FLOATS).apply();
			assertArrayEquals(new long[] { 5, 6 }, out.dimensionsAsLongArray());
			for (final var t : out)
				assertEquals(77, t.get(), 1e-6);
		}
	}

	/**
	 * A translated stack gives the results of the original stack, at the
	 * translated coordinates (without the focus dimension).
	 */
	@Test
	public void testTranslatedInput() {
		final var translated = Views.translate(stack(), 3, -5, 7);
		for (final var method : new String[] { "max", "weighted" }) {
			final var expected = ops.op("transform.focusStack").input(stack(), 0,
				1.0, method).outType(FLOATS).apply();
			final var out = ops.op("transform.focusStack").input(translated, 0, 1.0,
				method).outType(FLOATS).apply();
			assertArrayEquals(new long[] { -5, 7 }, out.minAsLongArray());
			assertArrayEquals(new long[] { W - 6, H + 6 }, out.maxAsLongArray());
			final var er = expected.randomAccess();
			final var oc = out.localizingCursor();
			while (oc.hasNext()) {
				final var v = oc.next().get();
				er.setPosition(new long[] { oc.getLongPosition(0) + 5, oc
					.getLongPosition(1) - 7 });
				assertEquals(er.get().get(), v, 1e-5, method);
			}
		}
		final var expectedIndex = ops.op("transform.focusStackIndex").input(
			stack(), 0).outType(INTS).apply();
		final var index = ops.op("transform.focusStackIndex").input(translated, 0)
			.outType(INTS).apply();
		assertArrayEquals(new long[] { -5, 7 }, index.minAsLongArray());
		final var er = expectedIndex.randomAccess();
		final var ic = index.localizingCursor();
		while (ic.hasNext()) {
			final var v = ic.next().get();
			er.setPosition(new long[] { ic.getLongPosition(0) + 5, ic
				.getLongPosition(1) - 7 });
			assertEquals(er.get().get(), v);
		}
	}
}
