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
import org.scijava.ops.api.Hints;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.matcher.simplify.IdentityLossReporter;
import org.scijava.ops.engine.matcher.simplify.PrimitiveArraySimplifiers;
import org.scijava.ops.engine.matcher.simplify.PrimitiveSimplifiers;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class SimplificationHintTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new SimplificationHintTest());
		ops.register(new IdentityLossReporter());
		ops.register(new PrimitiveSimplifiers());
		ops.register(new PrimitiveArraySimplifiers());
	}

	@OpField(names = "test.simplification.hints")
	public final Function<Double[], Double[]> op = (in) -> new Double[in.length];

	@Test
	public void testSimplification() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Function<Integer[], Integer[]> adaptable = ops.op(
			"test.simplification.hints").arity1().inType(Integer[].class).outType(
				Integer[].class).function();
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Simplification.FORBIDDEN);
		ops.setDefaultHints(hints);
		try {
			ops.op("test.simplification.hints").arity1().inType(Integer[].class).outType(
				Integer[].class).function();
			throw new IllegalStateException(
				"Simplification is forbidden - this op call should not match!");
		}
		catch (OpMatchingException e) {
		}

	}

	@Test
	public void testSimplificationPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		@SuppressWarnings("unused")
		Function<Integer[], Integer[]> adaptable = ops.op(
			"test.simplification.hints", hints).arity1().inType(Integer[].class).outType(
				Integer[].class).function();
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Simplification.FORBIDDEN);
		try {
			ops.op("test.simplification.hints", hints).arity1().inType(Integer[].class).outType(
				Integer[].class).function();
			throw new IllegalStateException(
				"Simplification is forbidden - this op call should not match!");
		}
		catch (OpMatchingException e) {
		}

	}

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "test.simplification.unsimplifiable")
	public final Function<Double[], Double[]> nonAdaptableOp = (
		in) -> new Double[in.length];

	@Test
	public void testUnsimplifiableOp() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.simplification.unsimplifiable").arity1().inType(Double[].class).outType(
				Double[].class).function();
		// make sure that we cannot match the Op via adaptation even when
		// simplification
		// is allowed (since it declares itself to be unsimplifiable)
		try {
			ops.op("test.simplification.unsimplifiable").arity1().inType(Integer[].class)
				.outType(Integer[].class).function();
			throw new IllegalStateException(
				"The only relevant Op is not simplifiable - this op call should not match!");
		}
		catch (OpMatchingException e) {
		}

	}

	@Test
	public void testUnsimplifiableOpPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.simplification.unsimplifiable", hints).arity1().inType(Double[].class).outType(
				Double[].class).function();
		// make sure that we cannot match the Op via adaptation even when
		// simplification
		// is allowed (since it declares itself to be unsimplifiable)
		try {
			ops.op("test.simplification.unsimplifiable", hints).arity1().inType(Integer[].class)
				.outType(Integer[].class).function();
			throw new IllegalStateException(
				"The only relevant Op is not simplifiable - this op call should not match!");
		}
		catch (OpMatchingException e) {
		}

	}

}
