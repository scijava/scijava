/*-
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

package org.scijava.ops.image.stats.entropy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imglib2.img.array.ArrayImgs;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultEntropy}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultEntropyTest extends AbstractOpTest {

	@Test
	public void testUniform() {
		final var img = ArrayImgs.unsignedBytes(new byte[] { 0, 1, 2, 3, 3, 2, 1,
			0 }, 8);
		final Double h = ops.op("stats.entropy").input(img, 4).outType(Double.class)
			.apply();
		assertEquals(2, h, 1e-12);
	}

	@Test
	public void testTwoLevels() {
		// One quarter of the pixels at 1, the rest at 0.
		final var img = ArrayImgs.doubles(new double[] { 0, 0, 0, 1 }, 2, 2);
		final Double h = ops.op("stats.entropy").input(img).outType(Double.class)
			.apply();
		final double expected = -(0.25 * Math.log(0.25) + 0.75 * Math.log(0.75)) /
			Math.log(2);
		assertEquals(expected, h, 1e-12);
	}

	@Test
	public void testConstant() {
		final var img = ArrayImgs.floats(new float[] { 7, 7, 7, 7, 7, 7 }, 3, 2);
		final Double h = ops.op("stats.entropy").input(img, 16).outType(
			Double.class).apply();
		assertEquals(0, h, 0);
	}
}
