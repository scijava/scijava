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

package org.scijava.ops.engine.matcher.impl;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.engine.exceptions.impl.PrivateOpException;
import org.scijava.ops.spi.OpCollection;

public class DefaultOpFieldInfoTest implements OpCollection {

	private final BiFunction<Double, Double, Double> foo = Double::sum;

	@Test
	public void testPrivateField() throws NoSuchFieldException {
		var field = this.getClass().getDeclaredField("foo");
		Assertions.assertThrows(PrivateOpException.class, //
			() -> new DefaultOpFieldInfo(//
				this, //
				field, //
				Versions.of(this.getClass()), //
				"", //
				new Hints(), //
				1.0, //
				"foo" //
			));
	}

	public final Computers.Arity1<Double, Double> bar = (in, out) -> out = in;

	@Test
	public void testImmutableOutput() throws NoSuchFieldException {
		var field = this.getClass().getDeclaredField("bar");
		Assertions.assertThrows(FunctionalTypeOpException.class, //
			() -> new DefaultOpFieldInfo(//
				this, //
				field, //
				Versions.of(this.getClass()), //
				"", //
				new Hints(), //
				1.0, //
				"bar" //
			));
	}
}
