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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.core.Computer;
import org.scijava.ops.util.Maps;
import org.scijava.param.ValidityException;
import org.scijava.ops.types.Nil;

import com.google.common.collect.Streams;

public class LiftTest extends AbstractTestEnvironment {

	Nil<Double> nilDouble = new Nil<Double>() {};

	Nil<double[]> nilDoubleArray = new Nil<double[]>() {};

	@Test
	public void testliftFunction() throws ValidityException {
		Function<Double, Double> powFunction = ops().findOp( //
				"math.pow", new Nil<Function<Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble, 2.0//
		);

		Function<Iterable<Double>, Iterable<Double>> liftedToIterable = Maps.Functions.Iterables.liftBoth(powFunction);
		Iterable<Double> res2 = liftedToIterable.apply(Arrays.asList(1.0, 2.0, 3.0, 4.0));

		arrayEquals(toArray(res2), 1.0, 4.0, 9.0, 16.0);
	}

	private static double[] toArray(Iterable<Double> iter) {
		return Streams.stream(iter).mapToDouble(d -> d).toArray();
	}

	@Test
	public void testliftComputer() throws ValidityException {

		Computer<double[], double[]> powComputer = ops().findOp( //
				"math.pow", new Nil<Computer<double[], double[]>>() {
				}, //
				new Nil[] { nilDoubleArray, nilDoubleArray, nilDouble }, //
				nilDoubleArray, 2.0//
		);

		Computer<Iterable<double[]>, Iterable<double[]>> liftedToIterable = Maps.Computers.Iterables.liftBoth(powComputer);
		Iterable<double[]> res = wrap(new double[4]);
		liftedToIterable.compute(wrap(new double[]{1.0, 2.0, 3.0, 4.0}), res);
		
		arrayEquals(unwrap(res), 1.0, 4.0, 9.0, 16.0);
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
