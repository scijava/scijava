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

import java.lang.reflect.Field;
import java.util.function.BiFunction;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * One powerful feature of SciJava Ops is the ability to transform individual
 * Ops to match user requests. In this way, SciJava Ops can satisfy many
 * different Op calls with just one implementation.
 * <p>
 * The simplest type of transformation is termed "adaptation". Adaptation
 * involves transforming an Op into a different functional type. A set of
 * "engine.adapt" Ops exist to perform this transformation, taking in any Op of
 * functional type X and returning an Op of functional type Y.
 * <p>
 * Adaptation can be used to call a Function like a Computer, or to call an Op
 * that operates on Doubles like an Op that operates on a List of Doubles.
 * <p>
 * Below, we can see how this works by calling the above Field Op, supposed to
 * work on Doubles, on an array of Doubles[]
 */
public class OpAdaptation implements OpCollection {

	/**
	 * A simple Op, written as a {@link Field}, that performs a simple
	 * calculation.
	 */
	@OpField(names = "tutorial.adapt")
	public final BiFunction<Double, Double, Double> fieldOp = (a, b) -> {
		return a * 2 + b;
	};

	public static void main(String... args) {
		// Create the OpEnvironment
		OpEnvironment ops = OpEnvironment.build();
		// Call the Op on some inputs
		Double[] firstArray = new Double[] { 1., 2., 3. };
		Double[] secondArray = new Double[] { 1., 2., 3. };
		// Ask for an Op of name "tutorial.adapt"
		Double[] result = ops.binary("tutorial.adapt") //
			// With our two ARRAY inputs
			.input(firstArray, secondArray) //
			// And get a Double[] out
			.outType(Double[].class) //
			// Note that we can call this Op on arrays, even though it wasn't
			// meant to be!
			.apply();
		System.out.println(result);

	}

}
