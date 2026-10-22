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

package org.scijava.ops.image.features.texture.ngldm;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.image.features.texture.TextureUtils;

/**
 * Computes the neighboring gray level dependence matrix (NGLDM) of an
 * n-dimensional region of interest, as defined by the Image Biomarker
 * Standardisation Initiative (IBSI). For each voxel of the ROI, the dependence
 * count is the number of neighbors within the ROI (at Chebyshev distance up to
 * {@code distance}) whose gray level differs from that of the voxel by at most
 * the coarseness {@code alpha}. Element {@code [i - 1][j - 1]} of the result
 * counts the voxels of gray level {@code i} with {@code j - 1} dependent
 * neighbors. Trailing columns without any entry are removed. The
 * {@code features.ngldm.*} Ops compute IBSI features from this matrix.
 * <p>
 * These Ops are adapted from {@code NGLDMFeatures.java} of RadiomicsJ
 * (https://github.com/tatsunidas/RadiomicsJ) by Tatsuaki Kobayashi, used
 * under the Apache License, Version 2.0. The code was modified: it was
 * translated to n-dimensional ImgLib2 ops. The original license notice
 * follows.
 * </p>
 *
 * <pre>
 * Copyright 2022 Tatsuaki Kobayashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </pre>
 * <p>
 * References: Zwanenburg A, Vallieres M, et al. The Image Biomarker
 * Standardization Initiative: Standardized Quantitative Radiomics for
 * High-Throughput Image-based Phenotyping. Radiology. 2020;295(2):328-338.
 * doi:10.1148/radiol.2020191145.
 * Sun C, Wee WG. Neighboring gray level dependence matrix for texture
 * classification. Computer Vision, Graphics, and Image Processing.
 * 1983;23(3):341-352.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Tatsuaki Kobayashi
 */
public final class NeighboringGrayLevelDependenceMatrix {

	private NeighboringGrayLevelDependenceMatrix() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the neighboring gray level dependence matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @return the dependence matrix, indexed by gray level and dependence count + 1
	 * @implNote op names='image.neighboringGrayLevelDependenceMatrix', type=Function
	 */
	public static <T extends RealType<T>> double[][]
		neighboringGrayLevelDependenceMatrix(final RandomAccessibleInterval<T> input)
	{
		return NeighboringGrayLevelDependenceMatrix.<T, BitType> neighboringGrayLevelDependenceMatrix(input, null, null, null, null);
	}

	/**
	 * Computes the neighboring gray level dependence matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @return the dependence matrix, indexed by gray level and dependence count + 1
	 * @implNote op names='image.neighboringGrayLevelDependenceMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		neighboringGrayLevelDependenceMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask)
	{
		return neighboringGrayLevelDependenceMatrix(input, mask, null, null, null);
	}

	/**
	 * Computes the neighboring gray level dependence matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @param nGrayLevels the number of gray levels into which the ROI
	 *          intensities are discretized (IBSI fixed bin number), or null to
	 *          use the input values, which must then be positive integers
	 * @return the dependence matrix, indexed by gray level and dependence count + 1
	 * @implNote op names='image.neighboringGrayLevelDependenceMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		neighboringGrayLevelDependenceMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask,
			final Integer nGrayLevels)
	{
		return neighboringGrayLevelDependenceMatrix(input, mask, nGrayLevels, null, null);
	}

	/**
	 * Computes the neighboring gray level dependence matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @param nGrayLevels the number of gray levels into which the ROI
	 *          intensities are discretized (IBSI fixed bin number), or null to
	 *          use the input values, which must then be positive integers
	 * @param distance the Chebyshev neighborhood distance (default 1)
	 * @return the dependence matrix, indexed by gray level and dependence count + 1
	 * @implNote op names='image.neighboringGrayLevelDependenceMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		neighboringGrayLevelDependenceMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask,
			final Integer nGrayLevels,
			final Integer distance)
	{
		return neighboringGrayLevelDependenceMatrix(input, mask, nGrayLevels, distance, null);
	}

	/**
	 * Computes the neighboring gray level dependence matrix.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @param nGrayLevels the number of gray levels into which the ROI
	 *          intensities are discretized (IBSI fixed bin number), or null to
	 *          use the input values, which must then be positive integers
	 * @param distance the Chebyshev neighborhood distance (default 1)
	 * @param alpha the coarseness: the maximum gray level difference for
	 *          a neighbor to be dependent (default 0)
	 * @return the dependence matrix, indexed by gray level and dependence count + 1
	 * @implNote op names='image.neighboringGrayLevelDependenceMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		neighboringGrayLevelDependenceMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask,
			final Integer nGrayLevels,
			final Integer distance,
			final Integer alpha)
	{
		final var g = TextureUtils.discretize(input, mask, nGrayLevels);
		final var delta = distance == null ? 1 : distance;
		final var a = alpha == null ? 0 : alpha;
		if (delta < 1) throw new IllegalArgumentException(
			"Distance must be positive: " + delta);
		final var offsets = TextureUtils.neighborOffsets(g.numDimensions(), delta);
		final var matrix = new double[g.nLevels][offsets.size() + 1];
		for (var p = 0; p < g.levels.length; p++) {
			final var level = g.levels[p];
			if (level == 0) continue;
			var count = 0;
			for (final var o : offsets) {
				final var q = g.level(g.offset(p, o));
				if (q != 0 && Math.abs(q - level) <= a) count++;
			}
			matrix[level - 1][count]++;
		}
		return TextureUtils.trimColumns(matrix);
	}
}
