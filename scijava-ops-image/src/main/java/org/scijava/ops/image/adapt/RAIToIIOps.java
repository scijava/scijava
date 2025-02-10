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

package org.scijava.ops.image.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

public class RAIToIIOps<T, U, V> {

	/**
	 * @input a {@link Function} that operates on an {@link Iterable}, producing
	 *        some output value
	 * @output a {@link Function} that operates on a
	 *         {@link RandomAccessibleInterval}, producing some output value
	 * @implNote op names='engine.adapt'
	 */
	public final Function<Function<Iterable<T>, U>, Function<RandomAccessibleInterval<T>, U>> func =
		(in) -> {
			return (in1) -> in.apply(Views.flatIterable(in1));
		};

	/**
	 * @input a {@link BiFunction} that operates on {@link Iterable}s, producing
	 *        some output value
	 * @output a {@link BiFunction} that operates on
	 *         {@link RandomAccessibleInterval}s, producing some output value
	 * @implNote op names='engine.adapt'
	 */
	public final Function<BiFunction<Iterable<T>, Iterable<U>, V>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, V>> biFunc =
		(in) -> {
			return (in1, in2) -> in.apply(Views.flatIterable(in1), Views.flatIterable(
				in2));
		};

}
