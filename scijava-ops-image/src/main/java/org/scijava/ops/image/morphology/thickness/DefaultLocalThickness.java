/*-
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

package org.scijava.ops.image.morphology.thickness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.concurrent.Parallelization;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.distance.DistanceTransform.DISTANCE_TYPE;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Computes the local thickness of a binary image: the diameter of the largest
 * ball that fits inside the foreground and contains the point, following
 * Hildebrand &amp; Rüegsegger (1997).
 * <p>
 * The pipeline is:
 * </p>
 * <ol>
 * <li>Squared Euclidean distance transform of the foreground (distance to the
 * nearest background element; the image border is <em>not</em> treated as
 * background), computed with {@code morphology.distanceTransform}.</li>
 * <li><em>Distance ridge</em>: foreground elements whose ball (radius = the
 * distance value) is not contained in the ball of any element of the
 * 3<sup>n</sup>-1 neighborhood. These are (approximately) the centres of
 * maximal inscribed balls.</li>
 * <li><em>Local thickness</em>: every element receives the largest radius of
 * all ridge balls covering it, reported as a diameter (2 &times; radius).</li>
 * <li><em>Clean-up</em>: elements of the thickness map that border its zero
 * region get the average of their interior neighbors, reducing "jaggies".</li>
 * <li>Optionally, elements outside the input foreground are set to 0.</li>
 * </ol>
 * <p>
 * This is an n-dimensional port of the distance ridge, local thickness and
 * clean-up plugins by Bob Dougherty (Copyright (c) 2006-2007, OptiNav, Inc.,
 * BSD-3-Clause), as distributed in the Fiji
 * <a href="https://github.com/fiji/LocalThickness">LocalThickness</a> project.
 * Unlike the original, which is 3D and isotropic only, this version supports
 * any dimensionality and anisotropic element spacing: the ridge-search
 * templates are computed for every subset of neighbor offset axes, and all
 * distances are measured in calibrated units. With unit calibration, results
 * match the original plugin.
 * </p>
 * <p>
 * Reference: T. Hildebrand and P. Rüegsegger, "A new method for the
 * model-independent assessment of thickness in three-dimensional images",
 * Journal of Microscopy 185(1):67-75, 1997.
 * </p>
 * <p>
 * Original license notice for the ported algorithm:
 * </p>
 *
 * <pre>
 * Copyright (c) 2006, 2007 OptiNav, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *   Neither the name of OptiNav, Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 * <p>
 * The input must contain at least one background element, and must have fewer
 * than 2<sup>31</sup> elements.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Bob Dougherty
 * @param <B> type of the binary input
 * @implNote op names='morphology.localThickness'
 */
