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

package org.scijava.ops.engine.matcher.convert;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.collections.ObjectArray;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.describe.PrimitiveDescriptors;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

public class ConvertedOpInfoTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register( //
			new ConvertedOpInfoTest(), //
			new PrimitiveConverters(), //
			new PrimitiveArrayConverters(), //
			new IdentityCollection<>(), //
			new SimpleOp() //
		);
	}

	@OpClass(names = "test.convertedDescription")
	static class SimpleOp implements BiFunction<Double, Double, Double[]>, Op {

		@Override
		public Double[] apply(Double t, Double u) {
			return new Double[] { t, u };
		}
	}

	@Test
	public void testConvertedOpFromEnvironment() {
		var op = ops.binary("test.convertedDescription").inType(Number.class,
			Number.class).outType(new Nil<ObjectArray<Number>>()
		{}).function();
		ObjectArray<Number> expected = new ObjectArray<>(new Double[] { 5., 6. });
		ObjectArray<Number> actual = op.apply(5, 6);
		Assertions.assertEquals(expected, actual);
	}

}
