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

package org.scijava.ops.image.coloc.pearsons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;

import org.scijava.ops.image.AbstractColocalisationTest;
import org.scijava.ops.image.coloc.pValue.PValueResult;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultPearsons}.
 *
 * @author Ellen T Arena
 */
public class DefaultPearsonsTest extends AbstractColocalisationTest {

	/**
	 * Tests if the fast implementation of Pearson's correlation with two zero
	 * correlated images produce a Pearson's R value of about zero.
	 */
	@Test
	public void fastPearsonsZeroCorrTest() {
		double result = ops.op("coloc.pearsons").input(getZeroCorrelationImageCh1(),
			getZeroCorrelationImageCh2()).outType(Double.class).apply();
		assertEquals(0.0, result, 0.05);
	}

	/**
	 * Tests if the fast implementation of Pearson's correlation with two positive
	 * correlated images produce a Pearson's R value of about 0.75.
	 */
	@Test
	public void fastPearsonsPositiveCorrTest() {
		double result = ops.op("coloc.pearsons").input(
			getPositiveCorrelationImageCh1(), getPositiveCorrelationImageCh2())
			.outType(Double.class).apply();
		assertEquals(0.75, result, 0.01);
	}

	/**
	 * Tests Pearson's correlation stays close to zero for image pairs with the
	 * same mean and spread of randomized pixel values around that mean.
	 */
	@Test
	public void differentMeansTest() {
		final double initialMean = 0.2;
		final double spread = 0.1;
		final double[] sigma = new double[] { 3.0, 3.0 };

		for (double mean = initialMean; mean < 1; mean += spread) {
			RandomAccessibleInterval<FloatType> ch1 = produceMeanBasedNoiseImage(
				new FloatType(), 512, 512, mean, spread, sigma, 0x01234567);
			RandomAccessibleInterval<FloatType> ch2 = produceMeanBasedNoiseImage(
				new FloatType(), 512, 512, mean, spread, sigma, 0x98765432);
			double resultFast = ops.op("coloc.pearsons").input(ch1, ch2).outType(
				Double.class).apply();
			assertEquals(0.0, resultFast, 0.1);

			/* If the means are the same, it causes a numerical problem in the classic implementation of Pearson's
			 * double resultClassic = PearsonsCorrelation.classicPearsons(cursor, mean, mean);
			 * assertTrue(Math.abs(resultClassic) < 0.1);
			 */
		}
	}

	@Test
	public void testPValue() {
		final double mean = 0.2;
		final double spread = 0.1;
		final double[] sigma = new double[] { 3.0, 3.0 };
		Img<FloatType> ch1 = AbstractColocalisationTest.produceMeanBasedNoiseImage(
			new FloatType(), 24, 24, mean, spread, sigma, 0x01234567);
		Img<FloatType> ch2 = AbstractColocalisationTest.produceMeanBasedNoiseImage(
			new FloatType(), 24, 24, mean, spread, sigma, 0x98765432);
		BiFunction<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>, Double> op =
			OpBuilder.matchFunction(ops, "coloc.pearsons",
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}, new Nil<RandomAccessibleInterval<FloatType>>() {},
				new Nil<Double>()
				{});
		PValueResult value = new PValueResult();
		ops.op("coloc.pValue").input(ch1, ch2, op).output(value).compute();
		assertEquals(0.66, value.getPValue(), 0.0);
	}

}
