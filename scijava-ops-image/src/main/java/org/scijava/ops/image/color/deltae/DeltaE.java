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

package org.scijava.ops.image.color.deltae;

import java.util.Locale;

/**
 * Colour difference formulas between two CIELAB colours, shared by the
 * {@code color.deltaE} ops.
 * <ul>
 * <li>{@link #CIE76}: the Euclidean distance in CIELAB (CIE 1976).</li>
 * <li>{@link #CIE94}: CIE 1994, with the graphic arts weights
 * {@code kL = 1, K1 = 0.045, K2 = 0.015}.</li>
 * <li>{@link #CIE94_TEXTILES}: CIE 1994, with the textile weights
 * {@code kL = 2, K1 = 0.048, K2 = 0.014}.</li>
 * <li>{@link #CIEDE2000}: CIEDE2000 (CIE 142-2001), with
 * {@code kL = kC = kH = 1}, as specified by Sharma, Wu and Dalal (2005).</li>
 * </ul>
 * <p>
 * CIE94 is not symmetric: the first colour is the reference, whose chroma
 * weights the chroma and hue differences.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @see <a href="https://doi.org/10.1002/col.20070">G. Sharma, W. Wu and E. N.
 *      Dalal, "The CIEDE2000 color-difference formula: Implementation notes,
 *      supplementary test data, and mathematical observations", Color Res.
 *      Appl. 30(1):21-30, 2005</a>
 */
public enum DeltaE {

		/** CIE 1976 colour difference. */
		CIE76("cie76", "cie1976", "de76"),

		/** CIE 1994 colour difference, graphic arts weights. */
		CIE94("cie94", "cie1994", "de94"),

		/** CIE 1994 colour difference, textile weights. */
		CIE94_TEXTILES("cie94textiles", "cie1994textiles", "de94textiles"),

		/** CIEDE2000 colour difference. */
		CIEDE2000("ciede2000", "cie2000", "de2000", "de00");

	private static final double POW25_7 = Math.pow(25, 7);

	private final String[] names;

	DeltaE(final String... names) {
		this.names = names;
	}

	/**
	 * Looks up a formula by name, ignoring case, spaces, dashes and
	 * underscores: {@code CIE76}, {@code CIE94}, {@code CIE94textiles} or
	 * {@code CIEDE2000}.
	 *
	 * @param name the name of the formula
	 * @return the formula
	 * @throws IllegalArgumentException if the name is unknown
	 */
	public static DeltaE byName(final String name) {
		if (name == null) throw new IllegalArgumentException(
			"Formula name is null");
		final var key = name.toLowerCase(Locale.ROOT).replaceAll("[\\s_\\-]", "");
		for (final var f : values()) {
			for (final var n : f.names) {
				if (n.equals(key)) return f;
			}
		}
		throw new IllegalArgumentException("Unknown colour difference formula: " +
			name);
	}

	/**
	 * Computes the colour difference between two CIELAB colours.
	 *
	 * @param l1 L* of the first (reference) colour
	 * @param a1 a* of the first colour
	 * @param b1 b* of the first colour
	 * @param l2 L* of the second colour
	 * @param a2 a* of the second colour
	 * @param b2 b* of the second colour
	 * @return the colour difference
	 */
	public double compute(final double l1, final double a1, final double b1,
		final double l2, final double a2, final double b2)
	{
		switch (this) {
			case CIE76:
				return cie76(l1, a1, b1, l2, a2, b2);
			case CIE94:
				return cie94(l1, a1, b1, l2, a2, b2, 1, 0.045, 0.015);
			case CIE94_TEXTILES:
				return cie94(l1, a1, b1, l2, a2, b2, 2, 0.048, 0.014);
			case CIEDE2000:
				return ciede2000(l1, a1, b1, l2, a2, b2);
			default:
				throw new IllegalStateException(toString());
		}
	}

