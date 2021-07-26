/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
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

package org.scijava.ops.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.function.Producer;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

/**
 * Tests the construction of {@link OpMethod}s.
 * 
 * @author Gabriel Selzer
 * @author Marcel Wiedenmann
 */
@Plugin(type = OpCollection.class)
public class OpMethodTest extends AbstractTestEnvironment {

//	@Test
//	public void testParseIntegerOp() {
//		// Will match a lambda created and returned by createParseIntegerOp() below.
//		final Function<String, Integer> parseIntegerOp = ops.op("test.parseInteger")
//			.inType(String.class).outType(Integer.class).function();
//
//		final String numericString = "42";
//		final Integer parsedInteger = parseIntegerOp.apply(numericString);
//		assertEquals(Integer.parseInt(numericString), (int) parsedInteger);
//	}
//
//	@Test
//	public void testMultiplyNumericStringsOpMethod() {
//		// Will match a lambda created and returned by
//		// createMultiplyNumericStringsOp(..), which itself captured a lambda
//		// created and returned by createParseIntegerOp().
//		final BiFunction<String, String, Integer> multiplyNumericStringsOp = ops.op(
//			"test.multiplyNumericStrings").inType(String.class, String.class).outType(
//				Integer.class).function();
//
//		final String numericString1 = "3";
//		final String numericString2 = "18";
//		final Integer multipliedNumericStrings = multiplyNumericStringsOp.apply(
//			numericString1, numericString2);
//		assertEquals(Integer.parseInt(numericString1) * Integer.parseInt(
//			numericString2), (int) multipliedNumericStrings);
//	}
//
//	@OpMethod(names = "test.parseInteger", type = Function.class)
//	// Refers to the input parameter of the function that's returned by this
//	// factory method.
//	// Refers to the output parameter of the function.
//	public static Integer createParseIntegerOp(String in) {
//		return Integer.parseInt(in);
//	}
//
//	@OpMethod(names = "test.multiplyNumericStrings", type = BiFunction.class)
//	public static Integer createMultiplyNumericStringsOp(final String in1,
//		final String in2, @OpDependency(
//			name = "test.parseInteger") Function<String, Integer> parseIntegerOp)
//	{
//		return parseIntegerOp.apply(in1) * parseIntegerOp.apply(in2);
//	}
	// -- Functions -- //

	@Test
	public void testOpMethodProducer() {
		final Integer out = ops.op("test.multiplyNumericStrings").input()
			.outType(Integer.class).create();
		final Integer expected = Integer.valueOf(1);
		assertEquals(expected, out);
	}

