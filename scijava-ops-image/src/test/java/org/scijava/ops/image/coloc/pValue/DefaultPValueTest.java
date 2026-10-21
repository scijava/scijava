/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.function.BiFunction;

import org.scijava.ops.image.AbstractColocalisationTest;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

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
	 * Runs the Costes et al. (2004) significance test (Pearson coefficient,
	 * PSF-sized blocks) on a channel and a noisy copy of it: the observed
	 * coefficient must exceed all shuffled ones (Costes P = 100%, p-value 0),
	 * and is not significant against an unrelated channel.
	 */
	@Test
	public void testCostes() {
		final Img<FloatType> ch1 = ArrayImgs.floats(60, 60);
		final Img<FloatType> ch2 = ArrayImgs.floats(60, 60);
		final Img<FloatType> ch3 = ArrayImgs.floats(60, 60);
		final var rng = new java.util.Random(42);
		final var c1 = ch1.localizingCursor();
		final var c2 = ch2.cursor();
		final var c3 = ch3.cursor();
		while (c1.hasNext()) {
			c1.fwd();
			final double x = c1.getDoublePosition(0), y = c1.getDoublePosition(1);
			final double v = Math.sin(x / 3) * Math.cos(y / 4);
			c1.get().setReal(100 + 50 * v);
			c2.next().setReal(100 + 50 * v + 20 * rng.nextGaussian());
			c3.next().setReal(100 + 20 * rng.nextGaussian());
		}
		final BiFunction<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>, Double> pearsons =
			ops.op("coloc.pearsons").inType(new Nil<RandomAccessibleInterval<FloatType>>() {},
				new Nil<RandomAccessibleInterval<FloatType>>() {}).outType(Double.class)
				.function();
		final PValueResult coloc = new PValueResult();
		ops.op("coloc.pValue").input(ch1, ch2, pearsons, 50, new FinalDimensions(
			5, 5)).output(coloc).compute();
		assertEquals(0.0, coloc.getPValue(), 0.0);
		final PValueResult noColoc = new PValueResult();
		ops.op("coloc.pValue").input(ch1, ch3, pearsons, 50, new FinalDimensions(
			5, 5)).output(noColoc).compute();
		assertTrue(noColoc.getPValue() > 0.05);
	}

	/**
	 * Tests that the shuffled images stay aligned with the second image when the
	 * images are trimmed to a multiple of the block size: with a single block,
	 * every shuffle is the identity, so each shuffled image must equal the
	 * (trimmed) first image.
	 */
	@Test
	public void testTrimmedAlignment() {
		final Img<FloatType> image = ArrayImgs.floats(8, 8);
		int i = 0;
		for (final FloatType t : image)
			t.setReal(i++);
		// Returns the largest absolute difference between the two images.
		final BiFunction<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>, Double> op =
			ops.typeLambda(new Nil<>() {}, (a, b) -> {
				final var ca = Views.flatIterable(a).cursor();
				final var cb = Views.flatIterable(b).cursor();
				double max = 0;
				while (ca.hasNext()) {
					max = Math.max(max, Math.abs(ca.next().get() - cb.next().get()));
				}
				return max;
			});
		final PValueResult output = new PValueResult();
		ops.op("coloc.pValue").input(image, image, op, 4, new FinalDimensions(6,
			6)).output(output).compute();
		assertEquals(0.0, output.getColocValue(), 0.0);
		for (final double v : output.getColocValuesArray()) {
			assertEquals(0.0, v, 0.0);
		}
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

		BiFunction<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>, Double> op =
			ops.typeLambda(new Nil<>() {}, (input1, input2) -> {
				double r;
				synchronized (this) {
					r = result[count[0]++];
				}
				return r;
			});

		PValueResult output = new PValueResult();
		ops.op("coloc.pValue").input(ch1, ch1, op, result.length - 1).output(
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
