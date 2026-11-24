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

package org.scijava.ops.image.features.texture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

/**
 * The IBSI 1 digital phantom (5 x 4 x 4 voxels, gray levels 1 to 6) and helpers
 * for comparing texture features against the IBSI reference values.
 * <p>
 * The phantom is part of the IBSI data sets
 * (https://github.com/theibsi/data_sets), original design by Alex Zwanenburg,
 * licensed under the Creative Commons Attribution 4.0 International License
 * (http://creativecommons.org/licenses/by/4.0/). The voxel values below were
 * transcribed from {@code ibsi_1_digital_phantom/tiff/phantom_img.tif} and
 * {@code phantom_msk.tif} as bundled with RadiomicsJ. The reference feature
 * values used by the tests are the IBSI consensus values for the digital
 * phantom, taken from {@code validation/IBSI_ValidationFile.xlsx} of RadiomicsJ
 * (Apache License 2.0); they are given to 3 significant digits.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class IBSIPhantom {

	private IBSIPhantom() {}

	/** Phantom intensities, indexed [z][y][x]. */
	private static final int[][][] IMAGE = { //
		{ { 1, 4, 4, 1, 1 }, { 1, 4, 6, 1, 1 }, { 4, 1, 6, 4, 1 }, { 4, 4, 6, 4,
			1 } }, //
		{ { 1, 4, 4, 1, 1 }, { 1, 1, 6, 1, 1 }, { 1, 1, 3, 1, 1 }, { 4, 4, 6, 1,
			1 } }, //
		{ { 1, 4, 4, 1, 1 }, { 1, 1, 1, 1, 1 }, { 1, 1, 9, 1, 1 }, { 1, 1, 6, 1,
			1 } }, //
		{ { 1, 4, 4, 1, 1 }, { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 }, { 1, 1, 6, 1,
			1 } } };

	/** Phantom ROI mask, indexed [z][y][x]. */
	private static final int[][][] MASK = { //
		{ { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1,
			1 } }, //
		{ { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1 }, { 1, 1, 1, 1,
			1 } }, //
		{ { 1, 1, 1, 0, 0 }, { 1, 1, 1, 1, 1 }, { 1, 1, 0, 1, 1 }, { 1, 1, 1, 1,
			1 } }, //
		{ { 1, 1, 1, 0, 0 }, { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1,
			1 } } };

	/** @return the phantom image, with dimensions [x, y, z] = [5, 4, 4]. */
	public static Img<UnsignedByteType> image() {
		final var img = ArrayImgs.unsignedBytes(5, 4, 4);
		final var ra = img.randomAccess();
		for (var z = 0; z < 4; z++)
			for (var y = 0; y < 4; y++)
				for (var x = 0; x < 5; x++)
					ra.setPositionAndGet(x, y, z).set(IMAGE[z][y][x]);
		return img;
	}

	/** @return the phantom mask, with dimensions [x, y, z] = [5, 4, 4]. */
	public static Img<BitType> mask() {
		final var img = ArrayImgs.bits(5, 4, 4);
		final var ra = img.randomAccess();
		for (var z = 0; z < 4; z++)
			for (var y = 0; y < 4; y++)
				for (var x = 0; x < 5; x++)
					ra.setPositionAndGet(x, y, z).set(MASK[z][y][x] != 0);
		return img;
	}

	/**
	 * @param img a 3D image
	 * @return the 2D slices of the image along the last axis
	 */
	public static <T> List<RandomAccessibleInterval<T>> slices(
		final RandomAccessibleInterval<T> img)
	{
		final List<RandomAccessibleInterval<T>> slices = new ArrayList<>();
		for (var z = 0; z < img.dimension(2); z++)
			slices.add(Views.hyperSlice(img, 2, z));
		return slices;
	}

	/**
	 * Asserts that features match IBSI reference values, which are given to 3
	 * significant digits.
	 *
	 * @param expected map from feature name to reference value
	 * @param actual function computing the value of a named feature
	 */
	public static void assertFeatures(final Map<String, Double> expected,
		final Function<String, Double> actual)
	{
		for (final var e : expected.entrySet()) {
			final double value = actual.apply(e.getKey());
			final var tolerance = 0.005 * Math.abs(e.getValue()) + 1e-12;
			assertEquals(e.getValue(), value, tolerance, e.getKey());
		}
	}

	/**
	 * @param values a list of values
	 * @return the mean of the values
	 */
	public static double mean(final List<Double> values) {
		var sum = 0.0;
		for (final var v : values)
			sum += v;
		return sum / values.size();
	}
}
