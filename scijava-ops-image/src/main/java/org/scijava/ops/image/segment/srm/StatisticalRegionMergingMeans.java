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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Segments an n-dimensional image into homogeneous regions using Statistical
 * Region Merging (SRM), replacing each sample by the mean intensity of its
 * region. This corresponds to the "showAverages" output of the original
 * plugin; use the {@code ImgLabeling}-producing
 * {@code segment.statisticalRegionMerging} op ({@link
 * DefaultStatisticalRegionMerging}) to obtain the regions themselves.
 * <p>
 * SRM is described in R. Nock and F. Nielsen, "Statistical Region Merging",
 * IEEE Transactions on Pattern Analysis and Machine Intelligence
 * 26(11):1452&ndash;1458 (2004),
 * <a href="https://doi.org/10.1109/TPAMI.2004.110">doi:10.1109/TPAMI.2004.110</a>.
 * </p>
 * <p>
 * This op is an n-dimensional translation of the {@code SRM_} plugin of the
 * <a href="https://github.com/fiji/Statistical_Region_Merging">Fiji
 * Statistical_Region_Merging</a> project, Copyright (C) 2009 - 2013 Johannes
 * Schindelin, distributed under the Simplified (2-clause) BSD License. Unlike
 * the original, which requires 8-bit images, it accepts any {@link RealType}:
 * intensities are mapped linearly from the value range of the image
 * {@code [min, max]} onto {@code [0, g - 1]}, and only the ordering of the
 * neighbor pairs is quantized (into g buckets). The region means are given in
 * the units of the input. For an 8-bit image spanning the full range
 * 0&ndash;255, the result is identical to that of the plugin.
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
 * @param <O> output type
 * @implNote op names='segment.statisticalRegionMerging'
 */
public class StatisticalRegionMergingMeans<T extends RealType<T>, O extends RealType<O>>
	implements
	Computers.Arity3<RandomAccessibleInterval<T>, Double, Integer, RandomAccessibleInterval<O>>
{

	/**
	 * Segments the input image using Statistical Region Merging, writing the
	 * mean intensity of the region of each sample into the output.
	 *
	 * @param input the image to segment, without NaN
	 * @param q the complexity parameter Q, which must be positive (default 25);
	 *          larger values yield more, smaller regions
	 * @param g the number of intensity levels g onto which the input
	 *          values are mapped (default 256); it determines the scale of the
	 *          merging predicate and the number of buckets used to sort the
	 *          neighbor pairs
	 * @param output the image of region means, of the same dimensions as the
	 *          input; a floating-point type is recommended
	 */
	@Override
	public void compute( //
		final RandomAccessibleInterval<T> input, //
		@Nullable Double q, //
		@Nullable Integer g, //
		final RandomAccessibleInterval<O> output //
	) {
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		if (q == null) q = StatisticalRegionMerging.DEFAULT_Q;
		if (g == null) g = StatisticalRegionMerging.DEFAULT_G;
		final var srm = StatisticalRegionMerging.run(input, q, g);
		final var c = Views.flatIterable(output).cursor();
		for (final var label : srm.labels) {
			c.next().setReal(srm.means[label]);
		}
	}

}
