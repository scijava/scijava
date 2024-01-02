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
		ops.register(
				new SimplifiedOpInfoTest(),
				new PrimitiveSimplifiers(),
				new PrimitiveArraySimplifiers(),
				new IdentityCollection<>(),
				new SimpleOp()
		);
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
		String expected = "test.simplifiedDescription(\n\t " //
				+
				"Inputs:\n\t\tjava.lang.Number input1\n\t\tjava.lang.Number input2\n\t " //
				+ "Output:\n\t\torg.scijava.collections.ObjectArray<java.lang.Number> output1\n)\n";
		String actual = info.toString();
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testSimplifiedExecution() {
		SimplifiedOpInfo info = createSimpleInfo();
		BiFunction<Number, Number, ObjectArray<Number>> op =
				(BiFunction<Number, Number, ObjectArray<Number>>) info.createOpInstance(Collections.emptyList()).object();
		ObjectArray<Number> expected = new ObjectArray<>(new Double[] {5., 6.});
		ObjectArray<Number> actual = op.apply(5, 6);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testSimplifiedOpFromEnvironment() {
		var op = ops.binary("test.simplifiedDescription").inType(Number.class, Number.class).outType(new Nil<ObjectArray<Number>>() {}).function();
		ObjectArray<Number> expected = new ObjectArray<>(new Double[] {5., 6.});
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

	@Test
	public void testSimpleDescriptions() {
		String actual = ops.unary("test.coalesceSimpleDescription").helpVerbose();
		String expected =  //
				"Ops:" +
				"\n\t> test.coalesceSimpleDescription(\n" +
				"\t\t Inputs:\n\t\t\tjava.util.List<java.lang.Long> input1\n" +
				"\t\t Container (I/O):\n\t\t\tjava.util.List<java.lang.Long> container1\n\t)\n\t" +
				"\n\t> test.coalesceSimpleDescription(\n" +
				"\t\t Inputs:\n\t\t\tjava.lang.Double input1\n" +
				"\t\t Output:\n\t\t\tjava.lang.Double output1\n\t)\n\t" +
				"\n\t> test.coalesceSimpleDescription(\n" +
				"\t\t Inputs:\n\t\t\tjava.lang.Long input1\n" +
				"\t\t Output:\n\t\t\tjava.lang.Long output1\n\t)\n\t";
		Assertions.assertEquals(expected, actual);

		actual = ops.unary("test.coalesceSimpleDescription").help();
		expected =  //
				"Ops:\n\t> test.coalesceSimpleDescription(\n" +
						"\t\t Inputs:\n\t\t\tList<Long> input1\n" +
						"\t\t Container (I/O):\n\t\t\tList<Long> container1\n\t)\n\t" +
						"\n\t> test.coalesceSimpleDescription(\n" +
						"\t\t Inputs:\n\t\t\tNumber input1\n" +
						"\t\t Output:\n\t\t\tNumber output1\n\t)\n\t";
		Assertions.assertEquals(expected, actual);
		// Finally test that different number of outputs doesn't retrieve the Ops
		actual = ops.nullary("test.coalesceSimpleDescription").help();
		expected = "No Ops found matching this request.";
		Assertions.assertEquals(expected, actual);
	}

}
