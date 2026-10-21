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

package org.scijava.ops.image.features.texture.ngtdm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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

/**
 * Tests {@link NeighborhoodGrayToneDifferenceMatrix} and {@link NGTDMFeatures}
 * against the IBSI digital phantom reference values (see {@link IBSIPhantom}).
 * As in IBSI, the 2D features are computed per slice and averaged.
 *
 * @author Curtis Rueden
 */
public class NGTDMFeaturesTest extends AbstractOpTest {

	private static final String[] NAMES = { "coarseness", "contrast",
		"busyness", "complexity", "strength" };

	private static Map<String, Double> expected(final double... values) {
		final Map<String, Double> map = new LinkedHashMap<>();
		for (var i = 0; i < NAMES.length; i++)
			map.put(NAMES[i], values[i]);
		return map;
	}

	private double[][] matrix(final RandomAccessibleInterval<UnsignedByteType> img,
		final RandomAccessibleInterval<BitType> mask)
	{
		return ops.op("image.neighborhoodGrayToneDifferenceMatrix").input(img,
			mask).outType(double[][].class).apply();
	}

	private double feature(final String name, final double[][] matrix) {
		return ops.op("features.ngtdm." + name).input(matrix).outType(
			DoubleType.class).apply().get();
	}

	@Test
	public void testMatrixSimple() {
		// In a 3x1 image 1 1 4, the first 1 differs by 0 from its neighbor, the
		// second by |1 - 2.5| = 1.5 from its neighbors, and the 4 by 3.
		final var img = ArrayImgs.unsignedBytes(new byte[] { 1, 1, 4 }, 3, 1);
		final double[][] m = ops.op("image.neighborhoodGrayToneDifferenceMatrix")
			.input(img).outType(double[][].class).apply();
		assertArrayEquals(new double[] { 2, 2.0 / 3, 1.5 }, m[0], 1e-12);
		assertArrayEquals(new double[] { 0, 0, 0 }, m[1], 1e-12);
		assertArrayEquals(new double[] { 1, 1.0 / 3, 3 }, m[3], 1e-12);
	}

	@Test
	public void testPhantom3D() {
		final var m = matrix(IBSIPhantom.image(), IBSIPhantom.mask());
		IBSIPhantom.assertFeatures(expected(0.0296, 0.584, 6.54, 13.5, 0.763),
			name -> feature(name, m));
	}

	@Test
	public void testPhantom2D() {
		final var imgs = IBSIPhantom.slices(IBSIPhantom.image());
		final var masks = IBSIPhantom.slices(IBSIPhantom.mask());
		IBSIPhantom.assertFeatures(expected(0.121, 0.925, 2.99, 10.4, 2.88),
			name -> {
				final List<Double> values = new ArrayList<>();
				for (var z = 0; z < imgs.size(); z++)
					values.add(feature(name, matrix(imgs.get(z), masks.get(z))));
				return IBSIPhantom.mean(values);
			});
	}
}
