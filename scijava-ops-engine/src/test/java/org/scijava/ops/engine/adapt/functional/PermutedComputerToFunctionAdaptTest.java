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

package org.scijava.ops.engine.adapt.functional;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

/**
 * Tests that a permuted computer, such as {@link Computers.Arity2_1}, can be
 * adapted as a {@link BiFunction}.
 *
 * @author Gabriel Selzer
 */
public class PermutedComputerToFunctionAdaptTest extends AbstractTestEnvironment
	implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new PermutedComputerToFunctionAdaptTest());
		ops.register(
			new ComputersToFunctionsViaFunction.Computer2ToFunction2ViaFunction<>());
		ops.register(new CreateOpCollection());
	}

	@OpMethod(names = "adapt.permuted", type = Computers.Arity2_1.class)
	public static void compute(@Container double[] out, double[] in1,
		double[] in2)
	{
		for (int i = 0; i < in1.length; i++) {
			out[i] = in1[i] * in2[i];
		}
	}

	@Test
	public void testAdaptingPermutedComputer() {
		double[] result = ops.binary("adapt.permuted").input(new double[] { 1.5 },
			new double[] { 2.0 }).outType(double[].class).apply();
		Assertions.assertEquals(3.0, result[0]);
	}

}
