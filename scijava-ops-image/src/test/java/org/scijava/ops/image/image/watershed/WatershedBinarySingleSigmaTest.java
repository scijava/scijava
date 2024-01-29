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

package org.scijava.ops.image.image.watershed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

import org.scijava.ops.image.AbstractOpTest;
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
		Img<FloatType> watershedTestImg = openRelativeFloatImg(WatershedTest.class,
			"watershed_test_image.png");

		// threshold it
		RandomAccessibleInterval<BitType> thresholdedImg = ops.op("create.img")
			.arity2().input(watershedTestImg, new BitType()).outType(
				new Nil<RandomAccessibleInterval<BitType>>()
				{}).apply();
		ops.op("threshold.apply").arity2().input(Views.flatIterable(
			watershedTestImg), new FloatType(1)).output(Views.flatIterable(
				thresholdedImg)).compute();

		// compute inverted distance transform and smooth it with gaussian
		// filtering
		final RandomAccessibleInterval<FloatType> distMap = ops.op("create.img")
			.arity2().input(thresholdedImg, new FloatType()).outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply();
		ops.op("image.distanceTransform").arity1().input(thresholdedImg).output(
			distMap).compute();
		final RandomAccessibleInterval<FloatType> invertedDistMap = ops.op(
			"create.img").arity2().input(distMap, new FloatType()).outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply();
		ops.op("image.invert").arity1().input(distMap).output(invertedDistMap)
			.compute();

		Double sigma = 3.0;
		final RandomAccessibleInterval<FloatType> gauss = ops.op("create.img")
			.arity2().input(invertedDistMap, new FloatType()).outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply();
		ops.op("filter.gauss").arity2().input(invertedDistMap, sigma).output(gauss)
			.compute();

		// compute result
		final ImgLabeling<Integer, IntType> out1 = ops.op("image.watershed")
			.arity5().input(thresholdedImg, true, false, sigma, thresholdedImg)
			.outType(new Nil<ImgLabeling<Integer, IntType>>()
			{}).apply();

		final ImgLabeling<Integer, IntType> expOut1 = ops.op("image.watershed")
			.arity4().input(gauss, true, false, thresholdedImg).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();

		assertResults(expOut1, out1);

		final ImgLabeling<Integer, IntType> out2 = ops.op("image.watershed")
			.arity4().input(thresholdedImg, true, false, sigma).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();

		final ImgLabeling<Integer, IntType> expOut2 = ops.op("image.watershed")
			.arity3().input(gauss, true, false).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();

		assertResults(expOut2, out2);

		// compute result
		final ImgLabeling<Integer, IntType> out3 = ops.op("image.watershed")
			.arity5().input(thresholdedImg, true, true, sigma, thresholdedImg)
			.outType(new Nil<ImgLabeling<Integer, IntType>>()
			{}).apply();

		final ImgLabeling<Integer, IntType> expOut3 = ops.op("image.watershed")
			.arity4().input(gauss, true, true, thresholdedImg).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();

		assertResults(expOut3, out3);

		final ImgLabeling<Integer, IntType> out4 = ops.op("image.watershed")
			.arity4().input(thresholdedImg, true, true, sigma).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();

		final ImgLabeling<Integer, IntType> expOut4 = ops.op("image.watershed")
			.arity3().input(gauss, true, true).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();

		assertResults(expOut4, out4);

		// compute result
		final ImgLabeling<Integer, IntType> out5 = ops.op("image.watershed")
			.arity5().input(thresholdedImg, false, true, sigma, thresholdedImg)
			.outType(new Nil<ImgLabeling<Integer, IntType>>()
			{}).apply();

		final ImgLabeling<Integer, IntType> expOut5 = ops.op("image.watershed")
			.arity4().input(gauss, false, true, thresholdedImg).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();

		assertResults(expOut5, out5);

		final ImgLabeling<Integer, IntType> out6 = ops.op("image.watershed")
			.arity4().input(thresholdedImg, false, true, sigma).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();

		final ImgLabeling<Integer, IntType> expOut6 = ops.op("image.watershed")
			.arity3().input(gauss, false, true).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();

		assertResults(expOut6, out6);
	}

	private void assertResults(final ImgLabeling<Integer, IntType> expOut,
		final ImgLabeling<Integer, IntType> out)
	{
		Cursor<LabelingType<Integer>> expOutCursor = expOut.cursor();
		RandomAccess<LabelingType<Integer>> raOut = out.randomAccess();

		while (expOutCursor.hasNext()) {
			expOutCursor.fwd();
			raOut.setPosition(expOutCursor);
			assertEquals(expOutCursor.get().size(), raOut.get().size());
			if (expOutCursor.get().size() > 0) assertEquals(expOutCursor.get()
				.iterator().next(), raOut.get().iterator().next());
		}

	}
}
