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

import net.imglib2.type.numeric.real.DoubleType;

/**
 * IBSI features of the neighborhood gray tone difference matrix (NGTDM). The
 * features are computed from a matrix as returned by
 * {@code image.neighborhoodGrayToneDifferenceMatrix}, whose row {@code i - 1}
 * holds the values {@code [n_i, p_i, s_i]} of gray level {@code i}. Each
 * feature is documented with its IBSI identifier. Sums over pairs of gray
 * levels only include levels present in the region of interest
 * ({@code p_i > 0}).
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
public final class NGTDMFeatures {

	private NGTDMFeatures() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the coarseness (IBSI QCDE). If the sum of {@code p_i s_i} is 0,
	 * the result is 1e6, as recommended by IBSI.
	 *
	 * @param matrix the matrix, as computed by
	 *          {@code image.neighborhoodGrayToneDifferenceMatrix}
	 * @return the coarseness
	 * @implNote op names='features.ngtdm.coarseness', type=Function
	 */
	public static DoubleType coarseness(final double[][] matrix) {
		var sum = 0.0;
		for (final var row : matrix)
			sum += row[1] * row[2];
		return new DoubleType(sum == 0 ? 1e6 : 1 / sum);
	}

	/**
	 * Computes the contrast (IBSI 65HE).
	 *
	 * @param matrix the matrix, as computed by
	 *          {@code image.neighborhoodGrayToneDifferenceMatrix}
	 * @return the contrast
	 * @implNote op names='features.ngtdm.contrast', type=Function
	 */
	public static DoubleType contrast(final double[][] matrix) {
		final var ngp = presentLevels(matrix);
		if (ngp < 2) return new DoubleType(0);
		var pairs = 0.0;
		var sumS = 0.0;
		for (var i = 0; i < matrix.length; i++) {
			sumS += matrix[i][2];
			for (var j = 0; j < matrix.length; j++) {
				final var d = i - j;
				pairs += matrix[i][1] * matrix[j][1] * d * d;
			}
		}
		return new DoubleType(pairs / (ngp * (ngp - 1.0)) * sumS / voxels(
			matrix));
	}

	/**
	 * Computes the busyness (IBSI NQ30).
	 *
	 * @param matrix the matrix, as computed by
	 *          {@code image.neighborhoodGrayToneDifferenceMatrix}
	 * @return the busyness
	 * @implNote op names='features.ngtdm.busyness', type=Function
	 */
	public static DoubleType busyness(final double[][] matrix) {
		var numerator = 0.0;
		var denominator = 0.0;
		for (var i = 0; i < matrix.length; i++) {
			if (matrix[i][1] == 0) continue;
			numerator += matrix[i][1] * matrix[i][2];
			for (var j = 0; j < matrix.length; j++) {
				if (matrix[j][1] == 0) continue;
				denominator += Math.abs((i + 1) * matrix[i][1] - (j + 1) *
					matrix[j][1]);
			}
		}
		return new DoubleType(denominator == 0 ? 0 : numerator / denominator);
	}

	/**
	 * Computes the complexity (IBSI HDEZ).
	 *
	 * @param matrix the matrix, as computed by
	 *          {@code image.neighborhoodGrayToneDifferenceMatrix}
	 * @return the complexity
	 * @implNote op names='features.ngtdm.complexity', type=Function
	 */
	public static DoubleType complexity(final double[][] matrix) {
		var sum = 0.0;
		for (var i = 0; i < matrix.length; i++) {
			final var pi = matrix[i][1];
			if (pi == 0) continue;
			for (var j = 0; j < matrix.length; j++) {
				final var pj = matrix[j][1];
				if (pj == 0) continue;
				sum += Math.abs(i - j) * (pi * matrix[i][2] + pj * matrix[j][2]) /
					(pi + pj);
			}
		}
		return new DoubleType(sum / voxels(matrix));
	}

	/**
	 * Computes the strength (IBSI 1X9X). If the sum of {@code s_i} is 0, the
	 * result is 0.
	 *
	 * @param matrix the matrix, as computed by
	 *          {@code image.neighborhoodGrayToneDifferenceMatrix}
	 * @return the strength
	 * @implNote op names='features.ngtdm.strength', type=Function
	 */
	public static DoubleType strength(final double[][] matrix) {
		var sum = 0.0;
		var sumS = 0.0;
		for (var i = 0; i < matrix.length; i++) {
			sumS += matrix[i][2];
			final var pi = matrix[i][1];
			if (pi == 0) continue;
			for (var j = 0; j < matrix.length; j++) {
				final var pj = matrix[j][1];
				if (pj == 0) continue;
				final var d = i - j;
				sum += (pi + pj) * d * d;
			}
		}
		return new DoubleType(sumS == 0 ? 0 : sum / sumS);
	}

	private static int presentLevels(final double[][] matrix) {
		var count = 0;
		for (final var row : matrix)
			if (row[1] > 0) count++;
		return count;
	}

	private static double voxels(final double[][] matrix) {
		var sum = 0.0;
		for (final var row : matrix)
			sum += row[0];
		return sum;
	}
}
