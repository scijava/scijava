/*-
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

package org.scijava.ops.image.filter.tensor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.function.ToDoubleFunction;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link TensorFilters}, mostly against analytic results on synthetic
 * images.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class TensorFiltersTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<DoubleType>> OUT =
		new Nil<>()
		{};

	// -- filter.hessianEigenvalues --

	@Test
	public void testHessianEigenvalues2D() {
		// For f = x² + xy + y², H = [[2, 1], [1, 2]] has eigenvalues 1 and 3.
		final var img = image(p -> p[0] * p[0] + p[0] * p[1] + p[1] * p[1], 21,
			21);
		for (final double sigma : new double[] { 0, 1.5 }) {
			final var evs = ops.op("filter.hessianEigenvalues").input(img, sigma)
				.outType(OUT).apply();
			assertArrayEquals(new long[] { 21, 21, 2 }, evs.dimensionsAsLongArray());
			assertArrayEquals(new double[] { 1, 3 }, at(evs, 10, 10), 1e-6);
		}
	}

	@Test
	public void testHessianEigenvaluesSortedByMagnitude3D() {
		// For f = -x² - 2y² + 3z², the eigenvalues are -2, -4 and 6.
		final var img = image(p -> -p[0] * p[0] - 2 * p[1] * p[1] + 3 * p[2] *
			p[2], 15, 15, 15);
		final var evs = ops.op("filter.hessianEigenvalues").input(img, 1.0)
			.outType(OUT).apply();
		assertArrayEquals(new double[] { -2, -4, 6 }, at(evs, 7, 7, 7), 1e-6);
	}

	@Test
	public void testHessianEigenvaluesNormalizedAndCalibrated() {
		// For f = x² in pixel units, Hxx is 2.
		final var img = image(p -> p[0] * p[0], 21, 21);
		// Scale normalization multiplies by sigma².
		final var normalized = ops.op("filter.hessianEigenvalues").input(img, 2.0,
			true).outType(OUT).apply();
		assertArrayEquals(new double[] { 0, 8 }, at(normalized, 10, 10), 1e-6);
		// With pixel spacing 0.5 along x, f is 4 x² in physical units, so Hxx is 8.
		final var calibrated = ops.op("filter.hessianEigenvalues").input(img, 1.0,
			false, new double[] { 0.5, 1 }).outType(OUT).apply();
		assertArrayEquals(new double[] { 0, 8 }, at(calibrated, 10, 10), 1e-6);
	}

	@Test
	public void testHessianEigenvaluesTranslatedInput() {
		final var img = image(p -> 3 * p[0] * p[0] - p[1] * p[1], 21, 21);
		final var translated = Views.translate(img, 100, -50);
		final var evs = ops.op("filter.hessianEigenvalues").input(translated, 1.0)
			.outType(OUT).apply();
		assertArrayEquals(new long[] { 0, 0, 0 }, evs.minAsLongArray());
		assertArrayEquals(new double[] { -2, 6 }, at(evs, 10, 10), 1e-6);
	}

	// -- filter.structureTensor --

	@Test
	public void testStructureTensor() {
		// The linear ramp f = 2x + 3y has gradient (2, 3) everywhere.
		final var img = image(p -> 2 * p[0] + 3 * p[1], 31, 31);
		final var tensor = ops.op("filter.structureTensor").input(img, 1.0, 2.0)
			.outType(OUT).apply();
		assertArrayEquals(new long[] { 31, 31, 3 }, tensor.dimensionsAsLongArray());
		assertArrayEquals(new double[] { 4, 6, 9 }, at(tensor, 15, 15), 1e-9);

		// In 3D, for f = x - 2y + 3z, a z spacing of 2 halves the z derivative.
		final var img3 = image(p -> p[0] - 2 * p[1] + 3 * p[2], 21, 21, 21);
		final var tensor3 = ops.op("filter.structureTensor").input(img3, 1.0, 1.0,
			new double[] { 1, 1, 2 }).outType(OUT).apply();
		// The gradient is (1, -2, 1.5), and the order is [xx, xy, xz, yy, yz, zz].
		assertArrayEquals(new double[] { 1, -2, 1.5, 4, -3, 2.25 }, at(tensor3, 10,
			10, 10), 1e-9);
	}

	@Test
	public void testStructureTensorEigenvalues() {
		final var img = image(p -> 2 * p[0] + 3 * p[1], 31, 31);
		final var evs = ops.op("filter.structureTensorEigenvalues").input(img, 1.0,
			2.0).outType(OUT).apply();
		assertArrayEquals(new double[] { 13, 0 }, at(evs, 15, 15), 1e-9);

		// For a random 3D image, the eigenvalues are sorted descending and non-negative,
		// and their sum equals the trace of the structure tensor.
		final var rnd = randomImage(new Random(42), 12, 13, 14);
		final var evs3 = ops.op("filter.structureTensorEigenvalues").input(rnd,
			1.0, 1.5).outType(OUT).apply();
		final var tensor3 = ops.op("filter.structureTensor").input(rnd, 1.0, 1.5)
			.outType(OUT).apply();
		for (final var pos : new long[][] { { 0, 0, 0 }, { 5, 6, 7 }, { 11, 12,
			13 } })
		{
			final var ev = at(evs3, pos);
			final var t = at(tensor3, pos);
			assertTrue(ev[0] >= ev[1] && ev[1] >= ev[2] && ev[2] >= -1e-12);
			assertEquals(t[0] + t[3] + t[5], ev[0] + ev[1] + ev[2], 1e-9);
		}
	}

	// -- filter.shapeIndex --

	@Test
	public void testShapeIndex() {
		// A cap gives 1.
		assertShapeIndex(1, p -> -(p[0] * p[0] + p[1] * p[1]));
		// A ridge gives 0.5.
		assertShapeIndex(0.5, p -> -p[0] * p[0]);
		// A saddle gives 0.
		assertShapeIndex(0, p -> p[0] * p[0] - p[1] * p[1]);
		// A valley gives -0.5.
		assertShapeIndex(-0.5, p -> 2 * p[1] * p[1]);
		// A cup gives -1.
		assertShapeIndex(-1, p -> p[0] * p[0] + p[1] * p[1]);
		// A plane, where the shape index is undefined, gives 0.
		assertShapeIndex(0, p -> 7);
		// An elliptic cap with κ1 = -2 and κ2 = -6 gives -(2/π) atan(-8/4).
		assertShapeIndex(2 / Math.PI * Math.atan(2), p -> -(p[0] * p[0] + 3 *
			p[1] * p[1]));
	}

	@Test
	public void testShapeIndexRequires2D() {
		final var img = image(p -> 0, 5, 5, 5);
		assertThrows(RuntimeException.class, () -> ops.op("filter.shapeIndex")
			.input(img, 1.0).outType(OUT).apply());
	}

	// -- filter.sheetness --

	@Test
	public void testSheetness() {
		final double s = 2, sigma = 2;
		final var plate = image(p -> gauss(p[2], s), 21, 21, 21);
		final var line = image(p -> gauss(p[1], s) * gauss(p[2], s), 21, 21, 21);
		final var blob = image(p -> gauss(p[0], s) * gauss(p[1], s) * gauss(p[2],
			s), 21, 21, 21);

		// Analytically, smoothing the plate profile gives a Gaussian of variance
		// w² = s² + σ² and amplitude s / w, whose second derivative at the
		// center is -s / w³; scale-normalized, the response is σ² s / w³.
		final var w = Math.sqrt(s * s + sigma * sigma);
		final var expected = sigma * sigma * s / (w * w * w);
		final var plateResponse = sheetness(plate, sigma)[0];
		assertEquals(expected, plateResponse, 0.05 * expected);

		final var lineResponse = sheetness(line, sigma)[0];
		final var blobResponse = sheetness(blob, sigma)[0];
		assertEquals(0, lineResponse, 1e-3 * plateResponse);
		assertEquals(0, blobResponse, 1e-3 * plateResponse);

		// A dark plate, whose curvature across it is positive, gives 0.
		final var darkPlate = image(p -> -gauss(p[2], s), 21, 21, 21);
		assertEquals(0, sheetness(darkPlate, sigma)[0], 0);

		// Away from the plate center, on its flank, the response decreases.
		final var flank = sheetness(plate, sigma)[1];
		assertTrue(flank < plateResponse);
	}

	@Test
	public void testSheetnessRequires3D() {
		final var img = image(p -> 0, 5, 5);
		assertThrows(RuntimeException.class, () -> ops.op("filter.sheetness")
			.input(img, 1.0).outType(OUT).apply());
	}

	// -- filter.orientation, filter.coherency --

	@Test
	public void testOrientationOfStripes() {
		for (final double alpha : new double[] { 0, Math.PI / 6, Math.PI / 4,
			-Math.PI / 3, 1.2 })
		{
			// The stripes run at angle alpha counterclockwise as displayed (y up),
			// i.e. along (cos α, -sin α) in pixel coordinates.
			final var img = stripes(alpha);
			final var orientation = ops.op("filter.orientation").input(img, 1.0, 3.0)
				.outType(OUT).apply();
			// Note: the tolerance covers the finite-difference bias of the gradient
			// direction on sampled sinusoids.
			assertEquals(alpha, at(orientation, 32, 32)[0], 1e-2, "alpha=" + alpha);
			final var coherency = ops.op("filter.coherency").input(img, 1.0, 3.0)
				.outType(OUT).apply();
			assertEquals(1, at(coherency, 32, 32)[0], 1e-6, "alpha=" + alpha);
		}
		// Vertical stripes give ±π/2.
		final var vertical = ops.op("filter.orientation").input(stripes(Math.PI /
			2), 1.0, 3.0).outType(OUT).apply();
		assertEquals(Math.PI / 2, Math.abs(at(vertical, 32, 32)[0]), 1e-2);
	}

	@Test
	public void testCoherencyIsotropic() {
		// An egg-crate pattern has no dominant orientation.
		final var w = 2 * Math.PI / 8;
		final var img = image(p -> Math.sin(w * p[0]) * Math.sin(w * p[1]), 64,
			64);
		final var coherency = ops.op("filter.coherency").input(img, 1.0, 8.0)
			.outType(OUT).apply();
		assertEquals(0, at(coherency, 32, 32)[0], 0.02);
		// A constant image has zero coherency.
		final var flat = ops.op("filter.coherency").input(image(p -> 5, 16, 16),
			1.0, 2.0).outType(OUT).apply();
		assertEquals(0, at(flat, 8, 8)[0], 0);
	}

	// -- filter.harris --

	@Test
	public void testHarrisSquare() {
		// Draw a bright square spanning [20, 59] on a dark background.
		final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(80, 80);
		Views.interval(img, Intervals.createMinMax(20, 20, 59, 59)).forEach(t -> t
			.set(200));
		final var response = ops.op("filter.harris").input(img, 1.0, 2.0)
			.outType(OUT).apply();

		// The strongest responses are at the four corners.
		final var c = Views.flatIterable(response).cursor();
		var max = Double.NEGATIVE_INFINITY;
		final var argmax = new long[2];
		while (c.hasNext()) {
			final var v = c.next().get();
			if (v > max) {
				max = v;
				c.localize(argmax);
			}
		}
		assertTrue(max > 0);
		final var dx = Math.min(Math.abs(argmax[0] - 19.5), Math.abs(argmax[0] -
			59.5));
		final var dy = Math.min(Math.abs(argmax[1] - 19.5), Math.abs(argmax[1] -
			59.5));
		assertTrue(dx <= 2 && dy <= 2, "argmax at " + argmax[0] + "," +
			argmax[1]);

		// Edges give negative responses, and flat regions give about 0.
		assertTrue(at(response, 40, 20)[0] < 0);
		assertTrue(at(response, 20, 40)[0] < 0);
		assertEquals(0, at(response, 40, 40)[0], 1e-9 * max);
		assertEquals(0, at(response, 3, 3)[0], 1e-9 * max);

		// The response respects the symmetry of the configuration.
		assertEquals(at(response, 20, 20)[0], at(response, 59, 59)[0], 1e-9 * max);
	}

	@Test
	public void testHarrisFormula() {
		// Check R = det(J) - k·trace(J)ⁿ against the structure tensor.
		final var rnd2 = randomImage(new Random(7), 16, 17);
		final var t2 = ops.op("filter.structureTensor").input(rnd2, 1.0, 1.5)
			.outType(OUT).apply();
		final var r2 = ops.op("filter.harris").input(rnd2, 1.0, 1.5, 0.06)
			.outType(OUT).apply();
		final var j = at(t2, 8, 9);
		final var tr2 = j[0] + j[2];
		assertEquals(j[0] * j[2] - j[1] * j[1] - 0.06 * tr2 * tr2, at(r2, 8,
			9)[0], 1e-9);

		final var rnd3 = randomImage(new Random(8), 10, 11, 12);
		final var t3 = ops.op("filter.structureTensor").input(rnd3, 1.0, 1.5)
			.outType(OUT).apply();
		final var r3 = ops.op("filter.harris").input(rnd3, 1.0, 1.5).outType(OUT)
			.apply();
		final var m = at(t3, 4, 5, 6);
		final double xx = m[0], xy = m[1], xz = m[2], yy = m[3], yz = m[4],
				zz = m[5];
		final var det = xx * (yy * zz - yz * yz) - xy * (xy * zz - yz * xz) + xz *
			(xy * yz - yy * xz);
		final var tr3 = xx + yy + zz;
		assertEquals(det - 0.04 * tr3 * tr3 * tr3, at(r3, 4, 5, 6)[0], 1e-9 * Math
			.abs(tr3 * tr3 * tr3));
	}

	// -- Helper methods --

	private void assertShapeIndex(final double expected,
		final ToDoubleFunction<double[]> f)
	{
		final var img = image(f, 21, 21);
		final var si = ops.op("filter.shapeIndex").input(img, 1.0).outType(OUT)
			.apply();
		assertEquals(expected, at(si, 10, 10)[0], 1e-6);
	}

	/** Returns sheetness at the center and 3 pixels off the plate center. */
	private double[] sheetness(final Img<DoubleType> img, final double sigma) {
		final var out = ops.op("filter.sheetness").input(img, sigma).outType(OUT)
			.apply();
		return new double[] { at(out, 10, 10, 10)[0], at(out, 10, 10, 13)[0] };
	}

	private static double gauss(final double x, final double s) {
		return Math.exp(-x * x / (2 * s * s));
	}

	private static Img<DoubleType> stripes(final double alpha) {
		final var w = 2 * Math.PI / 20;
		final var nx = Math.sin(alpha);
		final var ny = Math.cos(alpha);
		return image(p -> Math.sin(w * (nx * p[0] + ny * p[1])), 64, 64);
	}

	/**
	 * Creates an image whose values are given by a function of the position
	 * relative to the image center.
	 */
	private static Img<DoubleType> image(final ToDoubleFunction<double[]> f,
		final long... dims)
	{
		final var img = ArrayImgs.doubles(dims);
		final var c = img.localizingCursor();
		final var p = new double[dims.length];
		while (c.hasNext()) {
			c.fwd();
			for (var d = 0; d < dims.length; d++)
				p[d] = c.getDoublePosition(d) - dims[d] / 2;
			c.get().set(f.applyAsDouble(p));
		}
		return img;
	}

	private static Img<DoubleType> randomImage(final Random r,
		final long... dims)
	{
		final var img = ArrayImgs.doubles(dims);
		img.forEach(t -> t.set(r.nextDouble()));
		return img;
	}

	/** Gets all values along the last dimension at the given position. */
	private static double[] at(final RandomAccessibleInterval<DoubleType> img,
		final long... pos)
	{
		final var n = img.numDimensions();
		final var ra = img.randomAccess();
		if (pos.length == n) {
			ra.setPosition(pos);
			return new double[] { ra.get().get() };
		}
		final var values = new double[(int) img.dimension(n - 1)];
		for (var d = 0; d < pos.length; d++)
			ra.setPosition(pos[d], d);
		for (var i = 0; i < values.length; i++) {
			ra.setPosition(i, n - 1);
			values[i] = ra.get().get();
		}
		return values;
	}
}
