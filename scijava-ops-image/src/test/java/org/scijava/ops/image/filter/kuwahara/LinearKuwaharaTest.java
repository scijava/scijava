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

package org.scijava.ops.image.filter.kuwahara;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@code filter.kuwaharaLinear}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LinearKuwaharaTest extends AbstractOpTest {

	private static void assertImagesEqual(
		final RandomAccessibleInterval<DoubleType> expected,
		final RandomAccessibleInterval<DoubleType> actual, final double tol)
	{
		final var e = expected.randomAccess();
		final var c = net.imglib2.view.Views.flatIterable(actual)
			.localizingCursor();
		while (c.hasNext()) {
			final var v = c.next().get();
			assertEquals(e.setPositionAndGet(c).get(), v, tol, () -> "at " + c);
		}
	}

	@Test
	public void testConstant() {
		final var img = ArrayImgs.doubles(12, 10);
		for (final var t : img)
			t.set(7);
		final var out = ArrayImgs.doubles(12, 10);
		ops.op("filter.kuwaharaLinear").input(img).output(out).compute();
		assertImagesEqual(img, out, 1e-9);
		ops.op("filter.kuwaharaLinear").input(img, 8, 5, "varianceOverMean")
			.output(out).compute();
		assertImagesEqual(img, out, 1e-9);
	}

	@Test
	public void testStepEdgePreserved() {
		final var img = ArrayImgs.doubles(16, 16);
		final var c = img.localizingCursor();
		while (c.hasNext())
			c.next().set(c.getLongPosition(0) < 8 ? 10 : 90);
		final var out = ArrayImgs.doubles(16, 16);
		ops.op("filter.kuwaharaLinear").input(img).output(out).compute();
		assertImagesEqual(img, out, 1e-6);
	}

	@Test
	public void testThinLinePreserved() {
		// A one-pixel bright diagonal line on a noisy background.
		final var size = 32;
		final var rnd = new Random(3);
		final var img = ArrayImgs.doubles(size, size);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			final var t = c.next();
			final var onLine = c.getLongPosition(0) == c.getLongPosition(1);
			t.set((onLine ? 100 : 0) + 10 * rnd.nextGaussian());
		}
		final var linear = ArrayImgs.doubles(size, size);
		ops.op("filter.kuwaharaLinear").input(img, 36).output(linear).compute();
		final var iso = ArrayImgs.doubles(size, size);
		ops.op("filter.kuwahara").input(img, 5).output(iso).compute();

		final var la = linear.randomAccess();
		final var ia = iso.randomAccess();
		double linearLine = 0, isoLine = 0, bgSq = 0, noisySq = 0;
		var nBg = 0;
		final var ra = img.randomAccess();
		for (var i = 6; i < size - 6; i++) {
			linearLine += la.setPositionAndGet(i, i).get();
			isoLine += ia.setPositionAndGet(i, i).get();
			final var bg = la.setPositionAndGet(i, (i + size / 2) % size).get();
			bgSq += bg * bg;
			final var n = ra.setPositionAndGet(i, (i + size / 2) % size).get();
			noisySq += n * n;
			nBg++;
		}
		linearLine /= size - 12;
		isoLine /= size - 12;
		// The line keeps its brightness, unlike with the isotropic filter.
		assertTrue(linearLine > 85, "linear line mean " + linearLine);
		assertTrue(isoLine < 50, "isotropic line mean " + isoLine);
		// The background noise is reduced.
		assertTrue(Math.sqrt(bgSq / nBg) < Math.sqrt(noisySq / nBg) / 2);
	}

	@Test
	public void testOrientedConstantMatchesSearch() {
		final var rnd = new Random(5);
		final var img = ArrayImgs.doubles(10, 9);
		for (final var t : img)
			t.set(rnd.nextDouble() * 50);
		final var zero = ArrayImgs.doubles(10, 9);
		final var a = ArrayImgs.doubles(10, 9);
		final var b = ArrayImgs.doubles(10, 9);
		ops.op("filter.kuwaharaLinear").input(img, zero, 7).output(a).compute();
		ops.op("filter.kuwaharaLinear").input(img, 1, 7).output(b).compute();
		assertImagesEqual(b, a, 1e-12);
	}

	@Test
	public void testOrientationAdaptiveStripes() {
		// Horizontal stripes three rows wide, with noise.
		final long w = 24, h = 24;
		final var rnd = new Random(9);
		final var clean = ArrayImgs.doubles(w, h);
		final var noisy = ArrayImgs.doubles(w, h);
		final var cc = clean.localizingCursor();
		final var nc = noisy.cursor();
		while (cc.hasNext()) {
			final var t = cc.next();
			t.set(cc.getLongPosition(1) / 3 % 2 == 0 ? 100 : 0);
			nc.next().set(t.get() + 5 * rnd.nextGaussian());
		}
		// On the clean image, the structure tensor orientation is exactly 0
		// (horizontal) and the stripes are reproduced exactly.
		final var orientation = orientation(clean);
		final var out = ArrayImgs.doubles(w, h);
		ops.op("filter.kuwaharaLinear").input(clean, orientation).output(out)
			.compute();
		assertImagesEqual(clean, out, 1e-9);

		// On the noisy image, smoothing along the stripes removes most noise.
		ops.op("filter.kuwaharaLinear").input(noisy, orientation(noisy)).output(
			out).compute();
		var errSq = 0.0;
		var noiseSq = 0.0;
		final var oc = out.cursor();
		final var c2 = clean.cursor();
		final var n2 = noisy.cursor();
		while (oc.hasNext()) {
			final var e = c2.next().get();
			errSq += Math.pow(oc.next().get() - e, 2);
			noiseSq += Math.pow(n2.next().get() - e, 2);
		}
		assertTrue(errSq < noiseSq / 4, errSq + " vs " + noiseSq);
	}

	@Test
	public void testInvalidArguments() {
		final var img = ArrayImgs.doubles(5, 5);
		assertThrows(RuntimeException.class, () -> ops.op("filter.kuwaharaLinear")
			.input(img, 4, 4).output(ArrayImgs.doubles(5, 5)).compute());
		assertThrows(RuntimeException.class, () -> ops.op("filter.kuwaharaLinear")
			.input(img, 4, 5, "median").output(ArrayImgs.doubles(5, 5)).compute());
	}

	private RandomAccessibleInterval<DoubleType> orientation(
		final ArrayImg<DoubleType, DoubleArray> img)
	{
		return ops.op("filter.orientation").input(img, 1.0, 2.0).outType(
			new Nil<RandomAccessibleInterval<DoubleType>>()
			{}).apply();
	}

	/**
	 * An 8-bit output saturates rather than wrapping around: the result must be
	 * the floating-point result clamped to [0, 255].
	 */
	@Test
	public void testUnsignedByteOutputIsClamped() {
		final var in = ArrayImgs.floats(16, 16);
		final var rnd = new Random(3);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().setReal((c.getIntPosition(0) < 8 ? 300 : -20) + rnd
				.nextDouble());
		}
		final var expected = ArrayImgs.floats(16, 16);
		ops.op("filter.kuwaharaLinear").input(in).output(expected).compute();
		final var actual = ArrayImgs.unsignedBytes(16, 16);
		ops.op("filter.kuwaharaLinear").input(in).output(actual).compute();
		final var ec = expected.cursor();
		var saturated = 0;
		for (final var t : actual) {
			final var e = Math.max(0, Math.min(255, ec.next().get()));
			assertEquals(e, t.get(), 0.5 + 1e-4);
			if (t.get() == 0 || t.get() == 255) saturated++;
		}
		assertTrue(saturated > 100, "saturated pixels: " + saturated);
	}
}
