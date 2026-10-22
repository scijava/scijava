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

package org.scijava.ops.image.segment.srm;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Segments an n-dimensional image into homogeneous regions using Statistical
 * Region Merging (SRM), producing a labeling in which every sample belongs to
 * exactly one region.
 * <p>
 * SRM is described in R. Nock and F. Nielsen, "Statistical Region Merging",
 * IEEE Transactions on Pattern Analysis and Machine Intelligence
 * 26(11):1452&ndash;1458 (2004),
 * <a href="https://doi.org/10.1109/TPAMI.2004.110">doi:10.1109/TPAMI.2004.110</a>.
 * Starting from one region per sample, all pairs of face-adjacent samples are
 * visited in ascending order of their intensity difference, and the regions
 * of the two samples are merged whenever their mean intensities differ by
 * less than a statistical bound controlled by the complexity parameter Q.
 * </p>
 * <p>
 * This op is an n-dimensional translation of the {@code SRM_} plugin of the
 * <a href="https://github.com/fiji/Statistical_Region_Merging">Fiji
 * Statistical_Region_Merging</a> project, Copyright (C) 2009 - 2013 Johannes
 * Schindelin, distributed under the Simplified (2-clause) BSD License. Unlike
 * the original, which requires 8-bit images, it accepts any {@link RealType}:
 * intensities are mapped linearly from the value range of the image
 * {@code [min, max]} onto {@code [0, g - 1]}, and only the ordering of the
 * neighbor pairs is quantized (into g buckets). For an 8-bit image spanning
 * the full range 0&ndash;255, the result is identical to that of the plugin.
 * </p>
 * <p>
 * Regions are labeled {@code 0, 1, 2, ...} in order of their first sample in
 * flat iteration order, like the region index image of the original plugin.
 * </p>
 * <p>
 * The original Statistical_Region_Merging license notice follows.
 * </p>
 *
 * <pre>
 * Copyright (C) 2009 - 2013 Johannes Schindelin.
 *
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
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Johannes Schindelin
 * @param <T> input type
 * @implNote op names='segment.statisticalRegionMerging'
 */
public class DefaultStatisticalRegionMerging<T extends RealType<T>> implements
	Functions.Arity3<RandomAccessibleInterval<T>, Double, Integer, ImgLabeling<Integer, IntType>>
{

	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> imgLabelingCreator;

	/**
	 * Segments the input image using Statistical Region Merging.
	 *
	 * @param input the image to segment, without NaN
	 * @param q the complexity parameter Q, which must be positive (default 25);
	 *          larger values yield more, smaller regions
	 * @param g the number of intensity levels g onto which the input
	 *          values are mapped (default 256); it determines the scale of the
	 *          merging predicate and the number of buckets used to sort the
	 *          neighbor pairs
	 * @return a labeling assigning each sample its region label
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply( //
		final RandomAccessibleInterval<T> input, //
		@Nullable Double q, //
		@Nullable Integer g //
	) {
		if (q == null) q = StatisticalRegionMerging.DEFAULT_Q;
		if (g == null) g = StatisticalRegionMerging.DEFAULT_G;
		final var srm = StatisticalRegionMerging.run(input, q, g);
		final var output = imgLabelingCreator.apply(input, new IntType());
		final var c = Views.flatIterable(output).cursor();
		for (final var label : srm.labels) {
			c.next().add(label);
		}
		return output;
	}

}
