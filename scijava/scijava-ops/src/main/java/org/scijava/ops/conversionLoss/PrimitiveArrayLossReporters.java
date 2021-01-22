package org.scijava.ops.conversionLoss;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.hints.OpHints;
import org.scijava.ops.hints.BaseOpHints.Simplification;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class PrimitiveArrayLossReporters {
	
//	@Unsimplifiable
//	@Plugin(type = Op.class)
//	static class ByteArrayIntArrayReporter implements LosslessReporter<Byte[], Integer[]> {}
	
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Byte[], Integer[]> bArrIArr = (from, to) -> 0.;
	
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Double[], Integer[]> dArrIArr = (from, to) -> 0.;

}

//@Plugin(type = Op.class, name = "lossReporter")
//@Parameter(key = "fromNil")
//@Parameter(key = "toNil")
//@Parameter(key = "loss")
//public static class ArrayLossReporter<T extends Number, U extends Number> implements LossReporter<T[], U[]>{
//
//	@OpDependency(name = "lossReporter")
//	private LossReporter<T, U> elementReporter;
//	
//	@Override
//	public Double apply(Nil<T[]> from, Nil<U[]> to) {
//		Nil<T> fromElement = Nil.of(from.getType())
//	}
//	
//}
