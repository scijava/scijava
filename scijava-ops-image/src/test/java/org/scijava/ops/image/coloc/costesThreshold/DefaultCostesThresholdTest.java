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

package org.scijava.ops.image.coloc.costesThreshold;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.img.array.ArrayImgs;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractColocalisationTest;
import org.scijava.ops.image.coloc.manders.MandersResult;

/**
 * Tests {@link DefaultCostesThreshold}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultCostesThresholdTest extends AbstractColocalisationTest {

	/**
	 * Noise background with a colocalized bright population on the line
	 * {@code y = 2x + 1}: the thresholds must separate the two.
	 */
	@Test
	public void testSynthetic() {
		final var rnd = new Random(42);
		final var n = 20000;
		final var x = new double[n];
		final var y = new double[n];
		for (var i = 0; i < n; i++) {
			if (i % 4 == 0) {
				final var s = 50 + 50 * rnd.nextDouble();
				x[i] = s + rnd.nextGaussian();
				y[i] = 2 * s + 1 + rnd.nextGaussian();
			}
			else {
				x[i] = 10 + 3 * rnd.nextGaussian();
				y[i] = 21 + 3 * rnd.nextGaussian();
			}
		}
		final CostesResult r = ops.op("coloc.costesThreshold").input(ArrayImgs
			.doubles(x, n), ArrayImgs.doubles(y, n)).outType(CostesResult.class)
			.apply();
		assertEquals(2, r.getSlope(), 0.05);
		assertEquals(1, r.getIntercept(), 2);
		assertEquals(r.getSlope() * r.getThreshold1() + r.getIntercept(), r
			.getThreshold2(), 1e-12);
		assertTrue(r.getPearsonsBelow() <= 0);
		assertTrue(r.getThreshold1() > 15 && r.getThreshold1() < 50, r
			.toString());
	}

	/** Orthogonal regression of exactly linear data recovers the line. */
	@Test
	public void testRegression() {
		final var x = ArrayImgs.doubles(new double[] { 0, 1, 2, 3, 4, 5 }, 6);
		final var y = ArrayImgs.doubles(new double[] { 3, 2.5, 2, 1.5, 1, 0.5 },
			6);
		final CostesResult r = ops.op("coloc.costesThreshold").input(x, y)
			.outType(CostesResult.class).apply();
		assertEquals(-0.5, r.getSlope(), 1e-12);
		assertEquals(3, r.getIntercept(), 1e-12);
		// Anticorrelated channels have no colocalized pixels.
		assertEquals(5, r.getThreshold1(), 0);
	}

	/**
	 * Compares with Coloc 2 (Colocalisation_Analysis 3.1.0)
	 * {@code AutoThresholdRegression} on its sample images: slope
	 * 0.7039042236360126, intercept 0.7283700920021605; its linear Costes
	 * stepper gives the thresholds (8, 6) with a Pearson correlation below of
	 * -0.011639152703460071, and its bisection stepper (10, 8) with 0.0166.
	 * Coloc 2 counts values equal to the thresholds as above them, so its (8, 6)
	 * selects the same pixels as thresholds (7, 5.66) here; its tM1 and tM2 are
	 * 0.729066416798541 and 0.7419663476592765.
	 */
	@Test
	public void testColoc2() {
		final CostesResult r = ops.op("coloc.costesThreshold").input(
			getPositiveCorrelationImageCh1(), getPositiveCorrelationImageCh2())
			.outType(CostesResult.class).apply();
		// Note: Coloc 2 accumulates its regression sums differently.
		assertEquals(0.7039042236360126, r.getSlope(), 1e-7);
		assertEquals(0.7283700920021605, r.getIntercept(), 1e-6);
		assertEquals(7, r.getThreshold1(), 0);
		assertEquals(5, Math.floor(r.getThreshold2()), 0);
		assertEquals(-0.011639152703460071, r.getPearsonsBelow(), 1e-12);

		final MandersResult m = ops.op("coloc.manders").input(
			getPositiveCorrelationImageCh1(), getPositiveCorrelationImageCh2(), r
				.getThreshold1(), r.getThreshold2()).outType(MandersResult.class)
			.apply();
		assertEquals(0.729066416798541, m.getM1(), 1e-12);
		assertEquals(0.7419663476592765, m.getM2(), 1e-12);
	}
}
