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

import java.util.concurrent.ExecutorService;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;
import org.scijava.thread.ThreadService;

/**
 * Test for the binary watershed op.
 * 
 * @author Simon Schmid (University of Konstanz)
 **/
public class WatershedBinarySingleSigmaTest extends AbstractOpTest {

	private static final long SEED = 0x12345678;

	@Test
	public void test() {
		// load test image
		Img<FloatType> watershedTestImg = openFloatImg(WatershedTest.class, "watershed_test_image.png");

		// retrieve an ExecutorService TODO is there a better way to do this?
		ExecutorService es = context.getService(ThreadService.class).getExecutorService();

		// threshold it
		RandomAccessibleInterval<BitType> thresholdedImg = op("create.img").input(watershedTestImg, new BitType())
				.outType(new Nil<RandomAccessibleInterval<BitType>>() {}).apply();
		op("threshold.apply").input(Views.flatIterable(watershedTestImg), new FloatType(1))
				.output(Views.flatIterable(thresholdedImg)).compute();

		// compute inverted distance transform and smooth it with gaussian
		// filtering
		final RandomAccessibleInterval<FloatType> distMap = op("create.img").input(thresholdedImg, new FloatType())
				.outType(new Nil<RandomAccessibleInterval<FloatType>>() {}).apply();
		op("image.distanceTransform").input(thresholdedImg, es).output(distMap).compute();
		final RandomAccessibleInterval<FloatType> invertedDistMap = op("create.img").input(distMap, new FloatType())
				.outType(new Nil<RandomAccessibleInterval<FloatType>>() {}).apply();
		op("image.invert").input(Views.iterable(distMap)).output(Views.iterable(invertedDistMap)).compute();

		Double sigma = 3.0;
		final RandomAccessibleInterval<FloatType> gauss = op("create.img").input(invertedDistMap, new FloatType())
				.outType(new Nil<RandomAccessibleInterval<FloatType>>() {}).apply();
		op("filter.gauss").input(invertedDistMap, es, sigma).output(gauss).compute();

		// compute result
		final ImgLabeling<Integer, IntType> out1 = op("image.watershed")
				.input(thresholdedImg, true, false, sigma, thresholdedImg, es)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		final ImgLabeling<Integer, IntType> expOut1 = op("image.watershed").input(gauss, true, false, thresholdedImg)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		assertResults(expOut1, out1);

		final ImgLabeling<Integer, IntType> out2 = op("image.watershed").input(thresholdedImg, true, false, sigma, es)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		final ImgLabeling<Integer, IntType> expOut2 = op("image.watershed").input(gauss, true, false)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		assertResults(expOut2, out2);

		// compute result
		final ImgLabeling<Integer, IntType> out3 = op("image.watershed")
				.input(thresholdedImg, true, true, sigma, thresholdedImg, es)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		final ImgLabeling<Integer, IntType> expOut3 = op("image.watershed").input(gauss, true, true, thresholdedImg)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		assertResults(expOut3, out3);

		final ImgLabeling<Integer, IntType> out4 = op("image.watershed").input(thresholdedImg, true, true, sigma, es)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		final ImgLabeling<Integer, IntType> expOut4 = op("image.watershed").input(gauss, true, true)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		assertResults(expOut4, out4);

		// compute result
		final ImgLabeling<Integer, IntType> out5 = op("image.watershed")
				.input(thresholdedImg, false, true, sigma, thresholdedImg, es)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		final ImgLabeling<Integer, IntType> expOut5 = op("image.watershed").input(gauss, false, true, thresholdedImg)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		assertResults(expOut5, out5);

		final ImgLabeling<Integer, IntType> out6 = op("image.watershed").input(thresholdedImg, false, true, sigma, es)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		final ImgLabeling<Integer, IntType> expOut6 = op("image.watershed").input(gauss, false, true)
				.outType(new Nil<ImgLabeling<Integer, IntType>>() {}).apply();

		assertResults(expOut6, out6);
	}

	private void assertResults(final ImgLabeling<Integer, IntType> expOut, final ImgLabeling<Integer, IntType> out) {
		Cursor<LabelingType<Integer>> expOutCursor = expOut.cursor();
		RandomAccess<LabelingType<Integer>> raOut = out.randomAccess();

		while (expOutCursor.hasNext()) {
			expOutCursor.fwd();
			raOut.setPosition(expOutCursor);
			assertEquals(expOutCursor.get().size(), raOut.get().size());
			if (expOutCursor.get().size() > 0)
				assertEquals(expOutCursor.get().iterator().next(), raOut.get().iterator().next());
		}

	}
}
