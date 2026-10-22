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

package org.scijava.ops.image.filter.phaseCorrelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Estimates the translation between two n-dimensional images by phase
 * correlation (Kuglin &amp; Hines (1975), "The phase correlation image
 * alignment method", Proc. IEEE Conf. on Cybernetics and Society, 163-165),
 * with the candidate verification of Preibisch, Saalfeld &amp; Tomancak
 * (2009), "Globally optimal stitching of tiled 3D microscopic image
 * acquisitions", Bioinformatics 25(11):1463-1465.
 * <p>
 * Both images are mean-subtracted and zero-padded to a common size {@code N}
 * suitable for the FFT. The inverse Fourier transform of their normalized
 * cross-power spectrum, the phase correlation matrix, has a peak at the shift
 * between the images. Each frequency of the cross-power spectrum is divided
 * by its magnitude plus {@value #REGULARIZATION} times the largest magnitude,
 * so that frequencies without signal (as in smooth or noisy images) do not
 * get the same weight as the others. Because the phase correlation matrix is
 * periodic, a peak at {@code p} means a shift of either {@code p_d} or
 * {@code p_d - N_d} in each dimension {@code d}. Every shift that each of the
 * {@code numPeaks} highest local maxima of the matrix could mean is verified
 * by the Pearson correlation of the images on their overlap, and the shift
 * with the highest correlation wins.
 * </p>
 * <p>
 * Since the peak of the phase correlation matrix can be off by a pixel or
 * two, the two candidate shifts with the highest
 * correlation are first moved to the nearest integer shift
 * (within a few pixels) at which the correlation is locally maximal. With
 * subpixel localization, the winning shift is then refined in each dimension
 * by fitting a parabola to the correlation at that shift and its two
 * neighbors in that dimension. Fitting the cross-correlation rather than the
 * phase correlation matrix is much more robust to noise, since the
 * normalization of the cross-power spectrum amplifies the noise of the
 * frequencies where the images have little signal.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> type of the first image
 * @param <U> type of the second image
 * @implNote op names='filter.phaseCorrelate'
 */
public class DefaultPhaseCorrelate<T extends RealType<T>, U extends RealType<U>>
	implements
	Functions.Arity5<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Integer, Boolean, Long, PhaseCorrelationResult>
{

	/**
	 * The fraction of the largest magnitude of the cross-power spectrum which
	 * is added to the magnitude of each frequency before normalizing it.
	 */
	public static final double REGULARIZATION = 0.01;

	/** The number of verified candidate shifts refined by hill climbing. */
	private static final int CLIMB_CANDIDATES = 2;

	@OpDependency(name = "stats.mean")
	private Computers.Arity1<Iterable<T>, DoubleType> mean1Op;

	@OpDependency(name = "stats.mean")
	private Computers.Arity1<Iterable<U>, DoubleType> mean2Op;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, Img<FloatType>> createImg;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, ComplexFloatType, Boolean, RandomAccessibleInterval<ComplexFloatType>> createFFTOutput;

	@OpDependency(name = "filter.fft")
	private Computers.Arity1<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<ComplexFloatType>> fftOp;

	@OpDependency(name = "filter.ifft")
	private Computers.Arity1<RandomAccessibleInterval<ComplexFloatType>, RandomAccessibleInterval<FloatType>> ifftOp;

	@OpDependency(name = "coloc.pearsons")
	private BiFunction<Iterable<T>, Iterable<U>, Double> pearsonsOp;

	/**
	 * Estimates the translation between two images.
	 *
	 * @param image1 the first (reference) image
	 * @param image2 the second image, of the same dimensionality but possibly a
	 *          different size; both images are taken to start at the origin
	 * @param numPeaks the number of phase correlation peaks to verify (default
	 *          5)
	 * @param subpixel whether to localize the shift to subpixel accuracy
	 *          (default true)
	 * @param minOverlap the minimum number of overlapping pixels for a shift to
	 *          be considered (default: 5 percent of the pixels of the smaller
	 *          image, and at least 2)
	 * @return the shift {@code s} such that {@code image2(x) = image1(x - s)},
	 *         with the Pearson correlation on the overlap
	 */
	@Override
	public PhaseCorrelationResult apply(final RandomAccessibleInterval<T> image1,
		final RandomAccessibleInterval<U> image2, @Nullable Integer numPeaks,
		@Nullable Boolean subpixel, @Nullable Long minOverlap)
	{
		final var n = image1.numDimensions();
		if (image2.numDimensions() != n) throw new IllegalArgumentException(
			"The images must have the same number of dimensions.");
		if (numPeaks == null) numPeaks = 5;
		if (numPeaks < 1) throw new IllegalArgumentException(
			"At least one peak must be verified.");
		if (subpixel == null) subpixel = true;
		if (minOverlap == null) {
			final var smaller = Math.min(image1.size(), image2.size());
			minOverlap = Math.max(2, (long) Math.ceil(0.05 * smaller));
		}

		final var zero1 = Views.zeroMin(image1);
		final var zero2 = Views.zeroMin(image2);
		final var dims1 = zero1.dimensionsAsLongArray();
		final var dims2 = zero2.dimensionsAsLongArray();
		final var maxDims = new long[n];
		for (var d = 0; d < n; d++)
			maxDims[d] = Math.max(dims1[d], dims2[d]);
		final var padded = new long[n];
		FFTMethods.dimensionsRealToComplexFast(new FinalDimensions(maxDims),
			padded, new long[n]);
		final var paddedDims = new FinalDimensions(padded);

		// Compute the phase correlation matrix.
		final var fft1 = transform(zero1, mean(mean1Op, zero1), paddedDims);
		final var fft2 = transform(zero2, mean(mean2Op, zero2), paddedDims);
		LoopBuilder.setImages(fft1, fft2).forEachPixel((a, b) -> {
			// b * conj(a)
			final var re = b.getRealDouble() * a.getRealDouble() + b
				.getImaginaryDouble() * a.getImaginaryDouble();
			final var im = b.getImaginaryDouble() * a.getRealDouble() - b
				.getRealDouble() * a.getImaginaryDouble();
			b.setComplexNumber(re, im);
		});
		var maxMagnitude = 0.0;
		for (final var c : Views.iterable(fft2))
			maxMagnitude = Math.max(maxMagnitude, c.getPowerDouble());
		final var epsilon = REGULARIZATION * maxMagnitude;
		for (final var c : Views.iterable(fft2)) {
			final var magnitude = c.getPowerDouble() + epsilon;
			if (magnitude > 0) c.mul(1 / magnitude);
		}
		final var pcm = createImg.apply(paddedDims, new FloatType());
		ifftOp.compute(fft2, pcm);

		// Find the highest local maxima, with periodic boundaries.
		final var neighborhoods = new RectangleShape(1, true)
			.neighborhoodsRandomAccessible(Views.extendPeriodic(pcm)).randomAccess();
		final var heap = new PriorityQueue<Peak>((a, b) -> Double.compare(a.value,
			b.value));
		final var cursor = pcm.localizingCursor();
		while (cursor.hasNext()) {
			final var v = cursor.next().get();
			if (heap.size() == numPeaks && v <= heap.peek().value) continue;
			neighborhoods.setPosition(cursor);
			var isMax = true;
			for (final var neighbor : neighborhoods.get()) {
				if (neighbor.get() > v) {
					isMax = false;
					break;
				}
			}
			if (!isMax) continue;
			heap.add(new Peak(cursor.positionAsLongArray(), v));
			if (heap.size() > numPeaks) heap.poll();
		}
		final List<Peak> peaks = new ArrayList<>(heap);

		// Verify each possible shift of each peak by cross-correlation.
		final var verifier = new Verifier(zero1, zero2, minOverlap);
		final var candidates = new ArrayList<Candidate>();
		for (final var peak : peaks) {
			for (var variant = 0; variant < 1 << n; variant++) {
				final var shift = new long[n];
				for (var d = 0; d < n; d++) {
					shift[d] = (variant >> d & 1) == 0 ? peak.position[d] : //
						peak.position[d] - padded[d];
				}
				final var r = verifier.correlation(shift);
				if (!Double.isNaN(r)) candidates.add(new Candidate(shift, r,
					peak.value));
			}
		}

		// Note: Hill climbing costs up to 1 + MAX_STEPS * 2n correlations per
		// candidate, so only the best few verified candidates are refined.
		candidates.sort((a, b) -> Double.compare(b.r, a.r));
		var bestR = Double.NaN;
		var bestPeak = Double.NaN;
		long[] best = null;
		for (final var c : candidates.subList(0, Math.min(CLIMB_CANDIDATES,
			candidates.size())))
		{
			final var r = verifier.climb(c.shift);
			if (best == null || r > bestR) {
				bestR = r;
				bestPeak = c.peak;
				best = c.shift;
			}
		}
		if (best == null) {
			return new PhaseCorrelationResult(new double[n], Double.NaN, Double.NaN,
				0);
		}

		final var result = new double[n];
		if (subpixel) {
			// Fit a parabola in each dimension.
			for (var d = 0; d < n; d++) {
				best[d]--;
				final var below = verifier.correlation(best);
				best[d] += 2;
				final var above = verifier.correlation(best);
				best[d]--;
				final var curvature = below + above - 2 * bestR;
				if (curvature < 0) {
					final var offset = (below - above) / (2 * curvature);
					if (Math.abs(offset) <= 0.5) result[d] = offset;
				}
			}
		}
		for (var d = 0; d < n; d++)
			result[d] += best[d];
		return new PhaseCorrelationResult(result, bestR, bestPeak, verifier
			.overlap(best));
	}

	private <V extends RealType<V>> double mean(
		final Computers.Arity1<Iterable<V>, DoubleType> meanOp,
		final RandomAccessibleInterval<V> image)
	{
		final var mean = new DoubleType();
		meanOp.compute(Views.iterable(image), mean);
		return mean.get();
	}

	/**
	 * Copies the mean-subtracted image into the origin of a zero image of the
	 * padded size, and transforms it.
	 */
	private <V extends RealType<V>> RandomAccessibleInterval<ComplexFloatType>
		transform(final RandomAccessibleInterval<V> image, final double mean,
			final Dimensions paddedDims)
	{
		final var padded = createImg.apply(paddedDims, new FloatType());
		LoopBuilder.setImages(image, Views.interval(padded, image)).forEachPixel((
			in, out) -> out.setReal(in.getRealDouble() - mean));
		final var fft = createFFTOutput.apply(paddedDims, new ComplexFloatType(),
			true);
		fftOp.compute(padded, fft);
		return fft;
	}

	private static class Candidate {

		private final long[] shift;
		private final double r;
		private final double peak;

		private Candidate(final long[] shift, final double r, final double peak) {
			this.shift = shift;
			this.r = r;
			this.peak = peak;
		}
	}

	private static class Peak {

		private final long[] position;
		private final double value;

		private Peak(final long[] position, final double value) {
			this.position = position;
			this.value = value;
		}
	}

	/** Computes the Pearson correlation of the images on their overlap. */
	private class Verifier {

		private static final int MAX_STEPS = 3;

		private final RandomAccessibleInterval<T> image1;
		private final RandomAccessibleInterval<U> image2;
		private final long minOverlap;
		private final Map<List<Long>, Double> cache = new HashMap<>();

		private Verifier(final RandomAccessibleInterval<T> image1,
			final RandomAccessibleInterval<U> image2, final long minOverlap)
		{
			this.image1 = image1;
			this.image2 = image2;
			this.minOverlap = minOverlap;
		}

		/** Gets the overlap of image2 and image1 shifted by the given vector. */
		private FinalInterval interval(final long[] shift) {
			final var n = shift.length;
			final var min = new long[n];
			final var max = new long[n];
			for (var d = 0; d < n; d++) {
				min[d] = Math.max(0, shift[d]);
				max[d] = Math.min(image2.dimension(d), image1.dimension(d) +
					shift[d]) - 1;
			}
			return new FinalInterval(min, max);
		}

		private long overlap(final long[] shift) {
			final var interval = interval(shift);
			long size = 1;
			for (var d = 0; d < shift.length; d++)
				size *= Math.max(0, interval.dimension(d));
			return size;
		}

		/**
		 * Moves the shift, one pixel at a time and at most {@value #MAX_STEPS}
		 * times, to the neighbor with the highest correlation, until it is a
		 * local maximum of the correlation.
		 *
		 * @return the correlation at the final shift
		 */
		private double climb(final long[] shift) {
			var r = correlation(shift);
			if (Double.isNaN(r)) return r;
			for (var step = 0; step < MAX_STEPS; step++) {
				var bestD = -1;
				var bestDelta = 0;
				for (var d = 0; d < shift.length; d++) {
					for (var delta = -1; delta <= 1; delta += 2) {
						shift[d] += delta;
						final var neighbor = correlation(shift);
						shift[d] -= delta;
						if (neighbor > r) {
							r = neighbor;
							bestD = d;
							bestDelta = delta;
						}
					}
				}
				if (bestD < 0) break;
				shift[bestD] += bestDelta;
			}
			return r;
		}

		/**
		 * Gets the correlation under the given shift, or {@code NaN} if the
		 * overlap is too small or constant.
		 */
		private double correlation(final long[] shift) {
			final var key = new ArrayList<Long>(shift.length);
			for (final var v : shift)
				key.add(v);
			return cache.computeIfAbsent(key, k -> computeCorrelation(shift));
		}

		private double computeCorrelation(final long[] shift) {
			for (var d = 0; d < shift.length; d++)
				if (shift[d] >= image2.dimension(d) || //
					shift[d] <= -image1.dimension(d)) return Double.NaN;
			if (overlap(shift) < minOverlap) return Double.NaN;
			final var interval = interval(shift);
			try {
				return pearsonsOp.apply( //
					Views.flatIterable(Views.interval(Views.translate(image1, shift),
						interval)), //
					Views.flatIterable(Views.interval(image2, interval)));
			}
			catch (final IllegalArgumentException e) {
				return Double.NaN;
			}
		}
	}
}
