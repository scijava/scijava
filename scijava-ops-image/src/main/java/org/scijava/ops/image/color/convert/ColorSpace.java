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

package org.scijava.ops.image.color.convert;

import java.util.Locale;

/**
 * The colour spaces supported by {@code color.convert}, with conversions to
 * and from CIE XYZ.
 * <p>
 * All conversions use the sRGB standard (IEC 61966-2-1:1999): its primaries,
 * its D65 reference white and its transfer function. The RGB to XYZ matrix is
 * the one given in the standard, and the reference white used for CIELAB and
 * CIELUV is the XYZ of sRGB white under that matrix,
 * {@code (0.9505, 1, 1.0890)}, so that RGB white maps exactly to
 * {@code L = 100} with zero chroma. Conversions are not clipped: colours
 * outside the sRGB gamut yield RGB components outside {@code [0, 1]} (the
 * transfer function is extended to negative values by odd symmetry), and all
 * conversions are inverses of one another up to rounding.
 * </p>
 * <p>
 * Value ranges, for in-gamut colours:
 * </p>
 * <ul>
 * <li>{@link #RGB}: gamma-encoded sRGB R', G', B' in {@code [0, 1]}.</li>
 * <li>{@link #LINEAR_RGB}: linear-light sRGB R, G, B in {@code [0, 1]}.</li>
 * <li>{@link #HSB} (also called HSV): hue in degrees within
 * {@code [0, 360)}, saturation and brightness (value) in {@code [0, 1]},
 * computed from gamma-encoded RGB.</li>
 * <li>{@link #XYZ}: CIE 1931 XYZ, with {@code Y = 1} for the reference
 * white.</li>
 * <li>{@link #LAB}: CIE 1976 L*a*b*, with L* in {@code [0, 100]}.</li>
 * <li>{@link #LUV}: CIE 1976 L*u*v*, with L* in {@code [0, 100]}.</li>
 * <li>{@link #YCBCR}: full-range YCbCr from gamma-encoded RGB with the ITU-R
 * BT.601 luma coefficients (as in JPEG/JFIF), Y' in {@code [0, 1]} and Cb, Cr
 * in {@code [-0.5, 0.5]} (no offset).</li>
 * </ul>
 * <p>
 * The CIELAB and CIELUV formulas are those of CIE 15:2004, with the exact
 * constants {@code ε = 216/24389} and {@code κ = 24389/27}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public enum ColorSpace {

		/** Gamma-encoded sRGB. */
		RGB("rgb", "srgb"),

		/** Linear-light sRGB. */
		LINEAR_RGB("linearrgb", "linearsrgb", "linear"),

		/** Hue, saturation and brightness (value), from gamma-encoded sRGB. */
		HSB("hsb", "hsv"),

		/** CIE 1931 XYZ. */
		XYZ("xyz", "ciexyz"),

		/** CIE 1976 L*a*b*. */
		LAB("lab", "cielab"),

		/** CIE 1976 L*u*v*. */
		LUV("luv", "cieluv"),

		/** Full-range ITU-R BT.601 YCbCr, from gamma-encoded sRGB. */
		YCBCR("ycbcr");

	/** RGB to XYZ matrix of IEC 61966-2-1. */
	private static final double[][] RGB_TO_XYZ = { //
		{ 0.4124, 0.3576, 0.1805 }, //
		{ 0.2126, 0.7152, 0.0722 }, //
		{ 0.0193, 0.1192, 0.9505 } };

	private static final double[][] XYZ_TO_RGB = invert(RGB_TO_XYZ);

	/** X of the reference white (sRGB white). */
	public static final double XN = RGB_TO_XYZ[0][0] + RGB_TO_XYZ[0][1] +
		RGB_TO_XYZ[0][2];

	/** Y of the reference white (sRGB white). */
	public static final double YN = RGB_TO_XYZ[1][0] + RGB_TO_XYZ[1][1] +
		RGB_TO_XYZ[1][2];

	/** Z of the reference white (sRGB white). */
	public static final double ZN = RGB_TO_XYZ[2][0] + RGB_TO_XYZ[2][1] +
		RGB_TO_XYZ[2][2];

	private static final double UN = 4 * XN / (XN + 15 * YN + 3 * ZN);
	private static final double VN = 9 * YN / (XN + 15 * YN + 3 * ZN);

	private static final double EPSILON = 216.0 / 24389;
	private static final double KAPPA = 24389.0 / 27;

	private static final double KR = 0.299, KB = 0.114, KG = 1 - KR - KB;

	private final String[] names;

	ColorSpace(final String... names) {
		this.names = names;
	}

	/**
	 * Looks up a colour space by name, ignoring case, spaces, dashes,
	 * underscores and asterisks. Accepted names are {@code RGB}/{@code sRGB},
	 * {@code linearRGB}, {@code HSB}/{@code HSV}, {@code XYZ}, {@code Lab},
	 * {@code Luv} and {@code YCbCr}, optionally prefixed by {@code CIE} for the
	 * CIE spaces.
	 *
	 * @param name the name of the colour space
	 * @return the colour space
	 * @throws IllegalArgumentException if the name is unknown
	 */
	public static ColorSpace byName(final String name) {
		if (name == null) throw new IllegalArgumentException(
			"Colour space name is null");
		final var key = name.toLowerCase(Locale.ROOT).replaceAll("[\\s_\\-*]",
			"");
		for (final var space : values()) {
			for (final var n : space.names) {
				if (n.equals(key)) return space;
			}
		}
		throw new IllegalArgumentException("Unknown colour space: " + name);
	}

	/**
	 * Whether the components of this space are RGB intensities, which are
	 * scaled by the RGB range of {@code color.convert}.
	 *
	 * @return true for {@link #RGB} and {@link #LINEAR_RGB}
	 */
	public boolean isRGB() {
		return this == RGB || this == LINEAR_RGB;
	}

	/**
	 * Converts a colour of this space to CIE XYZ.
	 *
	 * @param c the three components of the colour
	 * @param xyz the X, Y and Z values (output; may be the same array as
	 *          {@code c})
	 */
	public void toXYZ(final double[] c, final double[] xyz) {
		switch (this) {
			case XYZ:
				System.arraycopy(c, 0, xyz, 0, 3);
				break;
			case LINEAR_RGB:
				multiply(RGB_TO_XYZ, c, xyz);
				break;
			case RGB:
				decode(c, xyz);
				multiply(RGB_TO_XYZ, xyz, xyz);
				break;
			case HSB:
				hsbToRGB(c, xyz);
				RGB.toXYZ(xyz, xyz);
				break;
			case YCBCR:
				ycbcrToRGB(c, xyz);
				RGB.toXYZ(xyz, xyz);
				break;
			case LAB:
				labToXYZ(c, xyz);
				break;
			case LUV:
				luvToXYZ(c, xyz);
				break;
			default:
				throw new IllegalStateException(toString());
		}
	}

	/**
	 * Converts a CIE XYZ colour to this space.
	 *
	 * @param xyz the X, Y and Z values
	 * @param c the three components of the colour in this space (output; may be
	 *          the same array as {@code xyz})
	 */
	public void fromXYZ(final double[] xyz, final double[] c) {
		switch (this) {
			case XYZ:
				System.arraycopy(xyz, 0, c, 0, 3);
				break;
			case LINEAR_RGB:
				multiply(XYZ_TO_RGB, xyz, c);
				break;
			case RGB:
				multiply(XYZ_TO_RGB, xyz, c);
				encode(c, c);
				break;
			case HSB:
				RGB.fromXYZ(xyz, c);
				rgbToHSB(c, c);
				break;
			case YCBCR:
				RGB.fromXYZ(xyz, c);
				rgbToYCbCr(c, c);
				break;
			case LAB:
				xyzToLab(xyz, c);
				break;
			case LUV:
				xyzToLuv(xyz, c);
				break;
			default:
				throw new IllegalStateException(toString());
		}
	}

	/**
	 * Converts a colour from one space to another. Conversions among RGB, HSB
	 * and YCbCr are done directly; all others go through CIE XYZ.
	 *
	 * @param from the space of the input colour
	 * @param to the space of the output colour
	 * @param in the input components
	 * @param out the output components (may be the same array as {@code in})
	 */
	public static void convert(final ColorSpace from, final ColorSpace to,
		final double[] in, final double[] out)
	{
		if (from == to) {
			System.arraycopy(in, 0, out, 0, 3);
			return;
		}
		if (from.isGammaRGBBased() && to.isGammaRGBBased()) {
			from.toRGB(in, out);
			to.fromRGB(out, out);
			return;
		}
		from.toXYZ(in, out);
		to.fromXYZ(out, out);
	}

	private boolean isGammaRGBBased() {
		return this == RGB || this == HSB || this == YCBCR;
	}

	private void toRGB(final double[] in, final double[] rgb) {
		if (this == HSB) hsbToRGB(in, rgb);
		else if (this == YCBCR) ycbcrToRGB(in, rgb);
		else System.arraycopy(in, 0, rgb, 0, 3);
	}

	private void fromRGB(final double[] rgb, final double[] out) {
		if (this == HSB) rgbToHSB(rgb, out);
		else if (this == YCBCR) rgbToYCbCr(rgb, out);
		else System.arraycopy(rgb, 0, out, 0, 3);
	}

	// -- sRGB transfer function --

	private static double decode(final double v) {
		final var a = Math.abs(v);
		final var l = a <= 0.04045 ? a / 12.92 : Math.pow((a + 0.055) / 1.055,
			2.4);
		return Math.copySign(l, v);
	}

	private static double encode(final double v) {
		final var a = Math.abs(v);
		final var e = a <= 0.0031308 ? 12.92 * a : 1.055 * Math.pow(a, 1 / 2.4) -
			0.055;
		return Math.copySign(e, v);
	}

	private static void decode(final double[] in, final double[] out) {
		for (var i = 0; i < 3; i++)
			out[i] = decode(in[i]);
	}

	private static void encode(final double[] in, final double[] out) {
		for (var i = 0; i < 3; i++)
			out[i] = encode(in[i]);
	}

	// -- HSB --

	private static void rgbToHSB(final double[] rgb, final double[] hsb) {
		final double r = rgb[0], g = rgb[1], b = rgb[2];
		final var max = Math.max(r, Math.max(g, b));
		final var min = Math.min(r, Math.min(g, b));
		final var delta = max - min;
		double h;
		if (delta == 0) h = 0;
		else if (max == r) h = 60 * ((g - b) / delta);
		else if (max == g) h = 60 * ((b - r) / delta + 2);
		else h = 60 * ((r - g) / delta + 4);
		if (h < 0) h += 360;
		hsb[0] = h;
		hsb[1] = max == 0 ? 0 : delta / max;
		hsb[2] = max;
	}

	private static void hsbToRGB(final double[] hsb, final double[] rgb) {
		var h = hsb[0] % 360;
		if (h < 0) h += 360;
		final double s = hsb[1], v = hsb[2];
		final var sector = h / 60;
		final var i = (int) Math.floor(sector);
		final var f = sector - i;
		final var p = v * (1 - s);
		final var q = v * (1 - s * f);
		final var t = v * (1 - s * (1 - f));
		final double r, g, b;
		switch (i % 6) {
			case 0:
				r = v;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = v;
				b = p;
				break;
			case 2:
				r = p;
				g = v;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = v;
				break;
			case 4:
				r = t;
				g = p;
				b = v;
				break;
			default:
				r = v;
				g = p;
				b = q;
		}
		rgb[0] = r;
		rgb[1] = g;
		rgb[2] = b;
	}

	// -- YCbCr --

	private static void rgbToYCbCr(final double[] rgb, final double[] ycc) {
		final double r = rgb[0], g = rgb[1], b = rgb[2];
		final var y = KR * r + KG * g + KB * b;
		ycc[0] = y;
		ycc[1] = (b - y) / (2 * (1 - KB));
		ycc[2] = (r - y) / (2 * (1 - KR));
	}

	private static void ycbcrToRGB(final double[] ycc, final double[] rgb) {
		final var y = ycc[0];
		final var r = y + 2 * (1 - KR) * ycc[2];
		final var b = y + 2 * (1 - KB) * ycc[1];
		rgb[0] = r;
		rgb[1] = (y - KR * r - KB * b) / KG;
		rgb[2] = b;
	}

	// -- CIELAB and CIELUV --

	private static double f(final double t) {
		return t > EPSILON ? Math.cbrt(t) : (KAPPA * t + 16) / 116;
	}

	private static double fInverse(final double f) {
		final var f3 = f * f * f;
		return f3 > EPSILON ? f3 : (116 * f - 16) / KAPPA;
	}

	private static double yFromLightness(final double l) {
		return YN * (l > KAPPA * EPSILON ? Math.pow((l + 16) / 116, 3) : l /
			KAPPA);
	}

	private static void xyzToLab(final double[] xyz, final double[] lab) {
		final var fx = f(xyz[0] / XN);
		final var fy = f(xyz[1] / YN);
		final var fz = f(xyz[2] / ZN);
		lab[0] = 116 * fy - 16;
		lab[1] = 500 * (fx - fy);
		lab[2] = 200 * (fy - fz);
	}

	private static void labToXYZ(final double[] lab, final double[] xyz) {
		final double l = lab[0], a = lab[1], b = lab[2];
		final var fy = (l + 16) / 116;
		xyz[0] = XN * fInverse(fy + a / 500);
		xyz[1] = yFromLightness(l);
		xyz[2] = ZN * fInverse(fy - b / 200);
	}

	private static void xyzToLuv(final double[] xyz, final double[] luv) {
		final double x = xyz[0], y = xyz[1], z = xyz[2];
		final var l = 116 * f(y / YN) - 16;
		final var denom = x + 15 * y + 3 * z;
		luv[0] = l;
		if (denom == 0) {
			luv[1] = 0;
			luv[2] = 0;
			return;
		}
		luv[1] = 13 * l * (4 * x / denom - UN);
		luv[2] = 13 * l * (9 * y / denom - VN);
	}

	private static void luvToXYZ(final double[] luv, final double[] xyz) {
		final var l = luv[0];
		if (l == 0) {
			xyz[0] = xyz[1] = xyz[2] = 0;
			return;
		}
		final var u = luv[1] / (13 * l) + UN;
		final var v = luv[2] / (13 * l) + VN;
		final var y = yFromLightness(l);
		xyz[0] = y * 9 * u / (4 * v);
		xyz[1] = y;
		xyz[2] = y * (12 - 3 * u - 20 * v) / (4 * v);
	}

	// -- Linear algebra --

	private static void multiply(final double[][] m, final double[] v,
		final double[] out)
	{
		final double a = v[0], b = v[1], c = v[2];
		for (var i = 0; i < 3; i++)
			out[i] = m[i][0] * a + m[i][1] * b + m[i][2] * c;
	}

	private static double[][] invert(final double[][] m) {
		final var inv = new double[3][3];
		final var det = m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
			m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) + m[0][2] * (m[1][0] *
				m[2][1] - m[1][1] * m[2][0]);
		for (var i = 0; i < 3; i++) {
			for (var j = 0; j < 3; j++) {
				// Note: Cyclic indices give the signed cofactor of m[j][i].
				final int r1 = (j + 1) % 3, r2 = (j + 2) % 3;
				final int c1 = (i + 1) % 3, c2 = (i + 2) % 3;
				inv[i][j] = (m[r1][c1] * m[r2][c2] - m[r1][c2] * m[r2][c1]) / det;
			}
		}
		return inv;
	}
}
