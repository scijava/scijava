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

package org.scijava.ops.image.features.haralick;

import org.scijava.ops.image.image.cooccurrenceMatrix.MatrixOrientation;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Implementation of the inverse variance (IBSI E8JP) haralick feature: {@code sum_(i != j) p_ij / (i - j)^2}.
 * <p>
 * Feature definition from the IBSI reference manual; this feature is also
 * implemented by {@code GLCMFeatures.java} of RadiomicsJ
 * (https://github.com/tatsunidas/RadiomicsJ), Copyright 2022 Tatsuaki
 * Kobayashi, licensed under the Apache License, Version 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0). Unless required by applicable
 * law or agreed to in writing, software distributed under that License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 * </p>
 * <p>
 * Reference: Zwanenburg A, Vallieres M, et al. The Image Biomarker
 * Standardization Initiative: Standardized Quantitative Radiomics for
 * High-Throughput Image-based Phenotyping. Radiology. 2020;295(2):328-338.
 * doi:10.1148/radiol.2020191145.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Tatsuaki Kobayashi
 * @implNote op names='features.haralick.inverseVariance'
 */
public class DefaultInverseVariance<T extends RealType<T>> extends
	AbstractHaralickFeature<T>
{

	/**
	 * Computes the inverse variance of the co-occurrence matrix.
	 *
	 * @param input the input image
	 * @param numGreyLevels the number of gray levels of the co-occurrence matrix
	 * @param distance the distance between co-occurring pixels
	 * @param orientation the orientation of co-occurring pixels
	 * @return the inverse variance
	 */
	@Override
	public DoubleType apply(final RandomAccessibleInterval<T> input,
		final Integer numGreyLevels, final Integer distance,
		final MatrixOrientation orientation)
	{
		final var matrix = getCooccurrenceMatrix(input, numGreyLevels,
			distance, orientation);
		final double ng = matrix.length;
		double res = 0;
		for (var i = 0; i < matrix.length; i++) {
			for (var j = 0; j < matrix.length; j++) {
				if (i != j) res += matrix[i][j] / ((i - j) * (i - j));
			}
		}
		return new DoubleType(res);
	}

}
