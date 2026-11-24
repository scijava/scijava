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

package org.scijava.ops.image.morphology.directional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.HorizontalLineShape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DirectionalFilter} and {@link OrientedLineShape}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DirectionalFilterTest extends AbstractOpTest {

	@Test
	public void testShapeOffsets() {
		final var horizontal = new OrientedLineShape(5, 0).getOffsets();
		assertEquals(5, horizontal.length);
		for (var i = 0; i < 5; i++) {
			assertEquals(i - 2, horizontal[i][0]);
			assertEquals(0, horizontal[i][1]);
		}
		final var vertical = new OrientedLineShape(7, Math.PI / 2).getOffsets();
		assertEquals(7, vertical.length);
		for (final var o : vertical)
			assertEquals(0, o[0]);

		// A diagonal segment of length 21 spans about 21 / sqrt(2) pixels.
		final var diagonal = new OrientedLineShape(21, Math.PI / 4).getOffsets();
		assertEquals(15, diagonal.length);
		for (final var o : diagonal)
			assertEquals(o[0], o[1]);

		// Every segment is symmetric about the center and contains it.
		final var rnd = new Random(5);
		for (var t = 0; t < 50; t++) {
			final var offsets = new OrientedLineShape(1 + 30 * rnd.nextDouble(), rnd
				.nextDouble() * 2 * Math.PI).getOffsets();
			final var set = new HashSet<String>();
			for (final var o : offsets)
				set.add(o[0] + "," + o[1]);
			assertTrue(set.contains("0,0"));
			assertEquals(offsets.length, set.size());
			for (final var o : offsets)
				assertTrue(set.contains(-o[0] + "," + -o[1]));
		}
	}

	@Test
	public void testShapeNeighborhoods() {
		final var img = ArrayImgs.doubles(9, 9);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(10 * c.getIntPosition(1) + c.getIntPosition(0));
		}
		final var shape = new OrientedLineShape(7, Math.PI / 4);
		final var ra = shape.neighborhoodsRandomAccessible(img).randomAccess();
		final var nbh = ra.setPositionAndGet(4, 4);
		assertEquals(5, nbh.size());
		var sum = 0.0;
		for (final var v : nbh)
			sum += v.get();
		// Pixels (2,2), (3,3), ..., (6,6).
		assertEquals(5 * 44, sum, 0);
		assertEquals(2, nbh.min(0));
		assertEquals(6, nbh.max(1));
	}

	/** With one horizontal direction, erosion is a horizontal minimum filter. */
	@Test
	public void testSingleDirectionErosion() {
		final var in = random(20, 15, 1);
		final var out = ArrayImgs.doubles(20, 15);
		ops.op("morphology.directionalFilter").input(in, "erosion", 7.0, 1)
			.output(out).compute();
		final var expected = ArrayImgs.doubles(20, 15);
		ops.op("filter.min").input(in, new HorizontalLineShape(3, 0, false),
			new OutOfBoundsBorderFactory<>()).output(expected).compute();
		assertImagesEqual(expected, out, 0);
	}

	/**
	 * The maximum of directional openings keeps thin lines of any orientation
	 * that are longer than the segment, and removes small spots.
	 */
	@Test
	public void testOpeningKeepsLines() {
		final var in = ArrayImgs.doubles(40, 40);
		final var ra = in.randomAccess();
		for (var i = 5; i < 35; i++) {
			ra.setPositionAndGet(i, 30).set(80); // horizontal line
			ra.setPositionAndGet(i, i).set(100); // diagonal line
			ra.setPositionAndGet(30 + (i - 5) / 3, i).set(60); // steep line
		}
		for (var x = 3; x < 6; x++)
			for (var y = 20; y < 23; y++)
				ra.setPositionAndGet(x + 10, y).set(200); // 3x3 spot
		final var out = ArrayImgs.doubles(40, 40);
		ops.op("morphology.directionalFilter").input(in, "opening", 9.0, 32)
			.output(out).compute();
		final var o = out.randomAccess();
		for (var i = 5; i < 35; i++) {
			if (i == 30) continue; // The crossing of the two lines.
			assertEquals(100, o.setPositionAndGet(i, i).get(), 0);
			assertEquals(80, o.setPositionAndGet(i, 30).get(), 0);
		}
		// The steep line is a digital line of slope 3 at a direction between the
		// sampled orientations, so it is mostly but not exactly preserved.
		var kept = 0;
		for (var i = 5; i < 35; i++)
			if (o.setPositionAndGet(30 + (i - 5) / 3, i).get() == 60) kept++;
		assertTrue(kept >= 24, "kept " + kept);
		for (var x = 3; x < 6; x++)
			for (var y = 20; y < 23; y++)
				assertEquals(0, o.setPositionAndGet(x + 10, y).get(), 0);
		// Opening is anti-extensive.
		final var ic = in.cursor();
		final var oc = out.cursor();
		while (ic.hasNext())
			assertTrue(oc.next().get() <= ic.next().get());
	}

	/** Closing and min-combination are dual to opening and max-combination. */
	@Test
	public void testDuality() {
		final var in = random(25, 20, 2);
		final var neg = ArrayImgs.doubles(25, 20);
		negate(in, neg);
		final var open = ArrayImgs.doubles(25, 20);
		ops.op("morphology.directionalFilter").input(in, "opening", 6.0, 8, "max")
			.output(open).compute();
		final var close = ArrayImgs.doubles(25, 20);
		ops.op("morphology.directionalFilter").input(neg, "closing", 6.0, 8, "min")
			.output(close).compute();
		final var negClose = ArrayImgs.doubles(25, 20);
		negate(close, negClose);
		assertImagesEqual(open, negClose, 1e-12);
	}

	@Test
	public void testMeanAndMedian() {
		final var in = ArrayImgs.doubles(12, 10);
		in.forEach(t -> t.set(7));
		for (final var op : new String[] { "mean", "median" }) {
			final var out = ArrayImgs.doubles(12, 10);
			ops.op("morphology.directionalFilter").input(in, op, 5.0, 4).output(out)
				.compute();
			out.forEach(t -> assertEquals(7, t.get(), 1e-12));
		}

		// The max of directional means is at least the isotropic mean along any
		// sampled line, e.g. the horizontal one.
		final var rnd = random(16, 16, 3);
		final var out = ArrayImgs.doubles(16, 16);
		ops.op("morphology.directionalFilter").input(rnd, "mean", 5.0, 4).output(
			out).compute();
		final var horiz = ArrayImgs.doubles(16, 16);
		ops.op("filter.mean").input(rnd, new HorizontalLineShape(2, 0, false),
			new OutOfBoundsBorderFactory<>()).output(horiz).compute();
		final var hc = horiz.cursor();
		final var oc = out.cursor();
		while (hc.hasNext())
			assertTrue(oc.next().get() >= hc.next().get() - 1e-12);
	}

	/** On a 3D image, each plane is filtered independently. */
	@Test
	public void testPlaneWise() {
		final var in = ArrayImgs.doubles(10, 9, 3);
		final var rnd = new Random(4);
		in.forEach(t -> t.set(rnd.nextInt(50)));
		final var out = ArrayImgs.doubles(10, 9, 3);
		ops.op("morphology.directionalFilter").input(in, "opening", 5.0, 6)
			.output(out).compute();
		for (var z = 0; z < 3; z++) {
			final var plane = ArrayImgs.doubles(10, 9);
			ops.op("morphology.directionalFilter").input(Views.hyperSlice(in, 2, z),
				"opening", 5.0, 6).output(plane).compute();
			assertImagesEqual(plane, Views.hyperSlice(out, 2, z), 0);
		}
	}

	@Test
	public void testInvalidArguments() {
		final var in = ArrayImgs.doubles(5, 5);
		final var out = ArrayImgs.doubles(5, 5);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"morphology.directionalFilter").input(in, "blur", 5.0, 4).output(out)
			.compute());
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"morphology.directionalFilter").input(in, "opening", 5.0, 4, "mean")
			.output(out).compute());
	}

	private static RandomAccessibleInterval<DoubleType> random(final long w,
		final long h, final long seed)
	{
		final var rnd = new Random(seed);
		final var img = ArrayImgs.doubles(w, h);
		img.forEach(t -> t.set(rnd.nextInt(100)));
		return img;
	}

	private static void negate(final RandomAccessibleInterval<DoubleType> in,
		final RandomAccessibleInterval<DoubleType> out)
	{
		final var ic = Views.flatIterable(in).cursor();
		final var oc = Views.flatIterable(out).cursor();
		while (ic.hasNext())
			oc.next().set(-ic.next().get());
	}

	private static void assertImagesEqual(
		final RandomAccessibleInterval<DoubleType> expected,
		final RandomAccessibleInterval<DoubleType> actual, final double tol)
	{
		final var ec = Views.flatIterable(expected).cursor();
		final var ac = Views.flatIterable(actual).cursor();
		while (ec.hasNext())
			assertEquals(ec.next().get(), ac.next().get(), tol);
	}
}
