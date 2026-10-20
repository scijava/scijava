/*
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

package org.scijava.ops.image.segment.hwatershed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@code segment.hWatershed}.
 * <p>
 * The {@code EXPECTED_*} label images were produced by running the original
 * Interactive H-Watershed classes ({@code HWatershedLabeling} with threshold
 * {@code -Infinity}, then {@code SegmentHierarchyToLabelMap.getLabelMap}, as
 * done by its headless {@code HWatershed_Plugin}) on the same integer-valued
 * synthetic images built by {@link #bumps}. Each string lists the labels of
 * the image in flat iteration order, one row of the first dimension per line.
 * </p>
 */
public class HWatershedTest extends AbstractOpTest {

	private static final long[] DIMS_2D = { 20, 20 };
	private static final double[][] PEAKS_2D = { //
		{ 5, 5, 100, 3 }, { 12, 6, 80, 3 }, { 8, 13, 60, 2.5 }, //
		{ 16, 15, 30, 2 }, { 3, 15, 12, 1.5 } };

	private static final long[] DIMS_3D = { 10, 9, 6 };
	private static final double[][] PEAKS_3D = { //
		{ 2, 2, 2, 50, 1.5 }, { 7, 3, 3, 40, 1.5 }, { 4, 7, 3, 30, 1.2 } };

	/** Original plugin, 2D FACE h=5.0 T=10.0 pf=100.0 split=true. */
	private static final String EXPECTED_2D_FACE_H5_T10 = //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"33333333333333333333" + //
		"22222222222233333333" + //
		"22222222222222311111" + //
		"22222222222222111111" + //
		"22222222222222111111" + //
		"22222222222221111111" + //
		"22222222222221111111" + //
		"22222222222221111111" + //
		"22222222222221111111" + //
		"22222222222221111111";

	/** Original plugin, 2D FACE h=0.0 T=10.0 pf=100.0 split=true. */
	private static final String EXPECTED_2D_FACE_H0_T10 = //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"44444444443333333333" + //
		"22222222222233333333" + //
		"22222222222222311111" + //
		"22222222222222111111" + //
		"22222222222222111111" + //
		"22222222222221111111" + //
		"22222222222221111111" + //
		"22222222222221111111" + //
		"22222222222221111111" + //
		"22222222222221111111";

	/** Original plugin, 2D FACE h=5.0 T=20.0 pf=100.0 split=true. */
	private static final String EXPECTED_2D_FACE_H5_T20 = //
		"03333333333333000000" + //
		"33333333333333330000" + //
		"33333333333333333000" + //
		"33333333333333333300" + //
		"33333333333333333300" + //
		"33333333333333333330" + //
		"33333333333333333330" + //
		"33333333333333333330" + //
		"33333333333333333300" + //
		"33333333333333333300" + //
		"03333333333333333000" + //
		"00222222222233333000" + //
		"00022222222222311100" + //
		"00022222222222111110" + //
		"00222222222222111110" + //
		"00222222222221111110" + //
		"00022222222221111110" + //
		"00000222222000111110" + //
		"00000000000000000000" + //
		"00000000000000000000";

	/** Original plugin, 2D FACE h=5.0 T=30.0 pf=50.0 split=false. */
	private static final String EXPECTED_2D_FACE_H5_T30_PF50_NOSPLIT = //
		"00000000000000000000" + //
		"00000000000000000000" + //
		"00000000000000000000" + //
		"00033333300000000000" + //
		"00033333333333000000" + //
		"00033333333333000000" + //
		"00033333333333300000" + //
		"00033333333333000000" + //
		"00000333333333000000" + //
		"00000000000000000000" + //
		"00000000000000000000" + //
		"00000022222200000000" + //
		"00000022222000000000" + //
		"00000022222000000000" + //
		"00000022222000011000" + //
		"00000000200000011100" + //
		"00000000000000001000" + //
		"00000000000000000000" + //
		"00000000000000000000" + //
		"00000000000000000000";

