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

package net.imagej.ops2.filter.dog;

import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import org.scijava.function.Computers;
import org.scijava.ops.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Difference of Gaussians (DoG) implementation where sigmas can vary by
 * dimension.
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 */
@Plugin(type = Op.class, name = "filter.DoG")
public class DoGVaryingSigmas<T extends NumericType<T> & NativeType<T>> implements
		Computers.Arity5<RandomAccessibleInterval<T>, double[], double[], OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, ExecutorService, RandomAccessibleInterval<T>> {

	@OpDependency(name = "filter.gauss")
	public Computers.Arity4<RandomAccessibleInterval<T>, ExecutorService, double[], //
			OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> defaultGaussRA;

	@OpDependency(name = "filter.DoG")
	private Computers.Arity3<RandomAccessibleInterval<T>, Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>, //
			Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> dogOp;

	//TODO: make the outOfBoundsFactory optional (see DoGTest for the default).
	/**
	 * TODO
	 *
	 * @param input
	 * @param sigmas1
	 * @param sigmas2
	 * @param outOfBoundsFactory
	 * @param executorService
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> t, final double[] sigmas1, //
			final double[] sigmas2, final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> fac,
			final ExecutorService es, final RandomAccessibleInterval<T> output) {
		if (sigmas1.length != sigmas2.length || sigmas1.length != t.numDimensions())
			throw new IllegalArgumentException("Do not have enough sigmas to apply to each dimension of the input!");

		Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gauss1 = (in, out) -> defaultGaussRA
				.compute(in, es, sigmas1, fac, out);
		Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gauss2 = (in, out) -> defaultGaussRA
				.compute(in, es, sigmas2, fac, out);

		dogOp.compute(t, gauss1, gauss2, output);
	}

}

@Plugin(type = Op.class, name = "filter.DoG")
class DoGSingleSigma<T extends NumericType<T> & NativeType<T>> implements
		Computers.Arity5<RandomAccessibleInterval<T>, Double, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, ExecutorService, RandomAccessibleInterval<T>> {

	@OpDependency(name = "filter.DoG")
	private Computers.Arity5<RandomAccessibleInterval<T>, double[], double[], //
			OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, ExecutorService, RandomAccessibleInterval<T>> dogOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param sigma1
	 * @param sigma2
	 * @param outOfBoundsFactory
	 * @param executorService
	 * @param output
	 */
	@Override
	public void compute(RandomAccessibleInterval<T> in1, Double in2, Double in3,
			OutOfBoundsFactory<T, RandomAccessibleInterval<T>> in4, ExecutorService in5,
			RandomAccessibleInterval<T> out) {
		double[] sigmas1 = new double[in1.numDimensions()];
		double[] sigmas2 = new double[in1.numDimensions()];
		for (int i = 0; i < in1.numDimensions(); i++) {
			sigmas1[i] = in2;
			sigmas2[i] = in3;
		}

		dogOp.compute(in1, sigmas1, sigmas2, in4, in5, out);

	}

}
