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

package org.scijava.ops.image.features.zernike;

import org.scijava.ops.image.util.BigComplex;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;

/**
 * Computes a specific zernike moment
 *
 * @author Andreas Graumann (University of Konstanz)
 * @implNote op names='features.zernike.computer'
 */
public class ZernikeComputer<T extends RealType<T>> implements
	Functions.Arity3<IterableInterval<T>, Integer, Integer, ZernikeMoment>
{

	/**
	 * TODO
	 *
	 * @param ii the {@link IterableInterval} to iterate over
	 * @param order the order of the moment to compute
	 * @param repetition the repetition of the moment to compute
	 * @return the zernike moment
	 */
	@Override
	public ZernikeMoment apply(final IterableInterval<T> ii, final Integer order,
		final Integer repetition)
	{

		final var width2 = (ii.dimension(0) - 1) / 2.0;
		final var height2 = (ii.dimension(1) - 1) / 2.0;

		final var centerX = width2 + ii.min(0);
		final var centerY = height2 + ii.min(1);

		final var radius = Math.sqrt(width2 * width2 + height2 * height2);

		// Compute pascal's triangle for binomial coefficients: d[x][y] equals (x
		// over y)
		final var d = computePascalsTriangle(order);

		// initialize zernike moment
		final var moment = initZernikeMoment(order, repetition, d);

		// get the cursor of the iterable interval
		final Cursor<? extends RealType<?>> cur = ii.localizingCursor();

		// run over iterable interval
		while (cur.hasNext()) {
			cur.fwd();

			// get 2d centered coordinates
			final var x = (int) (cur.getIntPosition(0) - ii.min(0));
			final var y = (int) (cur.getIntPosition(1) - ii.min(1));

			final var xm = (x - centerX) / radius;
			final var ym = (y - centerY) / radius;

			final var r = Math.sqrt(xm * xm + ym * ym);
			if (r <= 1 && cur.get().getRealDouble() != 0.0) {
				// calculate theta for this position
				final var theta = Math.atan2(xm, ym);
				moment.getZm().add(multiplyExp(1, moment.getP().evaluate(r), theta,
					moment.getM()));
			}
		}
		// normalization
		normalize(moment.getZm(), moment.getN(), getNumberOfPixelsInUnitDisk(
			radius));
		return moment;
	}

	/**
	 * Computes the number of whole pixels within a disk with radius r. This is
	 * based on Gauss's Circle Problem.
	 * http://mathworld.wolfram.com/GausssCircleProblem.html
	 *
	 * @param r the radius
	 * @return number of pixels within the disk
	 */
	private long getNumberOfPixelsInUnitDisk(final double r) {
		long tmp = 0;
		for (var i = 1; i <= Math.floor(r); i++) {
			tmp += Math.floor(Math.sqrt(r * r - i * i));
		}

		return (long) (1 + 4 * Math.floor(r)) + 4 * tmp;
	}

	/**
	 * Multiplication of pixel * rad * exp(-m*theta) using Euler's formula
	 * (pixel*rad) * (cos(m*theta) - i*sin(m*theta))
	 *
	 * @param pixel Current pixel
	 * @param rad Computed value of radial polynom,
	 * @param theta Angle of current position
	 * @param m Repetition m
	 * @return Computed term
	 */
	private BigComplex multiplyExp(final double pixel, final double rad,
		final double theta, final int m)
	{
        var c = new BigComplex();
		c.setReal(pixel * rad * Math.cos(m * theta));
		c.setImag(-(pixel * rad * Math.sin(m * theta)));
		return c;
	}

	/**
	 * Normalization of all calculated zernike moments in complex representation
	 *
	 * @param complex Complex representation of zernike moment
	 * @param n Order n
	 * @param count Number of pixel within unit circle
	 */
	private void normalize(final BigComplex complex, final int n,
		final long count)
	{
		complex.setReal(complex.getRealDouble() * (n + 1) / count);
		complex.setImag(complex.getImaginaryDouble() * (n + 1) / count);
	}

	/**
	 * Initialize a zernike moment with a given order and repetition
	 *
	 * @param o Order n
	 * @param repetition Repetition m
	 * @param d Pascal matrix
	 * @return Empty Zernike moment of order n and repetition m
	 */
	private ZernikeMoment initZernikeMoment(final int o, final int repetition,
		final double[][] d)
	{

		if (o - Math.abs(repetition) % 2 != 0) {
			// throw new IllegalArgumentException("This combination of order an
			// repetition is not valid!");
		}

		return createZernikeMoment(d, o, repetition);
	}

	/**
	 * Create one zernike moment of order n and repetition m with suitable radial
	 * polynom
	 *
	 * @param d Pascal matrix
	 * @param n Order n
	 * @param m Repetition m
	 * @return Empty Zernike moment of order n and repetition m
	 */
	private ZernikeMoment createZernikeMoment(double[][] d, int n, int m) {
        var p = new ZernikeMoment();
		p.setM(m);
		p.setN(n);
		p.setP(createRadialPolynom(n, m, d));
        var complexNumber = new BigComplex();
		p.setZm(complexNumber);
		return p;
	}

	/**
	 * Efficient calculation of pascal's triangle up to order max
	 *
	 * @param max maximal order of pascal's triangle
	 * @return pascal's triangle
	 */
	private double[][] computePascalsTriangle(int max) {
        var d = new double[max + 1][max + 1];
		for (var n = 0; n <= max; n++) {
			for (var k = 0; k <= n; k++) {
				if (n == 0 && k == 0 || n == k || k == 0) {
					d[n][k] = 1.0;
					continue;
				}
				d[n][k] = (double) n / (n - k) * d[n - 1][k];
			}
		}
		return d;
	}

	/**
	 * @param n Order n
	 * @param m Repetition m
	 * @param k Radius k
	 * @param d Pascal matrix
	 * @return computed term
	 */
	public static int computeBinomialFactorial(final int n, final int m,
		final int k, double[][] d)
	{
        var fac1 = (int) d[n - k][k];
        var fac2 = (int) d[n - 2 * k][(n - m) / 2 - k];
        var sign = (int) Math.pow(-1, k);

		return sign * fac1 * fac2;
	}

//	public void setOrder(int order) {
//		this.order = order;
//	}
//
//	public void setRepetition(int repetition) {
//		this.repetition = repetition;
//	}

	/**
	 * Creates a radial polynom for zernike moment with order n and repetition m
	 *
	 * @param n Order n
	 * @param m Repetition m
	 * @param d Pascal matrix
	 * @return Radial polnom for moment of order n and repetition m
	 */
	public static Polynom createRadialPolynom(final int n, final int m,
		final double[][] d)
	{
		final var result = new Polynom(n);
		for (var s = 0; s <= (n - Math.abs(m)) / 2; ++s) {
			final var pos = n - 2 * s;
			result.setCoefficient(pos, computeBinomialFactorial(n, m, s, d));
		}
		return result;
	}

}
