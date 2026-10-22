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

package org.scijava.ops.image.features.texture.gldzm;

import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.image.features.texture.MatrixFeatures;

/**
 * IBSI features of the gray level distance zone matrix (GLDZM).
 * The features are computed from a matrix as returned by
 * {@code image.distanceZoneMatrix}, in which element {@code [i - 1][j - 1]}
 * counts the entries with gray level {@code i} and zone distance {@code j}.
 * Each feature is documented with its IBSI identifier. Features of an empty
 * matrix are NaN.
 * <p>
 * These Ops are adapted from {@code GLDZMFeatures.java} of RadiomicsJ
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
 * Thibault G, et al. Advanced statistical matrices for texture
 * characterization: application to cell classification. IEEE Transactions on
 * Biomedical Engineering. 2014;61(3):630-637.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Tatsuaki Kobayashi
 */
public final class GLDZMFeatures {

	private GLDZMFeatures() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the small distance emphasis (IBSI 0GBI).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the small distance emphasis
	 * @implNote op names='features.gldzm.smallDistanceEmphasis', type=Function
	 */
	public static DoubleType smallDistanceEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 0, -2));
	}

	/**
	 * Computes the large distance emphasis (IBSI MB4I).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the large distance emphasis
	 * @implNote op names='features.gldzm.largeDistanceEmphasis', type=Function
	 */
	public static DoubleType largeDistanceEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 0, 2));
	}

	/**
	 * Computes the low gray level zone emphasis (IBSI S1RA).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the low gray level zone emphasis
	 * @implNote op names='features.gldzm.lowGrayLevelZoneEmphasis', type=Function
	 */
	public static DoubleType lowGrayLevelZoneEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, 0));
	}

	/**
	 * Computes the high gray level zone emphasis (IBSI K26C).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the high gray level zone emphasis
	 * @implNote op names='features.gldzm.highGrayLevelZoneEmphasis', type=Function
	 */
	public static DoubleType highGrayLevelZoneEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, 0));
	}

	/**
	 * Computes the small distance low gray level emphasis (IBSI RUVG).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the small distance low gray level emphasis
	 * @implNote op names='features.gldzm.smallDistanceLowGrayLevelEmphasis', type=Function
	 */
	public static DoubleType smallDistanceLowGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, -2));
	}

	/**
	 * Computes the small distance high gray level emphasis (IBSI DKNJ).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the small distance high gray level emphasis
	 * @implNote op names='features.gldzm.smallDistanceHighGrayLevelEmphasis', type=Function
	 */
	public static DoubleType smallDistanceHighGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, -2));
	}

	/**
	 * Computes the large distance low gray level emphasis (IBSI A7WM).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the large distance low gray level emphasis
	 * @implNote op names='features.gldzm.largeDistanceLowGrayLevelEmphasis', type=Function
	 */
	public static DoubleType largeDistanceLowGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, 2));
	}

	/**
	 * Computes the large distance high gray level emphasis (IBSI KLTH).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the large distance high gray level emphasis
	 * @implNote op names='features.gldzm.largeDistanceHighGrayLevelEmphasis', type=Function
	 */
	public static DoubleType largeDistanceHighGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, 2));
	}

	/**
	 * Computes the gray level non-uniformity (IBSI VFT7).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the gray level non-uniformity
	 * @implNote op names='features.gldzm.grayLevelNonUniformity', type=Function
	 */
	public static DoubleType grayLevelNonUniformity(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowNonUniformity(matrix, false));
	}

	/**
	 * Computes the normalized gray level non-uniformity (IBSI 7HP3).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the normalized gray level non-uniformity
	 * @implNote op names='features.gldzm.grayLevelNonUniformityNormalized', type=Function
	 */
	public static DoubleType grayLevelNonUniformityNormalized(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowNonUniformity(matrix, true));
	}

	/**
	 * Computes the zone distance non-uniformity (IBSI V294).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the zone distance non-uniformity
	 * @implNote op names='features.gldzm.zoneDistanceNonUniformity', type=Function
	 */
	public static DoubleType zoneDistanceNonUniformity(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnNonUniformity(matrix, false));
	}

	/**
	 * Computes the normalized zone distance non-uniformity (IBSI IATH).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the normalized zone distance non-uniformity
	 * @implNote op names='features.gldzm.zoneDistanceNonUniformityNormalized', type=Function
	 */
	public static DoubleType zoneDistanceNonUniformityNormalized(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnNonUniformity(matrix, true));
	}

	/**
	 * Computes the zone percentage (IBSI VIWW).
	 * Unlike the other features, this requires the number of voxels in the
	 * region of interest, which the matrix does not record.
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @param nVoxels the number of voxels in the region of interest
	 * @return the zone percentage
	 * @implNote op names='features.gldzm.zonePercentage', type=Function
	 */
	public static DoubleType zonePercentage(final double[][] matrix,
		final Long nVoxels)
	{
		return new DoubleType(MatrixFeatures.total(matrix) / nVoxels);
	}

	/**
	 * Computes the gray level variance (IBSI QK93).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the gray level variance
	 * @implNote op names='features.gldzm.grayLevelVariance', type=Function
	 */
	public static DoubleType grayLevelVariance(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowVariance(matrix));
	}

	/**
	 * Computes the zone distance variance (IBSI 7WT1).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the zone distance variance
	 * @implNote op names='features.gldzm.zoneDistanceVariance', type=Function
	 */
	public static DoubleType zoneDistanceVariance(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnVariance(matrix));
	}

	/**
	 * Computes the zone distance entropy (IBSI GBDU).
	 *
	 * @param matrix the matrix, as computed by {@code image.distanceZoneMatrix}
	 * @return the zone distance entropy
	 * @implNote op names='features.gldzm.zoneDistanceEntropy', type=Function
	 */
	public static DoubleType zoneDistanceEntropy(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.entropy(matrix));
	}
}
