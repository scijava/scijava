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

package org.scijava.ops.image.stats;

import static java.util.Collections.shuffle;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DefaultMedian}
 *
 * @author Brian Northan
 * @author Richard Domander
 */
public class DefaultMedianTest extends AbstractOpTest {

	@Test
	public void testOneElement() {
		final List<RealType<DoubleType>> numbers = new ArrayList<>();
		numbers.add(new DoubleType(1.0));

		final DoubleType result = new DoubleType();
		ops.op("stats.median").input(numbers).output(result).compute();

		assertEquals(numbers.get(0).getRealDouble(), result.getRealDouble(), 1e-12);
	}

	@Test
	public void testOddN() {
		final List<RealType<DoubleType>> numbers = new ArrayList<>();
		numbers.add(new DoubleType(1.0));
		numbers.add(new DoubleType(2.0));
		numbers.add(new DoubleType(3.0));
		numbers.add(new DoubleType(4.0));
		numbers.add(new DoubleType(5.0));
		shuffle(numbers);

		final DoubleType result = new DoubleType();
		ops.op("stats.median").input(numbers).output(result).compute();

		assertEquals(3.0, result.getRealDouble(), 1e-12);
	}

	@Test
	public void testEvenN() {
		final List<RealType<DoubleType>> numbers = new ArrayList<>();
		numbers.add(new DoubleType(1.0));
		numbers.add(new DoubleType(2.0));
		numbers.add(new DoubleType(3.0));
		numbers.add(new DoubleType(4.0));
		shuffle(numbers);

		final DoubleType result = new DoubleType();
		ops.op("stats.median").input(numbers).output(result).compute();

		assertEquals(2.5, result.getRealDouble(), 1e-12);
	}

	@Test
	public void testRandomImg() {
		final Img<UnsignedByteType> randomlyFilledImg = TestImgGeneration
			.randomlyFilledUnsignedByteWithSeed(new long[] { 100, 100 }, 1234567890L);

		final DoubleType result = new DoubleType();
		ops.op("stats.median").input(randomlyFilledImg).output(result).compute();

		assertEquals(128d, result.getRealDouble(), 0.00001d);
	}
}