public class DefaultLocalThickness<B extends BooleanType<B>> implements
	Functions.Arity3<RandomAccessibleInterval<B>, double[], Boolean, RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "morphology.distanceTransform")
	private Inplaces.Arity3_1<RandomAccessibleInterval<DoubleType>, DISTANCE_TYPE, double[]> edtOp;

	/**
	 * Computes the local thickness map of a binary image.
	 *
	 * @param input the binary image; {@code true} elements are foreground
	 * @param calibration the element spacing along each dimension (defaults to 1
	 *          for every dimension). Thickness values are reported in these
	 *          units.
	 * @param maskToInput whether to set elements outside the input foreground to
	 *          0 (default {@code true}). When {@code false}, the cleaned-up map
	 *          is returned as is, matching the Fiji "Local Thickness" driver;
	 *          its support then extends slightly into the background.
	 * @return the local thickness (diameter) map, with the same interval as
	 *         the input
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply( //
		final RandomAccessibleInterval<B> input, //
		@Nullable double[] calibration, //
		@Nullable Boolean maskToInput //
	) {
		final int n = input.numDimensions();
		if (calibration == null) {
			calibration = new double[n];
			Arrays.fill(calibration, 1);
		}
		if (calibration.length != n) {
			throw new IllegalArgumentException("Calibration length (" +
				calibration.length + ") does not match image dimensionality (" + n +
				")");
		}
		for (final double c : calibration) {
			if (!(c > 0) || Double.isInfinite(c)) {
				throw new IllegalArgumentException(
					"Calibration values must be positive and finite");
			}
		}
		if (maskToInput == null) maskToInput = true;

		final long[] dims = Intervals.dimensionsAsLongArray(input);
		if (Intervals.numElements(dims) > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + Intervals
				.numElements(dims) + " elements");
		}
		final int[] size = new int[n];
		for (int d = 0; d < n; d++)
			size[d] = (int) dims[d];
		final double[] cal2 = new double[n];
		for (int d = 0; d < n; d++)
			cal2[d] = calibration[d] * calibration[d];

		// Compute the squared distance transform. Foreground is initialized with
		// a value larger than any squared distance inside the image.
		final ArrayImg<DoubleType, DoubleArray> sqImg = ArrayImgs.doubles(dims);
		final double[] sq = sqImg.update(null).getCurrentStorageArray();
		final boolean[] fg = new boolean[sq.length];
		double big = 1;
		for (int d = 0; d < n; d++)
			big += cal2[d] * dims[d] * dims[d];
		int i = 0;
		for (final B b : Views.flatIterable(Views.zeroMin(input))) {
			fg[i] = b.get();
			sq[i] = fg[i] ? big : 0;
			i++;
		}
		edtOp.mutate(sqImg, DISTANCE_TYPE.EUCLIDIAN, cal2);

		final Neighbors nb = new Neighbors(size);
		final int[][] ridge = distanceRidge(sq, size, cal2, nb);
		final double[] lt = localThickness(sq.length, size, cal2, ridge, sq);
		final float[] out = cleanUp(lt, size, nb);
		if (maskToInput) {
			for (int k = 0; k < out.length; k++)
				if (!fg[k]) out[k] = 0;
		}

		final ArrayImg<FloatType, FloatArray> result = ArrayImgs.floats(out, dims);
		return Views.translate(result, input.minAsLongArray());
	}

	/**
	 * Finds the distance ridge: foreground elements whose ball is not contained
	 * in the ball of any neighbor. Returns the flat indices of the ridge points,
	 * in one array per parallel chunk.
	 */
	private static int[][] distanceRidge(final double[] sq, final int[] size,
		final double[] cal2, final Neighbors nb)
	{
		// Build ridge templates for every distinct squared radius.
		final double[] radii = Arrays.stream(sq).filter(v -> v > 0).distinct()
			.sorted().toArray();
		final Map<Double, double[]> templates = new HashMap<>(radii.length * 2);
		for (final double rSq : radii)
			templates.put(rSq, template(rSq, cal2));

		final int nChunks = Math.max(1, Parallelization.getTaskExecutor()
			.suggestNumberOfTasks());
		final int[][] chunks = new int[nChunks][];
		final List<Runnable> tasks = new ArrayList<>();
		final int total = sq.length;
		for (int c = 0; c < nChunks; c++) {
			final int chunk = c;
			final int start = (int) ((long) total * c / nChunks);
			final int end = (int) ((long) total * (c + 1) / nChunks);
			tasks.add(() -> {
				final int[] pos = new int[size.length];
				int[] found = new int[16];
				int count = 0;
				for (int idx = start; idx < end; idx++) {
					final double rSq = sq[idx];
					if (rSq <= 0) continue;
					final double[] t = templates.get(rSq);
					nb.position(idx, pos);
					boolean isRidge = true;
					for (int o = 0; o < nb.count; o++) {
						if (!nb.inBounds(pos, o)) continue;
						final double needed = t[nb.mask[o]];
						if (sq[idx + nb.delta[o]] >= needed * (1 - 1e-12)) {
							isRidge = false;
							break;
						}
					}
					if (isRidge) {
						if (count == found.length) found = Arrays.copyOf(found, count * 2);
						found[count++] = idx;
					}
				}
				chunks[chunk] = Arrays.copyOf(found, count);
			});
		}
		Parallelization.getTaskExecutor().runAll(tasks);
		return chunks;
	}

	/**
	 * For each subset of axes (bit mask) along which a neighbor is offset by
	 * one element, computes the smallest squared radius a ball centered on that
	 * neighbor must have to contain the grid ball of squared radius
	 * {@code rSq} centered at the origin.
	 */
	private static double[] template(final double rSq, final double[] cal2) {
		final int n = cal2.length;
		final double[] t = new double[1 << n];
		final int[] p = new int[n];
		final int[] max = new int[n];
		for (int d = 1; d < n; d++)
			max[d] = (int) Math.floor(Math.sqrt(rSq / cal2[d]) + 1e-9);
		// Iterate over grid points p >= 0 in dims 1..n-1 inside the ball, taking
		// the outermost grid point along dim 0.
		while (true) {
			double rest = 0;
			for (int d = 1; d < n; d++)
				rest += cal2[d] * p[d] * p[d];
			if (rest <= rSq * (1 + 1e-12)) {
				p[0] = (int) Math.floor(Math.sqrt(Math.max(0, rSq - rest) / cal2[0]) +
					1e-9);
				for (int mask = 1; mask < t.length; mask++) {
					double dist = 0;
					for (int d = 0; d < n; d++) {
						final int q = p[d] + ((mask >> d) & 1);
						dist += cal2[d] * q * q;
					}
					if (dist > t[mask]) t[mask] = dist;
				}
			}
			// Advance the odometer over dims 1..n-1.
			int d = 1;
			while (d < n && p[d] == max[d]) {
				p[d] = 0;
				d++;
			}
			if (d >= n) break;
			p[d]++;
		}
		return t;
	}

	/**
	 * For each element, finds the largest squared radius of all ridge balls
	 * containing it, and returns the corresponding diameter (2 &times; radius).
	 * The work is split into slabs along the last dimension, so that no two
	 * tasks write to the same element.
	 */
	private static double[] localThickness(final int total, final int[] size,
		final double[] cal2, final int[][] ridge, final double[] sq)
	{
		final int n = size.length;
		final double[] lt = new double[total];
		final int[] strides = strides(size);
		final int last = size[n - 1];
		final int nTasks = Math.max(1, Math.min(last, Parallelization
			.getTaskExecutor().suggestNumberOfTasks()));
		final List<Runnable> tasks = new ArrayList<>();
		for (int s = 0; s < nTasks; s++) {
			final int slabStart = (int) ((long) last * s / nTasks);
			final int slabEnd = (int) ((long) last * (s + 1) / nTasks) - 1;
			tasks.add(() -> {
				final int[] center = new int[n];
				final int[] lo = new int[n];
				final int[] hi = new int[n];
				for (final int[] chunk : ridge) {
					for (final int idx : chunk) {
						final double rSq = sq[idx];
						int rem = idx;
						for (int d = 0; d < n; d++) {
							center[d] = rem % size[d];
							rem /= size[d];
						}
						boolean empty = false;
						for (int d = 0; d < n; d++) {
							final int ext = (int) Math.floor(Math.sqrt(rSq / cal2[d]) + 1e-9);
							final int min = d == n - 1 ? slabStart : 0;
							final int max = d == n - 1 ? slabEnd : size[d] - 1;
							lo[d] = Math.max(min, center[d] - ext);
							hi[d] = Math.min(max, center[d] + ext);
							if (lo[d] > hi[d]) empty = true;
						}
						if (empty) continue;
						paint(lt, rSq, center, lo, hi, strides, cal2, n - 1, 0, 0);
					}
				}
			});
		}
		Parallelization.getTaskExecutor().runAll(tasks);
		for (int k = 0; k < total; k++)
			lt[k] = 2 * Math.sqrt(lt[k]);
		return lt;
	}

	/** Recursively paints {@code rSq} into all elements of the ball. */
	private static void paint(final double[] lt, final double rSq,
		final int[] center, final int[] lo, final int[] hi, final int[] strides,
		final double[] cal2, final int d, final double partial, final int base)
	{
		for (int x = lo[d]; x <= hi[d]; x++) {
			final int dx = x - center[d];
			final double dist = partial + cal2[d] * dx * dx;
			if (dist > rSq) continue;
			final int index = base + x * strides[d];
			if (d == 0) {
				if (rSq > lt[index]) lt[index] = rSq;
			}
			else {
				paint(lt, rSq, center, lo, hi, strides, cal2, d - 1, dist, index);
			}
		}
	}

	/**
	 * Replaces the value of each nonzero element bordering a zero element with
	 * the average of its interior (non-bordering) nonzero neighbors.
	 */
	private static float[] cleanUp(final double[] lt, final int[] size,
		final Neighbors nb)
	{
		final int total = lt.length;
		// Each flag is 0 for background, -1 for a non-background element that
		// borders background, and the input value for an interior element.
		final double[] flags = new double[total];
		final int[] pos = new int[size.length];
		for (int idx = 0; idx < total; idx++) {
			if (lt[idx] == 0) continue;
			nb.position(idx, pos);
			boolean border = false;
			for (int o = 0; o < nb.count; o++) {
				if (nb.inBounds(pos, o) && lt[idx + nb.delta[o]] == 0) {
					border = true;
					break;
				}
			}
			flags[idx] = border ? -1 : lt[idx];
		}
		// Border elements get the negated average of their interior neighbors,
		// so that already-processed border elements are excluded from later
		// averages.
		for (int idx = 0; idx < total; idx++) {
			if (flags[idx] != -1) continue;
			nb.position(idx, pos);
			int count = 0;
			double sum = 0;
			for (int o = 0; o < nb.count; o++) {
				if (!nb.inBounds(pos, o)) continue;
				final double v = flags[idx + nb.delta[o]];
				if (v > 0) {
					count++;
					sum += v;
				}
			}
			flags[idx] = -(count > 0 ? sum / count : lt[idx]);
		}
		final float[] out = new float[total];
		for (int idx = 0; idx < total; idx++)
			out[idx] = (float) Math.abs(flags[idx]);
		return out;
	}

	private static int[] strides(final int[] size) {
		final int[] strides = new int[size.length];
		int s = 1;
		for (int d = 0; d < size.length; d++) {
			strides[d] = s;
			s *= size[d];
		}
		return strides;
	}

	/** The 3<sup>n</sup>-1 neighborhood of an element in a flat array. */
	private static final class Neighbors {

		private final int[] size;
		private final int count;
		private final int[][] offsets;
		private final int[] delta;
		private final int[] mask;

		private Neighbors(final int[] size) {
			this.size = size;
			final int n = size.length;
			final int[] strides = strides(size);
			int total = 1;
			for (int d = 0; d < n; d++)
				total *= 3;
			count = total - 1;
			offsets = new int[count][n];
			delta = new int[count];
			mask = new int[count];
			int o = 0;
			for (int code = 0; code < total; code++) {
				final int[] off = new int[n];
				int c = code;
				int m = 0;
				int dl = 0;
				for (int d = 0; d < n; d++) {
					off[d] = c % 3 - 1;
					c /= 3;
					if (off[d] != 0) m |= 1 << d;
					dl += off[d] * strides[d];
				}
				if (m == 0) continue;
				offsets[o] = off;
				delta[o] = dl;
				mask[o] = m;
				o++;
			}
		}

		private void position(int idx, final int[] pos) {
			for (int d = 0; d < size.length; d++) {
				pos[d] = idx % size[d];
				idx /= size[d];
			}
		}

		private boolean inBounds(final int[] pos, final int o) {
			final int[] off = offsets[o];
			for (int d = 0; d < size.length; d++) {
				final int p = pos[d] + off[d];
				if (p < 0 || p >= size[d]) return false;
			}
			return true;
		}
	}
}
