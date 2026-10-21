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

package org.scijava.ops.image.features.texture.glszm;

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
 * Tests {@link SizeZoneMatrix} and {@link GLSZMFeatures} against the IBSI
 * digital phantom reference values (see {@link IBSIPhantom}). As in IBSI, the
 * 2D features are computed per slice and averaged.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class GLSZMFeaturesTest extends AbstractOpTest {

	private static final String[] NAMES = { "smallZoneEmphasis",
		"largeZoneEmphasis",
		"lowGrayLevelZoneEmphasis",
		"highGrayLevelZoneEmphasis",
		"smallZoneLowGrayLevelEmphasis",
		"smallZoneHighGrayLevelEmphasis",
		"largeZoneLowGrayLevelEmphasis",
		"largeZoneHighGrayLevelEmphasis",
		"grayLevelNonUniformity",
		"grayLevelNonUniformityNormalized",
		"zoneSizeNonUniformity",
		"zoneSizeNonUniformityNormalized",
		"zonePercentage",
		"grayLevelVariance",
		"zoneSizeVariance",
		"zoneSizeEntropy" };

	private static final Set<String> NEEDS_VOXEL_COUNT = Set.of();

	private static Map<String, Double> expected(final double... values) {
		final Map<String, Double> map = new LinkedHashMap<>();
		for (var i = 0; i < NAMES.length; i++)
			map.put(NAMES[i], values[i]);
		return map;
	}

	private double[][] matrix(final RandomAccessibleInterval<UnsignedByteType> img,
		final RandomAccessibleInterval<BitType> mask)
	{
		return ops.op("image.sizeZoneMatrix").input(img, mask).outType(
			double[][].class).apply();
	}

	private double feature(final String name, final double[][] matrix,
		final long nVoxels)
	{
		final var op = ops.op("features.glszm." + name);
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
		IBSIPhantom.assertFeatures(expected(0.255, 550, 0.253, 15.6, 0.0256, 2.76, 503, 1490, 1.4, 0.28, 1, 0.2, 0.0676, 2.64, 331, 2.32), //
			name -> feature(name, m, nVoxels));
	}

	@Test
	public void testPhantom2D() {
		final var imgs = IBSIPhantom.slices(IBSIPhantom.image());
		final var masks = IBSIPhantom.slices(IBSIPhantom.mask());
		IBSIPhantom.assertFeatures(expected(0.363, 43.9, 0.371, 16.4, 0.0259, 10.3, 40.4, 113, 1.41, 0.323, 1.49, 0.333, 0.24, 3.97, 21, 1.93), name -> {
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
		// Under full connectivity, the diagonal pairs of 1s and of 2s each form a
		// single zone of size 2.
		final var img = ArrayImgs.unsignedBytes(new byte[] { 2, 1, 1, 2 }, 2, 2);
		final double[][] m = ops.op("image.sizeZoneMatrix").input(img).outType(
			double[][].class).apply();
		assertArrayEquals(new double[] { 0, 1 }, m[0]);
		assertArrayEquals(new double[] { 0, 1 }, m[1]);
		assertEquals(0.5, feature("zonePercentage", m, 4), 1e-12);
	}
}
