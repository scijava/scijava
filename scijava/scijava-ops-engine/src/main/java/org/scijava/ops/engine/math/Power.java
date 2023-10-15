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

import java.util.function.BiFunction;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class Power implements OpCollection {

	public static final String NAMES = MathOps.POW;

	/**
	 * Computes the value of a number raised to the given power.
	 * 
	 * @input number The number to exponentiate.
	 * @input exponent The power to which to raise the number.
	 * @output result The number raised to the exponent.
	 */
	@OpField(names = NAMES, params = "number, exponent, result")
	public static final BiFunction<Double, Double, Double> MathPowerDoubleFunction = (base, exp) -> Math.pow(base, exp);

	@OpField(names = NAMES, params = "array, power, resultArray")
	public static final Computers.Arity2<double[], Double, double[]> MathPointwisePowerDoubleArrayComputer = (in, pow, out) -> {
		for (int i = 0; i < in.length; i++)
			out[i] = Math.pow(in[i], pow);
	};

}