	/** Original plugin, 2D FACE h=5.0 T=30.0 pf=70.0 split=true. */
	private static final String EXPECTED_2D_FACE_H5_T30_PF70 = //
		"00000000000000000000" + //
		"00000000000000000000" + //
		"00033333300000000000" + //
		"00333333333333000000" + //
		"00333333333333300000" + //
		"00333333333333330000" + //
		"00333333333333330000" + //
		"00333333333333330000" + //
		"00033333333333300000" + //
		"00000333333333000000" + //
		"00000033333300000000" + //
		"00000222222200000000" + //
		"00000222222200000000" + //
		"00000222222200000000" + //
		"00000022222000011100" + //
		"00000022220000011100" + //
		"00000000000000011100" + //
		"00000000000000000000" + //
		"00000000000000000000" + //
		"00000000000000000000";

	/** Original plugin, 2D FULL h=0.0 T=25.0 pf=80.0 split=true. */
	private static final String EXPECTED_2D_FULL_H0_T25_PF80 = //
		"00000000000000000000" + //
		"00044444400000000000" + //
		"00444444444443000000" + //
		"00444444444433300000" + //
		"04444444444333330000" + //
		"04444444443333333000" + //
		"04444444443333333000" + //
		"04444444443333333000" + //
		"00444444443333330000" + //
		"00044444433333330000" + //
		"00000444443333000000" + //
		"00000422222330000000" + //
		"00000222222200000000" + //
		"00000222222200111000" + //
		"00000222222200111100" + //
		"00000222222000111110" + //
		"00000002220000011100" + //
		"00000000000000001000" + //
		"00000000000000000000" + //
		"00000000000000000000";

	/** Original plugin, 3D FACE h=3.0 T=0.0 pf=100.0 split=true. */
	private static final String EXPECTED_3D_FACE_H3 = //
		"3333332222" + //
		"3333332222" + //
		"3333332222" + //
		"3333322222" + //
		"3333322222" + //
		"3333122222" + //
		"3331112222" + //
		"3311111122" + //
		"1111111122" + //
		"3333332222" + //
		"3333332222" + //
		"3333332222" + //
		"3333322222" + //
		"3333322222" + //
		"3333122222" + //
		"3331112222" + //
		"3111111122" + //
		"1111111122" + //
		"3333332222" + //
		"3333332222" + //
		"3333332222" + //
		"3333322222" + //
		"3333322222" + //
		"3333122222" + //
		"3311112222" + //
		"1111111122" + //
		"1111111122" + //
		"3333322222" + //
		"3333322222" + //
		"3333322222" + //
		"3333322222" + //
		"3333322222" + //
		"3331112222" + //
		"3111111222" + //
		"1111111122" + //
		"1111111112" + //
		"3333322222" + //
		"3333322222" + //
		"3333322222" + //
		"3333322222" + //
		"3333222222" + //
		"3331122222" + //
		"1111111222" + //
		"1111111122" + //
		"1111111122" + //
		"3333322222" + //
		"3333322222" + //
		"3333322222" + //
		"3333322222" + //
		"3333222222" + //
		"3331122222" + //
		"1111111222" + //
		"1111111122" + //
		"1111111122";

	/** Original plugin, 3D FULL h=0.0 T=5.0 pf=60.0 split=true. */
	private static final String EXPECTED_3D_FULL_H0_T5_PF60 = //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0333000000" + //
		"0333000000" + //
		"0333000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0333000000" + //
		"0333302220" + //
		"0333002220" + //
		"0000002220" + //
		"0000000000" + //
		"0000100000" + //
		"0001110000" + //
		"0000100000" + //
		"0000000000" + //
		"0333000000" + //
		"0333002220" + //
		"0333022220" + //
		"0000002220" + //
		"0000000000" + //
		"0001110000" + //
		"0001110000" + //
		"0000110000" + //
		"0000000000" + //
		"0000000000" + //
		"0000002220" + //
		"0000002220" + //
		"0000002220" + //
		"0000000000" + //
		"0000100000" + //
		"0001110000" + //
		"0000100000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000" + //
		"0000000000";

	// -- Regression tests against the original plugin --

