package org.scijava.ops.engine.math;

import java.util.stream.IntStream;

import org.scijava.function.Computers;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpField;
import org.scijava.ops.api.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class Zero {

	public static final String NAMES = MathOps.ZERO;

	// --------- Computers ---------

	@OpField(names = NAMES)
	public static final Computers.Arity0<double[]> MathParallelPointwiseZeroDoubleArrayComputer = out -> {
		IntStream.range(0, out.length).parallel().forEach(i -> {
			out[i] = 0.0;
		});
	};
}
