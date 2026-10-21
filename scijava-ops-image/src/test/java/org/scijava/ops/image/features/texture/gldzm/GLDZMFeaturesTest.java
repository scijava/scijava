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

package org.scijava.ops.image.features.texture.gldzm;

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
 * Tests {@link DistanceZoneMatrix} and {@link GLDZMFeatures} against the IBSI
 * digital phantom reference values (see {@link IBSIPhantom}). As in IBSI, the
 * 2D features are computed per slice and averaged.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class GLDZMFeaturesTest extends AbstractOpTest {

	private static final String[] NAMES = { "smallDistanceEmphasis",
		"largeDistanceEmphasis",
		"lowGrayLevelZoneEmphasis",
		"highGrayLevelZoneEmphasis",
		"smallDistanceLowGrayLevelEmphasis",
		"smallDistanceHighGrayLevelEmphasis",
		"largeDistanceLowGrayLevelEmphasis",
		"largeDistanceHighGrayLevelEmphasis",
		"grayLevelNonUniformity",
		"grayLevelNonUniformityNormalized",
		"zoneDistanceNonUniformity",
		"zoneDistanceNonUniformityNormalized",
		"zonePercentage",
		"grayLevelVariance",
		"zoneDistanceVariance",
		"zoneDistanceEntropy" };

	private static final Set<String> NEEDS_VOXEL_COUNT = Set.of("zonePercentage");

	private static Map<String, Double> expected(final double... values) {
		final Map<String, Double> map = new LinkedHashMap<>();
		for (var i = 0; i < NAMES.length; i++)
			map.put(NAMES[i], values[i]);
		return map;
	}

	private double[][] matrix(final RandomAccessibleInterval<UnsignedByteType> img,
		final RandomAccessibleInterval<BitType> mask)
	{
		return ops.op("image.distanceZoneMatrix").input(img, mask).outType(
			double[][].class).apply();
	}

	private double feature(final String name, final double[][] matrix,
		final long nVoxels)
	{
		final var op = ops.op("features.gldzm." + name);
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
		IBSIPhantom.assertFeatures(expected(1, 1, 0.253, 15.6, 0.253, 15.6, 0.253, 15.6, 1.4, 0.28, 5, 1, 0.0676, 2.64, 0, 1.92), //
			name -> feature(name, m, nVoxels));
	}

	@Test
	public void testPhantom2D() {
		final var imgs = IBSIPhantom.slices(IBSIPhantom.image());
		final var masks = IBSIPhantom.slices(IBSIPhantom.mask());
		IBSIPhantom.assertFeatures(expected(0.946, 1.21, 0.371, 16.4, 0.367, 15.2, 0.386, 21.3, 1.41, 0.323, 3.79, 0.898, 0.24, 3.97, 0.051, 1.73), name -> {
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
		// A 5x5 image of 1s with a central 2: the 1-zone touches the edge
		// (distance 1), while the 2 lies at distance 3 from the edge.
		final var bytes = new byte[25];
		java.util.Arrays.fill(bytes, (byte) 1);
		bytes[12] = 2;
		final var img = ArrayImgs.unsignedBytes(bytes, 5, 5);
		final double[][] m = ops.op("image.distanceZoneMatrix").input(img)
			.outType(double[][].class).apply();
		assertArrayEquals(new double[] { 1, 0, 0 }, m[0]);
		assertArrayEquals(new double[] { 0, 0, 1 }, m[1]);
	}
}
