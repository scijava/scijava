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

package org.scijava.ops.engine.hints;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.Hints;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.BaseOpHints.Adaptation;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.FunctionsToComputers;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AdaptationHintTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new AdaptationHintTest());
		ops.register(new FunctionsToComputers.Function1ToComputer1());
		ops.register(new CopyOpCollection());
	}

	@OpField(names = "test.adaptation.hints")
	public final Function<Double[], Double[]> op = (in) -> new Double[in.length];

	@Test
	public void testAdaptation() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Computers.Arity1<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.hints").arity1().inType(Double[].class).outType(
				Double[].class).computer();
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Adaptation.FORBIDDEN);
		ops.setDefaultHints(hints);
		try {
			ops.op("test.adaptation.hints").arity1().inType(Double[].class).outType(
				Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		}
		catch (OpMatchingException e) {}
	}

	@Test
	public void testAdaptationPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		@SuppressWarnings("unused")
		Computers.Arity1<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.hints", hints).arity1().inType(Double[].class).outType(
				Double[].class).computer();
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Adaptation.FORBIDDEN);
		try {
			ops.op("test.adaptation.hints", hints).arity1().inType(Double[].class)
				.outType(Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		}
		catch (OpMatchingException e) {}
	}

	@OpHints(hints = { Adaptation.FORBIDDEN })
	@OpField(names = "test.adaptation.unadaptable")
	public final Function<Double[], Double[]> nonAdaptableOp = (
		in) -> new Double[in.length];

	@Test
	public void testNonAdaptableOp() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.unadaptable").arity1().inType(Double[].class).outType(
				Double[].class).function();
		// make sure that we cannot match the Op via adaptation even when adaptation
		// is allowed (since it declares itself to be unadaptable)
		try {
			ops.op("test.adaptation.unadaptable").arity1().inType(Double[].class)
				.outType(Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		}
		catch (OpMatchingException e) {
			// NB: expected behavior.
		}
	}

	@Test
	public void testNonAdaptableOpPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.unadaptable", hints).arity1().inType(Double[].class)
			.outType(Double[].class).function();
		// make sure that we cannot match the Op via adaptation even when adaptation
		// is allowed (since it declares itself to be unadaptable)
		assertThrows(OpMatchingException.class, () -> ops.op(
			"test.adaptation.unadaptable", hints).arity1().inType(Double[].class)
			.outType(Double[].class).computer(), "This op call should not match!");
	}

}
