/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops.image.integral;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.integral.IntegralImg;
import net.imglib2.converter.RealDoubleConverter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Priority;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Wrapper op for the creation of integral images with
 * {@code net.imglib2.algorithm.integral.IntegralImg}.
 *
 * @see IntegralImg
 * @author Stefan Helfrich (University of Konstanz)
 */
@Plugin(type = Op.class, name = "image.integral", priority = Priority.LOW)
@Parameter(key = "input")
@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
public class WrappedIntegralImg<I extends RealType<I>>
	implements Function<RandomAccessibleInterval<I>, RandomAccessibleInterval<DoubleType>>
{

	private IntegralImg<I, DoubleType> integralImg;

	@Override
	public RandomAccessibleInterval<DoubleType> apply(
		final RandomAccessibleInterval<I> input)
	{
		// Create IntegralImg from input
		integralImg = new IntegralImg<>(input, new DoubleType(),
			new RealDoubleConverter<I>());

		// integralImg will be larger by one pixel in each dimension than input
		// due
		// to the computation of the integral image
		RandomAccessibleInterval<DoubleType> img = null;
		if (integralImg.process()) {
			img = integralImg.getResult();
		}

		return img;
	}

}
