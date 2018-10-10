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
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.types.Nil;

import com.google.common.collect.Streams;

public class AutoTransformTest extends AbstractTestEnvironment {

	private static Nil<Iterable<Double>> nilIterableDouble = new Nil<Iterable<Double>>() {
	};

	@Test
	public void autoLiftFunctionToIterables() {
		// There is no sqrt function for iterables in the system, however we can auto transform
		// as there is a lifter for Functions to iterables
		Function<Iterable<Double>, Iterable<Double>> sqrtFunction = ops().findOp( //
				"math.sqrt", new Nil<Function<Iterable<Double>, Iterable<Double>>>() {
				}, //
				new Nil[] { nilIterableDouble }, //
				nilIterableDouble//
		);
		
		Iterable<Double> res = sqrtFunction.apply(Arrays.asList(0.0, 4.0, 16.0));
		arrayEquals(Streams.stream(res).mapToDouble(d -> d).toArray(), 0.0, 2.0, 4.0);
	}
	
	@Test
	public void autoFunctionToComputer() {
		Function<double[], double[]> sqrtArrayFunction = ops().findOp( //
				"math.sqrt", new Nil<Function<double[], double[]>>() {
				}, //
				new Nil[] { Nil.of(double[].class) }, //
				Nil.of(double[].class)//
		);
		double[] res = sqrtArrayFunction.apply(new double[]{4.0, 16.0});
		arrayEquals(res, 2.0, 4.0);
	}
	
	@Test
	@Test
	public void autoLiftFuncToArray() {
		Function<Double[], Double[]> power3ArraysFunc = ops().findOp( //
				"math.pow", new Nil<Function<Double[], Double[]>>() {
				}, //
				new Nil[] { Nil.of(Double[].class), Nil.of(double.class) }, //
				Nil.of(Double[].class), //
				3.0//
		);
		
		Double[] result = power3ArraysFunc.apply(new Double[] { 1.0, 2.0, 3.0 });
		assert arrayEquals(Arrays.stream(result).mapToDouble(d -> d).toArray(), 1.0, 8.0, 27.0);
	}
	
	public void autoCompToFuncAndLift() {
		Nil<List<double[]>> n = new Nil<List<double[]>>() {
		};
		
		Function<List<double[]>, List<double[]>> sqrtListFunction = ops().findOp( //
				"math.sqrt", new Nil<Function<List<double[]>, List<double[]>>>() {
				}, //
				new Nil[] { n }, //
				n//
		);
		
		List<double[]> res = sqrtListFunction.apply(Arrays.asList(new double[]{4.0}, new double[]{4.0, 25.0}));
		double[] resArray = res.stream().flatMapToDouble(ds -> Arrays.stream(ds)).toArray();
		arrayEquals(resArray, 2.0, 2.0, 5.0);
	}
}
