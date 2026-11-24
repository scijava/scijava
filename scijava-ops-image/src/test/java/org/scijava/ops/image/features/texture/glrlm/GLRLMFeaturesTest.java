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

package org.scijava.ops.image.features.texture.glrlm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.features.texture.IBSIPhantom;
import org.scijava.ops.image.features.texture.TextureUtils;

/**
 * Tests {@link RunLengthMatrix} and {@link GLRLMFeatures} against the IBSI
 * digital phantom reference values (see {@link IBSIPhantom}).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class GLRLMFeaturesTest extends AbstractOpTest {

	private static final String[] NAMES = { "shortRunsEmphasis",
		"longRunsEmphasis", "lowGrayLevelRunEmphasis", "highGrayLevelRunEmphasis",
		"shortRunLowGrayLevelEmphasis", "shortRunHighGrayLevelEmphasis",
		"longRunLowGrayLevelEmphasis", "longRunHighGrayLevelEmphasis",
		"grayLevelNonUniformity", "grayLevelNonUniformityNormalized",
		"runLengthNonUniformity", "runLengthNonUniformityNormalized",
		"runPercentage", "grayLevelVariance", "runLengthVariance", "runEntropy" };

	private static Map<String, Double> expected(final double... values) {
		final Map<String, Double> map = new LinkedHashMap<>();
		for (var i = 0; i < NAMES.length; i++)
			map.put(NAMES[i], values[i]);
		return map;
	}

	private double[][] matrix(final RandomAccessibleInterval<UnsignedByteType> img,
		final RandomAccessibleInterval<BitType> mask, final int[] direction)
	{
		return ops.op("image.runLengthMatrix").input(img, mask, null, direction)
			.outType(double[][].class).apply();
	}

	private double feature(final String name, final double[][] matrix) {
		return ops.op("features.glrlm." + name).input(matrix).outType(
			DoubleType.class).apply().get();
	}

	@Test
	public void testMatrixSimple() {
		// Rows 1 1 2 and 2 2 2: runs (1, length 2), (2, length 1) and
		// (2, length 3) along the first axis, covering 6 voxels.
		final var img = ArrayImgs.unsignedBytes(new byte[] { 1, 1, 2, 2, 2, 2 },
			3, 2);
		final double[][] m = ops.op("image.runLengthMatrix").input(img, null, null,
			new int[] { 1, 0 }).outType(double[][].class).apply();
		assertArrayEquals(new double[] { 0, 1, 0 }, m[0]);
		assertArrayEquals(new double[] { 1, 0, 1 }, m[1]);
		assertEquals(0.5, feature("runPercentage", m), 1e-12);
	}

	@Test
	public void testPhantom3DMerged() {
		final var m = ops.op("image.runLengthMatrix").input(IBSIPhantom.image(),
			IBSIPhantom.mask()).outType(double[][].class).apply();
		IBSIPhantom.assertFeatures(expected(0.729, 2.76, 0.607, 9.64, 0.372, 8.67,
			2.16, 15.6, 281, 0.43, 328, 0.501, 0.68, 3.48, 0.598, 2.62), //
			name -> feature(name, m));
	}

	@Test
	public void testPhantom3DAveraged() {
		final List<double[][]> matrices = new ArrayList<>();
		for (final var dir : TextureUtils.directions(3))
			matrices.add(matrix(IBSIPhantom.image(), IBSIPhantom.mask(), dir));
		IBSIPhantom.assertFeatures(expected(0.705, 3.06, 0.603, 9.7, 0.352, 8.54,
			2.39, 17.6, 21.8, 0.43, 26.9, 0.513, 0.68, 3.46, 0.574, 2.43), //
			name -> average(name, matrices));
	}

	@Test
	public void testPhantom2DAveraged() {
		final List<double[][]> matrices = new ArrayList<>();
		final var masks = IBSIPhantom.slices(IBSIPhantom.mask());
		final var imgs = IBSIPhantom.slices(IBSIPhantom.image());
		for (var z = 0; z < imgs.size(); z++)
			for (final var dir : TextureUtils.directions(2))
				matrices.add(matrix(imgs.get(z), masks.get(z), dir));
		IBSIPhantom.assertFeatures(expected(0.641, 3.78, 0.604, 9.82, 0.294, 8.57,
			3.14, 17.4, 5.2, 0.46, 6.12, 0.492, 0.627, 3.35, 0.761, 2.17), //
			name -> average(name, matrices));
	}

	@Test
	public void testPhantom2DSliceMerged() {
		final List<double[][]> matrices = new ArrayList<>();
		final var masks = IBSIPhantom.slices(IBSIPhantom.mask());
		final var imgs = IBSIPhantom.slices(IBSIPhantom.image());
		for (var z = 0; z < imgs.size(); z++)
			matrices.add(matrix(imgs.get(z), masks.get(z), null));
		IBSIPhantom.assertFeatures(expected(0.661, 3.51, 0.609, 9.74, 0.311, 8.67,
			2.92, 16.1, 20.5, 0.456, 21.6, 0.441, 0.627, 3.37, 0.778, 2.57), //
			name -> average(name, matrices));
	}

	private double average(final String name, final List<double[][]> matrices) {
		final List<Double> values = new ArrayList<>();
		for (final var m : matrices)
			values.add(feature(name, m));
		return IBSIPhantom.mean(values);
	}
}
