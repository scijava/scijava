/*
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

package org.scijava.ops.engine.adapt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Producer;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.priority.Priority;

/**
 * Ensures that higher-priority {@code adapt} Ops are used over lower-priority
 * {@code adapt} Ops.
 *
 * @author Gabriel Selzer
 */
public class OpAdaptationPriorityTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OpAdaptationPriorityTest());
	}

	public static class PriorityThing {

		double priority;

		public PriorityThing(double priority) {
			this.priority = priority;
		}

		public void increasePriority(double newPriority) {
			priority += newPriority;
		}

		public double getPriority() {
			return priority;
		}
	}

	@OpField(names = "test.priorityOp")
	public static final Computers.Arity1<Double, PriorityThing> priorityOp = (in,
		out) -> {
		out.increasePriority(in);
	};

	@OpMethod(names = "engine.adapt", type = Function.class,
		priority = Priority.HIGH)
	public static <I, O> Function<I, O> highPriorityAdaptor( //
		@OpDependency(name = "engine.create", hints = {
			BaseOpHints.Adaptation.FORBIDDEN }) Function<I, O> creator, //
		Computers.Arity1<I, O> computer //
	) {
		return (in) -> {
			var out = creator.apply(in);
			computer.compute(in, out);
			return out;
		};
	}

	@OpMethod(names = "engine.adapt", type = Function.class,
		priority = Priority.LOW)
	public static <I, O> Function<I, O> lowPriorityAdaptor( //
		@OpDependency(name = "engine.create", hints = {
			BaseOpHints.Adaptation.FORBIDDEN }) Producer<O> creator, //
		Computers.Arity1<I, O> computer //
	) {
		return (in) -> {
			var out = creator.get();
			computer.compute(in, out);
			return out;
		};
	}

	@OpField(names = "engine.create")
	public static final Producer<PriorityThing> priorityThingProducer = //
		() -> new PriorityThing(10000);

	@OpField(names = "engine.create")
	public static final Function<Double, PriorityThing> priorityThingFunction = //
		(in) -> new PriorityThing(in);

	@Test
	public void testPriority() {
		PriorityThing pThing = ops.op("test.priorityOp").arity1() //
			.input(10.0) //
			.outType(PriorityThing.class) //
			.apply();
		assertEquals(20, pThing.getPriority(), 0.);
		// This would be the value of pThing if it were created using
		// PriorityThingProducer
		assertNotEquals(10010, pThing.getPriority(), 0.);
	}

}
