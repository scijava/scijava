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
package org.scijava.ops.engine.reduce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.exceptions.InvalidOpException;
import org.scijava.ops.engine.exceptions.impl.NullablesOnMultipleMethodsException;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

public class NullableArgumentsFromIFaceTest extends AbstractTestEnvironment
		implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new NullableArgumentsFromIFaceTest());
		ops.register(new TestOpNullableFromIFace());
	}

	@OpMethod(names = "test.nullableSubtract", type = BiFunctionWithNullable.class)
	public static Double foo(Double i1, Double i2, Double i3) {
		if (i3 == null) i3 = 0.;
		return i1 - i2 - i3;
	}

	@Test
	public void testMethodWithOneNullable() {
		Double o = ops.op("test.nullableSubtract").arity3().input(2., 5., 7.).outType(Double.class).apply();
		Double expected = -10.0;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testMethodWithoutNullables() {
		Double o = ops.op("test.nullableSubtract").arity2().input(2., 5.).outType(Double.class).apply();
		Double expected = -3.0;
		Assertions.assertEquals(expected, o);
	}

	@OpField(names = "test.nullableAnd", params = "in1, in2, in3")
	public final BiFunctionWithNullable<Boolean, Boolean, Boolean, Boolean> bar =
		(in1, in2, in3) -> {
			if (in3 == null) in3 = true;
			return in1 & in2 & in3;
		};

	@Test
	public void testFieldWithNullables() {
		Boolean in1 = true;
		Boolean in2 = true;
		Boolean in3 = false;
		Boolean o = ops.op("test.nullableAnd").arity3().input(in1, in2, in3).outType(Boolean.class).apply();
		Boolean expected = false;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testFieldWithoutNullables() {
		Boolean in1 = true;
		Boolean in2 = true;
		Boolean o = ops.op("test.nullableAnd").arity2().input(in1, in2).outType(Boolean.class).apply();
		Boolean expected = true;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testClassWithNullables() {
		Boolean in1 = true;
		Boolean in2 = false;
		Boolean in3 = false;
		Boolean o = ops.op("test.nullableOr").arity3().input(in1, in2, in3).outType(Boolean.class).apply();
		Boolean expected = true;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testClassWithoutNullables() {
		Boolean in1 = true;
		Boolean in2 = false;
		Boolean o = ops.op("test.nullableOr").arity2().input(in1, in2).outType(Boolean.class).apply();
		Boolean expected = true;
		Assertions.assertEquals(expected, o);
	}

	public static Boolean baz(Boolean i1, Boolean i2, @Nullable Boolean i3) {
		if (i3 == null) i3 = false;
		return i1 ^ i2 ^ i3;
	}

	/**
	 * Ensures that an {@link InvalidOpException} is thrown when {@link Nullable}
	 * parameters are on both the functional interface and on the Op.
	 */
	@Test
	public void testNullablesOnOpMethodAndIFace() throws NoSuchMethodException {
		// Find the method
		var m = this.getClass().getDeclaredMethod(//
			"baz", //
			Boolean.class, //
			Boolean.class, //
			Boolean.class //
		);
		// Try to create an OpMethodInfo
		Assertions.assertThrows(NullablesOnMultipleMethodsException.class, () ->
			new OpMethodInfo(//
				m, //
				BiFunctionWithNullable.class, //
				new Hints(), //
				"test.optionalOnIFaceAndOp" //
			));
	}

	public final BiFunctionWithNullable<Double, Double, Double, Double> foo = new BiFunctionWithNullable<Double, Double, Double, Double>() {
		@Override public Double apply(Double in1, Double in2, @Nullable Double in3) {
			if (in3 == null) in3 = 0.;
			return in1 + in2 + in3;
		}
	};

	/**
	 * Ensures that an {@link InvalidOpException} is thrown when {@link Nullable}
	 * parameters are on both the functional interface and on the Op.
	 */
	@Test
	public void testNullablesOnOpFieldAndIFace() throws NoSuchFieldException {
		// Find the method
		var f = this.getClass().getDeclaredField("foo");
		// Try to create an OpMethodInfo
		Assertions.assertThrows(NullablesOnMultipleMethodsException.class,
			() -> new OpFieldInfo(//
				this, //
				f, //
				new Hints(), //
				"test.optionalOnIFaceAndOp" //
			));
	}

}
