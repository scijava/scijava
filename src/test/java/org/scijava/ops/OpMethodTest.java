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
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * @author Marcel Wiedenmann
 */
@Plugin(type = OpCollection.class)
public class OpMethodTest extends AbstractTestEnvironment {

	@Test
	public void testParseIntegerOp() {
		// Will match a lambda created and returned by createParseIntegerOp() below.
		final Function<String, Integer> parseIntegerOp = Functions.unary(ops, "test.parseInteger", new Nil<String>() {},
			new Nil<Integer>()
			{});

		final String numericString = "42";
		final Integer parsedInteger = parseIntegerOp.apply(numericString);
		assertEquals(Integer.parseInt(numericString), (int) parsedInteger);
	}

	@Test
	public void testMultiplyNumericStringsOpMethod() {
		// Will match a lambda created and returned by
		// createMultiplyNumericStringsOp(..), which itself captured a lambda
		// created and returned by createParseIntegerOp().
		final BiFunction<String, String, Integer> multiplyNumericStringsOp = Functions.binary(ops,
			"test.multiplyNumericStrings", new Nil<String>()
			{}, new Nil<String>() {}, new Nil<Integer>() {});

		final String numericString1 = "3";
		final String numericString2 = "18";
		final Integer multipliedNumericStrings = multiplyNumericStringsOp.apply(numericString1, numericString2);
		assertEquals(Integer.parseInt(numericString1) * Integer.parseInt(numericString2), (int) multipliedNumericStrings);
	}

	@OpMethod(names = "test.parseInteger")
	// Refers to the input parameter of the function that's returned by this
	// factory method.
	@Parameter(key = "numericString")
	// Refers to the output parameter of the function.
	@Parameter(key = "parsedInteger", type = ItemIO.OUTPUT)
	public Function<String, Integer> createParseIntegerOp() {
		return Integer::parseInt;
	}

	@OpMethod(names = "test.multiplyNumericStrings")
	@Parameter(key = "numericString1")
	@Parameter(key = "numericString2")
	@Parameter(key = "multipliedNumericStrings", type = ItemIO.OUTPUT)
	// Refers to the first parameter of this factory method ("parseIntegerOp").
	// Will be automatically provided by the matching system. Can be used by the
	// returned lambda/Op as if it was an @OpDependency-annotated field in a
	// regular Op class.
	@OpDependency(name = "test.parseInteger")
	public BiFunction<String, String, Integer> createMultiplyNumericStringsOp(
		final Function<String, Integer> parseIntegerOp)
	{
		return (i1, i2) -> parseIntegerOp.apply(i1) * parseIntegerOp.apply(i2);
	}
}
