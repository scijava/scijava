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

import com.google.common.collect.Streams;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.junit.Test;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.ops.function.Computers;
import org.scijava.types.Nil;

//TODO: think about removing this class
public class AutoTransformTest extends AbstractTestEnvironment {

	private static Nil<Iterable<Double>> nilIterableDouble = new Nil<Iterable<Double>>() {};

	@Test
	public void autoLiftFunctionToIterables() {
		// There is no sqrt function for iterables in the system, however we can auto
		// transform
		// as there is a lifter for Functions to iterables
		Function<Iterable<Double>, Iterable<Double>> sqrtFunction = ops.op( //
				"math.sqrt", new Nil<Function<Iterable<Double>, Iterable<Double>>>() {}, //
				new Nil[] { nilIterableDouble }, //
				nilIterableDouble//
		);

		Iterable<Double> res = sqrtFunction.apply(Arrays.asList(0.0, 4.0, 16.0));
		arrayEquals(Streams.stream(res).mapToDouble(d -> d).toArray(), 0.0, 2.0, 4.0);
	}

	@Test
	public void autoFuncToCompAndLiftTest() {
		// There is no sqrt computer for iterables in the system (with this name,
		// however we can auto
		// transform
		// as there is a lifter for Functions to iterables
		Computers.Arity2<Iterable<double[]>, Iterable<double[]>, Iterable<double[]>> sqrtFunction = new OpBuilder(ops,
				"test.adaptersF").inType(new Nil<Iterable<double[]>>() {}, new Nil<Iterable<double[]>>() {})
						.outType(new Nil<Iterable<double[]>>() {}).computer();

		Iterable<double[]> res = Arrays.asList(new double[] {1.0, 0.0, 0.0});
		sqrtFunction.compute(Arrays.asList(new double[] {0.0, 4.0, 16.0}), Arrays.asList(new double[] {0.0, 4.0, 16.0}), res);
		arrayEquals(res.iterator().next(), 0.0, 8.0, 16.0);
	}

	@Test
	public void autoCompToFuncAndLiftIterableToIterable() {
		// check transformation as a Function<Iterable, Iterable>
		Nil<Iterable<double[]>> n = new Nil<Iterable<double[]>>() {};

		Function<Iterable<double[]>, Iterable<double[]>> sqrtListFunction = ops.op( //
				"test.liftSqrt", new Nil<Function<Iterable<double[]>, Iterable<double[]>>>() {}, //
				new Nil[] { n }, //
				n//
		);

		Iterable<double[]> res = sqrtListFunction
				.apply(Arrays.asList(new double[] { 4.0 }, new double[] { 4.0, 25.0 }));
		double[] resArray = StreamSupport.stream(res.spliterator(), false).flatMapToDouble(ds -> Arrays.stream(ds))
				.toArray();
		arrayEquals(resArray, 2.0, 2.0, 5.0);
	}

	@Test
	public void autoCompToFuncAndLiftListToIterable() {
		// check transformation as a Function<List, Iterable>
		Nil<Iterable<double[]>> n = new Nil<Iterable<double[]>>() {};
		Nil<List<double[]>> l = new Nil<List<double[]>>() {};

		Function<List<double[]>, Iterable<double[]>> sqrtListFunction = ops.op( //
				"test.liftSqrt", new Nil<Function<List<double[]>, Iterable<double[]>>>() {}, //
				new Nil[] { l }, //
				n//
		);

		Iterable<double[]> res = sqrtListFunction
				.apply(Arrays.asList(new double[] { 4.0 }, new double[] { 4.0, 25.0 }));
		double[] resArray = StreamSupport.stream(res.spliterator(), false).flatMapToDouble(ds -> Arrays.stream(ds))
				.toArray();
		arrayEquals(resArray, 2.0, 2.0, 5.0);
	}

}
