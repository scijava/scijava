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

import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.image.features.texture.MatrixFeatures;

/**
 * IBSI features of the neighboring gray level dependence matrix (NGLDM).
 * The features are computed from a matrix as returned by
 * {@code image.neighboringGrayLevelDependenceMatrix}, in which element {@code [i - 1][j - 1]}
 * counts the entries with gray level {@code i} and dependence count {@code j}.
 * Each feature is documented with its IBSI identifier. Features of an empty
 * matrix are NaN.
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
public final class NGLDMFeatures {

	private NGLDMFeatures() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the low dependence emphasis (IBSI SODN).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the low dependence emphasis
	 * @implNote op names='features.ngldm.lowDependenceEmphasis', type=Function
	 */
	public static DoubleType lowDependenceEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 0, -2));
	}

	/**
	 * Computes the high dependence emphasis (IBSI IMOQ).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the high dependence emphasis
	 * @implNote op names='features.ngldm.highDependenceEmphasis', type=Function
	 */
	public static DoubleType highDependenceEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 0, 2));
	}

	/**
	 * Computes the low gray level count emphasis (IBSI TL9H).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the low gray level count emphasis
	 * @implNote op names='features.ngldm.lowGrayLevelCountEmphasis', type=Function
	 */
	public static DoubleType lowGrayLevelCountEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, 0));
	}

	/**
	 * Computes the high gray level count emphasis (IBSI OAE7).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the high gray level count emphasis
	 * @implNote op names='features.ngldm.highGrayLevelCountEmphasis', type=Function
	 */
	public static DoubleType highGrayLevelCountEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, 0));
	}

	/**
	 * Computes the low dependence low gray level emphasis (IBSI EQ3F).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the low dependence low gray level emphasis
	 * @implNote op names='features.ngldm.lowDependenceLowGrayLevelEmphasis', type=Function
	 */
	public static DoubleType lowDependenceLowGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, -2));
	}

	/**
	 * Computes the low dependence high gray level emphasis (IBSI JA6D).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the low dependence high gray level emphasis
	 * @implNote op names='features.ngldm.lowDependenceHighGrayLevelEmphasis', type=Function
	 */
	public static DoubleType lowDependenceHighGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, -2));
	}

	/**
	 * Computes the high dependence low gray level emphasis (IBSI NBZI).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the high dependence low gray level emphasis
	 * @implNote op names='features.ngldm.highDependenceLowGrayLevelEmphasis', type=Function
	 */
	public static DoubleType highDependenceLowGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, 2));
	}

	/**
	 * Computes the high dependence high gray level emphasis (IBSI 9QMG).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the high dependence high gray level emphasis
	 * @implNote op names='features.ngldm.highDependenceHighGrayLevelEmphasis', type=Function
	 */
	public static DoubleType highDependenceHighGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, 2));
	}

	/**
	 * Computes the gray level non-uniformity (IBSI FP8K).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the gray level non-uniformity
	 * @implNote op names='features.ngldm.grayLevelNonUniformity', type=Function
	 */
	public static DoubleType grayLevelNonUniformity(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowNonUniformity(matrix, false));
	}

	/**
	 * Computes the normalized gray level non-uniformity (IBSI 5SPA).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the normalized gray level non-uniformity
	 * @implNote op names='features.ngldm.grayLevelNonUniformityNormalized', type=Function
	 */
	public static DoubleType grayLevelNonUniformityNormalized(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowNonUniformity(matrix, true));
	}

	/**
	 * Computes the dependence count non-uniformity (IBSI Z87G).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the dependence count non-uniformity
	 * @implNote op names='features.ngldm.dependenceCountNonUniformity', type=Function
	 */
	public static DoubleType dependenceCountNonUniformity(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnNonUniformity(matrix, false));
	}

	/**
	 * Computes the normalized dependence count non-uniformity (IBSI OKJI).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the normalized dependence count non-uniformity
	 * @implNote op names='features.ngldm.dependenceCountNonUniformityNormalized', type=Function
	 */
	public static DoubleType dependenceCountNonUniformityNormalized(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnNonUniformity(matrix, true));
	}

	/**
	 * Computes the dependence count percentage (IBSI 6XV8).
	 * Since every voxel of the region of interest contributes one
	 * neighborhood to the matrix, this is 1 unless the matrix was subsampled.
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @param nVoxels the number of voxels in the region of interest
	 * @return the dependence count percentage
	 * @implNote op names='features.ngldm.dependenceCountPercentage', type=Function
	 */
	public static DoubleType dependenceCountPercentage(final double[][] matrix,
		final Long nVoxels)
	{
		return new DoubleType(MatrixFeatures.total(matrix) / nVoxels);
	}

	/**
	 * Computes the gray level variance (IBSI 1PFV).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the gray level variance
	 * @implNote op names='features.ngldm.grayLevelVariance', type=Function
	 */
	public static DoubleType grayLevelVariance(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowVariance(matrix));
	}

	/**
	 * Computes the dependence count variance (IBSI DNX2).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the dependence count variance
	 * @implNote op names='features.ngldm.dependenceCountVariance', type=Function
	 */
	public static DoubleType dependenceCountVariance(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnVariance(matrix));
	}

	/**
	 * Computes the dependence count entropy (IBSI FCBV).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the dependence count entropy
	 * @implNote op names='features.ngldm.dependenceCountEntropy', type=Function
	 */
	public static DoubleType dependenceCountEntropy(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.entropy(matrix));
	}

	/**
	 * Computes the dependence count energy (IBSI CAS9).
	 *
	 * @param matrix the matrix, as computed by {@code image.neighboringGrayLevelDependenceMatrix}
	 * @return the dependence count energy
	 * @implNote op names='features.ngldm.dependenceCountEnergy', type=Function
	 */
	public static DoubleType dependenceCountEnergy(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.energy(matrix));
	}
}
