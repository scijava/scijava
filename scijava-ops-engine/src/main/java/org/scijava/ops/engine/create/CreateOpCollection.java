/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.create;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.priority.Priority;
import org.scijava.function.Producer;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * Creation ops
 *
 * @author Gabriel Selzer
 */
public class CreateOpCollection implements OpCollection {

	@OpField(names = "create.array, engine.create", priority = Priority.LOW,
		params = "array, arrayLike")
	public static final Function<double[], double[]> createdoubleArrayInputAware =
		from -> new double[from.length];

	@OpField(names = "create.array, engine.create", priority = Priority.LOW,
		params = "array, arrayLike")
	public static final Function<Double[], Double[]> createDoubleArrayInputAware =
		from -> new Double[from.length];

	@OpField(names = "create.array, engine.create", priority = Priority.LOW,
		params = "array, arrayLike")
	public static final Function<int[], int[]> createintArrayInputAware =
		from -> new int[from.length];

	@OpField(names = "create.array, engine.create", priority = Priority.LOW,
		params = "array, arrayLike")
	public static final Function<Integer[], Integer[]> createIntegerArrayInputAware =
		from -> new Integer[from.length];

	@OpField(names = "create.array, engine.create", priority = Priority.LOW,
		params = "array1, array2, arrayLike")
	public static final BiFunction<double[], double[], double[]> createDoubleArrayBiInputAware =
		(i1, i2) -> {
			if (i1.length != i2.length) {
				throw new IllegalArgumentException("Input array length muss be equal");
			}
			return new double[i1.length];
		};

	@OpField(names = "create.double, engine.create", priority = Priority.HIGH)
	public static final Producer<Double> doubleSource = () -> 0.0;
}
