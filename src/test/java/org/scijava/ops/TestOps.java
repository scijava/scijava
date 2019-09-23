
package org.scijava.ops;

import static org.scijava.ops.TestUtils.argsToString;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Inplaces;
import org.scijava.ops.function.Producer;
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
	@Parameter(key = "out", itemIO = ItemIO.BOTH)
	public static class LiftSqrt implements Computers.Arity1<double[], double[]> {

		@Override
		public void compute(double[] in, double[] out) {
			for (int i = 0; i < in.length; i++) {
				out[i] = Math.sqrt(in[i]);
			}
		}
	}

	// AdaptersTest

	@Plugin(type = Op.class, name = "test.adapters")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "out", itemIO = ItemIO.BOTH)
	public static class testAddTwoArraysComputer implements Computers.Arity2<double[], double[], double[]> {
		@Override
		public void compute(double[] arr1, double[] arr2, double[] out) {
			for (int i = 0; i < out.length; i++)
				out[i] = arr1[i] + arr2[i];
		}
	}

	@Plugin(type = Op.class, name = "test.adapters")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "out", itemIO = ItemIO.OUTPUT)
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
	@Parameter(key = "in")
	@Parameter(key = "out", itemIO = ItemIO.OUTPUT)
	public static class liftFunction implements Function<Double, Double> {
		@Override
		public Double apply(Double in) {
			return in + 1;
		}
	}

	@Plugin(type = Op.class, name = "test.liftComputer")
	@Parameter(key = "in")
	@Parameter(key = "out", itemIO = ItemIO.BOTH)
	public static class liftComputer implements Computers.Arity1<double[], double[]> {
		@Override
		public void compute(double[] in, double[] out) {
			for (int i = 0; i < in.length; i++)
				out[i] = in[i] + 1;
		}
	}

	// ComputerToFunctionTransformTest

	@Plugin(type = Op.class, name = "test.computer1ToFunction1TestOp")
	@Parameter(key = "in")
	@Parameter(key = "out", itemIO = ItemIO.BOTH)
	public static class Computer1ToFunctionTestOp implements Computers.Arity1<Byte, AtomicReference<String>> {

		@Override
		public void compute(Byte in1, @Mutable AtomicReference<String> out) {
			out.set(argsToString(in1));
		}
	}

	@Plugin(type = Op.class, name = "test.computer2ToFunction2TestOp")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "out", itemIO = ItemIO.BOTH)
	public static class Computer2Function2TestOp implements Computers.Arity2<Byte, Double, AtomicReference<String>> {

		@Override
		public void compute(Byte in1, Double in2, @Mutable AtomicReference<String> out) {
			out.set(argsToString(in1, in2));
		}
	}

	@Plugin(type = Op.class, name = "test.computer3ToFunction3TestOp")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "in3")
	@Parameter(key = "out", itemIO = ItemIO.BOTH)
	public static class Computer3ToFunction3TestOp implements Computers.Arity3<Byte, Double, Float, AtomicReference<String>> {

		@Override
		public void compute(Byte in1, Double in2, Float in3, @Mutable AtomicReference<String> out) {
			out.set(argsToString(in1, in2, in3));
		}
	}

	@Plugin(type = Op.class, name = "create")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public static class AtomicStringReferenceCreator implements Producer<AtomicReference<String>> {

		@Override
		public AtomicReference<String> create() {
			return new AtomicReference<>();
		}
	}

	// InplaceToFunctionTransformTest

	@Plugin(type = Op.class, name = "test.inplace1ToFunction1TestOp")
	@Parameter(key = "io", itemIO = ItemIO.BOTH)
	public static class InplaceToFunctionTestOp implements Inplaces.Arity1<AtomicReference<String>> {

		@Override
		public void mutate(@Mutable AtomicReference<String> in1) {
			in1.set(in1.get() + " inplace");
		}
	}

	@Plugin(type = Op.class, name = "test.inplace2_1ToFunction2TestOp")
	@Parameter(key = "io", itemIO = ItemIO.BOTH)
	@Parameter(key = "in2")
	public static class Inplace2_1ToFunction2TestOp implements Inplaces.Arity2_1<AtomicReference<String>, Byte> {

		@Override
		public void mutate(@Mutable AtomicReference<String> io, Byte in2) {
			io.set(argsToString(io, in2));
		}
	}

	@Plugin(type = Op.class, name = "test.inplace2_2ToFunction2TestOp")
	@Parameter(key = "in1")
	@Parameter(key = "io", itemIO = ItemIO.BOTH)
	public static class Inplace2_2ToFunction2TestOp implements Inplaces.Arity2_2<Byte, AtomicReference<String>> {

		@Override
		public void mutate(Byte in1, @Mutable AtomicReference<String> io) {
			io.set(argsToString(in1, io));
		}
	}

	@Plugin(type = Op.class, name = "test.inplace3_1ToFunction3TestOp")
	@Parameter(key = "io", itemIO = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "in3")
	public static class Inplace3_1ToFunction3TestOp implements Inplaces.Arity3_1<AtomicReference<String>, Byte, Double> {

		@Override
		public void mutate(@Mutable AtomicReference<String> io, Byte in2, Double in3) {
			io.set(argsToString(io, in2, in3));
		}
	}

	@Plugin(type = Op.class, name = "test.inplace3_2ToFunction3TestOp")
	@Parameter(key = "in1")
	@Parameter(key = "io", itemIO = ItemIO.BOTH)
	@Parameter(key = "in3")
	public static class Inplace3_2ToFunction3TestOp implements Inplaces.Arity3_2<Byte, AtomicReference<String>, Double> {

		@Override
		public void mutate(Byte in1, @Mutable AtomicReference<String> io, Double in3) {
			io.set(argsToString(in1, io, in3));
		}
	}

	@Plugin(type = Op.class, name = "test.inplace3_3ToFunction3TestOp")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "io", itemIO = ItemIO.BOTH)
	public static class Inplace3_3ToFunction3TestOp implements Inplaces.Arity3_3<Byte, Double, AtomicReference<String>> {

		@Override
		public void mutate(Byte in1, Double in2, @Mutable AtomicReference<String> io) {
			io.set(argsToString(in1, in2, io));
		}
	}

	// -- TODO: OpDependencies -- //
}