	@Test
	public void testRegression2D() {
		final var img = bumps(DIMS_2D, 10, PEAKS_2D);
		assertEquals(EXPECTED_2D_FACE_H5_T10, digits(hWatershed(img, 5, 10, 100,
			true, false)));
		assertEquals(EXPECTED_2D_FACE_H0_T10, digits(hWatershed(img, 0, 10, 100,
			true, false)));
		assertEquals(EXPECTED_2D_FACE_H5_T20, digits(hWatershed(img, 5, 20, 100,
			true, false)));
		assertEquals(EXPECTED_2D_FACE_H5_T30_PF50_NOSPLIT, digits(hWatershed(img, 5,
			30, 50, false, false)));
		assertEquals(EXPECTED_2D_FACE_H5_T30_PF70, digits(hWatershed(img, 5, 30,
			70, true, false)));
		assertEquals(EXPECTED_2D_FULL_H0_T25_PF80, digits(hWatershed(img, 0, 25,
			80, true, true)));
	}

	@Test
	public void testRegression3D() {
		final var img = bumps(DIMS_3D, 0, PEAKS_3D);
		assertEquals(EXPECTED_3D_FACE_H3, digits(hWatershed(img, 3, 0, 100, true,
			false)));
		assertEquals(EXPECTED_3D_FULL_H0_T5_PF60, digits(hWatershed(img, 0, 5, 60,
			true, true)));
	}

	// -- Analytic tests --

	/**
	 * Two peaks of heights 100 and 80.25, separated by a saddle at 60: the lower
	 * peak has dynamics 20.25, so it seeds its own segment iff hMin < 20.25.
	 */
	@Test
	public void testDynamicsSplit() {
		final var img = twoPeaks();
		assertEquals(2, labelCount(hWatershed(img, 20.2, 0, 100, true, false)));
		assertEquals(1, labelCount(hWatershed(img, 20.3, 0, 100, true, false)));
		assertEquals(2, labelCount(hWatershed(img, 0, 0, 100, true, true)));

		// Every pixel is labeled when there is no threshold.
		final var split = hWatershed(img, 10, 0, 100, true, false);
		assertEquals(0, countBackground(split));
		// The basins meet at the saddle (x = 10).
		final var ra = split.randomAccess();
		ra.setPosition(new long[] { 9, 0 });
		final var left = ra.get().iterator().next();
		ra.setPosition(new long[] { 11, 0 });
		final var right = ra.get().iterator().next();
		assertTrue(left != right);
	}

	/** Threshold and peak flooding remove the low pixels of each segment. */
	@Test
	public void testThresholdAndPeakFlooding() {
		final var img = twoPeaks();
		// Profile values >= 70 are at x in [2, 8] (peak 100) and x in [12, 18]
		// (peak 80.25), on all 3 rows.
		final var thresholded = hWatershed(img, 10, 70, 100, true, false);
		assertEquals(2, labelCount(thresholded));
		assertEquals(21 * 3 - (7 + 7) * 3, countBackground(thresholded));

		// With 50% flooding, the thresholds are 70 + 30 / 2 = 85 and
		// 70 + 10.25 / 2 = 75.125, which keeps x in [4, 6] and x in [13, 17].
		final var flooded = hWatershed(img, 10, 70, 50, true, false);
		assertEquals(2, labelCount(flooded));
		assertEquals(21 * 3 - (3 + 5) * 3, countBackground(flooded));
	}

	/**
	 * A threshold above the saddle separates a low peak from the segment it was
	 * merged into: it is kept iff splitting is allowed.
	 */
	@Test
	public void testAllowSplitting() {
		final var img = twoPeaks();
		assertEquals(2, labelCount(hWatershed(img, 30, 65, 100, true, false)));
		assertEquals(1, labelCount(hWatershed(img, 30, 65, 100, false, false)));
		assertEquals(1, labelCount(hWatershed(img, 30, 55, 100, true, false)));
	}

	@Test
	public void testDefaults() {
		final var img = bumps(DIMS_2D, 10, PEAKS_2D);
		final ImgLabeling<Integer, IntType> defaults = ops.op("segment.hWatershed")
			.input(img).outType(new Nil<ImgLabeling<Integer, IntType>>()
			{}).apply();
		// By default, hMin is 5% of the range and threshold is the image minimum.
		final var range = 115 - 10;
		assertEquals(digits(hWatershed(img, 0.05 * range, 10, 100, true, false)),
			digits(defaults));
	}

	@Test
	public void testFlatImage() {
		final var img = ArrayImgs.floats(5, 4);
		for (final var t : img)
			t.set(3);
		final var labeling = hWatershed(img, 0, 0, 100, true, false);
		assertEquals(0, labelCount(labeling));
		assertEquals(20, countBackground(labeling));
	}

