package org.scijava.ops.math;

import com.google.common.collect.Streams;

import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.scijava.core.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.inplace.BiInplaceFirst;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public final class Add<M extends Number, I extends Iterable<M>> {

	public static final String NAMES = MathOps.ADD;

	// --------- Functions ---------

	@OpField(names = NAMES)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BiFunction<Double, Double, Double> MathAddDoublesFunction = (t, u) -> t + u;

	@OpField(priority = Priority.HIGH, names = NAMES)
	@Parameter(key = "iter1")
	@Parameter(key = "iter2")
	@Parameter(key = "resultArray", type = ItemIO.OUTPUT)
	public final BiFunction<I, I, Iterable<Double>> MathPointwiseAddIterablesFunction = (i1, i2) -> {
		Stream<? extends Number> s1 = Streams.stream((Iterable<? extends Number>) i1);
		Stream<? extends Number> s2 = Streams.stream((Iterable<? extends Number>) i2);
		return () -> Streams.zip(s1, s2, (e1, e2) -> e1.doubleValue() + e2.doubleValue()).iterator();
	};

	// --------- Computers ---------

	@OpField(names = NAMES)
	@Parameter(key = "integer1")
	@Parameter(key = "integer2")
	@Parameter(key = "resultInteger", type = ItemIO.OUTPUT)
	public static final BiFunction<BigInteger, BigInteger, BigInteger> MathAddBigIntegersComputer = (t, u) -> t.add(u);

	@OpField(names = NAMES)
	@Parameter(key = "array1")
	@Parameter(key = "array2")
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static final BiComputer<double[], double[], double[]> MathPointwiseAddDoubleArraysComputer = (in1, in2,
			out) -> {
		for (int i = 0; i < out.length; i++) {
			out[i] = in1[i] + in2[i];
		}
	};

	// --------- Inplaces ---------

	@OpField(names = NAMES)
	@Parameter(key = "arrayIO", type = ItemIO.BOTH)
	@Parameter(key = "array1")
	public static final BiInplaceFirst<double[], double[]> MathPointwiseAddDoubleArraysInplace1 = (io, in2) -> {
		for (int i = 0; i < io.length; i++) {
			io[i] += in2[i];
		}
	};

	@OpField(names = NAMES)
	@Parameter(key = "val1")
	@Parameter(key = "val2")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final BiFunction<M, M, Double> MathAddNumbersFunction = (in1, in2) -> in1.doubleValue() + in2.doubleValue();

	@OpField(names = NAMES)
	@Parameter(key = "iterable")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<Iterable<M>, Double> MathReductionAdd = (iterable) -> {
		return StreamSupport.stream(iterable.spliterator(), false).mapToDouble(Number::doubleValue).sum();
	};

}
