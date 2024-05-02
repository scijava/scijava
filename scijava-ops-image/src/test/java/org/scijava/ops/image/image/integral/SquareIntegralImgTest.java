/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.image.integral;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Stefan Helfrich (University of Konstanz)
 */
public class SquareIntegralImgTest extends AbstractOpTest {

	RandomAccessibleInterval<DoubleType> out;

	/**
	 * Initialize image.
	 *
	 * @throws Exception
	 */
	@BeforeEach
	public void before() throws Exception {
		out = TestImgGeneration.doubleArray(true, new long[] { 3, 3 });
	}

	/**
	 * @see SquareIntegralImg
	 */
	@Test
	public void testSquareIntegralImageCorrectness() {
		ops.op("image.squareIntegral").input(generateKnownByteArrayTestImg())
			.output(out).compute();

		Img<ByteType> bytes = generateKnownSquareIntegralImage();

		IntegralImgTest.testIterableIntervalSimilarity(Views.iterable(bytes), Views
			.iterable(out));
	}

	private Img<ByteType> generateKnownSquareIntegralImage() {
		final long[] dims = new long[] { 3, 3 };
		final byte[] array = new byte[9];

		array[0] = (byte) 16;
		array[1] = (byte) 32;
		array[2] = (byte) 36;

		array[3] = (byte) 32;
		array[4] = (byte) 64;
		array[5] = (byte) 72;

		array[6] = (byte) 36;
		array[7] = (byte) 72;
		array[8] = (byte) 116;

		Img<ByteType> bytes = ArrayImgs.bytes(array, dims);
		return bytes;
	}

	public ArrayImg<ByteType, ByteArray> generateKnownByteArrayTestImg() {
		final long[] dims = new long[] { 3, 3 };
		final byte[] array = new byte[9];

		array[0] = (byte) 4;
		array[1] = (byte) 4;
		array[2] = (byte) 2;

		array[3] = (byte) 4;
		array[4] = (byte) 4;
		array[5] = (byte) 2;

		array[6] = (byte) 2;
		array[7] = (byte) 2;
		array[8] = (byte) 6;

		return ArrayImgs.bytes(array, dims);
	}

}