	private static double cie76(final double l1, final double a1,
		final double b1, final double l2, final double a2, final double b2)
	{
		final double dl = l1 - l2, da = a1 - a2, db = b1 - b2;
		return Math.sqrt(dl * dl + da * da + db * db);
	}

	private static double cie94(final double l1, final double a1,
		final double b1, final double l2, final double a2, final double b2,
		final double kL, final double k1, final double k2)
	{
		final var c1 = Math.hypot(a1, b1);
		final var c2 = Math.hypot(a2, b2);
		final double dl = l1 - l2, da = a1 - a2, db = b1 - b2;
		final var dc = c1 - c2;
		final var dh2 = Math.max(0, da * da + db * db - dc * dc);
		final var tl = dl / kL;
		final var tc = dc / (1 + k1 * c1);
		final var sh = 1 + k2 * c1;
		return Math.sqrt(tl * tl + tc * tc + dh2 / (sh * sh));
	}

	private static double ciede2000(final double l1, final double a1,
		final double b1, final double l2, final double a2, final double b2)
	{
		final var cBar = (Math.hypot(a1, b1) + Math.hypot(a2, b2)) / 2;
		final var cBar7 = Math.pow(cBar, 7);
		final var g = 0.5 * (1 - Math.sqrt(cBar7 / (cBar7 + POW25_7)));
		final var ap1 = (1 + g) * a1;
		final var ap2 = (1 + g) * a2;
		final var cp1 = Math.hypot(ap1, b1);
		final var cp2 = Math.hypot(ap2, b2);
		final var hp1 = hueAngle(ap1, b1);
		final var hp2 = hueAngle(ap2, b2);

		final var dL = l2 - l1;
		final var dC = cp2 - cp1;
		final var chromaProduct = cp1 * cp2;
		double dh = 0;
		if (chromaProduct != 0) {
			dh = hp2 - hp1;
			if (dh > 180) dh -= 360;
			else if (dh < -180) dh += 360;
		}
		final var dH = 2 * Math.sqrt(chromaProduct) * Math.sin(Math.toRadians(dh /
			2));

		final var lBar = (l1 + l2) / 2;
		final var cpBar = (cp1 + cp2) / 2;
		double hBar = hp1 + hp2;
		if (chromaProduct != 0) {
			if (Math.abs(hp1 - hp2) <= 180) hBar /= 2;
			else if (hBar < 360) hBar = (hBar + 360) / 2;
			else hBar = (hBar - 360) / 2;
		}
		final var t = 1 - 0.17 * cosDeg(hBar - 30) + 0.24 * cosDeg(2 * hBar) +
			0.32 * cosDeg(3 * hBar + 6) - 0.20 * cosDeg(4 * hBar - 63);
		final var dTheta = 30 * Math.exp(-Math.pow((hBar - 275) / 25, 2));
		final var cpBar7 = Math.pow(cpBar, 7);
		final var rc = 2 * Math.sqrt(cpBar7 / (cpBar7 + POW25_7));
		final var l50 = (lBar - 50) * (lBar - 50);
		final var sl = 1 + 0.015 * l50 / Math.sqrt(20 + l50);
		final var sc = 1 + 0.045 * cpBar;
		final var sh = 1 + 0.015 * cpBar * t;
		final var rt = -Math.sin(Math.toRadians(2 * dTheta)) * rc;

		final var tl = dL / sl;
		final var tc = dC / sc;
		final var th = dH / sh;
		return Math.sqrt(tl * tl + tc * tc + th * th + rt * tc * th);
	}

	/** Hue angle in degrees within {@code [0, 360)}; 0 for a neutral colour. */
	private static double hueAngle(final double a, final double b) {
		if (a == 0 && b == 0) return 0;
		final var h = Math.toDegrees(Math.atan2(b, a));
		return h < 0 ? h + 360 : h;
	}

	private static double cosDeg(final double deg) {
		return Math.cos(Math.toRadians(deg));
	}
}
