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
package net.imagej.ops2.image.watershed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Test for the watershed op.
 * 
 * @author Simon Schmid (University of Konstanz)
 **/
public class WatershedTest extends AbstractOpTest {

	@Test
	public void test() {
		// load test image
		Img<FloatType> watershedTestImg = openRelativeFloatImg(WatershedTest.class, "watershed_test_image.png");

		// retrieve an ExecutorService TODO is there a better way to do this?
		ExecutorService es = threads.getExecutorService();

		// threshold it
		RandomAccessibleInterval<BitType> thresholdedImg = ops.op("create.img")
				.input(watershedTestImg, new BitType()).outType(new Nil<RandomAccessibleInterval<BitType>>() {})
				.apply();
		ops.op("threshold.apply").input(Views.flatIterable(watershedTestImg), new FloatType(1))
				.output(Views.flatIterable(thresholdedImg)).compute();

		// compute inverted distance transform and smooth it with gaussian
		// filtering

		final RandomAccessibleInterval<FloatType> distMap = ops.op("create.img")
			.input(thresholdedImg, new FloatType()).outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply();
		ops.op("image.distanceTransform").input(thresholdedImg, es).output(distMap)
			.compute();
		final RandomAccessibleInterval<FloatType> invertedDistMap = ops.op(
			"create.img").input(distMap, new FloatType()).outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply();
		ops.op("image.invert").input(distMap).output(invertedDistMap).compute();
		final RandomAccessibleInterval<FloatType> gauss = ops.op("create.img")
			.input(invertedDistMap, new FloatType()).outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply();
		ops.op("filter.gauss").input(invertedDistMap, es, new double[] { 3, 3 })
			.output(gauss).compute();

		testWithoutMask(gauss);

		testWithMask(gauss);
	}

	private void testWithoutMask(final RandomAccessibleInterval<FloatType> in) {
		// create mask which is 1 everywhere
		long[] dims = new long[in.numDimensions()];
		in.dimensions(dims);
		Img<BitType> mask = ArrayImgs.bits(dims);
		for (BitType b : mask) {
			b.setOne();
		}

		/*
		 * use 8-connected neighborhood
		 */
		// compute result without watersheds
		ImgLabeling<Integer, IntType> out = ops.op("image.watershed")
				.input(in, true, false).outType(new Nil<ImgLabeling<Integer, IntType>>(){}).apply();

		assertResults(in, out, mask, true, false, false);

		// compute result with watersheds
		ImgLabeling<Integer, IntType> out2 = ops.op("image.watershed")
				.input(in, true, true).outType(new Nil<ImgLabeling<Integer, IntType>>(){}).apply();

		assertResults(in, out2, mask, true, true, false);

		/*
		 * use 4-connected neighborhood
		 */
		// compute result without watersheds
		ImgLabeling<Integer, IntType> out3 = ops.op("image.watershed")
				.input(in, false, false).outType(new Nil<ImgLabeling<Integer, IntType>>(){}).apply();

		assertResults(in, out3, mask, false, false, false);

		// compute result with watersheds
		ImgLabeling<Integer, IntType> out4 = ops.op("image.watershed")
				.input(in, false, true).outType(new Nil<ImgLabeling<Integer, IntType>>(){}).apply();

		assertResults(in, out4, mask, false, true, false);
	}

	private void testWithMask(final RandomAccessibleInterval<FloatType> in) {
		// create mask which is 1 everywhere
		long[] dims = new long[in.numDimensions()];
		in.dimensions(dims);
		Img<BitType> mask = ArrayImgs.bits(dims);
		RandomAccess<BitType> raMask = mask.randomAccess();
		for (BitType b : mask) {
			b.setZero();
		}
		for (int x = 0; x < dims[0] / 2; x++) {
			for (int y = 0; y < dims[1] / 2; y++) {
				raMask.setPosition(new int[] { x, y });
				raMask.get().setOne();
			}
		}

		/*
		 * use 8-connected neighborhood
		 */
		// compute result without watersheds
		ImgLabeling<Integer, IntType> out = ops.op("image.watershed")
				.input(in, true, false, mask).outType(new Nil<ImgLabeling<Integer, IntType>>(){}).apply();

		assertResults(in, out, mask, true, false, true);

		// compute result with watersheds
		ImgLabeling<Integer, IntType> out2 = ops.op("image.watershed")
				.input(in, true, true, mask).outType(new Nil<ImgLabeling<Integer, IntType>>(){}).apply();

		assertResults(in, out2, mask, true, true, true);

		/*
		 * use 4-connected neighborhood
		 */
		// compute result without watersheds
		ImgLabeling<Integer, IntType> out3 = ops.op("image.watershed")
				.input(in, false, false, mask).outType(new Nil<ImgLabeling<Integer, IntType>>(){}).apply();

		assertResults(in, out3, mask, false, false, true);

		// compute result with watersheds
		ImgLabeling<Integer, IntType> out4 = ops.op("image.watershed")
				.input(in, false, true, mask).outType(new Nil<ImgLabeling<Integer, IntType>>(){}).apply();

		assertResults(in, out4, mask, false, true, true);
	}

	private void assertResults(final RandomAccessibleInterval<FloatType> in, final ImgLabeling<Integer, IntType> out,
			final RandomAccessibleInterval<BitType> mask, final boolean useEighConnect, final boolean withWatersheds,
			final boolean smallMask) {

		final Cursor<LabelingType<Integer>> curOut = out.cursor();
		final RandomAccess<BitType> raMask = mask.randomAccess();
		while (curOut.hasNext()) {
			curOut.fwd();
			raMask.setPosition(curOut);
			if (raMask.get().get()) {
				assertEquals(true, curOut.get().size() == 0 || curOut.get().size() == 1);
			} else {
				assertEquals(true, curOut.get().isEmpty());
			}
		}
		// Sample the output image based on the mask
		IterableRegion<BitType> regions = Regions.iterable(mask);

		// count labels
		Set<Integer> labelSet = new HashSet<>();
		for (LabelingType<Integer> pixel : Regions.sample(
			(IterableInterval<Void>) regions, out))
		{
			labelSet.addAll(pixel);
		}

		// assert equals
		assertEquals(in.numDimensions(), out.numDimensions());
		assertEquals(in.dimension(0), out.dimension(0));
		assertEquals(in.dimension(1), out.dimension(1));
		if (smallMask) {
			assertEquals(3 + (withWatersheds ? 1 : 0), labelSet.size());
		} else {
			assertEquals(10 + (withWatersheds ? 1 : 0), labelSet.size());
		}
	}

}
