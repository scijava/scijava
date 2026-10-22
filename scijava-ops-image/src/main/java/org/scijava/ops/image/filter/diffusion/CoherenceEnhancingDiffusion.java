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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.image.filter.FlatImage;
import org.scijava.ops.image.filter.tensor.TensorUtils;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Coherence-enhancing diffusion (Weickert 1999) in <i>n</i> dimensions.
 * <p>
 * This nonlinear anisotropic diffusion smooths along the local orientation of
 * flow-like structures (fibers, fingerprints, wood grain) and hardly across
 * it, closing interrupted lines. Each iteration performs one explicit step of
 * {@code du/dt = div(D ∇u)}, where the diffusion tensor {@code D} is derived
 * from the structure tensor {@code J} of the current image (see
 * {@code filter.structureTensor}), computed with gradient scale {@code sigma}
 * and integration scale {@code rho}. {@code D} has the same eigenvectors as
 * {@code J}; with {@code μ1 >= ... >= μn} the eigenvalues of {@code J}, the
 * diffusivity along the eigenvector of {@code μn} (the direction of least
 * intensity variation, i.e. along the structure) is
 * </p>
 *
 * <pre>
 * λn = α + (1 - α) · exp(-C / (μ1 - μn)²)   if μ1 &gt; μn, and α otherwise,
 * </pre>
 * <p>
 * and the diffusivity along all other eigenvectors is the small constant
 * {@code α}, which keeps the process well-posed. With {@code α = 1}, the
 * diffusion is isotropic and linear. The coherence threshold {@code C} has
 * units of intensity to the fourth power, so it depends on the intensity
 * range of the image: structures whose coherence {@code (μ1 - μn)²} is much
 * larger than {@code C} are diffused along their orientation with diffusivity
 * close to 1. In 3D, this smooths along line-like structures, the extension
 * of Weickert (1999) for fibers.
 * </p>
 * <p>
 * Derivatives in {@code div(D ∇u)} use the standard explicit discretization
 * (Weickert 1998, section 3.4.2): differences at half-pixel positions for the
 * diagonal entries of {@code D} and central differences of central
 * differences for the mixed entries, with the border pixels repeated outside
 * the image. The scheme is not guaranteed to satisfy a maximum principle, but
 * it is stable for time steps up to about {@code 1 / (2n)}.
 * </p>
 * <p>
 * Implemented from the papers.
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
 * @see <a href="https://doi.org/10.1023/A:1008009714131">J. Weickert,
 *      "Coherence-enhancing diffusion filtering", Int. J. Comput. Vis.
 *      31(2/3):111-127, 1999</a>
 * @see <a href="https://www.mia.uni-saarland.de/weickert/Papers/book.pdf">J.
 *      Weickert, "Anisotropic Diffusion in Image Processing", Teubner,
 *      Stuttgart, 1998</a>
 * @implNote op names='filter.coherenceEnhancingDiffusion'
 */
public class CoherenceEnhancingDiffusion<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity7<RandomAccessibleInterval<T>, Double, Double, Integer, Double, Double, Double, RandomAccessibleInterval<V>>
{

	@OpDependency(name = "filter.structureTensor")
	private Functions.Arity3<RandomAccessibleInterval<DoubleType>, Double, Double, RandomAccessibleInterval<DoubleType>> structureTensorOp;

