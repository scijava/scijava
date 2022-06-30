/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.math;

import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;

/**
 * Binary Ops of the {@code math} namespace which operate on {@link RealType}s.
 * 
 * TODO: Can these be static?
 *
 * @author Leon Yang
 */
public class BinaryRealTypeMath <I1 extends RealType<I1>, I2 extends RealType<I2>, O extends RealType<O>>{

	/**
	 * Sets the real component of an output real number to the addition of the
	 * real components of two input real numbers.
	 * @input input1
	 * @input input2
	 * @container sum
	 * @implNote op names='math.add'
	 */
	public final Computers.Arity2<I1, I2, O> adder = (input1, input2, output) -> 
			output.setReal(input1.getRealDouble() + input2.getRealDouble());

	/**
	 * Sets the real component of an output real number to the logical AND of the
	 * real component of two input real numbers.
	 * @input input1
	 * @input input2
	 * @container result
	 * @implNote op names='math.and'
	 */
	public final Computers.Arity2<I1, I2, O> ander = (input1, input2, output) -> 
			output.setReal((long) input1.getRealDouble() & (long) input2
				.getRealDouble());

	/**
	 * Sets the real component of an output real number to the division of the
	 * real component of two input real numbers.
	 * @input input1
	 * @input input2
	 * @input divideByZeroValue
	 * @container result
	 * @implNote op names='math.divide'
	 */
	public final Computers.Arity3<I1, I2, Double, O> divider = (input1, input2, dbzVal, output) -> { 
			if (input2.getRealDouble() == 0) {
				output.setReal(dbzVal);
			}
			else {
				output.setReal(input1.getRealDouble() / input2.getRealDouble());
			}
	};

	/**
	 * Sets the real component of an output real number to the multiplication of
	 * the real component of two input real numbers.
	 * @input input1
	 * @input input2
	 * @container result
	 * @implNote op names='math.multiply'
	 */
	public final Computers.Arity2<I1, I2, O> multiplier = (input1, input2, output) -> 
			output.setReal(input1.getRealDouble() * input2.getRealDouble());

	/**
	 * Sets the real component of an output real number to the logical OR of the
	 * real component of two input real numbers.
	 * @input input1
	 * @input input2
	 * @container result
	 * @implNote op names='math.or'
	 */
	public final Computers.Arity2<I1, I2, O> orer = (input1, input2, output) -> 
			output.setReal((long) input1.getRealDouble() | (long) input2
				.getRealDouble());

	/**
	 * Sets the real component of an output real number to the subtraction between
	 * the real component of two input real numbers.
	 * @input input1
	 * @input input2
	 * @container result
	 * @implNote op names='math.subtract'
	 */
	public final Computers.Arity2<I1, I2, O> subtracter = (input1, input2, output) -> 
			output.setReal(input1.getRealDouble() - input2.getRealDouble());

	/**
	 * Sets the real component of an output real number to the logical XOR of the
	 * real component of two input real numbers.
	 * @input input1
	 * @input input2
	 * @container result
	 * @implNote op names='math.xor'
	 */
	public final Computers.Arity2<I1, I2, O> xorer = (input1, input2, output) -> 
			output.setReal((long) input1.getRealDouble() ^ (long) input2
				.getRealDouble());

}
