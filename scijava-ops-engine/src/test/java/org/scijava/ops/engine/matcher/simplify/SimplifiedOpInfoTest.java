/*-
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

package org.scijava.ops.engine.matcher.simplify;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.collections.ObjectArray;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

public class SimplifiedOpInfoTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new SimplifiedOpInfoTest(), new PrimitiveSimplifiers(),
			new PrimitiveArraySimplifiers(), new IdentityCollection<>(),
			new SimpleOp());
	}

	@OpClass(names = "test.simplifiedDescription")
	static class SimpleOp implements BiFunction<Double, Double, Double[]>, Op {

		@Override
		public Double[] apply(Double t, Double u) {
			return new Double[] { t, u };
		}
	}

	private SimplifiedOpInfo createSimpleInfo() {
		OpClassInfo info = new OpClassInfo(SimpleOp.class, new Hints(),
			"test.simplifiedDescription");
		return SimplificationUtils.simplifyInfo(ops, info);
	}

	@Test
	public void testSimplifiedDescription() {
		SimplifiedOpInfo info = createSimpleInfo();
		String expected =
			"org.scijava.ops.engine.matcher.simplify.SimplifiedOpInfoTest$SimpleOp|simple\n\t" //
				+ "> input1 : java.lang.Number\n\t" //
				+ "> input2 : java.lang.Number\n\t" //
				+ "Returns : org.scijava.collections.ObjectArray<java.lang.Number>";
		String actual = info.toString();
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testSimplifiedExecution() {
		SimplifiedOpInfo info = createSimpleInfo();
		BiFunction<Number, Number, ObjectArray<Number>> op =
			(BiFunction<Number, Number, ObjectArray<Number>>) info.createOpInstance(
				Collections.emptyList()).object();
		ObjectArray<Number> expected = new ObjectArray<>(new Double[] { 5., 6. });
		ObjectArray<Number> actual = op.apply(5, 6);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testSimplifiedOpFromEnvironment() {
		var op = ops.binary("test.simplifiedDescription").inType(Number.class,
			Number.class).outType(new Nil<ObjectArray<Number>>()
		{}).function();
		ObjectArray<Number> expected = new ObjectArray<>(new Double[] { 5., 6. });
		ObjectArray<Number> actual = op.apply(5, 6);
		Assertions.assertEquals(expected, actual);
	}

	@OpField(names = "test.coalesceSimpleDescription")
	public final Function<Double, Double> func1 = in -> in + 1.;

	@OpField(names = "test.coalesceSimpleDescription")
	public final Function<Long, Long> func2 = in -> in + 1;

	@OpField(names = "test.coalesceSimpleDescription")
	public final Computers.Arity1<List<Long>, List<Long>> comp1 = (in, out) -> {
		out.clear();
		out.addAll(in);
	};

	@OpField(names = "test.coalesceSimpleDescription")
	public final Inplaces.Arity2_1<List<Long>, Long> inplace1 = (in1, in2) -> {
		in1.clear();
		in1.add(in2);
	};

	@Test
	public void testSimpleDescriptions() {
		String actual = ops.unary("test.coalesceSimpleDescription").helpVerbose();
		String expected = "test.coalesceSimpleDescription:\n" +
			"\t- org.scijava.ops.engine.matcher.simplify.SimplifiedOpInfoTest$comp1\n" +
			"\t\t> input1 : java.util.List<java.lang.Long>\n" +
			"\t\t> container1 : @CONTAINER java.util.List<java.lang.Long>\n" +
			"\t- org.scijava.ops.engine.matcher.simplify.SimplifiedOpInfoTest$func1\n" +
			"\t\t> input1 : java.lang.Double\n" + "\t\tReturns : java.lang.Double\n" +
			"\t- org.scijava.ops.engine.matcher.simplify.SimplifiedOpInfoTest$func2\n" +
			"\t\t> input1 : java.lang.Long\n" + "\t\tReturns : java.lang.Long";

		Assertions.assertEquals(expected, actual);

		actual = ops.unary("test.coalesceSimpleDescription").help();
		expected = //
			"test.coalesceSimpleDescription:\n\t- (input1, @CONTAINER container1) -> None\n\t- (input1) -> Number";
		Assertions.assertEquals(expected, actual);

		// Test that with 2 inputs we do get the binary inplace Op, but no others
		actual = ops.binary("test.coalesceSimpleDescription").help();
		expected =  //
				"test.coalesceSimpleDescription:\n\t- (@MUTABLE mutable1, input1) -> None";
		Assertions.assertEquals(expected, actual);

		// Finally test that with no inputs we don't get any of the Ops
		actual = ops.nullary("test.coalesceSimpleDescription").help();
		expected = "No Ops found matching this request.";
		Assertions.assertEquals(expected, actual);
	}

}
