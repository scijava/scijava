
package org.scijava.ops;

import static org.scijava.ops.TestUtils.argsToString;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.function.Source;
import org.scijava.ops.core.inplace.BiInplaceFirst;
import org.scijava.ops.core.inplace.BiInplaceSecond;
import org.scijava.ops.core.inplace.Inplace;
import org.scijava.ops.core.inplace.Inplace3First;
import org.scijava.ops.core.inplace.Inplace3Second;
import org.scijava.ops.core.inplace.Inplace4First;
import org.scijava.ops.core.inplace.Inplace5First;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * This class contains various ops used in various tests used to check framework
 * functionality. These Ops SHOULD NEVER be changed or used outside of the tests
 * that rely on them, however all should feel free to add more tests to this
 * class as needed.
 * 
 * @author Gabriel Selzer
 *
 */
public class TestOps {

	// -- Op Classes -- //

	// AutoTransformTest

	@Plugin(type = Op.class, name = "test.liftSqrt")
	@Parameter(key = "in")
	@Parameter(key = "out", type = ItemIO.BOTH)
	public static class LiftSqrt implements Computer<double[], double[]> {

		@Override
		public void compute(double[] in, double[] out) {
			for (int i = 0; i < in.length; i++) {
				out[i] = Math.sqrt(in[i]);
			}
		}
	}

	// AdaptersTest

	@Plugin(type = Op.class, name = "test.adapters")
	@Parameter(key = "arr1")
	@Parameter(key = "arr2")
	@Parameter(key = "arrout", type = ItemIO.BOTH)
	public static class testAddTwoArraysComputer implements BiComputer<double[], double[], double[]> {
		@Override
		public void compute(double[] arr1, double[] arr2, double[] out) {
			for (int i = 0; i < out.length; i++)
				out[i] = arr1[i] + arr2[i];
		}
	}

	@Plugin(type = Op.class, name = "test.adapters")
	@Parameter(key = "arr1")
	@Parameter(key = "arr2")
	@Parameter(key = "arrout", type = ItemIO.OUTPUT)
	public static class testAddTwoArraysFunction implements BiFunction<double[], double[], double[]> {
		@Override
		public double[] apply(double[] arr1, double[] arr2) {
			double[] out = new double[arr1.length];
			for (int i = 0; i < out.length; i++)
				out[i] = arr1[i] + arr2[i];
			return out;
		}
	}

	// LiftTest

	@Plugin(type = Op.class, name = "test.liftFunction")
	@Parameter(key = "input")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public static class liftFunction implements Function<Double, Double> {
		@Override
		public Double apply(Double in) {
			return in + 1;
		}
	}

	@Plugin(type = Op.class, name = "test.liftComputer")
	@Parameter(key = "input")
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class liftComputer implements Computer<double[], double[]> {
		@Override
		public void compute(double[] in, double[] out) {
			for (int i = 0; i < in.length; i++)
				out[i] = in[i] + 1;
		}
	}

	// ComputerToFunctionTransformTest

	@Plugin(type = Op.class, name = "test.computerToFunctionTestOp")
	@Parameter(key = "input")
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class ComputerToFunctionTestOp implements Computer<Byte, AtomicReference<String>> {

		@Override
		public void compute(Byte in1, @Mutable AtomicReference<String> out) {
			out.set(argsToString(in1));
		}
	}

	@Plugin(type = Op.class, name = "test.biComputerToBiFunctionTestOp")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class BiComputerToBiFunctionTestOp implements BiComputer<Byte, Double, AtomicReference<String>> {

		@Override
		public void compute(Byte in1, Double in2, @Mutable AtomicReference<String> out) {
			out.set(argsToString(in1, in2));
		}
	}

