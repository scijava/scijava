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

package org.scijava.ops.image.registration;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

import java.util.ArrayList;
import java.util.function.BiFunction;

/**
 * @author Edward Evans
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='registration.lucasKanade', priority='100.'
 */

// TODO: write missing scaleView op
// TODO: export 'u' and 'v' optical flow array, can be used for segmentation
// TODO: consider making this Op a function and not a computer?

public class LucasKanadeRegistration<I extends RealType<I>> implements
	Computers.Arity7<RandomAccessibleInterval<I>, Integer, Integer, Float, Float, Boolean, Boolean, RandomAccessibeInteger<O>>
{
	@OpDependency(name = "transform.scaleView")
	private BiFunction<RandomAccessibleInterval<I>, double[], RandomAccessibleInterval<I>> scaleOp;

	/**
	 * Lucas-Kanade image registration. This Op utilizes Lucas-Kanade's method of
	 * optical flow to register an image stack.
	 *
	 * @param image input 3D (X, Y, Time) image
	 * @param pyramidLevel maximum pyramid level to use
	 * @param iterations number of iterations to perform
	 * @param updateCoefficient template update coefficient
	 * @param errorTolerance error tolerance
	 * @param useAffine use the affine method, if false use transform
	 * @param logTransform boolean to set log transform
	 * @param output the output image
	 */

	@Override
	public void compute(final RandomAccessibleInterval<I> image,
		final Integer pyramidLevel, final Integer iterations,
		final Float updateCoefficient, final Float errorTolerance,
		final Boolean useAfffine, final Boolean logTransform,
		RandomAccessibleInterval<O> output)
	{
		// ensure images are 3D only
		if (!(image.numDimensions() == 3)) {
			throw new IllegalArgumentException(
					"Input image must be 3D (X, Y, Time).");

		// construct pyramid
		long[] dims = image.dimensionsAsLongArray()
		var pyramid = createScalePyramid(pyramidLevel, dims)
		}
	}

	/**
	 * Creates a scale pyramid with the specified level.
	 *
	 * @param image input image
	 * @param level number of pyramid levels to create
	 * @param shape shape of the input image
	 * @return the scale pyramid as a list
	 */
	private ArrayList<RandomAccessibleInterval<I>> createScalePyramid(final RandomAccessibleInterval<I> image,
			final Integer level, final long[] shape)
	{
		var pyramid = new ArrayList<RandomAccessibleInterval<I>>();
		double divFactor = 2.0;
		double[] scaleFactors = {0.0, 0.0, 1.0};
		for (int i = 0; i < level; i++;) {
			// compute scale factor, only scale X and Y
			scaleFactors[0] = 1.0 / divFactor;
			scaleFactors[1] = 1.0 / divFactor;
			// scale image and add to the pyramid
			pyramid.add(scaleOp.apply(image, scaleFactors));
			// increment division scale factor
			divFactor *= 2;

		return pyramid
		}
	}
