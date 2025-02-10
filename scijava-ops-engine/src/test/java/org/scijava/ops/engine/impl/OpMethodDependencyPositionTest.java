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

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.exceptions.impl.OpDependencyPositionException;
import org.scijava.ops.engine.matcher.impl.DefaultOpMethodInfo;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;

public class OpMethodDependencyPositionTest extends AbstractTestEnvironment
	implements OpCollection
{

	public static void goodDep( //
		@OpDependency(name = "someDep") Function<String, Long> op, //
		List<String> in, //
		List<Long> out //
	) {
		out.clear();
		for (String s : in)
			out.add(op.apply(s));
	}

	@Test
	public void testOpDependencyBefore() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod(//
			"goodDep", //
			Function.class, //
			List.class, //
			List.class //
		);
		// The below call should not throw an Exception
		Assertions.assertDoesNotThrow(() -> new DefaultOpMethodInfo( //
			m, //
			Computers.Arity1.class, //
			"1.0", //
			"", new Hints(), //
			1.0, //
			"test.dependencyBeforeInput" //
		));
	}

	public static void badDep( //
		List<String> in, //
		@OpDependency(name = "someDep") Function<String, Long> op, //
		List<Long> out //
	) {
		out.clear();
		for (String s : in)
			out.add(op.apply(s));
	}

	@Test
	public void testOpDependencyAfter() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod(//
			"badDep", //
			List.class, //
			Function.class, //
			List.class //
		);
		createInvalidInfo(m, Computers.Arity1.class, "test.dependencyAfterInput");
	}

	public static void goodThenBadDep( //
		@OpDependency(name = "someDep") Function<String, Long> op, //
		List<String> in, //
		@OpDependency(name = "someOtherDep") Function<String, Long> op2, //
		List<Long> out //
	) {
		out.clear();
		for (String s : in)
			out.add(op.apply(s));
	}

	@Test
	public void testOpDependencyBeforeAndAfter() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod(//
			"goodThenBadDep", //
			Function.class, //
			List.class, //
			Function.class, //
			List.class //
		);
		createInvalidInfo(m, Computers.Arity1.class,
			"test.dependencyBeforeAndAfterInput");
	}

	/**
	 * Helper method for testing ops with dependencies before other params
	 */
	private void createInvalidInfo(Method m, Class<?> arity, String... names) {
		Assertions.assertThrows(OpDependencyPositionException.class,
			() -> new DefaultOpMethodInfo( //
				m, //
				arity, //
				"1.0", //
				"", //
				new Hints(), //
				1.0, //
				names));
	}
}
