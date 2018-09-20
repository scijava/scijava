package org.scijava.ops.math;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.scijava.core.Priority;
import org.scijava.ops.core.BiComputer;
import org.scijava.ops.core.BiInplace1;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

import com.google.common.collect.Streams;

public class Add {

	public interface MathAddOp extends Op {
		String NAME = "math.add";
		String ALIASES = "math.sum";
	}

	// --------- Functions ---------
	
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

	@Plugin(type = MathAddOp.class, priority = Priority.HIGH)
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
	
	@Plugin(type = MathAddOp.class, priority = Priority.HIGH)
	@Parameter(key = "iter1")
	@Parameter(key = "iter2")
	@Parameter(key = "resultArray", type = ItemIO.OUTPUT)
	public static class MathPointwiseAddIterablesFunction<M extends Number, I extends Iterable<M>>
			implements MathAddOp, BiFunction<I, I, Iterable<Double>> {
		@Override
		public Iterable<Double> apply(I i1, I i2) {
			Stream<? extends Number> s1 = Streams.stream((Iterable<? extends Number>) i1);
			Stream<? extends Number> s2 = Streams.stream((Iterable<? extends Number>) i2);
			return () -> Streams.zip(s1, s2, (e1, e2) -> e1.doubleValue() + e2.doubleValue()).iterator();
		}
	}

	// --------- Computers ---------
			
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
	
	// --------- Inplaces ---------
	
	@Plugin(type = MathAddOp.class)
	@Parameter(key = "arrayIO", type = ItemIO.BOTH)
	@Parameter(key = "array1")
	public static class MathPointwiseAddDoubleArraysInplace1 implements MathAddOp, BiInplace1<double[], double[]> {
		@Override
		public void mutate(double[] io, double[] in2) {
			for (int i = 0; i < io.length; i++ ) {
				io[i] += in2[i];
			}
		}
	}
	
	// @Op
	// @Parameter(key = "number1")
	// @Parameter(key = "number2")
	// @Parameter(key = "result", type = ItemIO.OUTPUT)
	// public static final BiFunction<Double, Double, Double> mathAddDoublesFunction = (d1, d2) -> d1 + d2;
}
