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

package org.scijava.ops.image.labeling.shapeBasedAverage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link ShapeBasedAverage}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class ShapeBasedAverageTest extends AbstractOpTest {

	private static final Nil<List<RandomAccessibleInterval<UnsignedByteType>>> IN =
		new Nil<>()
		{};

	private static final Nil<RandomAccessibleInterval<UnsignedByteType>> OUT =
		new Nil<>()
		{};

	/** Averaging copies of the same labeling returns it. */
	@Test
	public void testIdentical() {
		final var rnd = new Random(1);
		final var img = ArrayImgs.unsignedBytes(15, 12);
		img.forEach(t -> t.set(rnd.nextInt(4)));
		final var out = average(List.of(img, img, img));
		final var a = img.cursor();
		final var b = out.cursor();
		while (a.hasNext())
			assertEquals(a.next().get(), b.next().get());
	}

	/** Two half-plane labelings average to the mean boundary. */
	@Test
	public void testHalfPlanes() {
		final var a = halves(10);
		final var b = halves(14);
		final var out = average(List.of(a, b));
		final var c = out.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			assertEquals(c.getIntPosition(0) < 12 ? 1 : 2, c.get().get());
		}
	}

	/**
	 * Two disks of radius 6 shifted by ±4 pixels average to a symmetric lens
	 * around the midpoint: with mean signed distance
	 * {@code (|p - c1| + |p - c2|) / 2 - 6}, it extends 6 pixels along the
	 * shift and {@code sqrt(20) ≈ 4.5} pixels across it.
	 */
	@Test
	public void testShiftedDisks() {
		final var a = disk(12, 16, 6);
		final var b = disk(20, 16, 6);
		final var out = average(List.of(a, b));
		final var ra = out.randomAccess();
		assertEquals(1, ra.setPositionAndGet(16, 16).get());
		assertEquals(1, ra.setPositionAndGet(21, 16).get());
		assertEquals(1, ra.setPositionAndGet(11, 16).get());
		assertEquals(0, ra.setPositionAndGet(23, 16).get());
		assertEquals(0, ra.setPositionAndGet(9, 16).get());
		assertEquals(1, ra.setPositionAndGet(16, 20).get());
		assertEquals(1, ra.setPositionAndGet(16, 12).get());
		assertEquals(0, ra.setPositionAndGet(16, 22).get());
		assertEquals(0, ra.setPositionAndGet(16, 10).get());
		for (var x = 0; x < 32; x++) {
			for (var y = 0; y < 32; y++) {
				assertEquals(ra.setPositionAndGet(x, y).get(), ra.setPositionAndGet(
					32 - x, y).get(), x + "," + y);
				assertEquals(ra.setPositionAndGet(x, y).get(), ra.setPositionAndGet(x,
					32 - y).get(), x + "," + y);
			}
		}
	}

	private RandomAccessibleInterval<UnsignedByteType> average(
		final List<RandomAccessibleInterval<UnsignedByteType>> inputs)
	{
		return ops.op("labeling.shapeBasedAverage").inType(IN).outType(OUT)
			.function().apply(inputs);
	}

	private static RandomAccessibleInterval<UnsignedByteType> halves(
		final int boundary)
	{
		final var img = ArrayImgs.unsignedBytes(24, 6);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(c.getIntPosition(0) < boundary ? 1 : 2);
		}
		return img;
	}

	private static RandomAccessibleInterval<UnsignedByteType> disk(
		final double cx, final double cy, final double r)
	{
		final var img = ArrayImgs.unsignedBytes(33, 33);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final var dx = c.getDoublePosition(0) - cx;
			final var dy = c.getDoublePosition(1) - cy;
			c.get().set(dx * dx + dy * dy <= r * r ? 1 : 0);
		}
		return img;
	}
}
