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

import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.image.features.texture.MatrixFeatures;

/**
 * IBSI features of the gray level run length matrix (GLRLM).
 * The features are computed from a matrix as returned by
 * {@code image.runLengthMatrix}, in which element {@code [i - 1][j - 1]}
 * counts the entries with gray level {@code i} and run length {@code j}.
 * Each feature is documented with its IBSI identifier. Features of an empty
 * matrix are NaN.
 * <p>
 * These Ops are based on {@code GLRLMFeatures.java} from RadiomicsJ
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
 * Galloway MM. Texture analysis using gray level run lengths.
 * Computer Graphics and Image Processing. 1975;4(2):172-179.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Tatsuaki Kobayashi
 */
public final class GLRLMFeatures {

	private GLRLMFeatures() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the short runs emphasis (IBSI 22OV).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the short runs emphasis
	 * @implNote op names='features.glrlm.shortRunsEmphasis', type=Function
	 */
	public static DoubleType shortRunsEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 0, -2));
	}

	/**
	 * Computes the long runs emphasis (IBSI W4KF).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the long runs emphasis
	 * @implNote op names='features.glrlm.longRunsEmphasis', type=Function
	 */
	public static DoubleType longRunsEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 0, 2));
	}

	/**
	 * Computes the low gray level run emphasis (IBSI V3SW).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the low gray level run emphasis
	 * @implNote op names='features.glrlm.lowGrayLevelRunEmphasis', type=Function
	 */
	public static DoubleType lowGrayLevelRunEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, 0));
	}

	/**
	 * Computes the high gray level run emphasis (IBSI G3QZ).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the high gray level run emphasis
	 * @implNote op names='features.glrlm.highGrayLevelRunEmphasis', type=Function
	 */
	public static DoubleType highGrayLevelRunEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, 0));
	}

	/**
	 * Computes the short run low gray level emphasis (IBSI HTZT).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the short run low gray level emphasis
	 * @implNote op names='features.glrlm.shortRunLowGrayLevelEmphasis', type=Function
	 */
	public static DoubleType shortRunLowGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, -2));
	}

	/**
	 * Computes the short run high gray level emphasis (IBSI GD3A).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the short run high gray level emphasis
	 * @implNote op names='features.glrlm.shortRunHighGrayLevelEmphasis', type=Function
	 */
	public static DoubleType shortRunHighGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, -2));
	}

	/**
	 * Computes the long run low gray level emphasis (IBSI IVPO).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the long run low gray level emphasis
	 * @implNote op names='features.glrlm.longRunLowGrayLevelEmphasis', type=Function
	 */
	public static DoubleType longRunLowGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, -2, 2));
	}

	/**
	 * Computes the long run high gray level emphasis (IBSI 3KUM).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the long run high gray level emphasis
	 * @implNote op names='features.glrlm.longRunHighGrayLevelEmphasis', type=Function
	 */
	public static DoubleType longRunHighGrayLevelEmphasis(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.emphasis(matrix, 2, 2));
	}

	/**
	 * Computes the gray level non-uniformity (IBSI R5YN).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the gray level non-uniformity
	 * @implNote op names='features.glrlm.grayLevelNonUniformity', type=Function
	 */
	public static DoubleType grayLevelNonUniformity(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowNonUniformity(matrix, false));
	}

	/**
	 * Computes the normalized gray level non-uniformity (IBSI OVBL).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the normalized gray level non-uniformity
	 * @implNote op names='features.glrlm.grayLevelNonUniformityNormalized', type=Function
	 */
	public static DoubleType grayLevelNonUniformityNormalized(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowNonUniformity(matrix, true));
	}

	/**
	 * Computes the run length non-uniformity (IBSI W92Y).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the run length non-uniformity
	 * @implNote op names='features.glrlm.runLengthNonUniformity', type=Function
	 */
	public static DoubleType runLengthNonUniformity(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnNonUniformity(matrix, false));
	}

	/**
	 * Computes the normalized run length non-uniformity (IBSI IC23).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the normalized run length non-uniformity
	 * @implNote op names='features.glrlm.runLengthNonUniformityNormalized', type=Function
	 */
	public static DoubleType runLengthNonUniformityNormalized(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnNonUniformity(matrix, true));
	}

	/**
	 * Computes the run percentage (IBSI 9ZK5).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the run percentage
	 * @implNote op names='features.glrlm.runPercentage', type=Function
	 */
	public static DoubleType runPercentage(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.total(matrix) / MatrixFeatures
			.columnWeightedSum(matrix));
	}

	/**
	 * Computes the gray level variance (IBSI 8CE5).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the gray level variance
	 * @implNote op names='features.glrlm.grayLevelVariance', type=Function
	 */
	public static DoubleType grayLevelVariance(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.rowVariance(matrix));
	}

	/**
	 * Computes the run length variance (IBSI SXLW).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the run length variance
	 * @implNote op names='features.glrlm.runLengthVariance', type=Function
	 */
	public static DoubleType runLengthVariance(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.columnVariance(matrix));
	}

	/**
	 * Computes the run entropy (IBSI HJ9O).
	 *
	 * @param matrix the matrix, as computed by {@code image.runLengthMatrix}
	 * @return the run entropy
	 * @implNote op names='features.glrlm.runEntropy', type=Function
	 */
	public static DoubleType runEntropy(final double[][] matrix) {
		return new DoubleType(MatrixFeatures.entropy(matrix));
	}
}
