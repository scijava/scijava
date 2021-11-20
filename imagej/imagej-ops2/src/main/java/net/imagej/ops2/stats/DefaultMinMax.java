/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.stats;

import java.util.List;
import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;

import org.scijava.Priority;
import org.scijava.ops.Op;
import org.scijava.plugin.Plugin;

/**
 * {@link Op} to calculate the {@code stats.minMax}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I>
 *            input type
 */
@Plugin(type = Op.class, name = "stats.minMax", priority = Priority.HIGH)
public class DefaultMinMax<I extends RealType<I>> implements Function<RandomAccessibleInterval<I>, Pair<I, I>> {

	/**
	 * TODO
	 *
	 * @param iterableInput
	 * @param minMax
	 */
	@Override
	public Pair<I, I> apply(final RandomAccessibleInterval<I> input) {
		// set minVal to the largest possible value and maxVal to the smallest possible.
		final I minVal = Util.getTypeFromInterval(input).createVariable();
		minVal.setReal(minVal.getMinValue());
		final I maxVal = minVal.createVariable();
		maxVal.setReal(maxVal.getMaxValue());

		List<Pair<I, I>> minMaxes = LoopBuilder.setImages(input).multiThreaded().forEachChunk(chunk -> {
			final I min = maxVal.copy();
			final I max = minVal.copy();
			
			chunk.forEachPixel((in) -> {
				if (in.compareTo(min) < 0) min.set(in);
				if (in.compareTo(max) > 0) max.set(in);
			});
			
			return new ValuePair<>(min, max);
		});
		
		final I raiMin = minMaxes.parallelStream() //
				.map(pair -> pair.getA()) //
				.reduce(maxVal, (result, min) -> min.compareTo(result) < 0 ? min : result);
		final I raiMax = minMaxes.parallelStream() //
				.map(pair -> pair.getB()) //
				.reduce(minVal, (result, max) -> max.compareTo(result) > 0 ? max : result);
		return new ValuePair<> (raiMin, raiMax);
	}

}
