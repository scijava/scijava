package org.scijava.ops.math;

import java.util.function.Function;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.inplace.Inplace;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class Sqrt {

	public static final String NAMES = MathOps.SQRT;

	// --------- Functions ---------

	@OpField(names = NAMES)
	@Parameter(key = "number1")
	@Parameter(key = "result", itemIO = ItemIO.OUTPUT)
	public static final Function<Double, Double> MathSqrtDoubleFunction = Math::sqrt;

	// --------- Computers ---------

	@OpField(names = NAMES)
	@Parameter(key = "array1")
	@Parameter(key = "resultArray", itemIO = ItemIO.BOTH)
	public static final Computer<double[], double[]> MathPointwiseSqrtDoubleArrayComputer = (arr1, arr2) -> {
		for (int i = 0; i < arr1.length; i++)
			arr2[i] = Math.sqrt(arr1[i]);
	};

	// --------- Inplaces ---------

	@OpField(names = NAMES)
	@Parameter(key = "arrayIO", itemIO = ItemIO.BOTH)
	public static final Inplace<double[]> MathPointwiseSqrtDoubleArrayInplace = (arr) -> {
		for(int i = 0; i < arr.length; i++) arr[i] = Math.sqrt(arr[i]);
	};

}
