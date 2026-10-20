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

package org.scijava.ops.image.filter.weightedMedian;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link GaussianWeightedMedian}.
 *
 * @author Curtis Rueden
 */
public class GaussianWeightedMedianTest extends AbstractOpTest {

	@Test
	public void testKernel() {
		final var kernel = GaussianWeightedMedian.kernel(2, 1.0, 2);
		final var ra = kernel.randomAccess();
		assertEquals(1, ra.setPositionAndGet(2, 2).get(), 1e-12);
		assertEquals(Math.exp(-0.5), ra.setPositionAndGet(3, 2).get(), 1e-12);
		assertEquals(Math.exp(-1), ra.setPositionAndGet(1, 1).get(), 1e-12);
		assertEquals(Math.exp(-2), ra.setPositionAndGet(2, 0).get(), 1e-12);
		// The corners lie outside the circle of radius 2.
		assertEquals(0, ra.setPositionAndGet(0, 0).get(), 0);
		assertEquals(0, ra.setPositionAndGet(4, 3).get(), 0);
	}

	@Test
	public void testSpikeRemovedAndEdgePreserved() {
		// A vertical step edge with a single-pixel spike: the spike carries less
		// than half of the total weight, so it is removed, while the step edge
		// (a median root) is preserved exactly.
		final var in = ArrayImgs.unsignedShorts(20, 16);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(c.getLongPosition(0) < 10 ? 100 : 500);
		}
		in.randomAccess().setPositionAndGet(4, 8).set(4000);
		final var out = ArrayImgs.unsignedShorts(20, 16);
		ops.op("filter.gaussianWeightedMedian").input(in, 1.0).output(out)
			.compute();
		final var oc = out.localizingCursor();
		while (oc.hasNext()) {
			oc.fwd();
			assertEquals(oc.getLongPosition(0) < 10 ? 100 : 500, oc.get().get());
		}
	}

	@Test
	public void testMatchesWeightedMedian() {
		final var random = new Random(3);
		final var in = ArrayImgs.floats(12, 11, 5);
		in.forEach(t -> t.set(random.nextFloat()));
		final var out = ArrayImgs.floats(12, 11, 5);
		ops.op("filter.gaussianWeightedMedian").input(in, 1.5, 2).output(out)
			.compute();
		final var expected = ArrayImgs.floats(12, 11, 5);
		ops.op("filter.weightedMedian").input(in, GaussianWeightedMedian.kernel(3,
			1.5, 2)).output(expected).compute();
		LoopBuilder.setImages(expected, out).forEachPixel((e, o) -> assertEquals(e
			.get(), o.get(), 0));
	}
}
