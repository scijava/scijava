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

package org.scijava.ops.image.labeling.edgecorrection;

import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.IntegerType;

/**
 * Ops implementing the Miles&ndash;Lantu&eacute;joul edge correction for
 * counting objects in a bounded field of view.
 * <p>
 * Objects that touch the border of the field of view cannot be measured, but
 * simply excluding them biases the count (and size statistics) downwards,
 * because large objects are more likely to touch the border. The
 * Miles&ndash;Lantu&eacute;joul correction excludes the border-touching
 * objects and weights each remaining object by the inverse of the probability
 * that a randomly placed copy of its bounding box lies entirely inside the
 * field of view:
 * </p>
 *
 * <pre>
 * weight = &prod;_d W_d / (W_d - w_d)
 * </pre>
 * <p>
 * where {@code W_d} is the size of the field of view and {@code w_d} the
 * bounding-box size of the object in dimension {@code d}. Edge-corrected
 * statistics of any per-object measurement {@code m} can be computed as
 * {@code Σ weight·m / Σ weight}.
 * </p>
 * <p>
 * Based on the "Field-of-view measure correction" ({@code CountCorrection})
 * macro of the BioVoxxel Toolbox by Jan Brocher / BioVoxxel (BSD 3-Clause
 * license, Copyright (C) Jan Brocher / BioVoxxel), generalized from 2D to n
 * dimensions. References: R. E. Miles, "On the elimination of edge effects in
 * planar sampling", in <i>Stochastic Geometry</i>, Wiley, 1974; C.
 * Lantu&eacute;joul, "Computation of the histograms of the number of edges and
 * neighbours of cells in a tessellation", <i>Geometrical Probability and
 * Biological Structures</i>, Springer, 1978.
 * </p>
 * <p>
 * The original BioVoxxel Toolbox license notice follows.
 * </p>
 *
 * <pre>
 * Copyright (c) 2015, BioVoxxel / Jan Brocher
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of BioVoxxel nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jan Brocher (BioVoxxel)
 * @see <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 *      Toolbox</a>
 */
public final class EdgeCorrection {

	private EdgeCorrection() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the Miles&ndash;Lantu&eacute;joul edge-correction weight of each
	 * label, taking the interval of the labeling as the field of view. Labels
	 * touching the border of the field of view get weight 0.
	 *
	 * @param labeling the labeled objects
	 * @return a map from each label to its correction weight
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.edgeCorrectionWeights', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> weights(
		final ImgLabeling<L, I> labeling)
	{
		final var n = labeling.numDimensions();
		final var weights = new LinkedHashMap<L, Double>();
		for (final var region : new LabelRegions<>(labeling)) {
			double weight = 1;
			for (var d = 0; d < n; d++) {
				if (region.min(d) <= labeling.min(d) || region.max(d) >= labeling.max(
					d))
				{
					weight = 0;
					break;
				}
				final double fieldSize = labeling.dimension(d);
				weight *= fieldSize / (fieldSize - region.dimension(d));
			}
			weights.put(region.getLabel(), weight);
		}
		return weights;
	}

	/**
	 * Computes the Miles&ndash;Lantu&eacute;joul edge-corrected number of
	 * objects, i.e. the sum of the edge-correction weights of all labels, taking
	 * the interval of the labeling as the field of view. Labels touching the
	 * border of the field of view are excluded.
	 *
	 * @param labeling the labeled objects
	 * @return the edge-corrected object count
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.edgeCorrectedCount', type=Function
	 */
	public static <L, I extends IntegerType<I>> Double count(
		final ImgLabeling<L, I> labeling)
	{
		double sum = 0;
		for (final var w : weights(labeling).values())
			sum += w;
		return sum;
	}
}
