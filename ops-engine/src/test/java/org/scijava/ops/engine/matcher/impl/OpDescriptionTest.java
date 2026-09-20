/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.matcher.reduce.ReducedOpInfo;
import org.scijava.common3.Types;

public class OpDescriptionTest extends AbstractTestEnvironment {

	static class ClassOp implements BiFunction<Double, Double, Double> {

		@Override
		public Double apply(Double t, Double u) {
			return t + u;
		}
	}

	@Test
	public void testOpClassDescription() {
		DefaultOpClassInfo info = new DefaultOpClassInfo( //
			ClassOp.class, //
			"1.0", //
			"", //
			new Hints(), //
			1.0, //
			"test.classDescription" //
		);

		String expected =
			"org.scijava.ops.engine.matcher.impl.OpDescriptionTest$ClassOp\n\t" //
				+ "> input1 : java.lang.Double\n\t" //
				+ "> input2 : java.lang.Double\n\t" //
				+ "Returns : java.lang.Double";
		String actual = info.toString();
		Assertions.assertEquals(expected, actual);
	}

	public static Double methodOp(Double in1, Double in2) {
		return in1 + in2;
	}

	@Test
	public void testOpMethodDescription() throws NoSuchMethodException {
		Method method = OpDescriptionTest.class.getMethod("methodOp", Double.class,
			Double.class);
		DefaultOpMethodInfo info = new DefaultOpMethodInfo( //
			method, //
			BiFunction.class, //
			"1.0", //
			"", //
			new Hints(), //
			1.0, //
			"test.methodDescription" //
		);
		String expected =
			"org.scijava.ops.engine.matcher.impl.OpDescriptionTest.methodOp(java.lang.Double,java.lang.Double)\n\t" //
				+ "> input1 : java.lang.Double\n\t" //
				+ "> input2 : java.lang.Double\n\t" //
				+ "Returns : java.lang.Double";
		String actual = info.toString();
		Assertions.assertEquals(expected, actual);
	}

	public final BiFunction<Double, Double, Double> fieldOp = Double::sum;

	@Test
	public void testOpFieldDescription() throws NoSuchFieldException {
		Field field = OpDescriptionTest.class.getDeclaredField("fieldOp");
		DefaultOpFieldInfo info = new DefaultOpFieldInfo( //
			this, //
			field, //
			"", //
			"", //
			new Hints(), //
			1.0, //
			"test.fieldDescription" //
		);
		String expected =
			"org.scijava.ops.engine.matcher.impl.OpDescriptionTest$fieldOp\n\t" //
				+ "> input1 : java.lang.Double\n\t" //
				+ "> input2 : java.lang.Double\n\t" //
				+ "Returns : java.lang.Double";
		String actual = info.toString();
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testReducedDescription() {
		DefaultOpClassInfo info = new DefaultOpClassInfo( //
			ClassOp.class, //
			"1.0", //
			"", //
			new Hints(), //
			1.0, //
			"test.reductionDescription" //
		);

		Type opType = Types.parameterize(Function.class, new Type[] { Double.class,
			Double.class });
		ReducedOpInfo reduced = new ReducedOpInfo(info, opType, 1);
		String expected =
			"org.scijava.ops.engine.matcher.impl.OpDescriptionTest$ClassOpReduction1\n\t" //
				+ "> input1 : java.lang.Double\n\t" //
				+ "Returns : java.lang.Double";
		String actual = reduced.toString();
		Assertions.assertEquals(expected, actual);
	}

}
