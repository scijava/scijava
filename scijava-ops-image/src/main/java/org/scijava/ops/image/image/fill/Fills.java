/*
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

package org.scijava.ops.image.image.fill;

import java.math.BigInteger;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.Unsigned128BitType;

/**
 * Class containing Ops for filling images.
 *
 * @author Gabriel Selzer
 */
public class Fills {

	/**
	 * Default sequential implementation for all {@link Iterable}s
	 *
	 * @param constant the value to fill the image with
	 * @param output the output buffer to fill with {@code constant}
	 * @implNote op names='image.fill', type=Computer
	 */
	public static <T extends Type<T>> void iteratorFill(final T constant,
		final Iterable<T> output)
	{
		for (T t : output) {
			t.set(constant);
		}
	}

	/**
	 * Default sequential implementation for all {@link Iterable} of
	 * {@link Unsigned128BitType}
	 *
	 * @param constant the value to fill the image with
	 * @param output the output buffer to fill with {@code constant}
	 * @implNote op names='image.fill', priority='10' type=Computer
	 */
	public static void iteratorU128bitFill(final Unsigned128BitType constant,
		final Iterable<Unsigned128BitType> output)
	{
		BigInteger bi = constant.getBigInteger();
		for (Unsigned128BitType t : output) {
			t.set(bi);
		}
	}

	/**
	 * Default parallel implementation using {@link LoopBuilder}
	 *
	 * @param constant the value to fill the image with
	 * @param output the output buffer to fill with {@code constant}
	 * @implNote op names='image.fill', priority='100.', type=Computer
	 */
	public static <T extends Type<T>> void loopBuilderFill(final T constant,
		final RandomAccessibleInterval<T> output)
	{
		LoopBuilder.setImages(output).multiThreaded().forEachPixel(pixel -> pixel
			.set(constant));
	}

	/**
	 * Specialized fill implementation for {@link Unsigned128BitType}, handling
	 * overflow by passing the value via {@link BigInteger}
	 *
	 * @param constant the value to fill the image with
	 * @param output the output buffer to fill with {@code constant}
	 * @implNote op names='image.fill', priority='1000.', type=Computer
	 */
	public static void u128bitFill(final Unsigned128BitType constant,
		final RandomAccessibleInterval<Unsigned128BitType> output)
	{
		BigInteger bi = constant.getBigInteger();
		LoopBuilder.setImages(output).multiThreaded().forEachPixel(pixel -> pixel
			.set(bi));
	}
}
