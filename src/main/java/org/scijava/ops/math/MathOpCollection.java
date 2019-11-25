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
	public static final BiFunction<Number, Number, Double> addDoublesFunction = (x, y) -> x.doubleValue() + y.doubleValue();

	@OpField(names = MathOps.ADD, priority = Priority.EXTREMELY_HIGH)
	public static final BinaryOperator<Double> addDoublesOperator = (x, y) -> x + y;

	@OpField(names = MathOps.SUB)
	public static final BiFunction<Number, Number, Double> subDoublesFunction = (t, u) -> t.doubleValue() - u.doubleValue();

	@OpField(names = MathOps.MUL)
	public static final BiFunction<Number, Number, Double> mulDoublesFunction = (t, u) -> t.doubleValue() * u.doubleValue();

	@OpField(names = MathOps.DIV)
	public static final BiFunction<Number, Number, Double> divDoublesFunction = (t, u) -> t.doubleValue() / u.doubleValue();
	
	
	@OpField(names = MathOps.MOD)
	public static final BiFunction<Number, Number, Double> remainderDoublesFunction = (t, u) -> t.doubleValue() % u.doubleValue();

}
