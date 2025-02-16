/*-
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

package org.scijava.ops.image.transform;

import net.imglib2.Interval;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.Scale;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

import java.util.function.BiFunction;

/**
 * @author Edward Evans
 * @param <T> image type
 * @implNote op names='transform.scaleView', priority='100.'
 */

public class DefaultScaleView<T extends NumericType<T> & RealType<T>>
	implements BiFunction<RandomAccessibleInterval<T>, double[], RandomAccessibleInterval<T>>
{
	@OpDependency(name = "transform.realTransform")
	private Functions.Arity3<RandomAccessibleInterval<T>, InvertibleRealTransform, Interval, RandomAccessibleInterval<T>> transformOp;
	/**
	 * Image scale transformation. This Op scales input images using an array
	 * of scale factors per dimension.
	 *
	 * @param image the input image
	 * @param scaleFactors the scale factor as a list
	 * @return the scaled image
	 */
	@Override
	public RandomAccessibleInterval<T> apply(
		final RandomAccessibleInterval<T> image,
		final double[] scaleFactors)
	{
		// get input dimensions
		final long[] inputDims = image.dimensionsAsLongArray();

		// ensure scale factor dimensions match input image dimensions
		if (scaleFactors.length != inputDims.length) {
			throw new IllegalArgumentException(
					"Input image and scale factor dimension lengths do not match.");
		}

		// initialize the scale real transform
		final Scale s = new Scale(scaleFactors);

		// compute the scaled dimensions
		long[] scaleDims = new long[scaleFactors.length];
		for (int i = 0; i < scaleFactors.length; i++) {
			scaleDims[i] = (int) (inputDims[i] * scaleFactors[i]);
		}

		// create an interval for the scaled dimensions
		var scaleFinalDims = new FinalDimensions(scaleDims);

		return transformOp.apply(image, s, new FinalInterval(scaleFinalDims));
	}
}
