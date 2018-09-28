/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
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

package org.scijava.ops;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.scijava.param.ValidityException;
import org.scijava.ops.types.Nil;

public class OptionalParametersTest extends AbstractTestEnvironment {

	Nil<Double> nilDouble = new Nil<Double>() {
	};
	
	Nil<double[]> nilDoubleArray = new Nil<double[]>() {
	};
	
	@Test
	public void testSecondaryArgs() throws ValidityException {
		double min = 1.0;
		double max = 2.9;
		
		// Min and max set
		Function<double[], double[]> normFunction = ops().findOp( //
				"minmax", new Nil<Function<double[], double[]>>() {
				}, //
				new Nil[] { nilDoubleArray, nilDouble, nilDouble }, //
				nilDoubleArray,
				min, max//
		);
		
		// Max is not required, one arg is give for required min, max will be default 1.0
		double[] nums = IntStream.range(0, 10).mapToDouble(i -> i).toArray();
		double[] res = normFunction.apply(nums);
		assertTrue(Math.abs(max - Arrays.stream(res).max().getAsDouble()) < 0.000001);
		assertTrue(Math.abs(min - Arrays.stream(res).min().getAsDouble()) < 0.000001);
		
		normFunction = ops().findOp( //
				"minmax", new Nil<Function<double[], double[]>>() {
				}, //
				new Nil[] { nilDoubleArray, nilDouble, nilDouble }, //
				nilDoubleArray,
				0.5//
		);
		
		res = normFunction.apply(nums);
		assertTrue(Math.abs(1.0 - Arrays.stream(res).max().getAsDouble()) < 0.000001);
		assertTrue(Math.abs(0.5 - Arrays.stream(res).min().getAsDouble()) < 0.000001);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testRequiredMissing() throws ValidityException {
		// One required, but none are given
		ops().findOp( //
				"minmax", new Nil<Function<double[], double[]>>() {
				}, //
				new Nil[] { nilDoubleArray, nilDouble, nilDouble }, //
				nilDoubleArray
		);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testTooMannyArgs() throws ValidityException {
		// One required, one optional, but three given
		ops().findOp( //
				"minmax", new Nil<Function<double[], double[]>>() {
				}, //
				new Nil[] { nilDoubleArray, nilDouble, nilDouble }, //
				nilDoubleArray,
				1,2,3
		);
	}
}
