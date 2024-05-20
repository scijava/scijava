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

import org.scijava.ops.api.OpEnvironment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * While {@link OpTypes} shows how to write an Op as a {@link Class}, it is much
 * more convenient to write Ops with less code. This tutorial shows how you can
 * write Ops contained within {@link Field}s and {@link Method}s, leading to
 * less boilerplate code!
 *
 * @author Gabriel Selzer
 * @author Mark Hiner
 * @see OpTypes for information about writing Ops as {@link Class}es.
 */
public class WritingConciseOps {

	/**
	 * One major benefit of Ops written as {@link Field}s is that they can use
	 * Java's lambda syntax, maximizing expressiveness. Field Ops <b>must</b> be
	 * {@code public} <b>and</b> {@code final}, and should define Op parameters
	 * using the following tags:
	 * <ul>
	 * <li>{@code @input <name> <description>}</li> to describe a parameter named
	 * {@code <name>} with purpose {@code <description>}
	 * <li>{@code @container <name> <description>}</li> to describe a preallocated
	 * output <b>container</b> parameter named {@code <name>} with purpose
	 * {@code <description>}
	 * <li>{@code @mutable <name> <description>}</li> to describe a <b>mutable</b>
	 * input parameter named {@code <name>} with purpose {@code <description>}
	 * <li>{@code @output <description}</li> to describe a lambda return with
	 * purpose {@code <description>}
	 * </ul>
	 *
	 * @input b the base
	 * @input e the exponent
	 * @output the result
	 * @implNote op names="test.opField.power"
	 */
	public final BiFunction<Double, Double, Double> opFieldPower = (b, e) -> Math
		.pow(b, e);

	/**
	 * Ops can additionally be written as {@link Method}s. Method Ops must be
	 * {@code public} <b>and</b> {@code static}, and should declare their
	 * parameters using the following tags:
	 * <ul>
	 * <li>{@code @param <name> <description>}</li> to describe a parameter named
	 * {@code <name>} with purpose {@code <description>}
	 * <li>{@code @param <name> <description> (container)}</li> to describe a
	 * preallocated output <b>container</b> parameter named {@code <name>} with
	 * purpose {@code <description>}
	 * <li>{@code @param <name> <description> (mutable)}</li> to describe a
	 * <b>mutable</b> input parameter named {@code <name>} with purpose
	 * {@code <description>}
	 * <li>{@code @return <description}</li> to describe a method return with
	 * purpose {@code <description>}
	 * </ul>
	 *
	 * @param b the base
	 * @param e the exponent
	 * @return the result
	 * @implNote op names="test.opMethod.power"
	 */
	public static Double opMethodPower(Double b, Double e) {
		return Math.pow(b, e);
	}

	public static void main(String... args) {
		OpEnvironment ops = OpEnvironment.build();

		Double result = ops.op("test.opField.power") //
			.input(2.0, 10.0) //
			.outType(Double.class) //
			.apply();

		System.out.println("2.0 to the power of 10.0 is " + result);

		result = ops.op("test.opMethod.power") //
			.input(2.0, 20.0) //
			.outType(Double.class) //
			.apply();

		System.out.println("2.0 to the power of 20.0 is " + result);
	}

}
