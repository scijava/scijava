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

package org.scijava.ops.image.labeling.featuremap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultFeatureMap}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class FeatureMapTest extends AbstractOpTest {

	@Test
	public void testFeatureMap() {
		final var labeling = new ImgLabeling<String, IntType>(ArrayImgs.ints(6, 4,
			2));
		final var ra = labeling.randomAccess();
		ra.setPositionAndGet(0, 0, 0).add("a");
		ra.setPositionAndGet(1, 0, 0).add("a");
		ra.setPositionAndGet(5, 3, 1).add("b");
		ra.setPositionAndGet(2, 2, 1).add("c");
		// This pixel belongs to both a and b.
		ra.setPositionAndGet(3, 3, 0).add("a");
		ra.get().add("b");

		// Use each label's size as its feature.
		final Map<String, Long> sizes = new HashMap<>();
		for (final var region : new LabelRegions<>(labeling))
			sizes.put(region.getLabel(), region.inside().size());
		sizes.remove("c");

		final var out = ArrayImgs.doubles(6, 4, 2);
		ops.op("labeling.featureMap").input(labeling, sizes, -1.0).output(out)
			.compute();
		final var o = out.randomAccess();
		assertEquals(3, o.setPositionAndGet(0, 0, 0).get());
		assertEquals(3, o.setPositionAndGet(1, 0, 0).get());
		assertEquals(2, o.setPositionAndGet(5, 3, 1).get());
		assertEquals(2.5, o.setPositionAndGet(3, 3, 0).get());
		// Label c has no value, so it gets the background value.
		assertEquals(-1, o.setPositionAndGet(2, 2, 1).get());
		assertEquals(-1, o.setPositionAndGet(4, 1, 0).get());

		final var outDefault = ArrayImgs.floats(6, 4, 2);
		ops.op("labeling.featureMap").input(labeling, sizes).output(outDefault)
			.compute();
		assertEquals(0, outDefault.randomAccess().setPositionAndGet(4, 1, 0).get());
		assertEquals(3, outDefault.randomAccess().setPositionAndGet(0, 0, 0).get());
	}
}
