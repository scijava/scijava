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

package org.scijava.ops.engine.adapt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.BaseOpHints.Adaptation;
import org.scijava.ops.spi.*;
import org.scijava.priority.Priority;
import org.scijava.common3.Any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * When the user requests an {@link Any} as the output, we should ensure that an
 * output is used that can satisfy the typing constraints of the underlying Op.
 * In other words, ensure that when an Op is matched for adaptation, we test
 * that its type variable assignments are captured and used for dependency
 * matching.
 *
 * @param <N>
 * @author Gabriel Selzer
 */
public class AdaptationTypeVariableCaptureTest<N extends Number, O extends Number> extends
	AbstractTestEnvironment implements OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new Computer1ToFunction1ViaFunction<>());
		ops.register(new AdaptationTypeVariableCaptureTest<>());
	}

	/** Adaptor */
	@OpClass(names = "engine.adapt")
	public static class Computer1ToFunction1ViaFunction<I, O> implements
		Function<Computers.Arity1<I, O>, Function<I, O>>, Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Function<I, O> creator;

		/**
		 * @param computer the Computer to adapt
		 * @return computer, adapted into a Function
		 */
		@Override
		public Function<I, O> apply(Computers.Arity1<I, O> computer) {
			return (in) -> {
				O out = creator.apply(in);
				computer.compute(in, out);
				return out;
			};
		}

	}

	/** Op that should be adapted */
	@OpField(names = "test.adaptedCapture")
	public final Computers.Arity1<List<N>, List<Double>> original = (in, out) -> {
		out.clear();
		for (var n : in) {
			out.add(n.doubleValue());
		}
	};

	/** Op that will match if type variables aren't captured */
	@OpField(names = "engine.create", priority = Priority.HIGH)
	public final Function<List<Byte>, List<Byte>> highCopier = (list) -> {
		throw new IllegalStateException("This Op should not be called");
	};

	/** Op that should match if type variables are captured */
	@OpField(names = "engine.create", priority = Priority.LOW)
	public final Function<List<Byte>, List<Double>> lowCopier = (
		list) -> new ArrayList<>();

	/**
	 * Test ensuring that in adaptation type variable assignments are captured.
	 */
	@Test
	public void testCapture() {
		List<Byte> in = Arrays.asList((byte) 0, (byte) 1);
		Object out = ops.op("test.adaptedCapture") //
			.input(in) //
			.apply();
		Assertions.assertInstanceOf(List.class, out);
		Assertions.assertEquals(2, in.size());
	}

	/** Op that should be adapted */
	@OpField(names = "test.adaptedAnyFallback")
	public final Computers.Arity1<N, List<O>> originalTypeVars = (in, out) -> {
		O o = out.get(0);
		out.clear();
		for (int i = 0; i < (int) in; i++) {
			out.add(o);
		}
	};

	@OpField(names="engine.create")
	public final Function<N, List<Double>> foo = (in) -> {
		List<Double> f = new ArrayList<>();
		f.add(1.0);
		return f;
	};

	@OpField(names="engine.create", priority = Priority.HIGH)
	public final Function<N, List<String>> bar = (in) -> {
		throw new IllegalStateException("This Op should not be used");
	};

	/**
	 * Ensures that, when an {@link Any} is used in matching, the type variable
	 * assignment is <b>not</b> captured, and instead the type variable is
	 * mapped to the original type variable bounds on the Op.
	 */
	@Test
	public void testAnyFallbacks() {
		Integer in = 10;
		Object out = ops.op("test.adaptedAnyFallback") //
				.input(in) //
				.apply();
		Assertions.assertInstanceOf(List.class, out);
		Assertions.assertEquals(in, ((List) out).size());
	}

}
