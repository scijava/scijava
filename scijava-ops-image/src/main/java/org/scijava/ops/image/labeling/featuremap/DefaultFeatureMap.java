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

package org.scijava.ops.image.labeling.featuremap;

import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Paints a per-label value (such as a shape descriptor, size or mean
 * intensity) into an image: each labeled pixel is set to the value of its
 * label, producing a "feature map" for visualizing or thresholding objects by
 * a measurement.
 * <p>
 * Pixels without a label, or whose labels have no value in the map, are set to
 * the background value. Pixels with several labels are set to the mean of
 * their labels' values.
 * </p>
 * <p>
 * Based on the "Shape Descriptor Maps" macro of the BioVoxxel Toolbox by Jan
 * Brocher / BioVoxxel (BSD 3-Clause license, Copyright (C) Jan Brocher /
 * BioVoxxel), generalized from a fixed set of 2D shape descriptors to any
 * per-label value in n dimensions.
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
 * @param <L> label type
 * @param <I> index type of the labeling
 * @param <N> value type
 * @param <V> output pixel type
 * @see <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 *      Toolbox</a>
 * @implNote op names='labeling.featureMap'
 */
public class DefaultFeatureMap<L, I extends IntegerType<I>, N extends Number, V extends RealType<V>>
	implements
	Computers.Arity3<ImgLabeling<L, I>, Map<L, N>, Double, RandomAccessibleInterval<V>>
{

	/**
	 * Paints the feature map.
	 *
	 * @param labeling the labeled objects
	 * @param values the value of each label
	 * @param background the value of pixels without a (mapped) label (default
	 *          0)
	 * @param output the feature map
	 */
	@Override
	public void compute(final ImgLabeling<L, I> labeling, final Map<L, N> values,
		@Nullable Double background, final RandomAccessibleInterval<V> output)
	{
		final var bg = background == null ? 0 : background;
		final var mapping = labeling.getMapping();
		final var indexCount = mapping.numSets();

		// Precompute the value for each index of the labeling.
		final var indexValues = new double[indexCount];
		for (var i = 0; i < indexCount; i++) {
			double sum = 0;
			var count = 0;
			for (final var label : mapping.labelsAtIndex(i)) {
				final var value = values.get(label);
				if (value == null) continue;
				sum += value.doubleValue();
				count++;
			}
			indexValues[i] = count == 0 ? bg : sum / count;
		}

		LoopBuilder.setImages(labeling.getIndexImg(), output).forEachPixel((
			index, o) -> o.setReal(indexValues[index.getInteger()]));
	}
}
