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

package org.scijava.ops.engine.matcher.convert;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class ConversionIOTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new CopyOpCollection<>());
		ops.register(new IdentityCollection<>());
		ops.register(new PrimitiveConverters<>());
		ops.register(new PrimitiveArrayConverters<>());
		ops.register(new ConversionIOTest());
		ops.register(new UtilityConverters());
	}

	@OpField(names = "test.math.square")
	public final Function<Double, Double> squareOp = in -> in * in;

	@Test
	public void testFunctionOutputConversion() {
		Integer in = 4;
		Integer square = ops.op("test.math.square").arity1().input(in).outType(
			Integer.class).apply();

		assertEquals(square, 16, 0.);
	}

	@OpField(names = "test.math.square")
	public final Computers.Arity1<Double[], Double[]> squareArray = (in, out) -> {
		for (int i = 0; i < in.length && i < out.length; i++) {
			out[i] = squareOp.apply(in[i]);
		}
	};

	@OpField(names = "test.math.add")
	public final Inplaces.Arity2_1<Double[], Double[]> addArray1 = (io, in1) -> {
		for (int i = 0; i < io.length && i < in1.length; i++) {
			io[i] += in1[i];
		}
	};

	@OpField(names = "test.math.add")
	public final Inplaces.Arity2_2<Double[], Double[]> addArray2 = (in0, io) -> {
		for (int i = 0; i < io.length && i < in0.length; i++) {
			io[i] += in0[i];
		}
	};

	@Test
	public void basicComputerTest() {
		Integer[] in = new Integer[] { 1, 2, 3 };
		Integer[] out = new Integer[] { 4, 5, 6 };

		ops.op("test.math.square").arity1().input(in).output(out).compute();
		assertArrayEquals(out, new Integer[] { 1, 4, 9 });
	}

	@Test
	public void basicInplace2_1Test() {
		Integer[] io = new Integer[] { 1, 2, 3 };
		Integer[] in1 = new Integer[] { 4, 5, 6 };
		Integer[] expected = new Integer[] { 5, 7, 9 };

		ops.op("test.math.add").arity2().input(io, in1).mutate1();
		assertArrayEquals(io, expected);
	}

	@Test
	public void basicInplace2_2Test() {
		Integer[] in0 = new Integer[] { 4, 5, 6 };
		Integer[] io = new Integer[] { 1, 2, 3 };
		Integer[] expected = new Integer[] { 5, 7, 9 };

		ops.op("test.math.add").arity2().input(in0, io).mutate2();
		assertArrayEquals(io, expected);
	}

}
