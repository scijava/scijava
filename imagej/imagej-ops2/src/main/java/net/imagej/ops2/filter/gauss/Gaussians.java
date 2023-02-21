/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

import org.scijava.ops.spi.OpCollection;

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
import org.scijava.ops.spi.Optional;

/**
 * {@link OpCollection} containing various wrappings of Gaussian operations.
 * 
 * @author Gabriel Selzer
 */
public class Gaussians {

	/**
	 * Gaussian filter, wrapping {@link Gauss3} of imglib2-algorithms.
	 *
	 * @author Christian Dietz (University of Konstanz)
	 * @author Stephan Saalfeld
	 * @param <T> type of input and output
	 * @param input the input image
	 * @param es the {@link ExecutorService}
	 * @param sigmas the sigmas for the gaussian
	 * @param outOfBounds the {@link OutOfBoundsFactory} that defines how the
	 *          calculation is affected outside the input bounds. (required = false)
	 * @param output the output image
	 * @implNote op names='filter.gauss',
	 *           type='org.scijava.function.Computers$Arity4'
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends NumericType<T> & NativeType<T>> void defaultGaussRAI(
		final RandomAccessibleInterval<T> input, //
		final ExecutorService es, //
		final double[] sigmas, //
		@Optional OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBounds, //
		final RandomAccessibleInterval<T> output //
	) {
		if (outOfBounds == null)
			 outOfBounds = new OutOfBoundsMirrorFactory<>(Boundary.SINGLE);

		final RandomAccessible<FloatType> eIn = //
			(RandomAccessible) Views.extend(input, outOfBounds);

		try {
			SeparableSymmetricConvolution.convolve(Gauss3.halfkernels(sigmas), eIn,
				output, es);
		}
		catch (final IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}
	}

	// -- Convenience Ops -- //
	/**
	 * Gaussian filter which can be called with single sigma, i.e. the sigma is
	 * the same in each dimension.
	 *
	 * @author Christian Dietz (University of Konstanz)
	 * @author Stephan Saalfeld
	 * @param <T> type of input
	 * @param input the input image
	 * @param es the {@link ExecutorService}
	 * @param sigma the sigmas for the Gaussian
	 * @param outOfBounds the {@link OutOfBoundsFactory} that defines how the
	 *          calculation is affected outside the input bounds. (required = false)
	 * @param output the preallocated output image
	 * @implNote op names='filter.gauss',
	 *           type='org.scijava.function.Computers$Arity4'
	 */
	public static <T extends NumericType<T> & NativeType<T>> void
		gaussRAISingleSigma( //
			final RandomAccessibleInterval<T> input, //
			final ExecutorService es, //
			final Double sigma, //
			@Optional OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBounds, //
			final RandomAccessibleInterval<T> output //
	) {
		final double[] sigmas = new double[input.numDimensions()];
		Arrays.fill(sigmas, sigma);
		defaultGaussRAI(input, es, sigmas, outOfBounds, output);
	};
}
