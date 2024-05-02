/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2024 ImageJ2 developers.
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

import org.apache.commons.math3.distribution.NormalDistribution;

import net.imglib2.loops.LoopBuilder;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.RealType;
import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * @author Edward Evans
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='stats.pnorm', priority='100.'
 */

public final class DefaultPNorm<I extends RealType<I>, O extends RealType<O>>
	implements Computers.Arity2<I, Boolean, O>
{

	private static final NormalDistribution NORMAL_DISTRIBUTION =
		new NormalDistribution();

	/**
	 * Op to calculate the cumulative probability of a normal distribution.
	 *
	 * @param q
	 * @param lowerTail indicates whether to compute the lower tail cumulative
	 *          probability
	 * @param o probability
	 */
	@Override
	public void compute(final I q, @Nullable Boolean lowerTail, O o) {
		// set lowerTail if necessary
		if (lowerTail == null) lowerTail = true;

		double normDistValue = NORMAL_DISTRIBUTION.cumulativeProbability(q
			.getRealDouble());
		if (!lowerTail) {
			normDistValue = 1 - normDistValue;
		}
		o.setReal(normDistValue);

	}
}
