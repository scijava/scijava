/*-
 * #%L
 * Interactive tutorial for SciJava Ops.
 * %%
 * Copyright (C) 2023 - 2025 SciJava developers.
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
 * Ops to match user requests. In this way, SciJava Ops can satisfy many Op
 * calls with just one implementation. The simplest type of transformation is
 * showcased in {@link OpAdaptation}.
 * <p>
 * A more complex transformation, termed parameter "conversion", alters
 * individual Op parameter types. Parameter conversion makes use of two
 * different Op types:
 * <ul>
 * <li>{@code engine.convert} Ops transform user inputs into a different data
 * type</li>
 * <li>{@code engine.copy} Ops propagate changes to mutable Op parameters back
 * into the user's output buffers.</li>
 * </ul>
 * <p>
 * Conversion can be used to call an Op using parameters of completely different
 * types. This can be as simple as using an Integer instead of a Double, or go
 * beyond Java type assignment rules with custom defined type conversion (e.g.
 * images from one library to another). When this happens, the following steps
 * are taken:
 * <ol>
 * <li>All inputs are converted to types required by the Op, using
 * {@code engine.convert} "preconverter" Ops.</li>
 * <li>The Op is invoked on the converted inputs</li>
 * <li>If the Op defines a <em>pure</em> output object, that output is converted
 * to the user's requested type using a {@code engine.convert} "postconverter"
 * Op</li>
 * <li>If the Op defines a <em>mutable</em> output object (i.e. a pre-allocated
 * output buffer), the converted pre-allocated output buffer is copied back into
 * the user's pre-allocated output buffer using a {@code engine.copy} Op.</li>
 * </ol>
 * <p>
 * Below, we can see how this works by calling the below Field Op, implemented
 * for {@link Double}s, with {@link Integer} arguments
 */
public class OpConversion {

	/**
	 * A simple Op, written as a {@link Field}, that performs a simple
	 * calculation.
	 *
	 * @input a the first {@link Double}
	 * @input b the second {@link Double}
	 * @output a linear combination of {@code a} and {@code b}
	 * @implNote op names="tutorial.conversion"
	 */
	public final BiFunction<Double, Double, Double> fieldOp = (a, b) -> {
		return a * 2 + b;
	};

	public static void main(String... args) {
		// Create the OpEnvironment
        var ops = OpEnvironment.build();
		// Call the Op on some inputs
		Integer first = 1;
		Integer second = 2;
		// Ask for an Op of name "tutorial.conversion"
        var result = ops.op("tutorial.conversion") //
			// With our two Integer inputs
			.input(first, second) //
			// And get an Integer out
			.outType(Integer.class) //
			// Note that we can call this Op on Integers, even though it is written
			// for Doubles!
			.apply();

		/*
		The conversion routine works as follows:
		1. SciJava Ops determines that there are no existing "tutorial.conversion"
		Ops that work on Integers

		2. SciJava Ops finds an "engine.convert" Op that can make Doubles from
		Integers. Thus, if the inputs are Integers, SciJava Ops could use this
		"engine.convert" Op to make those Integers into Doubles

		2. SciJava Ops finds an "engine.convert" Op that can make Integers from
		Doubles. Thus, if a Double output is requested, SciJava Ops could use this
		"engine.convert" Op to make a Double output from an Integer

		3. By using the "engine.convert" Ops, SciJava Ops can convert each of
		the Integer inputs into Double inputs, and then can convert the Double
		output back into the Integer the user requested. Thus, we get an Op that can
		run on Integers!
		 */
		System.out.println(result);

	}

}
