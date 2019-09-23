package org.scijava.ops.math;

import java.util.function.BiFunction;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class Power {

	public static final String NAMES = MathOps.POW;

	@OpField(names = NAMES)
	@Parameter(key = "number")
	@Parameter(key = "exponent")
	@Parameter(key = "result", itemIO = ItemIO.OUTPUT)
	public static final BiFunction<Double, Double, Double> MathPowerDoubleFunction = (base, exp) -> Math.pow(base, exp);

	@OpField(names = NAMES)
	@Parameter(key = "array")
	@Parameter(key = "power")
	@Parameter(key = "resultArray", itemIO = ItemIO.BOTH)
	public static final Computers.Arity2<double[], Double, double[]> MathPointwisePowerDoubleArrayComputer = (in, pow, out) -> {
		for (int i = 0; i < in.length; i++)
			out[i] = Math.pow(in[i], pow);
	};

}
