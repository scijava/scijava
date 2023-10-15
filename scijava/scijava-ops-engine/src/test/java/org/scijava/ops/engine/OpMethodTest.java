/*
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

package org.scijava.ops.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

/**
 * Tests the construction of {@link OpMethod}s.
 * 
 * @author Gabriel Selzer
 * @author Marcel Wiedenmann
 */
public class OpMethodTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OpMethodTestOps());
	}

	// -- Functions -- //

	@Test
	public void testOpMethodProducer() {
		final Integer out = ops.op("test.multiplyNumericStrings").arity0()
			.outType(Integer.class).create();
		final Integer expected = Integer.valueOf(1);
		assertEquals(expected, out);
	}

	@Test
	public void testOpMethodFunction1() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity1().input(in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 1), out, 0);
	}

	@Test
	public void testOpMethodFunction2() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity2().input(in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 2), out, 0);
	}

	@Test
	public void testOpMethodFunction3() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity3().input(in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 3), out, 0);
	}

	@Test
	public void testOpMethodFunction4() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity4().input(in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 4), out, 0);
	}

	@Test
	public void testOpMethodFunction5() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity5().input(in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 5), out, 0);
	}

	@Test
	public void testOpMethodFunction6() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity6().input(in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 6), out, 0);
	}

	@Test
	public void testOpMethodFunction7() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity7().input(in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 7), out, 0);
	}

	@Test
	public void testOpMethodFunction8() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity8().input(in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 8), out, 0);
	}

	@Test
	public void testOpMethodFunction9() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity9().input(in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 9), out, 0);
	}

	@Test
	public void testOpMethodFunction10() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity10().input(in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 10), out, 0);
	}

	@Test
	public void testOpMethodFunction11() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity11().input(in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 11), out, 0);
	}

	@Test
	public void testOpMethodFunction12() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity12().input(in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 12), out, 0);
	}

	@Test
	public void testOpMethodFunction13() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity13().input(in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 13), out, 0);
	}

	@Test
	public void testOpMethodFunction14() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity14().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 14), out, 0);
	}

	@Test
	public void testOpMethodFunction15() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 15), out, 0);
	}

	@Test
	public void testOpMethodFunction16() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
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
		ops.op("test.doubleList").arity0()			.output(out).compute();
		assertEquals(expected(0, 0), out);
	}

	@Test
	public void testOpMethodComputer1() {
		String in = "1";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity1().input(in)			.output(out).compute();
		assertEquals(expected(1, 1), out);
	}

	@Test
	public void testOpMethodComputer2() {
		String in = "2";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity2().input(in, in)			.output(out).compute();
		assertEquals(expected(2, 2), out);
	}

	@Test
	public void testOpMethodComputer3() {
		String in = "3";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity3().input(in, in, in)			.output(out).compute();
		assertEquals(expected(3, 3), out);
	}

	@Test
	public void testOpMethodComputer4() {
		String in = "4";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity4().input(in, in, in, in)			.output(out).compute();
		assertEquals(expected(4, 4), out);
	}

	@Test
	public void testOpMethodComputer5() {
		String in = "5";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity5().input(in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(5, 5), out);
	}

	@Test
	public void testOpMethodComputer6() {
		String in = "6";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity6().input(in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(6, 6), out);
	}

	@Test
	public void testOpMethodComputer7() {
		String in = "7";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity7().input(in, in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(7, 7), out);
	}

	@Test
	public void testOpMethodComputer8() {
		String in = "8";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity8().input(in, in, in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(8, 8), out);
	}

	@Test
	public void testOpMethodComputer9() {
		String in = "9";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity9().input(in, in, in, in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(9, 9), out);
	}

	@Test
	public void testOpMethodComputer10() {
		String in = "10";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity10().input(in, in, in, in, in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(10, 10), out);
	}

	@Test
	public void testOpMethodComputer11() {
		String in = "11";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity11().input(in, in, in, in, in, in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(11, 11), out);
	}

	@Test
	public void testOpMethodComputer12() {
		String in = "12";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity12().input(in, in, in, in, in, in, in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(12, 12), out);
	}

	@Test
	public void testOpMethodComputer13() {
		String in = "13";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity13().input(in, in, in, in, in, in, in, in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(13, 13), out);
	}

	@Test
	public void testOpMethodComputer14() {
		String in = "14";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity14().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(14, 14), out);
	}

	@Test
	public void testOpMethodComputer15() {
		String in = "15";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)			.output(out).compute();
		assertEquals(expected(15, 15), out);
	}

	@Test
	public void testOpMethodComputer16() {
		String in = "16";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)			.output(out).compute();
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
		ops.op("test.addDoubles1").arity1().input(io).mutate();
		assertTrue(outputExpected(io, 1));
	}

	@Test
	public void testOpMethodInplace2_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles2_1").arity2().input(io, in).mutate1();
		assertTrue(outputExpected(io, 2));
	}

	@Test
	public void testOpMethodInplace2_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles2_2").arity2().input(in, io).mutate2();
		assertTrue(outputExpected(io, 2));
	}

	@Test
	public void testOpMethodInplace3_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles3_1").arity3().input(io, in, in).mutate1();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testOpMethodInplace3_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles3_2").arity3().input(in, io, in).mutate2();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testOpMethodInplace3_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles3_3").arity3().input(in, in, io).mutate3();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testOpMethodInplace4_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles4_1").arity4().input(io, in, in, in).mutate1();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testOpMethodInplace4_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles4_2").arity4().input(in, io, in, in).mutate2();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testOpMethodInplace4_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles4_3").arity4().input(in, in, io, in).mutate3();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testOpMethodInplace4_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles4_4").arity4().input(in, in, in, io).mutate4();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testOpMethodInplace5_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_1").arity5().input(io, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace5_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_2").arity5().input(in, io, in, in, in).mutate2();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace5_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_3").arity5().input(in, in, io, in, in).mutate3();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace5_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_4").arity5().input(in, in, in, io, in).mutate4();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace5_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles5_5").arity5().input(in, in, in, in, io).mutate5();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testOpMethodInplace6_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_1").arity6().input(io, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_2").arity6().input(in, io, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_3").arity6().input(in, in, io, in, in, in).mutate3();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_4").arity6().input(in, in, in, io, in, in).mutate4();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_5").arity6().input(in, in, in, in, io, in).mutate5();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace6_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles6_6").arity6().input(in, in, in, in, in, io).mutate6();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testOpMethodInplace7_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_1").arity7().input(io, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_2").arity7().input(in, io, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_3").arity7().input(in, in, io, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_4").arity7().input(in, in, in, io, in, in, in).mutate4();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_5").arity7().input(in, in, in, in, io, in, in).mutate5();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_6").arity7().input(in, in, in, in, in, io, in).mutate6();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace7_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles7_7").arity7().input(in, in, in, in, in, in, io).mutate7();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testOpMethodInplace8_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_1").arity8().input(io, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_2").arity8().input(in, io, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_3").arity8().input(in, in, io, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_4").arity8().input(in, in, in, io, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_5").arity8().input(in, in, in, in, io, in, in, in).mutate5();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_6").arity8().input(in, in, in, in, in, io, in, in).mutate6();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_7").arity8().input(in, in, in, in, in, in, io, in).mutate7();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace8_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles8_8").arity8().input(in, in, in, in, in, in, in, io).mutate8();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testOpMethodInplace9_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_1").arity9().input(io, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_2").arity9().input(in, io, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_3").arity9().input(in, in, io, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_4").arity9().input(in, in, in, io, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_5").arity9().input(in, in, in, in, io, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_6").arity9().input(in, in, in, in, in, io, in, in, in).mutate6();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_7").arity9().input(in, in, in, in, in, in, io, in, in).mutate7();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_8").arity9().input(in, in, in, in, in, in, in, io, in).mutate8();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace9_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles9_9").arity9().input(in, in, in, in, in, in, in, in, io).mutate9();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testOpMethodInplace10_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_1").arity10().input(io, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_2").arity10().input(in, io, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_3").arity10().input(in, in, io, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_4").arity10().input(in, in, in, io, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_5").arity10().input(in, in, in, in, io, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_6").arity10().input(in, in, in, in, in, io, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_7").arity10().input(in, in, in, in, in, in, io, in, in, in).mutate7();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_8").arity10().input(in, in, in, in, in, in, in, io, in, in).mutate8();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_9").arity10().input(in, in, in, in, in, in, in, in, io, in).mutate9();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace10_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles10_10").arity10().input(in, in, in, in, in, in, in, in, in, io).mutate10();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testOpMethodInplace11_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_1").arity11().input(io, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_2").arity11().input(in, io, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_3").arity11().input(in, in, io, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_4").arity11().input(in, in, in, io, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_5").arity11().input(in, in, in, in, io, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_6").arity11().input(in, in, in, in, in, io, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_7").arity11().input(in, in, in, in, in, in, io, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_8").arity11().input(in, in, in, in, in, in, in, io, in, in, in).mutate8();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_9").arity11().input(in, in, in, in, in, in, in, in, io, in, in).mutate9();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_10").arity11().input(in, in, in, in, in, in, in, in, in, io, in).mutate10();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace11_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles11_11").arity11().input(in, in, in, in, in, in, in, in, in, in, io).mutate11();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testOpMethodInplace12_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_1").arity12().input(io, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_2").arity12().input(in, io, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_3").arity12().input(in, in, io, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_4").arity12().input(in, in, in, io, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_5").arity12().input(in, in, in, in, io, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_6").arity12().input(in, in, in, in, in, io, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_7").arity12().input(in, in, in, in, in, in, io, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_8").arity12().input(in, in, in, in, in, in, in, io, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_9").arity12().input(in, in, in, in, in, in, in, in, io, in, in, in).mutate9();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_10").arity12().input(in, in, in, in, in, in, in, in, in, io, in, in).mutate10();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_11").arity12().input(in, in, in, in, in, in, in, in, in, in, io, in).mutate11();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace12_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles12_12").arity12().input(in, in, in, in, in, in, in, in, in, in, in, io).mutate12();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testOpMethodInplace13_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_1").arity13().input(io, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_2").arity13().input(in, io, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_3").arity13().input(in, in, io, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_4").arity13().input(in, in, in, io, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_5").arity13().input(in, in, in, in, io, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_6").arity13().input(in, in, in, in, in, io, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_7").arity13().input(in, in, in, in, in, in, io, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_8").arity13().input(in, in, in, in, in, in, in, io, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_9").arity13().input(in, in, in, in, in, in, in, in, io, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_10").arity13().input(in, in, in, in, in, in, in, in, in, io, in, in, in).mutate10();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_11").arity13().input(in, in, in, in, in, in, in, in, in, in, io, in, in).mutate11();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_12").arity13().input(in, in, in, in, in, in, in, in, in, in, in, io, in).mutate12();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace13_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles13_13").arity13().input(in, in, in, in, in, in, in, in, in, in, in, in, io).mutate13();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testOpMethodInplace14_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_1").arity14().input(io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_2").arity14().input(in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_3").arity14().input(in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_4").arity14().input(in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_5").arity14().input(in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_6").arity14().input(in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_7").arity14().input(in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_8").arity14().input(in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_9").arity14().input(in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_10").arity14().input(in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_11").arity14().input(in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate11();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_12").arity14().input(in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate12();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_13").arity14().input(in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate13();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace14_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles14_14").arity14().input(in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate14();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testOpMethodInplace15_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_1").arity15().input(io, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_2").arity15().input(in, io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_3").arity15().input(in, in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_4").arity15().input(in, in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_5").arity15().input(in, in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_6").arity15().input(in, in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_7").arity15().input(in, in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_8").arity15().input(in, in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_9").arity15().input(in, in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_10").arity15().input(in, in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_11").arity15().input(in, in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate11();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_12").arity15().input(in, in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate12();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_13").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate13();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_14").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate14();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace15_15() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles15_15").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate15();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testOpMethodInplace16_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_1").arity16().input(io, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_2").arity16().input(in, io, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_3").arity16().input(in, in, io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_4").arity16().input(in, in, in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_5").arity16().input(in, in, in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_6").arity16().input(in, in, in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_7").arity16().input(in, in, in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_8").arity16().input(in, in, in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_9").arity16().input(in, in, in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_10").arity16().input(in, in, in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_11").arity16().input(in, in, in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate11();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_12").arity16().input(in, in, in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate12();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_13").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate13();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_14").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate14();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_15() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_15").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate15();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testOpMethodInplace16_16() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles16_16").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate16();
		assertTrue(outputExpected(io, 16));
	}

	// -- Dependent Functions -- //

	@Test
	public void testDependentMethodFunction1() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity1().input(in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 1), out, 0);
	}

	@Test
	public void testDependentMethodFunction2() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity2().input(in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 2), out, 0);
	}

	@Test
	public void testDependentMethodFunction3() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity3().input(in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 3), out, 0);
	}

	@Test
	public void testDependentMethodFunction4() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity4().input(in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 4), out, 0);
	}

	@Test
	public void testDependentMethodFunction5() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity5().input(in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 5), out, 0);
	}

	@Test
	public void testDependentMethodFunction6() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity6().input(in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 6), out, 0);
	}

	@Test
	public void testDependentMethodFunction7() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity7().input(in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 7), out, 0);
	}

	@Test
	public void testDependentMethodFunction8() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity8().input(in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 8), out, 0);
	}

	@Test
	public void testDependentMethodFunction9() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity9().input(in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 9), out, 0);
	}

	@Test
	public void testDependentMethodFunction10() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity10().input(in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 10), out, 0);
	}

	@Test
	public void testDependentMethodFunction11() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity11().input(in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 11), out, 0);
	}

	@Test
	public void testDependentMethodFunction12() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity12().input(in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 12), out, 0);
	}

	@Test
	public void testDependentMethodFunction13() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity13().input(in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 13), out, 0);
	}

	@Test
	public void testDependentMethodFunction14() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity14().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 14), out, 0);
	}

	@Test
	public void testDependentMethodFunction15() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 15), out, 0);
	}

	@Test
	public void testDependentMethodFunction16() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, 16), out, 0);
	}

	// -- Dependent Computers -- //

	@Test
	public void testDependentMethodComputer1() {
		String in = "1";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity1().input(in)
			.output(out).compute();
		assertEquals(expected(1, 1), out);
	}

	@Test
	public void testDependentMethodComputer2() {
		String in = "2";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity2().input(in, in)
			.output(out).compute();
		assertEquals(expected(2, 2), out);
	}

	@Test
	public void testDependentMethodComputer3() {
		String in = "3";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity3().input(in, in, in)
			.output(out).compute();
		assertEquals(expected(3, 3), out);
	}

	@Test
	public void testDependentMethodComputer4() {
		String in = "4";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity4().input(in, in, in, in)
			.output(out).compute();
		assertEquals(expected(4, 4), out);
	}

	@Test
	public void testDependentMethodComputer5() {
		String in = "5";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity5().input(in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(5, 5), out);
	}

	@Test
	public void testDependentMethodComputer6() {
		String in = "6";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity6().input(in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(6, 6), out);
	}

	@Test
	public void testDependentMethodComputer7() {
		String in = "7";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity7().input(in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(7, 7), out);
	}

	@Test
	public void testDependentMethodComputer8() {
		String in = "8";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity8().input(in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(8, 8), out);
	}

	@Test
	public void testDependentMethodComputer9() {
		String in = "9";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity9().input(in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(9, 9), out);
	}

	@Test
	public void testDependentMethodComputer10() {
		String in = "10";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity10().input(in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(10, 10), out);
	}

	@Test
	public void testDependentMethodComputer11() {
		String in = "11";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity11().input(in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(11, 11), out);
	}

	@Test
	public void testDependentMethodComputer12() {
		String in = "12";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity12().input(in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(12, 12), out);
	}

	@Test
	public void testDependentMethodComputer13() {
		String in = "13";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity13().input(in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(13, 13), out);
	}

	@Test
	public void testDependentMethodComputer14() {
		String in = "14";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity14().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(14, 14), out);
	}

	@Test
	public void testDependentMethodComputer15() {
		String in = "15";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(15, 15), out);
	}

	@Test
	public void testDependentMethodComputer16() {
		String in = "16";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in)
			.output(out).compute();
		assertEquals(expected(16, 16), out);
	}

	// -- Dependent Inplaces -- //

	@Test
	public void testDependentMethodInplace1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles1").arity1().input(io).mutate();
		assertTrue(outputExpected(io, 1));
	}

	@Test
	public void testDependentMethodInplace2_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles2_1").arity2().input(io, in).mutate1();
		assertTrue(outputExpected(io, 2));
	}

	@Test
	public void testDependentMethodInplace2_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles2_2").arity2().input(in, io).mutate2();
		assertTrue(outputExpected(io, 2));
	}

	@Test
	public void testDependentMethodInplace3_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles3_1").arity3().input(io, in, in).mutate1();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testDependentMethodInplace3_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles3_2").arity3().input(in, io, in).mutate2();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testDependentMethodInplace3_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles3_3").arity3().input(in, in, io).mutate3();
		assertTrue(outputExpected(io, 3));
	}

	@Test
	public void testDependentMethodInplace4_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles4_1").arity4().input(io, in, in, in).mutate1();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testDependentMethodInplace4_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles4_2").arity4().input(in, io, in, in).mutate2();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testDependentMethodInplace4_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles4_3").arity4().input(in, in, io, in).mutate3();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testDependentMethodInplace4_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles4_4").arity4().input(in, in, in, io).mutate4();
		assertTrue(outputExpected(io, 4));
	}

	@Test
	public void testDependentMethodInplace5_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_1").arity5().input(io, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace5_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_2").arity5().input(in, io, in, in, in).mutate2();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace5_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_3").arity5().input(in, in, io, in, in).mutate3();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace5_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_4").arity5().input(in, in, in, io, in).mutate4();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace5_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles5_5").arity5().input(in, in, in, in, io).mutate5();
		assertTrue(outputExpected(io, 5));
	}

	@Test
	public void testDependentMethodInplace6_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_1").arity6().input(io, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_2").arity6().input(in, io, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_3").arity6().input(in, in, io, in, in, in).mutate3();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_4").arity6().input(in, in, in, io, in, in).mutate4();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_5").arity6().input(in, in, in, in, io, in).mutate5();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace6_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles6_6").arity6().input(in, in, in, in, in, io).mutate6();
		assertTrue(outputExpected(io, 6));
	}

	@Test
	public void testDependentMethodInplace7_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_1").arity7().input(io, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_2").arity7().input(in, io, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_3").arity7().input(in, in, io, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_4").arity7().input(in, in, in, io, in, in, in).mutate4();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_5").arity7().input(in, in, in, in, io, in, in).mutate5();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_6").arity7().input(in, in, in, in, in, io, in).mutate6();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace7_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles7_7").arity7().input(in, in, in, in, in, in, io).mutate7();
		assertTrue(outputExpected(io, 7));
	}

	@Test
	public void testDependentMethodInplace8_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_1").arity8().input(io, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_2").arity8().input(in, io, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_3").arity8().input(in, in, io, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_4").arity8().input(in, in, in, io, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_5").arity8().input(in, in, in, in, io, in, in, in).mutate5();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_6").arity8().input(in, in, in, in, in, io, in, in).mutate6();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_7").arity8().input(in, in, in, in, in, in, io, in).mutate7();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace8_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles8_8").arity8().input(in, in, in, in, in, in, in, io).mutate8();
		assertTrue(outputExpected(io, 8));
	}

	@Test
	public void testDependentMethodInplace9_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_1").arity9().input(io, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_2").arity9().input(in, io, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_3").arity9().input(in, in, io, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_4").arity9().input(in, in, in, io, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_5").arity9().input(in, in, in, in, io, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_6").arity9().input(in, in, in, in, in, io, in, in, in).mutate6();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_7").arity9().input(in, in, in, in, in, in, io, in, in).mutate7();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_8").arity9().input(in, in, in, in, in, in, in, io, in).mutate8();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace9_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles9_9").arity9().input(in, in, in, in, in, in, in, in, io).mutate9();
		assertTrue(outputExpected(io, 9));
	}

	@Test
	public void testDependentMethodInplace10_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_1").arity10().input(io, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_2").arity10().input(in, io, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_3").arity10().input(in, in, io, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_4").arity10().input(in, in, in, io, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_5").arity10().input(in, in, in, in, io, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_6").arity10().input(in, in, in, in, in, io, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_7").arity10().input(in, in, in, in, in, in, io, in, in, in).mutate7();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_8").arity10().input(in, in, in, in, in, in, in, io, in, in).mutate8();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_9").arity10().input(in, in, in, in, in, in, in, in, io, in).mutate9();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace10_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles10_10").arity10().input(in, in, in, in, in, in, in, in, in, io).mutate10();
		assertTrue(outputExpected(io, 10));
	}

	@Test
	public void testDependentMethodInplace11_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_1").arity11().input(io, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_2").arity11().input(in, io, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_3").arity11().input(in, in, io, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_4").arity11().input(in, in, in, io, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_5").arity11().input(in, in, in, in, io, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_6").arity11().input(in, in, in, in, in, io, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_7").arity11().input(in, in, in, in, in, in, io, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_8").arity11().input(in, in, in, in, in, in, in, io, in, in, in).mutate8();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_9").arity11().input(in, in, in, in, in, in, in, in, io, in, in).mutate9();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_10").arity11().input(in, in, in, in, in, in, in, in, in, io, in).mutate10();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace11_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles11_11").arity11().input(in, in, in, in, in, in, in, in, in, in, io).mutate11();
		assertTrue(outputExpected(io, 11));
	}

	@Test
	public void testDependentMethodInplace12_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_1").arity12().input(io, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_2").arity12().input(in, io, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_3").arity12().input(in, in, io, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_4").arity12().input(in, in, in, io, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_5").arity12().input(in, in, in, in, io, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_6").arity12().input(in, in, in, in, in, io, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_7").arity12().input(in, in, in, in, in, in, io, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_8").arity12().input(in, in, in, in, in, in, in, io, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_9").arity12().input(in, in, in, in, in, in, in, in, io, in, in, in).mutate9();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_10").arity12().input(in, in, in, in, in, in, in, in, in, io, in, in).mutate10();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_11").arity12().input(in, in, in, in, in, in, in, in, in, in, io, in).mutate11();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace12_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles12_12").arity12().input(in, in, in, in, in, in, in, in, in, in, in, io).mutate12();
		assertTrue(outputExpected(io, 12));
	}

	@Test
	public void testDependentMethodInplace13_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_1").arity13().input(io, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_2").arity13().input(in, io, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_3").arity13().input(in, in, io, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_4").arity13().input(in, in, in, io, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_5").arity13().input(in, in, in, in, io, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_6").arity13().input(in, in, in, in, in, io, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_7").arity13().input(in, in, in, in, in, in, io, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_8").arity13().input(in, in, in, in, in, in, in, io, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_9").arity13().input(in, in, in, in, in, in, in, in, io, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_10").arity13().input(in, in, in, in, in, in, in, in, in, io, in, in, in).mutate10();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_11").arity13().input(in, in, in, in, in, in, in, in, in, in, io, in, in).mutate11();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_12").arity13().input(in, in, in, in, in, in, in, in, in, in, in, io, in).mutate12();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace13_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles13_13").arity13().input(in, in, in, in, in, in, in, in, in, in, in, in, io).mutate13();
		assertTrue(outputExpected(io, 13));
	}

	@Test
	public void testDependentMethodInplace14_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_1").arity14().input(io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_2").arity14().input(in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_3").arity14().input(in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_4").arity14().input(in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_5").arity14().input(in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_6").arity14().input(in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_7").arity14().input(in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_8").arity14().input(in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_9").arity14().input(in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_10").arity14().input(in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_11").arity14().input(in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate11();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_12").arity14().input(in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate12();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_13").arity14().input(in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate13();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace14_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles14_14").arity14().input(in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate14();
		assertTrue(outputExpected(io, 14));
	}

	@Test
	public void testDependentMethodInplace15_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_1").arity15().input(io, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_2").arity15().input(in, io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_3").arity15().input(in, in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_4").arity15().input(in, in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_5").arity15().input(in, in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_6").arity15().input(in, in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_7").arity15().input(in, in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_8").arity15().input(in, in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_9").arity15().input(in, in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_10").arity15().input(in, in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_11").arity15().input(in, in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate11();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_12").arity15().input(in, in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate12();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_13").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate13();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_14").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate14();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace15_15() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles15_15").arity15().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate15();
		assertTrue(outputExpected(io, 15));
	}

	@Test
	public void testDependentMethodInplace16_1() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_1").arity16().input(io, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate1();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_2() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_2").arity16().input(in, io, in, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate2();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_3() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_3").arity16().input(in, in, io, in, in, in, in, in, in, in, in, in, in, in, in, in).mutate3();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_4() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_4").arity16().input(in, in, in, io, in, in, in, in, in, in, in, in, in, in, in, in).mutate4();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_5() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_5").arity16().input(in, in, in, in, io, in, in, in, in, in, in, in, in, in, in, in).mutate5();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_6() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_6").arity16().input(in, in, in, in, in, io, in, in, in, in, in, in, in, in, in, in).mutate6();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_7() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_7").arity16().input(in, in, in, in, in, in, io, in, in, in, in, in, in, in, in, in).mutate7();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_8() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_8").arity16().input(in, in, in, in, in, in, in, io, in, in, in, in, in, in, in, in).mutate8();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_9() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_9").arity16().input(in, in, in, in, in, in, in, in, io, in, in, in, in, in, in, in).mutate9();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_10() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_10").arity16().input(in, in, in, in, in, in, in, in, in, io, in, in, in, in, in, in).mutate10();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_11() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_11").arity16().input(in, in, in, in, in, in, in, in, in, in, io, in, in, in, in, in).mutate11();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_12() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_12").arity16().input(in, in, in, in, in, in, in, in, in, in, in, io, in, in, in, in).mutate12();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_13() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_13").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, io, in, in, in).mutate13();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_14() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_14").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, io, in, in).mutate14();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_15() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_15").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, io, in).mutate15();
		assertTrue(outputExpected(io, 16));
	}

	@Test
	public void testDependentMethodInplace16_16() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles16_16").arity16().input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, io).mutate16();
		assertTrue(outputExpected(io, 16));
	}
}
