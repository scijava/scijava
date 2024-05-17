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

import java.util.Arrays;
import java.util.function.Function;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpMethod;

/**
 * A simple showcase of an Op that uses an {@link OpDependency}. Using
 * {@link OpDependency}s to break up your code into simple, reusable blocks, Ops
 * become more modular and enable specialization for unseen types.
 *
 * @author Gabriel Selzer
 */
public class OpDependencies {

	/**
	 * An Op that computes the size of a {@link double[]}
	 *
	 * @param inArray the input
	 * @return the size of {@code inArray}
	 * @implNote op names="stats.size"
	 */
	public static double size(final double[] inArray) {
		return inArray.length;
	}

	/**
	 * An Op that computes the sum of a {@link double[]}
	 *
	 * @param inArray the input
	 * @return the sum of {@code inArray}
	 * @implNote op names="stats.sum"
	 */
	public static double sum(final double[] inArray) {
		return Arrays.stream(inArray).sum();
	}

	/**
	 * An Op that computes the mean of a {@link double[]}
	 *
	 * @param sumOp an Op that computes the sum of the a {@link double[]}
	 * @param sizeOp an Op that computes the size of the a {@link double[]}
	 * @param inArray the input
	 * @return the mean of {@code inArray}
	 * @implNote op names="stats.mean"
	 */
	public static double mean( //
		@OpDependency(name = "stats.sum") Function<double[], Double> sumOp, //
		@OpDependency(name = "stats.size") Function<double[], Double> sizeOp, //
		final double[] inArray)
	{
		return sumOp.apply(inArray) / sizeOp.apply(inArray);
	}

	public static void main(String... args) {
		OpEnvironment ops = OpEnvironment.build();
		// The mean of this array is 3.0
		double[] arr = { 1.0, 2.0, 3.0, 4.0, 5.0 };

		// Note that we call only deal with the "stats.mean" Op - the Engine takes
		// care of matching the dependencies for us!
		double mean = ops.op("stats.mean").input(arr).outType(Double.class).apply();

		System.out.println("The mean of array " + Arrays.toString(arr) + " is " +
			mean);

	}

}
