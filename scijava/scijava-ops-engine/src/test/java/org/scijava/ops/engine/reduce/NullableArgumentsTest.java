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
import org.scijava.ops.spi.Nullable;

import java.util.Arrays;

public class NullableArgumentsTest extends AbstractTestEnvironment //
		 implements OpCollection{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new NullableArgumentsTest());
		ops.register(new TestOpNullableArg());
	}


	@Test
	public void testClassWithTwoNullables() {
		Double sum = ops.op("test.nullableAdd").arity3().input(2.0, 5.0, 7.0).outType(Double.class).apply();
		Double expected = 14.0;
		Assertions.assertEquals(expected, sum);
	}

	@Test
	public void testClassWithOneNullable() {
		Double sum = ops.op("test.nullableAdd").arity2().input(2.0, 5.0).outType(Double.class).apply();
		Double expected = 7.0;
		Assertions.assertEquals(expected, sum);
	}

	@Test
	public void testClassWithoutNullables() {
		Double sum = ops.op("test.nullableAdd").arity1().input(2.0).outType(Double.class).apply();
		Double expected = 2.0;
		Assertions.assertEquals(expected, sum);
	}

	@OpField(names = "test.nullableMultiply")
	public final Computers.Arity3<Double[], Double[], Double[], Double[]> nullableField =
		new Computers.Arity3<>()
		{

			@Override
			public void compute(Double[] in1, @Nullable Double[] in2,
				@Nullable Double[] in3, Double[] out)
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
	public void testFieldWithTwoNullables() {
		Double[] d1 = {2.0};
		Double[] d2 = {5.0};
		Double[] d3 = {7.0};
		Double[] o = {50.0};
		ops.op("test.nullableMultiply").arity3().input(d1, d2, d3).output(o).compute();
		Double expected = 70.0;
		Assertions.assertEquals(expected, o[0]);
	}

	@Test
	public void testFieldWithOneNullable() {
		Double[] d1 = {2.0};
		Double[] d2 = {5.0};
		Double[] o = {50.0};
		ops.op("test.nullableMultiply").arity2().input(d1, d2).output(o).compute();
		Double expected = 10.0;
		Assertions.assertEquals(expected, o[0]);
	}

	@Test
	public void testFieldWithoutNullables() {
		Double[] d1 = {2.0};
		Double[] o = {50.0};
		ops.op("test.nullableMultiply").arity1().input(d1).output(o).compute();
		Double expected = 2.0;
		Assertions.assertEquals(expected, o[0]);
	}

	@OpMethod(names = "test.nullableConcatenate", type = Functions.Arity3.class)
	public static String nullableMethod(String in1, @Nullable
	String in2, @Nullable String in3) {
		if (in2 == null) in2 = "";
		if (in3 == null) in3 = "";
		return in1.concat(in2).concat(in3);
	}

	@Test
	public void testMethodWithTwoNullables() {
		String out = ops.op("test.nullableConcatenate").arity3().input("a", "b", "c").outType(String.class).apply();
		String expected = "abc";
		Assertions.assertEquals(expected, out);
	}

	@Test
	public void testMethodWithOneNullable() {
		String out = ops.op("test.nullableConcatenate").arity2().input("a", "b").outType(String.class).apply();
		String expected = "ab";
		Assertions.assertEquals(expected, out);
	}

	@Test
	public void testMethodWithoutNullables() {
		String out = ops.op("test.nullableConcatenate").arity1().input("a").outType(String.class).apply();
		String expected = "a";
		Assertions.assertEquals(expected, out);
	}

}
