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

package org.scijava.ops.image.segment.graphCut;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Segments an n-dimensional image into a foreground and a background region
 * of different mean intensities, by a minimum graph cut.
 * <p>
 * The intensities of each region are modeled as Gaussian with the region mean
 * and a common standard deviation {@code sigma}. The data term is the negative
 * log-likelihood {@code (I_p - mean)^2 / (2 sigma^2)} of each label, and the
 * smoothness term is a Potts model with weight {@code lambda}, optionally
 * reduced across intensity edges. The labeling of minimal energy is found
 * exactly with the min-cut/max-flow algorithm of Boykov &amp; Kolmogorov
 * (2004); see {@link GridGraphCut} for the energy and {@link BKMaxFlow} for
 * the algorithm. With {@code lambda = 0}, a pixel is foreground if and only
 * if it is closer to the foreground mean than to the background mean.
 * </p>
 * <p>
 * The means and {@code sigma} are estimated by fitting a mixture of two
 * Gaussians of equal variance to the intensities with the EM algorithm
 * (Dempster, Laird &amp; Rubin (1977), J. R. Stat. Soc. B 39(1):1-38),
 * starting from the two classes of the iterative intermeans threshold
 * (Ridler &amp; Calvard (1978), "Picture thresholding using an iterative
 * selection method", IEEE Trans. Syst. Man Cybern. 8(8):630-632), the
 * brighter class being the foreground. Given means are kept fixed during the
 * fit. For other data terms, pass cost images to the
 * {@code segment.graphCut} op which takes them.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> the image type
 * @implNote op names='segment.graphCut'
 */
public class DefaultGraphCut<T extends RealType<T>> implements
	Functions.Arity6<RandomAccessibleInterval<T>, Double, Double, Double, Boolean, Boolean, RandomAccessibleInterval<BitType>>
{

	private static final int MAX_ITERATIONS = 1000;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> createOp;

	/**
	 * Segments an image.
	 *
	 * @param image the image to segment
	 * @param lambda the weight of the smoothness term relative to the data
	 *          term (default 1); 0 gives a pixelwise segmentation
	 * @param foregroundMean the mean intensity of the foreground (default:
	 *          estimated)
	 * @param backgroundMean the mean intensity of the background (default:
	 *          estimated)
	 * @param contrast whether to weaken the smoothness term across intensity
	 *          edges, by the factor exp(-beta (I_p - I_q)^2) with beta the
	 *          inverse of twice the mean squared neighbor difference (default
	 *          false)
	 * @param fullConnectivity whether diagonal neighbors are connected too, with
	 *          weights divided by their distance, or only face neighbors
	 *          (default false); full connectivity makes boundaries costlier,
	 *          by a factor of about 2.4 in 2D and 6 in 3D, so it calls for a
	 *          smaller lambda
	 * @return the foreground mask
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply(
		final RandomAccessibleInterval<T> image, @Nullable Double lambda,
		@Nullable Double foregroundMean, @Nullable Double backgroundMean,
		@Nullable Boolean contrast, @Nullable Boolean fullConnectivity)
	{
		if (lambda == null) lambda = 1.0;
		if (contrast == null) contrast = false;
		if (fullConnectivity == null) fullConnectivity = false;

		final var values = GridGraphCut.flatten(image);
		final var means = intermeans(values);
		if (backgroundMean != null) means[0] = backgroundMean;
		if (foregroundMean != null) means[1] = foregroundMean;
		final var variance = fitMixture(values, means, backgroundMean != null,
			foregroundMean != null);
		final double fg = means[1], bg = means[0];

		final var fgCost = new double[values.length];
		final var bgCost = new double[values.length];
		for (var i = 0; i < values.length; i++) {
			final var df = values[i] - fg;
			final var db = values[i] - bg;
			fgCost[i] = df * df / (2 * variance);
			bgCost[i] = db * db / (2 * variance);
		}

		final var foreground = GridGraphCut.segment(image.dimensionsAsLongArray(),
			fgCost, bgCost, contrast ? values : null, lambda, fullConnectivity);
		return GridGraphCut.mask(createOp, image, foreground);
	}

	/**
	 * Fits a mixture of two Gaussians of equal variance with the EM algorithm.
	 *
	 * @param means the initial background and foreground means, updated in
	 *          place
	 * @return the variance
	 */
	private static double fitMixture(final double[] values, final double[] means,
		final boolean fixBackground, final boolean fixForeground)
	{
		final var count = values.length;
		if (count == 0) return 1;

		// Start from the pooled variance of the nearest-mean classes.
		var variance = 0.0;
		var weight = 0.0;
		for (final var v : values) {
			final var d0 = v - means[0];
			final var d1 = v - means[1];
			variance += Math.min(d0 * d0, d1 * d1);
			if (d1 * d1 < d0 * d0) weight++;
		}
		variance /= count;
		weight = Math.min(Math.max(weight / count, 0.01), 0.99);
		final var scale = Math.abs(means[1] - means[0]);
		if (!(variance > 0) || scale == 0) return variance > 0 ? variance : 1;

		for (var iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
			double n1 = 0, s0 = 0, s1 = 0, q0 = 0, q1 = 0;
			final var prior = Math.log(weight / (1 - weight));
			for (final var v : values) {
				final var d0 = v - means[0];
				final var d1 = v - means[1];
				final var logOdds = prior + (d0 * d0 - d1 * d1) / (2 * variance);
				final var r = 1 / (1 + Math.exp(-logOdds));
				n1 += r;
				s0 += (1 - r) * v;
				s1 += r * v;
				q0 += (1 - r) * v * v;
				q1 += r * v * v;
			}
			final var n0 = count - n1;
			final double m0 = fixBackground || n0 <= 0 ? means[0] : s0 / n0;
			final double m1 = fixForeground || n1 <= 0 ? means[1] : s1 / n1;
			final var next = (q0 - 2 * m0 * s0 + m0 * m0 * n0 + q1 - 2 * m1 * s1 +
				m1 * m1 * n1) / count;
			if (!(next > 1e-12 * scale * scale)) break;
			final var change = Math.abs(m0 - means[0]) + Math.abs(m1 - means[1]) +
				Math.abs(Math.sqrt(next) - Math.sqrt(variance));
			means[0] = m0;
			means[1] = m1;
			variance = next;
			weight = Math.min(Math.max(n1 / count, 1e-6), 1 - 1e-6);
			if (change < 1e-9 * scale) break;
		}
		return variance;
	}

	/**
	 * Computes the class means of the iterative intermeans threshold.
	 *
	 * @return the lower and upper class means
	 */
	private static double[] intermeans(final double[] values) {
		var min = Double.POSITIVE_INFINITY;
		var max = Double.NEGATIVE_INFINITY;
		for (final var v : values) {
			min = Math.min(min, v);
			max = Math.max(max, v);
		}
		var threshold = (min + max) / 2;
		var lower = min;
		var upper = max;
		for (var iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
			double sumLower = 0, sumUpper = 0;
			long countLower = 0, countUpper = 0;
			for (final var v : values) {
				if (v <= threshold) {
					sumLower += v;
					countLower++;
				}
				else {
					sumUpper += v;
					countUpper++;
				}
			}
			if (countLower == 0 || countUpper == 0) break;
			lower = sumLower / countLower;
			upper = sumUpper / countUpper;
			final var next = (lower + upper) / 2;
			if (next == threshold) break;
			threshold = next;
		}
		return new double[] { lower, upper };
	}
}
