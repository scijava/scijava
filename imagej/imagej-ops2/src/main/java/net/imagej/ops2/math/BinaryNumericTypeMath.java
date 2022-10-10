/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;

/**
 * Binary Ops of the {@code math} namespace which operate on {@link RealType}s.
 * 
 * TODO: Can these be static?
 *
 * @author Leon Yang
 */
public class BinaryNumericTypeMath <T extends NumericType<T>> {

	/**
	 * Sets the real component of an output real number to the addition of the
	 * real components of two input real numbers.
	 * @input input1
	 * @input input2
	 * @container sum
	 * @implNote op names='math.add', priority='100.'
	 */
	public final Computers.Arity2<T, T, T> adder = (input1, input2, output) -> {
		output.set(input1);
		output.add(input2);
	};

	/**
	 * Sets the real component of an output real number to the division of the
	 * real component of two input real numbers.
	 * @input input1
	 * @input input2
	 * @input divideByZeroValue
	 * @container result
	 * @implNote op names='math.divide', priority='100.'
	 */
	public final Computers.Arity3<T, T, T, T> divider = (input1, input2, dbzVal, output) -> { 
		try {
			output.set(input1);
			output.div(input2);
		} catch(Exception e) {
			output.set(dbzVal);
		}
	};

	/**
	 * Sets the real component of an output real number to the multiplication of
	 * the real component of two input real numbers.
	 * @input input1
	 * @input input2
	 * @container result
	 * @implNote op names='math.multiply', priority='100.'
	 */
	public final Computers.Arity2<T, T, T> multiplier = (input1, input2, output) -> {
		output.set(input1);
		output.mul(input2);
	};

	/*
	 * Sets the real component of an output real number to the subtraction between
	 * the real component of two input real numbers.
	 * @input input1
	 * @input input2
	 * @container result
	 * @implNote op names='math.subtract', priority='100.'
	 */
	public final Computers.Arity2<T, T, T> subtracter = (input1, input2, output) -> {
		output.set(input1);
		output.sub(input2);
	};

}
