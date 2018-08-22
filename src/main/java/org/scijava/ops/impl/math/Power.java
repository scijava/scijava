package org.scijava.ops.impl.math;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.Computer;
import org.scijava.ops.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class Power {

	public interface MathPowerOp extends Op {
	}

	@Plugin(type = MathPowerOp.class)
	@Parameter(key = "number")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathPowerDoublConstantFunction implements MathPowerOp, Function<Double, Double> {
		
		@Parameter
		private Double exponent;
		
		@Override
		public Double apply(Double t) {
			return Math.pow(t, exponent);
		}
	}
	
	@Plugin(type = MathPowerOp.class)
	@Parameter(key = "number")
	@Parameter(key = "exponent")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathPowerDoublFunction implements MathPowerOp, BiFunction<Double, Double, Double> {
		@Override
		public Double apply(Double t, Double exp) {
			return Math.pow(t, exp);
		}
	}

	@Plugin(type = MathPowerOp.class)
	@Parameter(key = "array")
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static class MathPointwisePowerDoubleArrayComputer implements MathPowerOp, Computer<double[], double[]> {
		
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
