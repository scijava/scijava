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

package org.scijava.ops.image.features.texture.ngldm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.features.texture.IBSIPhantom;

/**
 * Tests {@link NeighboringGrayLevelDependenceMatrix} and {@link NGLDMFeatures} against the IBSI
 * digital phantom reference values (see {@link IBSIPhantom}). As in IBSI, the
 * 2D features are computed per slice and averaged.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class NGLDMFeaturesTest extends AbstractOpTest {

	private static final String[] NAMES = { "lowDependenceEmphasis",
		"highDependenceEmphasis",
		"lowGrayLevelCountEmphasis",
		"highGrayLevelCountEmphasis",
		"lowDependenceLowGrayLevelEmphasis",
		"lowDependenceHighGrayLevelEmphasis",
		"highDependenceLowGrayLevelEmphasis",
		"highDependenceHighGrayLevelEmphasis",
		"grayLevelNonUniformity",
		"grayLevelNonUniformityNormalized",
		"dependenceCountNonUniformity",
		"dependenceCountNonUniformityNormalized",
		"dependenceCountPercentage",
		"grayLevelVariance",
		"dependenceCountVariance",
		"dependenceCountEntropy",
		"dependenceCountEnergy" };

	private static final Set<String> NEEDS_VOXEL_COUNT = Set.of("dependenceCountPercentage");

	private static Map<String, Double> expected(final double... values) {
		final Map<String, Double> map = new LinkedHashMap<>();
		for (var i = 0; i < NAMES.length; i++)
			map.put(NAMES[i], values[i]);
		return map;
	}

	private double[][] matrix(final RandomAccessibleInterval<UnsignedByteType> img,
		final RandomAccessibleInterval<BitType> mask)
	{
		return ops.op("image.neighboringGrayLevelDependenceMatrix").input(img, mask).outType(
			double[][].class).apply();
	}

	private double feature(final String name, final double[][] matrix,
		final long nVoxels)
	{
		final var op = ops.op("features.ngldm." + name);
		if (NEEDS_VOXEL_COUNT.contains(name)) {
			return op.input(matrix, nVoxels).outType(DoubleType.class).apply().get();
		}
		return op.input(matrix).outType(DoubleType.class).apply().get();
	}

	private static long count(final RandomAccessibleInterval<BitType> mask) {
		long n = 0;
		for (final var b : mask)
			if (b.get()) n++;
		return n;
	}

	@Test
	public void testPhantom3D() {
		final var m = matrix(IBSIPhantom.image(), IBSIPhantom.mask());
		final var nVoxels = count(IBSIPhantom.mask());
		IBSIPhantom.assertFeatures(expected(0.045, 109, 0.693, 7.66, 0.00963, 0.736, 102, 235, 37.9, 0.512, 4.86, 0.0657, 1, 3.05, 22.1, 4.4, 0.0533), //
			name -> feature(name, m, nVoxels));
	}

	@Test
	public void testPhantom2D() {
		final var imgs = IBSIPhantom.slices(IBSIPhantom.image());
		final var masks = IBSIPhantom.slices(IBSIPhantom.mask());
		IBSIPhantom.assertFeatures(expected(0.158, 19.2, 0.702, 7.49, 0.0473, 3.06, 17.6, 49.5, 10.2, 0.562, 3.96, 0.212, 1, 2.7, 2.73, 2.71, 0.17), name -> {
			final List<Double> values = new ArrayList<>();
			for (var z = 0; z < imgs.size(); z++) {
				values.add(feature(name, matrix(imgs.get(z), masks.get(z)), count(
					masks.get(z))));
			}
			return IBSIPhantom.mean(values);
		});
	}

	@Test
	public void testMatrixSimple() {
		// In a 3x1 image 1 1 2, each 1 has one dependent neighbor and the 2 none.
		final var img = ArrayImgs.unsignedBytes(new byte[] { 1, 1, 2 }, 3, 1);
		final double[][] m = ops.op("image.neighboringGrayLevelDependenceMatrix")
			.input(img).outType(double[][].class).apply();
		assertArrayEquals(new double[] { 0, 2 }, m[0]);
		assertArrayEquals(new double[] { 1, 0 }, m[1]);
		// With coarseness 1, all three voxels depend on their neighbors.
		final double[][] m1 = ops.op("image.neighboringGrayLevelDependenceMatrix")
			.input(img, null, null, 1, 1).outType(double[][].class).apply();
		assertArrayEquals(new double[] { 0, 1, 1 }, m1[0]);
		assertArrayEquals(new double[] { 0, 1, 0 }, m1[1]);
		assertEquals(1.0, feature("dependenceCountPercentage", m1, 3), 1e-12);
	}
}
