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

package org.scijava.ops.image.transform.focus;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Extended depth of field (focus stacking): combines the slices of a focal
 * series into a single image that is in focus everywhere.
 * <p>
 * For every slice {@code z} along {@code focusDim}, a per-pixel focus measure
 * {@code M_z = G_sigma * (Lap(G_1 * I_z))^2} is computed: the squared
 * Laplacian of the slice after light Gaussian smoothing (sigma 1, to suppress
 * pixel noise), averaged over a Gaussian window of the given sigma. This is
 * a local variance of the Laplacian, a standard sharpness measure
 * (Pech-Pacheco et al. 2000). Smoothing and the Laplacian are computed within
 * each slice, with border extension. The slices are then combined per pixel:
 * </p>
 * <ul>
 * <li>{@code "max"}: the value of the slice with the highest focus measure
 * (the first such slice in case of ties). The slice indices are available
 * from {@code transform.focusStackIndex}.</li>
 * <li>{@code "weighted"}: the mean of all slices weighted by their focus
 * measure, which gives smoother transitions between slices. Pixels where all
 * weights are zero get the unweighted mean.</li>
 * </ul>
 * <p>
 * Implemented from the published descriptions of focus measures; the
 * weighted mode is similar in spirit to the Gaussian_Stack_Focuser plugin of
 * Fiji, which weights slices by the local magnitude of the difference from a
 * Gaussian-blurred copy.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @see <a href="https://doi.org/10.1109/ICPR.2000.903548">J. L. Pech-Pacheco
 *      et al., "Diatom autofocusing in brightfield microscopy: a comparative
 *      study", Proc. ICPR 2000, 3:314-317</a>
 * @implNote op names='transform.focusStack'
 */
public class FocusStack<T extends RealType<T>> implements
	Functions.Arity4<RandomAccessibleInterval<T>, Integer, Double, String, RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	@OpDependency(name = "filter.gauss")
	private Computers.Arity3<RandomAccessibleInterval<T>, double[], OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<FloatType>> gaussOp;

	@OpDependency(name = "filter.gauss")
	private Computers.Arity3<RandomAccessibleInterval<FloatType>, double[], OutOfBoundsFactory<FloatType, RandomAccessibleInterval<FloatType>>, RandomAccessibleInterval<FloatType>> gaussFloatOp;

	/**
	 * Computes the extended depth of field image.
	 *
	 * @param input the focal series
	 * @param focusDim the index of the focus (z) dimension
	 * @param sigma the standard deviation, in pixels, of the Gaussian window
	 *          over which the focus measure is averaged (default: 2)
	 * @param method how to combine the slices: max or weighted (default: max)
	 * @return the composite image, with the focus dimension removed
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply(
		final RandomAccessibleInterval<T> input, final Integer focusDim,
		@Nullable final Double sigma, @Nullable final String method)
	{
		final var m = method == null ? "max" : method;
		if (!m.equals("max") && !m.equals("weighted")) {
			throw new IllegalArgumentException("Unknown method: " + m);
		}
		final var stacker = new FocusStacker<>(input, focusDim, sigma, m.equals(
			"weighted"), createOp, gaussOp, gaussFloatOp);
		final var output = createOp.apply(Views.hyperSlice(input, focusDim, input
			.min(focusDim)), new FloatType());
		var i = 0;
		for (final var t : Views.flatIterable(output))
			t.setReal(stacker.composite[i++]);
		return output;
	}
}
