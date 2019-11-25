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

package net.imagej.ops.stats;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

	@Override
	@Before
	public void setUp() {
		super.setUp();

		// make a random float array image
		img = generateFloatArrayTestImg(true, 100, 100);

		// get direct access to the float array
		array = img.update(null).getCurrentStorageArray();

		arraySize = 1;
		for (int d = 0; d < img.numDimensions(); d++)
			arraySize *= img.dimension(d);

		randomlyFilledImg = generateRandomlyFilledUnsignedByteTestImgWithSeed(
			new long[] { 100, 100 }, 1234567890L);
	}
	
	@Test
	public void MinMaxTest() {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		
		// loop through the array calculating min and max
		for (int i = 0; i < arraySize; i++) {
			if (array[i] < min) min = array[i];
			if (array[i] > max) max = array[i];
		}
		
		Pair<FloatType, FloatType> minMax = (Pair<FloatType, FloatType>) new OpBuilder(ops, "stats.minMax").input((Iterable<FloatType>) img).apply();
		
		Assert.assertEquals(min, minMax.getA().get(), 0);
		Assert.assertEquals(max, minMax.getB().get(), 0);
		
	}

	@Test
	public void MeanStdTest() {
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
		new OpBuilder(ops, "stats.mean").input(img, mean2).apply();

		// check that the ratio between mean1 and mean2 is 1.0
		Assert.assertEquals(1.0, mean1 / mean2.getRealFloat(), delta);

		// calculate standard deviation using ops
		final DoubleType std2 = new DoubleType();
		new OpBuilder(ops, "stats.stdDev").input(img, std2).apply();

		// check that the ratio between std1 and std2 is 1.0
		Assert.assertEquals(1.0, std1 / std2.getRealFloat(), delta);
	}

	@Test
	public void testMax() {
		final UnsignedByteType max = new UnsignedByteType();
		new OpBuilder(ops, "stats.max").input(randomlyFilledImg, max).apply();
		Assert.assertEquals("Max", 254d, max.getRealDouble(), 0.00001d);

		// NB: should work with negative numbers
		final ByteType maxByte = new ByteType();
		new OpBuilder(ops, "stats.max").input(ArrayImgs.bytes(new byte[] { -1, -2, -4, -3 }, 2, 2), maxByte).apply();
		Assert.assertEquals("Max", -1.0, maxByte.getRealDouble(), 0.0);
	}


	@Test
	public void testMedian() {
		final DoubleType median = new DoubleType();
		new OpBuilder(ops, "stats.median").input(randomlyFilledImg, median).apply();
		Assert.assertEquals("Median", 128d, median.getRealDouble(), 0.00001d);
	}

	@Test
	public void testMin() {
		final UnsignedByteType min = new UnsignedByteType();
		new OpBuilder(ops, "stats.min").input(randomlyFilledImg, min).apply();
		Assert.assertEquals("Min", 0, min.getRealDouble(), 0.00001d);
	}

	@Test
	public void testStdDev() {
		final DoubleType stdDev = new DoubleType();
		new OpBuilder(ops, "stats.stdDev").input(randomlyFilledImg, stdDev).apply();
		Assert.assertEquals("StdDev", 73.7460374274008, stdDev.getRealDouble(), 0.00001d);
	}

	@Test
	public void testSum() {
		final DoubleType sum = new DoubleType();
		new OpBuilder(ops, "stats.sum").input(randomlyFilledImg, sum).apply();
		Assert.assertEquals("Sum", 1277534.0, sum.getRealDouble(), 0.00001d);
	}

	@Test
	public void testVariance() {
		final DoubleType variance = new DoubleType();
		new OpBuilder(ops, "stats.variance").input(randomlyFilledImg, variance).apply();
		Assert.assertEquals("Variance", 5438.4780362436, variance.getRealDouble(), 0.00001d);
	}

	@Test
	public void testGeometricMean() {
		final DoubleType geoMetricMean = new DoubleType();
		new OpBuilder(ops, "stats.geometricMean").input(randomlyFilledImg, geoMetricMean).apply();
		Assert.assertEquals("Geometric Mean", 0, geoMetricMean.getRealDouble(),
			0.00001d);
	}

	@Test
	public void testHarmonicMean() {
		final DoubleType harmonicMean = new DoubleType();
		new OpBuilder(ops, "stats.harmonicMean").input(randomlyFilledImg, harmonicMean).apply();
		Assert.assertEquals("Harmonic Mean", 0, harmonicMean.getRealDouble(), 0.00001d);
	}

	@Test
	public void testKurtosis() {
		final DoubleType kurtosis = new DoubleType();
		new OpBuilder(ops, "stats.kurtosis").input(randomlyFilledImg, kurtosis).apply();
		Assert.assertEquals("Kurtosis", 1.794289587623922, kurtosis.getRealDouble(), 0.00001d);
	}

	@Test
	public void testMoment1AboutMean() {
		final DoubleType moment1 = new DoubleType();
		new OpBuilder(ops, "stats.moment1AboutMean").input(randomlyFilledImg, moment1).apply();
		Assert.assertEquals("Moment 1 About Mean", 0, moment1.getRealDouble(), 0.00001d);
	}

	@Test
	public void testMoment2AboutMean() {
		final DoubleType moment2 = new DoubleType();
		new OpBuilder(ops, "stats.moment2AboutMean").input(randomlyFilledImg, moment2).apply();
		Assert.assertEquals("Moment 2 About Mean", 5437.93418843998, moment2.getRealDouble(), 0.00001d);
	}

	@Test
	public void testMoment3AboutMean() {
		final DoubleType moment3 = new DoubleType();
		new OpBuilder(ops, "stats.moment3AboutMean").input(randomlyFilledImg, moment3).apply();
		Assert.assertEquals("Moment 3 About Mean", -507.810691261427, moment3.getRealDouble(), 0.00001d);
	}

	@Test
	public void testMoment4AboutMean() {
		final DoubleType moment4 = new DoubleType();
		new OpBuilder(ops, "stats.moment4AboutMean").input(randomlyFilledImg, moment4).apply();
		Assert.assertEquals("Moment 4 About Mean", 53069780.9168701, moment4.getRealDouble(), 0.00001d);
	}

	@Test
	public void testPercentile() {
		final DoubleType percentile = new DoubleType();
		new OpBuilder(ops, "stats.percentile").input(randomlyFilledImg, 50d, percentile).apply();
		Assert.assertEquals("50-th Percentile", 128d, percentile.getRealDouble(), 0.00001d);
	}

	@Test
	public void testQuantile() {
		final DoubleType quantile = new DoubleType();
		new OpBuilder(ops, "stats.quantile").input(randomlyFilledImg, 0.5d, quantile).apply();
		Assert.assertEquals("0.5-th Quantile", 128d, quantile.getRealDouble(), 0.00001d);
	}

	@Test
	public void testSkewness() {
		final DoubleType skewness = new DoubleType();
		new OpBuilder(ops, "stats.skewness").input(randomlyFilledImg, skewness).apply();
		Assert.assertEquals("Skewness", -0.0012661517853476312, skewness.getRealDouble(), 0.00001d);
	}

	@Test
	public void testSumOfInverses() {
		final DoubleType sumOfInverses = new DoubleType();
		new OpBuilder(ops, "stats.sumOfInverses").input(randomlyFilledImg, sumOfInverses).apply();
		Assert.assertEquals("Sum Of Inverses", Double.POSITIVE_INFINITY, sumOfInverses.getRealDouble(), 0.00001d);
	}

	@Test
	public void testSumOfLogs() {
		final DoubleType sumOfLogs = new DoubleType();
		new OpBuilder(ops, "stats.sumOfLogs").input(randomlyFilledImg, sumOfLogs).apply();
		Assert.assertEquals("Sum Of Logs", Double.NEGATIVE_INFINITY, sumOfLogs.getRealDouble(), 0.00001d);
	}

	@Test
	public void testSumOfSquares() {
		final DoubleType sumOfSquares = new DoubleType();
		new OpBuilder(ops, "stats.sumOfSquares").input(randomlyFilledImg, sumOfSquares).apply();
		Assert.assertEquals("Sum Of Squares", 217588654, sumOfSquares.getRealDouble(), 0.00001d);
	}

}
