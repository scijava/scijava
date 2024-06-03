/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.transform;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Applies an Affine transform to a {@link RandomAccessibleInterval}
 *
 * @author Brian Northan (True North Intelligent Algorithms)
 * @author Martin Horn (University of Konstanz)
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op names="transform.realTransform", priority="101.0"
 */
public class DefaultTransformView<T extends NumericType<T> & RealType<T>>
	implements
	Functions.Arity4<RandomAccessibleInterval<T>, InvertibleRealTransform, Interval, InterpolatorFactory<T, RandomAccessible<T>>, RandomAccessibleInterval<T>>
{

	/**
	 * TODO
	 *
	 * @param input the input
	 * @param transform the transform to apply
	 * @param outputInterval the output interval
	 * @param interpolator the {@link InterpolatorFactory} delegated to for
	 *          interpolation
	 * @return the output
	 */
	@Override
	public RandomAccessibleInterval<T> apply( //
		RandomAccessibleInterval<T> input, //
		InvertibleRealTransform transform, //
		@Nullable Interval outputInterval, //
		@Nullable InterpolatorFactory<T, RandomAccessible<T>> interpolator //
	) {
		if (outputInterval == null) {
			outputInterval = new FinalInterval(input);
		}

		if (interpolator == null) {
			interpolator = new LanczosInterpolatorFactory<>();
		}

		var extended = Views.extendZero(input);
		var interpolated = Views.interpolate(extended, interpolator);
		var transformed = RealViews.transformReal(interpolated, transform);
		var rasterized = Views.raster(transformed);
		return Views.interval(rasterized, outputInterval);
	}

}
