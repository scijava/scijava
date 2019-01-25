package org.scijava.ops.math;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.scijava.core.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class MathOpCollection {

	@OpField(names = MathOps.ADD, priority = Priority.LOW)
	public static final BiFunction<Double, Double, Double> addDoublesFunction = (x, y) -> x + y;

	// Set this priority to extremely high for now to test that the op with the highest priority is actually retrieved
	@OpField(names = MathOps.ADD, priority = Priority.EXTREMELY_HIGH)
	public static final BinaryOperator<Double> addDoublesOperator = (x, y) -> x + y;

	@OpField(names = MathOps.SUB)
	public static final BiFunction<Double, Double, Double> subDoublesFunction = (t, u) -> t - u;

	@OpField(names = MathOps.MUL)
	public static final BiFunction<Double, Double, Double> mulDoublesFunction = (t, u) -> t * u;

	@OpField(names = MathOps.DIV)
	public static final BiFunction<Double, Double, Double> divDoublesFunction = (t, u) -> t / u;

}
