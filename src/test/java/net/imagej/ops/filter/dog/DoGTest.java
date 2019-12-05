/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops.filter.dog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.dog.DifferenceOfGaussian;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.view.Views;

import org.junit.Test;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.ops.types.Nil;
import org.scijava.thread.ThreadService;

/**
 * Tests Difference of Gaussians (DoG) implementations.
 * 
 * @author Christian Dietz (University of Konstanz)
 */
public class DoGTest extends AbstractOpTest {

	@Test
	public void dogRAITest() {
		ExecutorService es = context.getService(ThreadService.class).getExecutorService();

		final double[] sigmas1 = new double[] { 1, 1 };
		final double[] sigmas2 = new double[] { 2, 2 };
		final long[] dims = new long[] { 10, 10 };

		final Img<ByteType> in = generateByteArrayTestImg(true, dims);
		final Img<ByteType> out1 = generateByteArrayTestImg(false, dims);
		final Img<ByteType> out2 = generateByteArrayTestImg(false, dims);
		final OutOfBoundsFactory<ByteType, Img<ByteType>> outOfBounds = new OutOfBoundsMirrorFactory<>(Boundary.SINGLE);

		op("filter.DoG").input(in, sigmas1, sigmas2, outOfBounds, es).output(out1).compute();

		// test against native imglib2 implementation
		DifferenceOfGaussian.DoG(sigmas1, sigmas2, Views.extendMirrorSingle(in), out2,
				Executors.newFixedThreadPool(10));

		final Cursor<ByteType> out1Cursor = out1.cursor();
		final Cursor<ByteType> out2Cursor = out2.cursor();

		while (out1Cursor.hasNext()) {
			org.junit.Assert.assertEquals(out1Cursor.next().getRealDouble(), out2Cursor.next().getRealDouble(), 0);
		}
	}

	@Test
	public void dogRAISingleSigmasTest() {
		ExecutorService es = context.getService(ThreadService.class).getExecutorService();
		final OutOfBoundsFactory<ByteType, Img<ByteType>> outOfBounds = new OutOfBoundsMirrorFactory<>(Boundary.SINGLE);
		final RandomAccessibleInterval<ByteType> res = op("filter.DoG")
				.input(generateByteArrayTestImg(true, new long[] { 10, 10 }), 1., 2., outOfBounds, es)
				.outType(new Nil<RandomAccessibleInterval<ByteType>>() {}).apply();

		org.junit.Assert.assertNotNull(res);
	}
}
