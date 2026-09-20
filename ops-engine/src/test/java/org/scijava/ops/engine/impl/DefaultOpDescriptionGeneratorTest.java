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

package org.scijava.ops.engine.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.OpDescriptionGenerator;
import org.scijava.ops.engine.describe.BaseDescriptors;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Tests basic behavior of {@link DefaultOpDescriptionGenerator}.
 *
 * @author Gabriel Selzer
 */
public class DefaultOpDescriptionGeneratorTest extends AbstractTestEnvironment
	implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register( //
			new DefaultOpDescriptionGeneratorTest(), //
			new BaseDescriptors() //
		);
	}

	@OpField(names = "test.coalesceDescription")
	public final Function<Double, Double> func1 = in -> in + 1.;

	@OpField(names = "test.coalesceDescription")
	public final Function<Long, Long> func2 = in -> in + 1;

	@OpField(names = "test.coalesceDescription")
	public final Computers.Arity1<List<Long>, List<Long>> comp1 = (in, out) -> {
		out.clear();
		out.addAll(in);
	};

	@OpField(names = "test.coalesceDescription")
	public final Inplaces.Arity2_1<List<Long>, Long> inplace1 = (in1, in2) -> {
		in1.clear();
		in1.add(in2);
	};

	/**
	 * Tests that, when multiple Ops declare the same number of parameters, and
	 * when each corresponding parameter index is described in the same way, that
	 * the two Ops are coalesced into one entry in the help message.
	 */
	@Test
	public void testCoalescedDescriptions() {
		String actual = ops.op("test.coalesceDescription").helpVerbose();
		String expected = "test.coalesceDescription:\n" +
			"\t- org.scijava.ops.engine.impl.DefaultOpDescriptionGeneratorTest$comp1\n" +
			"\t\t> input1 : java.util.List<java.lang.Long>\n" +
			"\t\t> container1 : @CONTAINER java.util.List<java.lang.Long>\n" +
			"\t- org.scijava.ops.engine.impl.DefaultOpDescriptionGeneratorTest$func1\n" +
			"\t\t> input1 : java.lang.Double\n" + "\t\tReturns : java.lang.Double\n" +
			"\t- org.scijava.ops.engine.impl.DefaultOpDescriptionGeneratorTest$func2\n" +
			"\t\t> input1 : java.lang.Long\n" + "\t\tReturns : java.lang.Long\n" +
			"\t- org.scijava.ops.engine.impl.DefaultOpDescriptionGeneratorTest$inplace1\n" +
			"\t\t> mutable1 : @MUTABLE java.util.List<java.lang.Long>\n" +
			"\t\t> input1 : java.lang.Long";
		// Assert that helpVerbose returns four entries
		Assertions.assertEquals(expected, actual);
		// But assert that only three are seen for help
		actual = ops.op("test.coalesceDescription").help();
		expected = //
			"test.coalesceDescription:\n" +
				"\t- (list<number>, @CONTAINER list<number>) -> None\n" +
				"\t- (number) -> number\n" +
				"\t- (@MUTABLE list<number>, number) -> None";
		Assertions.assertEquals(expected, actual);
		// Test that with 2 inputs we do get the binary inplace Op, but no others
		actual = ops.op("test.coalesceDescription").input(null, null).help();
		expected = //
			"test.coalesceDescription:\n\t- (@MUTABLE list<number>, number) -> None";
		Assertions.assertEquals(expected, actual);
		// Finally test that with no inputs we don't get any of the Ops
		actual = ops.op("test.coalesceDescription").output(null).help();
		expected = OpDescriptionGenerator.NO_OP_MATCHES;
		Assertions.assertEquals(expected, actual);
	}

	private static final String TEST_DESC = "Adds two integers";

	@OpField(names = "math.add", description = TEST_DESC)
	public final BiFunction<Integer, Integer, Integer> functionAdder = //
		Integer::sum;

	/**
	 * Tests that, when an {@link OpInfo} provides a description, it is included
	 * in the description. The double usage of descriptions is definitely
	 * confusing!
	 */
	@Test
	public void testDescriptionWithDescription() {
		String actual = ops.op("math.add").helpVerbose();
		String expected = "math.add:\n" +
			"\t- org.scijava.ops.engine.impl.DefaultOpDescriptionGeneratorTest$functionAdder\n" +
			"\t\t" + TEST_DESC + "\n" + "\t\t> input1 : java.lang.Integer\n" +
			"\t\t> input2 : java.lang.Integer\n" + "\t\tReturns : java.lang.Integer";
		// Assert that helpVerbose shows the description as a part of the result.
		Assertions.assertEquals(expected, actual);
	}

}
