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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

/**
 * Colour deconvolution after Ruifrok and Johnston (2001), shared by the
 * {@code color.deconvolveStains} ops.
 * <p>
 * By the Beer-Lambert law, the optical density {@code OD = -log10(I / I0)} of
 * each RGB channel is linear in the stain concentrations:
 * {@code OD = c · M}, where row {@code k} of the 3 &times; 3 matrix {@code M} is
 * the unit-length OD vector of stain {@code k}. The stain amounts of a pixel
 * are thus {@code c = OD · M}<sup>-1</sup>.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class ColourDeconvolution {

	/** Smallest transmittance allowed for non-integer images. */
	private static final double MIN_TRANSMITTANCE = 1e-6;

	private ColourDeconvolution() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Builds the normalized 3 &times; 3 stain matrix from two or three stain
	 * vectors. Each vector is scaled to unit length. If only two vectors are
	 * given, or the third one is all zero, the third stain is taken to be the
	 * unit vector orthogonal to the first two (their normalized cross product),
	 * so that it absorbs whatever the first two stains cannot explain.
	 *
	 * @param stains two or three rows of red, green and blue OD values
	 * @return the normalized stain matrix, one stain per row
	 */
	public static double[][] stainMatrix(final double[][] stains) {
		if (stains == null || stains.length < 2 || stains.length > 3) {
			throw new IllegalArgumentException(
				"Expected two or three stain vectors");
		}
		final var m = new double[3][];
		for (var k = 0; k < stains.length; k++) {
			if (stains[k] == null || stains[k].length != 3) {
				throw new IllegalArgumentException("Stain vector " + k +
					" must have three (red, green, blue) components");
			}
			m[k] = stains[k].clone();
		}
		for (var k = 0; k < 2; k++) {
			if (!normalize(m[k])) {
				throw new IllegalArgumentException("Stain vector " + k + " is zero");
			}
		}
		if (m[2] == null || !normalize(m[2])) {
			m[2] = new double[] { //
				m[0][1] * m[1][2] - m[0][2] * m[1][1], //
				m[0][2] * m[1][0] - m[0][0] * m[1][2], //
				m[0][0] * m[1][1] - m[0][1] * m[1][0] };
			if (!normalize(m[2])) {
				throw new IllegalArgumentException(
					"The first two stain vectors are parallel");
			}
		}
		return m;
	}

	/**
	 * Inverts a 3 &times; 3 matrix.
	 *
	 * @param m the matrix
	 * @return its inverse
	 * @throws IllegalArgumentException if the matrix is singular
	 */
	public static double[][] invert(final double[][] m) {
		final var c00 = m[1][1] * m[2][2] - m[1][2] * m[2][1];
		final var c01 = m[1][2] * m[2][0] - m[1][0] * m[2][2];
		final var c02 = m[1][0] * m[2][1] - m[1][1] * m[2][0];
		final var det = m[0][0] * c00 + m[0][1] * c01 + m[0][2] * c02;
		if (Math.abs(det) < 1e-12) {
			throw new IllegalArgumentException(
				"Stain vectors are linearly dependent");
		}
		final var inv = new double[3][3];
		inv[0][0] = c00 / det;
		inv[1][0] = c01 / det;
		inv[2][0] = c02 / det;
		inv[0][1] = (m[0][2] * m[2][1] - m[0][1] * m[2][2]) / det;
		inv[1][1] = (m[0][0] * m[2][2] - m[0][2] * m[2][0]) / det;
		inv[2][1] = (m[0][1] * m[2][0] - m[0][0] * m[2][1]) / det;
		inv[0][2] = (m[0][1] * m[1][2] - m[0][2] * m[1][1]) / det;
		inv[1][2] = (m[0][2] * m[1][0] - m[0][0] * m[1][2]) / det;
		inv[2][2] = (m[0][0] * m[1][1] - m[0][1] * m[1][0]) / det;
		return inv;
	}

	/**
	 * Computes the default incident light intensity: the maximum value of the
	 * pixel type for integer types, and the maximum intensity of the image
	 * otherwise.
	 *
	 * @param input the image
	 * @return the incident light intensity
	 */
	public static <T extends RealType<T>> double defaultI0(
		final RandomAccessibleInterval<T> input)
	{
		final var type = Util.getTypeFromInterval(input);
		if (type instanceof IntegerType) return type.getMaxValue();
		var max = Double.NEGATIVE_INFINITY;
		for (final var t : Views.iterable(input))
			max = Math.max(max, t.getRealDouble());
		return max;
	}

	/**
	 * Deconvolves the stains of an RGB image.
	 *
	 * @param input the RGB image
	 * @param stains two or three stain OD vectors (see {@link #stainMatrix})
	 * @param channelDim the dimension holding the red, green and blue channels
	 * @param i0 the incident (background) light intensity
	 * @param output the stain amounts (in OD units), with the same dimensions as
	 *          the input; index {@code k} of the channel dimension holds stain
	 *          {@code k}
	 */
	public static <T extends RealType<T>> void deconvolve(
		final RandomAccessibleInterval<T> input, final double[][] stains,
		final int channelDim, final double i0,
		final RandomAccessibleInterval<DoubleType> output)
	{
		if (channelDim < 0 || channelDim >= input.numDimensions() || input
			.dimension(channelDim) != 3)
		{
			throw new IllegalArgumentException("Channel dimension " + channelDim +
				" must exist and have size 3");
		}
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		if (!(i0 > 0)) {
			throw new IllegalArgumentException("I0 must be positive: " + i0);
		}
		final var inv = invert(stainMatrix(stains));
		final var integer = Util.getTypeFromInterval(input) instanceof IntegerType;
		final var minI = integer ? 1 : i0 * MIN_TRANSMITTANCE;

		final long cMin = input.min(channelDim);
		final long oMin = output.min(channelDim);
		LoopBuilder.setImages( //
			Views.hyperSlice(input, channelDim, cMin), //
			Views.hyperSlice(input, channelDim, cMin + 1), //
			Views.hyperSlice(input, channelDim, cMin + 2), //
			Views.hyperSlice(output, channelDim, oMin), //
			Views.hyperSlice(output, channelDim, oMin + 1), //
			Views.hyperSlice(output, channelDim, oMin + 2) //
		).multiThreaded().forEachPixel((r, g, b, c0, c1, c2) -> {
			final var odR = od(r.getRealDouble(), minI, i0);
			final var odG = od(g.getRealDouble(), minI, i0);
			final var odB = od(b.getRealDouble(), minI, i0);
			c0.set(odR * inv[0][0] + odG * inv[1][0] + odB * inv[2][0]);
			c1.set(odR * inv[0][1] + odG * inv[1][1] + odB * inv[2][1]);
			c2.set(odR * inv[0][2] + odG * inv[1][2] + odB * inv[2][2]);
		});
	}

	private static double od(final double v, final double minI,
		final double i0)
	{
		return -Math.log10(Math.max(v, minI) / i0);
	}

	private static boolean normalize(final double[] v) {
		final var len = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
		if (len == 0) return false;
		for (var i = 0; i < 3; i++)
			v[i] /= len;
		return true;
	}
}
