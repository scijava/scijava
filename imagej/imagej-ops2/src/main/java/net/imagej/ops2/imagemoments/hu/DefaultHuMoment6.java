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
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;

/**
 * {@link Op} to calculate the {@code imageMoments.huMoment6}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 * @see <a href="https://en.wikipedia.org/wiki/Image_moment#Rotation_invariants"> This page </a>
 * @implNote op names='imageMoments.huMoment6', label='Image Moment: HuMoment6'
 */
public class DefaultHuMoment6<I extends RealType<I>, O extends RealType<O>> implements AbstractImageMomentOp<I, O> {

	@OpDependency(name = "imageMoments.normalizedCentralMoment30")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment30Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment12")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment12Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment21")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment21Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment03")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment03Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment02")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment02Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment11")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment11Func;

	@OpDependency(name = "imageMoments.normalizedCentralMoment20")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> normalizedCentralMoment20Func;

	/**
	 * TODO
	 *
	 * @param input
	 * @param output
	 */
	@Override
	public void computeMoment(final RandomAccessibleInterval<I> input, final O output) {
		final O n02 = output.createVariable();
		normalizedCentralMoment02Func.compute(input, n02);
		final O n03 = output.createVariable();
		normalizedCentralMoment03Func.compute(input, n03);
		final O n11 = output.createVariable();
		normalizedCentralMoment11Func.compute(input, n11);
		final O n12 = output.createVariable();
		normalizedCentralMoment12Func.compute(input, n12);
		final O n20 = output.createVariable();
		normalizedCentralMoment20Func.compute(input, n20);
		final O n21 = output.createVariable();
		normalizedCentralMoment21Func.compute(input, n21);
		final O n30 = output.createVariable();
		normalizedCentralMoment30Func.compute(input, n30);
		
		// term11 = (n20 - n02)
		final O term11 = n20.copy();
		term11.sub(n02);
		// term12 = [(n30 + n12)^2 - (n21 + n03)^2]
		final O term12 = n30.copy();
		term12.add(n12);
		term12.mul(term12);
		output.set(n21);
		output.add(n03);
		output.mul(output);
		term12.sub(output);
		// term1 = term11 * term12
		final O term1 = term11.copy();
		term1.mul(term12);
		
		// term21 = 4*n11
		final O term21 = n11.copy();
		term21.mul(4);
		// term22 = (n30 + n12)
		final O term22 = n30.copy();
		term22.add(n12);
		// term23 = (n21 + n03)
		final O term23 = n21.copy();
		term23.add(n03);
		// term2 = term21 * term22 * term23 
		final O term2 = term21.copy();
		term2.mul(term22);
		term2.mul(term23);
		
		output.set(term1);
		output.add(term2);
	}
}
