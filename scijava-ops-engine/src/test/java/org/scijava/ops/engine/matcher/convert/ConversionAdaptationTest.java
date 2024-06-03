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

package org.scijava.ops.engine.matcher.convert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaFunction;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class ConversionAdaptationTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new ConversionAdaptationTest());
		ops.register(new CopyOpCollection<>());
		ops.register(new CreateOpCollection());
		ops.register(new IdentityCollection<>());
		ops.register(new PrimitiveConverters<>());
		ops.register(new PrimitiveArrayConverters<>());
		ops.register(new UtilityConverters());
		Object[] adapters = objsFromNoArgConstructors(
			ComputersToFunctionsViaFunction.class.getDeclaredClasses());
		ops.register(adapters);
	}

	@OpField(names = "test.math.modulus")
	public final Computers.Arity2<Integer[], Integer, Integer[]> modOp = (inArr,
		mod, outArr) -> {
		for (int i = 0; i < inArr.length && i < outArr.length; i++) {
			outArr[i] = inArr[i] % mod;
		}
	};

	@Test
	public void testAdaptAndConvert() {
		Double[] inArr = { 1., 4., 6. };
		Double modulus = 3.;

		Double[] expected = { 1., 1., 0. };
		Double[] actual = ops.op("test.math.modulus").input(inArr, modulus).outType(
			Double[].class).apply();
		Assertions.assertArrayEquals(expected, actual);
	}

}
