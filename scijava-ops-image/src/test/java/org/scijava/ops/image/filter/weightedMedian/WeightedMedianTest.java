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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultWeightedMedian}.
 *
 * @author Curtis Rueden
 */
public class WeightedMedianTest extends AbstractOpTest {

	@Test
	public void testUniformKernelIsMedian() {
		final var random = new Random(42);
		final var in = ArrayImgs.floats(17, 13, 4);
		in.forEach(t -> t.set(random.nextInt(1000)));
		final var kernel = ArrayImgs.doubles(3, 3, 3);
		kernel.forEach(t -> t.set(2.5));

		final var out = ArrayImgs.floats(17, 13, 4);
		ops.op("filter.weightedMedian").input(in, kernel).output(out).compute();
		final var expected = ArrayImgs.floats(17, 13, 4);
		ops.op("filter.median").input(in, new RectangleShape(1, false),
			new OutOfBoundsBorderFactory<>()).output(expected).compute();
		LoopBuilder.setImages(expected, out).forEachPixel((e, o) -> assertEquals(e
			.get(), o.get(), 0));
	}

	@Test
	public void testHandComputed() {
		final var in = ArrayImgs.doubles(new double[] { 1, 10, 3, 2, 11 }, 5);
		final var kernel = ArrayImgs.doubles(new double[] { 1, 1, 3, 1, 1 }, 5);
		final var out = ArrayImgs.doubles(5);
		ops.op("filter.weightedMedian").input(in, kernel).output(out).compute();
		// At x=2 the neighborhood is (1, 10, 3, 2, 11) with weights
		// (1, 1, 3, 1, 1); sorted, the cumulative weights are 1 (1), 2 (2),
		// 5 (3), 6 (10) and 7 (11), so the weighted median is 3.
		assertEquals(3, out.randomAccess().setPositionAndGet(2).get());
		// At x=0 the border-extended neighborhood is (1, 1, 1, 10, 3), so the
		// median is 1.
		assertEquals(1, out.randomAccess().setPositionAndGet(0).get());
		// At x=4 the neighborhood is (3, 2, 11, 11, 11), so the median is 11.
		assertEquals(11, out.randomAccess().setPositionAndGet(4).get());
	}

	@Test
	public void testDominantCenterIsIdentity() {
		final var random = new Random(7);
		final var in = ArrayImgs.unsignedBytes(9, 8);
		in.forEach(t -> t.set(random.nextInt(256)));
		final var kernel = ArrayImgs.doubles(new double[] { 1, 1, 1, 1, 9, 1, 1, 1,
			1 }, 3, 3);
		final var out = ArrayImgs.unsignedBytes(9, 8);
		ops.op("filter.weightedMedian").input(in, kernel).output(out).compute();
		LoopBuilder.setImages(in, out).forEachPixel((i, o) -> assertEquals(i.get(),
			o.get()));
	}

	@Test
	public void testEvenKernelThrows() {
		final var in = ArrayImgs.doubles(5, 5);
		final var kernel = ArrayImgs.doubles(2, 3);
		kernel.forEach(t -> t.set(1));
		final var out = ArrayImgs.doubles(5, 5);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"filter.weightedMedian").input(in, kernel).output(out).compute());
	}
}
