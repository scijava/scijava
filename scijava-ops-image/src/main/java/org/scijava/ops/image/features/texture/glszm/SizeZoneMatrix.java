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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.image.features.texture.TextureUtils;

/**
 * Computes the gray level size zone matrix (GLSZM) of an n-dimensional region
 * of interest, as defined by the Image Biomarker Standardisation Initiative
 * (IBSI). A zone is a connected group of voxels of the same gray level, using
 * full connectivity (8-connectivity in 2D, 26-connectivity in 3D). Element
 * {@code [i - 1][j - 1]} of the result counts the zones of gray level
 * {@code i} and size {@code j} voxels. Trailing columns without any zone are
 * removed. The {@code features.glszm.*} Ops compute IBSI features from this
 * matrix.
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
 * @author Tatsuaki Kobayashi
 * @author Curtis Rueden
 */
public final class SizeZoneMatrix {

	private SizeZoneMatrix() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the gray level size zone matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @return the size zone matrix, indexed by gray level and zone size
	 * @implNote op names='image.sizeZoneMatrix', type=Function
	 */
	public static <T extends RealType<T>> double[][]
		sizeZoneMatrix(final RandomAccessibleInterval<T> input)
	{
		return SizeZoneMatrix.<T, BitType> sizeZoneMatrix(input, null, null);
	}

	/**
	 * Computes the gray level size zone matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @return the size zone matrix, indexed by gray level and zone size
	 * @implNote op names='image.sizeZoneMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		sizeZoneMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask)
	{
		return sizeZoneMatrix(input, mask, null);
	}

	/**
	 * Computes the gray level size zone matrix.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @param nGrayLevels the number of gray levels into which the ROI
	 *          intensities are discretized (IBSI fixed bin number), or null to
	 *          use the input values, which must then be positive integers
	 * @return the size zone matrix, indexed by gray level and zone size
	 * @implNote op names='image.sizeZoneMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		sizeZoneMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask,
			final Integer nGrayLevels)
	{
		final var g = TextureUtils.discretize(input, mask, nGrayLevels);
		final var labels = TextureUtils.labelZones(g);
		final var nZones = labels[labels.length - 1];
		final var sizes = new int[nZones + 1];
		final var zoneLevels = new int[nZones + 1];
		var maxSize = 1;
		for (var p = 0; p < g.levels.length; p++) {
			if (labels[p] == 0) continue;
			zoneLevels[labels[p]] = g.levels[p];
			maxSize = Math.max(maxSize, ++sizes[labels[p]]);
		}
		final var matrix = new double[g.nLevels][maxSize];
		for (var z = 1; z <= nZones; z++)
			matrix[zoneLevels[z] - 1][sizes[z] - 1]++;
		return matrix;
	}
}
