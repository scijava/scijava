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

package org.scijava.ops.image.morphology.reconstruction;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests the ops of this package on synthetic images with known answers, and
 * checks the defining properties of the transforms.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class ReconstructionTest extends AbstractOpTest {

	// -- Reconstruction --

	@Test
	public void testReconstructByDilation() {
		// Two bumps; only the left one is marked.
		final var mask = doubles(new double[] { 0, 3, 5, 3, 1, 4, 7, 4, 0 }, 9);
		final var marker = doubles(new double[] { 0, 0, 9, 0, 0, 0, 0, 0, 0 }, 9);
		final var out = ArrayImgs.doubles(9);
		ops.op("morphology.reconstructByDilation").input(marker, mask, null)
			.output(out).compute();
		// The marker is clipped to the mask; the right bump is flattened to the
		// level of the saddle between the bumps.
		assertArrayEquals(new double[] { 0, 3, 5, 3, 1, 1, 1, 1, 0 }, values(out));
	}

	@Test
	public void testReconstructByErosion() {
		final var mask = doubles(new double[] { 9, 6, 4, 6, 8, 5, 2, 5, 9 }, 9);
		final var marker = doubles(new double[] { 9, 9, 9, 9, 9, 9, 2, 9, 9 }, 9);
		final var out = ArrayImgs.doubles(9);
		ops.op("morphology.reconstructByErosion").input(marker, mask, null)
			.output(out).compute();
		assertArrayEquals(new double[] { 9, 8, 8, 8, 8, 5, 2, 5, 9 }, values(out));
	}

	@Test
	public void testReconstructionProperties() {
		final var random = new Random(42);
		final var mask = ArrayImgs.doubles(7, 6, 5);
		final var marker = ArrayImgs.doubles(7, 6, 5);
		for (final var t : mask)
			t.set(random.nextInt(20));
		for (final var t : marker)
			t.set(random.nextDouble() < 0.05 ? random.nextInt(25) : 0);
		for (final var shape : new Object[] { new DiamondShape(1),
			new RectangleShape(1, false) })
		{
			final var rec = ArrayImgs.doubles(7, 6, 5);
			ops.op("morphology.reconstructByDilation").input(marker, mask, shape)
				.output(rec).compute();
			final var rec2 = ArrayImgs.doubles(7, 6, 5);
			ops.op("morphology.reconstructByDilation").input(rec, mask, shape)
				.output(rec2).compute();
			assertArrayEquals(values(rec), values(rec2), "idempotence");
			final var r = values(rec);
			final var f = values(mask);
			final var m = values(marker);
			for (var i = 0; i < r.length; i++) {
				assertTrue(r[i] <= f[i]);
				assertTrue(r[i] >= Math.min(m[i], f[i]));
			}
			// Same result as iterating geodesic dilations until stability.
			assertArrayEquals(naiveReconstruction(m, f, 7, 6, 5, shape), r);
		}
	}

	@Test
	public void testReconstructionDefaultsToFullConnectivity() {
		// A diagonal path: only reachable with full connectivity.
		final var mask = doubles(new double[] { 5, 0, 0, 0, 5, 0, 0, 0, 5 }, 3, 3);
		final var marker = doubles(new double[] { 5, 0, 0, 0, 0, 0, 0, 0, 0 }, 3,
			3);
		final var out = ArrayImgs.doubles(3, 3);
		ops.op("morphology.reconstructByDilation").input(marker, mask).output(out)
			.compute();
		assertArrayEquals(values(mask), values(out));
		ops.op("morphology.reconstructByDilation").input(marker, mask,
			new DiamondShape(1)).output(out).compute();
		assertArrayEquals(values(marker), values(out));
	}

	@Test
	public void testReconstructionFunction() {
		final var mask = doubles(new double[] { 0, 3, 5, 3, 1, 4, 7, 4, 0 }, 9);
		final var marker = doubles(new double[] { 0, 0, 9, 0, 0, 0, 0, 0, 0 }, 9);
		final RandomAccessibleInterval<DoubleType> out = ops.op(
			"morphology.reconstructByDilation").input(marker, mask).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();
		assertArrayEquals(new double[] { 0, 3, 5, 3, 1, 1, 1, 1, 0 }, values(out));
	}

	@Test
	public void testTranslatedInterval() {
		final var mask = Views.translate(doubles(new double[] { 0, 3, 5, 3, 1, 4,
			7, 4, 0 }, 9), -4);
		final var marker = Views.translate(doubles(new double[] { 0, 0, 9, 0, 0, 0,
			0, 0, 0 }, 9), 10);
		final var out = Views.translate(ArrayImgs.doubles(9), 3);
		ops.op("morphology.reconstructByDilation").input(marker, mask, null)
			.output(out).compute();
		assertArrayEquals(new double[] { 0, 3, 5, 3, 1, 1, 1, 1, 0 }, values(out));
	}

	@Test
	public void testDimensionMismatch() {
		final var a = ArrayImgs.doubles(4, 4);
		final var b = ArrayImgs.doubles(4, 5);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"morphology.reconstructByDilation").input(a, b, null).output(a)
			.compute());
	}

	// -- Regional extrema --

	@Test
	public void testRegionalMaximaPlateaus() {
		// A plateau on the border, a plateau next to a higher pixel, a single
		// pixel maximum and a plateau maximum.
		final var f = doubles(new double[] { 7, 7, 3, 4, 4, 5, 2, 6, 6, 1 }, 10);
		final var out = ArrayImgs.bits(10);
		ops.op("morphology.regionalMaxima").input(f, null).output(out).compute();
		assertArrayEquals(new boolean[] { true, true, false, false, false, true,
			false, true, true, false }, flags(out));
		ops.op("morphology.regionalMinima").input(f, null).output(out).compute();
		assertArrayEquals(new boolean[] { false, false, true, false, false, false,
			true, false, false, true }, flags(out));
	}

	@Test
	public void testRegionalMaximaConnectivity() {
		final var f = doubles(new double[] { //
			0, 0, 0, 0, 0, //
			0, 5, 0, 0, 0, //
			0, 0, 6, 0, 0, //
			0, 0, 0, 0, 0 }, 5, 4);
		final var out = ArrayImgs.bits(5, 4);
		ops.op("morphology.regionalMaxima").input(f, new DiamondShape(1)).output(
			out).compute();
		assertEquals(2, count(out));
		ops.op("morphology.regionalMaxima").input(f, new RectangleShape(1, false))
			.output(out).compute();
		assertEquals(1, count(out));
		ops.op("morphology.regionalMaxima").input(f).output(out).compute();
		assertEquals(1, count(out));
		// The background plateau is the only minimum.
		ops.op("morphology.regionalMinima").input(f).output(out).compute();
		assertEquals(18, count(out));
	}

	@Test
	public void testRegionalMaximaFunction() {
		final var f = doubles(new double[] { 7, 7, 3, 4, 4, 5, 2, 6, 6, 1 }, 10);
		final RandomAccessibleInterval<BitType> out = ops.op(
			"morphology.regionalMaxima").input(f).outType(
				new Nil<RandomAccessibleInterval<BitType>>()
				{}).apply();
		assertArrayEquals(new boolean[] { true, true, false, false, false, true,
			false, true, true, false }, flags(out));
	}

	@Test
	public void testRegionalMaximaConstantImage() {
		final var f = ArrayImgs.unsignedBytes(4, 3, 2);
		for (final var t : f)
			t.set(17);
		final var out = ArrayImgs.bits(4, 3, 2);
		ops.op("morphology.regionalMaxima").input(f).output(out).compute();
		assertEquals(24, count(out));
		ops.op("morphology.regionalMinima").input(f).output(out).compute();
		assertEquals(24, count(out));
	}

	@Test
	public void testRegionalMaximaOfTwoGaussians() {
		final var f = ArrayImgs.doubles(40, 30);
		final var c = f.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final double x = c.getIntPosition(0), y = c.getIntPosition(1);
			c.get().set(100 * Math.exp(-((x - 10) * (x - 10) + (y - 12) * (y -
				12)) / 30) + 60 * Math.exp(-((x - 28) * (x - 28) + (y - 18) * (y -
					18)) / 20));
		}
		final var out = ArrayImgs.bits(40, 30);
		ops.op("morphology.regionalMaxima").input(f).output(out).compute();
		assertEquals(2, count(out));
		assertTrue(out.getAt(10, 12).get());
		assertTrue(out.getAt(28, 18).get());
	}

	// -- h-transforms and extended extrema --

	@Test
	public void testHMaxima() {
		final var f = doubles(new double[] { 0, 5, 2, 8, 0 }, 5);
		final var out = ArrayImgs.doubles(5);
		ops.op("morphology.hMaxima").input(f, 3.0, null).output(out).compute();
		assertArrayEquals(new double[] { 0, 2, 2, 5, 0 }, values(out));

		// Saturation at zero for unsigned types gives the same result here.
		final var u = ArrayImgs.unsignedBytes(new byte[] { 0, 5, 2, 8, 0 }, 5);
		final var uOut = ArrayImgs.unsignedBytes(5);
		ops.op("morphology.hMaxima").input(u, 3.0, null).output(uOut).compute();
		assertArrayEquals(new double[] { 0, 2, 2, 5, 0 }, values(uOut));

		final var bits = ArrayImgs.bits(5);
		ops.op("morphology.extendedMaxima").input(f, 3.0, null).output(bits)
			.compute();
		assertArrayEquals(new boolean[] { false, false, false, true, false },
			flags(bits));
		// The left peak has dynamic 3, so it survives any smaller h.
		ops.op("morphology.extendedMaxima").input(f, 2.5, null).output(bits)
			.compute();
		assertArrayEquals(new boolean[] { false, true, false, true, false }, flags(
			bits));
		ops.op("morphology.extendedMaxima").input(f, 0.0, null).output(bits)
			.compute();
		final var rmax = ArrayImgs.bits(5);
		ops.op("morphology.regionalMaxima").input(f, null).output(rmax).compute();
		assertArrayEquals(flags(rmax), flags(bits));
	}

	@Test
	public void testHMinima() {
		final var f = doubles(new double[] { 9, 4, 7, 1, 9 }, 5);
		final var out = ArrayImgs.doubles(5);
		ops.op("morphology.hMinima").input(f, 3.0, null).output(out).compute();
		assertArrayEquals(new double[] { 9, 7, 7, 4, 9 }, values(out));
		final var bits = ArrayImgs.bits(5);
		ops.op("morphology.extendedMinima").input(f, 2.5, null).output(bits)
			.compute();
		assertArrayEquals(new boolean[] { false, true, false, true, false }, flags(
			bits));
	}

	@Test
	public void testHTransformProperties() {
		final var random = new Random(7);
		final var f = ArrayImgs.unsignedBytes(9, 8, 3);
		final var inv = ArrayImgs.unsignedBytes(9, 8, 3);
		final var c = f.cursor();
		final var ci = inv.cursor();
		while (c.hasNext()) {
			final var v = random.nextInt(256);
			c.next().set(v);
			ci.next().set(255 - v);
		}
		final var h = 40.0;
		final var hmax = ArrayImgs.unsignedBytes(9, 8, 3);
		ops.op("morphology.hMaxima").input(f, h, null).output(hmax).compute();
		final var hmin = ArrayImgs.unsignedBytes(9, 8, 3);
		ops.op("morphology.hMinima").input(inv, h, null).output(hmin).compute();
		final var fv = values(f);
		final var hv = values(hmax);
		final var hm = values(hmin);
		for (var i = 0; i < fv.length; i++) {
			assertTrue(hv[i] <= fv[i]);
			assertTrue(hv[i] >= Math.max(fv[i] - h, 0));
			assertEquals(255 - hv[i], hm[i], "duality");
		}
		// Extended maxima are the regional maxima of the h-maxima.
		final var emax = ArrayImgs.bits(9, 8, 3);
		ops.op("morphology.extendedMaxima").input(f, h, null).output(emax)
			.compute();
		final var rmax = ArrayImgs.bits(9, 8, 3);
		ops.op("morphology.regionalMaxima").input(hmax, null).output(rmax)
			.compute();
		assertArrayEquals(flags(rmax), flags(emax));
	}

	@Test
	public void testNegativeHeight() {
		final var f = ArrayImgs.doubles(3, 3);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"morphology.hMaxima").input(f, -1.0, null).output(f).compute());
	}

	// -- Minima imposition --

	@Test
	public void testImposeMinimaFloat() {
		final var f = doubles(new double[] { 3, 1, 4, 1, 5, 9, 2, 6 }, 8);
		final var markers = ArrayImgs.bits(8);
		markers.getAt(4).set(true);
		final var out = ArrayImgs.doubles(8);
		ops.op("morphology.imposeMinima").input(f, markers, null).output(out)
			.compute();
		final var inf = Double.NEGATIVE_INFINITY;
		assertArrayEquals(new double[] { 4, 4, 4, 1, inf, 9, 9, 9 }, values(out));
		ops.op("morphology.imposeMaxima").input(f, markers, null).output(out)
			.compute();
		assertArrayEquals(new double[] { 1, 1, 1, 1, Double.POSITIVE_INFINITY, 9,
			2, 2 }, values(out));
	}

	@Test
	public void testImposedMinimaAreMarkers() {
		final var random = new Random(3);
		final var f = ArrayImgs.unsignedBytes(10, 9);
		for (final var t : f)
			t.set(random.nextInt(200));
		final var markers = ArrayImgs.bits(10, 9);
		markers.getAt(2, 2).set(true);
		markers.getAt(3, 2).set(true);
		markers.getAt(7, 6).set(true);
		markers.getAt(0, 8).set(true);
		for (final var shape : new Object[] { new DiamondShape(1),
			new RectangleShape(1, false) })
		{
			final var out = ArrayImgs.unsignedBytes(10, 9);
			final var rmin = ArrayImgs.bits(10, 9);
			ops.op("morphology.imposeMinima").input(f, markers, shape).output(out)
				.compute();
			ops.op("morphology.regionalMinima").input(out, shape).output(rmin)
				.compute();
			assertArrayEquals(flags(markers), flags(rmin));
			ops.op("morphology.imposeMaxima").input(f, markers, shape).output(out)
				.compute();
			ops.op("morphology.regionalMaxima").input(out, shape).output(rmin)
				.compute();
			assertArrayEquals(flags(markers), flags(rmin));
		}
	}

	// -- Kill borders and fill holes --

	@Test
	public void testKillBordersBinary() {
		final var img = bits(new int[] { //
			1, 1, 0, 0, 0, 0, //
			0, 0, 1, 1, 1, 0, //
			0, 0, 1, 0, 0, 0, //
			0, 0, 0, 0, 1, 1, //
			0, 1, 0, 0, 0, 0 }, 6, 5);
		final var out = ArrayImgs.bits(6, 5);
		ops.op("morphology.killBorders").input(img, new DiamondShape(1)).output(
			out).compute();
		assertArrayEquals(bitFlags(new int[] { //
			0, 0, 0, 0, 0, 0, //
			0, 0, 1, 1, 1, 0, //
			0, 0, 1, 0, 0, 0, //
			0, 0, 0, 0, 0, 0, //
			0, 0, 0, 0, 0, 0 }), flags(out));
		// With full connectivity, the central component touches the top left one
		// by a corner, so it is connected to the border too.
		ops.op("morphology.killBorders").input(img).output(out).compute();
		assertEquals(0, count(out));
	}

	@Test
	public void testKillBordersGray() {
		final var f = doubles(new double[] { //
			2, 2, 2, 2, 2, //
			2, 5, 1, 9, 2, //
			2, 2, 2, 2, 2 }, 5, 3);
		final var out = ArrayImgs.doubles(5, 3);
		ops.op("morphology.killBorders").input(f).output(out).compute();
		assertArrayEquals(new double[] { //
			0, 0, 0, 0, 0, //
			0, 3, 0, 7, 0, //
			0, 0, 0, 0, 0 }, values(out));
	}

	@Test
	public void testFillHolesGray() {
		final var f = doubles(new double[] { //
			1, 1, 1, 1, 1, 1, //
			1, 6, 6, 6, 6, 1, //
			1, 6, 2, 3, 6, 1, //
			1, 6, 6, 4, 6, 1, //
			1, 1, 1, 1, 1, 1 }, 6, 5);
		final var out = ArrayImgs.doubles(6, 5);
		ops.op("morphology.fillHoles").input(f).output(out).compute();
		assertArrayEquals(new double[] { //
			1, 1, 1, 1, 1, 1, //
			1, 6, 6, 6, 6, 1, //
			1, 6, 4, 4, 6, 1, //
			1, 6, 6, 4, 6, 1, //
			1, 1, 1, 1, 1, 1 }, values(out));
	}

	@Test
	public void testFillHolesGrayMatchesBinary() {
		final var random = new Random(11);
		final var bin = ArrayImgs.bits(12, 10);
		final var gray = ArrayImgs.unsignedBytes(12, 10);
		final var cb = bin.cursor();
		final var cg = gray.cursor();
		while (cb.hasNext()) {
			final var b = random.nextDouble() < 0.5;
			cb.next().set(b);
			cg.next().set(b ? 1 : 0);
		}
		for (final var shape : new Object[] { new DiamondShape(1),
			new RectangleShape(1, false) })
		{
			final var binOut = ArrayImgs.bits(12, 10);
			ops.op("morphology.fillHoles").input(bin, shape).output(binOut)
				.compute();
			final var grayOut = ArrayImgs.unsignedBytes(12, 10);
			ops.op("morphology.fillHoles").input(gray, shape).output(grayOut)
				.compute();
			final var bv = flags(binOut);
			final var gv = values(grayOut);
			for (var i = 0; i < bv.length; i++)
				assertEquals(bv[i] ? 1.0 : 0.0, gv[i]);
		}
	}

	// -- Helper methods --

	/** Reconstruction by iterated elementary geodesic dilations. */
	private static double[] naiveReconstruction(final double[] marker,
		final double[] mask, final int w, final int h, final int d,
		final Object shape)
	{
		final var full = shape instanceof RectangleShape;
		var cur = new double[marker.length];
		for (var i = 0; i < cur.length; i++)
			cur[i] = Math.min(marker[i], mask[i]);
		var changed = true;
		while (changed) {
			changed = false;
			final var next = cur.clone();
			for (var z = 0; z < d; z++)
				for (var y = 0; y < h; y++)
					for (var x = 0; x < w; x++) {
						final var i = x + w * (y + h * z);
						var v = cur[i];
						for (var dz = -1; dz <= 1; dz++)
							for (var dy = -1; dy <= 1; dy++)
								for (var dx = -1; dx <= 1; dx++) {
									final var dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
									if (dist == 0 || !full && dist > 1) continue;
									final int xx = x + dx, yy = y + dy, zz = z + dz;
									if (xx < 0 || yy < 0 || zz < 0 || xx >= w || yy >= h ||
										zz >= d) continue;
									v = Math.max(v, cur[xx + w * (yy + h * zz)]);
								}
						next[i] = Math.min(v, mask[i]);
						if (next[i] != cur[i]) changed = true;
					}
			cur = next;
		}
		return cur;
	}

	private static Img<DoubleType> doubles(final double[] values,
		final long... dims)
	{
		return ArrayImgs.doubles(values.clone(), dims);
	}

	private static Img<BitType> bits(final int[] values, final long... dims) {
		final var img = ArrayImgs.bits(dims);
		var i = 0;
		for (final var b : img)
			b.set(values[i++] != 0);
		return img;
	}

	private static boolean[] bitFlags(final int[] values) {
		final var result = new boolean[values.length];
		for (var i = 0; i < values.length; i++)
			result[i] = values[i] != 0;
		return result;
	}

	private static double[] values(
		final RandomAccessibleInterval<? extends RealType<?>> image)
	{
		final var result = new double[(int) Views.iterable(image).size()];
		var i = 0;
		for (final var t : Views.flatIterable(image))
			result[i++] = t.getRealDouble();
		return result;
	}

	private static boolean[] flags(
		final RandomAccessibleInterval<? extends BooleanType<?>> image)
	{
		final var result = new boolean[(int) Views.iterable(image).size()];
		var i = 0;
		for (final var t : Views.flatIterable(image))
			result[i++] = t.get();
		return result;
	}

	private static int count(
		final RandomAccessibleInterval<? extends BooleanType<?>> image)
	{
		var n = 0;
		for (final var b : flags(image))
			if (b) n++;
		return n;
	}

	// -- NaN handling --

	/**
	 * A single NaN would otherwise spread through the raster scans and turn the
	 * whole output into NaN, or flag every NaN pixel as both a regional maximum
	 * and minimum; like the attribute filters, these ops reject NaN.
	 */
	@Test
	public void testNaNRejected() {
		final var f = ArrayImgs.floats(new float[] { Float.NaN, 1, 2, 1, 0, 1, 3, 1,
			0 }, 3, 3);
		final var g = ArrayImgs.floats(new float[] { 5, 5, 5, 5, 5, 5, 5, 5, 5 }, 3,
			3);
		final var out = ArrayImgs.floats(3, 3);
		final var bits = ArrayImgs.bits(3, 3);
		final var marks = ArrayImgs.bits(3, 3);
		marks.firstElement().set(true);
		final var shape = new RectangleShape(1, false);
		// NaN in the marker or in the mask.
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"morphology.reconstructByDilation").input(f, g, shape).output(out)
			.compute());
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"morphology.reconstructByDilation").input(ArrayImgs.floats(3, 3), f,
				shape).output(out).compute());
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"morphology.reconstructByErosion").input(g, f, shape).output(out)
			.compute());
		for (final var name : new String[] { "morphology.hMaxima",
			"morphology.hMinima" })
		{
			assertThrows(IllegalArgumentException.class, () -> ops.op(name).input(f,
				1.0, shape).output(out).compute(), name);
		}
		for (final var name : new String[] { "morphology.extendedMaxima",
			"morphology.extendedMinima" })
		{
			assertThrows(IllegalArgumentException.class, () -> ops.op(name).input(f,
				1.0, shape).output(bits).compute(), name);
		}
		for (final var name : new String[] { "morphology.imposeMaxima",
			"morphology.imposeMinima" })
		{
			assertThrows(IllegalArgumentException.class, () -> ops.op(name).input(f,
				marks, shape).output(out).compute(), name);
		}
		for (final var name : new String[] { "morphology.killBorders",
			"morphology.fillHoles" })
		{
			assertThrows(IllegalArgumentException.class, () -> ops.op(name).input(f,
				null).output(out).compute(), name);
		}
		for (final var name : new String[] { "morphology.regionalMaxima",
			"morphology.regionalMinima" })
		{
			assertThrows(IllegalArgumentException.class, () -> ops.op(name).input(f,
				shape).output(bits).compute(), name);
		}
	}
}
