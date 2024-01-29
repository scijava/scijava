
package org.scijava.ops.benchmarks;

import net.imagej.ops.Ops;
import net.imagej.ops.special.inplace.AbstractUnaryInplaceOp;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Image.Invert.class)
public class InvertOp extends AbstractUnaryInplaceOp<byte[]> implements
	Ops.Image.Invert
{

	@Override
	public void mutate(byte[] o) {
		PerformanceBenchmark.invertRaw(o);
	}
}
