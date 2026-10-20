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

package org.scijava.ops.image.segment.dog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultDetectSpotsDoG}.
 *
 * @author Curtis Rueden
 */
public class DetectSpotsDoGTest extends AbstractOpTest {

	private static final double SPOT_SIGMA = 2;

	// These are the true spot centers, in (x, y) pairs, at subpixel positions.
	private static final double[][] SPOTS = { //
		{ 10.3, 12.7 }, { 30.8, 9.2 }, { 50.5, 14.1 }, //
		{ 14.6, 40.4 }, { 33.1, 35.9 }, { 49.9, 44.6 } //
	};

	/** Renders Gaussian spots on a constant background, with optional noise. */
	private static Img<FloatType> render(final double amplitude,
		final double background, final boolean noise)
	{
		final var img = ArrayImgs.floats(64, 56);
		final var rng = new Random(42);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final double x = c.getDoublePosition(0), y = c.getDoublePosition(1);
			var v = background;
			for (final var s : SPOTS) {
				final double dx = x - s[0], dy = y - s[1];
				v += amplitude * Math.exp(-(dx * dx + dy * dy) / (2 * SPOT_SIGMA *
					SPOT_SIGMA));
			}
			// Note: Gaussian noise with variance v approximates Poisson noise.
			if (noise) v += Math.sqrt(Math.abs(v)) * rng.nextGaussian();
			c.get().setReal(v);
		}
		return img;
	}

	private List<RealPoint> detect(final Img<FloatType> img,
		final Double threshold, final Boolean bright, final Boolean subpixel)
	{
		return ops.op("segment.detectSpotsDoG").input(img, SPOT_SIGMA, null,
			threshold, bright, subpixel).outType(new Nil<List<RealPoint>>()
		{}).apply();
	}

	/** Returns the RMS distance from each true spot to its nearest detection. */
	private static double rmsError(final List<RealPoint> detections) {
		double sum = 0;
		for (final var s : SPOTS) {
			var best = Double.POSITIVE_INFINITY;
			for (final var p : detections) {
				final var dx = p.getDoublePosition(0) - s[0];
				final var dy = p.getDoublePosition(1) - s[1];
				best = Math.min(best, dx * dx + dy * dy);
			}
			sum += best;
		}
		return Math.sqrt(sum / SPOTS.length);
	}

	@Test
	public void testSubpixelNoiseless() {
		final var spots = ops.op("segment.detectSpotsDoG").input(render(1000, 100,
			false), SPOT_SIGMA, null, 10.0).outType(new Nil<List<RealPoint>>()
		{}).apply();
		assertEquals(SPOTS.length, spots.size());
		assertTrue(rmsError(spots) < 0.05, "RMS error: " + rmsError(spots));
	}

	@Test
	public void testSubpixelNoisy() {
		final var spots = detect(render(1000, 100, true), 50.0, null, null);
		assertEquals(SPOTS.length, spots.size());
		assertTrue(rmsError(spots) < 0.1, "RMS error: " + rmsError(spots));
	}

	@Test
	public void testPixelPositions() {
		final var spots = detect(render(1000, 100, false), 10.0, true, false);
		assertEquals(SPOTS.length, spots.size());
		for (final var p : spots) {
			for (var d = 0; d < 2; d++) {
				final var v = p.getDoublePosition(d);
				assertEquals(Math.rint(v), v, 0);
			}
		}
		// Each pixel detection is within a pixel of the true center.
		assertTrue(rmsError(spots) < 1, "RMS error: " + rmsError(spots));
	}

	@Test
	public void testDarkSpots() {
		final var img = render(-500, 1000, false);
		// Bright-spot detection may respond to the rings around the dark spots,
		// but never at their centers.
		assertTrue(rmsError(detect(img, 10.0, true, true)) > 1);
		final var spots = detect(img, 10.0, false, true);
		assertEquals(SPOTS.length, spots.size());
		assertTrue(rmsError(spots) < 0.05, "RMS error: " + rmsError(spots));
	}

	@Test
	public void testThreshold() {
		final var img = render(1000, 100, false);
		// Without a threshold, weak background extrema between spots appear.
		assertTrue(detect(img, null, null, null).size() > SPOTS.length);
		assertEquals(SPOTS.length, detect(img, 10.0, null, null).size());
		assertEquals(0, detect(img, 1e6, null, null).size());
	}

	@Test
	public void testFlatImage() {
		final var img = ArrayImgs.floats(20, 20);
		for (final var t : img)
			t.set(7);
		final List<RealPoint> spots = ops.op("segment.detectSpotsDoG").input(img,
			SPOT_SIGMA).outType(new Nil<List<RealPoint>>()
		{}).apply();
		assertEquals(0, spots.size());
	}
}
