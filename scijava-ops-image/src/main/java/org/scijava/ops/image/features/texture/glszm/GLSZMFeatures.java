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

package org.scijava.ops.image.features.texture.glszm;

import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.image.features.texture.MatrixFeatures;

/**
 * IBSI features of the gray level size zone matrix (GLSZM).
 * The features are computed from a matrix as returned by
 * {@code image.sizeZoneMatrix}, in which element {@code [i - 1][j - 1]}
 * counts the entries with gray level {@code i} and zone size {@code j}.
 * Each feature is documented with its IBSI identifier. Features of an empty
 * matrix are NaN.
 * <p>
 * These Ops are based on {@code GLSZMFeatures.java} from RadiomicsJ
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
 * Thibault G, et al. Texture indexes and gray level size zone matrix.
 * Application to cell nuclei classification. Pattern Recognition and
 * Information Processing. 2009:140-145.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Tatsuaki Kobayashi
 */
public final class GLSZMFeatures {

	private GLSZMFeatures() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the small zone emphasis (IBSI 5QRC).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the small zone emphasis
	 * @implNote op names='features.glszm.smallZoneEmphasis', type=Function
	 */
	public static DoubleType smallZoneEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 0, -2));
	}

	/**
	 * Computes the large zone emphasis (IBSI 48P8).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the large zone emphasis
	 * @implNote op names='features.glszm.largeZoneEmphasis', type=Function
	 */
	public static DoubleType largeZoneEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 0, 2));
	}

	/**
	 * Computes the low gray level zone emphasis (IBSI XMSY).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the low gray level zone emphasis
	 * @implNote op names='features.glszm.lowGrayLevelZoneEmphasis', type=Function
	 */
	public static DoubleType lowGrayLevelZoneEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, 0));
	}

	/**
	 * Computes the high gray level zone emphasis (IBSI 5GN9).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the high gray level zone emphasis
	 * @implNote op names='features.glszm.highGrayLevelZoneEmphasis', type=Function
	 */
	public static DoubleType highGrayLevelZoneEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, 0));
	}

	/**
	 * Computes the small zone low gray level emphasis (IBSI 5RAI).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the small zone low gray level emphasis
	 * @implNote op names='features.glszm.smallZoneLowGrayLevelEmphasis', type=Function
	 */
	public static DoubleType smallZoneLowGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, -2));
	}

	/**
	 * Computes the small zone high gray level emphasis (IBSI HW1V).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the small zone high gray level emphasis
	 * @implNote op names='features.glszm.smallZoneHighGrayLevelEmphasis', type=Function
	 */
	public static DoubleType smallZoneHighGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, -2));
	}

	/**
	 * Computes the large zone low gray level emphasis (IBSI YH51).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the large zone low gray level emphasis
	 * @implNote op names='features.glszm.largeZoneLowGrayLevelEmphasis', type=Function
	 */
	public static DoubleType largeZoneLowGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, 2));
	}

	/**
	 * Computes the large zone high gray level emphasis (IBSI J17V).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the large zone high gray level emphasis
	 * @implNote op names='features.glszm.largeZoneHighGrayLevelEmphasis', type=Function
	 */
	public static DoubleType largeZoneHighGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, 2));
	}

	/**
	 * Computes the gray level non-uniformity (IBSI JNSA).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the gray level non-uniformity
	 * @implNote op names='features.glszm.grayLevelNonUniformity', type=Function
	 */
	public static DoubleType grayLevelNonUniformity(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowNonUniformity(matrix, false));
	}

	/**
	 * Computes the normalized gray level non-uniformity (IBSI Y1RO).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the normalized gray level non-uniformity
	 * @implNote op names='features.glszm.grayLevelNonUniformityNormalized', type=Function
	 */
	public static DoubleType grayLevelNonUniformityNormalized(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowNonUniformity(matrix, true));
	}

	/**
	 * Computes the zone size non-uniformity (IBSI 4JP3).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the zone size non-uniformity
	 * @implNote op names='features.glszm.zoneSizeNonUniformity', type=Function
	 */
	public static DoubleType zoneSizeNonUniformity(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnNonUniformity(matrix, false));
	}

	/**
	 * Computes the normalized zone size non-uniformity (IBSI VB3A).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the normalized zone size non-uniformity
	 * @implNote op names='features.glszm.zoneSizeNonUniformityNormalized', type=Function
	 */
	public static DoubleType zoneSizeNonUniformityNormalized(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnNonUniformity(matrix, true));
	}

	/**
	 * Computes the zone percentage (IBSI P30P).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the zone percentage
	 * @implNote op names='features.glszm.zonePercentage', type=Function
	 */
	public static DoubleType zonePercentage(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.total(matrix) / MatrixFeatures
			.columnWeightedSum(matrix));
	}

	/**
	 * Computes the gray level variance (IBSI BYLV).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the gray level variance
	 * @implNote op names='features.glszm.grayLevelVariance', type=Function
	 */
	public static DoubleType grayLevelVariance(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowVariance(matrix));
	}

	/**
	 * Computes the zone size variance (IBSI 3NSA).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the zone size variance
	 * @implNote op names='features.glszm.zoneSizeVariance', type=Function
	 */
	public static DoubleType zoneSizeVariance(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnVariance(matrix));
	}

	/**
	 * Computes the zone size entropy (IBSI GU8N).
	 *
	 * @param matrix the matrix, as computed by {@code image.sizeZoneMatrix}
	 * @return the zone size entropy
	 * @implNote op names='features.glszm.zoneSizeEntropy', type=Function
	 */
	public static DoubleType zoneSizeEntropy(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.entropy(matrix));
	}
}
