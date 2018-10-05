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

import java.util.Arrays;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.types.Nil;

import com.google.common.collect.Streams;

public class AutoTransformTest extends AbstractTestEnvironment {

	private static Nil<Iterable<Double>> nilIterableDouble = new Nil<Iterable<Double>>() {
	};

	@Test
	public void autoLiftFunctionToIterables() {
		// There is no sqrt function for iterables in the system, however we can auto transform
		// as there is a lifter for Functions to iterables
		Function<Iterable<Double>, Iterable<Double>> sqrtFunction = ops().findOp( //
				"math.sqrt", new Nil<Function<Iterable<Double>, Iterable<Double>>>() {
				}, //
				new Nil[] { nilIterableDouble }, //
				nilIterableDouble//
		);
		
		Iterable<Double> res = sqrtFunction.apply(Arrays.asList(0.0, 4.0, 16.0));
		assertTrue(Arrays.deepEquals(Streams.stream(res).toArray(Double[]::new), new Double[]{0.0, 2.0, 4.0}));
	}
}
