package org.scijava.ops.math;

import java.util.stream.IntStream;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.NullaryComputer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class Zero {

	public static final String NAMES = MathOps.ZERO;

	// --------- Computers ---------

	@OpField(names = NAMES)
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static final NullaryComputer<double[]> MathParallelPointwiseZeroDoubleArrayComputer = (out) -> {
		IntStream.range(0, out.length).parallel().forEach(i -> {
			out[i] = 0.0;
		});
	};
}
