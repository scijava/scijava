package org.scijava.ops.engine.math;

import java.util.stream.IntStream;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class Zero implements OpCollection {

	public static final String NAMES = MathOps.ZERO;

	// --------- Computers ---------

	@OpField(names = NAMES)
	public static final Computers.Arity0<double[]> MathParallelPointwiseZeroDoubleArrayComputer = out -> {
		IntStream.range(0, out.length).parallel().forEach(i -> {
			out[i] = 0.0;
		});
	};
}