	/**
	 * Applies coherence-enhancing diffusion.
	 *
	 * @param input the image to filter
	 * @param sigma the Gaussian scale for computing the gradient, in pixels
	 *          (the noise scale, e.g. 0.5 to 1)
	 * @param rho the Gaussian integration scale of the structure tensor, in
	 *          pixels (the size of the structures to be enhanced, e.g. 4)
	 * @param iterations the number of iterations (default: 20)
	 * @param dt the time step, at most 1 / (2n) for an n-dimensional image
	 *          (default: 1 / (4n))
	 * @param alpha the diffusivity across structures, in (0, 1] (default:
	 *          0.001)
	 * @param c the coherence threshold C, in units of intensity to the fourth
	 *          power (default: 1, suited to 8-bit intensities)
	 * @param output the filtered image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Double sigma, final Double rho, @Nullable final Integer iterations,
		@Nullable final Double dt, @Nullable final Double alpha,
		@Nullable final Double c, final RandomAccessibleInterval<V> output)
	{
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		final var n = input.numDimensions();
		final int iters = iterations == null ? 20 : iterations;
		final double step = dt == null ? 1.0 / (4 * n) : dt;
		if (!(step > 0) || step > 1.0 / (2 * n)) {
			throw new IllegalArgumentException("Time step must be in (0, 1/" + 2 *
				n + "] for stability: " + step);
		}
		final double a = alpha == null ? 0.001 : alpha;
		if (!(a > 0 && a <= 1)) {
			throw new IllegalArgumentException("Alpha must be in (0, 1]: " + a);
		}
		final double cc = c == null ? 1 : c;
		if (!(cc > 0)) {
			throw new IllegalArgumentException("C must be positive: " + cc);
		}
		final var m = n * (n + 1) / 2;
		final var count = FlatImage.size(input, m);
		final var dims = input.dimensionsAsLongArray();
		final var strides = FlatImage.strides(dims);
		// Index of the tensor component (r, s) in upper-triangular order.
		final var comp = new int[n][n];
		for (int r = 0, k = 0; r < n; r++)
			for (var s = r; s < n; s++, k++)
				comp[r][s] = comp[s][r] = k;

		var u = FlatImage.read(input);
		var next = new double[count];

		final var diffusion = new double[m * count];
		final var mat = new double[n][n];
		final var vecs = new double[n][n];
		final var vals = new double[n];
		final var pos = new int[n];
		for (var it = 0; it < iters; it++) {
			// Compute the diffusion tensor from the structure tensor.
			final var tensor = structureTensorOp.apply(ArrayImgs.doubles(u, dims),
				sigma, rho);
			var i = 0;
			for (final var t : Views.flatIterable(tensor))
				diffusion[i++] = t.get();
			for (var p = 0; p < count; p++) {
				for (var r = 0; r < n; r++)
					for (var s = 0; s < n; s++)
						mat[r][s] = diffusion[comp[r][s] * count + p];
				TensorUtils.symmetricEigen(mat, vals, vecs);
				var max = 0;
				var min = 0;
				for (var k = 1; k < n; k++) {
					if (vals[k] > vals[max]) max = k;
					if (vals[k] < vals[min]) min = k;
				}
				final var coherence = vals[max] - vals[min];
				final var lambda = coherence > 0 ? a + (1 - a) * Math.exp(-cc /
					(coherence * coherence)) : a;
				// Note: D = α·I + (λn - α)·vn·vnᵀ, as all other diffusivities are α.
				for (var r = 0; r < n; r++) {
					for (var s = r; s < n; s++) {
						diffusion[comp[r][s] * count + p] = (r == s ? a : 0) + (lambda -
							a) * vecs[r][min] * vecs[s][min];
					}
				}
			}

			// Take one explicit step of du/dt = div(D grad u).
			Arrays.fill(pos, 0);
			for (var p = 0; p < count; p++) {
				var change = 0.0;
				for (var r = 0; r < n; r++) {
					final var up = offset(pos, dims, strides, r, 1);
					final var down = offset(pos, dims, strides, r, -1);
					final var kr = comp[r][r] * count + p;
					change += 0.5 * (diffusion[kr] + diffusion[kr + up]) * (u[p + up] -
						u[p]);
					change -= 0.5 * (diffusion[kr] + diffusion[kr + down]) * (u[p] - u[p +
						down]);
					for (var s = 0; s < n; s++) {
						if (s == r) continue;
						final var k = comp[r][s] * count + p;
						final var upS = offset(pos, dims, strides, s, 1);
						final var downS = offset(pos, dims, strides, s, -1);
						final var fluxUp = diffusion[k + up] * (u[p + up + upS] - u[p + up +
							downS]);
						final var fluxDown = diffusion[k + down] * (u[p + down + upS] -
							u[p + down + downS]);
						change += 0.25 * (fluxUp - fluxDown);
					}
				}
				next[p] = u[p] + step * change;
				FlatImage.increment(pos, dims);
			}
			final var tmp = u;
			u = next;
			next = tmp;
		}

		FlatImage.write(u, output);
	}

	/**
	 * Gets the flat index offset to the neighbor in direction {@code ±e_d},
	 * repeating the border pixel outside the image.
	 */
	private static int offset(final int[] pos, final long[] dims,
		final int[] strides, final int d, final int dir)
	{
		final var q = pos[d] + dir;
		return q < 0 || q >= dims[d] ? 0 : dir * strides[d];
	}
}
