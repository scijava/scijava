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

package net.imagej.ops2.filter.gauss;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gauss3.SeparableSymmetricConvolution;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * {@link OpCollection} containing various wrappings of Gaussian operations.
 * 
 * @author Gabriel Selzer
 *
 * @param <T>
 *            the input type
 */
@Plugin(type = OpCollection.class)
public class Gaussians<T extends NumericType<T> & NativeType<T>> {

	/**
	 * Gaussian filter, wrapping {@link Gauss3} of imglib2-algorithms. Note that we
	 * put this at a lower priority so then if the input is a
	 * {@link RandomAccessibleInterval} we match to
	 * {@link Gaussians#defaultGaussRAISimple} instead.
	 *
	 * @author Stephan Saalfeld
	 * @author Christian Dietz (University of Konstanz)
	 * @param <T>
	 *            type of input and output
	 */
	@OpField(names = "filter.gauss", priority = Priority.LOW, params = "input, executorService, sigmas, output")
	public final Computers.Arity3<RandomAccessible<T>, ExecutorService, double[], RandomAccessibleInterval<T>> defaultGaussRA = (input,
			es, sigmas, output) -> {
		try {
			SeparableSymmetricConvolution.convolve(Gauss3.halfkernels(sigmas), input, output,
					es);
		} catch (final IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}
	};

	/**
	 * Gaussian filter, wrapping {@link Gauss3} of imglib2-algorithms.
	 *
	 * @author Christian Dietz (University of Konstanz)
	 * @author Stephan Saalfeld
	 * @param <T>
	 *            type of input and output
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@OpField(names = "filter.gauss", params = "input, executorService, sigmas, outOfBoundsFactory, output")
	public final Computers.Arity4<RandomAccessibleInterval<T>, ExecutorService, double[], //
			OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> defaultGaussRAI = (input,
					es, sigmas, outOfBounds, output) -> {

				final RandomAccessible<FloatType> eIn = //
						(RandomAccessible) Views.extend(input, outOfBounds);

				try {
					SeparableSymmetricConvolution.convolve(Gauss3.halfkernels(sigmas), eIn, output,
							es);
				} catch (final IncompatibleTypeException e) {
					throw new RuntimeException(e);
				}
			};

	// -- Convenience Ops -- //

	/**
	 * Performs the above gaussian convolution with a reasonable
	 * {@link OutOfBoundsFactory}, in the case that the user does not provide one.
	 * 
	 * @author Gabriel Selzer
	 *
	 * @param <T>
	 *            type of input
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@OpField(names = "filter.gauss", params = "input, executorService, sigmas, output")
	public final Computers.Arity3<RandomAccessibleInterval<T>, ExecutorService, double[], RandomAccessibleInterval<T>> defaultGaussRAISimple = (
			input, es, sigmas, output) -> defaultGaussRAI.compute(input, es, sigmas,
					new OutOfBoundsMirrorFactory<>(Boundary.SINGLE), output);

	/**
	 * Gaussian filter which can be called with single sigma, i.e. the sigma is the
	 * same in each dimension.
	 *
	 * @author Christian Dietz (University of Konstanz)
	 * @author Stephan Saalfeld
	 * @param <T>
	 *            type of input
	 */
	@OpField(names = "filter.gauss", params = "input, executorService, sigma, outOfBoundsFactory, output")
	public final Computers.Arity4<RandomAccessibleInterval<T>, ExecutorService, Double, //
			OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> //
	gaussRAISingleSigma = (input, es, sigma, outOfBounds, output) -> { //
		final double[] sigmas = new double[input.numDimensions()];
		Arrays.fill(sigmas, sigma);
		defaultGaussRAI.compute(input, es, sigmas, outOfBounds, output);
	};

	/**
	 * Gaussian filter which can be called with single sigma, i.e. the sigma is the
	 * same in each dimension. Used when the user does not provide an
	 * {@link OutOfBoundsFactory}
	 *
	 * @author Gabriel Selzer
	 * @param <T>
	 *            type of input
	 */
	@OpField(names = "filter.gauss", params = "input, executorService, sigma, output")
	public final Computers.Arity3<RandomAccessibleInterval<T>, ExecutorService, Double, RandomAccessibleInterval<T>> gaussRAISingleSigmaSimple = (
			input, es, sigma, output) -> gaussRAISingleSigma.compute(input, es, sigma,
					new OutOfBoundsMirrorFactory<>(Boundary.SINGLE), output);
}
