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

package org.scijava.ops.image.segment.srm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultStatisticalRegionMerging} and
 * {@link StatisticalRegionMergingMeans}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class StatisticalRegionMergingTest extends AbstractOpTest {

	private static final String OP = "segment.statisticalRegionMerging";

	/**
	 * Regression test against the original Fiji {@code SRM_} plugin
	 * (Statistical_Region_Merging, git HEAD as of 2026-10). The expected values
	 * were produced by a throwaway harness that compiled {@code SRM_.java}
	 * against ImageJ 1.54p, built an 8-bit {@code ImagePlus} from
	 * {@link #synth}, and called {@code srm2D(ip, false)} (for d = 1) or
	 * {@code srm3D(false)}, then {@code srm2D(ip, true)} / {@code srm3D(true)}.
	 * {@code labelHash} is {@link Arrays#hashCode(int[])} of the plugin's region
	 * index image in raster order; {@code meanSum} is the sum of its
	 * "showAverages" image. The region partition and numbering must match
	 * exactly.
	 */
	@Test
	public void testRegressionAgainstOriginal() {
		for (final var row : REGRESSION) {
			assertRegression((int) row[0], (int) row[1], (int) row[2], (int) row[3],
				row[4], (int) row[5], (int) row[6], row[7]);
		}
	}

	/**
	 * Columns: synth mode, width, height, depth, Q, region count, label hash,
	 * sum of region means.
	 */
	private static final double[][] REGRESSION = { //
		{ 0, 24, 20, 1, 5, 17, 1452090626, 55285.000314712524 }, //
		{ 0, 24, 20, 1, 25, 17, 1452090626, 55285.000314712524 }, //
		{ 0, 24, 20, 1, 100, 18, 931531555, 55285.00026893616 }, //
		{ 0, 12, 10, 6, 25, 9, 544277378, 95061.99896621704 }, //
		{ 0, 12, 10, 6, 100, 10, 1736166980, 95061.99871826172 }, //
		{ 1, 24, 20, 1, 5, 6, 101628027, 59074.99913024902 }, //
		{ 1, 24, 20, 1, 25, 9, 1450233234, 59075.00372314453 }, //
		{ 1, 24, 20, 1, 100, 53, 1256104942, 59074.999629974365 }, //
		{ 1, 24, 20, 1, 400, 150, -965902072, 59075.00006484985 }, //
		{ 1, 12, 10, 6, 25, 11, 1790855819, 86966.9997253418 }, //
		{ 1, 12, 10, 6, 400, 124, -9543278, 86967.00000572205 }, //
	};

	private void assertRegression(final int mode, final int w, final int h,
		final int d, final double q, final int expectedRegions,
		final int expectedLabelHash, final double expectedMeanSum)
	{
		final var img = synth(mode, w, h, d);
		final var labeling = labeling(img, q, null);
		final var labels = labels(labeling);
		assertEquals(expectedRegions, Arrays.stream(labels).max().getAsInt() + 1);
		assertEquals(expectedLabelHash, Arrays.hashCode(labels));

		final var means = ArrayImgs.floats(img.dimensionsAsLongArray());
		ops.op(OP).input(img, q, null).output(means).compute();
		var sum = 0.0;
		for (final var v : means)
			sum += v.get();
		assertEquals(expectedMeanSum, sum, 1e-2);
		assertMeansMatchRegions(img, labels, means);
	}

	/** Default parameters are Q = 25 and g = 256. */
	@Test
	public void testDefaults() {
		final var img = synth(1, 24, 20, 1);
		final ImgLabeling<Integer, IntType> labeling = ops.op(OP).input(img)
			.outType(new Nil<ImgLabeling<Integer, IntType>>()
			{}).apply();
		assertArrayEquals(labels(labeling(img, 25.0, 256)), labels(labeling));
		assertEquals(1450233234, Arrays.hashCode(labels(labeling)));

		final var means = ArrayImgs.doubles(24, 20);
		ops.op(OP).input(img).output(means).compute();
		final var expected = ArrayImgs.doubles(24, 20);
		ops.op(OP).input(img, 25.0, 256).output(expected).compute();
		assertArrayEquals(expected.update(null).getCurrentStorageArray(), means
			.update(null).getCurrentStorageArray());
	}

	/** A constant image is a single region whose mean is the constant. */
	@Test
	public void testConstantImage() {
		final var img = ArrayImgs.doubles(7, 5, 3);
		for (final var t : img)
			t.set(-4.5);
		assertArrayEquals(new int[7 * 5 * 3], labels(labeling(img, null, null)));
		final var means = ArrayImgs.doubles(7, 5, 3);
		ops.op(OP).input(img, null, null).output(means).compute();
		for (final var t : means)
			assertEquals(-4.5, t.get(), 0);
	}

	/** Two flat halves separated by a strong step remain two regions, in 4D. */
	@Test
	public void testStep4D() {
		final var img = ArrayImgs.unsignedBytes(4, 3, 5, 6);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(c.getIntPosition(3) < 2 ? 10 : 200);
		}
		final var labels = labels(labeling(img, null, null));
		final var c2 = img.localizingCursor();
		for (final var label : labels) {
			c2.fwd();
			assertEquals(c2.getIntPosition(3) < 2 ? 0 : 1, label);
		}
		final var means = ArrayImgs.floats(4, 3, 5, 6);
		ops.op(OP).input(img, null, null).output(means).compute();
		final var m = means.cursor();
		for (final var t : img)
			assertEquals(t.get(), m.next().get(), 1e-4);
	}

	/**
	 * The segmentation is invariant under positive affine intensity
	 * transformations, since intensities are rescaled from their value range;
	 * the region means transform accordingly.
	 */
	@Test
	public void testAffineIntensityInvariance() {
		final var img = synth(1, 12, 10, 6);
		final var floats = ArrayImgs.floats(12, 10, 6);
		final var f = floats.cursor();
		for (final var t : img)
			f.next().set(t.get() * 0.25f - 3);
		assertArrayEquals(labels(labeling(img, 100.0, null)), labels(labeling(
			floats, 100.0, null)));

		final var means = ArrayImgs.doubles(12, 10, 6);
		ops.op(OP).input(img, 100.0, null).output(means).compute();
		final var floatMeans = ArrayImgs.doubles(12, 10, 6);
		ops.op(OP).input(floats, 100.0, null).output(floatMeans).compute();
		final var fm = floatMeans.cursor();
		for (final var t : means)
			assertEquals(t.get() * 0.25 - 3, fm.next().get(), 1e-9);
	}

	/** Larger Q never yields fewer regions on these inputs. */
	@Test
	public void testQMonotonicity() {
		final var img = synth(1, 24, 20, 1);
		var previous = 0;
		for (final var q : new double[] { 1, 5, 25, 100, 400, 1600 }) {
			final var regions = Arrays.stream(labels(labeling(img, q, null))).max()
				.getAsInt() + 1;
			assertTrue(regions >= previous, "Q = " + q);
			previous = regions;
		}
	}

	/** Inputs with a nonzero origin are processed in flat iteration order. */
	@Test
	public void testTranslatedInput() {
		final var img = synth(0, 24, 20, 1);
		final var translated = Views.translate(img, 5, -7);
		assertArrayEquals(labels(labeling(img, null, null)), labels(labeling(
			translated, null, null)));
	}

	@Test
	public void testInvalidParameters() {
		final var img = synth(0, 4, 4, 1);
		assertThrows(IllegalArgumentException.class, () -> labeling(img, 0.0,
			null));
		assertThrows(IllegalArgumentException.class, () -> labeling(img, null,
			1));
	}

	// -- Helper methods --

	private <T extends RealType<T>> ImgLabeling<Integer, IntType> labeling(
		final RandomAccessibleInterval<T> img, final Double q, final Integer g)
	{
		return ops.op(OP).input(img, q, g).outType(
			new Nil<ImgLabeling<Integer, IntType>>()
			{}).apply();
	}

	/** Extracts the single label of each sample, in flat iteration order. */
	private static int[] labels(final ImgLabeling<Integer, IntType> labeling) {
		final var labels = new int[(int) Views.iterable(labeling).size()];
		var i = 0;
		for (final var set : Views.flatIterable(labeling)) {
			assertEquals(1, set.size());
			labels[i++] = set.iterator().next();
		}
		return labels;
	}

	/** Checks that each output value is the input mean over its region. */
	private static void assertMeansMatchRegions(final Img<UnsignedByteType> img,
		final int[] labels, final Img<FloatType> means)
	{
		final var regionCount = Arrays.stream(labels).max().getAsInt() + 1;
		final var sums = new double[regionCount];
		final var counts = new int[regionCount];
		var i = 0;
		for (final var t : Views.flatIterable(img)) {
			sums[labels[i]] += t.get();
			counts[labels[i]]++;
			i++;
		}
		i = 0;
		for (final var t : Views.flatIterable(means)) {
			final var label = labels[i++];
			assertEquals(sums[label] / counts[label], t.get(), 1e-3);
		}
	}

	/**
	 * Generates the synthetic 8-bit test image also used by the regression
	 * harness: mode 0 is a piecewise constant pattern with mild noise; mode 1 is
	 * a smooth sinusoidal pattern with stronger noise. The first and last
	 * samples are set to 0 and 255, so that the value range is the full 8-bit
	 * range.
	 */
	private static Img<UnsignedByteType> synth(final int mode, final int w,
		final int h, final int d)
	{
		final var p = new byte[w * h * d];
		var seed = 42;
		for (var z = 0; z < d; z++)
			for (var y = 0; y < h; y++)
				for (var x = 0; x < w; x++) {
					seed = seed * 1103515245 + 12345;
					final int noise;
					final int base;
					if (mode == 0) {
						noise = ((seed >>> 16) % 31) - 15;
						base = ((x / 6 + y / 5 + z / 3) % 3) * 90 + 30;
					}
					else {
						noise = ((seed >>> 16) % 51) - 25;
						base = (int) (127 + 100 * StrictMath.sin(x / 3.0) * StrictMath
							.cos((y + z) / 4.0));
					}
					p[x + w * (y + h * z)] = (byte) Math.max(0, Math.min(255, base +
						noise));
				}
		p[0] = 0;
		p[p.length - 1] = (byte) 255;
		return d == 1 ? ArrayImgs.unsignedBytes(p, w, h) : ArrayImgs
			.unsignedBytes(p, w, h, d);
	}

	/** NaN has no order, so images containing NaN are rejected. */
	@Test
	public void testNaN() {
		final var img = ArrayImgs.floats(8, 8);
		img.firstElement().set(Float.NaN);
		assertThrows(IllegalArgumentException.class, () -> ops.op(OP).input(img)
			.outType(new Nil<ImgLabeling<Integer, IntType>>()
			{}).apply());
	}
}
