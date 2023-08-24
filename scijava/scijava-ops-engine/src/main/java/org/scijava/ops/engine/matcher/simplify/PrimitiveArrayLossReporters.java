package org.scijava.ops.engine.matcher.simplify;

import org.scijava.ops.api.OpHints;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class PrimitiveArrayLossReporters implements OpCollection {
	
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Byte[], Integer[]> bArrIArr = (from, to) -> 0.;
	
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Double[], Integer[]> dArrIArr = (from, to) -> 0.;

}
