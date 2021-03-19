
package org.scijava.ops;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

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
	public static class LiftSqrt implements Computers.Arity1<double[], double[]> {

		@Override
		public void compute(double[] in, double[] out) {
			for (int i = 0; i < in.length; i++) {
				out[i] = Math.sqrt(in[i]);
			}
		}
	}

	// AdaptersTest

	@Plugin(type = Op.class, name = "test.adaptersC")
	public static class testAddTwoArraysComputer implements Computers.Arity2<double[], double[], double[]> {
		@Override
		public void compute(double[] arr1, double[] arr2, double[] out) {
			for (int i = 0; i < out.length; i++)
				out[i] = arr1[i] + arr2[i];
		}
	}

	@Plugin(type = Op.class, name = "test.adaptersF")
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
	public static class liftFunction implements Function<Double, Double> {
		@Override
		public Double apply(Double in) {
			return in + 1;
		}
	}

	@Plugin(type = Op.class, name = "test.liftComputer")
	public static class liftComputer implements Computers.Arity1<double[], double[]> {
		@Override
		public void compute(double[] in, double[] out) {
			for (int i = 0; i < in.length; i++)
				out[i] = in[i] + 1;
		}
	}

	// -- TODO: OpDependencies -- //
}
