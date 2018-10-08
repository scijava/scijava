package org.scijava.ops.create;

import java.util.function.Function;

import org.scijava.core.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class CreateOpCollection {

	@OpField(names = "create, src, source", priority = Priority.LOW)
	@Parameter(key = "array")
	@Parameter(key = "arrayLike", type = ItemIO.OUTPUT)
	public static final Function<double[], double[]> createDoubleArray = from -> new double[from.length];

}
