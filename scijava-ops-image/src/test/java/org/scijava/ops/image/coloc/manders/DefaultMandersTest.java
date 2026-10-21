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

package org.scijava.ops.image.coloc.manders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.img.array.ArrayImgs;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractColocalisationTest;

/**
 * Tests {@link DefaultManders}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultMandersTest extends AbstractColocalisationTest {

	@Test
	public void testAnalytic() {
		final var red = ArrayImgs.doubles(new double[] { 4, 2, 0, 1, 3 }, 5);
		final var green = ArrayImgs.doubles(new double[] { 1, 0, 5, 0, 2 }, 5);
		final MandersResult m = ops.op("coloc.manders").input(red, green).outType(
			MandersResult.class).apply();
		// Red where green is nonzero: 4 + 3 of 10; green where red is: 1 + 2 of 8.
		assertEquals(0.7, m.getM1(), 1e-12);
		assertEquals(3.0 / 8, m.getM2(), 1e-12);

		final MandersResult t = ops.op("coloc.manders").input(red, green, 2.0, 1.0)
			.outType(MandersResult.class).apply();
		// Red where green is above 1: 0 + 3; green where red is above 2: 1 + 2.
		assertEquals(0.3, t.getM1(), 1e-12);
		assertEquals(3.0 / 8, t.getM2(), 1e-12);
	}

	@Test
	public void testNoSignal() {
		final var zeros = ArrayImgs.floats(4);
		final var ones = ArrayImgs.floats(new float[] { 1, 1, 1, 1 }, 4);
		final MandersResult m = ops.op("coloc.manders").input(zeros, ones).outType(
			MandersResult.class).apply();
		assertTrue(Double.isNaN(m.getM1()));
		assertEquals(0, m.getM2(), 0);
	}

	/**
	 * Compares with Coloc 2 (Colocalisation_Analysis 3.1.0) on its sample images:
	 * {@code MandersColocalization.calculateMandersCorrelation} without
	 * thresholds, and with the Costes thresholds (8, 6) of its linear stepper in
	 * {@code ThresholdMode.Above}, which counts values equal to the thresholds
	 * as above them.
	 */
	@Test
	public void testColoc2() {
		final var ch1 = getPositiveCorrelationImageCh1();
		final var ch2 = getPositiveCorrelationImageCh2();
		final MandersResult m = ops.op("coloc.manders").input(ch1, ch2).outType(
			MandersResult.class).apply();
		assertEquals(0.9945220990935079, m.getM1(), 1e-12);
		assertEquals(0.9964314056397573, m.getM2(), 1e-12);
		final MandersResult t = ops.op("coloc.manders").input(ch1, ch2, 7.0, 5.0)
			.outType(MandersResult.class).apply();
		assertEquals(0.729066416798541, t.getM1(), 1e-12);
		assertEquals(0.7419663476592765, t.getM2(), 1e-12);
	}
}
