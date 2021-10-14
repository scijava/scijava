package org.scijava.ops.engine.math;

import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class Sqrt {

	public static final String NAMES = MathOps.SQRT;

	// --------- Functions ---------

	@OpField(names = NAMES, params = "number1, result")
	public static final Function<Double, Double> MathSqrtDoubleFunction = Math::sqrt;

	// --------- Computers ---------

	@OpField(names = NAMES, params = "array1, resultArray")
	public static final Computers.Arity1<double[], double[]> MathPointwiseSqrtDoubleArrayComputer = (arr1, arr2) -> {
		for (int i = 0; i < arr1.length; i++)
			arr2[i] = Math.sqrt(arr1[i]);
	};

	// --------- Inplaces ---------

	@OpField(names = NAMES, params = "arrayIO")
	public static final Inplaces.Arity1<double[]> MathPointwiseSqrtDoubleArrayInplace = (arr) -> {
		for(int i = 0; i < arr.length; i++) arr[i] = Math.sqrt(arr[i]);
	};

}
