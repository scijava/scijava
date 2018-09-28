package org.scijava.ops.math;

import java.util.function.Function;
import java.util.stream.IntStream;

import org.scijava.ops.core.Computer;
import org.scijava.ops.core.Inplace;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class Sqrt {

	public static final String NAMES = MathOps.SQRT;
	
	// --------- Functions ---------

	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "number1")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathSqrtDoubleFunction implements Function<Double, Double> {
		@Override
		public Double apply(Double t) {
			return Math.sqrt(t);
		}
	}
	
	// --------- Computers ---------

	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "array1")
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static class MathPointwiseSqrtDoubleArrayComputer implements Computer<double[], double[]> {
		@Override
		public void compute(double[] in1, double[] out) {
			for (int i = 0; i < out.length; i++) {
				out[i] = Math.sqrt(in1[i]);
			}
		}
	}
	
	// --------- Inplaces ---------
	
	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "arrayIO", type = ItemIO.BOTH)
	public static class MathPointwiseSqrtDoubleArrayInplace implements Inplace<double[]> {
		@Override
		public void mutate(double[] in1) {
			IntStream.range(0, in1.length).forEach(index -> {
				in1[index] = Math.sqrt(in1[index]);
			});
		}
	}
}
