/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.directionality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.gpl.AbstractOpTest;

/**
 * Tests {@link DefaultDirectionality}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DirectionalityTest extends AbstractOpTest {

	/**
	 * Synthetic test images; must match the harness that produced
	 * {@code directionality-expected.txt}.
	 */
	private static float value(final int kind, final int x, final int y) {
		final double noise = (((x * 73856093) ^ (y * 19349663)) & 1023) / 1023.0;
		switch (kind) {
			case 0: { // grating with structures oriented at 30 degrees
				final double a = Math.toRadians(30);
				final double t = -x * Math.sin(a) - y * Math.cos(a);
				return (float) (100 + 60 * Math.sin(2 * Math.PI * t / 6) + 20 * noise);
			}
			case 1: { // non-square: -50 degree grating plus a weaker 20 degree one
				final double a = Math.toRadians(-50), b = Math.toRadians(20);
				final double t = -x * Math.sin(a) - y * Math.cos(a);
				final double s = -x * Math.sin(b) - y * Math.cos(b);
				return (float) (100 + 60 * Math.sin(2 * Math.PI * t / 7) + 25 * Math
					.sin(2 * Math.PI * s / 5) + 30 * noise);
			}
			default: // noise only
				return (float) (255 * noise);
		}
	}

	private static final int[][] SIZES = { { 64, 64 }, { 80, 48 }, { 40, 40 } };

	private static Img<FloatType> image(final int kind) {
		final int w = SIZES[kind][0], h = SIZES[kind][1];
		final float[] data = new float[w * h];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				data[x + y * w] = value(kind, x, y);
		return ArrayImgs.floats(data, w, h);
	}

	/** A sinusoidal grating whose stripes are oriented at the given angle. */
	private static Img<FloatType> grating(final double degrees, final int size,
		final double period)
	{
		final double a = Math.toRadians(degrees);
		final float[] data = new float[size * size];
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				final double t = -x * Math.sin(a) - y * Math.cos(a);
				data[x + y * size] = (float) (100 + 50 * Math.sin(2 * Math.PI * t /
					period));
			}
		}
		return ArrayImgs.floats(data, size, size);
	}

	private Directionality directionality(final Img<FloatType> img,
		final DirectionalityMethod method, final int nBins, final double start,
		final double end)
	{
		return ops.op("features.directionality").input(img, method, nBins, start,
			end).outType(Directionality.class).apply();
	}

	/**
	 * Compares with the Fiji Directionality plugin (v2.3.0, compiled from
	 * source), run headless on the same synthetic images by the harness in
	 * {@code .claude/fiji-plugin-ops/g2/src/DirHarness.java}, through its
	 * {@code computeHistograms}, {@code fitHistograms}, {@code getHistograms},
	 * {@code getFitParameters} and {@code getFitAnalysis} methods.
	 */
	@Test
	public void testRegression() throws IOException {
		final List<String> lines = new ArrayList<>();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(
			getClass().getResourceAsStream("directionality-expected.txt"),
			StandardCharsets.UTF_8)))
		{
			for (String line; (line = in.readLine()) != null;)
				lines.add(line);
		}
		int cases = 0;
		for (int l = 0; l < lines.size(); l += 3) {
			final String[] c = lines.get(l).split(" ");
			final String[] fit = lines.get(l + 1).split(" ");
			final String[] hist = lines.get(l + 2).split(" ");
			final int kind = Integer.parseInt(c[1]);
			final DirectionalityMethod method = DirectionalityMethod.valueOf(c[2]);
			final int nBins = Integer.parseInt(c[3]);
			final double start = Double.parseDouble(c[4]);
			final double end = Double.parseDouble(c[5]);
			final String what = lines.get(l);

			final Directionality d = directionality(image(kind), method, nBins,
				start, end);
			assertEquals(nBins, d.binCount(), what);
			final double[] h = d.histogram();
			if (method == DirectionalityMethod.LOCAL_GRADIENT_ORIENTATION) {
				// Note: The op bins by rounding where the plugin truncates, so the
				// plugin histogram is offset by half a bin and only its fitted
				// direction can be compared, shifted by that half bin.
				double sum = 0;
				for (final double v : h)
					sum += v;
				assertEquals(1, sum, 1e-9, what);
				if (kind == 2) continue;
				final double binWidth = (end - start) / (nBins - 1);
				assertEquals(Double.parseDouble(fit[3]) + binWidth / 2, d.direction(),
					binWidth / 4, what);
				cases++;
				continue;
			}
			for (int i = 0; i < nBins; i++) {
				final double expected = Double.parseDouble(hist[i + 1]);
				assertEquals(expected, h[i], 1e-4 * expected + 1e-9, what +
					" bin " + i);
			}
			assertEquals(start, d.binCenters()[0], 1e-12, what);
			assertEquals(end, d.binCenters()[nBins - 1], 1e-12, what);
			// Note: The noise image has no clear peak, so its fit is not compared.
			if (kind == 2) continue;
			assertEquals(Double.parseDouble(fit[1]), d.offset(), 1e-5, what);
			assertEquals(Double.parseDouble(fit[2]), d.peak(), 1e-5, what);
			assertEquals(Double.parseDouble(fit[3]), d.direction(), 1e-3, what);
			assertEquals(Double.parseDouble(fit[4]), d.dispersion(), 1e-3, what);
			// Note: Unlike the plugin, the amount wraps around the ends of the
			// range, so compare the plugin amount with the unwrapped sum.
			double unwrapped = 0;
			final double[] bins = d.binCenters();
			for (int i = 0; i < nBins; i++) {
				if (Math.abs(bins[i] - d.direction()) <= 2 * d.dispersion())
					unwrapped += h[i];
			}
			assertEquals(Double.parseDouble(fit[5]), unwrapped, 1e-6, what);
			assertTrue(d.amount() >= unwrapped - 1e-12, what);
			assertEquals(Double.parseDouble(fit[6]), d.goodness(), 1e-5, what);
			cases++;
		}
		assertEquals(8, cases);
	}

	/**
	 * The local gradient method is not biased by half a bin. Before the fix,
	 * each bin collected the orientations in {@code [center, center + width)},
	 * so over these gratings the direction was off by -1.0 degrees on average
	 * with 90 bins and by -3.1 degrees with 30 bins, half a bin width each time.
	 */
	@Test
	public void testGradientBinningUnbiased() {
		for (final int nBins : new int[] { 90, 30 }) {
			double sum = 0;
			int n = 0;
			for (double angle = -70; angle <= 70; angle += 7.3) {
				final Directionality d = directionality(grating(angle, 128, 12),
					DirectionalityMethod.LOCAL_GRADIENT_ORIENTATION, nBins, -90, 90);
				sum += d.direction() - angle;
				n++;
			}
			final double halfBin = 90.0 / (nBins - 1);
			assertEquals(0, sum / n, halfBin / 4, "nBins=" + nBins);
		}
	}

	/** Gratings at known orientations give their orientation as direction. */
	@Test
	public void testGratings() {
		for (final DirectionalityMethod method : DirectionalityMethod.values()) {
			// Note: The Fourier filters select periods near a quarter of the block
			// size; the 5x5 gradient kernels prefer coarser structures.
			final double period = method == DirectionalityMethod.FOURIER_COMPONENTS
				? 4.3 : 10;
			final double tolerance = method == DirectionalityMethod.FOURIER_COMPONENTS
				? 1 : 3.5;
			for (final double angle : new double[] { -75, -60, -20, 0, 15, 45, 70 }) {
				final Directionality d = directionality(grating(angle, 64, period),
					method, 90, -90, 90);
				assertEquals(angle, d.direction(), tolerance, method + " " + angle);
				// The histogram peaks in the bin of the grating orientation.
				final double[] h = d.histogram();
				int max = 0;
				double sum = 0;
				for (int i = 0; i < h.length; i++) {
					if (h[i] > h[max]) max = i;
					sum += h[i];
				}
				assertEquals(1, sum, 1e-9);
				final double binWidth = 180.0 / 89;
				assertEquals(angle, d.binCenters()[max], tolerance + binWidth, method +
					" " + angle);
			}
		}
	}

	/** The default parameters are the Fourier method over 90 bins. */
	@Test
	public void testDefaults() {
		final Img<FloatType> img = grating(30, 32, 4);
		final Directionality d = ops.op("features.directionality").input(img)
			.outType(Directionality.class).apply();
		assertEquals(90, d.binCount());
		assertEquals(-90, d.binCenters()[0], 1e-12);
		assertEquals(90, d.binCenters()[89], 1e-12);
		final Directionality explicit = directionality(img,
			DirectionalityMethod.FOURIER_COMPONENTS, 90, -90, 90);
		assertEquals(explicit.direction(), d.direction(), 0);
		assertEquals(30, d.direction(), 2.5);
	}

	@Test
	public void testInvalidInput() {
		final Img<FloatType> img3D = ArrayImgs.floats(8, 8, 2);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"features.directionality").input(img3D).outType(Directionality.class)
			.apply());
	}
}
