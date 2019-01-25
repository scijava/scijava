package org.scijava.ops.math;

import java.util.stream.IntStream;

import org.scijava.ops.core.NullaryComputer;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class Zero {

	public static final String NAMES = MathOps.ZERO;
	
	// --------- Computers ---------

	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "resultArray", itemIO = ItemIO.BOTH)
	public static class MathParallelPointwiseZeroDoubleArrayComputer implements NullaryComputer<double[]> {
		@Override
		public void compute(double[] out) {
			IntStream.range(0, out.length).parallel().forEach(i -> {
				out[i] = 0.0;
			});
		}
	}
}
