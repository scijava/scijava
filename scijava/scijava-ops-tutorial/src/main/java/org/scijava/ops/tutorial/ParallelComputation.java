/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.tutorial;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.types.Nil;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.Parallelization;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Using the {@link net.imglib2.parallel.Parallelization} class, we can perform
 * independent computations in parallel. This tutorial showcases running many
 * Ops in parallel using {@link net.imglib2.parallel.Parallelization}.
 *
 * @author Gabriel Selzer
 */
public class ParallelComputation {

	public static void main(String... args) {
		OpEnvironment ops = OpEnvironment.getEnvironment();
		// To compute tasks using Parallelization, we must first gather a list of
		// parameters.
		List<Double> fillValues = Arrays.asList(1.0, 2.0, 3.0, 4.0);
		Img<UnsignedByteType> data = ArrayImgs.unsignedBytes(10, 10, 10);
		Nil<Img<UnsignedByteType>> outNil = new Nil<>() {};

		// Note that this function will be run many times in parallel
		// - it's not terribly complex, but we could do much more
		Function<Double, Img<UnsignedByteType>> fillImage = fillValue -> {
			// create a new image of the same size as our data
			var output = ops.op("create.img").arity1().input(data).outType(outNil)
				.apply();
			// fill it with the fill value
			LoopBuilder.setImages(output).forEachPixel(pixel -> pixel.setReal(
				fillValue));
			// and return it
			return output;
		};

		// Parallelization.getTaskExecutor().forEachApply() takes a list of
		// parameters,
		// and a function to apply on each parameter in the list. The function will
		// then be applied in parallel on each parameter, and the return is a list,
		// with the ith output being the application of the function on the ith
		// parameter.
		List<Img<UnsignedByteType>> filledImages = //
			Parallelization.getTaskExecutor().forEachApply(fillValues, fillImage);

		for (int i = 0; i < fillValues.size(); i++) {
			if (filledImages.get(i) != null) {
				System.out.println("Image " + i + " was filled with value " +
					filledImages.get(i).firstElement().get());
			}
		}

	}

}
