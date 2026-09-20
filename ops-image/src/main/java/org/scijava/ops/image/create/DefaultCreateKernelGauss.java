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

package org.scijava.ops.image.create;

import java.util.function.BiFunction;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

/**
 * Creates a Gaussian Kernel
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Martin Horn (University of Konstanz)
 * @author Michael Zinsmaier (University of Konstanz)
 * @author Stephan Sellien (University of Konstanz)
 * @author Brian Northan
 * @author Gabriel Selzer
 */
public final class DefaultCreateKernelGauss {

	private DefaultCreateKernelGauss() {
		// Prevent instantiation of static utility class
	}

	public static <T extends Type<T>, C extends ComplexType<C>>
		RandomAccessibleInterval<C> createKernel(double[] input, C type,
			BiFunction<Dimensions, T, Img<T>> imgFromDimsAndType)
	{
		final var sigmaPixels = new double[input.length];

		final var dims = new long[input.length];
		final var kernelArrays = new double[input.length][];

		for (var d = 0; d < input.length; d++) {
			sigmaPixels[d] = input[d];

			dims[d] = Math.max(3, 2 * (int) (3 * sigmaPixels[d] + 0.5) + 1);
			kernelArrays[d] = Util.createGaussianKernel1DDouble(sigmaPixels[d], true);
		}

		final var out =
			(RandomAccessibleInterval<C>) imgFromDimsAndType.apply(new FinalInterval(
				dims), (T) type);

		final var cursor = Views.iterable(out).cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			double result = 1.0f;
			for (var d = 0; d < input.length; d++) {
				result *= kernelArrays[d][cursor.getIntPosition(d)];
			}

			cursor.get().setReal(result);
		}

		return out;
	}

}
