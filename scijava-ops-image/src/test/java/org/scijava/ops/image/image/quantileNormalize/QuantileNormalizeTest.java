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

package org.scijava.ops.image.image.quantileNormalize;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link QuantileNormalize}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class QuantileNormalizeTest extends AbstractOpTest {

	private static final Nil<List<RandomAccessibleInterval<DoubleType>>> IMAGES =
		new Nil<>()
		{};

	private static final Nil<List<RandomAccessibleInterval<DoubleType>>> OUT =
		new Nil<>()
		{};

	/**
	 * With one class per pixel, the k-th smallest value of every image becomes
	 * the mean of the k-th smallest values (Bolstad et al. 2003).
	 */
	@Test
	public void testClassic() {
		final var a = ArrayImgs.doubles(new double[] { 5, 2, 3, 4 }, 2, 2);
		final var b = ArrayImgs.doubles(new double[] { 4, 1, 4, 2 }, 2, 2);
		final var c = ArrayImgs.doubles(new double[] { 3, 4, 6, 8 }, 2, 2);
		final List<RandomAccessibleInterval<DoubleType>> images = List.of(a, b, c);
		final var out = normalize(images, 4);
		// Sorted columns: (2, 3, 4, 5), (1, 2, 4, 4), (3, 4, 6, 8).
		// Means: 2, 3, 14/3, 17/3.
		assertArrayEquals(new double[] { 17. / 3, 2, 3, 14. / 3 }, values(out.get(
			0)), 1e-12);
		// Note: The tied 4s rank in flat iteration order.
		assertArrayEquals(new double[] { 14. / 3, 2, 17. / 3, 3 }, values(out.get(
			1)), 1e-12);
		assertArrayEquals(new double[] { 2, 3, 14. / 3, 17. / 3 }, values(out.get(
			2)), 1e-12);
	}

	/** After normalization, all images have the same sorted values. */
	@Test
	public void testSameDistribution() {
		final var rnd = new Random(1);
		final List<RandomAccessibleInterval<DoubleType>> images = new ArrayList<>();
		for (var i = 0; i < 3; i++) {
			final var img = ArrayImgs.doubles(20, 10);
			final var scale = 1 + i;
			img.forEach(t -> t.set(scale * rnd.nextGaussian() + 10 * scale));
			images.add(img);
		}
		final var out = normalize(images, 50);
		final var ref = sorted(out.get(0));
		assertEquals(50, Arrays.stream(ref).distinct().count());
		for (var i = 1; i < 3; i++)
			assertArrayEquals(ref, sorted(out.get(i)), 1e-12);
		// Ranks are preserved.
		for (var i = 0; i < 3; i++) {
			final var in = values(images.get(i));
			final var o = values(out.get(i));
			for (var p = 0; p < in.length; p++)
				for (var r = 0; r < in.length; r++)
					if (in[p] < in[r]) assertEquals(true, o[p] <= o[r]);
		}

		// A single image with one class per pixel is unchanged.
		final var single = normalize(List.of(images.get(0)),
			200);
		assertArrayEquals(values(images.get(0)), values(single.get(0)), 1e-12);
	}

	/** Pixels outside the masks keep their values and do not contribute. */
	@Test
	public void testMasks() {
		final var a = ArrayImgs.doubles(new double[] { 1, 2, 100, 3 }, 4);
		final var b = ArrayImgs.doubles(new double[] { 10, 20, 30, -5 }, 4);
		final var ma = ArrayImgs.bits(4);
		final var mb = ArrayImgs.bits(4);
		final var ra = ma.randomAccess();
		final var rb = mb.randomAccess();
		for (final var p : new int[] { 0, 1, 3 }) {
			ra.setPositionAndGet(p).set(true);
		}
		for (final var p : new int[] { 0, 1, 2 }) {
			rb.setPositionAndGet(p).set(true);
		}
		final List<RandomAccessibleInterval<DoubleType>> images = List.of(a, b);
		final List<RandomAccessibleInterval<BitType>> masks = List.of(ma, mb);
		final var out = normalize(images, 3, masks);
		assertArrayEquals(new double[] { 5.5, 11, 100, 16.5 }, values(out.get(0)),
			1e-12);
		assertArrayEquals(new double[] { 5.5, 11, 16.5, -5 }, values(out.get(1)),
			1e-12);
	}

	private List<RandomAccessibleInterval<DoubleType>> normalize(
		final List<RandomAccessibleInterval<DoubleType>> images, final int q)
	{
		return ops.op("image.quantileNormalize").inType(IMAGES, new Nil<Integer>()
		{}).outType(OUT).function().apply(images, q);
	}

	private List<RandomAccessibleInterval<DoubleType>> normalize(
		final List<RandomAccessibleInterval<DoubleType>> images, final int q,
		final List<RandomAccessibleInterval<BitType>> masks)
	{
		return ops.op("image.quantileNormalize").inType(IMAGES, new Nil<Integer>()
		{}, new Nil<List<RandomAccessibleInterval<BitType>>>()
		{}).outType(OUT).function().apply(images, q, masks);
	}

	private static double[] values(final RandomAccessibleInterval<DoubleType> img) {
		final var list = new ArrayList<Double>();
		for (final var t : Views.flatIterable(img))
			list.add(t.get());
		return list.stream().mapToDouble(Double::doubleValue).toArray();
	}

	private static double[] sorted(final RandomAccessibleInterval<DoubleType> img) {
		final var v = values(img);
		Arrays.sort(v);
		return v;
	}
}
