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

package org.scijava.ops.image.filter.derivativeGauss;

import java.util.function.BiFunction;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Performs the 2-D partial derivative Gaussian kernel convolutions on an image,
 * at a particular point.
 *
 * @author Gabriel Selzer
 * @implNote op names='filter.derivativeGauss'
 */
public class DefaultDerivativeGauss<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, double[], int[], RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "copy.rai")
	private Computers.Arity1<RandomAccessibleInterval<DoubleType>, RandomAccessibleInterval<DoubleType>> copyOp;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	double SQRT2PI = Math.sqrt(2 * Math.PI);

	/**
	 * Calculates a value at a specified location in a normal mask.
	 *
	 * @param x - the location in the mask.
	 * @param sigma - the sigma for the convolution.
	 * @return double - the value of the mask at location x.
	 */
	private double phi0(final double x, final double sigma) {
		final var t = x / sigma;
		return -sigma * Math.exp(-0.5 * t * t) / (SQRT2PI * x);
	}

	/**
	 * Calculates a value at a specified location in a first partial derivative
	 * mask.
	 *
	 * @param x - the location in the mask.
	 * @param sigma - the sigma for the convolution.
	 * @return double - the value of the mask at location x.
	 */
	private double phi1(final double x, final double sigma) {
		final var t = x / sigma;
		return Math.exp(-0.5 * t * t) / (SQRT2PI * sigma);
	}

	/**
	 * Calculates a value at a specified location in a second partial derivative
	 * mask.
	 *
	 * @param x - the location in the mask.
	 * @param sigma - the sigma for the convolution.
	 * @return double - the value of the mask at location x.
	 */
	private double phi2(final double x, final double sigma) {
		final var t = x / sigma;
		return -x * Math.exp(-0.5 * t * t) / (SQRT2PI * Math.pow(sigma, 3));
	}

	/**
	 * Creates the mask for normal convolutions
	 *
	 * @param sigma - The sigma for the convolution.
	 * @return double[] - The mask.
	 */
	private double[] get_mask_0(final double sigma) {

		final var x = (int) Math.ceil(4 * sigma);
		final var h = new double[2 * x + 1];

		for (var i = -x + 1; i < x; i++) {
			h[i + x] = Math.abs(phi0(i + 0.5, sigma) - phi0(i - 0.5, sigma));
		}
		h[0] = phi0(-x + 0.5, sigma);
		h[h.length - 1] = phi0(-x + 0.5, sigma);
		return h;
	}

	/**
	 * Creates the mask for first partial derivative convolutions
	 *
	 * @param sigma - The sigma for the convolution.
	 * @return double[] - The mask.
	 */
	private double[] get_mask_1(final double sigma) {

		final var x = (int) Math.ceil(4 * sigma);
		final var h = new double[2 * x + 1];

		for (var i = -x + 1; i < x; i++) {
			h[i + x] = phi1(-i + 0.5, sigma) - phi1(-i - 0.5, sigma);
		}
		h[0] = -phi1(x - 0.5, sigma);
		h[h.length - 1] = phi1(-x + 0.5, sigma);
		return h;
	}

	/**
	 * Creates the mask for second partial derivative convolutions
	 *
	 * @param sigma - The sigma for the convolution.
	 * @return double[] - The mask.
	 */
	private double[] get_mask_2(final double sigma) {

		final var x = (int) Math.ceil(4 * sigma);
		final var h = new double[2 * x + 1];

		for (var i = -x + 1; i < x; i++) {
			h[i + x] = phi2(-i + 0.5, sigma) - phi2(-i - 0.5, sigma);
		}
		h[0] = -phi2(-x + 0.5, sigma);
		h[h.length - 1] = phi2(x - 0.5, sigma);
		return h;
	}

	/**
	 * Returns the correct mask of nth partial derivative. Leaves the calculations
	 * to the helper methods.
	 *
	 * @param sigma - The sigma for the convolution.
	 * @param n - A number specifying the nth partial derivative.
	 * @return double[] - The mask.
	 */
	private double[] get_mask_general(final int n, final double sigma) {
		double[] h;
		switch (n) {
			case 0:
				h = get_mask_0(sigma);
				break;
			case 1:
				h = get_mask_1(sigma);
				break;
			case 2:
				h = get_mask_2(sigma);
				break;
			default:
				h = get_mask_0(sigma);
				break;
		}
		return h;
	}

	/**
	 * Convolves the columns of the image
	 *
	 * @param input - The input image.
	 * @param output - The output image.
	 * @param mask - The mask needed for the convolution, determined beforehand.
	 */
	private <T extends RealType<T>> void convolve_x(
		final RandomAccessibleInterval<T> input,
		final RandomAccessibleInterval<DoubleType> output, final double[] mask)
	{
		double sum;
		final var cursor = Views.iterable(input).localizingCursor();
		final var osmf =
			new OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>>(Boundary.SINGLE);
		final RandomAccess<T> inputRA = osmf.create(input);
		final var outputRA = output.randomAccess();

		while (cursor.hasNext()) {
			cursor.fwd();
			inputRA.setPosition(cursor);
			outputRA.setPosition(cursor);
			sum = 0;
			// loop from the bottom of the image to the top
			final var halfWidth = mask.length / 2;
			for (var i = -halfWidth; i <= halfWidth; i++) {
				inputRA.setPosition(cursor.getLongPosition(0) + i, 0);
				inputRA.setPosition(cursor.getLongPosition(1), 1);
				sum += inputRA.get().getRealDouble() * mask[i + halfWidth];
			}
			outputRA.get().setReal(sum);
		}

	}

	/**
	 * Convolves the rows of the image
	 *
	 * @param input - The input image.
	 * @param output - The output image.
	 * @param mask - The mask needed for the convolution, determined beforehand.
	 */
	private <T extends RealType<T>> void convolve_n(
		final RandomAccessibleInterval<T> input,
		final RandomAccessibleInterval<DoubleType> output, final double[] mask,
		final int n)
	{
		double sum;
		final var cursor = Views.iterable(input).localizingCursor();
		final var osmf =
			new OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>>(Boundary.SINGLE);
		final RandomAccess<T> inputRA = osmf.create(input);
		final var outputRA = output.randomAccess();

		while (cursor.hasNext()) {
			cursor.fwd();
			inputRA.setPosition(cursor);
			outputRA.setPosition(cursor);
			sum = 0;
			// loop from the bottom of the image to the top
			final var halfWidth = mask.length / 2;
			for (var i = -halfWidth; i <= halfWidth; i++) {
				for (var dim = 0; dim < input.numDimensions(); dim++) {
                    var position = cursor.getLongPosition(dim);
					if (dim == n) position += i;
					inputRA.setPosition(position, dim);
				}
				sum += inputRA.get().getRealDouble() * mask[i + halfWidth];
			}
			outputRA.get().setReal(sum);
		}
	}

	/**
	 * TODO
	 *
	 * @param input
	 * @param sigma the sigma in each dimension of the gaussian
	 * @param derivatives the value at each index indicates the derivative to take
	 *          in each dimension of the image.
	 * @param output
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void compute(final RandomAccessibleInterval<T> input,
		final double[] sigma, final int[] derivatives,
		final RandomAccessibleInterval<DoubleType> output)
	{

		// throw exception if not enough derivative values were given
		if (input.numDimensions() != derivatives.length)
			throw new IllegalArgumentException(
				"derivatives array must include values for each dimension!");

		// throw exception if derivatives contains a derivative this Op cannot
		// perform
		for (final var derivative : derivatives)
			if (derivative < 0 || derivative > 2) throw new IllegalArgumentException(
				"derivatives greater than second-order or less than zeroth order cannot be performed!");

		// create the intermediate image used as the input for all convolutions
		// after the first
		final var intermediate = createOp.apply(
			input, new DoubleType());

		// convolve the first dimension, transferring data to the intermediary
		convolve_n(input, intermediate, get_mask_general(derivatives[0], sigma[0]),
			0);

		// convolve the remaining dimensions
		for (var n = 1; n < input.numDimensions(); n++) {
			// convolve from the intermediary, outputting to output
			convolve_n(intermediate, output, get_mask_general(derivatives[n],
				sigma[n]), n);
			// if there is still another dimension to convolve, transfer the data from
			// the last convolution back into output so that we can convolve again.
			if (n + 1 != input.numDimensions()) copyOp.compute(output, intermediate);
		}

	}

}
