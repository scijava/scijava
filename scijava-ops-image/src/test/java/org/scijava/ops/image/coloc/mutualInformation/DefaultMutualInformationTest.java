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

package org.scijava.ops.image.coloc.mutualInformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractColocalisationTest;

/**
 * Tests {@link DefaultMutualInformation}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultMutualInformationTest extends AbstractColocalisationTest {

	/** Two binary images: identical, inverted, and independent. */
	@Test
	public void testTwoLevels() {
		final var x = ArrayImgs.unsignedBytes(new byte[] { 0, 1, 0, 1, 0, 1, 0,
			1 }, 8);
		final var inverted = ArrayImgs.unsignedBytes(new byte[] { 1, 0, 1, 0, 1, 0,
			1, 0 }, 8);
		final var independent = ArrayImgs.unsignedBytes(new byte[] { 0, 0, 1, 1, 0,
			0, 1, 1 }, 8);
		assertEquals(1, mi(x, x, 2), 1e-12);
		assertEquals(1, mi(x, inverted, 2), 1e-12);
		assertEquals(0, mi(x, independent, 2), 1e-12);
	}

	/** The mutual information of an image with itself is its entropy. */
	@Test
	public void testSelfInformation() {
		final var img = getPositiveCorrelationImageCh1();
		final Double h = ops.op("stats.entropy").input(img, 64).outType(
			Double.class).apply();
		assertEquals(h, mi(img, img, 64), 1e-10);
		assertTrue(h > 1);
	}

	/** Independent random images have almost no mutual information. */
	@Test
	public void testIndependentNoise() {
		final var rnd = new Random(0xc0ffee);
		final var a = ArrayImgs.doubles(256, 256);
		final var b = ArrayImgs.doubles(256, 256);
		a.forEach(t -> t.set(rnd.nextGaussian()));
		b.forEach(t -> t.set(rnd.nextGaussian()));
		assertTrue(mi(a, b, 16) < 0.01);
		final var mixed = ArrayImgs.doubles(256, 256);
		final var ca = a.cursor();
		final var cb = b.cursor();
		for (final DoubleType t : mixed)
			t.set(ca.next().get() + cb.next().get());
		// Note: for jointly Gaussian channels, MI = -log2(1 - rho^2) / 2.
		final double expected = -0.5 * Math.log(1 - 0.5) / Math.log(2);
		assertEquals(expected, mi(a, mixed, 32), 0.05);
	}

	@Test
	public void testCorrelatedImages() {
		final var mi = mi(getPositiveCorrelationImageCh1(),
			getPositiveCorrelationImageCh2(), null);
		final var miZero = mi(getZeroCorrelationImageCh1(),
			getZeroCorrelationImageCh2(), null);
		assertTrue(mi > miZero, mi + " <= " + miZero);
	}

	private double mi(final Object a, final Object b, final Integer bins) {
		if (bins == null) {
			return ops.op("coloc.mutualInformation").input(a, b).outType(
				Double.class).apply();
		}
		return ops.op("coloc.mutualInformation").input(a, b, bins).outType(
			Double.class).apply();
	}
}
