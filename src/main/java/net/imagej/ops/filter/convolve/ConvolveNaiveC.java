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

package net.imagej.ops.filter.convolve;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Convolves an image naively.
 */
@Plugin(type = Op.class, name = "filter.convolve")
@Parameter(key = "input")
@Parameter(key = "kernel")
@Parameter(key = "output", type = ItemIO.BOTH)
public class ConvolveNaiveC<I extends RealType<I>, K extends RealType<K>, O extends RealType<O>>
		implements BiComputer<RandomAccessible<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>> {
	// TODO: should this be binary so we can use different kernels?? Not sure.. what
	// if someone tried to re-use
	// with a big kernel that should be matched with ConvolveFFT

	@Override
	public void compute(final RandomAccessible<I> input, final RandomAccessibleInterval<K> kernel,
			final RandomAccessibleInterval<O> output) {
		// conforms only if the kernel is sufficiently small
		if (Intervals.numElements(kernel) > 9)
			throw new IllegalArgumentException("The kernel is too large to perform computation!");

		// TODO: try a decomposition of the kernel into n 1-dim kernels

		final long[] min = new long[input.numDimensions()];
		final long[] max = new long[input.numDimensions()];

		for (int d = 0; d < kernel.numDimensions(); d++) {
			min[d] = -kernel.dimension(d);
			max[d] = kernel.dimension(d) + output.dimension(d);
		}

		final RandomAccess<I> inRA = input.randomAccess(new FinalInterval(min, max));

		final Cursor<K> kernelC = Views.iterable(kernel).localizingCursor();

		final Cursor<O> outC = Views.iterable(output).localizingCursor();

		final long[] pos = new long[input.numDimensions()];
		final long[] kernelRadius = new long[kernel.numDimensions()];
		for (int i = 0; i < kernelRadius.length; i++) {
			kernelRadius[i] = kernel.dimension(i) / 2;
		}

		float val;

		while (outC.hasNext()) {
			// image
			outC.fwd();
			outC.localize(pos);

			// kernel inlined version of the method convolve
			val = 0;
			inRA.setPosition(pos);

			kernelC.reset();
			while (kernelC.hasNext()) {
				kernelC.fwd();

				for (int i = 0; i < kernelRadius.length; i++) {
					// dimension can have zero extension e.g. vertical 1d kernel
					if (kernelRadius[i] > 0) {
						inRA.setPosition(pos[i] + kernelC.getLongPosition(i) - kernelRadius[i], i);
					}
				}

				val += inRA.get().getRealDouble() * kernelC.get().getRealDouble();
			}

			outC.get().setReal(val);
		}
	}

}
