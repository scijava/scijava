package org.scijava.ops;

import org.scijava.ops.core.OpCollection;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class RunTest extends AbstractTestEnvironment {

	// FUNCTIONS
	
	//TODO: delete

//	@OpField(names = "test.function1")
//	public static final Function<Double, Double> f1 = num1 -> num1 + 1;
//
//	@Test
//	public void testRunFunction() {
//		Double answer = (Double) new OpBuilder(ops, "test.function1").input(1.0).apply();
//		Assert.assertEquals(2.0, answer, 0);
//	}
//
//	@OpField(names = "test.function2")
//	public static final BiFunction<Double, Double, Double> f2 = //
//		(num1, num2) -> num1 + num2;
//
//	@Test
//	public void testRunFunction2() {
//		Double answer = (Double) new OpBuilder(ops, "test.function2").input(1.0, 1.0).apply();
//		Assert.assertEquals(2.0, answer, 0);
//	}
//
//	@OpField(names = "test.function3")
//	public static final Functions.Arity3<Double, Double, Double, Double> f3 = //
//		(num1, num2, num3) -> num1 + num2 + num3;
//
//	@Test
//	public void testRunFunction3() {
//		Double answer = (Double) new OpBuilder(ops, "test.function3").input(1.0, 1.0, 1.0).apply();
//		Assert.assertEquals(3.0, answer, 0);
//	}
//
//	// COMPUTERS
//
//	@OpField(names = "test.computer1")
//	public static final Computers.Arity1<double[], double[]> c1 = (arr1, out) -> {
//		for (int i = 0; i < arr1.length; i++)
//			out[i] = arr1[i] * 2;
//	};
//
//	@Test
//	public void testRunComputer1() {
//		double[] arr1 = { 1, 2, 3 };
//		double[] out = new double[3];
//		new OpBuilder(ops, "test.computer1").input(arr1, out).apply();
//		Assert.assertArrayEquals(new double[] { 2, 4, 6 }, out, 0);
//	}
//
//	@OpField(names = "test.computer2")
//	public static final Computers.Arity2<double[], double[], double[]> testComputer2 = (arr1, arr2, out) -> {
//		for (int i = 0; i < arr1.length; i++) {
//			out[i] = arr1[i] + arr2[i];
//		}
//	};
//
//	@Test
//	public void testRunComputer2() {
//		double[] arr1 = { 1, 2, 3 };
//		double[] arr2 = { 1, 2, 3 };
//		double[] out = new double[3];
//		new OpBuilder(ops, "test.computer2").input(arr1, arr2, out).apply();
//		Assert.assertArrayEquals(new double[] { 2, 4, 6 }, out, 0);
//	}
//
//	@OpField(names = "test.computer3")
//	public static final Computers.Arity3<double[], double[], double[], double[]> testComputer3 = (arr1, arr2, arr3, out) -> {
//		for (int i = 0; i < arr1.length; i++) {
//			out[i] = arr1[i] + arr2[i] + arr3[i];
//		}
//	};
//
//	@Test
//	public void testRunComputer3() {
//		double[] arr1 = { 1, 2, 3 };
//		double[] arr2 = { 1, 2, 3 };
//		double[] arr3 = { 1, 2, 3 };
//		double[] out = new double[3];
//		new OpBuilder(ops, "test.computer3").input(arr1, arr2, arr3, out).apply();
//		Assert.assertArrayEquals(new double[] { 3, 6, 9 }, out, 0);
//	}
//
//	// INPLACES
//
//	@OpField(names = "test.inplace1")
//	public static final Inplaces.Arity1<double[]> testInplace = io -> {
//		for (int i = 0; i < io.length; i++)
//			io[i] *= 2;
//	};
//
//	@Test
//	public void testRunInplace() {
//		double[] io = { 1, 2, 3 };
//		new OpBuilder(ops, "test.inplace1").input(io).apply();
//		Assert.assertArrayEquals(new double[] { 2, 4, 6 }, io, 0);
//	}
//
//	@OpField(names = "test.inplace2_1")
//	public static final Inplaces.Arity2_1<double[], double[]> testInplace2_1 = (io, in2) -> {
//		for (int i = 0; i < io.length; i++)
//			io[i] += in2[i];
//	};
//
//	@Test
//	public void testRunInplace2_1() {
//		double[] io = { 1, 2, 3 };
//		double[] in2 = { 1, 2, 3 };
//		new OpBuilder(ops, "test.inplace2_1").input(io, in2).apply();
//		Assert.assertArrayEquals(new double[] { 2, 4, 6 }, io, 0);
//	}
//
//	@OpField(names = "test.inplace2_2")
//	public static final Inplaces.Arity2_2<double[], double[]> testInplace2_2 = (in1, io) -> {
//		for (int i = 0; i < io.length; i++)
//			io[i] *= in1[i];
//	};
//
//	@Test
//	public void testRunInplace2_2() {
//		double[] in1 = { 1, 2, 3 };
//		double[] io = { 1, 2, 3 };
//		new OpBuilder(ops, "test.inplace2_2").input(in1, io).apply();
//		Assert.assertArrayEquals(new double[] { 1, 4, 9 }, io, 0);
//	}
//
//	@OpField(names = "test.inplace3_1")
//	public static final Inplaces.Arity3_1<double[], double[], double[]> testInplace3_1 = (io, in2, in3) -> {
//		for (int i = 0; i < io.length; i++) {
//			io[i] += in2[i];
//			io[i] += in3[i];
//		}
//	};
//
//	@Test
//	public void testRunInplace3_1() {
//		double[] io = { 1, 2, 3 };
//		double[] in2 = { 1, 2, 3 };
//		double[] in3 = { 1, 2, 3 };
//		new OpBuilder(ops, "test.inplace3_1").input(io, in2, in3).apply();
//		Assert.assertArrayEquals(new double[] { 3, 6, 9 }, io, 0);
//	}
}
