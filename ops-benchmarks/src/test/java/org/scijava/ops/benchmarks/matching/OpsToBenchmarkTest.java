/*-
 * #%L
 * Benchmarks for the SciJava Ops framework.
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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

package org.scijava.ops.benchmarks.matching;

import net.imagej.ops.OpService;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.ops.api.OpEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A simple test ensuring that the Ops used in benchmarks get matched.
 *
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public class OpsToBenchmarkTest {

	@Test
	public void testImageJOps() {
		try (var ctx = new Context(OpService.class)) {
			var ops = ctx.service(OpService.class);
			byte[] b = { 77 };
			ops.run("benchmark.increment", b);
			assertEquals(78, b[0]);
		}
	}

	@Test
	public void testConversion() {
		OpEnvironment env = OpEnvironment.build();
		byte[] b = { 26 };
		env.op("benchmark.increment").input(b).mutate();
		assertEquals(27, b[0]);
	}

	@Test
	public void testConversionAdaptation() {
		OpEnvironment env = OpEnvironment.build();
		double[] d = { 12.3 };
		double[] result = env.op("benchmark.increment").input(d).outType(
			double[].class).apply();
		assertEquals(12.3, d[0]);
		assertEquals(13, result[0]);
	}

	@Test
	public void testByteCreation() {
		OpEnvironment env = OpEnvironment.build();
		byte[] b = env.op("engine.create").outType(byte[].class).create();
		assertNotNull(b);
		assertEquals(1, b.length);
	}
}
