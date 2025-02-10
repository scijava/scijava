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

package org.scijava.ops.image.threshold;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import net.imglib2.util.Util;
import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * An abstract base class for Ops using a {@link Histogram1d} to compute a
 * threshold across an {@link Iterable}.
 * <p>
 * Note the use of type parameters {@code I} and {@code J}, allowing
 * dependencies to be matched on the concrete input types instead of just on
 * {@link Iterable}
 * </p>
 *
 * @author Curtis Rueden
 * @author Christian Dietz (University of Konstanz)
 * @author Gabriel Selzer
 * @param <T> the {@link RealType} implementation of input elements
 * @param <I> the {@link Iterable} subclass of the input
 * @param <J> the {@link Iterable} subclass of the output
 */
public abstract class AbstractApplyThresholdImg< //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> implements Computers.Arity1<I, J> {

	@OpDependency(name = "image.histogram")
	private Function<I, Histogram1d<T>> createHistogramOp;

	@OpDependency(name = "threshold.apply")
	private Computers.Arity2<I, T, J> applyThresholdOp;

	/**
	 * Thresholds {@code input}, storing the result in {@code output}.
	 *
	 * @param input the input dataset
	 * @param output the output dataset
	 */
	@Override
	public void compute(final I input, final J output) {
		// Compute the histogram
		final var inputHistogram = createHistogramOp.apply(input);
		// Compute the threshold value from the histogram
		final var threshold = input.iterator().next().createVariable();
		getComputeThresholdOp().compute(inputHistogram, threshold);
		// Threshold the image against the computed value
		applyThresholdOp.compute(input, threshold, output);
	}

	protected abstract Computers.Arity1<Histogram1d<T>, T>
		getComputeThresholdOp();

}
