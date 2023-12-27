package org.scijava.ops.benchmarks;

import net.imagej.ops.Ops;
import net.imagej.ops.special.inplace.AbstractUnaryInplaceOp;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Math.RandomUniform.class)
public class RandomOp extends AbstractUnaryInplaceOp<byte[]>
		implements Ops.Math.RandomUniform
{

	@Override public void mutate(byte[] o) {
		PerformanceBenchmark.randomizeRaw(o);
	}
}
