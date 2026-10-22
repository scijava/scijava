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

package org.scijava.ops.image.filter.tv;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import org.scijava.function.Computers;
import org.scijava.ops.image.filter.FlatImage;
import org.scijava.ops.spi.Nullable;

/**
 * Total variation (Rudin-Osher-Fatemi) denoising in <i>n</i> dimensions, using
 * the dual projection algorithm of Chambolle (2004).
 * <p>
 * The output {@code u} approximates the minimizer of
 * {@code TV(u) + ||u - f||^2 / (2 lambda)}, where {@code f} is the input and
 * {@code TV} is the (isotropic) total variation. Larger values of {@code lambda}
 * remove more noise; {@code lambda} is in units of intensity, and a flat region of
 * {@code k} pixels next to an edge moves by at most about {@code lambda / k}
 * towards its neighbour. Each iteration updates the dual field {@code p} as
 * </p>
 *
 * <pre>
 * g = grad(div p - f / lambda)
 * p = (p + tau g) / (1 + tau |g|)
 * </pre>
 * <p>
 * and the result is {@code u = f - lambda div p}. Gradients are forward
 * differences with Neumann boundary conditions, and {@code div} is their
 * negative adjoint, so the mean intensity is preserved exactly. Convergence is
 * guaranteed for {@code tau <= 1 / (4n)}.
 * </p>
 * <p>
 * Implemented from the paper. The ROF_Denoise plugin of Fiji (GPL) uses the
 * same algorithm with different boundary handling, so results differ near the
 * image border and slightly elsewhere.
 * </p>
 * <p>
 * Results are clamped to the range of the output type, so an integer output
 * saturates rather than wrapping around.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @see <a href="https://doi.org/10.1023/B:JMIV.0000011325.36760.1e">A.
 *      Chambolle, "An algorithm for total variation minimization and
 *      applications", J. Math. Imaging Vis. 20:89-97, 2004</a>
 * @see <a href="https://doi.org/10.1016/0167-2789(92)90242-F">L. I. Rudin, S.
 *      Osher and E. Fatemi, "Nonlinear total variation based noise removal
 *      algorithms", Physica D 60:259-268, 1992</a>
 * @implNote op names='filter.tvDenoise'
 */
public class DefaultTVDenoise<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity4<RandomAccessibleInterval<T>, Double, Integer, Double, RandomAccessibleInterval<V>>
{

	/**
	 * Applies total variation denoising.
	 *
	 * @param input the image to denoise
	 * @param lambda the regularization weight, in units of intensity (larger
	 *          values smooth more)
	 * @param iterations the number of iterations (default: 50)
	 * @param tau the step size (default: 1 / (4n) for an n-dimensional image,
	 *          the largest step with guaranteed convergence)
	 * @param output the denoised image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Double lambda, @Nullable final Integer iterations,
		@Nullable final Double tau, final RandomAccessibleInterval<V> output)
	{
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		if (!(lambda > 0)) {
			throw new IllegalArgumentException("Lambda must be positive: " + lambda);
		}
		final var n = input.numDimensions();
		final int iters = iterations == null ? 50 : iterations;
		final double step = tau == null ? 1.0 / (4 * n) : tau;
		if (!(step > 0)) {
			throw new IllegalArgumentException("Tau must be positive: " + step);
		}
		final var f = FlatImage.read(input);
		final var count = f.length;
		final var dims = input.dimensionsAsLongArray();
		final var strides = FlatImage.strides(dims);

		final var p = new double[n][count];
		final var divP = new double[count];
		final var w = new double[count];
		final var g = new double[n];
		final var pos = new long[n];
		for (var it = 0; it < iters; it++) {
			for (var i = 0; i < count; i++)
				w[i] = divP[i] - f[i] / lambda;
			Arrays.fill(pos, 0);
			for (var i = 0; i < count; i++) {
				var norm = 0.0;
				for (var d = 0; d < n; d++) {
					g[d] = pos[d] < dims[d] - 1 ? w[i + strides[d]] - w[i] : 0;
					norm += g[d] * g[d];
				}
				final var denom = 1 + step * Math.sqrt(norm);
				for (var d = 0; d < n; d++)
					p[d][i] = (p[d][i] + step * g[d]) / denom;
				FlatImage.increment(pos, dims);
			}
			divergence(p, dims, strides, pos, divP);
		}

		final var u = new double[count];
		for (var i = 0; i < count; i++)
			u[i] = f[i] - lambda * divP[i];
		FlatImage.write(u, output);
	}

	/** Computes the discrete divergence, the negative adjoint of the gradient. */
	private static void divergence(final double[][] p, final long[] dims,
		final int[] strides, final long[] pos, final double[] out)
	{
		final var n = dims.length;
		Arrays.fill(pos, 0);
		for (var i = 0; i < out.length; i++) {
			var div = 0.0;
			for (var d = 0; d < n; d++) {
				if (pos[d] < dims[d] - 1) div += p[d][i];
				if (pos[d] > 0) div -= p[d][i - strides[d]];
			}
			out[i] = div;
			FlatImage.increment(pos, dims);
		}
	}
}
