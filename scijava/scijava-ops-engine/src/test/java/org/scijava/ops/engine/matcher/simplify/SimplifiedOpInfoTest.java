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

package org.scijava.ops.engine.matcher.simplify;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.matcher.impl.DefaultOpRequest;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.types.Types;

public class SimplifiedOpInfoTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void addNeededOps() {
		ops.registerInfosFrom(new PrimitiveSimplifiers());
		ops.registerInfosFrom(new PrimitiveArraySimplifiers());
		ops.register(new Identity<>());
	}

	static class SimpleOp implements BiFunction<Double, Double, Double[]> {

		@Override
		public Double[] apply(Double t, Double u) {
			return new Double[] { t, u };
		}
	}

	@Test
	public void testSimplifiedDescription() {
		OpClassInfo info = new OpClassInfo(SimpleOp.class, new Hints(),
				"test.simplifiedDescription");
		// NB it's a lot easier to let the framework create our SimplifiedOpInfo
		Type opType = Types.parameterize(BiFunction.class, new Type[] {
				Integer.class, Integer.class, Integer[].class });
		OpRequest
				simpleReq = new DefaultOpRequest("test.adaptationDescription", opType,
				Integer[].class, new Type[] { Integer.class, Integer.class });
		SimplifiedOpInfo simplified = new InfoSimplificationGenerator(info, ops)
				.generateSuitableInfo(ops, simpleReq, new Hints());
		String expected = "test.simplifiedDescription(\n\t " //
				+
				"Inputs:\n\t\tjava.lang.Integer input1\n\t\tjava.lang.Integer input2\n\t " //
				+ "Outputs:\n\t\tjava.lang.Integer[] output1\n)\n";
		String actual = simplified.toString();
		Assertions.assertEquals(expected, actual);
	}

}
