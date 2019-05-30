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

import static org.junit.Assert.assertEquals;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.core.Op;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * @author Marcel Wiedenmann
 * @see OpMethodTest
 */
public class OpConstructorTest extends AbstractTestEnvironment {

	@Test
	public void testMultiplyNumericStringsOpMethod() {
		final BiFunction<String, String, Integer> multiplyNumericStringsOp = Functions.binary(ops,
			"test.multiplyNumericStringsClass", new Nil<String>()
			{}, new Nil<String>() {}, new Nil<Integer>() {});

		final String numericString1 = "3";
		final String numericString2 = "18";
		final Integer multipliedNumericStrings = multiplyNumericStringsOp.apply(numericString1, numericString2);
		assertEquals(Integer.parseInt(numericString1) * Integer.parseInt(numericString2), (int) multipliedNumericStrings);
	}

	/**
	 * The advantage of this approach over OpMethod is that we don't need to
	 * create an additional factory method (+ an enclosing OpCollection). This is
	 * mostly relevant for more complex Ops that deserve to live in their own
	 * class file. More simple Ops don't need to be proper classes but can be
	 * lambdas built within their factory methods.
	 */
	@Plugin(type = Op.class, name = "test.multiplyNumericStringsClass")
	@Parameter(key = "numericString1")
	@Parameter(key = "numericString2")
	@Parameter(key = "multipliedNumericStrings", type = ItemIO.OUTPUT)
	public static class MultiplyNumericStringsOp implements BiFunction<String, String, Integer> {

		private final Function<String, Integer> parseIntegerOp;

		@OpDependency(name = "test.parseInteger")
		public MultiplyNumericStringsOp(final Function<String, Integer> parseIntegerOp) {
			this.parseIntegerOp = parseIntegerOp;
		}

		@Override
		public Integer apply(String t, String u) {
			return parseIntegerOp.apply(t) * parseIntegerOp.apply(u);
		}
	}
}
