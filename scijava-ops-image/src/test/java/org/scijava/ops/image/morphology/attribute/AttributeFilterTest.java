/* #%L
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

package org.scijava.ops.image.morphology.attribute;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.HorizontalLineShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests the attribute openings and closings against their definition by
 * threshold decomposition, computed by brute force.
 * <p>
 * For an increasing attribute, the opening of {@code f} at a pixel {@code p}
 * is the highest level {@code t} such that the connected component of
 * {@code {f >= t}} containing {@code p} has an attribute of at least the
 * threshold (or covers a whole connected component of the image); for the
 * area attribute this is the maximum, over all connected sets of at least
 * {@code minArea} pixels containing {@code p}, of the minimum of {@code f} on
 * the set. The reference implementation floods each such component
 * explicitly, independently of the component tree.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class AttributeFilterTest extends AbstractOpTest {

	private static final String[] ATTRIBUTES = { "area", "boxDiagonal",
		"grayVolume" };

	@Test
	public void testRandom2D() {
		final var random = new Random(1);
		for (var trial = 0; trial < 6; trial++) {
			final var img = randomImage(random, new long[] { 9, 7 }, 4);
			for (final var shape : shapes()) {
				check(img, "area", new double[] { 1, 2, 4, 7, 15, 40, 64 }, shape);
				check(img, "boxDiagonal", new double[] { 0.5, 1, 1.5, 2.5, 4, 7, 20 },
					shape);
				check(img, "grayVolume", new double[] { 0.5, 2, 5, 12, 30, 100 },
					shape);
			}
		}
	}

	@Test
	public void testRandom3D() {
		final var random = new Random(2);
		for (var trial = 0; trial < 3; trial++) {
			final var img = randomImage(random, new long[] { 5, 4, 3 }, 5);
			for (final var shape : shapes()) {
				check(img, "area", new double[] { 2, 5, 11, 30 }, shape);
				check(img, "boxDiagonal", new double[] { 1, 2, 3.2, 6 }, shape);
				check(img, "grayVolume", new double[] { 3, 10, 50 }, shape);
			}
		}
	}

	@Test
	public void testRandomFloat() {
		// Note: continuous values make (almost) every pixel its own level.
		final var random = new Random(3);
		final var img = ArrayImgs.doubles(8, 6);
		for (final var t : img)
			t.set(random.nextGaussian());
		for (final var shape : shapes()) {
			check(img, "area", new double[] { 3, 9 }, shape);
			check(img, "boxDiagonal", new double[] { 2, 3.5 }, shape);
			check(img, "grayVolume", new double[] { 0.5, 4 }, shape);
		}
	}

	@Test
	public void testPlateaus() {
		// A plateau of 4 pixels at 5 holding a 2-pixel peak at 8, a lone peak
		// of 1 pixel at 7 and a 6-pixel dark hole at 0, on a background of 3.
		final var img = image(new long[] { 8, 4 }, //
			3, 3, 3, 3, 3, 3, 3, 3, //
			3, 5, 8, 3, 7, 0, 0, 0, //
			3, 5, 8, 3, 3, 0, 0, 0, //
			3, 5, 5, 3, 3, 3, 3, 3);
		assertArrayEquals(new double[] { //
			3, 3, 3, 3, 3, 3, 3, 3, //
			3, 5, 5, 3, 3, 0, 0, 0, //
			3, 5, 5, 3, 3, 0, 0, 0, //
			3, 5, 5, 3, 3, 3, 3, 3 }, values(filter("morphology.areaOpening", img,
				3L, new DiamondShape(1))), 0);
		assertArrayEquals(new double[] { //
			3, 3, 3, 3, 3, 3, 3, 3, //
			3, 5, 8, 3, 7, 3, 3, 3, //
			3, 5, 8, 3, 3, 3, 3, 3, //
			3, 5, 5, 3, 3, 3, 3, 3 }, values(filter("morphology.areaClosing", img,
				7L, new DiamondShape(1))), 0);
	}

	@Test
	public void testTrivialThresholds() {
		final var img = randomImage(new Random(4), new long[] { 7, 6 }, 6);
		final var expected = values(img);
		assertArrayEquals(expected, values(filter("morphology.areaOpening", img,
			1L, null)), 0);
		assertArrayEquals(expected, values(filter("morphology.areaClosing", img,
			0L, null)), 0);
		assertArrayEquals(expected, values(filter("morphology.boxDiagonalOpening",
			img, 0.0, null)), 0);
		assertArrayEquals(expected, values(filter(
			"morphology.grayVolumeClosing", img, 0.0, null)), 0);

		// A size larger than the image flattens it to its minimum or maximum.
		final var min = Arrays.stream(expected).min().getAsDouble();
		final var max = Arrays.stream(expected).max().getAsDouble();
		final var flatMin = new double[expected.length];
		final var flatMax = new double[expected.length];
		Arrays.fill(flatMin, min);
		Arrays.fill(flatMax, max);
		assertArrayEquals(flatMin, values(filter("morphology.areaOpening", img,
			43L, null)), 0);
		assertArrayEquals(flatMax, values(filter("morphology.areaClosing", img,
			43L, null)), 0);
	}

	@Test
	public void testOpeningProperties() {
		final var img = randomImage(new Random(5), new long[] { 10, 8 }, 8);
		final var f = values(img);
		for (final var name : new String[] { "morphology.areaOpening",
			"morphology.areaClosing" })
		{
			final var once = filter(name, img, 6L, null);
			final var twice = filter(name, once, 6L, null);
			assertArrayEquals(values(once), values(twice), 0, "idempotence");
			final var g = values(once);
			for (var i = 0; i < f.length; i++) {
				assertTrue(name.endsWith("Opening") ? g[i] <= f[i] : g[i] >= f[i]);
			}
		}
	}

	@Test
	public void testBinary() {
		// Components of 1, 3 and 10 pixels (8-connected), the largest one
		// enclosing a hole of 2 pixels (4-connected).
		final var img = ArrayImgs.bits(7, 5);
		final var bits = new int[] { //
			1, 0, 0, 1, 1, 1, 0, //
			0, 0, 0, 1, 0, 1, 0, //
			0, 0, 0, 1, 0, 1, 0, //
			1, 0, 0, 1, 1, 1, 0, //
			1, 1, 0, 0, 0, 0, 0 };
		var i = 0;
		for (final var t : img)
			t.set(bits[i++] != 0);
		final var opened = ArrayImgs.bits(7, 5);
		ops.op("morphology.areaOpening").input(img, 4L).output(opened).compute();
		assertArrayEquals(new double[] { //
			0, 0, 0, 1, 1, 1, 0, //
			0, 0, 0, 1, 0, 1, 0, //
			0, 0, 0, 1, 0, 1, 0, //
			0, 0, 0, 1, 1, 1, 0, //
			0, 0, 0, 0, 0, 0, 0 }, values(opened), 0);
		final var closed = ArrayImgs.bits(7, 5);
		ops.op("morphology.areaClosing").input(img, 3L, new DiamondShape(1))
			.output(closed).compute();
		assertArrayEquals(new double[] { //
			1, 0, 0, 1, 1, 1, 0, //
			0, 0, 0, 1, 1, 1, 0, //
			0, 0, 0, 1, 1, 1, 0, //
			1, 0, 0, 1, 1, 1, 0, //
			1, 1, 0, 0, 0, 0, 0 }, values(closed), 0);
	}

	@Test
	public void testGenericAttributeOps() {
		final var img = randomImage(new Random(6), new long[] { 8, 7 }, 5);
		final var shape = new DiamondShape(1);
		assertArrayEquals(values(filter("morphology.areaOpening", img, 5L,
			shape)), values(attribute("morphology.attributeOpening", img, "area", 5,
				shape)), 0);
		assertArrayEquals(values(filter("morphology.boxDiagonalClosing", img, 2.5,
			shape)), values(attribute("morphology.attributeClosing", img,
				"BOXDIAGONAL", 2.5, shape)), 0);
		assertArrayEquals(values(filter("morphology.grayVolumeOpening", img, 6.0,
			shape)), values(attribute("morphology.attributeOpening", img,
				"grayVolume", 6, shape)), 0);
		assertThrows(IllegalArgumentException.class, () -> attribute(
			"morphology.attributeOpening", img, "perimeter", 1, shape));

		// Note: "volume" means the voxel count in MorphoLibJ, so it is rejected.
		final var e = assertThrows(IllegalArgumentException.class,
			() -> attribute("morphology.attributeClosing", img, "Volume", 1, shape));
		assertTrue(e.getMessage().contains("\"area\""));
		assertTrue(e.getMessage().contains("\"grayVolume\""));
	}

	@Test
	public void testDisconnectingShape() {
		// Note: with horizontal neighbors only, each row is filtered on its own.
		final var img = image(new long[] { 5, 2 }, //
			1, 4, 4, 2, 6, //
			0, 0, 3, 0, 0);
		final var horizontal = new HorizontalLineShape(1, 0, true);
		check(img, "area", new double[] { 2, 3 }, horizontal);
		check(img, "grayVolume", new double[] { 1, 5 }, horizontal);
		assertArrayEquals(new double[] { //
			1, 4, 4, 2, 2, //
			0, 0, 0, 0, 0 }, values(filter("morphology.areaOpening", img, 2L,
				horizontal)), 0);
	}

	// -- Helper methods --

	private static Shape[] shapes() {
		return new Shape[] { new DiamondShape(1), new RectangleShape(1, false) };
	}

	private void check(final RandomAccessibleInterval<DoubleType> img,
		final String attribute, final double[] thresholds, final Shape shape)
	{
		final var name = attribute.substring(0, 1).toUpperCase(Locale.ROOT) +
			attribute.substring(1);
		for (final var closing : new boolean[] { false, true }) {
			final var op = "morphology." + attribute + (closing ? "Closing"
				: "Opening");
			for (final var threshold : thresholds) {
				final var expected = reference(img, attribute, threshold, shape,
					closing);
				final var actual = values(attribute.equals("area") ? filter(op, img,
					(long) Math.ceil(threshold), shape) : filter(op, img, threshold,
						shape));
				assertArrayEquals(expected, actual, 1e-9, op + " " + threshold +
					" with " + shape);
				assertArrayEquals(expected, values(attribute("morphology.attribute" +
					(closing ? "Closing" : "Opening"), img, name, threshold, shape)),
					1e-9);
			}
		}
	}

	private Img<DoubleType> filter(final String op,
		final RandomAccessibleInterval<DoubleType> img, final Object threshold,
		final Shape shape)
	{
		final var out = ArrayImgs.doubles(img.dimensionsAsLongArray());
		if (shape == null) ops.op(op).input(img, threshold).output(out).compute();
		else ops.op(op).input(img, threshold, shape).output(out).compute();
		return out;
	}

	private Img<DoubleType> attribute(final String op,
		final RandomAccessibleInterval<DoubleType> img, final String attribute,
		final double threshold, final Shape shape)
	{
		final var out = ArrayImgs.doubles(img.dimensionsAsLongArray());
		ops.op(op).input(img, attribute, threshold, shape).output(out).compute();
		return out;
	}

	private static Img<DoubleType> randomImage(final Random random,
		final long[] dims, final int levels)
	{
		final var img = ArrayImgs.doubles(dims);
		for (final var t : img)
			t.set(random.nextInt(levels));
		return img;
	}

	private static Img<DoubleType> image(final long[] dims,
		final double... values)
	{
		return ArrayImgs.doubles(values, dims);
	}

	private static double[] values(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		final var values = new double[(int) Intervals.numElements(img)];
		var i = 0;
		for (final var t : Views.flatIterable(img))
			values[i++] = t.getRealDouble();
		return values;
	}

	/**
	 * Computes an attribute opening (or closing) by threshold decomposition,
	 * flooding the level set component of each pixel at each level.
	 */
	private static double[] reference(
		final RandomAccessibleInterval<DoubleType> img, final String attribute,
		final double threshold, final Shape shape, final boolean closing)
	{
		final var dims = img.dimensionsAsLongArray();
		final var n = dims.length;
		final var f = values(img);
		if (closing) for (var i = 0; i < f.length; i++)
			f[i] = -f[i];
		final var offsets = offsets(shape, n);
		final var levels = Arrays.stream(f).distinct().sorted().toArray();
		final var out = new double[f.length];
		for (var p = 0; p < f.length; p++) {
			for (var l = levels.length - 1; l >= 0; l--) {
				final var t = levels[l];
				if (t > f[p]) continue;
				if (accept(f, dims, offsets, p, t, attribute, threshold)) {
					out[p] = t;
					break;
				}
			}
		}
		if (closing) for (var i = 0; i < out.length; i++)
			out[i] = -out[i];
		return out;
	}

	private static boolean accept(final double[] f, final long[] dims,
		final int[][] offsets, final int seed, final double t,
		final String attribute, final double threshold)
	{
		final var n = dims.length;
		final var inside = new boolean[f.length];
		final var queue = new ArrayDeque<Integer>();
		inside[seed] = true;
		queue.add(seed);
		var area = 0;
		var sum = 0.0;
		var outside = Double.NEGATIVE_INFINITY;
		final var min = new long[n];
		final var max = new long[n];
		Arrays.fill(min, Long.MAX_VALUE);
		Arrays.fill(max, Long.MIN_VALUE);
		final var pos = new long[n];
		while (!queue.isEmpty()) {
			final int p = queue.poll();
			area++;
			sum += f[p];
			position(p, dims, pos);
			for (var d = 0; d < n; d++) {
				min[d] = Math.min(min[d], pos[d]);
				max[d] = Math.max(max[d], pos[d]);
			}
			for (final var o : offsets) {
				var q = 0L;
				var stride = 1L;
				var valid = true;
				for (var d = 0; d < n; d++) {
					final var c = pos[d] + o[d];
					if (c < 0 || c >= dims[d]) valid = false;
					q += c * stride;
					stride *= dims[d];
				}
				if (!valid || inside[(int) q]) continue;
				if (f[(int) q] >= t) {
					inside[(int) q] = true;
					queue.add((int) q);
				}
				else outside = Math.max(outside, f[(int) q]);
			}
		}
		if (outside == Double.NEGATIVE_INFINITY) return true;
		switch (attribute) {
			case "area":
				return area >= threshold;
			case "boxDiagonal":
				var d2 = 0.0;
				for (var d = 0; d < n; d++)
					d2 += (double) (max[d] - min[d]) * (max[d] - min[d]);
				return Math.sqrt(d2) >= threshold;
			default:
				return sum - area * outside >= threshold;
		}
	}

	private static void position(long index, final long[] dims,
		final long[] pos)
	{
		for (var d = 0; d < dims.length; d++) {
			pos[d] = index % dims[d];
			index /= dims[d];
		}
	}

	private static int[][] offsets(final Shape shape, final int n) {
		if (shape instanceof HorizontalLineShape) {
			final var o = new int[2][n];
			o[0][0] = -1;
			o[1][0] = 1;
			return o;
		}
		final var diamond = shape instanceof DiamondShape;
		final var list = new ArrayList<int[]>();
		final var o = new int[n];
		Arrays.fill(o, -1);
		while (true) {
			var l1 = 0;
			for (final var v : o)
				l1 += Math.abs(v);
			if (l1 > 0 && (!diamond || l1 == 1)) list.add(o.clone());
			var d = 0;
			while (d < n && ++o[d] > 1) o[d++] = -1;
			if (d == n) break;
		}
		return list.toArray(new int[0][]);
	}
}
