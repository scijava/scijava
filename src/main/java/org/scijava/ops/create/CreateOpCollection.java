package org.scijava.ops.create;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.core.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class CreateOpCollection {

	@OpField(names = "create, src, source", priority = Priority.LOW, params = "x")
	@Parameter(key = "array")
	@Parameter(key = "arrayLike", itemIO = ItemIO.OUTPUT)
	public static final Function<double[], double[]> createDoubleArrayInputAware = from -> new double[from.length];
	
	@OpField(names = "create, src, source", priority = Priority.LOW, params = "x")
	@Parameter(key = "array1")
	@Parameter(key = "array2")
	@Parameter(key = "arrayLike", itemIO = ItemIO.OUTPUT)
	public static final BiFunction<double[], double[], double[]> createDoubleArrayBiInputAware = (i1, i2) -> {
		if (i1.length != i2.length) {
			throw new IllegalArgumentException("Input array length muss be equal");
		}
		return new double[i1.length];
	};
}
