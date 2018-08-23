package org.scijava.ops.impl.math;

import java.util.function.Function;

import org.scijava.ops.Computer;
import org.scijava.ops.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class Sqrt {

	public interface MathSqrtOp extends Op {
	}

	@Plugin(type = MathSqrtOp.class)
	@Parameter(key = "number1")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathSqrtDoubleFunction implements MathSqrtOp, Function<Double, Double> {
		@Override
		public Double apply(Double t) {
			return Math.sqrt(t);
		}
	}

	@Plugin(type = MathSqrtOp.class)
	@Parameter(key = "array1")
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static class MathPointwiseSqrtDoubleArrayComputer implements MathSqrtOp, Computer<double[], double[]> {
		@Override
		public void compute(double[] in1, double[] out) {
			for (int i = 0; i < out.length; i++) {
				out[i] = Math.sqrt(in1[i]);
			}
		}
	}
}