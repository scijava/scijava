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

package org.scijava.ops.image.imagemoments.normalizedcentralmoments;

import org.scijava.ops.image.imagemoments.AbstractImageMomentOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;

/**
 * Op to calculate the {@code imageMoments.normalizedCentralMoment30}.
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='imageMoments.normalizedCentralMoment30', label='Image
 *           Moment: NormalizedCentralMoment30'
 */
public class DefaultNormalizedCentralMoment30<I extends RealType<I>, O extends RealType<O>>
	implements AbstractImageMomentOp<I, O>
{

	@OpDependency(name = "imageMoments.centralMoment00")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> centralMoment00Func;

	@OpDependency(name = "imageMoments.centralMoment30")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> centralMoment30Func;

	@Override
	public void computeMoment(final RandomAccessibleInterval<I> input,
		final O output)
	{
		final var moment00 = output.createVariable();
		centralMoment00Func.compute(input, moment00);
		final var moment30 = output.createVariable();
		centralMoment30Func.compute(input, moment30);

		output.setReal(moment30.getRealDouble() / Math.pow(moment00.getRealDouble(),
			1 + ((3 + 0) / 2.0)));
	}
}
