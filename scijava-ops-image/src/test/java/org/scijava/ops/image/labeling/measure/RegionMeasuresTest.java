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

package org.scijava.ops.image.labeling.measure;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imglib2.RealLocalizable;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.geom.real.Sphere;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Pair;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link InscribedBalls}, {@link EquivalentEllipsoids} and
 * {@link FeretDiameters}.
 * <p>
 * Unless noted otherwise, expected values were produced by running
 * MorphoLibJ 1.6.4 ({@code LargestInscribedCircle}, {@code EquivalentEllipse},
 * {@code EquivalentEllipsoid}, {@code MaxFeretDiameter3D}) on the same images.
 * MorphoLibJ places pixel centers at half-integer positions for ellipses,
 * whereas these ops place them at integer positions.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class RegionMeasuresTest extends AbstractOpTest {

	private static final Nil<Map<Integer, Sphere>> BALLS = new Nil<>() {};
	private static final Nil<Map<Integer, EquivalentEllipsoid>> ELLIPSOIDS =
		new Nil<>()
		{};
	private static final Nil<Map<Integer, Double>> DIAMETERS = new Nil<>() {};
	private static final Nil<Map<Integer, Pair<RealLocalizable, RealLocalizable>>> FERETS =
		new Nil<>()
		{};

	/**
	 * A disk of radius 5 at (8, 8) (1), a 6 x 3 rectangle (2), an ellipse with
	 * semi-axes 7 and 3 rotated by 0.5 radians around (22, 15) (3) and a single
	 * pixel (4).
	 */
	private static final String[] SHAPES = { //
		"00000000000000000000000000000000", //
		"00000000000000000000000000000000", //
		"00000000000000000022222200000000", //
		"00000000100000000022222200000000", //
		"00000111111100000022222200000000", //
		"00001111111110000000000000000000", //
		"00001111111110000000000000000000", //
		"00001111111110000000000000000000", //
		"00011111111111000000000000000000", //
		"00001111111110000000000000000000", //
		"00001111111110000000000000000000", //
		"00001111111110000333300000000000", //
		"00000111111100003333333000000000", //
		"00000000100000003333333330000000", //
		"00000000000000000333333333000000", //
		"00000000000000000033333333300000", //
		"00000000000000000003333333330000", //
		"00000000000000000000333333333000", //
		"00000000000000000000003333333000", //
		"00000000000000000000000033330000", //
		"00400000000000000000000000000000", //
		"00000000000000000000000000000000", //
		"00000000000000000000000000000000", //
		"00000000000000000000000000000000" };

	private static ImgLabeling<Integer, IntType> labeling(final Img<IntType> index,
		final int max)
	{
		return ImgLabeling.fromImageAndLabels(index, IntStream.rangeClosed(1, max)
			.boxed().collect(Collectors.toList()));
	}

	private static ImgLabeling<Integer, IntType> shapes2D() {
		final var index = ArrayImgs.ints(SHAPES[0].length(), SHAPES.length);
		final var ra = index.randomAccess();
		for (var y = 0; y < SHAPES.length; y++)
			for (var x = 0; x < SHAPES[y].length(); x++)
				ra.setPositionAndGet(x, y).set(SHAPES[y].charAt(x) - '0');
		return labeling(index, 4);
	}

	/** A 7 x 4 x 3 box (1), a ball of radius 4 (2) and a single voxel (3). */
	private static ImgLabeling<Integer, IntType> shapes3D() {
		final var index = ArrayImgs.ints(20, 13, 12);
		final var ra = index.randomAccess();
		for (var z = 0; z < 12; z++) {
			for (var y = 0; y < 13; y++) {
				for (var x = 0; x < 20; x++) {
					if (x >= 1 && x <= 7 && y >= 1 && y <= 4 && z >= 1 && z <= 3) {
						ra.setPositionAndGet(x, y, z).set(1);
					}
					final var r2 = (x - 14) * (x - 14) + (y - 6) * (y - 6) + (z - 6) *
						(z - 6);
					if (r2 <= 16) ra.setPositionAndGet(x, y, z).set(2);
				}
			}
		}
		ra.setPositionAndGet(2, 10, 9).set(3);
		return labeling(index, 3);
	}

	private static void assertBall(final Sphere ball, final double radius,
		final double... center)
	{
		assertEquals(radius, ball.radius(), 1e-9);
		final var c = new double[center.length];
		ball.center().localize(c);
		assertArrayEquals(center, c, 1e-9);
	}

	@Test
	public void testInscribedBall() {
		final Map<Integer, Sphere> balls = ops.op("labeling.inscribedBall").input(
			shapes2D()).outType(BALLS).apply();
		// Note: MorphoLibJ uses chamfer distances (giving radii 5 and 3 for the
		// disk and the ellipse); these exact values come from a brute-force
		// search for the nearest outside pixel center.
		assertBall(balls.get(1), Math.sqrt(26), 8, 8);
		assertBall(balls.get(2), 2, 19, 3);
		assertBall(balls.get(3), Math.sqrt(10), 22, 15);
		assertBall(balls.get(4), 1, 2, 20);

		// Analytic values.
		final Map<Integer, Sphere> balls3 = ops.op("labeling.inscribedBall").input(
			shapes3D(), new double[] { 1, 1, 0.5 }).outType(BALLS).apply();
		assertBall(balls3.get(1), 1, 1, 1, 1);
		final Map<Integer, Sphere> balls3u = ops.op("labeling.inscribedBall")
			.input(shapes3D()).outType(BALLS).apply();
		assertBall(balls3u.get(1), 2, 2, 2, 2);
		assertBall(balls3u.get(2), Math.sqrt(17), 14, 6, 6);
		assertBall(balls3u.get(3), 1, 2, 10, 9);
	}

	@Test
	public void testEquivalentEllipse() {
		final Map<Integer, EquivalentEllipsoid> e = ops.op(
			"labeling.equivalentEllipsoid").input(shapes2D()).outType(ELLIPSOIDS)
			.apply();
		assertArrayEquals(new double[] { 8, 8 }, e.get(1).center(), 1e-12);
		assertArrayEquals(new double[] { 5.129195061148351, 5.129195061148351 }, e
			.get(1).radii(), 1e-9);
		assertArrayEquals(new double[] { 20.5, 3 }, e.get(2).center(), 1e-12);
		assertArrayEquals(new double[] { 3.4641016151377544, 1.7320508075688772 },
			e.get(2).radii(), 1e-9);
		assertArrayEquals(new double[] { 1, 0 }, e.get(2).axis(0), 1e-12);
		assertArrayEquals(new double[] { 22, 15 }, e.get(3).center(), 1e-12);
		assertArrayEquals(new double[] { 7.273360524644183, 2.9831563323807426 }, e
			.get(3).radii(), 1e-9);
		final var major = e.get(3).axis(0);
		assertEquals(31.439348297920663, Math.toDegrees(Math.atan2(major[1],
			major[0])), 1e-9);
		assertArrayEquals(new double[] { 0.5773502691896258, 0.5773502691896258 },
			e.get(4).radii(), 1e-12);
	}

	@Test
	public void testEquivalentEllipsoid() {
		final Map<Integer, EquivalentEllipsoid> e = ops.op(
			"labeling.equivalentEllipsoid").input(shapes3D()).outType(ELLIPSOIDS)
			.apply();
		assertArrayEquals(new double[] { 4.51848057057532, 2.581988897471611,
			1.9364916731037085 }, e.get(1).radii(), 1e-9);
		assertArrayEquals(new double[] { 4, 2.5, 2 }, e.get(1).center(), 1e-12);
		assertArrayEquals(new double[] { 0, 0, 1 }, e.get(1).axis(2), 1e-12);
		assertArrayEquals(new double[] { 3.987862584094877, 3.987862584094877,
			3.987862584094877 }, e.get(2).radii(), 1e-9);
		assertArrayEquals(new double[] { 0.6454972243679028, 0.6454972243679028,
			0.6454972243679028 }, e.get(3).radii(), 1e-12);
		// Anisotropic spacing scales the radii (analytic).
		final Map<Integer, EquivalentEllipsoid> c = ops.op(
			"labeling.equivalentEllipsoid").input(shapes3D(), new double[] { 1, 1,
				4 }).outType(ELLIPSOIDS).apply();
		assertArrayEquals(new double[] { 4 * 1.9364916731037085,
			4.51848057057532, 2.581988897471611 }, c.get(1).radii(), 1e-9);
	}

	@Test
	public void testMaximumFeretDiameter() {
		final Map<Integer, Double> d = ops.op("labeling.maximumFeretDiameter")
			.input(shapes3D()).outType(DIAMETERS).apply();
		assertEquals(8.602325267042627, d.get(1), 1e-9);
		assertEquals(9.1104335791443, d.get(2), 1e-9);
		assertEquals(1.7320508075688774, d.get(3), 1e-9);

		final Map<Integer, Pair<RealLocalizable, RealLocalizable>> f = ops.op(
			"labeling.maximumFeret").input(shapes3D(), new double[] { 2, 1, 1 })
			.outType(FERETS).apply();
		final var a = f.get(1).getA().positionAsDoubleArray();
		final var b = f.get(1).getB().positionAsDoubleArray();
		assertArrayEquals(new double[] { 1, 0.5, 0.5 }, a, 1e-12);
		assertArrayEquals(new double[] { 15, 4.5, 3.5 }, b, 1e-12);

		// 2D, analytic: the diagonal of the rectangle and of a pixel.
		final Map<Integer, Double> d2 = ops.op("labeling.maximumFeretDiameter")
			.input(shapes2D()).outType(DIAMETERS).apply();
		assertEquals(Math.hypot(6, 3), d2.get(2), 1e-12);
		assertEquals(Math.sqrt(2), d2.get(4), 1e-12);
	}
}
