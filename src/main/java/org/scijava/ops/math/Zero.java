package org.scijava.ops.math;

import java.util.stream.IntStream;

import org.scijava.ops.NullaryComputer;
import org.scijava.ops.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class Zero {

	public interface MathZeroOp extends Op {
	}
	
	// --------- Computers ---------

	@Plugin(type = MathZeroOp.class)
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static class MathParallelPointwiseZeroDoubleArrayComputer implements MathZeroOp, NullaryComputer<double[]> {
		@Override
		public void compute(double[] out) {
			IntStream.range(0, out.length).parallel().forEach(i -> {
				out[i] = 0.0;
			});
		}
	}
}
