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

package org.scijava.ops.image.color.deltae;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.image.color.deconvolve.ColourDeconvolution;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Computes the per-pixel colour difference between two CIELAB images, whose
 * L*, a* and b* components lie along one dimension (as produced by
 * {@code color.convert} to Lab). See {@link DeltaE} for the formulas.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> pixel type of the first image
 * @param <U> pixel type of the second image
 * @see <a href="https://doi.org/10.1002/col.20070">G. Sharma, W. Wu and E. N.
 *      Dalal, "The CIEDE2000 color-difference formula: Implementation notes,
 *      supplementary test data, and mathematical observations", Color Res.
 *      Appl. 30(1):21-30, 2005</a>
 * @implNote op names='color.deltaE'
 */
public class DeltaEImage<T extends RealType<T>, U extends RealType<U>>
	implements
	Functions.Arity4<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, String, Integer, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	/**
	 * Computes the colour difference of each pair of pixels.
	 *
	 * @param lab1 the first (reference) CIELAB image
	 * @param lab2 the second CIELAB image, with the same dimensions
	 * @param formula the colour difference formula: CIE76, CIE94,
	 *          CIE94textiles or CIEDE2000 (default: CIEDE2000)
	 * @param channelDim the dimension holding the L*, a* and b* components
	 *          (default: the last dimension of size 3)
	 * @return the colour differences, with the channel dimension removed
	 */
	@Override
	public RandomAccessibleInterval<DoubleType> apply(
		final RandomAccessibleInterval<T> lab1,
		final RandomAccessibleInterval<U> lab2, @Nullable final String formula,
		@Nullable final Integer channelDim)
	{
		if (!Intervals.equalDimensions(lab1, lab2)) {
			throw new IllegalArgumentException(
				"Both images must have the same dimensions");
		}
		final var f = formula == null ? DeltaE.CIEDE2000 : DeltaE.byName(formula);
		final int cd = channelDim == null ? ColourDeconvolution.defaultChannelDim(
			lab1) : channelDim;
		if (cd < 0 || cd >= lab1.numDimensions() || lab1.dimension(cd) != 3) {
			throw new IllegalArgumentException("Channel dimension " + cd +
				" must exist and have size 3");
		}
		final var last = lab1.numDimensions() - 1;
		final var c1 = Views.collapse(Views.moveAxis(lab1, cd, last));
		final var c2 = Views.collapse(Views.moveAxis(lab2, cd, last));
		final var output = createOp.apply(c1, new DoubleType());
		LoopBuilder.setImages(c1, c2, output).multiThreaded().forEachPixel((p, q,
			out) -> out.set(f.compute( //
				p.get(0).getRealDouble(), p.get(1).getRealDouble(), p.get(2)
					.getRealDouble(), //
				q.get(0).getRealDouble(), q.get(1).getRealDouble(), q.get(2)
					.getRealDouble())));
		return output;
	}
}
