package org.scijava.ops.math;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.Computer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class Power {

	public static final String NAMES = MathOps.POW;

	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "number")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathPowerDoublConstantFunction implements Function<Double, Double> {

		@Parameter
		private double exponent;

		@Override
		public Double apply(Double t) {
			return Math.pow(t, exponent);
		}
	}

	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "number")
	@Parameter(key = "exponent")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathPowerDoubleFunction implements BiFunction<Double, Double, Double> {
		@Override
		public Double apply(Double t, Double exp) {
			return Math.pow(t, exp);
		}
	}

	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "array")
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static class MathPointwisePowerDoubleArrayComputer implements Computer<double[], double[]> {

		@Parameter
		private Double exponent;

		@Override
		public void compute(double[] in1, double[] out) {
			for (int i = 0; i < out.length; i++) {
				out[i] = Math.pow(in1[i], exponent);
			}
		}
	}
}
