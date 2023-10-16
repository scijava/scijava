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

package org.scijava.ops.engine.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Functions;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.math.MathOpCollection;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultEval}.
 * 
 * @author Curtis Rueden
 */
public class EvalTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new DefaultEval());
		ops.register(new MathOpCollection());
	}

	@Test
	public void testMath() {
		final Map<String, Object> vars = new HashMap<>();
		vars.put("a", 2);
		vars.put("b", 3);
		vars.put("c", 5);

		// TODO: can we use ops.run here?
		Functions.Arity3<String, Map<String, Object>, OpEnvironment, Object> evaluator =
			ops.op("eval", //
				new Nil<>()
				{}, //
				new Nil[] { new Nil<String>() {}, new Nil<Map<String, Object>>() {}, new Nil<OpEnvironment>() {} }, //
				new Nil<>()
				{} //
			);

		assertEquals(7., evaluator.apply("a+c", vars, ops));
		assertEquals(3., evaluator.apply("c-a", vars, ops));
		assertEquals(6., evaluator.apply("a*b", vars, ops));
		assertEquals(2.5, evaluator.apply("c/a", vars, ops));
		assertEquals(1., evaluator.apply("c%a", vars, ops));
		assertEquals(17., evaluator.apply("a+b*c", vars, ops));
	}

}
