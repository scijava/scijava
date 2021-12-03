/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
 * Wisconsin-Madison and University of Konstanz.
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

package net.imagej.ops2.threshold;

import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link ComputeThresholdHistogram} implementations.
 *
 * @author Brian Northan
 * @author Curtis Rueden
 */
public class ComputeThresholdTest extends AbstractThresholdTest {

	/** Tests {@link net.imagej.ops2.threshold.huang.ComputeHuangThreshold}. */
	@Test
	public void testHuang() {
		testComputeThresholdOp(36874, "threshold.huang");
	}

	/** Tests {@link net.imagej.ops2.threshold.ij1.ComputeIJ1Threshold}. */
	@Test
	public void testIJ1() {
		testComputeThresholdOp(31836, "threshold.ij1");
	}

	/**
	 * Tests
	 * {@link net.imagej.ops2.threshold.intermodes.ComputeIntermodesThreshold}.
	 */
	@Test
	public void testIntermodes() {
		testComputeThresholdOp(34859, "threshold.intermodes");
	}

	/** Tests {@link net.imagej.ops2.threshold.isoData.ComputeIsoDataThreshold}. */
	@Test
	public void testIsoData() {
		testComputeThresholdOp(33095, "threshold.isoData");
	}

	/** Tests {@link net.imagej.ops2.threshold.li.ComputeLiThreshold}. */
	@Test
	public void testLi() {
		testComputeThresholdOp(26798, "threshold.li");
	}

	/**
	 * Tests
	 * {@link net.imagej.ops2.threshold.maxEntropy.ComputeMaxEntropyThreshold}.
	 */
	@Test
	public void testMaxEntropy() {
		testComputeThresholdOp(28309, "threshold.maxEntropy");
	}

	/**
	 * Tests
	 * {@link net.imagej.ops2.threshold.maxLikelihood.ComputeMaxLikelihoodThreshold}.
	 */
	@Test
	public void testMaxLikelihood() {
		testComputeThresholdOp(46698, "threshold.maxLikelihood");
	}

	/** Tests {@link net.imagej.ops2.threshold.mean.ComputeMeanThreshold}. */
	@Test
	public void testMean() {
		testComputeThresholdOp(32591, "threshold.mean");
	}

	/**
	 * Tests {@link net.imagej.ops2.threshold.minError.ComputeMinErrorThreshold}.
	 */
	@Test
	public void testMinError() {
		testComputeThresholdOp(32843, "threshold.minError");
	}

	/** Tests {@link net.imagej.ops2.threshold.minimum.ComputeMinimumThreshold}. */
	@Test
	public void testMinimum() {
		testComputeThresholdOp(44935, "threshold.minimum");
	}

	/** Tests {@link net.imagej.ops2.threshold.moments.ComputeMomentsThreshold}. */
	@Test
	public void testMoments() {
		testComputeThresholdOp(34355, "threshold.moments");
	}

	/** Tests {@link net.imagej.ops2.threshold.otsu.ComputeOtsuThreshold}. */
	@Test
	public void testOtsu() {
		testComputeThresholdOp(34103, "threshold.otsu");
	}

	/**
	 * Tests
	 * {@link net.imagej.ops2.threshold.percentile.ComputePercentileThreshold}.
	 */
	@Test
	public void testPercentile() {
		testComputeThresholdOp(32088, "threshold.percentile");
	}

	/**
	 * Tests
	 * {@link net.imagej.ops2.threshold.renyiEntropy.ComputeRenyiEntropyThreshold}.
	 */
	@Test
	public void testRenyiEntropy() {
		testComputeThresholdOp(26546, "threshold.renyiEntropy");
	}

	/**
	 * Tests {@link net.imagej.ops2.threshold.shanbhag.ComputeShanbhagThreshold}.
	 */
	@Test
	public void testShanbhag() {
		testComputeThresholdOp(27553, "threshold.shanbhag");
	}

	/**
	 * Tests {@link net.imagej.ops2.threshold.triangle.ComputeTriangleThreshold}.
	 */
	@Test
	public void testTriangle() {
		testComputeThresholdOp(34607, "threshold.triangle");
	}

	/** Tests {@link net.imagej.ops2.threshold.yen.ComputeYenThreshold}. */
	@Test
	public void testYen() {
		testComputeThresholdOp(24531, "threshold.yen");
	}

	private void testComputeThresholdOp(final int expectedOutput,
		final String name)
	{
		final Computers.Arity1<Histogram1d<UnsignedShortType>, UnsignedShortType> opToTest =
			getComputeThresholdOp(name);
		final UnsignedShortType actualOutput = new UnsignedShortType();
		opToTest.compute(histogram(), actualOutput);
		assertThreshold(expectedOutput, actualOutput);
	}

	private Computers.Arity1<Histogram1d<UnsignedShortType>, UnsignedShortType>
		getComputeThresholdOp(final String name)
	{
		return OpBuilder.matchComputer(ops, name,
			new Nil<Histogram1d<UnsignedShortType>>()
			{}, new Nil<UnsignedShortType>() {});
	}

}
