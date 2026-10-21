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

package org.scijava.ops.image.geom.geom3d;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.integer.IntType;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultInertiaTensor3D}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class InertiaTensor3DTest extends AbstractOpTest {

	/**
	 * Checks the second moments of a box whose centroid has distinct
	 * coordinates, against the analytic values for a discrete uniform box: the
	 * variance along an axis of length n is (n^2 - 1) / 12, and the cross
	 * moments vanish by symmetry.
	 */
	@Test
	public void testOffCenterBox() {
		final var img = ArrayImgs.ints(20, 20, 20);
		final ImgLabeling<Integer, IntType> labeling = new ImgLabeling<>(img);
		final var ra = labeling.randomAccess();
		final int[] min = { 2, 7, 11 };
		final int[] len = { 3, 5, 7 };
		for (int z = min[2]; z < min[2] + len[2]; z++) {
			for (int y = min[1]; y < min[1] + len[1]; y++) {
				for (int x = min[0]; x < min[0] + len[0]; x++) {
					ra.setPositionAndGet(x, y, z).add(1);
				}
			}
		}
		final var region = new LabelRegions<>(labeling).getLabelRegion(1);
		final var tensor = ops.op("geom.secondMoment").input(region).outType(
			RealMatrix.class).apply();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				final double expected = i == j ? (len[i] * len[i] - 1) / 12.0 : 0;
				assertEquals(expected, tensor.getEntry(i, j), 1e-10, "entry (" + i +
					", " + j + ")");
			}
		}
	}
}
