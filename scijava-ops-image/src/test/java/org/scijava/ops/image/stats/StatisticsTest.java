/*
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

package org.scijava.ops.image.stats;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Tests statistics operations using the following general pattern.
 * <ol>
 * <li>Generate a random test image.</li>
 * <li>Get a reference to the raw data pointer.</li>
 * <li>Calculate the statistic by directly using the raw data.</li>
 * <li>Calculate the statistic by calling the op.</li>
 * <li>Assert that the two values are the same.</li>
 * </ol>
 *
 * @author Brian Northan
 */
public class StatisticsTest extends AbstractOpTest {

	double delta = 0.001;

	ArrayImg<FloatType, FloatArray> img;
	float array[];
	long arraySize;

	private Img<UnsignedByteType> randomlyFilledImg;

	@BeforeEach
	public void setUp() {
		// make a random float array image
		img = TestImgGeneration.floatArray(true, 100, 100);

		// get direct access to the float array
		array = img.update(null).getCurrentStorageArray();

		arraySize = 1;
		for (int d = 0; d < img.numDimensions(); d++)
			arraySize *= img.dimension(d);

		randomlyFilledImg = TestImgGeneration.randomlyFilledUnsignedByteWithSeed(
			new long[] { 100, 100 }, 1234567890L);
	}

	@Test
	public void testMinMax() {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;

		// loop through the array calculating min and max
		for (int i = 0; i < arraySize; i++) {
			if (array[i] < min) min = array[i];
			if (array[i] > max) max = array[i];
		}

		Pair<FloatType, FloatType> minMax = ops.op("stats.minMax").input(
			(Iterable<FloatType>) img).outType(new Nil<Pair<FloatType, FloatType>>()
		{}).apply();

		Assertions.assertEquals(min, minMax.getA().get(), 0);
		Assertions.assertEquals(max, minMax.getB().get(), 0);

	}

	@Test
	public void testMeanStd() {
		float sum = 0.0f;

		for (int i = 0; i < arraySize; i++) {

			sum += array[i];
		}

		float variance = 0.0f;

		float mean1 = sum / arraySize;

		// use the mean to calculate the variance
		for (int i = 0; i < arraySize; i++) {
			float temp = array[i] - mean1;
			variance += temp * temp;
		}

		variance = variance / arraySize;
		float std1 = (float) Math.sqrt(variance);

		// calculate mean using ops
		final FloatType mean2 = new FloatType();
		ops.op("stats.mean").input(img).output(mean2).compute();

		// check that the ratio between mean1 and mean2 is 1.0
		Assertions.assertEquals(1.0, mean1 / mean2.getRealFloat(), delta);

		// calculate standard deviation using ops
		final DoubleType std2 = new DoubleType();
		ops.op("stats.stdDev").input(img).output(std2).compute();

		// check that the ratio between std1 and std2 is 1.0
		Assertions.assertEquals(1.0, std1 / std2.getRealFloat(), delta);
	}

	@Test
	public void testMax() {
		final UnsignedByteType max = new UnsignedByteType();
		ops.op("stats.max").input(randomlyFilledImg).output(max).compute();
		Assertions.assertEquals(254d, max.getRealDouble(), 0.00001d, "Max");

		// NB: should work with negative numbers
		final ByteType maxByte = new ByteType();
		ops.op("stats.max").input(ArrayImgs.bytes(new byte[] { -1, -2, -4, -3 }, 2,
			2)).output(maxByte).compute();
		Assertions.assertEquals(-1.0, maxByte.getRealDouble(), 0.0, "Max");
	}

	@Test
	public void testMin() {
		final UnsignedByteType min = new UnsignedByteType();
		ops.op("stats.min").input(randomlyFilledImg).output(min).compute();
		Assertions.assertEquals(0, min.getRealDouble(), 0.00001d, "Min");
	}

	@Test
	public void testMinAdapted() {
		final UnsignedByteType min = ops.op("stats.min") //
				.input(randomlyFilledImg) //
				.outType(UnsignedByteType.class) //
				.apply();
		Assertions.assertEquals(0, min.getRealDouble(), 0.00001d, "Min");
	}

	@Test
	public void testMaxAdapted() {
		final UnsignedByteType max = ops.op("stats.max") //
				.input(randomlyFilledImg) //
				.outType(UnsignedByteType.class) //
				.apply();
		Assertions.assertEquals(254d, max.getRealDouble(), 0.00001d, "Max");
	}

	@Test
	public void testStdDev() {
		final DoubleType stdDev = new DoubleType();
		ops.op("stats.stdDev").input(randomlyFilledImg).output(stdDev).compute();
		Assertions.assertEquals(73.7460374274008, stdDev.getRealDouble(), 0.00001d,
			"StdDev");
	}

	@Test
	public void testSum() {
		final FloatType sum = new FloatType();
		ops.op("stats.sum").input(randomlyFilledImg).output(sum).compute();
		Assertions.assertEquals(1277534.0, sum.getRealDouble(), 0.00001d, "Sum");
	}

