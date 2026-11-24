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

package org.scijava.ops.image.threshold.rats;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Robust Automatic Threshold Selection (RATS): thresholds an image against
 * the local, gradient-weighted threshold surface computed by
 * {@code threshold.ratsSurface} (see there for the precise algorithm). A
 * pixel is foreground if its value exceeds the threshold at its position.
 * <p>
 * Based on the method of Kittler, Illingworth and F&ouml;glein (1985) and
 * Wilkinson (1998).
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @see "J. Kittler, J. Illingworth and J. F&ouml;glein, Threshold selection
 *      based on a simple image statistic, Comput. Vis. Graph. Image Process.
 *      30:125-147, 1985"
 * @see "M. H. F. Wilkinson, Optimizing edge detectors for robust automatic
 *      threshold selection: coping with edge curvature and noise, Graph.
 *      Models Image Process. 60(5):385-401, 1998"
 * @implNote op names='threshold.rats'
 */
public class Rats<T extends RealType<T>> implements
	Functions.Arity4<RandomAccessibleInterval<T>, Double, Double, Integer, RandomAccessibleInterval<BitType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> createOp;

	/**
	 * Thresholds an image with RATS.
	 *
	 * @param input the image to threshold
	 * @param sigma the standard deviation of the image noise (default: estimated
	 *          from the image)
	 * @param lambda the noise gate, in multiples of sigma (default: 3)
	 * @param minLeafSize the side length, in pixels, of the smallest regions of
	 *          the tree (default: 32)
	 * @return a binary image, true where the input exceeds the local threshold
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply(
		final RandomAccessibleInterval<T> input, @Nullable final Double sigma,
		@Nullable final Double lambda, @Nullable final Integer minLeafSize)
	{
		final var surface = RatsSurface.surface(input, sigma, lambda,
			minLeafSize);
		final var output = createOp.apply(input, new BitType());
		final var c = Views.flatIterable(input).cursor();
		var i = 0;
		for (final var t : Views.flatIterable(output))
			t.set(c.next().getRealDouble() > surface[i++]);
		return output;
	}
}