	@Test
	public void testOpMethodFunction1() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 1), out, 0);
	}

	@Test
	public void testOpMethodFunction2() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 2), out, 0);
	}

	@Test
	public void testOpMethodFunction3() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 3), out, 0);
	}

	@Test
	public void testOpMethodFunction4() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 4), out, 0);
	}

	@Test
	public void testOpMethodFunction5() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 5), out, 0);
	}

	@Test
	public void testOpMethodFunction6() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 6), out, 0);
	}

	@Test
	public void testOpMethodFunction7() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 7), out, 0);
	}

	@Test
	public void testOpMethodFunction8() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 8), out, 0);
	}

	@Test
	public void testOpMethodFunction9() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 9), out, 0);
	}

	@Test
	public void testOpMethodFunction10() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 10), out, 0);
	}

	@Test
	public void testOpMethodFunction11() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 11), out, 0);
	}

	@Test
	public void testOpMethodFunction12() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 12), out, 0);
	}

	@Test
	public void testOpMethodFunction13() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 13), out, 0);
	}

	@Test
	public void testOpMethodFunction14() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 14), out, 0);
	}

	@Test
	public void testOpMethodFunction15() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 15), out, 0);
	}

	@Test
	public void testOpMethodFunction16() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 16), out, 0);
	}

	// -- Computers -- //

	public List<Double> expected(double expected, int size) {
		List<Double> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			list.add(expected);
		}
		return list;
	}

	@Test
	public void testOpMethodComputer0() {
		String in = "0";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input()
			.output(out).compute();
		assertEquals(expected(0, 0), out);
	}

	@Test
	public void testOpMethodComputer1() {
		String in = "1";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in)
			.output(out).compute();
		assertEquals(expected(1, 1), out);
	}

	@Test
	public void testOpMethodComputer2() {
		String in = "2";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in)
			.output(out).compute();
		assertEquals(expected(2, 2), out);
	}

	@Test
	public void testOpMethodComputer3() {
		String in = "3";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in)
			.output(out).compute();
		assertEquals(expected(3, 3), out);
	}

	@Test
	public void testOpMethodComputer4() {
		String in = "4";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in)
			.output(out).compute();
		assertEquals(expected(4, 4), out);
	}

	@Test
	public void testOpMethodComputer5() {
		String in = "5";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(5, 5), out);
	}

	@Test
	public void testOpMethodComputer6() {
		String in = "6";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(6, 6), out);
	}

	@Test
	public void testOpMethodComputer7() {
		String in = "7";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(7, 7), out);
	}

	@Test
	public void testOpMethodComputer8() {
		String in = "8";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(8, 8), out);
	}

	@Test
	public void testOpMethodComputer9() {
		String in = "9";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(9, 9), out);
	}

	@Test
	public void testOpMethodComputer10() {
		String in = "10";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(10, 10), out);
	}

	@Test
	public void testOpMethodComputer11() {
		String in = "11";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(11, 11), out);
	}

	@Test
	public void testOpMethodComputer12() {
		String in = "12";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(12, 12), out);
	}

	@Test
	public void testOpMethodComputer13() {
		String in = "13";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(13, 13), out);
	}

	@Test
	public void testOpMethodComputer14() {
		String in = "14";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(14, 14), out);
	}

	@Test
	public void testOpMethodComputer15() {
		String in = "15";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(15, 15), out);
	}

	@Test
	public void testOpMethodComputer16() {
		String in = "16";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(16, 16), out);
	}

	// -- Inplaces -- //

	private boolean outputExpected(double[] array, int multiplier) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] != (i + 1) * multiplier) return false;
		}
		return true;
	}

	@Test
	public void testOpMethodInplace1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles1").input(io).mutate();
		assertTrue(outputExpected(io, 1));
	}

	@Test
	public void testOpMethodInplace2_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles2_1").input(io, in).mutate1();
		assertTrue(outputExpected(io, 2));
	}

	@Test
	public void testOpMethodInplace2_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles2_2").input(in, io).mutate2();
		assertTrue(outputExpected(io, 2));
	}

	@Test
	public void testOpMethodInplace3_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles3_1").input(io, in, in).mutate1();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testOpMethodInplace3_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles3_2").input(in, io, in).mutate2();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testOpMethodInplace3_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles3_3").input(in, in, io).mutate3();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testOpMethodInplace4_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles4_1").input(io, in, in, in).mutate1();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testOpMethodInplace4_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles4_2").input(in, io, in, in).mutate2();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testOpMethodInplace4_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles4_3").input(in, in, io, in).mutate3();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testOpMethodInplace4_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles4_4").input(in, in, in, io).mutate4();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testOpMethodInplace5_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_1").input(io, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace5_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_2").input(in, io, in, in, in).mutate2();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace5_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_3").input(in, in, io, in, in).mutate3();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace5_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_4").input(in, in, in, io, in).mutate4();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace5_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_5").input(in, in, in, in, io).mutate5();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace6_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_1").input(io, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_2").input(in, io, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_3").input(in, in, io, in, in, in).mutate3();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_4").input(in, in, in, io, in, in).mutate4();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_5").input(in, in, in, in, io, in).mutate5();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_6").input(in, in, in, in, in, io).mutate6();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace7_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_1").input(io, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_2").input(in, io, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_3").input(in, in, io, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_4").input(in, in, in, io, in, in, in).mutate4();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_5").input(in, in, in, in, io, in, in).mutate5();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_6").input(in, in, in, in, in, io, in).mutate6();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_7").input(in, in, in, in, in, in, io).mutate7();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace8_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_1").input(io, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_2").input(in, io, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_3").input(in, in, io, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_4").input(in, in, in, io, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_5").input(in, in, in, in, io, in, in, in).mutate5();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_6").input(in, in, in, in, in, io, in, in).mutate6();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_7").input(in, in, in, in, in, in, io, in).mutate7();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_8").input(in, in, in, in, in, in, in, io).mutate8();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace9_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_1").input(io, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_2").input(in, io, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_3").input(in, in, io, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_4").input(in, in, in, io, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_5").input(in, in, in, in, io, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_6").input(in, in, in, in, in, io, in, in, in).mutate6();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_7").input(in, in, in, in, in, in, io, in, in).mutate7();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_8").input(in, in, in, in, in, in, in, io, in).mutate8();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_9").input(in, in, in, in, in, in, in, in, io).mutate9();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace10_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_1").input(io, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_2").input(in, io, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_3").input(in, in, io, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_4").input(in, in, in, io, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_5").input(in, in, in, in, io, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_6").input(in, in, in, in, in, io, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_7").input(in, in, in, in, in, in, io, in, in, in).mutate7();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_8").input(in, in, in, in, in, in, in, io, in, in).mutate8();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_9").input(in, in, in, in, in, in, in, in, io, in).mutate9();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_10").input(in, in, in, in, in, in, in, in, in, io).mutate10();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace11_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_1").input(io, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_2").input(in, io, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_3").input(in, in, io, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_4").input(in, in, in, io, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_5").input(in, in, in, in, io, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_6").input(in, in, in, in, in, io, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_7").input(in, in, in, in, in, in, io, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_8").input(in, in, in, in, in, in, in, io, in, in, in).mutate8();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_9").input(in, in, in, in, in, in, in, in, io, in, in).mutate9();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_10").input(in, in, in, in, in, in, in, in, in, io, in).mutate10();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_11").input(in, in, in, in, in, in, in, in, in, in, io).mutate11();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace12_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_1").input(io, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_2").input(in, io, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_3").input(in, in, io, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_4").input(in, in, in, io, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_5").input(in, in, in, in, io, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_6").input(in, in, in, in, in, io, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_7").input(in, in, in, in, in, in, io, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_8").input(in, in, in, in, in, in, in, io, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_9").input(in, in, in, in, in, in, in, in, io, in, in, in).mutate9();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_10").input(in, in, in, in, in, in, in, in, in, io, in, in).mutate10();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_11").input(in, in, in, in, in, in, in, in, in, in, io, in).mutate11();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_12").input(in, in, in, in, in, in, in, in, in, in, in, io).mutate12();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace13_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_1").input(io, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_2").input(in, io, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_3").input(in, in, io, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_4").input(in, in, in, io, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_5").input(in, in, in, in, io, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_6").input(in, in, in, in, in, io, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_7").input(in, in, in, in, in, in, io, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_8").input(in, in, in, in, in, in, in, io, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_9").input(in, in, in, in, in, in, in, in, io, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_10").input(in, in, in, in, in, in, in, in, in, io, in, in, in).mutate10();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_11").input(in, in, in, in, in, in, in, in, in, in, io, in, in).mutate11();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_12").input(in, in, in, in, in, in, in, in, in, in, in, io, in).mutate12();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_13").input(in, in, in, in, in, in, in, in, in, in, in, in, io).mutate13();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace14_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_1").input(io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_2").input(in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_3").input(in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_4").input(in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_5").input(in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_6").input(in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_7").input(in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_8").input(in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_9").input(in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_10").input(in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_11").input(in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate11();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_12").input(in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate12();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_13").input(in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate13();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_14").input(in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate14();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace15_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_1").input(io, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_2").input(in, io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_3").input(in, in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_4").input(in, in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_5").input(in, in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_6").input(in, in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_7").input(in, in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_8").input(in, in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_9").input(in, in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_10").input(in, in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_11").input(in, in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate11();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_12").input(in, in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate12();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_13").input(in, in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate13();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_14").input(in, in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate14();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_15() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_15").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate15();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace16_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_1").input(io, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_2").input(in, io, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_3").input(in, in, io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_4").input(in, in, in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_5").input(in, in, in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_6").input(in, in, in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_7").input(in, in, in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_8").input(in, in, in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_9").input(in, in, in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_10").input(in, in, in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_11").input(in, in, in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate11();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_12").input(in, in, in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate12();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_13").input(in, in, in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate13();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_14").input(in, in, in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate14();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_15() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_15").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate15();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_16() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_16").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate16();
		assertTrue(outputExpected(io, 16));
	}

	// -- Dependent Functions -- //

	@Test
	public void testDependentMethodFunction1() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 1), out, 0);
	}

	@Test
	public void testDependentMethodFunction2() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 2), out, 0);
	}

	@Test
	public void testDependentMethodFunction3() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 3), out, 0);
	}

	@Test
	public void testDependentMethodFunction4() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 4), out, 0);
	}

	@Test
	public void testDependentMethodFunction5() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 5), out, 0);
	}

	@Test
	public void testDependentMethodFunction6() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 6), out, 0);
	}

	@Test
	public void testDependentMethodFunction7() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 7), out, 0);
	}

	@Test
	public void testDependentMethodFunction8() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 8), out, 0);
	}

	@Test
	public void testDependentMethodFunction9() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 9), out, 0);
	}

	@Test
	public void testDependentMethodFunction10() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 10), out, 0);
	}

	@Test
	public void testDependentMethodFunction11() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 11), out, 0);
	}

	@Test
	public void testDependentMethodFunction12() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 12), out, 0);
	}

	@Test
	public void testDependentMethodFunction13() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 13), out, 0);
	}

	@Test
	public void testDependentMethodFunction14() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 14), out, 0);
	}

	@Test
	public void testDependentMethodFunction15() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 15), out, 0);
	}

	@Test
	public void testDependentMethodFunction16() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 16), out, 0);
	}

	// -- Dependent Computers -- //

	@Test
	public void testDependentMethodComputer1() {
		String in = "1";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in)
			.output(out).compute();
		assertEquals(expected(1, 1), out);
	}

	@Test
	public void testDependentMethodComputer2() {
		String in = "2";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in)
			.output(out).compute();
		assertEquals(expected(2, 2), out);
	}

	@Test
	public void testDependentMethodComputer3() {
		String in = "3";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in)
			.output(out).compute();
		assertEquals(expected(3, 3), out);
	}

	@Test
	public void testDependentMethodComputer4() {
		String in = "4";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in)
			.output(out).compute();
		assertEquals(expected(4, 4), out);
	}

	@Test
	public void testDependentMethodComputer5() {
		String in = "5";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(5, 5), out);
	}

	@Test
	public void testDependentMethodComputer6() {
		String in = "6";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(6, 6), out);
	}

	@Test
	public void testDependentMethodComputer7() {
		String in = "7";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(7, 7), out);
	}

	@Test
	public void testDependentMethodComputer8() {
		String in = "8";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(8, 8), out);
	}

	@Test
	public void testDependentMethodComputer9() {
		String in = "9";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(9, 9), out);
	}

	@Test
	public void testDependentMethodComputer10() {
		String in = "10";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(10, 10), out);
	}

	@Test
	public void testDependentMethodComputer11() {
		String in = "11";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(11, 11), out);
	}

	@Test
	public void testDependentMethodComputer12() {
		String in = "12";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(12, 12), out);
	}

	@Test
	public void testDependentMethodComputer13() {
		String in = "13";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(13, 13), out);
	}

	@Test
	public void testDependentMethodComputer14() {
		String in = "14";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(14, 14), out);
	}

	@Test
	public void testDependentMethodComputer15() {
		String in = "15";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(15, 15), out);
	}

	@Test
	public void testDependentMethodComputer16() {
		String in = "16";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(16, 16), out);
	}

	// -- Dependent Inplaces -- //

	@Test
	public void testDependentMethodInplace1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles1").input(io).mutate();
		assertTrue(outputExpected(io, 1));
	}

	@Test
	public void testDependentMethodInplace2_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles2_1").input(io, in).mutate1();
		assertTrue(outputExpected(io, 2));
	}

	@Test
	public void testDependentMethodInplace2_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles2_2").input(in, io).mutate2();
		assertTrue(outputExpected(io, 2));
	}

	@Test
	public void testDependentMethodInplace3_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles3_1").input(io, in, in).mutate1();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testDependentMethodInplace3_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles3_2").input(in, io, in).mutate2();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testDependentMethodInplace3_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles3_3").input(in, in, io).mutate3();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testDependentMethodInplace4_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles4_1").input(io, in, in, in).mutate1();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testDependentMethodInplace4_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles4_2").input(in, io, in, in).mutate2();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testDependentMethodInplace4_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles4_3").input(in, in, io, in).mutate3();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testDependentMethodInplace4_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles4_4").input(in, in, in, io).mutate4();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testDependentMethodInplace5_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_1").input(io, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace5_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_2").input(in, io, in, in, in).mutate2();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace5_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_3").input(in, in, io, in, in).mutate3();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace5_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_4").input(in, in, in, io, in).mutate4();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace5_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_5").input(in, in, in, in, io).mutate5();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace6_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_1").input(io, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_2").input(in, io, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_3").input(in, in, io, in, in, in).mutate3();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_4").input(in, in, in, io, in, in).mutate4();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_5").input(in, in, in, in, io, in).mutate5();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_6").input(in, in, in, in, in, io).mutate6();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace7_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_1").input(io, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_2").input(in, io, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_3").input(in, in, io, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_4").input(in, in, in, io, in, in, in).mutate4();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_5").input(in, in, in, in, io, in, in).mutate5();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_6").input(in, in, in, in, in, io, in).mutate6();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_7").input(in, in, in, in, in, in, io).mutate7();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace8_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_1").input(io, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_2").input(in, io, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_3").input(in, in, io, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_4").input(in, in, in, io, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_5").input(in, in, in, in, io, in, in, in).mutate5();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_6").input(in, in, in, in, in, io, in, in).mutate6();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_7").input(in, in, in, in, in, in, io, in).mutate7();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_8").input(in, in, in, in, in, in, in, io).mutate8();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace9_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_1").input(io, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_2").input(in, io, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_3").input(in, in, io, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_4").input(in, in, in, io, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_5").input(in, in, in, in, io, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_6").input(in, in, in, in, in, io, in, in, in).mutate6();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_7").input(in, in, in, in, in, in, io, in, in).mutate7();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_8").input(in, in, in, in, in, in, in, io, in).mutate8();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_9").input(in, in, in, in, in, in, in, in, io).mutate9();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace10_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_1").input(io, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_2").input(in, io, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_3").input(in, in, io, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_4").input(in, in, in, io, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_5").input(in, in, in, in, io, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_6").input(in, in, in, in, in, io, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_7").input(in, in, in, in, in, in, io, in, in, in).mutate7();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_8").input(in, in, in, in, in, in, in, io, in, in).mutate8();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_9").input(in, in, in, in, in, in, in, in, io, in).mutate9();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_10").input(in, in, in, in, in, in, in, in, in, io).mutate10();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace11_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_1").input(io, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_2").input(in, io, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_3").input(in, in, io, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_4").input(in, in, in, io, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_5").input(in, in, in, in, io, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_6").input(in, in, in, in, in, io, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_7").input(in, in, in, in, in, in, io, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_8").input(in, in, in, in, in, in, in, io, in, in, in).mutate8();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_9").input(in, in, in, in, in, in, in, in, io, in, in).mutate9();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_10").input(in, in, in, in, in, in, in, in, in, io, in).mutate10();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_11").input(in, in, in, in, in, in, in, in, in, in, io).mutate11();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace12_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_1").input(io, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_2").input(in, io, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_3").input(in, in, io, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_4").input(in, in, in, io, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_5").input(in, in, in, in, io, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_6").input(in, in, in, in, in, io, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_7").input(in, in, in, in, in, in, io, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_8").input(in, in, in, in, in, in, in, io, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_9").input(in, in, in, in, in, in, in, in, io, in, in, in).mutate9();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_10").input(in, in, in, in, in, in, in, in, in, io, in, in).mutate10();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_11").input(in, in, in, in, in, in, in, in, in, in, io, in).mutate11();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_12").input(in, in, in, in, in, in, in, in, in, in, in, io).mutate12();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace13_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_1").input(io, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_2").input(in, io, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_3").input(in, in, io, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_4").input(in, in, in, io, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_5").input(in, in, in, in, io, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_6").input(in, in, in, in, in, io, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_7").input(in, in, in, in, in, in, io, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_8").input(in, in, in, in, in, in, in, io, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_9").input(in, in, in, in, in, in, in, in, io, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_10").input(in, in, in, in, in, in, in, in, in, io, in, in, in).mutate10();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_11").input(in, in, in, in, in, in, in, in, in, in, io, in, in).mutate11();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_12").input(in, in, in, in, in, in, in, in, in, in, in, io, in).mutate12();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_13").input(in, in, in, in, in, in, in, in, in, in, in, in, io).mutate13();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace14_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_1").input(io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_2").input(in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_3").input(in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_4").input(in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_5").input(in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_6").input(in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_7").input(in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_8").input(in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_9").input(in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_10").input(in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_11").input(in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate11();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_12").input(in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate12();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_13").input(in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate13();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_14").input(in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate14();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace15_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_1").input(io, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_2").input(in, io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_3").input(in, in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_4").input(in, in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_5").input(in, in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_6").input(in, in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_7").input(in, in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_8").input(in, in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_9").input(in, in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_10").input(in, in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_11").input(in, in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate11();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_12").input(in, in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate12();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_13").input(in, in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate13();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_14").input(in, in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate14();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_15() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_15").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate15();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace16_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_1").input(io, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_2").input(in, io, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_3").input(in, in, io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_4").input(in, in, in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_5").input(in, in, in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_6").input(in, in, in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_7").input(in, in, in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_8").input(in, in, in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_9").input(in, in, in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_10").input(in, in, in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_11").input(in, in, in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate11();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_12").input(in, in, in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate12();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_13").input(in, in, in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate13();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_14").input(in, in, in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate14();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_15() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_15").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate15();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_16() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_16").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate16();
		assertTrue(outputExpected(io, 16));
	}
}
