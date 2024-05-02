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

package org.scijava.ops.image.coloc.icq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;

import org.scijava.ops.image.AbstractColocalisationTest;
import org.scijava.ops.image.coloc.pValue.PValueResult;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link org.scijava.ops.image.coloc.icq.LiICQ}.
 *
 * @author Curtis Rueden
 */
public class LiICQTest extends AbstractColocalisationTest {

	@Test
	public void testICQ() {
		final Img<ByteType> img1 = TestImgGeneration.byteArray(true, 10, 15, 20);
		final Img<ByteType> img2 = TestImgGeneration.byteArray(true, 10, 15, 20);

		final Double icqValue = ops.op("coloc.icq").input(img1, img2).outType(
			Double.class).apply();

		assertEquals(0.5, icqValue, 0.0);
	}

	/**
	 * Checks Li's ICQ value for positive correlated images.
	 */
	@Test
	public void liPositiveCorrTest() {
		final Double icqValue = ops.op("coloc.icq").input(
			getPositiveCorrelationImageCh1(), getPositiveCorrelationImageCh2())
			.outType(Double.class).apply();
		assertTrue(icqValue > 0.34 && icqValue < 0.35);
	}

	/**
	 * Checks Li's ICQ value for zero correlated images. The ICQ value should be
	 * about zero.
	 */
	@Test
	public void liZeroCorrTest() {
		final Object icqValue = ops.op("coloc.icq").input(
			getZeroCorrelationImageCh1(), getZeroCorrelationImageCh2()).apply();

		assertTrue(icqValue instanceof Double);
		final double icq = (Double) icqValue;
		assertTrue(Math.abs(icq) < 0.01);
	}

	/**
	 * Checks calculated pValue for Li's ICQ.
	 */
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
			OpBuilder.matchFunction(ops, "coloc.icq",
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}, new Nil<RandomAccessibleInterval<FloatType>>() {},
				new Nil<Double>()
				{});
		PValueResult value = new PValueResult();
		ops.op("coloc.pValue").input(ch1, ch2, op).output(value).compute();
		assertEquals(0.72, value.getPValue(), 0.0);
	}

}
