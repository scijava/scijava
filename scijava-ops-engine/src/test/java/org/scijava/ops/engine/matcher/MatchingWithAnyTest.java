/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Producer;
import org.scijava.ops.api.Ops;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaSource;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.priority.Priority;
import org.scijava.common3.Any;

/**
 * Tests op matcher functionality with {@link Any} types.
 *
 * @author Gabriel Selzer
 */
public class MatchingWithAnyTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new MatchingWithAnyTest());
		ops.register(
			new ComputersToFunctionsViaSource.Computer2ToFunction2ViaSource());
	}

	@Test
	public void testAny() {

		NestedThing<String, Thing<String>> nthing = new NestedThing<>();
		Double e = ops.op("test.nestedAny").input(nthing).outType(Double.class)
			.apply();

		Thing<Double> thing = new Thing<>();
		Double d = ops.op("test.any").input(thing).outType(Double.class).apply();

		assert d == 5.;
		assert e == 5.;

	}

	/**
	 * NOTE: this is where ops.run() and the Any paradigm fail. However, this can
	 * easily be avoided by making TypeExtractors for any class for which this
	 * kind of exception can happen.
	 */
	@Test
	public void testExceptionalThing() {

		ExceptionalThing<Double> ething = new ExceptionalThing<>(0.5);
		assertThrows(ClassCastException.class, () -> {
			Double d = ops.op("test.exceptionalAny").input(ething).outType(
				Double.class).apply();
		});

	}

	// TODO: Note that this wouldn't work for Computer -> Function because here
	// LiftFunctionToArrayTransformer is the first transformer which is asked for
	// source refs. This transformer doesn't support Any and would fail.
	// TODO: can we remove this test?
	@Test
	public void testRunAnyFunction2FromComputer2() {
		final int in1 = 11;
		final long in2 = 31;
		final StringContainer out = ops.op("test.integerAndLongAndNotAnyComputer")
			.input(in1, in2).outType(StringContainer.class).apply();
		assertEquals(Long.toString(in1 + in2), out.getValue());
	}

	@Test
	public void testMatchingAnyWithDependencies() {
		var op = ops.op("test.AnyWithDependencies").inType(Any.class).outType(
			Double.class).function();
		var richOp = Ops.rich(op);
		var info = Ops.info(op);
		Assertions.assertTrue(info.toString().contains("dependentAnyOp"));
		var dInfo = richOp.infoTree().dependencies().get(0);
		Assertions.assertTrue(dInfo.toString().contains("lowPriority"));
	}

	@OpMethod(names = "test.AnyWithDependencies", type = Function.class)
	public static <N extends Number> Double dependentAnyOp(@OpDependency(
		name = "test.AnyDependent") Function<N, Double> e, List<N> input)
	{
		return input.stream().map(e).reduce(0.0, Double::sum);
	}

	@OpMethod(names = "test.AnyDependent", type = Function.class,
		priority = Priority.HIGH)
	public static Double highPriorityDependingOp(String input) {
		throw new IllegalStateException("This should not be called!");
	}

	@OpMethod(names = "test.AnyDependent", type = Function.class,
		priority = Priority.LOW)
	public static Double lowPriorityDependingOp(Number input) {
		return input.doubleValue();
	}

	@OpField(names = "test.functionAndLongToLong")
	public final BiFunction<Function<Long, Long>, Long, Long> funcAndLongToLong = //
		(t, u) -> t.apply(u);

	@OpField(names = "test.integerAndLongAndNotAnyComputer")
	public final Computers.Arity2<Integer, Long, StringContainer> integerAndLongAndNotAnyComputer = //
		(in1, in2, out) -> out.setValue(Long.toString(in1 + in2));

	@OpField(names = "engine.create, create.stringContainer")
	public final Producer<StringContainer> stringContainerCreator = //
		StringContainer::new;

	@OpField(names = "test.any")
	public final Function<Thing<String>, Double> thingFunction = //
		(t) -> t.create("Hello");

	@OpField(names = "test.exceptionalAny")
	public final Function<ExceptionalThing<String>, Double> exceptionalThingFunction = //
		(t) -> {
			String s = t.getU();
			return t.create("Hello");
		};

	@OpField(names = "test.nestedAny")
	public final Function<NestedThing<String, Thing<String>>, Double> nestedThingFunction = //
		(t) -> {
			return 5.;
		};

}

class StringContainer {

	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

class Thing<U> {

	public double create(U u) {
		return 5.;
	}
}

class ExceptionalThing<U> {

	public ExceptionalThing(U u) {
		thing = u;
	};

	U thing;

	U getU() {
		return thing;
	}

	public double create(U u) {
		thing = u;
		return 5.;
	}
}

class NestedThing<U, V extends Thing<?>> {

	public double create(V u) {
		return 5.;
	}
}
