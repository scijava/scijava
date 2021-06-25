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

package net.imagej.ops2.imagemoments.hu;

import net.imagej.ops2.imagemoments.AbstractImageMomentOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * {@link Op} to calculate the {@code imageMoments.huMoment2}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Gabriel Selzer
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 * @see <a href="https://en.wikipedia.org/wiki/Image_moment#Rotation_invariants"> This page </a>
 */
@Plugin(type = Op.class, name = "imageMoments.huMoment2", label = "Image Moment: HuMoment2")
@Parameter(key = "input")
@Parameter(key = "output")
public class DefaultHuMoment2<I extends RealType<I>, O extends RealType<O>> implements AbstractImageMomentOp<I, O> {

	@OpDependency(name = "imageMoments.normalizedCentralMoment20")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment20Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment02")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment02Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment11")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment11Func;

	@Override
	public void computeMoment(final RandomAccessibleInterval<I> input, final O output) {

		final O n11 = output.createVariable();
		normalizedCentralMoment11Func.compute(input, n11);
		final O n20 = output.createVariable();
		normalizedCentralMoment20Func.compute(input, n20);
		final O n02 = output.createVariable();
		normalizedCentralMoment02Func.compute(input, n02);
		
		// n11Squared = 4 * n11 * n11
		final O n11Squared = n11.copy();
		n11Squared.mul(n11);
		output.setReal(4d);
		n11Squared.mul(output);
		
		// n2Squared = (n20 - n02)^2
		final O n2Squared = n20.copy();
		n2Squared.sub(n02);
		n2Squared.mul(n2Squared);
		
		//output = n2Squared + n11Squared
		output.set(n2Squared);
		output.add(n11Squared);
	}
}
