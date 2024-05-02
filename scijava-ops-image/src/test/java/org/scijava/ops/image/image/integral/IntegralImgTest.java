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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * @author Stefan Helfrich (University of Konstanz)
 */
public class IntegralImgTest extends AbstractOpTest {

	Img<ByteType> in;
	RandomAccessibleInterval<DoubleType> out1;
	RandomAccessibleInterval<DoubleType> out2;

	/**
	 * Initialize images.
	 *
	 * @throws Exception
	 */
	@BeforeEach
	public void before() throws Exception {
		in = TestImgGeneration.byteArray(true, new long[] { 10, 10 });
		out1 = TestImgGeneration.doubleArray(true, new long[] { 10, 10 });
	}

	/**
	 * @see DefaultIntegralImg
	 */
	@Test
	public void testIntegralImageSimilarity() {
		// should match DefaultIntegralImg
		Computers.Arity1<RandomAccessibleInterval<ByteType>, RandomAccessibleInterval<DoubleType>> defaultOp =
			OpBuilder.matchComputer(ops, "image.integral",
				new Nil<RandomAccessibleInterval<ByteType>>()
				{}, new Nil<RandomAccessibleInterval<DoubleType>>() {});
		defaultOp.compute(in, out1);

		// should match WrappedIntegralImg
		out2 = ops.op("image.integral").input(in).outType(
			new Nil<RandomAccessibleInterval<DoubleType>>()
			{}).apply();

		// Remove 0s from integralImg by shifting its interval by +1
		final long[] min = new long[out2.numDimensions()];
		final long[] max = new long[out2.numDimensions()];

		for (int d = 0; d < out2.numDimensions(); ++d) {
			min[d] = out2.min(d) + 1;
			max[d] = out2.max(d);
		}

		// Define the Interval on the infinite random accessibles
		final FinalInterval interval = new FinalInterval(min, max);

		testIterableIntervalSimilarity(Views.iterable(out1), Views.iterable(Views
			.offsetInterval(out2, interval)));
	}

	public ArrayImg<ByteType, ByteArray> generateKnownByteArrayTestImgLarge() {
		final long[] dims = new long[] { 3, 3 };
		final byte[] array = new byte[9];

		array[0] = (byte) 40;
		array[1] = (byte) 40;
		array[2] = (byte) 20;

		array[3] = (byte) 40;
		array[4] = (byte) 40;
		array[5] = (byte) 20;

		array[6] = (byte) 20;
		array[7] = (byte) 20;
		array[8] = (byte) 100;

		return ArrayImgs.bytes(array, dims);
	}

	/**
	 * Checks if two {@link IterableInterval} have the same content.
	 *
	 * @param ii1
	 * @param ii2
	 */
	public static <T extends RealType<T>, S extends RealType<S>> void
		testIterableIntervalSimilarity(IterableInterval<T> ii1,
			IterableInterval<S> ii2)
	{
		// Test for pixel-wise equality of the results
		Cursor<T> cursor1 = ii1.localizingCursor();
		Cursor<S> cursor2 = ii2.cursor();
		while (cursor1.hasNext() && cursor2.hasNext()) {
			T value1 = cursor1.next();
			S value2 = cursor2.next();

			assertEquals(value1.getRealDouble(), value2.getRealDouble(), 0.00001d);
		}
	}

}
