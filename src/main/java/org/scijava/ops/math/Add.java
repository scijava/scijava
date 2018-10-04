package org.scijava.ops.math;

import com.google.common.collect.Streams;

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

public class Add {
	
	public static final String NAMES = MathOps.ADD;
	
	// --------- Functions ---------
	
	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathAddDoublesFunction implements BiFunction<Double, Double, Double> {
		@Override
		public Double apply(Double t, Double u) {
			return t + u;
		}
	}

	// This Op is not needed anymore, can be handled via auto op transformation
//	@Plugin(type = Op.class, priority = Priority.HIGH, name = NAMES)
//	@Parameter(key = "array1")
//	@Parameter(key = "array2")
//	@Parameter(key = "resultArray", type = ItemIO.OUTPUT)
//	public static class MathPointwiseAddDoubleArraysFunction
//			implements BiFunction<double[], double[], double[]> {
//		@Override
//		public double[] apply(double[] arr1, double[] arr2) {
//			Stream<Double> s1 = Arrays.stream(arr1).boxed();
//			Stream<Double> s2 = Arrays.stream(arr2).boxed();
//			return Streams.zip(s1, s2, (num1, num2) -> num1 + num2).mapToDouble(Double::doubleValue).toArray();
//		}
//	}
	
	@Plugin(type = Op.class, priority = Priority.HIGH, name = NAMES)
	@Parameter(key = "iter1")
	@Parameter(key = "iter2")
	@Parameter(key = "resultArray", type = ItemIO.OUTPUT)
	public static class MathPointwiseAddIterablesFunction<M extends Number, I extends Iterable<M>>
			implements BiFunction<I, I, Iterable<Double>> {
		@Override
		public Iterable<Double> apply(I i1, I i2) {
			Stream<? extends Number> s1 = Streams.stream((Iterable<? extends Number>) i1);
			Stream<? extends Number> s2 = Streams.stream((Iterable<? extends Number>) i2);
			return () -> Streams.zip(s1, s2, (e1, e2) -> e1.doubleValue() + e2.doubleValue()).iterator();
		}
	}

	// --------- Computers ---------
			
	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "integer1")
	@Parameter(key = "integer2")
	@Parameter(key = "resultInteger", type = ItemIO.OUTPUT)
	public static class MathAddBigIntegersComputer implements BiFunction<BigInteger, BigInteger, BigInteger> {
		@Override
		public BigInteger apply(BigInteger t, BigInteger u) {
			return t.add(u);
		}
	}

	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "array1")
	@Parameter(key = "array2")
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static class MathPointwiseAddDoubleArraysComputer implements BiComputer<double[], double[], double[]> {
		@Override
		public void compute(double[] in1, double[] in2, double[] out) {
			for (int i = 0; i < out.length; i++) {
				out[i] = in1[i] + in2[i];
			}
		}
	}
	
	// --------- Inplaces ---------
	
	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "arrayIO", type = ItemIO.BOTH)
	@Parameter(key = "array1")
	public static class MathPointwiseAddDoubleArraysInplace1 implements BiInplace1<double[], double[]> {
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
