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

package net.imagej.ops.imagemoments.hu;

import net.imagej.ops.imagemoments.AbstractImageMomentOp;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * {@link Op} to calculate the {@code imageMoments.huMoment5}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 */
@Plugin(type = Op.class, name = "imageMoments.huMoment5", label = "Image Moment: HuMoment5")
@Parameter(key = "input")
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class DefaultHuMoment5<I extends RealType<I>, O extends RealType<O>> implements AbstractImageMomentOp<I, O> {

	@OpDependency(name = "imageMoments.normalizedCentralMoment30")
	private Computers.Arity1<IterableInterval<I>, O> normalizedCentralMoment30Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment12")
	private Computers.Arity1<IterableInterval<I>, O> normalizedCentralMoment12Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment21")
	private Computers.Arity1<IterableInterval<I>, O> normalizedCentralMoment21Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment03")
	private Computers.Arity1<IterableInterval<I>, O> normalizedCentralMoment03Func;

	@Override
	public void computeMoment(final IterableInterval<I> input, final O output) {
		final O n30 = output.createVariable();
		normalizedCentralMoment30Func.compute(input, n30);
		final O n12 = output.createVariable();
		normalizedCentralMoment12Func.compute(input, n12);
		final O n21 = output.createVariable();
		normalizedCentralMoment21Func.compute(input, n21);
		final O n03 = output.createVariable();
		normalizedCentralMoment03Func.compute(input, n03);

		output.setReal((n30.getRealDouble() - 3 * n12.getRealDouble()) * (n30.getRealDouble() + n12.getRealDouble())
				* (Math.pow(n30.getRealDouble() + n12.getRealDouble(), 2)
						- 3 * Math.pow(n21.getRealDouble() + n03.getRealDouble(), 2))
				+ (3 * n21.getRealDouble() - n03.getRealDouble()) * (n21.getRealDouble() + n03.getRealDouble())
						* (3 * Math.pow(n30.getRealDouble() + n12.getRealDouble(), 2)
								- Math.pow(n21.getRealDouble() + n03.getRealDouble(), 2)));
	}
}
