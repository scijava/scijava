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

package org.scijava.ops.image.features.texture.ngtdm;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.image.features.texture.TextureUtils;

/**
 * Computes the neighborhood gray tone difference matrix (NGTDM) of an
 * n-dimensional region of interest, as defined by the Image Biomarker
 * Standardisation Initiative (IBSI). Row {@code i - 1} of the result describes
 * gray level {@code i} with three columns: {@code n_i}, the number of ROI
 * voxels with gray level {@code i}; {@code p_i}, the fraction of ROI voxels
 * with gray level {@code i}; and {@code s_i}, the sum over these voxels of the
 * absolute difference between {@code i} and the mean gray level of their
 * neighbors within the ROI (at Chebyshev distance up to {@code distance}).
 * Voxels without any neighbor in the ROI do not contribute to {@code s_i}. The
 * {@code features.ngtdm.*} Ops compute IBSI features from this matrix.
 * <p>
 * These Ops are based on {@code NGTDMFeatures.java} from RadiomicsJ
 * (https://github.com/tatsunidas/RadiomicsJ), Copyright 2022 Tatsuaki
 * Kobayashi, licensed under the Apache License, Version 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0). Unless required by applicable
 * law or agreed to in writing, software distributed under that License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 * </p>
 * <p>
 * References: Zwanenburg A, Vallieres M, et al. The Image Biomarker
 * Standardization Initiative: Standardized Quantitative Radiomics for
 * High-Throughput Image-based Phenotyping. Radiology. 2020;295(2):328-338.
 * doi:10.1148/radiol.2020191145.
 * Amadasun M, King R. Textural features corresponding to textural
 * properties. IEEE Transactions on Systems, Man, and Cybernetics.
 * 1989;19(5):1264-1274.
 * </p>
 *
 * @author Tatsuaki Kobayashi
 * @author Curtis Rueden
 */
public final class NeighborhoodGrayToneDifferenceMatrix {

	private NeighborhoodGrayToneDifferenceMatrix() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the neighborhood gray tone difference matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @return the matrix of [n_i, p_i, s_i] values, indexed by gray level
	 * @implNote op names='image.neighborhoodGrayToneDifferenceMatrix', type=Function
	 */
	public static <T extends RealType<T>> double[][]
		neighborhoodGrayToneDifferenceMatrix(final RandomAccessibleInterval<T> input)
	{
		return NeighborhoodGrayToneDifferenceMatrix.<T, BitType> neighborhoodGrayToneDifferenceMatrix(input, null, null, null);
	}

	/**
	 * Computes the neighborhood gray tone difference matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @return the matrix of [n_i, p_i, s_i] values, indexed by gray level
	 * @implNote op names='image.neighborhoodGrayToneDifferenceMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		neighborhoodGrayToneDifferenceMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask)
	{
		return neighborhoodGrayToneDifferenceMatrix(input, mask, null, null);
	}

	/**
	 * Computes the neighborhood gray tone difference matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @param nGrayLevels the number of gray levels into which the ROI
	 *          intensities are discretized (IBSI fixed bin number), or null to
	 *          use the input values, which must then be positive integers
	 * @return the matrix of [n_i, p_i, s_i] values, indexed by gray level
	 * @implNote op names='image.neighborhoodGrayToneDifferenceMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		neighborhoodGrayToneDifferenceMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask,
			final Integer nGrayLevels)
	{
		return neighborhoodGrayToneDifferenceMatrix(input, mask, nGrayLevels, null);
	}

	/**
	 * Computes the neighborhood gray tone difference matrix.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @param nGrayLevels the number of gray levels into which the ROI
	 *          intensities are discretized (IBSI fixed bin number), or null to
	 *          use the input values, which must then be positive integers
	 * @param distance the Chebyshev neighborhood distance (default 1)
	 * @return the matrix of [n_i, p_i, s_i] values, indexed by gray level
	 * @implNote op names='image.neighborhoodGrayToneDifferenceMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		neighborhoodGrayToneDifferenceMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask,
			final Integer nGrayLevels,
			final Integer distance)
	{
		final var g = TextureUtils.discretize(input, mask, nGrayLevels);
		final var delta = distance == null ? 1 : distance;
		if (delta < 1) throw new IllegalArgumentException(
			"Distance must be positive: " + delta);
		final var offsets = TextureUtils.neighborOffsets(g.numDimensions(), delta);
		final var matrix = new double[g.nLevels][3];
		for (var p = 0; p < g.levels.length; p++) {
			final var level = g.levels[p];
			if (level == 0) continue;
			var sum = 0.0;
			var count = 0;
			for (final var o : offsets) {
				final var q = g.level(g.offset(p, o));
				if (q == 0) continue;
				sum += q;
				count++;
			}
			matrix[level - 1][0]++;
			if (count > 0) matrix[level - 1][2] += Math.abs(level - sum / count);
		}
		for (final var row : matrix)
			row[1] = row[0] / g.count;
		return matrix;
	}
}
