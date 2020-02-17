package org.scijava.ops.math;

import java.util.function.BiFunction;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class Power {

	public static final String NAMES = MathOps.POW;

	@OpField(names = NAMES, params = "number, exponent, result")
	public static final BiFunction<Double, Double, Double> MathPowerDoubleFunction = (base, exp) -> Math.pow(base, exp);

	@OpField(names = NAMES, params = "array, power, resultArray")
	public static final Computers.Arity2<double[], Double, double[]> MathPointwisePowerDoubleArrayComputer = (in, pow, out) -> {
		for (int i = 0; i < in.length; i++)
			out[i] = Math.pow(in[i], pow);
	};

}
