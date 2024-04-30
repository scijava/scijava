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

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * @author Edward Evans
 * @implNote op names='stats.pnorm', priority='100.'
 */

public final class DefaultPNorm implements
	Computers.Arity2<RandomAccessibleInterval<DoubleType>, Boolean, RandomAccessibleInterval<DoubleType>>
{

	/**
	 * Op to calculate the pixel-wise cumulative probability of a normal
	 * distribution over an image.
	 *
	 * @param input input image
	 * @param lowerTail indicates whether to compute the lower tail cumulative
	 *          probability
	 * @param result the pixel-wise normal distribution over the image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<DoubleType> input,
		@Nullable Boolean lowerTail, RandomAccessibleInterval<DoubleType> result)
	{
		// set lowerTail if necessary
		if (lowerTail == null) lowerTail = true;
		final boolean finalLowerTail = lowerTail;

		// compute normal distribution over the image
		NormalDistribution normalDistribution = new NormalDistribution();

		LoopBuilder.setImages(input, result).multiThreaded().forEachPixel((i,
			r) -> {
			double normDistValue = normalDistribution.cumulativeProbability(i.get());
			if (!finalLowerTail) {
				normDistValue = 1 - normDistValue;
			}
			r.set(normDistValue);
		});
	}
}
