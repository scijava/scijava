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

package org.scijava.ops.image.filter.diffusion;

import java.util.Arrays;
import java.util.Locale;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import org.scijava.function.Computers;
import org.scijava.ops.image.filter.FlatImage;
import org.scijava.ops.spi.Nullable;

/**
 * Perona-Malik anisotropic diffusion in <i>n</i> dimensions.
 * <p>
 * Each iteration performs one explicit step of the nonlinear diffusion
 * equation {@code dI/dt = div(g(|grad I|) grad I)} on the 2n nearest
 * neighbours of each pixel:
 * </p>
 *
 * <pre>
 * I(x) += dt * sum over neighbours y of g(|I(y) - I(x)|) * (I(y) - I(x))
 * </pre>
 * <p>
 * The conductance {@code g} is close to 1 for differences much smaller than
 * {@code kappa}, so noise is smoothed, and close to 0 for larger differences,
 * so edges are preserved. The two conductance functions of Perona and Malik
 * are available: {@code "exponential"}, {@code g(s) = exp(-(s/kappa)^2)},
 * which favours high-contrast edges, and {@code "quadratic"},
 * {@code g(s) = 1 / (1 + (s/kappa)^2)}, which favours wide regions over
 * smaller ones. There is no flux across the image border, so the total
 * intensity is preserved. The scheme is stable for {@code dt <= 1 / (2n)}.
 * </p>
 * <p>
 * Implemented from the paper. Note: Fiji's "Anisotropic Diffusion 2D" plugin
 * implements a different algorithm (Tschumperl&eacute;-Deriche tensor-driven
 * diffusion), hence the more specific name {@code filter.peronaMalik}.
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
 * @see <a href="https://doi.org/10.1109/34.56205">P. Perona and J. Malik,
 *      "Scale-space and edge detection using anisotropic diffusion", IEEE
 *      Trans. Pattern Anal. Mach. Intell. 12(7):629-639, 1990</a>
 * @implNote op names='filter.peronaMalik'
 */
public class PeronaMalikDiffusion<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity5<RandomAccessibleInterval<T>, Double, Integer, Double, String, RandomAccessibleInterval<V>>
{

	/**
	 * Applies Perona-Malik diffusion.
	 *
	 * @param input the image to filter
	 * @param kappa the gradient threshold, in units of intensity: differences
	 *          well above kappa are treated as edges
	 * @param iterations the number of iterations (default: 10)
	 * @param dt the time step, at most 1 / (2n) for an n-dimensional image
	 *          (default: 1 / (4n))
	 * @param conductance the conductance function, "exponential" or "quadratic"
	 *          (default: "exponential")
	 * @param output the filtered image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Double kappa, @Nullable final Integer iterations,
		@Nullable final Double dt, @Nullable final String conductance,
		final RandomAccessibleInterval<V> output)
	{
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		if (!(kappa > 0)) {
			throw new IllegalArgumentException("Kappa must be positive: " + kappa);
		}
		final var n = input.numDimensions();
		final int iters = iterations == null ? 10 : iterations;
		final double step = dt == null ? 1.0 / (4 * n) : dt;
		if (!(step > 0) || step > 1.0 / (2 * n)) {
			throw new IllegalArgumentException("Time step must be in (0, 1/" + 2 *
				n + "] for stability: " + step);
		}
		final boolean exponential;
		final var g = conductance == null ? "exponential" : conductance
			.toLowerCase(Locale.ROOT);
		if (g.equals("exponential")) exponential = true;
		else if (g.equals("quadratic")) exponential = false;
		else throw new IllegalArgumentException(
			"Conductance must be exponential or quadratic: " + conductance);

		var current = FlatImage.read(input);
		final var count = current.length;
		var next = new double[count];
		final var dims = input.dimensionsAsLongArray();
		final var strides = FlatImage.strides(dims);

		final var invKappaSq = 1 / (kappa * kappa);
		final var pos = new long[n];
		for (var it = 0; it < iters; it++) {
			System.arraycopy(current, 0, next, 0, count);
			Arrays.fill(pos, 0);
			for (var i = 0; i < count; i++) {
				final var v = current[i];
				for (var d = 0; d < n; d++) {
					// Note: Each neighbour pair is visited once, from its lower pixel,
					// and the flux is applied to both pixels.
					if (pos[d] < dims[d] - 1) {
						final var j = i + strides[d];
						final var diff = current[j] - v;
						final var s = diff * diff * invKappaSq;
						final var flux = step * diff * (exponential ? Math.exp(-s) : 1 /
							(1 + s));
						next[i] += flux;
						next[j] -= flux;
					}
				}
				FlatImage.increment(pos, dims);
			}
			final var tmp = current;
			current = next;
			next = tmp;
		}

		FlatImage.write(current, output);
	}
}
