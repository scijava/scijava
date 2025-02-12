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

package org.scijava.ops.engine;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

/**
 * Ensures correct behavior in {@link OpBuilder} calls <b>where no output type
 * or {@code Object} is given</b>.
 *
 * @author Gabriel Selzer
 * @see OpBuilderTest
 */
public class OpBuilderNoOutputTest<T extends Number> extends
	AbstractTestEnvironment implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OpBuilderNoOutputTest());
	}

	public final String opName = "test.noOutput";

	// private wrapper class
	private static class WrappedList<E> extends ArrayList<E> {}

	/**
	 * @input in
	 * @output out
	 */
	@OpField(names = opName)
	public final Function<T, WrappedList<T>> func = in -> {

		WrappedList<T> out = new WrappedList<>();
		out.add(in);
		return out;
	};

	@Test
	public void testNoParameterizedTypeOutputGiven() {
		Object output = ops.op(opName).input(5.).apply();
		Type expectedOutputType = new Nil<WrappedList<Double>>() {}.type();
		Assertions.assertEquals(ops.genericType(output), expectedOutputType);
	}
}
