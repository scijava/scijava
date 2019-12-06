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

package net.imagej.ops.filter.gauss;

import java.util.concurrent.ExecutorService;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.junit.Test;
import org.scijava.ops.types.Nil;
import org.scijava.thread.ThreadService;

/**
 * Tests Gaussian convolution.
 *
 * @author Martin Horn (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 */
public class GaussTest extends AbstractOpTest {

	/** Tests the Gaussian. */
	@Test
	public void gaussRegressionTest() {

		// retrieve an ExecutorService TODO is there a better way to do this?
		ExecutorService es = createContext().getService(ThreadService.class).getExecutorService();

		final Img<ByteType> in = generateByteArrayTestImg(true, new long[] { 10, 10 });
		final Img<ByteType> out1 = op("create.img").input(in, Util.getTypeFromInterval(in))
				.outType(new Nil<Img<ByteType>>() {}).apply();
		final double sigma = 5;
		final Img<ByteType> out2 = op("create.img").input(in, Util.getTypeFromInterval(in))
				.outType(new Nil<Img<ByteType>>() {}).apply();

		op("filter.gauss").input(in, es, sigma).output(out1).compute();
		try {
			Gauss3.gauss(sigma, Views.extendMirrorSingle(in), out2);
		} catch (IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}

		// compare outputs
		final Cursor<ByteType> c1 = out1.cursor();
		final Cursor<ByteType> c2 = out2.cursor();

		while (c1.hasNext()) {
			org.junit.Assert.assertEquals(c1.next().getRealDouble(), c2.next().getRealDouble(), 0);
		}
	}
	//
	// /** Tests the Gaussian matching. */
	// @Test
	// public void gaussMatchingTest() {
	//
	// Gauss defaultGaussRAI = ops.op(Ops.Filter.Gauss.class, ArrayImgs.bytes(1, 2),
	// new double[] {1, 2});
	// assertTrue(defaultGaussRAI instanceof DefaultGaussRAI);
	//
	// defaultGaussRAI = ops.op(
	// Ops.Filter.Gauss.class,
	// ArrayImgs.bytes(1, 2),
	// ArrayImgs.bytes(1, 2),
	// new double[] {1, 2});
	// assertTrue(defaultGaussRAI instanceof DefaultGaussRAI);
	//
	// Gauss defaultGaussRA = ops.op(
	// Ops.Filter.Gauss.class,
	// ArrayImgs.bytes(1, 2),
	// Views.extendMirrorSingle(ArrayImgs.bytes(1, 2)),
	// new double[] {1, 2});
	// assertTrue(defaultGaussRA instanceof DefaultGaussRA);
	// }
}
