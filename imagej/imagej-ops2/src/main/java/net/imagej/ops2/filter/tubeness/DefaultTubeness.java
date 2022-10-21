/*-
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

package net.imagej.ops2.filter.tubeness;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gradient.HessianMatrix;
import net.imglib2.algorithm.linalg.eigen.TensorEigenValues;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * The Tubeness filter: enhance filamentous structures of a specified thickness.
 * <p>
 * This filter works on 2D and 3D image exclusively and produces a score for how
 * "tube-like" each point in the image is. This is useful as a preprocessing
 * step for tracing neurons or blood vessels, for example. For 3D image stacks,
 * the filter uses the eigenvalues of the Hessian matrix to calculate this
 * measure of "tubeness", using one of the simpler metrics me mentioned in
 * <u>Sato et al 1997</u>: if the larger two eigenvalues (λ₂ and λ₃) are both
 * negative then value is √(λ₂λ₃), otherwise the value is 0. For 2D images, if
 * the large eigenvalue is negative, we return its absolute value and otherwise
 * return 0.
 * <ul>
 * <li>Source image is filtered first by a gaussian with 𝜎 that sets its scale.</li>
 * <li>The the Hessian matrix is calculated for each pixel.</li>
 * <li>We yield the eigenvalues of the Hessian matrix. The output of the
 * tubeness filter is a combination of these eigenvalues:</li>
 * <ul>
 * <li>in 2D where {@code λ₂} is the largest eigenvalue:
 * {@code out = 𝜎 × 𝜎 × |λ₂|} if {@code λ₂} is negative, 0
 * otherwise.</li>
 * <li>in 3D where {@code λ₂} and {@code λ₃} are the largest
 * eigenvalues:, {@code out = 𝜎 × 𝜎 × sqrt( λ₂ * λ₃ )} if {@code λ₂}
 * and {@code λ₃} are negative, 0 otherwise.</li>
 * </ul>
 * </ul>
 * This results in enhancing filaments of roughly {@code 𝜎 / sqrt(d)}
 * thickness.
 * <p>
 * Port of the tubeness filter of the VIB package, with original authors Mark
 * Longair and Stephan Preibisch, to ImageJ-ops.
 *
 * @see <a href=
 *      "https://github.com/fiji/VIB/blob/master/src/main/java/features/Tubeness_.java">Tubeness
 *      VIB plugin code</a>
 * @author Jean-Yves Tinevez
 * @param <T>
 *            the type of the source pixels. Must extends {@link RealType}.
 *@implNote op names='filter.tubeness'
 */
public class DefaultTubeness<T extends RealType<T>> implements
	Computers.Arity4<RandomAccessibleInterval<T>, ExecutorService, Double, double[], IterableInterval<DoubleType>>
{

	@OpDependency(name = "create.imgFactory")
	private Function<Dimensions, ImgFactory<DoubleType>> createFactoryOp;

	//TODO: make sure this works
	@OpDependency(name = "project")
	private Computers.Arity3<RandomAccessibleInterval<DoubleType>, Computers.Arity1<Iterable<DoubleType>, DoubleType>, Integer, IterableInterval<DoubleType>> projector;

	@Override
	public void compute(final RandomAccessibleInterval<T> input, ExecutorService es, final Double sigma,
		final double[] calibration, final IterableInterval<DoubleType> tubeness)
	{

		final int numDimensions = input.numDimensions();
		// Sigmas in pixel units.
		final double[] sigmas = new double[numDimensions];
		for (int d = 0; d < sigmas.length; d++) {
			final double cal = d < calibration.length ? calibration[d] : 1;
			sigmas[d] = sigma / cal;
		}

		/*
		 * Hessian.
		 */

		// Get a suitable image factory.
		final long[] gradientDims = new long[numDimensions + 1];
		final long[] hessianDims = new long[numDimensions + 1];
		for (int d = 0; d < numDimensions; d++) {
			hessianDims[d] = input.dimension(d);
			gradientDims[d] = input.dimension(d);
		}
		hessianDims[numDimensions] = numDimensions * (numDimensions + 1) / 2;
		gradientDims[numDimensions] = numDimensions;
		final Dimensions hessianDimensions = FinalDimensions.wrap(hessianDims);
		final FinalDimensions gradientDimensions = FinalDimensions.wrap(gradientDims);
		final ImgFactory<DoubleType> factory = createFactoryOp.apply(hessianDimensions);
		final Img<DoubleType> hessian = factory.create(hessianDimensions, new DoubleType());
		final Img<DoubleType> gradient = factory.create(gradientDimensions, new DoubleType());
		final Img<DoubleType> gaussian = factory.create(input, new DoubleType());

		// Handle multithreading.
		final int nThreads = Runtime.getRuntime().availableProcessors();

		try {
			// Hessian calculation.
			HessianMatrix.calculateMatrix(Views.extendBorder(input), gaussian, gradient, hessian,
					new OutOfBoundsBorderFactory<>(), nThreads, es, sigma);

			// Hessian eigenvalues.
			final RandomAccessibleInterval<DoubleType> evs = TensorEigenValues.calculateEigenValuesSymmetric(hessian,
					TensorEigenValues.createAppropriateResultImg(hessian, factory, new DoubleType()), nThreads, es);

			final Computers.Arity1<Iterable<DoubleType>, DoubleType> method;
			switch (numDimensions) {
			case 2:
				method = new Tubeness2D(sigma);
				break;
			case 3:
				method = new Tubeness3D(sigma);
				break;
			default:
				System.err.println("Cannot compute tubeness for " + numDimensions + "D images.");
				return;
			}
			projector.compute(evs, method, numDimensions, tubeness);

			return;
		} catch (final IncompatibleTypeException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return;
		}
	}

	private static final class Tubeness2D implements Computers.Arity1<Iterable<DoubleType>, DoubleType> {

		private final double sigma;

		public Tubeness2D(final double sigma) {
			this.sigma = sigma;
		}

		@Override
		public void compute(final Iterable<DoubleType> input, final DoubleType output) {
			// Use just the largest one.
			final Iterator<DoubleType> it = input.iterator();
			it.next();
			final double val = it.next().get();
			if (val >= 0.)
				output.setZero();
			else
				output.set(sigma * sigma * Math.abs(val));
		}
	}

	private static final class Tubeness3D implements Computers.Arity1<Iterable<DoubleType>, DoubleType> {

		private final double sigma;

		public Tubeness3D(final double sigma) {
			this.sigma = sigma;
		}

		@Override
		public void compute(final Iterable<DoubleType> input, final DoubleType output) {
			// Use the two largest ones.
			final Iterator<DoubleType> it = input.iterator();
			it.next();
			final double val1 = it.next().get();
			final double val2 = it.next().get();
			if (val1 >= 0. || val2 >= 0.)
				output.setZero();
			else
				output.set(sigma * sigma * Math.sqrt(val1 * val2));
		}
	}
}

/**
 *@implNote op names='filter.tubeness'
 */
class DefaultTubenessWithoutCalibration<T extends RealType<T>> implements
		Computers.Arity3<RandomAccessibleInterval<T>, ExecutorService, Double, IterableInterval<DoubleType>> {

	@OpDependency(name = "filter.tubeness")
	Computers.Arity4<RandomAccessibleInterval<T>, ExecutorService, Double, double[], IterableInterval<DoubleType>> tubenessOp;

	@Override
	public void compute(RandomAccessibleInterval<T> in1, ExecutorService in2, Double in3,
			IterableInterval<DoubleType> out) {
		tubenessOp.compute(in1, in2, in3, new double[] {}, out);
	}
}
