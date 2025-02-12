/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

import java.util.ArrayList;

import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;

/**
 * Op to calculate the {@code stats.median}.
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Jan Eglinger
 * @author Richard Domander
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='stats.median'
 */
public class DefaultMedian<I extends RealType<I>, O extends RealType<O>>
	implements Computers.Arity1<Iterable<I>, O>
{

	@OpDependency(name = "stats.quantile")
	private Computers.Arity2<Iterable<I>, Double, O> quantileOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param median
	 */
	@Override
	public void compute(final Iterable<I> input, final O median) {
		final var statistics = new ArrayList<Double>();

		input.forEach(i -> statistics.add(i.getRealDouble()));

		final var k = statistics.size() / 2;
        var result = DefaultQuantile.select(statistics, k);
		if (statistics.size() % 2 == 0) {
			result += DefaultQuantile.select(statistics, k - 1);
			result *= 0.5;
		}

		median.setReal(result);
	}

}
