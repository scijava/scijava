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

import java.util.function.BiFunction;

import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.types.Nil;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.logic.BitType;

/**
 * A tutorial describing the major flavors of Ops
 *
 * @author Gabriel Selzer
 */
public class OpTypes {

	public static void main(String... args) {
		/*
		While there are no restrictions on the FunctionalInterface that an Op,
		implements, there are three main categories used by the majority of Ops.

		We will showcase each of the types below.
		 */
        var ops = OpEnvironment.build();

		/**
		 * The most basic category of Ops are Functions. Function Ops encapsulate
		 * their functionality within a method named "apply", which takes in some
		 * number of inputs and returns a single output.
		 */
		@OpClass(names = "tutorial.add")
		class SampleFunction implements Op, BiFunction<Double, Double, Double> {

			@Override
			public Double apply(Double d1, Double d2) {
				return d1 + d2;
			}
		}

		ops.register(new SampleFunction());

        var sumOp = ops //
			// Look for a "math.add" op
			.op("tutorial.add") //
			// There should be two Double inputs
			.inType(Double.class, Double.class) //
			// And one Double output
			.outType(Double.class) //
			// And we want back the function
			.function(); //

		/*
		To execute a Function, call the "apply" method with our two Double inputs.
		 */
        var onePlusTwo = sumOp.apply(1., 2.);
		System.out.println("One plus Two is " + onePlusTwo);

		/*
		Computer Ops differ from Functions in that Computers require the user
		pass a pre-allocated output, which will then be populated by the Op.

		One situation where a Computer can be more useful than a Function is when
		you plan to call the Computer many times - if the output takes time to
		create, reusing a single output, if possible, can improve performance.

		The request for a Computer is very similar to the request for a Function:
		 */
        var powerOp = ops //
			// Look for a "math.power" Op
			.op("math.power") //
			.inType(double[].class, Double.class) //
			.outType(double[].class) //
			.computer();

        var bases = new double[] { 1, 2, 3, 4 };
        var exponent = 2.;
        var powers = new double[4];

		/*
		Inplace Ops improve on the performance of Computers by directly mutating
		one of the inputs - you don't even need to create an output!

		Of course, you will lose your input data in the process.

		The request for an Inplace is very similar to the request for the others.
		Note that the INDEX of the mutable input is specified in the
		 */

        var openOp =
			ops.op("morphology.open") //
				.inType(new Nil<RandomAccessibleInterval<BitType>>()
				{}, Nil.of(Interval.class), Nil.of(Shape.class), Nil.of(Integer.class))
				.inplace1();

	}
}
