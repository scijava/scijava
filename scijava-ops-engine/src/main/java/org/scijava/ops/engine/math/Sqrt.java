/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.math;

import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class Sqrt implements OpCollection {

	public static final String NAMES = MathOps.SQRT;

	// --------- Functions ---------

	@OpField(names = NAMES, params = "number1, result")
	public static final Function<Double, Double> MathSqrtDoubleFunction =
		Math::sqrt;

	// --------- Computers ---------

	@OpField(names = NAMES, params = "array1, resultArray")
	public static final Computers.Arity1<double[], double[]> MathPointwiseSqrtDoubleArrayComputer =
		(arr1, arr2) -> {
			for (int i = 0; i < arr1.length; i++)
				arr2[i] = Math.sqrt(arr1[i]);
		};

	// --------- Inplaces ---------

	@OpField(names = NAMES, params = "arrayIO")
	public static final Inplaces.Arity1<double[]> MathPointwiseSqrtDoubleArrayInplace =
		(arr) -> {
			for (int i = 0; i < arr.length; i++)
				arr[i] = Math.sqrt(arr[i]);
		};

}
