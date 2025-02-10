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

package org.scijava.ops.image.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.types.Nil;

/**
 * Tests for binary functions in {@link org.scijava.ops.image.math} applied to
 * images
 */
public class ImageBinaryMathTest extends AbstractOpTest {

	private static final IntType I = new IntType(25);

	@Test
	public void testAddImageC() {
		final Img<IntType> imgA = TestImgGeneration.intArray(true, 10, 10);
		final Img<IntType> imgB = TestImgGeneration.intArray(true, 10, 10);
		final Img<IntType> imgC = TestImgGeneration.intArray(true, 10, 10);

		int originalVal = imgA.cursor().next().get() + imgB.cursor().next().get();

		ops.op("math.add").input(imgA, imgB).output(imgC).compute();
		assertEquals(originalVal, imgC.cursor().next().get());
	}

	@Test
	public void testAddImageF() {
		final Img<IntType> imgA = TestImgGeneration.intArray(true, 10, 10);
		final Img<IntType> imgB = TestImgGeneration.intArray(true, 10, 10);

		int originalVal = imgA.cursor().next().get() + imgB.cursor().next().get();
		final var outType = new Nil<Img<IntType>>() {};

		var imgC = ops.op("math.add").input(imgA, imgB).outType(outType).apply();
		assertEquals(originalVal, imgC.cursor().next().get());
	}

	@Test
	public void testSubImageFNoOutType() {
		final Img<IntType> imgA = TestImgGeneration.intArray(true, 10, 10);

		int originalVal = imgA.cursor().next().get();

		var imgC = (Img<IntType>) ops.op("math.sub").input(imgA, new IntType(25))
			.apply();
		assertEquals(originalVal - 25, imgC.cursor().next().get());
	}

	@Test
	public void testAddRAIRealTypeC() {
		final Img<IntType> imgIntType = TestImgGeneration.intArray(true, 10, 10);
		int originalVal = imgIntType.cursor().next().get();
		var inType = new Nil<RandomAccessibleInterval<IntType>>() {};
		var outType = new Nil<RandomAccessibleInterval<IntType>>() {};

		Computers.Arity2<RandomAccessibleInterval<IntType>, IntType, RandomAccessibleInterval<IntType>> computer =
			ops.op("math.add").inType(inType, Nil.of(IntType.class)).outType(outType)
				.computer();

		final RandomAccessibleInterval<IntType> result = TestImgGeneration.intArray(
			true, 10, 10);
		computer.compute(imgIntType, I, result);

		assertEquals((originalVal + I.get()), result.getAt(0, 0).get());
	}

	@Test
	public void testAddRealTypeF() {
		final Img<IntType> imgIntType = TestImgGeneration.intArray(true, 10, 10);
		int originalVal = imgIntType.cursor().next().get();

		var outType = new Nil<Img<IntType>>() {};
		Img<IntType> result = ops.op("math.add").input(imgIntType, I).outType(
			outType).apply();
		validateResult(result, originalVal);
	}

	@Test
	public void testAddRealTypeC() {
		final Img<IntType> imgIntType = TestImgGeneration.intArray(true, 10, 10);
		int originalVal = imgIntType.cursor().next().get();

		final Img<IntType> out = TestImgGeneration.intArray(false, 10, 10);
		ops.op("math.add").input(imgIntType, I).output(out).compute();
		validateResult(out, originalVal);
	}

	private void validateResult(Img<IntType> result, int originalVal) {
		assertEquals(result.cursor().next().get(), originalVal + I.get());
	}
}
