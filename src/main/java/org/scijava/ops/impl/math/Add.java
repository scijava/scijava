package org.scijava.ops.impl.math;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.scijava.ops.BiComputer;
import org.scijava.ops.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

import com.google.common.collect.Streams;

public class Add {

	public interface MathAddOp extends Op {
	}

	@Plugin(type = MathAddOp.class)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathAddDoublesFunction implements MathAddOp, BiFunction<Double, Double, Double> {
		@Override
		public Double apply(Double t, Double u) {
			return t + u;
		}
	}

	@Plugin(type = MathAddOp.class)
	@Parameter(key = "array1")
	@Parameter(key = "array2")
	@Parameter(key = "resultArray", type = ItemIO.OUTPUT)
	public static class MathPointwiseAddDoubleArraysFunction
			implements MathAddOp, BiFunction<double[], double[], double[]> {
		@Override
		public double[] apply(double[] arr1, double[] arr2) {
			Stream<Double> s1 = Arrays.stream(arr1).boxed();
			Stream<Double> s2 = Arrays.stream(arr2).boxed();
			return Streams.zip(s1, s2, (num1, num2) -> num1 + num2).mapToDouble(Double::doubleValue).toArray();
		}
	}

	@Plugin(type = MathAddOp.class)
	@Parameter(key = "integer1")
	@Parameter(key = "integer2")
	@Parameter(key = "resultInteger", type = ItemIO.OUTPUT)
	public static class MathAddBigIntegersComputer implements MathAddOp, BiFunction<BigInteger, BigInteger, BigInteger> {
		@Override
		public BigInteger apply(BigInteger t, BigInteger u) {
			return t.add(u);
		}
	}

	@Plugin(type = MathAddOp.class)
	@Parameter(key = "array1")
	@Parameter(key = "array2")
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static class MathPointwiseAddDoubleArraysComputer implements MathAddOp, BiComputer<double[], double[], double[]> {
		@Override
		public void compute(double[] in1, double[] in2, double[] out) {
			for (int i = 0; i < out.length; i++) {
				out[i] = in1[i] + in2[i];
			}
		}
	}
	
	// @Op
	// @Parameter(key = "number1")
	// @Parameter(key = "number2")
	// @Parameter(key = "result", type = ItemIO.OUTPUT)
	// public static final BiFunction<Double, Double, Double> mathAddDoublesFunction = (d1, d2) -> d1 + d2;
}