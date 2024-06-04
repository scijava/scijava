/*-
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

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * @author Shulei Wang
 * @author Ellen TA Dobson
 * @author Curtis Rueden
 * @author Edward Evans
 * @implNote op names='stats.qnorm', priority='100.'
 */

public final class DefaultQNorm implements
	Functions.Arity5<Double, Double, Double, Boolean, Boolean, Double>
{

	/**
	 * Op to calculate the quantile function for a normal distribution.
	 *
	 * @param p
	 * @param mean
	 * @param sd
	 * @param lowerTail
	 * @param logP
	 * @return quantiles for input probaility
	 */
	@Override
	public Double apply(Double p, @Nullable Double mean, @Nullable Double sd,
		@Nullable Boolean lowerTail, @Nullable Boolean logP)
	{
		// set mean if necessary
		if (mean == null) mean = 0.0;

		// set sd if necessary
		if (sd == null) sd = 1.0;

		// set lowerTail if necessary
		if (lowerTail == null) lowerTail = true;

		// set logP if necessary
		if (logP == null) logP = false;

		// check the bounds of p
		if (p < 0 || p > 1) {
			return Double.NaN;
		}
		if (p == 0 || p == 1) {
			return Double.POSITIVE_INFINITY;
		}

		// compute QNorm
		final NormalDistribution dist = new NormalDistribution(mean, sd);
		if (logP) p = Math.exp(p);
		final double q = dist.inverseCumulativeProbability(p);
		return lowerTail ? q : -q;

	}
}
