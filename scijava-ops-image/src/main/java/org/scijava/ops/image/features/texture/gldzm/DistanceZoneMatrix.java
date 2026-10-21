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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.image.features.texture.TextureUtils;

/**
 * Computes the gray level distance zone matrix (GLDZM) of an n-dimensional
 * region of interest, as defined by the Image Biomarker Standardisation
 * Initiative (IBSI). Zones are connected groups of voxels of the same gray
 * level, using full connectivity (8-connectivity in 2D, 26-connectivity in
 * 3D). The distance of a zone is the smallest distance of its voxels to the
 * edge of the region of interest, where the distance is measured in steps
 * between face-adjacent voxels (city block distance) and voxels on the edge
 * of the ROI have distance 1. Element {@code [i - 1][j - 1]} of the result
 * counts the zones of gray level {@code i} and distance {@code j}. Trailing
 * columns without any zone are removed. The {@code features.gldzm.*} Ops
 * compute IBSI features from this matrix.
 * <p>
 * These Ops are based on {@code GLDZMFeatures.java} from RadiomicsJ
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
 * Thibault G, et al. Advanced statistical matrices for texture
 * characterization: application to cell classification. IEEE Transactions on
 * Biomedical Engineering. 2014;61(3):630-637.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Tatsuaki Kobayashi
 */
public final class DistanceZoneMatrix {

	private DistanceZoneMatrix() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Computes the gray level distance zone matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @return the distance zone matrix, indexed by gray level and zone distance
	 * @implNote op names='image.distanceZoneMatrix', type=Function
	 */
	public static <T extends RealType<T>> double[][]
		distanceZoneMatrix(final RandomAccessibleInterval<T> input)
	{
		return DistanceZoneMatrix.<T, BitType> distanceZoneMatrix(input, null, null);
	}

	/**
	 * Computes the gray level distance zone matrix,
	 * with default values for the omitted parameters.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @return the distance zone matrix, indexed by gray level and zone distance
	 * @implNote op names='image.distanceZoneMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		distanceZoneMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask)
	{
		return distanceZoneMatrix(input, mask, null);
	}

	/**
	 * Computes the gray level distance zone matrix.
	 *
	 * @param input the input image
	 * @param mask the region of interest (may be null for the whole image); only
	 *          the intersection of the image and mask intervals is considered
	 * @param nGrayLevels the number of gray levels into which the ROI
	 *          intensities are discretized (IBSI fixed bin number), or null to
	 *          use the input values, which must then be positive integers
	 * @return the distance zone matrix, indexed by gray level and zone distance
	 * @implNote op names='image.distanceZoneMatrix', type=Function
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> double[][]
		distanceZoneMatrix(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask,
			final Integer nGrayLevels)
	{
		final var g = TextureUtils.discretize(input, mask, nGrayLevels);
		final var n = g.numDimensions();
		final var faces = new int[2 * n][n];
		for (var d = 0; d < n; d++) {
			faces[2 * d][d] = -1;
			faces[2 * d + 1][d] = 1;
		}
		final var dist = new int[g.levels.length];
		final var queue = new int[g.levels.length];
		var head = 0;
		var tail = 0;
		for (var p = 0; p < g.levels.length; p++) {
			if (g.levels[p] == 0) continue;
			for (final var f : faces) {
				if (g.level(g.offset(p, f)) == 0) {
					dist[p] = 1;
					queue[tail++] = p;
					break;
				}
			}
		}
		while (head < tail) {
			final var p = queue[head++];
			for (final var f : faces) {
				final var q = g.offset(p, f);
				if (q < 0 || g.levels[q] == 0 || dist[q] != 0) continue;
				dist[q] = dist[p] + 1;
				queue[tail++] = q;
			}
		}
		final var labels = TextureUtils.labelZones(g);
		final var nZones = labels[labels.length - 1];
		final var zoneDist = new int[nZones + 1];
		final var zoneLevels = new int[nZones + 1];
		var maxDist = 1;
		for (var p = 0; p < g.levels.length; p++) {
			final var z = labels[p];
			if (z == 0) continue;
			zoneLevels[z] = g.levels[p];
			if (zoneDist[z] == 0 || dist[p] < zoneDist[z]) zoneDist[z] = dist[p];
			maxDist = Math.max(maxDist, dist[p]);
		}
		final var matrix = new double[g.nLevels][maxDist];
		for (var z = 1; z <= nZones; z++)
			matrix[zoneLevels[z] - 1][zoneDist[z] - 1]++;
		return TextureUtils.trimColumns(matrix);
	}
}