	/** The op works in any dimension; here two 4D bumps. */
	@Test
	public void test4D() {
		final var img = bumps(new long[] { 7, 6, 5, 4 }, 0, new double[][] { //
			{ 1, 1, 1, 1, 50, 1.2 }, { 5, 4, 3, 2, 50, 1.2 } });
		assertEquals(2, labelCount(hWatershed(img, 5, 0, 100, true, false)));
		assertEquals(1, labelCount(hWatershed(img, 60, 0, 100, true, false)));
	}

	@Test
	public void testNonZeroMinInterval() {
		final var img = bumps(DIMS_2D, 10, PEAKS_2D);
		final var translated = Views.translate(img, 5, -3);
		assertEquals(EXPECTED_2D_FACE_H5_T10, digits(hWatershed(translated, 5, 10,
			100, true, false)));
	}

	@Test
	public void testNegativeHMin() {
		final var img = twoPeaks();
		assertThrows(IllegalArgumentException.class, () -> hWatershed(img, -1, 0,
			100, true, false));
	}

	// -- Helper methods --

	private ImgLabeling<Integer, IntType> hWatershed(
		final RandomAccessibleInterval<FloatType> img, final double hMin,
		final double threshold, final double peakFlooding,
		final boolean allowSplitting, final boolean useEightConnectivity)
	{
		return ops.op("segment.hWatershed").input(img, hMin, threshold,
			peakFlooding, allowSplitting, useEightConnectivity).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();
	}

	/**
	 * A 21x3 image whose rows all follow the same piecewise linear profile: a
	 * peak of 100 at x = 5, a saddle of 60 at x = 10 and a peak of 80.25 at x =
	 * 15, descending by 10 per pixel elsewhere.
	 */
	private static Img<FloatType> twoPeaks() {
		final float[] profile = { 50, 60, 70, 80, 90, 100, 90, 80, 70, 65, 60,
			65.25f, 70.25f, 75.25f, 77.75f, 80.25f, 77.75f, 75.25f, 70.25f, 60.25f,
			50.25f };
		final var w = profile.length;
		final var data = new float[w * 3];
		for (var y = 0; y < 3; y++)
			System.arraycopy(profile, 0, data, y * w, w);
		return ArrayImgs.floats(data, w, 3);
	}

	/**
	 * Integer-valued sum of isotropic Gaussian bumps, each given as
	 * {@code {position..., amplitude, sigma}}; must match the harness used to
	 * produce the expected values.
	 */
	private static Img<FloatType> bumps(final long[] dims, final double base,
		final double[][] peaks)
	{
		var n = 1;
		for (final var d : dims)
			n *= (int) d;
		final var data = new float[n];
		final var pos = new long[dims.length];
		for (var i = 0; i < n; i++) {
			var idx = i;
			for (var d = 0; d < dims.length; d++) {
				pos[d] = idx % dims[d];
				idx /= (int) dims[d];
			}
			var v = base;
			for (final var p : peaks) {
				double r2 = 0;
				for (var d = 0; d < dims.length; d++) {
					final var dd = pos[d] - p[d];
					r2 += dd * dd;
				}
				final var s = p[dims.length + 1];
				v += p[dims.length] * Math.exp(-r2 / (2 * s * s));
			}
			data[i] = (float) Math.floor(v);
		}
		return ArrayImgs.floats(data, dims);
	}

	private static String digits(final ImgLabeling<Integer, IntType> labeling) {
		final var sb = new StringBuilder();
		for (final var t : Views.flatIterable(labeling)) {
			assertTrue(t.size() <= 1);
			final var label = t.isEmpty() ? 0 : t.iterator().next();
			assertTrue(label < 10);
			sb.append(label);
		}
		return sb.toString();
	}

	private static int labelCount(final ImgLabeling<Integer, IntType> labeling) {
		final Set<Integer> labels = new HashSet<>();
		for (final var t : labeling)
			labels.addAll(t);
		return labels.size();
	}

	private static int countBackground(
		final ImgLabeling<Integer, IntType> labeling)
	{
		var count = 0;
		for (final var t : labeling)
			if (t.isEmpty()) count++;
		return count;
	}
}
