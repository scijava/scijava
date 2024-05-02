/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.image.histogram;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Nullable;

/**
 * @author Martin Horn (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @implNote op names='image.histogram'
 */
public class HistogramCreate<T extends RealType<T>> implements
	BiFunction<Iterable<T>, Integer, Histogram1d<T>>
{

	public static final int DEFAULT_NUM_BINS = 256;

	@OpDependency(name = "stats.minMax")
	private Function<Iterable<T>, Pair<T, T>> minMaxFunc;

	/**
	 * TODO
	 *
	 * @param input the iterable to feed into the {@link Histogram1d}
	 * @param numBins the number of bins in the resulting {@link Histogram1d}
	 * @return a histogram
	 */
	@Override
	public Histogram1d<T> apply(final Iterable<T> input,
		@Nullable Integer numBins)
	{
		if (numBins == null) numBins = DEFAULT_NUM_BINS;

		final Pair<T, T> res = minMaxFunc.apply(input);

		final Histogram1d<T> histogram1d = new Histogram1d<>(new Real1dBinMapper<T>(
			res.getA().getRealDouble(), res.getB().getRealDouble(), numBins, false));

		histogram1d.countData(input);

		return histogram1d;
	}

}
