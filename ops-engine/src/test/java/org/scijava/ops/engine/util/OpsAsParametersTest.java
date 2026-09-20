/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

public class OpsAsParametersTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OpsAsParametersTest());
		ops.register(new FuncClass());
	}

	@OpField(names = "test.parameter.computer")
	public final Function<Number, Double> func = (x) -> x.doubleValue();

	@OpField(names = "test.parameter.op")
	public final BiFunction<List<Number>, Function<Number, Double>, List<Double>> biFunc =
		(x, op) -> {
			List<Double> output = new ArrayList<>();
			for (Number n : x)
				output.add(op.apply(n));
			return output;
		};

	@Test
	public void testOpWithNonReifiableFunction() {
		var list = Arrays.asList(40L, 20.5, 4.0d);
		var actual = ops//
			.op("test.parameter.op") //
			.input(list, func) //
			.outType(List.class) //
			.apply();
		assertEquals(Arrays.asList(40.0, 20.5, 4.0), actual);
	}

	@Test
	public void testOpWithOpFieldWithoutRun() {
		List<Number> list = new ArrayList<>();
		list.add(40l);
		list.add(20.5);
		list.add(4.0d);

		BiFunction<List<Number>, Function<Number, Double>, List<Double>> thing =
			OpBuilder.matchFunction(ops, "test.parameter.op", new Nil<List<Number>>()
			{}, new Nil<Function<Number, Double>>() {}, new Nil<List<Double>>() {});

		List<Double> output = thing.apply(list, func);
	}

	@Test
	public void testOpWithOpClass() {
		List<Number> list = new ArrayList<>();
		list.add(40l);
		list.add(20.5);
		list.add(4.0d);

		Function<Number, Double> funcClass = OpBuilder.matchFunction(ops,
			"test.parameter.class", new Nil<Number>()
			{}, new Nil<Double>() {});

		@SuppressWarnings("unused")
		List<Double> output = ops.op("test.parameter.op").input(list, funcClass)
			.outType(new Nil<List<Double>>()
			{}).apply();
	}

}

@OpClass(names = "test.parameter.class")
class FuncClass implements Function<Number, Double>, Op {

	@Override
	public Double apply(Number t) {
		return t.doubleValue() + 1;
	}

}
