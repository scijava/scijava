/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops.imagemoments.centralmoments;

import net.imagej.ops.imagemoments.AbstractImageMomentOp;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.Computer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * {@link Op} to calculate the {@code imageMoments.centralMoment02}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 */
@Plugin(type = Op.class, name = "imageMoments.centralMoment02", label = "Image Moment: CentralMoment02")
@Parameter(key = "input")
@Parameter(key = "output", type = ItemIO.BOTH)
public class DefaultCentralMoment02<I extends RealType<I>, O extends RealType<O>>
		implements AbstractImageMomentOp<I, O> {

	// Required
	@OpDependency(name = "imageMoments.moment00")
	private Computer<IterableInterval<I>, O> moment00Func;

	@OpDependency(name = "imageMoments.moment01")
	private Computer<IterableInterval<I>, O> moment01Func;

	@Override
	public void computeMoment(final IterableInterval<I> input, final O output) {

		final O moment00 = output.createVariable();
		moment00Func.compute(input, moment00);
		final O moment01 = output.createVariable();
		moment01Func.compute(input, moment01);

		final double centerY = moment01.getRealDouble() / moment00.getRealDouble();

		double centralmoment02 = 0;

		final Cursor<I> it = input.localizingCursor();
		while (it.hasNext()) {
			it.fwd();
			final double y = it.getDoublePosition(1) - centerY;
			final double val = it.get().getRealDouble();

			centralmoment02 += val * y * y;
		}

		output.setReal(centralmoment02);
	}
}
