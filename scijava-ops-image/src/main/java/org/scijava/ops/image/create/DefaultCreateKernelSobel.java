/* #%L
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
import net.imglib2.view.Views;

/**
 * Creates a separated sobel kernel.
 *
 * @author Eike Heinz, University of Konstanz
 */

public final class DefaultCreateKernelSobel {

	private DefaultCreateKernelSobel() {
		// Prevent instantiation of static utility class
	}

	private static final float[] values = { 1.0f, 2.0f, 1.0f, -1.0f, 0.0f, 1.0f };

	public static <T extends Type<T>, C extends ComplexType<C>>
		RandomAccessibleInterval<C> createKernel(C type,
			BiFunction<Dimensions, T, Img<T>> createFunc)
	{
        var dim = new long[4];

		dim[0] = 3;
		dim[1] = 1;

		for (var k = 2; k < dim.length; k++) {
			dim[k] = 1;
		}

		dim[dim.length - 1] = 2;

        var output =
			(RandomAccessibleInterval<C>) createFunc.apply(new FinalInterval(dim),
				(T) type);
		final var cursor = Views.iterable(output).cursor();
        var i = 0;
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.get().setReal(values[i]);
			i++;
		}

		return output;
	}

}
