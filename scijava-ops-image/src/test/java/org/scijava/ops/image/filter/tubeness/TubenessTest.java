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

package org.scijava.ops.image.filter.tubeness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;
import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

/**
 * Tests {@link DefaultTubeness}.
 *
 * @author Gabriel Selzer
 */
public class TubenessTest extends AbstractOpTest {

	@Test
	public void testTubeness() {
		Img<UnsignedByteType> input = openRelativeUnsignedByteImg(
			DefaultTubeness.class, "TubesInput.png");
		Img<DoubleType> expected = openDoubleImg("tube.tif");

		final double scale = 5;
		final double sigma = scale / Math.sqrt(2);

		Img<DoubleType> actual = ops.op("create.img").input(input, new DoubleType())
			.outType(new Nil<Img<DoubleType>>()
			{}).apply();
		ops.op("filter.tubeness").input(input, sigma).output(actual).compute();

		ImgLib2Assert.assertImageEquals(expected, actual);
	}

	/**
	 * Tests that calibration is honored: a straight tube along X with a Gaussian
	 * cross-section of physical standard deviation {@code s = 6} is sampled with
	 * voxel size 1 x 1 x 2. At the tube axis, the Gaussian-smoothed Hessian has
	 * two equal eigenvalues {@code -100 s² / (s² + σ²)²}, so the expected
	 * tubeness is {@code σ² × 100 s² / (s² + σ²)²}, up to the finite difference
	 * error of the Hessian (about 6% here).
	 * <p>
	 * Note: VIB's {@code TubenessProcessor} with calibration enabled smooths
	 * with calibrated sigmas but differentiates in pixel units, which yields
	 * twice the analytic value here (10.90 instead of 5.54 for σ = 1.5).
	 */
	@Test
	public void testTubenessCalibrated() {
		final double s = 6;
		final double[] cal = { 1, 1, 2 };
		final int w = 11, h = 61, d = 31;
		final var input = ArrayImgs.doubles(w, h, d);
		final var ra = input.randomAccess();
		for (int z = 0; z < d; z++) {
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					final double dy = (y - 30) * cal[1], dz = (z - 15) * cal[2];
					ra.setPositionAndGet(x, y, z).set(100 * Math.exp(-(dy * dy + dz *
						dz) / (2 * s * s)));
				}
			}
		}
		for (final double sigma : new double[] { 1.5, 2.5 }) {
			final var actual = ArrayImgs.doubles(w, h, d);
			ops.op("filter.tubeness").input(input, sigma, cal).output(actual)
				.compute();
			final double v = actual.randomAccess().setPositionAndGet(5, 30, 15)
				.get();
			final double ss = s * s + sigma * sigma;
			final double expected = sigma * sigma * 100 * s * s / (ss * ss);
			assertEquals(expected, v, 0.1 * expected);
		}
	}

	/**
	 * A crop view with a non-zero minimum must be filtered over its own
	 * window, giving the same result as a zero-min copy of the crop.
	 */
	@Test
	public void testNonZeroMinInput() {
		Img<UnsignedByteType> input = openRelativeUnsignedByteImg(
			DefaultTubeness.class, "TubesInput.png");
		final long[] min = { 20, 15 };
		final long[] max = { 59, 49 };
		final var crop = Views.interval(input, min, max);
		final var copy = ArrayImgs.unsignedBytes(40, 35);
		final var cc = copy.cursor();
		for (final var t : Views.flatIterable(crop))
			cc.next().set(t);

		final var expected = ArrayImgs.doubles(40, 35);
		ops.op("filter.tubeness").input(copy, 2.0).output(expected).compute();
		final var actual = ArrayImgs.doubles(40, 35);
		ops.op("filter.tubeness").input(crop, 2.0).output(actual).compute();
		ImgLib2Assert.assertImageEquals(expected, actual);
	}

	/** Only 2D and 3D images are supported. */
	@Test
	public void testUnsupportedDimensionality() {
		final var input = ArrayImgs.doubles(5, 5, 5, 5);
		final var output = ArrayImgs.doubles(5, 5, 5, 5);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"filter.tubeness").input(input, 1.0).output(output).compute());
	}

}
