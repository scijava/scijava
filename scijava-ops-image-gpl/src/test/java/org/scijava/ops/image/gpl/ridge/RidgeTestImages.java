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

package org.scijava.ops.image.gpl.ridge;

/**
 * Synthetic line images and detection parameters shared by
 * {@link DetectLinesTest} and the harness that ran the original plugin to
 * produce {@code ridge-expected.txt}; this class must not depend on anything
 * but the JDK.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class RidgeTestImages {

	private RidgeTestImages() {
		// Prevent instantiation of utility class.
	}

	public static final int W = 64;
	public static final int H = 48;

	/** Image kinds. */
	public static final int GAUSS = 0, DARK_BAR = 1, ASYMMETRIC = 2, CROSS = 3,
			RING_T = 4;

	/** Detection parameters of one test case. */
	public static final class Case {

		public final int image;
		public final double sigma, low, high, minLength, maxLength;
		public final boolean dark, width, correct, extend, slope;

		Case(final int image, final double sigma, final double low,
			final double high, final boolean dark, final boolean width,
			final boolean correct, final boolean extend, final double minLength,
			final double maxLength, final boolean slope)
		{
			this.image = image;
			this.sigma = sigma;
			this.low = low;
			this.high = high;
			this.dark = dark;
			this.width = width;
			this.correct = correct;
			this.extend = extend;
			this.minLength = minLength;
			this.maxLength = maxLength;
			this.slope = slope;
		}
	}

	public static final Case[] CASES = { //
		new Case(GAUSS, 1.5, 3, 8, false, true, false, true, 0, 0, false), //
		new Case(GAUSS, 1.5, 3, 8, false, false, false, false, 0, 0, false), //
		new Case(DARK_BAR, 1.94, 4, 8, true, true, true, true, 0, 0, false), //
		new Case(ASYMMETRIC, 1.94, 4, 8, false, true, false, true, 0, 0, false), //
		new Case(ASYMMETRIC, 1.94, 4, 8, false, true, true, true, 0, 0, false), //
		new Case(CROSS, 1.4, 4, 10, false, true, true, true, 0, 0, false), //
		new Case(CROSS, 1.4, 4, 10, false, true, true, true, 0, 0, true), //
		new Case(CROSS, 1.4, 4, 10, false, false, false, true, 0, 0, true), //
		new Case(CROSS, 1.4, 4, 10, false, true, false, true, 0, 0, true), //
		new Case(RING_T, 1.4, 4, 10, false, true, false, true, 0, 0, false), //
		new Case(RING_T, 1.4, 4, 10, false, true, false, true, 10, 40, false), //
	};

	/** Small deterministic noise in [0, 1). */
	private static double noise(final int x, final int y) {
		return (((x * 73856093) ^ (y * 19349663)) & 1023) / 1023.0;
	}

	/** Anti-aliased bar profile of the given width at distance d. */
	private static double bar(final double d, final double width) {
		return Math.max(0, Math.min(1, width / 2 + 0.5 - Math.abs(d)));
	}

	/** Distance of (x, y) to the line through (cx, cy) at angle a. */
	private static double dist(final double x, final double y, final double cx,
		final double cy, final double a)
	{
		return -(x - cx) * Math.sin(a) + (y - cy) * Math.cos(a);
	}

	/**
	 * @param kind the image kind
	 * @return the pixels of the image, row by row
	 */
	public static float[] image(final int kind) {
		final float[] px = new float[W * H];
		for (int y = 0; y < H; y++) {
			for (int x = 0; x < W; x++) {
				final double v;
				switch (kind) {
					case GAUSS: {
						// Gaussian profile (s = 1.2) along a sine curve.
						final double t = 2 * Math.PI / 40;
						final double cy = 24 + 6 * Math.sin(t * x);
						final double slope = 6 * t * Math.cos(t * x);
						final double d = (y - cy) / Math.sqrt(1 + slope * slope);
						v = 30 + 120 * Math.exp(-d * d / (2 * 1.2 * 1.2));
						break;
					}
					case DARK_BAR: {
						// Dark bar of width 5 at 30 degrees on a bright background.
						final double d = dist(x, y, 32, 24, Math.toRadians(30));
						v = 200 - 140 * bar(d, 5);
						break;
					}
					case ASYMMETRIC: {
						// Vertical bright bar of width 5 between backgrounds 40 and 110.
						final double d = x - 32.0;
						final double bg = d < 0 ? 40 : 110;
						v = bg + (200 - bg) * bar(d, 5);
						break;
					}
					case CROSS: {
						// Two bright bars of width 3 crossing at (32, 24).
						final double b1 = bar(dist(x, y, 32, 24, Math.toRadians(20)), 3);
						final double b2 = bar(dist(x, y, 32, 24, Math.toRadians(110)), 3);
						v = 20 + 150 * Math.max(b1, b2);
						break;
					}
					default: {
						// A ring (radius 12) and a horizontal bar touching it (T).
						final double r = Math.hypot(x - 22, y - 24);
						final double ring = bar(r - 12, 3);
						final double stem = x >= 34 && x <= 58 ? bar(y - 24, 3) : 0;
						v = 20 + 150 * Math.max(ring, stem);
						break;
					}
				}
				px[y * W + x] = (float) (v + 3 * noise(x, y));
			}
		}
		return px;
	}
}
