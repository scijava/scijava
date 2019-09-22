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

package net.imagej.ops.imagemoments.normalizedcentralmoments;

import net.imagej.ops.imagemoments.AbstractImageMomentOp;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.Computer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * {@link Op} to calculate the {@code imageMoments.normalizedCentralMoment03}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I> input type
 * @param <O> output type
 */
@Plugin(type = Op.class, name = "imageMoments.normalizedCentralMoment03",
	label = "Image Moment: NormalizedCentralMoment03")
@Parameter(key = "input")
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class DefaultNormalizedCentralMoment03<I extends RealType<I>, O extends RealType<O>>
	implements AbstractImageMomentOp<I, O> 
{

	@OpDependency(name = "imageMoments.centralMoment00")
	private Computer<IterableInterval<I>, O> centralMoment00Func;

	@OpDependency(name = "imageMoments.centralMoment03")
	private Computer<IterableInterval<I>, O> centralMoment03Func;

	@Override
	public void computeMoment(final IterableInterval<I> input, final O output) {
		final O moment00 = output.createVariable();
		centralMoment00Func.compute(input, moment00);
		final O moment03 = output.createVariable();
		centralMoment03Func.compute(input, moment03);

		output.setReal(moment03.getRealDouble() /
			Math.pow(moment00.getRealDouble(), 1 + ((0 + 3) / 2.0)));
	}
}
