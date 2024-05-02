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

package org.scijava.ops.image.stats;

import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;

/**
 * Op to calculate the n-th {@code stats.percentile}.
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Jan Eglinger
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='stats.percentile'
 */
public class DefaultPercentile<I extends RealType<I>, N extends Number, O extends RealType<O>>
	implements Computers.Arity2<Iterable<I>, N, O>
{

	@OpDependency(name = "stats.quantile")
	private Computers.Arity2<Iterable<I>, Double, O> op;

	/**
	 * TODO
	 *
	 * @param iterableInput
	 * @param percent
	 * @param percentile
	 */
	@Override
	public void compute(final Iterable<I> input, final N percent,
		final O output)
	{
		if (percent.doubleValue() < 0 || percent.doubleValue() > 100) {
			throw new IllegalArgumentException(
				"Percent must be between 0 and 100 (inclusive) but was " + percent);
		}
		op.compute(input, percent.doubleValue() / 100, output);
	}
}
