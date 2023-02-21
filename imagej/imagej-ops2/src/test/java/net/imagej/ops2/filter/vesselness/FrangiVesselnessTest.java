/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

package net.imagej.ops2.filter.vesselness;

import io.scif.img.IO;

import java.net.URL;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the Frangi Vesselness operation.
 * 
 * @author Gabe Selzer
 */
public class FrangiVesselnessTest extends AbstractOpTest {

	@Test
	public void regressionTest() {

		// load in input image and expected output image.
		Img<DoubleType> inputImg = regressionInput();
		Img<FloatType> expectedOutput = regressionOutput();

		// create output image
		final long[] dims = new long[inputImg.numDimensions()];
		inputImg.dimensions(dims);
		final Img<FloatType> actualOutput = ArrayImgs.floats(dims);

		// scale over which the filter operates (sensitivity)
		final int scale = 1;

		// physical spacing between data points (1,1 since I got it from the
		// computer)
		final double[] spacing = { 1, 1 };

		// run the op
		ops.op("filter.frangiVesselness").input(inputImg, scale, spacing).output(actualOutput).compute();

		// compare the output image data to that stored in the file.
		final Cursor<FloatType> cursor = Views.iterable(actualOutput).localizingCursor();
		final RandomAccess<FloatType> actualRA = actualOutput.randomAccess();
		final RandomAccess<FloatType> expectedRA = expectedOutput.randomAccess();

		while (cursor.hasNext()) {
			cursor.fwd();
			actualRA.setPosition(cursor);
			expectedRA.setPosition(cursor);
			assertEquals(expectedRA.get().get(), actualRA.get().get(), 0);
		}
	}

	private Img<DoubleType> regressionInput() {
		// create img
		Img<DoubleType> inputImg = ArrayImgs.doubles(8, 8);
		setVesselPosValues(inputImg, new DoubleType(1.), new DoubleType(0.));
		return inputImg;
	}

	private Img<FloatType> regressionOutput() {
		Img<FloatType> expectedOutput = ArrayImgs.floats(8, 8);
		setVesselPosValues(expectedOutput, new FloatType(0.0031375182f), new FloatType(0));
		return expectedOutput;
	}

	private <T extends RealType<T>> void setVesselPosValues(Img<T> img, T vesselVal, T otherVal) {
		Cursor<T> inputCursor = img.localizingCursor();
		// for each position, set to one if column at vesselPos, 0 otherwise
		int vesselPos = 4;
		while(inputCursor.hasNext()) {
			inputCursor.fwd();
			double[] pos = inputCursor.positionAsDoubleArray();
			inputCursor.get().set(pos[1] == vesselPos ? vesselVal : otherVal);
		}

	}

}
