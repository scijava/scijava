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

package org.scijava.ops.image.color.deconvolve;

import java.util.Locale;

/**
 * Published optical density (OD) vectors of common histological stain
 * combinations, for use with {@code color.deconvolveStains}.
 * <p>
 * Each stain is a vector of its relative absorbance in the red, green and blue
 * channels. The hematoxylin, eosin and DAB vectors are those determined by
 * Ruifrok and Johnston (2001); note that {@link #H_E} therefore uses the
 * published hematoxylin and eosin vectors, not the measured "H&amp;E" set of
 * ImageJ's Colour Deconvolution plugin. The other combinations are the
 * measured values distributed with G. Landini's Colour Deconvolution plugin
 * for ImageJ. Combinations of two stains leave the third vector unspecified;
 * it is then completed as described in {@link ColourDeconvolution#stainMatrix}.
 * </p>
 * <p>
 * Stain vectors depend on the staining protocol and the imaging system, so for
 * quantitative work it is best to measure them from single-stained samples and
 * pass them to {@code color.deconvolveStains} directly.
 * </p>
 *
 * @author Curtis Rueden
 * @see <a href="https://pubmed.ncbi.nlm.nih.gov/11531144/">A. C. Ruifrok and
 *      D. A. Johnston, "Quantification of histochemical staining by color
 *      deconvolution", Anal. Quant. Cytol. Histol. 23(4):291-299, 2001</a>
 * @see <a href="https://blog.bham.ac.uk/intellimic/g-landini-software/colour-deconvolution-2/">Colour
 *      Deconvolution (G. Landini)</a>
 */
public enum StainVectors {

	/** Hematoxylin and eosin. */
	H_E("H&E", Stain.HEMATOXYLIN, Stain.EOSIN),

	/** Hematoxylin and DAB (3,3-diaminobenzidine). */
	H_DAB("H DAB", Stain.HEMATOXYLIN, Stain.DAB),

	/** Hematoxylin, eosin and DAB. */
	H_E_DAB("H&E DAB", Stain.HEMATOXYLIN, Stain.EOSIN, Stain.DAB),

	/** Hematoxylin and AEC (3-amino-9-ethylcarbazole). */
	H_AEC("H AEC", Stain.HEMATOXYLIN, //
		new double[] { 0.2743, 0.6796, 0.6803 }),

	/** Hematoxylin and periodic acid-Schiff. */
	H_PAS("H PAS", //
		new double[] { 0.644211, 0.716556, 0.266844 }, //
		new double[] { 0.175411, 0.972178, 0.154589 }),

	/** Alcian blue and hematoxylin. */
	ALCIAN_BLUE_H("Alcian blue & H", //
		new double[] { 0.874622, 0.457711, 0.158256 }, //
		new double[] { 0.552556, 0.7544, 0.353744 }),

	/** Feulgen and light green. */
	FEULGEN_LIGHT_GREEN("Feulgen Light Green", //
		new double[] { 0.46420921, 0.83008335, 0.30827187 }, //
		new double[] { 0.94705542, 0.25373821, 0.19650764 }),

	/** Giemsa: methylene blue and eosin. */
	GIEMSA("Giemsa", //
		new double[] { 0.834750233, 0.513556283, 0.196330403 }, //
		new double[] { 0.092789, 0.954111, 0.283111 }),

	/** Fast red, fast blue and DAB. */
	FASTRED_FASTBLUE_DAB("FastRed FastBlue DAB", //
		new double[] { 0.21393921, 0.85112669, 0.47794022 }, //
		new double[] { 0.74890292, 0.60624161, 0.26731082 }, //
		Stain.DAB),

	/** Methyl green and DAB. */
	METHYL_GREEN_DAB("Methyl Green DAB", //
		new double[] { 0.98, 0.144316, 0.133146 }, Stain.DAB),

	/** Azan-Mallory: aniline blue, azocarmine and orange G. */
	AZAN_MALLORY("Azan-Mallory", //
		new double[] { 0.853033, 0.508733, 0.112656 }, //
		new double[] { 0.09289875, 0.8662008, 0.49098468 }, //
		new double[] { 0.10732849, 0.36765403, 0.9237484 }),

	/** Masson trichrome: methyl blue and ponceau fuchsin. */
	MASSON_TRICHROME("Masson Trichrome", //
		new double[] { 0.7995107, 0.5913521, 0.10528667 }, //
		new double[] { 0.09997159, 0.73738605, 0.6680326 });

	/** Single-stain vectors of Ruifrok and Johnston (2001). */
	private static final class Stain {

		private static final double[] HEMATOXYLIN = { 0.650, 0.704, 0.286 };
		private static final double[] EOSIN = { 0.072, 0.990, 0.105 };
		private static final double[] DAB = { 0.268, 0.570, 0.776 };
	}

	private final String label;
	private final double[][] vectors;

	StainVectors(final String label, final double[]... vectors) {
		this.label = label;
		this.vectors = vectors;
	}

	/**
	 * Gets the human-readable name of the stain combination.
	 *
	 * @return the name, e.g. {@code "H&E"}
	 */
	public String label() {
		return label;
	}

	/**
	 * Gets a copy of the stain OD vectors.
	 *
	 * @return one row of length 3 (red, green, blue) per stain; there are two or
	 *         three rows
	 */
	public double[][] vectors() {
		final var copy = new double[vectors.length][];
		for (var i = 0; i < vectors.length; i++)
			copy[i] = vectors[i].clone();
		return copy;
	}

	/**
	 * Looks up a stain combination by name. Matching ignores case and all
	 * characters other than letters and digits, and accepts both the
	 * {@link #label()} (e.g. {@code "H&E"}, {@code "H DAB"}) and the constant
	 * name (e.g. {@code "H_E"}).
	 *
	 * @param name the name of the stain combination
	 * @return the matching stain combination
	 * @throws IllegalArgumentException if no stain combination matches
	 */
	public static StainVectors fromName(final String name) {
		final var key = normalize(name);
		for (final var s : values()) {
			if (normalize(s.label).equals(key) || normalize(s.name()).equals(key)) {
				return s;
			}
		}
		final var sb = new StringBuilder();
		for (final var s : values())
			sb.append(sb.length() == 0 ? "" : ", ").append(s.label);
		throw new IllegalArgumentException("Unknown stain combination: " + name +
			" (known: " + sb + ")");
	}

	private static String normalize(final String s) {
		return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}
}
