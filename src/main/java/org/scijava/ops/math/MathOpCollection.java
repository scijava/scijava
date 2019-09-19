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
	public static final BiFunction<Number, Number, Double> addDoublesFunction = (x, y) -> x.doubleValue() + y.doubleValue();

	@OpField(names = MathOps.ADD, priority = Priority.EXTREMELY_HIGH)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BinaryOperator<Double> addDoublesOperator = (x, y) -> x + y;

	@OpField(names = MathOps.SUB)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BiFunction<Number, Number, Double> subDoublesFunction = (t, u) -> t.doubleValue() - u.doubleValue();

	@OpField(names = MathOps.MUL)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BiFunction<Number, Number, Double> mulDoublesFunction = (t, u) -> t.doubleValue() * u.doubleValue();

	@OpField(names = MathOps.DIV)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BiFunction<Number, Number, Double> divDoublesFunction = (t, u) -> t.doubleValue() / u.doubleValue();
	
	
	@OpField(names = MathOps.MOD)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static final BiFunction<Number, Number, Double> remainderDoublesFunction = (t, u) -> t.doubleValue() % u.doubleValue();

}
