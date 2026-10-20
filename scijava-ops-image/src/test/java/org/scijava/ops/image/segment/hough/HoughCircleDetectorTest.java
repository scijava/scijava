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
package org.scijava.ops.image.segment.hough;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link HoughCircleDetector}.
 *
 * @author Curtis Rueden
 */
public class HoughCircleDetectorTest extends AbstractOpTest {

	/**
	 * Draws the discrete sphere (offsets whose norm rounds to the radius) into
	 * the image, restricted to points whose first coordinate is at least
	 * {@code xMin}.
	 */
	private static void drawSphere(final RandomAccessibleInterval<BitType> img,
		final long[] center, final long radius, final long xMin)
	{
		final var c = Views.iterable(img).localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			if (c.getLongPosition(0) < xMin) continue;
			var r2 = 0.0;
			for (var d = 0; d < center.length; d++) {
				final var dx = c.getDoublePosition(d) - center[d];
				r2 += dx * dx;
			}
			if (Math.round(Math.sqrt(r2)) == radius) c.get().set(true);
		}
	}

	private List<HoughCircle> detect(final RandomAccessibleInterval<BitType> img,
		final int minRadius, final int maxRadius, final Integer stepRadius,
		final Double sensitivity)
	{
		final RandomAccessibleInterval<DoubleType> votes = ops.op(
			"transform.houghCircle").input(img, minRadius, maxRadius, stepRadius)
			.outType(new Nil<RandomAccessibleInterval<DoubleType>>()
			{}).apply();
		return ops.op("segment.detectHoughCircles") //
			.input(votes, minRadius, stepRadius, sensitivity) //
			.outType(new Nil<List<HoughCircle>>()
			{}).apply();
	}

	private static void assertCircle(final HoughCircle circle,
		final double radius, final double... center)
	{
		assertEquals(center.length, circle.numDimensions());
		for (var d = 0; d < center.length; d++)
			assertEquals(center[d], circle.getDoublePosition(d), 0.5);
		assertEquals(radius, circle.getRadius(), 0.5);
	}

	/** Two complete rings are found, with sensitivity close to 1. */
	@Test
	public void testTwoRings() {
		final var img = ArrayImgs.bits(64, 50);
		drawSphere(img, new long[] { 16, 15 }, 8, Long.MIN_VALUE);
		drawSphere(img, new long[] { 44, 30 }, 12, Long.MIN_VALUE);
		final var circles = detect(img, 5, 15, null, 3.0);
		assertEquals(2, circles.size(), circles.toString());
		final var small = circles.get(0).getRadius() < circles.get(1).getRadius()
			? circles.get(0) : circles.get(1);
		final var large = small == circles.get(0) ? circles.get(1) : circles.get(
			0);
		assertCircle(small, 8, 16, 15);
		assertCircle(large, 12, 44, 30);
		for (final var c : circles)
			assertEquals(1, c.getSensitivity(), 0.25);
		assertTrue(circles.get(0).getSensitivity() <= circles.get(1)
			.getSensitivity());
	}

	/**
	 * The permissive default sensitivity also reports weak spurious circles, but
	 * the true circles come first.
	 */
	@Test
	public void testDefaultSensitivity() {
		final var img = ArrayImgs.bits(64, 50);
		drawSphere(img, new long[] { 16, 15 }, 8, Long.MIN_VALUE);
		drawSphere(img, new long[] { 44, 30 }, 12, Long.MIN_VALUE);
		final var circles = detect(img, 5, 15, null, null);
		assertTrue(circles.size() > 2);
		assertCircle(circles.get(0), 8, 16, 15);
		assertCircle(circles.get(1), 12, 44, 30);
		for (final var c : circles.subList(2, circles.size()))
			assertTrue(c.getSensitivity() > 3 && c.getSensitivity() <= 20);
	}

	/** A radius step greater than 1 still yields the right radius. */
	@Test
	public void testRadiusStep() {
		final var img = ArrayImgs.bits(40, 40);
		drawSphere(img, new long[] { 20, 19 }, 11, Long.MIN_VALUE);
		final var circles = detect(img, 5, 17, 2, 3.0);
		assertEquals(1, circles.size(), circles.toString());
		assertCircle(circles.get(0), 11, 20, 19);
	}

	/**
	 * A half ring has sensitivity close to 2, so it is only found with a
	 * permissive enough sensitivity.
	 */
	@Test
	public void testSensitivity() {
		final var img = ArrayImgs.bits(40, 40);
		drawSphere(img, new long[] { 20, 20 }, 10, 20);
		final var circles = detect(img, 8, 12, null, 3.0);
		assertEquals(1, circles.size(), circles.toString());
		assertCircle(circles.get(0), 10, 20, 20);
		assertEquals(2, circles.get(0).getSensitivity(), 0.5);
		assertEquals(0, detect(img, 8, 12, null, 1.5).size());
	}

	/** A 3D spherical shell is found as a sphere. */
	@Test
	public void testSphere3D() {
		final var img = ArrayImgs.bits(24, 22, 20);
		drawSphere(img, new long[] { 12, 10, 9 }, 6, Long.MIN_VALUE);
		final var circles = detect(img, 4, 8, null, 3.0);
		assertEquals(1, circles.size(), circles.toString());
		assertCircle(circles.get(0), 6, 12, 10, 9);
		assertEquals(1, circles.get(0).getSensitivity(), 0.25);
	}

	@Test
	public void testEmpty() {
		assertTrue(detect(ArrayImgs.bits(20, 20), 3, 6, null, null).isEmpty());
	}

	@Test
	public void testInvalidSensitivity() {
		assertThrows(IllegalArgumentException.class, () -> detect(ArrayImgs.bits(
			10, 10), 1, 3, null, 0.0));
	}
}
