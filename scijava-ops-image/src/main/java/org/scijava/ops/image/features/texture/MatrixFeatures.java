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

package org.scijava.ops.image.features.texture;

/**
 * Generic computations over a gray level texture matrix {@code m}, where
 * {@code m[i - 1][j - 1]} counts the entities (runs, zones, neighborhoods) of
 * gray level {@code i} with property value {@code j} (run length, zone size,
 * zone distance, dependence count). These are the building blocks of the IBSI
 * features of the GLRLM, GLSZM, GLDZM and NGLDM families. All functions return
 * {@link Double#NaN} for an empty matrix.
 *
 * @author Curtis Rueden
 */
public final class MatrixFeatures {

	private MatrixFeatures() {
		// Prevent instantiation of utility class.
	}

	/**
	 * @param m the matrix
	 * @return the sum of all matrix elements (the number of entities)
	 */
	public static double total(final double[][] m) {
		var sum = 0.0;
		for (final var row : m)
			for (final var v : row)
				sum += v;
		return sum;
	}

	/**
	 * Computes {@code 1/N sum_ij m_ij i^a j^b}.
	 *
	 * @param m the matrix
	 * @param a exponent of the gray level
	 * @param b exponent of the column value
	 * @return the emphasis
	 */
	public static double emphasis(final double[][] m, final int a, final int b) {
		var sum = 0.0;
		for (var i = 0; i < m.length; i++) {
			final var wi = Math.pow(i + 1, a);
			for (var j = 0; j < m[i].length; j++) {
				if (m[i][j] == 0) continue;
				sum += m[i][j] * wi * Math.pow(j + 1, b);
			}
		}
		return sum / total(m);
	}

	/**
	 * Computes the gray level non-uniformity {@code 1/N sum_i (sum_j m_ij)^2}.
	 *
	 * @param m the matrix
	 * @param normalized if true, divide by {@code N^2} instead of {@code N}
	 * @return the gray level non-uniformity
	 */
	public static double rowNonUniformity(final double[][] m,
		final boolean normalized)
	{
		var sum = 0.0;
		for (final var row : m) {
			var r = 0.0;
			for (final var v : row)
				r += v;
			sum += r * r;
		}
		final var n = total(m);
		return sum / (normalized ? n * n : n);
	}

	/**
	 * Computes the column non-uniformity {@code 1/N sum_j (sum_i m_ij)^2}.
	 *
	 * @param m the matrix
	 * @param normalized if true, divide by {@code N^2} instead of {@code N}
	 * @return the column (run length, zone size, ...) non-uniformity
	 */
	public static double columnNonUniformity(final double[][] m,
		final boolean normalized)
	{
		final var cols = columnSums(m);
		var sum = 0.0;
		for (final var c : cols)
			sum += c * c;
		final var n = total(m);
		return sum / (normalized ? n * n : n);
	}

	/**
	 * Computes the variance of the gray level, with each matrix element
	 * weighted by its probability {@code m_ij / N}.
	 *
	 * @param m the matrix
	 * @return the gray level variance
	 */
	public static double rowVariance(final double[][] m) {
		return variance(m, true);
	}

	/**
	 * Computes the variance of the column value, with each matrix element
	 * weighted by its probability {@code m_ij / N}.
	 *
	 * @param m the matrix
	 * @return the column (run length, zone size, ...) variance
	 */
	public static double columnVariance(final double[][] m) {
		return variance(m, false);
	}

	/**
	 * Computes the entropy {@code -sum_ij p_ij log2 p_ij}.
	 *
	 * @param m the matrix
	 * @return the entropy
	 */
	public static double entropy(final double[][] m) {
		final var n = total(m);
		var sum = 0.0;
		for (final var row : m) {
			for (final var v : row) {
				if (v == 0) continue;
				final var p = v / n;
				sum -= p * Math.log(p);
			}
		}
		return n == 0 ? Double.NaN : sum / Math.log(2);
	}

	/**
	 * Computes the energy {@code sum_ij p_ij^2}.
	 *
	 * @param m the matrix
	 * @return the energy
	 */
	public static double energy(final double[][] m) {
		final var n = total(m);
		var sum = 0.0;
		for (final var row : m)
			for (final var v : row)
				sum += v * v;
		return sum / (n * n);
	}

	/**
	 * Computes {@code sum_ij j m_ij}, which is the number of voxels for run
	 * length and size zone matrices.
	 *
	 * @param m the matrix
	 * @return the column-weighted sum
	 */
	public static double columnWeightedSum(final double[][] m) {
		var sum = 0.0;
		for (final var row : m)
			for (var j = 0; j < row.length; j++)
				sum += (j + 1) * row[j];
		return sum;
	}

	private static double[] columnSums(final double[][] m) {
		var width = 0;
		for (final var row : m)
			width = Math.max(width, row.length);
		final var cols = new double[width];
		for (final var row : m)
			for (var j = 0; j < row.length; j++)
				cols[j] += row[j];
		return cols;
	}

	private static double variance(final double[][] m, final boolean rows) {
		final var n = total(m);
		var mu = 0.0;
		for (var i = 0; i < m.length; i++)
			for (var j = 0; j < m[i].length; j++)
				mu += (rows ? i + 1 : j + 1) * m[i][j] / n;
		var sum = 0.0;
		for (var i = 0; i < m.length; i++) {
			for (var j = 0; j < m[i].length; j++) {
				final var dv = (rows ? i + 1 : j + 1) - mu;
				sum += dv * dv * m[i][j] / n;
			}
		}
		return sum;
	}
}
