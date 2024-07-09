/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine.conversionLoss.impl;

import org.scijava.ops.spi.OpHints;
import org.scijava.ops.engine.BaseOpHints.Conversion;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

/**
 * An {@link OpCollection} containing the {@link LossReporter}s for primitive
 * type conversions
 *
 * @author Gabriel Selzer
 */
public class PrimitiveLossReporters implements OpCollection {

	@FunctionalInterface
	interface IntToDecimalReporter<T, R> extends LossReporter<T, R> {

		Double intToDecimalLoss(Nil<T> from, Nil<R> to);

		@Override
		default Double apply(Nil<T> from, Nil<R> to) {
			return 1 + intToDecimalLoss(from, to);
		}

	}

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.lossReporter")
	public final IntToDecimalReporter<Long, Double> LongDoubleReporter = (from,
		to) -> {
        var maxValue = Long.MAX_VALUE - 1;
		double converted = maxValue;
		return (double) Math.abs(maxValue - (long) converted);
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.lossReporter")
	public final LossReporter<Double, Long> DoubleLongReporter = (from, to) -> {
        var maxValue = Double.MAX_VALUE;
        var converted = (long) maxValue;
		return maxValue - converted;
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.lossReporter")
	public final LossReporter<Long, Integer> LongIntegerReporter = (from, to) -> {
        var maxValue = Long.MAX_VALUE;
        var converted = (int) maxValue;
		return (double) Math.abs(maxValue - converted);
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.lossReporter")
	public final LossReporter<Integer, Long> IntegerLongReporter = (from, to) -> {
		return 0.;
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.lossReporter")
	public final IntToDecimalReporter<Integer, Double> IntegerDoubleReporter = (
		from, to) -> {
		long maxValue = Integer.MAX_VALUE;
		double converted = maxValue;
		return (double) Math.abs(maxValue - (long) converted);
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.lossReporter")
	public final LossReporter<Double, Integer> DoubleIntegerReporter = (from,
		to) -> {
        var maxValue = Double.MAX_VALUE;
        var converted = (int) maxValue;
		return maxValue - converted;
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.lossReporter")
	public final LossReporter<Number, Double> NumberDoubleReporter = (from,
		to) -> LongDoubleReporter.apply(Nil.of(Long.class), Nil.of(Double.class));

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.lossReporter")
	public final LossReporter<Number, Long> NumberLongReporter = (from,
		to) -> DoubleLongReporter.apply(Nil.of(Double.class), Nil.of(Long.class));
}
