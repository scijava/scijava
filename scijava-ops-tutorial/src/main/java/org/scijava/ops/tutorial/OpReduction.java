/*-
 * #%L
 * Interactive tutorial for SciJava Ops.
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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

import java.lang.reflect.Method;

import org.scijava.function.Functions;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

/**
 * One powerful feature of SciJava Ops is the ability to transform individual
 * Ops to match user requests. In this way, SciJava Ops can satisfy many Op
 * calls with just one implementation.
 * <p>
 * Another transformation is called "reduction". This type of transformation
 * allows marked "Nullable" parameters to be omitted from Op requests, <b>in a
 * right-to-left order</b>. For example, if the rightmost parameter to an Op is
 * marked as Nullable, then it can be provided, or omitted, in Op signatures. If
 * the rightmost <b>two</b> parameters are both marked as Nullable, then both
 * can be provided, both can be omitted, or only the rightmost parameter can be
 * omitted.
 * <p>
 * Within Ops declaring Nullable parameters, omitted parameters are given
 * {@code null} arguments. The Op is thus responsible for null-checking any
 * parameters it declares as Nullable.
 * <p>
 * Below, we can see how this works by calling the above Method Op, normally
 * requiring three parameters, with only two parameters.
 */
public class OpReduction {

	/**
	 * An {@link Method} annotated to be an Op.
	 *
	 * @param in1 the first input
	 * @param in2 the second input. OPTIONAL.
	 * @param in3 the third input. OPTIONAL.
	 * @return the sum of the passed numbers.
	 * @implNote op names="tutorial.reduce"
	 */
	public static Double nullableMethod(Double in1, @Nullable Double in2,
		@Nullable Double in3)
	{
		// neither were given
		if (in2 == null && in3 == null) {
			System.out.println("Op called with in1");
			in2 = 0.;
			in3 = 0.;
		}
		// only in2 was given
		else if (in3 == null) {
			System.out.println("Op called with in1, in2");
			in3 = 0.;
		}
		// both were given
		else {
			System.out.println("Op called with in1, in2, in3");
		}
		// Then comes the normal computation
		return in1 + in2 + in3;
	}

	public static void main(String... args) {
		// Create the OpEnvironment
        var ops = OpEnvironment.build();

		// Define some data
		Double first = 1.;
		Double second = 2.;
		Double third = 3.;

		// -- CALL WITH ALL THREE PARAMETERS -- //

		// Ask for an Op of name "tutorial.reduce"
        var noNullable = ops.op("tutorial.reduce") //
			// With our two Double inputs
			.input(first, second, third) //
			// And get a Double out
			.outType(Double.class) //
			// Note that we can call this Op with only two arguments!
			.apply();

		// -- CALL WITH ONE OPTIONAL PARAMETER -- //

		// Ask for an Op of name "tutorial.reduce"
        var oneNullable = ops.op("tutorial.reduce") //
			// With our two Double inputs
			.input(first, second) //
			// And get a Double out
			.outType(Double.class) //
			// Note that we can call this Op with only two arguments!
			.apply();

		// -- CALL WITH TWO OPTIONAL PARAMETERS -- //

		// Ask for an Op of name "tutorial.reduce"
        var twoNullable = ops.op("tutorial.reduce") //
			// With our two Double inputs
			.input(first) //
			// And get a Double out
			.outType(Double.class) //
			// Note that we can call this Op with only two arguments!
			.apply();
	}

}
