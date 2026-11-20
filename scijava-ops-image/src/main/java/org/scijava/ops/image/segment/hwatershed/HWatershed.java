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

/*
 * This file is derived from Interactive-H-Watershed
 * (https://github.com/mpicbg-scicomp/Interactive-H-Watershed), whose original
 * notice follows.
 *
 * Author: Benoit Lombardot, Scientific Computing Facility, MPI-CBG, Dresden
 *
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 * Dresden, Germany
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1 - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2 - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3 - Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.scijava.ops.image.segment.hwatershed;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * H-watershed: segments the bright objects of an n-dimensional grayscale image
 * by a watershed seeded with its h-maxima.
 * <p>
 * The image is flooded from its regional maxima, and the resulting hierarchy of
 * peaks (see {@link HSegmentTree}) is cut so that only peaks whose
 * <em>dynamics</em> (height above the saddle where they meet a higher peak) is
 * at least {@code hMin} seed their own segment; lower peaks are merged into the
 * segment of their higher neighbor. Pixels below {@code threshold} are
 * background, and each segment can be restricted to the top
 * {@code peakFlooding} percent of its height above the threshold. Touching
 * segments are not separated by watershed lines (there is no option for
 * them, unlike in {@code segment.compactWatershed}).
 * </p>
 * <p>
 * To segment dark objects, invert the image first.
 * </p>
 * <p>
 * This op is a port of the headless {@code H_Watershed} command of the
 * Interactive H-Watershed Fiji plugin by Benoit Lombardot (Scientific Computing
 * Facility, MPI-CBG, Dresden), licensed under the BSD-3-Clause license; see
 * <a href="https://imagej.net/plugins/interactive-watershed">its
 * documentation</a>. Unlike the original, intensities are not truncated to
 * integers and images of any dimensionality are supported.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Benoit Lombardot (MPI-CBG)
 * @param <T> element type of the input image
 * @implNote op names='segment.hWatershed'
 */
public class HWatershed<T extends RealType<T>> implements
	Functions.Arity6<RandomAccessibleInterval<T>, Double, Double, Double, Boolean, Shape, ImgLabeling<Integer, IntType>>
{

	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	/**
	 * Computes the H-watershed segmentation of an image.
	 *
	 * @param input the grayscale image to segment, without NaN; bright peaks
	 *          become segments.
	 * @param hMin the minimal dynamics (peak height above its saddle) for a peak
	 *          to seed its own segment. Defaults to 5% of the image intensity
	 *          range.
	 * @param threshold pixels with intensity below this value are background.
	 *          Defaults to the image minimum (i.e. no thresholding).
	 * @param peakFlooding the percentage (0-100) of the height of each segment above
	 *          {@code threshold} that is flooded: pixels below
	 *          {@code threshold + (peak - threshold) * (1 - peakFlooding / 100)}
	 *          are background. Defaults to 100 (flood down to the threshold).
	 * @param allowSplitting if true, peaks whose dynamics is below {@code hMin}
	 *          but which get separated from their segment by the threshold or the
	 *          peak flooding level form segments of their own; if false they are
	 *          discarded. Defaults to true.
	 * @param connectivity the neighborhood defining which pixels are adjacent
	 *          during the flooding, such as a {@code DiamondShape} of radius 1
	 *          for face connectivity (4-connectivity in 2D, 6-connectivity in
	 *          3D) or a {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity, unlike the original plugin, which defaults to face
	 *          connectivity. Regional maxima are always detected with full
	 *          connectivity.
	 * @return a labeling with one label (numbered from 1) per segment;
	 *         background pixels have no label
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply( //
		final RandomAccessibleInterval<T> input, //
		@Nullable Double hMin, //
		@Nullable Double threshold, //
		@Nullable Double peakFlooding, //
		@Nullable Boolean allowSplitting, //
		@Nullable Shape connectivity //
	) {
		if (peakFlooding == null) peakFlooding = 100.0;
		if (allowSplitting == null) allowSplitting = true;

		final var tree = HSegmentTree.build(input, connectivity);
		if (hMin == null) hMin = 0.05 * (tree.max() - tree.min());
		if (threshold == null) threshold = tree.min();
		if (hMin < 0) {
			throw new IllegalArgumentException("hMin must be non-negative: " + hMin);
		}

		final var labels = tree.labels(hMin, threshold, peakFlooding,
			allowSplitting);

		final var labeling = labelingCreator.apply(input, new IntType());
		var i = 0;
		for (final var t : Views.flatIterable(labeling)) {
			final var label = labels[i++];
			if (label > 0) t.add(label);
		}
		return labeling;
	}
}
