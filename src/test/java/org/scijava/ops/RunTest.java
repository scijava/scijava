package org.scijava.ops;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function4;
import org.scijava.ops.core.function.Function5;
import org.scijava.ops.core.inplace.BiInplaceFirst;
import org.scijava.ops.core.inplace.BiInplaceSecond;
import org.scijava.ops.core.inplace.Inplace;
import org.scijava.ops.core.inplace.Inplace3First;
import org.scijava.ops.core.inplace.Inplace4First;
import org.scijava.ops.core.inplace.Inplace5First;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class RunTest extends AbstractTestEnvironment {

	// FUNCTIONS

	@OpField(names = "test.Function")
	@Parameter(key = "num1")
	@Parameter(key = "sum", type = ItemIO.OUTPUT)
	public static final Function<Double, Double> testFunction = (num1) -> num1 + 1;

	@Test
	public void testRunFunction() {
		Double answer = (Double) ops.run("test.Function", 1.0);
		Assert.assertEquals(2.0, answer, 0);
	}

	@OpField(names = "test.BiFunction")
	@Parameter(key = "num1")
	@Parameter(key = "num2")
	@Parameter(key = "sum", type = ItemIO.OUTPUT)
	public static final BiFunction<Double, Double, Double> testBiFunction = (num1, num2) -> num1 + num2;

	@Test
	public void testRunBiFunction() {
		Double answer = (Double) ops.run("test.BiFunction", 1.0, 1.0);
		Assert.assertEquals(2.0, answer, 0);
	}

	@OpField(names = "test.Function3")
	@Parameter(key = "num1")
	@Parameter(key = "num2")
	@Parameter(key = "num3")
	@Parameter(key = "sum", type = ItemIO.OUTPUT)
	public static final Function3<Double, Double, Double, Double> testFunction3 = (num1, num2, num3) -> num1 + num2
			+ num3;

	@Test
	public void testRunFunction3() {
		Double answer = (Double) ops.run("test.Function3", 1.0, 1.0, 1.0);
		Assert.assertEquals(3.0, answer, 0);
	}

	@OpField(names = "test.Function4")
	@Parameter(key = "num1")
	@Parameter(key = "num2")
	@Parameter(key = "num3")
	@Parameter(key = "num4")
	@Parameter(key = "sum", type = ItemIO.OUTPUT)
	public static final Function4<Double, Double, Double, Double, Double> testFunction4 = (num1, num2, num3,
			num4) -> num1 + num2 + num3 + num4;

	@Test
	public void testRunFunction4() {
		Double answer = (Double) ops.run("test.Function4", 1.0, 1.0, 1.0, 1.0);
		Assert.assertEquals(4.0, answer, 0);
	}

	@OpField(names = "test.Function5")
	@Parameter(key = "num1")
	@Parameter(key = "num2")
	@Parameter(key = "num3")
	@Parameter(key = "num4")
	@Parameter(key = "num5")
	@Parameter(key = "sum", type = ItemIO.OUTPUT)
	public static final Function5<Double, Double, Double, Double, Double, Double> testFunction5 = (num1, num2, num3,
			num4, num5) -> num1 + num2 + num3 + num4 + num5;

	@Test
	public void testRunFunction5() {
		Double answer = (Double) ops.run("test.Function5", 1.0, 1.0, 1.0, 1.0, 1.0);
		Assert.assertEquals(5.0, answer, 0);
	}

	// COMPUTERS

	@OpField(names = "test.Computer")
	@Parameter(key = "arr1")
	@Parameter(key = "arr2", type = ItemIO.BOTH)
	public static final Computer<double[], double[]> testComputer = (arr1, out) -> {
		for (int i = 0; i < arr1.length; i++) {
			out[i] = arr1[i] * 2;
		}
	};

	@Test
	public void testRunComputer() {
		double[] arr1 = { 1, 2, 3 };
		double[] out = new double[3];
		ops.run("test.Computer", arr1, out);
		Assert.assertArrayEquals(new double[] { 2, 4, 6 }, out, 0);
	}

	@OpField(names = "test.BiComputer")
	@Parameter(key = "arr1")
	@Parameter(key = "arr2")
	@Parameter(key = "out", type = ItemIO.BOTH)
	public static final BiComputer<double[], double[], double[]> testBiComputer = (arr1, arr2, out) -> {
		for (int i = 0; i < arr1.length; i++) {
			out[i] = arr1[i] + arr2[i];
		}
	};

	@Test
	public void testRunBiComputer() {
		double[] arr1 = { 1, 2, 3 };
		double[] arr2 = { 1, 2, 3 };
		double[] out = new double[3];
		ops.run("test.BiComputer", arr1, arr2, out);
		Assert.assertArrayEquals(new double[] { 2, 4, 6 }, out, 0);
	}

	@OpField(names = "test.Computer3")
	@Parameter(key = "arr1")
	@Parameter(key = "arr2")
	@Parameter(key = "arr3")
	@Parameter(key = "out", type = ItemIO.BOTH)
	public static final Computer3<double[], double[], double[], double[]> testComputer3 = (arr1, arr2, arr3, out) -> {
		for (int i = 0; i < arr1.length; i++) {
			out[i] = arr1[i] + arr2[i] + arr3[i];
		}
	};

	@Test
	public void testRunComputer3() {
		double[] arr1 = { 1, 2, 3 };
		double[] arr2 = { 1, 2, 3 };
		double[] arr3 = { 1, 2, 3 };
		double[] out = new double[3];
		ops.run("test.Computer3", arr1, arr2, arr3, out);
		Assert.assertArrayEquals(new double[] { 3, 6, 9 }, out, 0);
	}

	@OpField(names = "test.Computer4")
	@Parameter(key = "arr1")
	@Parameter(key = "arr2")
	@Parameter(key = "arr3")
	@Parameter(key = "arr4")
	@Parameter(key = "out", type = ItemIO.BOTH)
	public static final Computer4<double[], double[], double[], double[], double[]> testComputer4 = (arr1, arr2, arr3,
			arr4, out) -> {
		for (int i = 0; i < arr1.length; i++) {
			out[i] = arr1[i] + arr2[i] + arr3[i] + arr4[i];
		}
	};

	@Test
	public void testRunComputer4() {
		double[] arr1 = { 1, 2, 3 };
		double[] arr2 = { 1, 2, 3 };
		double[] arr3 = { 1, 2, 3 };
		double[] arr4 = { 1, 2, 3 };
		double[] out = new double[3];
		ops.run("test.Computer4", arr1, arr2, arr3, arr4, out);
		Assert.assertArrayEquals(new double[] { 4, 8, 12 }, out, 0);
	}

	@OpField(names = "test.Computer5")
	@Parameter(key = "arr1")
	@Parameter(key = "arr2")
	@Parameter(key = "arr3")
	@Parameter(key = "arr4")
	@Parameter(key = "arr5")
	@Parameter(key = "out", type = ItemIO.BOTH)
	public static final Computer5<double[], double[], double[], double[], double[], double[]> testComputer5 = (arr1,
			arr2, arr3, arr4, arr5, out) -> {
		for (int i = 0; i < arr1.length; i++) {
			out[i] = arr1[i] + arr2[i] + arr3[i] + arr4[i] + arr5[i];
		}
	};

	@Test
	public void testRunComputer5() {
		double[] arr1 = { 1, 2, 3 };
		double[] arr2 = { 1, 2, 3 };
		double[] arr3 = { 1, 2, 3 };
		double[] arr4 = { 1, 2, 3 };
		double[] arr5 = { 1, 2, 3 };
		double[] out = new double[3];
		ops.run("test.Computer5", arr1, arr2, arr3, arr4, arr5, out);
		Assert.assertArrayEquals(new double[] { 5, 10, 15 }, out, 0);
	}

	// INPLACES

	@OpField(names = "test.Inplace")
	@Parameter(key = "io", type = ItemIO.BOTH)
	public static final Inplace<double[]> testInplace = (io) -> {
		for (int i = 0; i < io.length; i++)
			io[i] *= 2;
	};

	@Test
	public void testRunInplace() {
		double[] io = { 1, 2, 3 };
		ops.run("test.Inplace", io);
		Assert.assertArrayEquals(new double[] { 2, 4, 6 }, io, 0);
	}

	@OpField(names = "test.BiInplaceFirst")
	@Parameter(key = "io", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	public static final BiInplaceFirst<double[], double[]> testBiInplaceFirst = (io, in2) -> {
		for (int i = 0; i < io.length; i++)
			io[i] += in2[i];
	};

	@Test
	public void testRunBiInplaceFirst() {
		double[] io = { 1, 2, 3 };
		double[] in2 = { 1, 2, 3 };
		ops.run("test.BiInplaceFirst", io, in2);
		Assert.assertArrayEquals(new double[] { 2, 4, 6 }, io, 0);
	}

	@OpField(names = "test.BiInplaceSecond")
	@Parameter(key = "in1")
	@Parameter(key = "io", type = ItemIO.BOTH)
	public static final BiInplaceSecond<double[], double[]> testBiInplaceSecond = (in1, io) -> {
		for (int i = 0; i < io.length; i++)
			io[i] *= in1[i];
	};

	@Test
	public void testRunBiInplaceSecond() {
		double[] in1 = { 1, 2, 3 };
		double[] io = { 1, 2, 3 };
		ops.run("test.BiInplaceSecond", in1, io);
		Assert.assertArrayEquals(new double[] { 1, 4, 9 }, io, 0);
	}

	@OpField(names = "test.Inplace3First")
	@Parameter(key = "io", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "in3")
	public static final Inplace3First<double[], double[], double[]> testInplace3First = (io, in2, in3) -> {
		for (int i = 0; i < io.length; i++) {
			io[i] += in2[i];
			io[i] += in3[i];
		}
	};

	@Test
	public void testRunInplace3First() {
		double[] io = { 1, 2, 3 };
		double[] in2 = { 1, 2, 3 };
		double[] in3 = { 1, 2, 3 };
		ops.run("test.Inplace3First", io, in2, in3);
		Assert.assertArrayEquals(new double[] { 3, 6, 9 }, io, 0);
	}

	@OpField(names = "test.Inplace4First")
	@Parameter(key = "io", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "in3")
	@Parameter(key = "in4")
	public static final Inplace4First<double[], double[], double[], double[]> testInplace4First = (io, in2, in3,
			in4) -> {
		for (int i = 0; i < io.length; i++) {
			io[i] += in2[i];
			io[i] += in3[i];
			io[i] += in4[i];
		}
	};

	@Test
	public void testRunInplace4First() {
		double[] io = { 1, 2, 3 };
		double[] in2 = { 1, 2, 3 };
		double[] in3 = { 1, 2, 3 };
		double[] in4 = { 1, 2, 3 };
		ops.run("test.Inplace4First", io, in2, in3, in4);
		Assert.assertArrayEquals(new double[] { 4, 8, 12 }, io, 0);
	}
	
	@OpField(names = "test.Inplace5First")
	@Parameter(key = "io", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "in3")
	@Parameter(key = "in4")
	@Parameter(key = "in5")
	public static final Inplace5First<double[], double[], double[], double[], double[]> testInplace5First = (io, in2, in3,
			in4, in5) -> {
		for (int i = 0; i < io.length; i++) {
			io[i] += in2[i];
			io[i] += in3[i];
			io[i] += in4[i];
			io[i] += in5[i];
		}
	};

	@Test
	public void testRunInplace5First() {
		double[] io = { 1, 2, 3 };
		double[] in2 = { 1, 2, 3 };
		double[] in3 = { 1, 2, 3 };
		double[] in4 = { 1, 2, 3 };
		double[] in5 = { 1, 2, 3 };
		ops.run("test.Inplace5First", io, in2, in3, in4, in5);
		Assert.assertArrayEquals(new double[] { 5, 10, 15 }, io, 0);
	}

}
