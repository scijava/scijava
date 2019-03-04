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

package net.imagej.ops.coloc.icq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

import net.imagej.ops.coloc.ColocalisationTest;
import net.imagej.ops.coloc.pValue.PValueResult;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;
import org.scijava.ops.core.function.GenericFunctions;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Functions;
import org.scijava.thread.ThreadService;

/**
 * Tests {@link net.imagej.ops.Ops.Coloc.ICQ}.
 *
 * @author Curtis Rueden
 */
public class LiICQTest extends ColocalisationTest {

	@Test
	public void testICQ() {
		final Img<ByteType> img1 = generateByteArrayTestImg(true, 10, 15, 20);
		final Img<ByteType> img2 = generateByteArrayTestImg(true, 10, 15, 20);

		final Object icqValue = ops.run("coloc.icq", img1, img2);

		assertTrue(icqValue instanceof Double);
		assertEquals(0.5, (Double) icqValue, 0.0);
	}

	/**
	 * Checks Li's ICQ value for positive correlated images.
	 */
	@Test
	public void liPositiveCorrTest() {
		final Object icqValue = ops.run("coloc.icq", positiveCorrelationImageCh1, positiveCorrelationImageCh2);

		assertTrue(icqValue instanceof Double);
		final double icq = (Double) icqValue;
		assertTrue(icq > 0.34 && icq < 0.35);
	}

	/**
	 * Checks Li's ICQ value for zero correlated images. The ICQ value should be
	 * about zero.
	 */
	@Test
	public void liZeroCorrTest() {
		final Object icqValue = ops.run("coloc.icq", zeroCorrelationImageCh1, zeroCorrelationImageCh2);

		assertTrue(icqValue instanceof Double);
		final double icq = (Double) icqValue;
		assertTrue(Math.abs(icq) < 0.01);
	}

	/**
	 * Checks calculated pValue for Li's ICQ.
	 */
	@Test
	public void testPValue() {
		ExecutorService es = context.getService(ThreadService.class).getExecutorService();
		final double mean = 0.2;
		final double spread = 0.1;
		final double[] sigma = new double[] { 3.0, 3.0 };
		Img<FloatType> ch1 = ColocalisationTest.produceMeanBasedNoiseImage(new FloatType(), 24, 24, mean, spread, sigma,
				0x01234567);
		Img<FloatType> ch2 = ColocalisationTest.produceMeanBasedNoiseImage(new FloatType(), 24, 24, mean, spread, sigma,
				0x98765432);
		BiFunction<Iterable<FloatType>, Iterable<FloatType>, Double> op = Functions.binary(ops, "coloc.icq",
				new Nil<Iterable<FloatType>>() {}, new Nil<Iterable<FloatType>>() {}, new Nil<Double>() {});
		BiFunction<Iterable<FloatType>, Iterable<FloatType>, Double> genericOp = GenericFunctions.Functions.generic(op,
				new Nil<BiFunction<Iterable<FloatType>, Iterable<FloatType>, Double>>() {}.getType());
		PValueResult value = new PValueResult();
		ops.run("coloc.pValue", ch1, ch2, genericOp, es, value);
		assertEquals(0.72, value.getPValue(), 0.0);
	}

}