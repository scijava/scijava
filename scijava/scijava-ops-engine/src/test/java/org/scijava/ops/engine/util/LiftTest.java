/*
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

package org.scijava.ops.engine.util;

import com.google.common.collect.Streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.TestOps;
import org.scijava.types.Nil;

public class LiftTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		Object[] objects = objsFromNoArgConstructors(TestOps.class.getDeclaredClasses());
		ops.register(objects);
	}

	Nil<Double> nilDouble = new Nil<>() {
	};

	Nil<double[]> nilDoubleArray = new Nil<>() {
	};

	@Test
	public void testliftFunction(){
		Function<Double, Double> powFunction = OpBuilder.matchFunction(ops, "test.liftFunction", nilDouble, nilDouble);

		Function<Iterable<Double>, Iterable<Double>> liftedToIterable = Maps.FunctionMaps.Iterables.liftBoth(powFunction);
		Iterable<Double> res2 = liftedToIterable.apply(Arrays.asList(1.0, 2.0, 3.0, 4.0));
		Assertions.assertTrue(arrayEquals(toArray(res2), 2.0, 3.0, 4.0, 5.0));

		Function<Double[], Double[]> liftedToArray = Maps.FunctionMaps.Arrays.liftBoth(powFunction, Double.class);
		Double[] res3 = liftedToArray.apply(new Double[] { 1.0, 2.0, 3.0, 4.0 });
		Assertions.assertTrue(arrayEquals(Arrays.stream(res3).mapToDouble(d -> d).toArray(), 2.0, 3.0, 4.0, 5.0));
	}

	private static double[] toArray(Iterable<Double> iter) {
		return Streams.stream(iter).mapToDouble(d -> d).toArray();
	}

	@Test
	public void testliftComputer() {

		Computers.Arity1<double[], double[]> powComputer = OpBuilder.matchComputer(ops, "test.liftComputer", nilDoubleArray, nilDoubleArray);

		Computers.Arity1<Iterable<double[]>, Iterable<double[]>> liftedToIterable = Maps.ComputerMaps.Iterables
				.liftBoth(powComputer);
		Iterable<double[]> res = wrap(new double[4]);
		liftedToIterable.compute(wrap(new double[] { 1.0, 2.0, 3.0, 4.0 }), res);

		Assertions.assertTrue(arrayEquals(unwrap(res), 2.0, 3.0, 4.0, 5.0));
	}

	private static double[] unwrap(Iterable<double[]> ds) {
		List<Double> wraps = new ArrayList<>();
		for (double[] d : ds) {
			wraps.add(d[0]);
		}
		return wraps.stream().mapToDouble(d -> d).toArray();
	}

	private static Iterable<double[]> wrap(double... ds) {
		List<double[]> wraps = new ArrayList<>();
		for (double d : ds) {
			wraps.add(new double[] { d });
		}
		return wraps;
	}
}
