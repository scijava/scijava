package org.scijava.ops.math;

import java.util.function.BiFunction;

import org.scijava.function.Computers;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class Power {

	public static final String NAMES = MathOps.POW;

	/**
	 * Computes the value of a number raised to the given power.
	 * 
	 * @input number The number to exponentiate.
	 * @input exponent The power to which to raise the number.
	 * @output result The number raised to the exponent.
	 */
	@OpField(names = NAMES, params = "number, exponent, result")
	public static final BiFunction<Double, Double, Double> MathPowerDoubleFunction = (base, exp) -> Math.pow(base, exp);

	@OpField(names = NAMES, params = "array, power, resultArray")
	public static final Computers.Arity2<double[], Double, double[]> MathPointwisePowerDoubleArrayComputer = (in, pow, out) -> {
		for (int i = 0; i < in.length; i++)
			out[i] = Math.pow(in[i], pow);
	};

}
