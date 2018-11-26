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
import static org.junit.Assert.assertTrue;

import java.util.function.BiFunction;

import org.junit.Test;
import org.scijava.core.Priority;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.Source;
import org.scijava.ops.types.Nil;
import org.scijava.param.Parameter;
import org.scijava.param.ValidityException;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class OpPriorityTest extends AbstractTestEnvironment {
	
	@Plugin(type = Op.class, name = "test.priority", priority = Priority.HIGH)
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	private static final class testDouble implements Source<Number>{
		@Override
		public Number create() {
			return new Double(0.0);
		}
	}

	@Plugin(type = Op.class, name = "test.priority", priority = Priority.LOW)
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	private static final class testFloat implements Source<Number>{
		@Override
		public Number create() {
			return new Float(0.0);
		}
	}
	

	Nil<Double> nilDouble = new Nil<Double>() {
	};
	
	@Test
	public void testOpCollection() throws ValidityException {
		BiFunction<Double, Double, Double> divFunction = ops().findOp( //
				"math.add", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);

		// The found op is assumed to come from the MathOpCollection
		assertTrue(divFunction.getClass().getName().contains("MathOpCollection$$Lambda"));
	}
	
	@Test
	public void testOpPriority() {

		Source<Number> testFunc = ops().findOp("test.priority", new Nil<Source<Number>>() {}, new Nil[] {}, new Nil<Number>() {});
		Number x = testFunc.create();
		assertTrue(x instanceof Double);
	}
}
