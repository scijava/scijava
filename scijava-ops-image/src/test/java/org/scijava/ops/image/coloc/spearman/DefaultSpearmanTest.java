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

package org.scijava.ops.image.coloc.spearman;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imglib2.img.array.ArrayImgs;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractColocalisationTest;

/**
 * Tests {@link DefaultSpearman}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultSpearmanTest extends AbstractColocalisationTest {

	@Test
	public void testMonotonic() {
		final var x = ArrayImgs.doubles(new double[] { 1, 2, 3, 4, 5 }, 5);
		final var y = ArrayImgs.doubles(new double[] { 1, 8, 27, 64, 125 }, 5);
		final var reversed = ArrayImgs.doubles(new double[] { 9, 7, 5, 3, 1 }, 5);
		assertEquals(1, rho(x, y), 1e-12);
		assertEquals(-1, rho(x, reversed), 1e-12);
	}

	@Test
	public void testTies() {
		// Ranks of x: 1, 2.5, 2.5, 4; ranks of y: 2, 1, 3.5, 3.5.
		final var x = ArrayImgs.doubles(new double[] { 1, 2, 2, 3 }, 4);
		final var y = ArrayImgs.doubles(new double[] { 5, 4, 6, 6 }, 4);
		final double[] rx = { 1, 2.5, 2.5, 4 };
		final double[] ry = { 2, 1, 3.5, 3.5 };
		double sxy = 0, sxx = 0, syy = 0;
		for (var i = 0; i < 4; i++) {
			sxy += (rx[i] - 2.5) * (ry[i] - 2.5);
			sxx += (rx[i] - 2.5) * (rx[i] - 2.5);
			syy += (ry[i] - 2.5) * (ry[i] - 2.5);
		}
		assertEquals(sxy / Math.sqrt(sxx * syy), rho(x, y), 1e-12);
	}

	/**
	 * Compares with Coloc 2 (Colocalisation_Analysis 3.1.0)
	 * {@code SpearmanRankCorrelation.calculateSpearmanRank} on its sample
	 * images.
	 */
	@Test
	public void testColoc2() {
		assertEquals(0.5463428429415399, rho(getPositiveCorrelationImageCh1(),
			getPositiveCorrelationImageCh2()), 1e-10);
	}

	private double rho(final Object a, final Object b) {
		return ops.op("coloc.spearman").input(a, b).outType(Double.class).apply();
	}
}
