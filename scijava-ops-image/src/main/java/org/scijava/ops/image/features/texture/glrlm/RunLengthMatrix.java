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

package org.scijava.ops.image.features.texture.glrlm;

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.image.features.texture.TextureUtils;

/**
 * Computes the gray level run length matrix (GLRLM) of an n-dimensional region
 * of interest, as defined by the Image Biomarker Standardisation Initiative
 * (IBSI). Element {@code [i - 1][j - 1]} of the result counts the runs of gray
 * level {@code i} and length {@code j} along the requested direction (or, if
 * no direction is given, along all (3<sup>n</sup>-1)/2 grid directions, with
 * the per-direction matrices merged by summation). The matrix holds raw run
 * counts; trailing columns without any run are removed. The
 * {@code features.glrlm.*} Ops compute IBSI features from this matrix.
 * <p>
 * These Ops are adapted from {@code GLRLMFeatures.java} of RadiomicsJ
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
 * doi:10.1148/radiol.2020191145. Galloway MM. Texture analysis using gray
 * level run lengths. Computer Graphics and Image Processing. 1975;4(2):172-179.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Tatsuaki Kobayashi
 */
public final class RunLengthMatrix {

	private RunLengthMatrix() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the gray level run length matrix of a whole image, using its
	 * values as gray levels, merged over all grid directions.
	 *
	 * @param input the input image, whose values must be positive integers
	 * @return the run length matrix, indexed by gray level and run length
	 * @implNote op names='image.runLengthMatrix', type=Function
	 */
	public static <T extends RealType<T>> double[][] runLengthMatrix(
		final RandomAccessibleInterval<T> input)
	{
		return RunLengthMatrix.<T, BitType> runLengthMatrix(input, null, null,
			null);
	}

	/**
	 * Computes the gray level run length matrix of a region of interest, using
	 * the image values as gray levels, merged over all grid directions.
	 *
	 * @param input the input image, whose values must be positive integers
	 * @param mask the region of interest; only the intersection of the image
	 *          and mask intervals is considered
	 * @return the run length matrix, indexed by gray level and run length
	 * @implNote op names='image.runLengthMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		runLengthMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask)
	{
		return runLengthMatrix(input, mask, null, null);
	}

	/**
	 * Computes the gray level run length matrix of a region of interest, merged
	 * over all grid directions.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @param nGrayLevels the number of gray levels into which the ROI
	 *          intensities are discretized (IBSI fixed bin number), or null to
	 *          use the input values, which must then be positive integers
	 * @return the run length matrix, indexed by gray level and run length
	 * @implNote op names='image.runLengthMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		runLengthMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask, final Integer nGrayLevels)
	{
		return runLengthMatrix(input, mask, nGrayLevels, null);
	}

	/**
	 * Computes the gray level run length matrix of a region of interest.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @param nGrayLevels the number of gray levels into which the ROI
	 *          intensities are discretized (IBSI fixed bin number), or null to
	 *          use the input values, which must then be positive integers
	 * @param direction the run direction, e.g. [1, 0] for runs along the first
	 *          axis, or null to merge the matrices of all grid directions
	 * @return the run length matrix, indexed by gray level and run length
	 * @implNote op names='image.runLengthMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		runLengthMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask, final Integer nGrayLevels,
			final int[] direction)
	{
		final var g = TextureUtils.discretize(input, mask, nGrayLevels);
		final var n = g.numDimensions();
		final var dirs = direction == null ? TextureUtils.directions(n) : List
			.of(checkDirection(direction, n));
		var maxLength = 1;
		for (final var d : g.dims)
			maxLength = Math.max(maxLength, d);
		final var matrix = new double[g.nLevels][maxLength];
		final var back = new int[n];
		for (final var dir : dirs) {
			for (var d = 0; d < n; d++)
				back[d] = -dir[d];
			for (var p = 0; p < g.levels.length; p++) {
				final var level = g.levels[p];
				if (level == 0) continue;
				// Note: only start counting at the first voxel of each run.
				if (g.level(g.offset(p, back)) == level) continue;
				var length = 1;
				var q = g.offset(p, dir);
				while (g.level(q) == level) {
					length++;
					q = g.offset(q, dir);
				}
				matrix[level - 1][length - 1]++;
			}
		}
		return TextureUtils.trimColumns(matrix);
	}

	private static int[] checkDirection(final int[] direction, final int n) {
		if (direction.length != n) {
			throw new IllegalArgumentException("Direction has " +
				direction.length + " dimensions but the input has " + n);
		}
		for (final var v : direction)
			if (v != 0) return direction;
		throw new IllegalArgumentException("Direction must be nonzero");
	}
}