	@Plugin(type = Op.class, name = "test.computer3ToFunction3TestOp")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "input3")
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class Computer3ToFunction3TestOp implements Computer3<Byte, Double, Float, AtomicReference<String>> {

		@Override
		public void compute(Byte in1, Double in2, Float in3, @Mutable AtomicReference<String> out) {
			out.set(argsToString(in1, in2, in3));
		}
	}

	@Plugin(type = Op.class, name = "test.computer4ToFunction4TestOp")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "input3")
	@Parameter(key = "input4")
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class Computer4ToFunction4TestOp implements
		Computer4<Byte, Double, Float, Integer, AtomicReference<String>>
	{

		@Override
		public void compute(Byte in1, Double in2, Float in3, Integer in4, @Mutable AtomicReference<String> out) {
			out.set(argsToString(in1, in2, in3, in4));
		}
	}

	@Plugin(type = Op.class, name = "test.computer5ToFunction5TestOp")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "input3")
	@Parameter(key = "input4")
	@Parameter(key = "input5")
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class Computer5ToFunction5TestOp implements
		Computer5<Byte, Double, Float, Integer, Long, AtomicReference<String>>
	{

		@Override
		public void compute(Byte in1, Double in2, Float in3, Integer in4, Long in5, @Mutable AtomicReference<String> out) {
			out.set(argsToString(in1, in2, in3, in4, in5));
		}
	}

	@Plugin(type = Op.class, name = "create")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public static class AtomicStringReferenceCreator implements Source<AtomicReference<String>> {

		@Override
		public AtomicReference<String> create() {
			return new AtomicReference<>();
		}
	}

	// InplaceToFunctionTransformTest

	@Plugin(type = Op.class, name = "test.inplaceToFunctionTestOp")
	@Parameter(key = "inout", type = ItemIO.BOTH)
	public static class InplaceToFunctionTestOp implements Inplace<AtomicReference<String>> {

		@Override
		public void mutate(@Mutable AtomicReference<String> in1) {
			in1.set(in1.get() + " inplace");
		}
	}

	@Plugin(type = Op.class, name = "test.biInplaceFirstToBiFunctionTestOp")
	@Parameter(key = "inout", type = ItemIO.BOTH)
	@Parameter(key = "input2")
	public static class BiInplaceFirstToBiFunctionTestOp implements BiInplaceFirst<AtomicReference<String>, Byte> {

		@Override
		public void mutate(@Mutable AtomicReference<String> io, Byte in2) {
			io.set(argsToString(io, in2));
		}
	}

	@Plugin(type = Op.class, name = "test.biInplaceSecondToBiFunctionTestOp")
	@Parameter(key = "input1")
	@Parameter(key = "inout", type = ItemIO.BOTH)
	public static class BiInplaceSecondToBiFunctionTestOp implements BiInplaceSecond<Byte, AtomicReference<String>> {

		@Override
		public void mutate(Byte in1, @Mutable AtomicReference<String> io) {
			io.set(argsToString(in1, io));
		}
	}

	@Plugin(type = Op.class, name = "test.inplace3FirstToFunction3TestOp")
	@Parameter(key = "inout", type = ItemIO.BOTH)
	@Parameter(key = "input2")
	@Parameter(key = "input3")
	public static class Inplace3FirstToFunction3TestOp implements Inplace3First<AtomicReference<String>, Byte, Double> {

		@Override
		public void mutate(@Mutable AtomicReference<String> io, Byte in2, Double in3) {
			io.set(argsToString(io, in2, in3));
		}
	}

	@Plugin(type = Op.class, name = "test.inplace3SecondToFunction3TestOp")
	@Parameter(key = "input1")
	@Parameter(key = "inout", type = ItemIO.BOTH)
	@Parameter(key = "input3")
	public static class Inplace3SecondToFunction3TestOp implements Inplace3Second<Byte, AtomicReference<String>, Double> {

		@Override
		public void mutate(Byte in1, @Mutable AtomicReference<String> io, Double in3) {
			io.set(argsToString(in1, io, in3));
		}
	}

	@Plugin(type = Op.class, name = "test.inplace4FirstToFunction4TestOp")
	@Parameter(key = "inout", type = ItemIO.BOTH)
	@Parameter(key = "input2")
	@Parameter(key = "input3")
	@Parameter(key = "input4")
	public static class Inplace4FirstToFunction4TestOp implements
		Inplace4First<AtomicReference<String>, Byte, Double, Float>
	{

		@Override
		public void mutate(@Mutable AtomicReference<String> io, Byte in2, Double in3, Float in4) {
			io.set(argsToString(io, in2, in3, in4));
		}
	}

	@Plugin(type = Op.class, name = "test.inplace5FirstToFunction5TestOp")
	@Parameter(key = "inout", type = ItemIO.BOTH)
	@Parameter(key = "input2")
	@Parameter(key = "input3")
	@Parameter(key = "input4")
	@Parameter(key = "input5")
	public static class Inplace5FirstToFunction5TestOp implements
		Inplace5First<AtomicReference<String>, Byte, Double, Float, Integer>
	{

		@Override
		public void mutate(@Mutable AtomicReference<String> io, Byte in2, Double in3, Float in4, Integer in5) {
			io.set(argsToString(io, in2, in3, in4, in5));
		}
	}

	// -- TODO: OpDependencies -- //
}
