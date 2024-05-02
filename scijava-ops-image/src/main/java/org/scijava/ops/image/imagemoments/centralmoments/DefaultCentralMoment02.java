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

import java.util.List;

import org.scijava.ops.image.imagemoments.AbstractImageMomentOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;

/**
 * Op to calculate the {@code imageMoments.centralMoment02}.
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='imageMoments.centralMoment02', label='Image Moment:
 *           CentralMoment02'
 */
public class DefaultCentralMoment02<I extends RealType<I>, O extends RealType<O>>
	implements AbstractImageMomentOp<I, O>
{

	// Required
	@OpDependency(name = "imageMoments.moment00")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> moment00Func;
	@OpDependency(name = "imageMoments.moment01")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> moment01Func;
	@OpDependency(name = "math.power")
	private Computers.Arity2<O, Integer, O> powerOp;

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

		final O centerY = moment01.copy();
		centerY.div(moment00);

		List<O> sums = LoopBuilder.setImages(input, Intervals.positions(input))
			.multiThreaded().forEachChunk(chunk -> {
				O sum = output.createVariable();
				O temp = output.createVariable();
				O y = output.createVariable();
				chunk.forEachPixel((pixel, pos) -> {
					// y = (pixelPos - centerY)^2
					temp.setReal(pos.getDoublePosition(1));
					temp.sub(centerY);
					powerOp.compute(temp, 2, y);
					// temp = pixelVal * (pixelPos - centerY)^2
					temp.setReal(pixel.getRealDouble());
					temp.mul(y);
					sum.add(temp);
				});
				return sum;
			});

		output.setZero();
		for (O sum : sums)
			output.add(sum);
	}
}
