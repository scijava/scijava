/* #%L
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

package net.imagej.ops2.filter.derivative;

import java.util.function.Function;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Calculates the derivative (with sobel kernel) of an image in a given
 * dimension.
 * 
 * @author Eike Heinz, University of Konstanz
 *
 * @param <T>
 *            type of input
 */
@Plugin(type = Op.class, name = "filter.partialDerivative")
public class PartialDerivativeRAI<T extends RealType<T>>
		implements Computers.Arity2<RandomAccessibleInterval<T>, Integer, RandomAccessibleInterval<T>> {

	@OpDependency(name = "create.img")
	private Function<RandomAccessibleInterval<T>, Img<T>> createRAI;

	@OpDependency(name = "create.img")
	private Function<long[], Img<DoubleType>> createImg;

	@OpDependency(name = "math.add")
	private Computers.Arity2<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> addOp;

	@OpDependency(name = "filter.convolve")
	private Computers.Arity2<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> convolveOp;

	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> kernelBConvolveOp;

	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>[] kernelAConvolveOps;

	@OpDependency(name = "create.kernelSobel")
	private Function<T, RandomAccessibleInterval<T>> sobelKernelCreator;

	// TODO: is there any way to speed this up?
	@SuppressWarnings("unchecked")
	public void setupConvolves(RandomAccessibleInterval<T> input, Integer dimension) {
		RandomAccessibleInterval<T> kernel = sobelKernelCreator.apply(Util.getTypeFromInterval(input));

		RandomAccessibleInterval<T> kernelA = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 0);

		RandomAccessibleInterval<T> kernelB = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 1);

		// add dimensions to kernel to rotate properly
		if (input.numDimensions() > 2) {
			RandomAccessible<T> expandedKernelA = Views.addDimension(kernelA);
			RandomAccessible<T> expandedKernelB = Views.addDimension(kernelB);
			for (int i = 0; i < input.numDimensions() - 3; i++) {
				expandedKernelA = Views.addDimension(expandedKernelA);
				expandedKernelB = Views.addDimension(expandedKernelB);
			}
			long[] dims = new long[input.numDimensions()];
			for (int j = 0; j < input.numDimensions(); j++) {
				dims[j] = 1;
			}
			dims[0] = 3;
			Interval kernelInterval = new FinalInterval(dims);
			kernelA = Views.interval(expandedKernelA, kernelInterval);
			kernelB = Views.interval(expandedKernelB, kernelInterval);
		}

		long[] dims = new long[input.numDimensions()];
		if (dimension == 0) {
			// HACK needs to be final so that the compiler can encapsulate it.
			final RandomAccessibleInterval<T> finalKernelB = kernelB;
			// FIXME hack
			kernelBConvolveOp = (in, out) -> convolveOp.compute(in, finalKernelB, out);
		} else {
			// rotate kernel B to dimension
			for (int j = 0; j < input.numDimensions(); j++) {
				if (j == dimension) {
					dims[j] = 3;
				} else {
					dims[j] = 1;
				}
			}

			Img<DoubleType> kernelInterval = createImg.apply(dims);

			RandomAccessibleInterval<T> rotatedKernelB = kernelB;
			for (int i = 0; i < dimension; i++) {
				rotatedKernelB = Views.rotate(rotatedKernelB, i, i + 1);
			}

			// HACK needs to be final so that the compiler can encapsulate it.
			final RandomAccessibleInterval<T> finalRotatedKernelB = Views.interval(rotatedKernelB, kernelInterval);
			kernelBConvolveOp = (in, out) -> convolveOp.compute(in, finalRotatedKernelB, out);
		}

		dims = null;

		// rotate kernel A to all other dimensions
		kernelAConvolveOps = new Computers.Arity1[input.numDimensions()];
		if (dimension != 0) {
			// HACK needs to be final so that the compiler can encapsulate it.
			final RandomAccessibleInterval<T> finalKernelA = kernelA;
			kernelAConvolveOps[0] = (in, out) -> convolveOp.compute(in, finalKernelA, out);
		}
		RandomAccessibleInterval<T> rotatedKernelA = kernelA;
		for (int i = 1; i < input.numDimensions(); i++) {
			if (i != dimension) {
				dims = new long[input.numDimensions()];
				for (int j = 0; j < input.numDimensions(); j++) {
					if (i == j) {
						dims[j] = 3;
					} else {
						dims[j] = 1;
					}
				}
				Img<DoubleType> kernelInterval = createImg.apply(dims);
				for (int j = 0; j < i; j++) {
					rotatedKernelA = Views.rotate(rotatedKernelA, j, j + 1);
				}

				// HACK needs to be final so that the compiler can encapsulate it.
				final RandomAccessibleInterval<T> finalRotatedKernelA = rotatedKernelA;
				kernelAConvolveOps[i] = (in, out) -> convolveOp.compute(in,
						Views.interval(finalRotatedKernelA, kernelInterval), out);
				rotatedKernelA = kernelA;
			}
		}

	}

	/**
	 * TODO
	 *
	 * @param input
	 * @param dimension
	 * @param output
	 */
	@Override
	public void compute(RandomAccessibleInterval<T> input, final Integer dimension,
			RandomAccessibleInterval<T> output) {
		setupConvolves(input, dimension);
		RandomAccessibleInterval<T> in = input;
		for (int i = input.numDimensions() - 1; i >= 0; i--) {
			RandomAccessibleInterval<T> derivative = createRAI.apply(input);
			if (dimension == i) {
				kernelBConvolveOp.compute(Views.interval(Views.extendMirrorDouble(in), input), derivative);
			} else {
				kernelAConvolveOps[i].compute(Views.interval(Views.extendMirrorDouble(in), input), derivative);
			}
			in = derivative;
		}
		addOp.compute(output, in, output);
	}

}
