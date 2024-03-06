/*-
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2024 ImageJ developers.
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

package org.scijava.ops.image.coloc.saca;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Spatially Adaptive Colocalization Analysis (SACA) Adapted from Shulei's
 * original Java code for AdaptiveSmoothedKendallTau from his RKColocal R
 * package.
 * (https://github.com/lakerwsl/RKColocal/blob/master/RKColocal_0.0.1.0000.tar.gz)
 *
 * @author Shulei Wang
 * @author Curtis Rueden
 * @author Ellen TA Dobson
 * @author Edward Evans
 * @implNote op names='coloc.saca.heatmapPValue', priority='100.'
 */

public class SACAHeatmapPValue implements
	Computers.Arity2<RandomAccessibleInterval<DoubleType>, Boolean, RandomAccessibleInterval<DoubleType>>
{

	/**
	 * Spatially Adaptive Colocalization Analysis (SACA) P Value heatmap
	 *
	 * @param heatmap input heatmap returned from 'coloc.saca.heatmapZScore'
	 * @param lowerTail lower tail (default=false)
	 * @param result P value heatmap output
	 */

	@Override
	public void compute(final RandomAccessibleInterval<DoubleType> heatmap,
		@Nullable Boolean lowerTail, RandomAccessibleInterval<DoubleType> result)
	{
        // set lowerTail if necessary
        if (lowerTail == null) lowerTail = true;

        // compute the normal distribution
		PNorm.compute(heatmap, lowerTail, result);
	}
}
