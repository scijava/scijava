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

import java.util.function.BiFunction;

import org.scijava.ops.api.OpEnvironment;

/**
 * The OpBuilder syntax leads to concise, script-like Op execution.
 * <p>
 * Below we show how you can use the OpBuilder syntax to call Ops on your inputs,
 * <em>or</em> how to instead retrieve an Op for more use later.
 */
public class OpBuilder {

	public static void main(String... args) {
		// All Ops calls start from an OpEnvironment. This environment determines the available Ops.
		OpEnvironment ops = OpEnvironment.build();

		/*
		To run an Op we have to match it. Ops themselves have a name, some number of inputs, and potentially an output
		(or input to be modified in-place). There are several ways to specify these. To clarify this process we created
		the OpBuilder.

		The OpBuilder sequentially defines the Op attributes required for matching. You start by providing an OpEnvironment
		with the name, using the "ops.op(String name)" method:

			ops.op("math.add")

		This method also contains usage documentation, if needed outside of these tutorials.

		Then, you specify the number of inputs, which we refer to as "arity" (see https://en.wikipedia.org/wiki/Arity).
		So:

			ops.op("math.add").arity2() specifies an addition op with 2 inputs
			ops.op("math.add").arity3() would be an addition op with 3 inputs, and so on.

		Ops from arity 0-16 are supported. There is also a convenience option within the OpEnvironment where the "op"
		and "arity" methods are combined, using the names in the wikipedia article above. In this way, the above two Op
		calls would instead be:

			ops.binary("math.add")
			ops.ternary("math.add")

		Next, the inputs are provided - either by type ("inType") or value ("input"). When using "inType" you can either
		give Classes, or use org.scijava.type.Nil in cases when your types have generic parameters that you want to
		preserve in matching. Using "input" potentially allows the builder to match and then immediately run the matched
		Op, and is necessary to match Computer ops. For example:

			ops.binary("math.add").input(5, 10)
			ops.binary("math.add").inType(new Nil<Img<ByteType>>() {}, Nil.of(Integer.class))

		After the inputs are set one of three cases apply:
		1. If you are matching an Inplace Op you're done
		2. If you want a Function, you need to specify the desired output type via outType()
		3. If you want a Computer, to operate on pre-allocated output, use the output() method

			ops.binary("math.add").input(5, 10).outType(Integer.class)
			ops.binary("math.add").input(img1, img2).output(img3)

		Finally, you tell the builder to match the Op. You can choose to either save the matched Op instance, i.e. for
		reuse (or if concrete inputs/outputs were NOT provided), or you can have the builder also execute the Op. The
		methods differ for each type of Op being matched, as follows:

			Op Type		|		Match only		|		Match and run
			---------------------------------------------------------
			Function	|		function()		|		apply()
			Computer	|		computer()		|		compute()
			Inplace *	|		inplaceN() 	|		mutateN()

		* For "Inplace" ops, N is the index of the input to mutate and must be in the range of [1, {arity}].

		Examples:

			ops.binary("math.add").input(Integer.class, Integer.class).inplace1() 				// Matches and returns an Inplace op that will mutate the first parameter
			ops.binary("math.add").input(5, 10).mutate2()										// Matches and runs an Inplace op, mutating the second parameter
			ops.binary("math.add").input(Integer.class, Integer.class).outType(Integer.class)	// Matches and returns a Function op
			ops.binary("math.add").input(img1, img2).output(img3).compute()						// Matches and runs a Computer op, storing the result in img3
		*/

		// Code examples follow

		/*
		In this simple example, we want to add two Doubles, and get back the sum.
		Thus, we want a function (making a Double to populate later is silly, not
		to mention impossible!), with two Double inputs and a Double output
		 */
		Double result;
		result = ops.binary("math.add") // provide the name
				.input(1.0, 2.0) // provide the inputs
				.outType(Double.class) // provide the output TYPE
				.apply(); // call apply()

		System.out.println("1+2 added within OpBuilder: " + result);

		/*
		If you are going to be running an Op many times, choose to obtain the Op
		instead of the result. This can be done by modifying the LAST function
		of any OpBuilder call:

		* For Functions, call function() instead of apply()
		* For Computers, call computer() instead of compute()
		* For Inplaces,  call inplaceX() instead of mutateX()

		Choosing to get the Op instead will save time immediately, and will skip
		matching if you want to call the same Op again later.
		 */
		BiFunction<Double, Double, Double> function = // Now, we get a function
				ops.binary("math.add") // again, provide the name
				.input(1.0, 2.0) // again, provide the inputs
				.outType(Double.class) // again, provide the output TYPE
				.function(); // this time, call function()

		result = function.apply(1.0, 2.0);

		System.out.println("1+2 added after matching on values: " + result);

		/*
		The above call can also be useful if you want to get an Op before you
		have inputs to pass. In this case, you can instead pass the types of your
		Op by using inType() instead of input().
		 */

		BiFunction<Double, Double, Double> function2 = // Now, we get a function
				ops.binary("math.add") // again, provide the name
						.inType(Double.class, Double.class) // we want to give two Doubles
						.outType(Double.class) // again, provide the output TYPE
						.function(); // this time, call function()

		result = function.apply(1.0, 2.0);

		System.out.println("1+2 added after matching on types: " + result);

	}

}
