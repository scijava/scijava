package org.scijava.ops.copy;

import org.scijava.core.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.core.Computer;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class CopyOpCollection {

	@OpField(names = "cp, copy", priority = Priority.LOW)
	@Parameter(key = "array")
	@Parameter(key = "arrayCopy", type = ItemIO.OUTPUT)
	public static final Computer<double[], double[]> copyDoubleArray = (from, to) -> {
		for (int i = 0; i < to.length; i++) {
			to[i] = from[i];
		}
	};

}
