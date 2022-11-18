package org.scijava.ops.engine.reduce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.ops.spi.Optional;

import java.util.Arrays;

public class OptionalArgumentsTest extends AbstractTestEnvironment //
		 implements OpCollection{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OptionalArgumentsTest());
		ops.register(new TestOpOptionalArg());
	}


	@Test
	public void testClassWithTwoOptionals() {
		Double sum = ops.op("test.optionalAdd").input(2.0, 5.0, 7.0).outType(Double.class).apply();
		Double expected = 14.0;
		Assertions.assertEquals(expected, sum);
	}

	@Test
	public void testClassWithOneOptional() {
		Double sum = ops.op("test.optionalAdd").input(2.0, 5.0).outType(Double.class).apply();
		Double expected = 7.0;
		Assertions.assertEquals(expected, sum);
	}

	@Test
	public void testClassWithoutOptionals() {
		Double sum = ops.op("test.optionalAdd").input(2.0).outType(Double.class).apply();
		Double expected = 2.0;
		Assertions.assertEquals(expected, sum);
	}

	@OpField(names = "test.optionalMultiply")
	public final Computers.Arity3<Double[], Double[], Double[], Double[]> optionalField =
		new Computers.Arity3<>()
		{

			@Override
			public void compute(Double[] in1, @Optional Double[] in2,
				@Optional Double[] in3, Double[] out)
		{
				if (in2 == null) {
					in2 = new Double[in1.length];
					Arrays.fill(in2, 1.);
				}
				if (in3 == null) {
					in3 = new Double[in1.length];
					Arrays.fill(in3, 1.);
				}
				for (int i = 0; i < in1.length; i++) {
					out[i] = in1[i] * in2[i] * in3[i];
				}
			}
		};

	@Test
	public void testFieldWithTwoOptionals() {
		Double[] d1 = {2.0};
		Double[] d2 = {5.0};
		Double[] d3 = {7.0};
		Double[] o = {50.0};
		ops.op("test.optionalMultiply").input(d1, d2, d3).output(o).compute();
		Double expected = 70.0;
		Assertions.assertEquals(expected, o[0]);
	}

	@Test
	public void testFieldWithOneOptional() {
		Double[] d1 = {2.0};
		Double[] d2 = {5.0};
		Double[] o = {50.0};
		ops.op("test.optionalMultiply").input(d1, d2).output(o).compute();
		Double expected = 10.0;
		Assertions.assertEquals(expected, o[0]);
	}

	@Test
	public void testFieldWithoutOptionals() {
		Double[] d1 = {2.0};
		Double[] o = {50.0};
		ops.op("test.optionalMultiply").input(d1).output(o).compute();
		Double expected = 2.0;
		Assertions.assertEquals(expected, o[0]);
	}

	@OpMethod(names = "test.optionalConcatenate", type = Functions.Arity3.class)
	public static String optionalMethod(String in1, @Optional String in2, @Optional String in3) {
		if (in2 == null) in2 = "";
		if (in3 == null) in3 = "";
		return in1.concat(in2).concat(in3);
	}

	@Test
	public void testMethodWithTwoOptionals() {
		String out = ops.op("test.optionalConcatenate").input("a", "b", "c").outType(String.class).apply();
		String expected = "abc";
		Assertions.assertEquals(expected, out);
	}

	@Test
	public void testMethodWithOneOptional() {
		String out = ops.op("test.optionalConcatenate").input("a", "b").outType(String.class).apply();
		String expected = "ab";
		Assertions.assertEquals(expected, out);
	}

	@Test
	public void testMethodWithoutOptionals() {
		String out = ops.op("test.optionalConcatenate").input("a").outType(String.class).apply();
		String expected = "a";
		Assertions.assertEquals(expected, out);
	}

}
