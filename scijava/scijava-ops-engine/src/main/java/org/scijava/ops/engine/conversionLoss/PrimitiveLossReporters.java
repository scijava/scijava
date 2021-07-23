
package org.scijava.ops.engine.conversionLoss;

import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpField;
import org.scijava.ops.engine.OpHints;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.api.OpField;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

/**
 * An {@link OpCollection} containing the {@link LossReporter}s for primitive
 * type conversions
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class PrimitiveLossReporters {
	
	@FunctionalInterface
	static interface IntToDecimalReporter<T, R> extends LossReporter<T, R> {
		
		Double intToDecimalLoss(Nil<T> from, Nil<R> to);

		@Override
		default Double apply(Nil<T> from, Nil<R> to) {
			return 1 + intToDecimalLoss(from, to);
		}
		
	}


	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final IntToDecimalReporter<Long, Double> LongDoubleReporter = (from, to) -> {
		long maxValue = Long.MAX_VALUE - 1;
		double converted = maxValue;
		return (double) Math.abs(maxValue - (long) converted);
	};

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Double, Long> DoubleLongReporter = (from, to) -> {
		double maxValue = Double.MAX_VALUE;
		long converted = (long) maxValue;
		return maxValue - converted;
	};

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Long, Integer> LongIntegerReporter = (from, to) -> {
		long maxValue = Long.MAX_VALUE;
		int converted = (int) maxValue;
		return (double) Math.abs(maxValue - converted);
	};

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Integer, Long> IntegerLongReporter = (from, to) -> {
		return 0.;
	};

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final IntToDecimalReporter<Integer, Double> IntegerDoubleReporter = (from, to) -> {
		long maxValue = Integer.MAX_VALUE;
		double converted = maxValue;
		return (double) Math.abs(maxValue - (long) converted);
	};

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Double, Integer> DoubleIntegerReporter = (from, to) -> {
		double maxValue = Double.MAX_VALUE;
		int converted = (int) maxValue;
		return maxValue - converted;
	};

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Number, Double> NumberDoubleReporter = (from,
		to) -> LongDoubleReporter.apply(Nil.of(Long.class), Nil.of(Double.class));

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<Number, Long> NumberLongReporter = (from,
		to) -> DoubleLongReporter.apply(Nil.of(Double.class), Nil.of(Long.class));
}