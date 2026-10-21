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

package org.scijava.ops.image.geom.extent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.geom.real.DefaultWritablePolygon2D;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link Extents}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class ExtentsTest extends AbstractOpTest {

	private double extent(final Object shape) {
		return ops.op("geom.extent").input(shape).outType(DoubleType.class).apply()
			.get();
	}

	@Test
	public void testPolygons() {
		final var rectangle = new DefaultWritablePolygon2D(new double[] { 1, 11, 11,
			1 }, new double[] { 2, 2, 6, 6 });
		assertEquals(1, extent(rectangle), 1e-12);
		final var triangle = new DefaultWritablePolygon2D(new double[] { 0, 10,
			0 }, new double[] { 0, 0, 6 });
		assertEquals(0.5, extent(triangle), 1e-12);
		final var diamond = new DefaultWritablePolygon2D(new double[] { 5, 10, 5,
			0 }, new double[] { 0, 5, 10, 5 });
		assertEquals(0.5, extent(diamond), 1e-12);
	}

	@Test
	public void testRegion3D() {
		// An L-shaped region of 3 voxels in a 2 x 2 x 1 bounding box, and a
		// single voxel.
		final var index = ArrayImgs.ints(5, 5, 5);
		final var ra = index.randomAccess();
		ra.setPositionAndGet(1, 1, 1).set(1);
		ra.setPositionAndGet(2, 1, 1).set(1);
		ra.setPositionAndGet(1, 2, 1).set(1);
		ra.setPositionAndGet(3, 3, 3).set(2);
		final var labeling = ImgLabeling.fromImageAndLabels(index, Arrays.asList(
			1, 2));
		final var regions = new LabelRegions<>(labeling);
		assertEquals(0.75, extent(regions.getLabelRegion(1)), 1e-12);
		assertEquals(1, extent(regions.getLabelRegion(2)), 1e-12);
	}
}
