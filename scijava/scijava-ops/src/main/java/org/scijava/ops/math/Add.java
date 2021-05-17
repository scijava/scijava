package org.scijava.ops.math;

import com.google.common.collect.Streams;

import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public final class Add<M extends Number, I extends Iterable<M>> {

	public static final String NAMES = MathOps.ADD;

	// --------- Functions ---------

	@OpField(names = NAMES) // vars = "number1, number2, result"
	public static final BiFunction<Double, Double, Double> MathAddDoublesFunction = (t, u) -> t + u;

	@OpField(priority = Priority.HIGH, names = NAMES) //  vars = "iter1, iter2, resultArray"
	public final BiFunction<I, I, Iterable<Double>> MathPointwiseAddIterablesFunction = (i1, i2) -> {
		Stream<? extends Number> s1 = Streams.stream((Iterable<? extends Number>) i1);
		Stream<? extends Number> s2 = Streams.stream((Iterable<? extends Number>) i2);
		return () -> Streams.zip(s1, s2, (e1, e2) -> e1.doubleValue() + e2.doubleValue()).iterator();
	};

	// --------- Computers ---------

	@OpField(names = NAMES) // vars = "integer1, integer2, resultInteger"
	public static final BiFunction<BigInteger, BigInteger, BigInteger> MathAddBigIntegersComputer = (t, u) -> t.add(u);

	@OpField(names = NAMES) // vars = "array1, array2, resultArray"
	public static final Computers.Arity2<double[], double[], double[]> MathPointwiseAddDoubleArraysComputer = (in1, in2, out) -> {
		for (int i = 0; i < out.length; i++) {
			out[i] = in1[i] + in2[i];
		}
	};

	// --------- Inplaces ---------

	@OpField(names = NAMES) // vars = "arrayIO, array1"
	public static final Inplaces.Arity2_1<double[], double[]> MathPointwiseAddDoubleArraysInplace1 = (io, in2) -> {
		for (int i = 0; i < io.length; i++) {
			io[i] += in2[i];
		}
	};

	@OpField(names = NAMES) // vars = "val1, val2, output"
	public final BiFunction<M, M, Double> MathAddNumbersFunction = (in1, in2) -> in1.doubleValue() + in2.doubleValue();

	@OpField(names = NAMES) // vars = "iterable, result"
	public final Function<Iterable<M>, Double> MathReductionAdd = (iterable) -> {
		return StreamSupport.stream(iterable.spliterator(), false).mapToDouble(Number::doubleValue).sum();
	};

}
