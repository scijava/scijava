package org.scijava.ops;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
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

	// OpsTest

	@Plugin(type = Op.class, name = "test.secondaryInputsFunction")
	@Parameter(key = "number")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathPowerDoublConstantFunction implements Function<Double, Double> {

		@Parameter(type = ItemIO.INPUT)
		private double exponent;

		@Override
		public Double apply(Double t) {
			return Math.pow(t, exponent);
		}
	}

	@Plugin(type = Op.class, name = "test.secondaryInputsComputer")
	@Parameter(key = "array")
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static class MathPointwisePowerDoubleArrayComputer implements Computer<double[], double[]> {

		@Parameter(type = ItemIO.INPUT)
		private Double exponent;

		@Override
		public void compute(double[] in1, double[] out) {
			for (int i = 0; i < out.length; i++) {
				out[i] = Math.pow(in1[i], exponent);
			}
		}
	}

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

	// -- TODO: OpDependencies -- //
}
