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

package org.scijava.ops.engine.matcher.impl;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Producer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.engine.exceptions.impl.InstanceOpMethodException;
import org.scijava.ops.engine.exceptions.impl.PrivateOpException;

public class OpMethodInfoTest {

	private static Boolean privateOp() {
		return true;
	}

	@Test
	public void testPrivateMethod() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("privateOp");
		Assertions.assertThrows(PrivateOpException.class, //
			() -> new DefaultOpMethodInfo( //
				m, //
				Producer.class, //
				"1.0", //
				"", //
				new Hints(), //
				1.0, //
				"privateOp"));
	}

	public Boolean instanceOp() {
		return true;
	}

	@Test
	public void testInstanceMethod() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("instanceOp");
		Assertions.assertThrows(InstanceOpMethodException.class, //
			() -> new DefaultOpMethodInfo( //
				m, //
				Producer.class, //
				"1.0", //
				"", //
				new Hints(), //
				1.0, //
				"instanceOp"));
	}

	public static Boolean staticOp() {
		return true;
	}

	@Test
	public void testNonFuncIFace() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("staticOp");
		Assertions.assertThrows(FunctionalTypeOpException.class,
			() -> new DefaultOpMethodInfo( //
				m, //
				getClass(), //
				"1.0", //
				"", //
				new Hints(), //
				1.0, //
				"staticOp"));
	}

	@Test
	public void testWrongOpType() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("staticOp");
		Assertions.assertThrows(FunctionalTypeOpException.class,
			() -> new DefaultOpMethodInfo( //
				m, //
				Function.class, //
				"1.0", //
				"", //
				new Hints(), //
				1.0, //
				"staticOp"));
	}

	public static void mutateDouble(final Double input, final Double io) {}

	@Test
	public void testImmutableOutput() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("mutateDouble", Double.class,
			Double.class);
		Assertions.assertThrows(FunctionalTypeOpException.class,
			() -> new DefaultOpMethodInfo( //
				m, //
				Computers.Arity1.class, //
				"1.0", //
				"", //
				new Hints(), //
				1.0, //
				"mutateDouble"));
	}
}
