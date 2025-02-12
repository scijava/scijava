/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.segment.detectRidges;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Helper Method to generate the meta image used for {@link DefaultDetectRidges}
 *
 * @author Gabriel Selzer
 */
public class RidgeDetectionMetadata {

	protected Img<DoubleType> pValues;
	protected Img<DoubleType> nValues;
	protected Img<DoubleType> gradients;

	RandomAccessibleInterval<DoubleType> x;
	RandomAccessibleInterval<DoubleType> y;
	RandomAccessibleInterval<DoubleType> xx;
	RandomAccessibleInterval<DoubleType> xy;
	RandomAccessibleInterval<DoubleType> yy;

	/**
	 * Generates the metadata images from the input image
	 *
	 * @param input - the image to be detected
	 * @param sigma - the sigma for the gaussian derivative convolutions
	 */
	public <T extends RealType<T>> RidgeDetectionMetadata(
		final RandomAccessibleInterval<T> input, final double sigma,
		final double smallMax, final double bigMax,
		final Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<DoubleType>> convertOp,
		final BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp,
		final Function<RandomAccessibleInterval<DoubleType>, RandomAccessibleInterval<DoubleType>> copyOp,
		final Computers.Arity3<RandomAccessibleInterval<DoubleType>, double[], int[], RandomAccessibleInterval<DoubleType>> partialDerivativeOp)
	{
		// convert input to doubleType
		final var converted = createOp.apply(input,
			new DoubleType());
		convertOp.compute(input, converted);

		// create dimensions array for p and n values images, later used for setting
		// positions for their randomAccesses
		final var valuesArr = new long[input.numDimensions() + 1];
		for (var d = 0; d < input.numDimensions(); d++)
			valuesArr[d] = input.dimension(d);
		valuesArr[valuesArr.length - 1] = 2;

		// create metadata images and randomAccesses
		pValues = (Img<DoubleType>) createOp.apply(new FinalDimensions(valuesArr),
			new DoubleType());
		final var pRA = pValues.randomAccess();
		nValues = (Img<DoubleType>) createOp.apply(new FinalDimensions(valuesArr),
			new DoubleType());
		final var nRA = nValues.randomAccess();
		gradients = (Img<DoubleType>) createOp.apply(input, new DoubleType());
		final var gradientsRA = gradients.randomAccess();

		// create a cursor of the input to direct all of the randomAccesses.
		final var cursor = Views.iterable(input).localizingCursor();

		// create partial derivative images, randomAccesses
		x = copyOp.apply(converted);
		final var xRA = x.randomAccess();
		y = copyOp.apply(converted);
		final var yRA = y.randomAccess();
		xx = copyOp.apply(converted);
		final var xxRA = xx.randomAccess();
		xy = copyOp.apply(converted);
		final var xyRA = xy.randomAccess();
		yy = copyOp.apply(converted);
		final var yyRA = yy.randomAccess();

		// fill partial derivative images with gaussian derivative convolutions
		partialDerivativeOp.compute(converted, new double[] { sigma, sigma },
			new int[] { 1, 0 }, x);
		partialDerivativeOp.compute(converted, new double[] { sigma, sigma },
			new int[] { 2, 0 }, xx);
		partialDerivativeOp.compute(converted, new double[] { sigma, sigma },
			new int[] { 1, 1 }, xy);
		partialDerivativeOp.compute(converted, new double[] { sigma, sigma },
			new int[] { 0, 1 }, y);
		partialDerivativeOp.compute(converted, new double[] { sigma, sigma },
			new int[] { 0, 2 }, yy);

		// loop through the points, fill in potentialPoints with second directional
		// derivative across the line, eigenx with the x component of the normal
		// vector to the line, and eigeny with the y component of that vector.
		while (cursor.hasNext()) {
			cursor.fwd();
			xRA.setPosition(cursor);
			yRA.setPosition(cursor);
			xxRA.setPosition(cursor);
			xyRA.setPosition(cursor);
			yyRA.setPosition(cursor);

			// Get all of the values needed for the point.
			final var rx = xRA.get().getRealDouble();
			final var ry = yRA.get().getRealDouble();
			final var rxx = xxRA.get().getRealDouble();
			final var rxy = xyRA.get().getRealDouble();
			final var ryy = yyRA.get().getRealDouble();

			// convolve image with 2D partial kernel,
			// make a Hessian using the kernels
			final var hessian = new Matrix(input.numDimensions(), input
				.numDimensions());
			hessian.set(0, 0, xxRA.get().getRealDouble());
			hessian.set(0, 1, xyRA.get().getRealDouble());
			hessian.set(1, 0, xyRA.get().getRealDouble());
			hessian.set(1, 1, yyRA.get().getRealDouble());

			// Jacobian rotation to eliminate rxy
			final var e = hessian.eig();
			final var eigenvalues = e.getD();
			final var eigenvectors = e.getV();

			// since the eigenvalues matrix is diagonal, find the index of the largest
			// eigenvalue
			final var index = Math.abs(eigenvalues.get(0, 0)) > Math.abs(eigenvalues
				.get(1, 1)) ? 0 : 1;

			// get (nx, ny), i.e. the components of a vector perpendicular to our
			// line, with length of one.
			final var nx = eigenvectors.get(0, index);
			final var ny = eigenvectors.get(1, index);

			// obtain (px, py), the point in subpixel space where the first
			// directional derivative vanishes.
			final var t = -1 * (rx * nx + ry * ny) / (rxx * nx * nx + 2 * rxy *
				nx * ny + ryy * ny * ny);
			final var px = t * nx;
			final var py = t * ny;

			// so long as the absolute values of px and py are below 0.5, this point
			// is a line point.
			if (Math.abs(px) <= 0.5 && Math.abs(py) <= 0.5) {

				// create long array for setting position of pValues and nValues
				valuesArr[0] = cursor.getLongPosition(0);
				valuesArr[1] = cursor.getLongPosition(1);
				valuesArr[2] = 0;

				// set px to the first z slice of pValues
				pRA.setPosition(valuesArr);
				pRA.get().set(px);

				// set py to the second z slice of pValues
				pRA.fwd(2);
				pRA.get().set(py);

				// set nx to the first z slice of nValues
				nRA.setPosition(valuesArr);
				nRA.get().set(nx);

				// set ny to the second z slice of nValues
				nRA.fwd(2);
				nRA.get().set(ny);

				// the eigenvalue is equal to the gradient at that pixel. If a large
				// negative, we are on a line. Otherwise 0.
				final var gradient = eigenvalues.get(index, index) < -smallMax ? Math
					.abs(eigenvalues.get(index, index)) : 0;

				// set the gradient
				gradientsRA.setPosition(cursor);
				gradientsRA.get().set(gradient);
			}

		}
	}

	/**
	 * returns the pValue image
	 *
	 * @return the image containing the pValues
	 */
	protected Img<DoubleType> getPValues() {
		return pValues;
	}

	/**
	 * returns the pValue image's RandomAccess
	 *
	 * @return the RandomAccess containing the pValues
	 */
	protected RandomAccess<DoubleType> getPValuesRandomAccess() {
		return pValues.randomAccess();
	}

	/**
	 * returns the nValue image
	 *
	 * @return the image containing the nValues
	 */
	protected Img<DoubleType> getNValues() {
		return nValues;
	}

	/**
	 * returns the nValue image's RandomAccess
	 *
	 * @return the RandomAccess containing the nValues
	 */
	protected RandomAccess<DoubleType> getNValuesRandomAccess() {
		return nValues.randomAccess();
	}

	/**
	 * returns the gradient image
	 *
	 * @return the image containing the gradients
	 */
	protected Img<DoubleType> getGradients() {
		return gradients;
	}

	/**
	 * returns the gradient image's RandomAccess
	 *
	 * @return the RandomAccess containing the gradients
	 */
	protected RandomAccess<DoubleType> getGradientsRandomAccess() {
		return gradients.randomAccess();
	}
}
