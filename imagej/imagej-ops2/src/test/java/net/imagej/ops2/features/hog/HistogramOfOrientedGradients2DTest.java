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
package net.imagej.ops2.features.hog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ExecutorService;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.types.Nil;
import org.scijava.thread.ThreadService;

/**
 * The HoG Op is tested by comparing its result with the ground-truth which was
 * created on a certain test image. So this test runs only with this certain
 * test image. The correctness of the ground-truth has been verified by hand.
 * 
 * @author Simon Schmid (University of Konstanz)
 */
public class HistogramOfOrientedGradients2DTest extends AbstractOpTest {

	private static final double EPSILON = 0.00001;

	@Test
	public void test() {
		ExecutorService es = context.getService(ThreadService.class).getExecutorService();

		Img<FloatType> hogTestImg = openFloatImg("HoG2DResult.tif");
		Img<FloatType> hogInputImg = openFloatImg("HoG2DInput.png");

		// use numOrientations = 9 and spanOfNeighborhood = 2 for test
		RandomAccessibleInterval<FloatType> hogOp = op("features.hog").input(hogInputImg, 9, 2, es)
				.outType(new Nil<RandomAccessibleInterval<FloatType>>() {}).apply();

		RandomAccess<FloatType> raOp = hogOp.randomAccess();
		RandomAccess<FloatType> raTest = hogTestImg.randomAccess();

		// check dimensions
		assertEquals(hogTestImg.numDimensions(), hogOp.numDimensions());
		assertEquals(hogTestImg.dimension(0), hogOp.dimension(0));
		assertEquals(hogTestImg.dimension(1), hogOp.dimension(1));
		assertEquals(hogTestImg.dimension(2), hogOp.dimension(2));

		// check pixel values
		for (int i = 0; i < hogTestImg.dimension(0); i++) {
			for (int j = 0; j < hogTestImg.dimension(1); j++) {
				for (int k = 0; k < hogTestImg.dimension(2); k++) {
					raTest.setPosition(new long[] { i, j, k });
					raOp.setPosition(new long[] { i, j, k });
					assertEquals(raTest.get().getRealFloat(), raOp.get().getRealFloat(),
						EPSILON, "i=" + i + ", j=" + j + ", k=" + k);
				}
			}
		}
	}
}
