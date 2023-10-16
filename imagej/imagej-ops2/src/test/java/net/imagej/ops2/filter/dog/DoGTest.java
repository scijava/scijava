/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package net.imagej.ops2.filter.dog;

import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

import net.imagej.ops2.AbstractOpTest;
import net.imagej.testutil.TestImgGeneration;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.dog.DifferenceOfGaussian;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.view.Views;

/**
 * Tests Difference of Gaussians (DoG) implementations.
 * 
 * @author Christian Dietz (University of Konstanz)
 */
public class DoGTest extends AbstractOpTest {

	@Test
	public void dogRAITest() {
		final double[] sigmas1 = new double[] { 1, 1 };
		final double[] sigmas2 = new double[] { 2, 2 };
		final long[] dims = new long[] { 10, 10 };

		final Img<ByteType> in = TestImgGeneration.byteArray(true, dims);
		final Img<ByteType> out1 = TestImgGeneration.byteArray(false, dims);
		final Img<ByteType> out2 = TestImgGeneration.byteArray(false, dims);
		final OutOfBoundsFactory<ByteType, Img<ByteType>> outOfBounds = new OutOfBoundsMirrorFactory<>(Boundary.SINGLE);

		ops.op("filter.DoG").arity4().input(in, sigmas1, sigmas2, outOfBounds).output(out1).compute();

		// test against native imglib2 implementation
		DifferenceOfGaussian.DoG(sigmas1, sigmas2, Views.extendMirrorSingle(in), out2,
				Executors.newFixedThreadPool(10));

		final Cursor<ByteType> out1Cursor = out1.cursor();
		final Cursor<ByteType> out2Cursor = out2.cursor();

		while (out1Cursor.hasNext()) {
			Assertions.assertEquals(out1Cursor.next().getRealDouble(), out2Cursor.next().getRealDouble(), 0);
		}
	}

	@Test
	public void dogRAISingleSigmasTest() {
		final OutOfBoundsFactory<ByteType, Img<ByteType>> outOfBounds = new OutOfBoundsMirrorFactory<>(Boundary.SINGLE);
		final RandomAccessibleInterval<ByteType> res = ops.op("create.img")
				.arity2().input(TestImgGeneration.byteArray(true, new long[] { 10, 10 }), new ByteType())
				.outType(new Nil<RandomAccessibleInterval<ByteType>>() {}).apply();
		ops.op("filter.DoG").arity4().input(TestImgGeneration.byteArray(true, new long[] { 10, 10 }), 1., 2., outOfBounds)
				.output(res).compute();

		Assertions.assertNotNull(res);
	}
}
