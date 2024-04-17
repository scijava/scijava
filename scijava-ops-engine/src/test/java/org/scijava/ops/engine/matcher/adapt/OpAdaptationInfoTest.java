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

package org.scijava.ops.engine.matcher.adapt;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.Ops;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.FunctionsToComputers;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;
import org.scijava.types.Types;

public class OpAdaptationInfoTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OpAdaptationInfoTest());
		ops.register(new CopyOpCollection());
		ops.register(new FunctionsToComputers.Function2ToComputer2<>());
	}

	private static final String TEST_DESC = "This is an Op that is being adapted";

	@OpMethod( //
		names = "test.adaptationDescription", //
		type = BiFunction.class, //
		description = TEST_DESC //
	)
	public static double[] adaptableOp(final Double t, final Double u) {
		return new double[] { t, u };
	}

	@Test
	public void testAdaptedDescription() {
		// Match the above Op as a Computer
		var adapted = ops.op("test.adaptationDescription") //
			.inType(Double.class, Double.class) //
			.outType(double[].class) //
			.computer();
		var info = Ops.info(adapted);
		Assertions.assertInstanceOf(OpAdaptationInfo.class, info);
		Assertions.assertEquals(TEST_DESC, info.description());
		String expected =
			"org.scijava.ops.engine.matcher.adapt.OpAdaptationInfoTest.adaptableOp(java.lang.Double,java.lang.Double)\n\t" +
				"Adaptor: org.scijava.ops.engine.adapt.functional.FunctionsToComputers$Function2ToComputer2\n\t\t" +
				"Depends upon: org.scijava.ops.engine.copy.CopyOpCollection$copyDoubleArray\n\t" //
				+ TEST_DESC + "\n\n\t" //
				+ "> input1 : java.lang.Double\n\t" //
				+ "> input2 : java.lang.Double\n\t" //
				+ "> output1 : @CONTAINER double[]";
		String actual = adapted.toString();
		Assertions.assertEquals(expected, actual);
	}
}
