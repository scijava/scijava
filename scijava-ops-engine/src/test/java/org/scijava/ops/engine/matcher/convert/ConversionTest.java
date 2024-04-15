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
import org.scijava.function.Container;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.Ops;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.conversionLoss.impl.IdentityLossReporter;
import org.scijava.ops.engine.conversionLoss.impl.PrimitiveLossReporters;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.engine.matcher.impl.LossReporterWrapper;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic Op conversion test
 *
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public class ConversionTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register( //
			new CopyOpCollection<>(), //
			new CreateOpCollection(), //
			new IdentityCollection<>(), //
			new IdentityLossReporter<>(), //
			new LossReporterWrapper<>(), //
			new PrimitiveArrayConverters<>(), //
			new PrimitiveConverters<>(), //
			new PrimitiveLossReporters(), //
			new ConversionTest(), //
			new UtilityConverters() //
		);
	}

	@OpField(names = "test.math.powDouble", params = "base, exponent, result")
	public final BiFunction<Double, Double, Double> powOp = Math::pow;

	@OpField(names = "test.math.powDouble", params = "base, exponent, result")
	public final BiFunction<Long, Long, Double> powOpL = Math::pow;

	@OpField(names = "test.math.powDouble", params = "base, exponent, result")
	public final BiFunction<Integer[], Double, Double> powOpArray = (b, e) -> Math
		.pow(b[0], e);

	@Test
	public void testConversion() {
		Integer number = 2;
		Integer exponent = 2;
		Double result = ops.op("test.math.powDouble").input(number, exponent)
			.outType(Double.class).apply();
		assertEquals(4.0, result, 0);
	}

	@Test
	public void testSomeConversion() {
		Integer number = 2;
		Double exponent = 2.;
		Double result = ops.op("test.math.powDouble").input(number, exponent)
			.outType(Double.class).apply();
		assertEquals(4.0, result, 0);
	}

	@Test
	public void testArrayConversion() {
		byte[] number = { 2 };
		Double exponent = 3.;
		Double result = ops.op("test.math.powDouble").input(number, exponent)
			.outType(Double.class).apply();
		assertEquals(8.0, result, 0);
	}

	@Test
	public void testConvertedOp() {
		BiFunction<Number, Number, Double> numFunc = ops.op("test.math.powDouble")
			.inType(Number.class, Number.class).outType(Double.class).function();

		Double result = numFunc.apply(3., 4.);
		assertEquals(81., result, 0);
	}

	@OpMethod(names = "test.differentOrder", type = Computers.Arity1_1.class)
	public static void foo(@Container Integer[] i1, Double[] i2) {
		for (int i = 0; i < i1.length; i++) {
			i1[i] = i2[i].intValue() * i2[i].intValue();
		}
	}

	@Test
	public void testConvertedOpInfo() {
		Long[] i1 = { 1L };
		Byte[] i2 = { 2 };
		var op = ops.op("test.differentOrder").input(i1).output(i2).computer();
		var info = Ops.rich(op).infoTree().info();
		Assertions.assertInstanceOf(ConvertedOpInfo.class, info);
		// Assert input types are expected
		Type[] expected = { Long[].class, Byte[].class };
		Assertions.assertArrayEquals(expected, info.inputTypes().toArray());
		// Assert the output type is expected
		assertEquals(Byte[].class, info.outputType());

		// Assert the struct types are correct
		var members = info.struct().members();
		assertEquals(Long[].class, members.get(0).getType());
		assertEquals(Byte[].class, members.get(1).getType());

		// Assert the implementation name reflects the new parameter types
		var expImplName = "org.scijava.ops.engine.matcher.convert.ConversionTest" +
			".foo(java.lang.Integer[],java.lang.Double[])|converted_Long_Arr_Byte_Arr";
		var actImplName = info.implementationName();
		assertEquals(expImplName, actImplName);
	}

	@OpMethod(names = "test.anyConversion", type = Function.class)
	public static Integer foo(Integer i1) {
		return i1 * i1;
	}

	@Test
	public void testConvertAnys() {
		Double in = 2.0;
		var out = ops.op("test.anyConversion").input(in).apply();
		Assertions.assertInstanceOf(Integer.class, out);
	}

	/**
	 * An Op, written as a method, whose type variable has multiple bounds.
	 * <p>
	 * Note that, for the purposes of this test, the {@link Number} bound is
	 * necessary even though it is not needed for the functionality of the test
	 * Op.
	 * </p>
	 */
	@OpMethod(names = "test.maxNumberAndComparable", type = BiFunction.class)
	public static <T extends Number & Comparable<T>> T foo(T in1, T in2) {
		return in1.compareTo(in2) > 0 ? in1 : in2;
	}

	/**
	 * Tests that conversion is possible when {@link TypeVariable}s in the
	 * {@link OpInfo} are bounded by multiple types.
	 */
	@Test
	public void testConvertMultipleBounds() {
		// Assert that there's only one possible match for our Op call
		var name = "test.maxNumberAndComparable";
		var infos = ops.infos(name);
		assertEquals(1, infos.size());
		// And its input types are TypeVariables with two upper bounds.
		var inType = infos.first().inputTypes().get(0);
		Assertions.assertInstanceOf(TypeVariable.class, inType);
		var numBounds = ((TypeVariable<?>) inType).getBounds().length;
		assertEquals(2, numBounds);
		// Now, call it such that we need conversion
		Integer i1 = 1;
		Double i2 = 2.0;
		var result = ops.op(name) //
			.input(i1, i2).apply();
		// Assert the result is an Integer
		Assertions.assertInstanceOf(Integer.class, result);
		Integer intResult = (Integer) result;
		assertEquals(2, intResult);
	}

	/**
	 * An Op, written as a method, whose type variable has multiple bounds.
	 * <p>
	 * Note that, for the purposes of this test, the {@link Number} bound is
	 * necessary even though it is not needed for the functionality of the test
	 * Op.
	 * </p>
	 */
	@OpMethod(names = "test.maxNumberIntegerNumber", type = BiFunction.class)
	public static Number max(Number in1, Integer in2) {
		return in2.compareTo(in1.intValue()) > 0 ? in1 : in2;
	}

	/**
	 * Tests that subtypes are "identity-converted"
	 */
	@Test
	public void testIdentityConversion() {
		// Conversion is required for the second input.
		// The first input, then, should be identity-converted
		ops.op("test.maxNumberIntegerNumber") //
			.inType(Double.class, Double.class) //
			.outType(Number.class) //
			.function();
	}

}
