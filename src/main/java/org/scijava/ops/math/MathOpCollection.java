package org.scijava.ops.math;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.scijava.core.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

// TODO: Derive parameters from the one abstract method of the functional interface in question.
// This also will tell us the number of inputs vs. outputs. But!
// It will NOT tell us if an argument is "BOTH".
// And it will NOT tell us the preferred NAME of the argument.

@Plugin(type = OpCollection.class)
public class MathOpCollection {

	@OpField(names = MathOps.ADD, priority = Priority.LOW)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BiFunction<Double, Double, Double> addDoublesFunction = (x, y) -> x + y;

	@OpField(names = MathOps.ADD, priority = Priority.HIGH)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BinaryOperator<Double> addDoublesOperator = (x, y) -> x + y;

	@OpField(names = MathOps.SUB)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BiFunction<Double, Double, Double> subDoublesFunction = (t, u) -> t - u;

	@OpField(names = MathOps.MUL)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BiFunction<Double, Double, Double> mulDoublesFunction = (t, u) -> t * u;

	@OpField(names = MathOps.DIV)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BiFunction<Double, Double, Double> divDoublesFunction = (t, u) -> t / u;

}
