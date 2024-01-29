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

package org.scijava.ops.engine.matcher.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.function.Producer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.exceptions.impl.FinalOpDependencyFieldException;
import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.spi.OpDependency;

public class OpClassInfoTest {

	static class FinalOpDependency implements Producer<String> {

		@OpDependency(name = "foo")
		public final Producer<Boolean> foo = null;

		@Override
		public String create() {
			return "This Op has a final OpDependency";
		}
	}

	@Test
	public void testFinalDependency() {
		Assertions.assertThrows(FinalOpDependencyFieldException.class, () -> //
		new OpClassInfo( //
			FinalOpDependency.class, //
			new Hints(), //
			"finalDependency" //
		));
	}

	static class ImmutableOutput implements Computers.Arity1<Double, Double> {

		@Override
		public void compute(Double in, @Container Double out) {}
	}

	@Test
	public void testImmutableOutput() {
		Assertions.assertThrows(FunctionalTypeOpException.class,
			() -> new OpClassInfo( //
				ImmutableOutput.class, //
				new Hints(), //
				"finalDependency" //
			));
	}

}
