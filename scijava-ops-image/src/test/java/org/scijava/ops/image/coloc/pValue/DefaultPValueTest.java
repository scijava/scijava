/*-
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

package org.scijava.ops.image.coloc.pValue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.function.BiFunction;

import org.scijava.ops.image.AbstractColocalisationTest;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultPValue}.
 *
 * @author Ellen T Arena
 */
public class DefaultPValueTest extends AbstractColocalisationTest {

	/*
	 * Tests
	 */
	@Test
	public void testPValuePerfectColoc() {
		double[] array = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		assertColoc(0.0, 1.0, array, 1, 0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testPValueNoColoc() {
		double[] array = { 1.0, 2.0, 3.0, 4.0, 5.0 };
		assertColoc(1.0, 0.0, array, 0, 1, 2, 3, 4, 5);
	}

	@Test
	public void testPValueSomeColoc() {
		double[] array = { 0.25, 0.25, 0.75, 0.75, 0.75 };
		assertColoc(0.6, 0.25, array, 0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
	}

	/**
	 * Function is called once with original images. Thereafter, each call is with
	 * a shuffled version of the first image.
	 *
	 * @param expectedPValue
	 * @param expectedColocValue
	 * @param result
	 */
	private void assertColoc(double expectedPValue, double expectedColocValue,
		double[] expectedColocValuesArray, double... result)
	{
		Img<FloatType> ch1 = ArrayImgs.floats(1); // NB: Images will be ignored.

		// Mock the underlying op.
		final int[] count = { 0 };
		BiFunction<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>, Double> op = //
			(input1, input2) -> {
				double r;
				synchronized (this) {
					r = result[count[0]++];
				}
				return r;
			};

		BiFunction<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>, Double> wrapped =
			ops.bakeLambdaType(op,
				new Nil<BiFunction<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>, Double>>()
				{}.type());

		PValueResult output = new PValueResult();
		ops.op("coloc.pValue").input(ch1, ch1, wrapped, result.length - 1).output(
			output).compute();
		Double actualPValue = output.getPValue();
		Double actualColocValue = output.getColocValue();
		double[] actualColocValuesArray = output.getColocValuesArray();
		assertEquals(expectedPValue, actualPValue, 0.0);
		assertEquals(expectedColocValue, actualColocValue, 0.0);
		Arrays.sort(actualColocValuesArray);
		for (int i = 0; i < expectedColocValuesArray.length; i++) {
			assertEquals(expectedColocValuesArray[i], actualColocValuesArray[i], 0.0);
		}
	}

}
