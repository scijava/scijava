package org.scijava.ops.conversionLoss;

import org.scijava.ops.OpCollection;
import org.scijava.ops.OpField;
import org.scijava.ops.hints.OpHints;
import org.scijava.ops.hints.BaseOpHints.Simplification;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class PrimitiveArrayLossReporters {
	
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Byte[], Integer[]> bArrIArr = (from, to) -> 0.;
	
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Double[], Integer[]> dArrIArr = (from, to) -> 0.;

}