	@Test
	public void testVariance() {
		final DoubleType variance = new DoubleType();
		ops.op("stats.variance").input(randomlyFilledImg).output(variance)
			.compute();
		Assertions.assertEquals(5438.4780362436, variance.getRealDouble(), 0.00001d,
			"Variance");
	}

	@Test
	public void testGeometricMean() {
		final DoubleType geoMetricMean = new DoubleType();
		ops.op("stats.geometricMean").input(randomlyFilledImg).output(geoMetricMean)
			.compute();
		Assertions.assertEquals(0, geoMetricMean.getRealDouble(), 0.00001d,
			"Geometric Mean");
	}

	@Test
	public void testHarmonicMean() {
		final DoubleType harmonicMean = new DoubleType();
		ops.op("stats.harmonicMean").input(randomlyFilledImg).output(harmonicMean)
			.compute();
		Assertions.assertEquals(0, harmonicMean.getRealDouble(), 0.00001d,
			"Harmonic Mean");
	}

	@Test
	public void testKurtosis() {
		final DoubleType kurtosis = new DoubleType();
		ops.op("stats.kurtosis").input(randomlyFilledImg).output(kurtosis)
			.compute();
		Assertions.assertEquals(1.794289587623922, kurtosis.getRealDouble(),
			0.00001d, "Kurtosis");
	}

	@Test
	public void testMoment1AboutMean() {
		final DoubleType moment1 = new DoubleType();
		ops.op("stats.moment1AboutMean").input(randomlyFilledImg).output(moment1)
			.compute();
		Assertions.assertEquals(0, moment1.getRealDouble(), 0.00001d,
			"Moment 1 About Mean");
	}

	@Test
	public void testMoment2AboutMean() {
		final DoubleType moment2 = new DoubleType();
		ops.op("stats.moment2AboutMean").input(randomlyFilledImg).output(moment2)
			.compute();
		Assertions.assertEquals(5437.93418843998, moment2.getRealDouble(), 0.00001d,
			"Moment 2 About Mean");
	}

	@Test
	public void testMoment3AboutMean() {
		final DoubleType moment3 = new DoubleType();
		ops.op("stats.moment3AboutMean").input(randomlyFilledImg).output(moment3)
			.compute();
		Assertions.assertEquals(-507.810691261427, moment3.getRealDouble(),
			0.00001d, "Moment 3 About Mean");
	}

	@Test
	public void testMoment4AboutMean() {
		final DoubleType moment4 = new DoubleType();
		ops.op("stats.moment4AboutMean").input(randomlyFilledImg).output(moment4)
			.compute();
		Assertions.assertEquals(53069780.9168701, moment4.getRealDouble(), 0.00001d,
			"Moment 4 About Mean");
	}

	@Test
	public void testPercentile() {
		final DoubleType percentile = new DoubleType();
		ops.op("stats.percentile").input(randomlyFilledImg, 50d).output(percentile)
			.compute();
		Assertions.assertEquals(128d, percentile.getRealDouble(), 0.00001d,
			"50-th Percentile");
	}

	@Test
	public void testQuantile() {
		final DoubleType quantile = new DoubleType();
		UnsignedByteType test = new UnsignedByteType();
		ops.op("stats.quantile").input(randomlyFilledImg, 0.5d).output(quantile)
			.compute();
		Assertions.assertEquals(128d, quantile.getRealDouble(), 0.00001d,
			"0.5-th Quantile");
	}

	@Test
	public void testSkewness() {
		final DoubleType skewness = new DoubleType();
		ops.op("stats.skewness").input(randomlyFilledImg).output(skewness)
			.compute();
		Assertions.assertEquals(-0.0012661517853476312, skewness.getRealDouble(),
			0.00001d, "Skewness");
	}

	@Test
	public void testSumOfInverses() {
		final DoubleType sumOfInverses = new DoubleType();
		ops.op("stats.sumOfInverses").input(randomlyFilledImg, new DoubleType(0))
			.output(sumOfInverses).compute();
		Assertions.assertEquals(236.60641289378648, sumOfInverses.getRealDouble(),
			0.00001d, "Sum Of Inverses");
	}

	@Test
	public void testSumOfLogs() {
		final DoubleType sumOfLogs = new DoubleType();
		ops.op("stats.sumOfLogs").input(randomlyFilledImg).output(sumOfLogs)
			.compute();
		Assertions.assertEquals(Double.NEGATIVE_INFINITY, sumOfLogs.getRealDouble(),
			0.00001d, "Sum Of Logs");
	}

	@Test
	public void testSumOfSquares() {
		final DoubleType sumOfSquares = new DoubleType();
		ops.op("stats.sumOfSquares").input(randomlyFilledImg).output(sumOfSquares)
			.compute();
		Assertions.assertEquals(217588654, sumOfSquares.getRealDouble(), 0.00001d,
			"Sum Of Squares");
	}

}
