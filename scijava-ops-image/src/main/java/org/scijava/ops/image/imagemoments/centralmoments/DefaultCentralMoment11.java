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

package org.scijava.ops.image.imagemoments.centralmoments;

import org.scijava.ops.image.imagemoments.AbstractImageMomentOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;

/**
 * Op to calculate the {@code imageMoments.centralMoment11}.
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='imageMoments.centralMoment11', label='Image Moment:
 *           CentralMoment11'
 */
public class DefaultCentralMoment11<I extends RealType<I>, O extends RealType<O>>
	implements AbstractImageMomentOp<I, O>
{

	@OpDependency(name = "imageMoments.moment00")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> moment00Func;

	@OpDependency(name = "imageMoments.moment01")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> moment01Func;

	@OpDependency(name = "imageMoments.moment10")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> moment10Func;

	@OpDependency(name = "imageMoments.moment11")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> moment11Func;

	/**
	 * TODO
	 *
	 * @param input
	 * @param output
	 */
	@Override
	public void computeMoment(final RandomAccessibleInterval<I> input,
		final O output)
	{
		final O moment00 = output.createVariable();
		moment00Func.compute(input, moment00);
		final O moment01 = output.createVariable();
		moment01Func.compute(input, moment01);
		final O moment10 = output.createVariable();
		moment10Func.compute(input, moment10);
		final O moment11 = output.createVariable();
		moment11Func.compute(input, moment11);

		// output = moment11 - (moment10 * moment01 / moment00)
		final O centerX = moment10;
		centerX.div(moment00);
		centerX.mul(moment01);

		output.set(moment11);
		output.sub(centerX);
	}
}
