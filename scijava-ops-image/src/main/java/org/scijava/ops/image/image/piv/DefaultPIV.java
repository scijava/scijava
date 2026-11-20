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

package org.scijava.ops.image.image.piv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Particle image velocimetry (PIV): estimates the displacement field between
 * two n-dimensional frames on a regular grid of interrogation windows.
 * <p>
 * For each interrogation window of the first frame, the zero-normalized
 * cross-correlation with the second frame is computed for every integer
 * displacement within the search range. Where the displaced window leaves the
 * second frame, the correlation is restricted to the overlap (masked
 * normalized cross-correlation, Padfield (2012), "Masked object registration
 * in the Fourier domain", IEEE Trans. Image Process. 21(5):2706-2718), and
 * displacements under which less than {@value #MIN_OVERLAP} of the window
 * overlaps the second frame are not considered. The correlations are computed
 * with the FFT, and the local sums of the second frame with separable box
 * sums.
 * The displacement of the window is the correlation peak, refined to subpixel
 * accuracy in each dimension by the three-point Gaussian peak fit (Willert
 * &amp; Gharib (1991), "Digital particle image velocimetry", Experiments in
 * Fluids 10:181-193), or by a parabolic fit where the correlations around the
 * peak are not all positive. See Raffel, Willert, Scarano, K&auml;hler,
 * Wereley &amp; Kompenhans (2018), <em>Particle Image Velocimetry: A Practical
 * Guide</em>, 3rd ed., Springer, for an overview of the method.
 * </p>
 * <p>
 * Optionally, the vectors are validated by the normalized median test of
 * Westerweel &amp; Scarano (2005), "Universal outlier detection for PIV data",
 * Experiments in Fluids 39:1096-1100: for each vector component, the residual
 * of the vector from the median of its (up to {@code 3^n - 1}) neighbors is
 * divided by the median residual of the neighbors from that median plus
 * {@value #MEDIAN_TEST_EPSILON} pixels. A vector whose normalized residual
 * exceeds the threshold in any component is an outlier, and is replaced,
 * like any vector that could not be measured, by the component-wise median of
 * its valid neighbors.
 * </p>
 * <p>
 * The windows are placed from the minimum of the first frame with the given
 * step, as many as fit inside the frame. Windows without contrast in the
 * first frame give {@code NaN} vectors (before replacement).
 * </p>
 * <p>
 * Implemented from the papers above. Like Fiji's PIV_analyser, this is a
 * single-pass analysis without window deformation or iterative refinement;
 * unlike it, windows of any size are allowed (not only powers of two). Each
 * frame is limited to 2<sup>31</sup> pixels.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> type of the first frame
 * @param <U> type of the second frame
 * @implNote op names='image.piv'
 */
public class DefaultPIV<T extends RealType<T>, U extends RealType<U>>
	implements
	Functions.Arity7<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, int[], int[], int[], Boolean, Double, PIVResult>
{

	/** The noise level, in pixels, of the normalized median test. */
	public static final double MEDIAN_TEST_EPSILON = 0.1;

	/**
	 * The smallest fraction of a window which must overlap the second frame
	 * under a candidate displacement.
	 */
	public static final double MIN_OVERLAP = 0.5;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, Img<FloatType>> createImg;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, ComplexFloatType, Boolean, RandomAccessibleInterval<ComplexFloatType>> createFFTOutput;

	@OpDependency(name = "filter.fft")
	private Computers.Arity1<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<ComplexFloatType>> fftOp;

	@OpDependency(name = "filter.ifft")
	private Computers.Arity1<RandomAccessibleInterval<ComplexFloatType>, RandomAccessibleInterval<FloatType>> ifftOp;

	/**
	 * Estimates the displacement field between two frames.
	 *
	 * @param frame1 the first frame
	 * @param frame2 the second frame, of the same size as the first
	 * @param windowSize the size of the interrogation windows, either one value
	 *          for all dimensions or one per dimension (default 32, or the frame
	 *          size if smaller)
	 * @param step the distance between neighboring windows, either one value
	 *          for all dimensions or one per dimension (default: the window
	 *          size, i.e. no overlap; half of it gives the common 50 percent
	 *          overlap)
	 * @param searchRange the largest displacement searched for, either one
	 *          value for all dimensions or one per dimension (default: half the
	 *          window size)
	 * @param subpixel whether to refine the displacements to subpixel accuracy
	 *          (default true)
	 * @param medianThreshold the threshold of the normalized median test for
	 *          outliers, typically 2; by default, the vectors are not validated
	 * @return the displacement field
	 */
	@Override
	public PIVResult apply(final RandomAccessibleInterval<T> frame1,
		final RandomAccessibleInterval<U> frame2, @Nullable int[] windowSize,
		@Nullable int[] step, @Nullable int[] searchRange,
		@Nullable Boolean subpixel, @Nullable Double medianThreshold)
	{
		final var n = frame1.numDimensions();
		if (frame2.numDimensions() != n) throw new IllegalArgumentException(
			"The frames must have the same number of dimensions.");
		final var dims = new int[n];
		for (var d = 0; d < n; d++) {
			if (frame1.dimension(d) != frame2.dimension(d))
				throw new IllegalArgumentException("The frames must have the same size.");
			if (frame1.dimension(d) > Integer.MAX_VALUE)
				throw new IllegalArgumentException("The frames are too large.");
			dims[d] = (int) frame1.dimension(d);
		}
		if (frame1.size() > Integer.MAX_VALUE) throw new IllegalArgumentException(
			"The frames are too large.");

		var w = perDimension(windowSize, n, "window size");
		if (w == null) {
			w = new int[n];
			for (var d = 0; d < n; d++)
				w[d] = Math.min(32, dims[d]);
		}
		var s = perDimension(step, n, "step");
		if (s == null) s = w.clone();
		var r = perDimension(searchRange, n, "search range");
		if (r == null) {
			r = new int[n];
			for (var d = 0; d < n; d++)
				r[d] = w[d] / 2;
		}
		if (subpixel == null) subpixel = true;
		for (var d = 0; d < n; d++) {
			if (w[d] < 1 || w[d] > dims[d]) throw new IllegalArgumentException(
				"The window size must be between 1 and the frame size.");
			if (s[d] < 1) throw new IllegalArgumentException(
				"The step must be positive.");
			if (r[d] < 0) throw new IllegalArgumentException(
				"The search range must not be negative.");
		}

		final var data1 = flatten(frame1);
		final var data2 = flatten(frame2);
		final var gridSize = new long[n];
		var count = 1;
		for (var d = 0; d < n; d++) {
			gridSize[d] = (dims[d] - w[d]) / s[d] + 1;
			count *= gridSize[d];
		}

		final var correlator = new Correlator(dims, w, r, subpixel);
		final var displacements = new double[count * n];
		final var correlations = new double[count];
		final var measured = new boolean[count];
		final var gridPoint = new int[n];
		final var origin = new int[n];
		final var vector = new double[n];
		for (var i = 0; i < count; i++) {
			for (var d = 0; d < n; d++)
				origin[d] = gridPoint[d] * s[d];
			correlations[i] = correlator.correlate(data1, data2, origin, vector);
			measured[i] = !Double.isNaN(correlations[i]);
			System.arraycopy(vector, 0, displacements, i * n, n);
			increment(gridPoint, gridSize);
		}

		final var valid = medianThreshold == null ? measured : validate(
			displacements, measured, gridSize, medianThreshold);

		final var min = frame1.minAsLongArray();
		return new PIVResult(gridSize, min, w, s, displacements, correlations,
			valid);
	}

	/**
	 * Expands a parameter given either once or once per dimension, or returns
	 * {@code null} if it is {@code null}.
	 */
	private static int[] perDimension(final int[] values, final int n,
		final String name)
	{
		if (values == null) return null;
		if (values.length == n) return values.clone();
		if (values.length == 1) {
			final var expanded = new int[n];
			Arrays.fill(expanded, values[0]);
			return expanded;
		}
		throw new IllegalArgumentException("The " + name +
			" must have one value or one per dimension.");
	}

	/** Copies an image into an array, with dimension 0 varying fastest. */
	private static <V extends RealType<V>> double[] flatten(
		final RandomAccessibleInterval<V> image)
	{
		final var data = new double[(int) image.size()];
		var i = 0;
		for (final var v : Views.flatIterable(image))
			data[i++] = v.getRealDouble();
		return data;
	}

	private static void increment(final int[] point, final long[] size) {
		for (var d = 0; d < point.length; d++) {
			if (++point[d] < size[d]) return;
			point[d] = 0;
		}
	}

	private static int product(final int[] values) {
		var product = 1;
		for (final var v : values)
			product *= v;
		return product;
	}

	/**
	 * Sums each box of the given size within an array of the given dimensions,
	 * giving an array of size {@code dims - box + 1}.
	 */
	private static double[] boxSums(double[] data, final int[] dims,
		final int[] box)
	{
		final var n = dims.length;
		final var current = dims.clone();
		for (var d = 0; d < n; d++) {
			final var next = current.clone();
			next[d] = current[d] - box[d] + 1;
			var stride = 1;
			for (var e = 0; e < d; e++)
				stride *= current[e];
			final var sums = new double[product(next)];
			final var outer = sums.length / (stride * next[d]);
			for (var o = 0; o < outer; o++) {
				for (var inner = 0; inner < stride; inner++) {
					final var src = o * stride * current[d] + inner;
					final var dst = o * stride * next[d] + inner;
					var sum = 0.0;
					for (var k = 0; k < box[d]; k++)
						sum += data[src + k * stride];
					sums[dst] = sum;
					for (var k = 1; k < next[d]; k++) {
						sum += data[src + (k + box[d] - 1) * stride] - data[src + (k - 1) *
							stride];
						sums[dst + k * stride] = sum;
					}
				}
			}
			data = sums;
			current[d] = next[d];
		}
		return data;
	}

	/**
	 * Applies the normalized median test, and replaces the outliers and the
	 * unmeasured vectors by the median of their valid neighbors.
	 *
	 * @return whether each vector is valid
	 */
	private static boolean[] validate(final double[] displacements,
		final boolean[] measured, final long[] gridSize, final double threshold)
	{
		final var n = gridSize.length;
		final var count = measured.length;
		final var valid = measured.clone();
		final var gridPoint = new int[n];
		final var values = new double[(int) Math.pow(3, n)];
		final var residuals = new double[values.length];
		for (var i = 0; i < count; i++) {
			if (measured[i]) {
				final var neighbors = neighbors(gridPoint, gridSize, measured);
				if (!neighbors.isEmpty()) {
					for (var d = 0; d < n && valid[i]; d++) {
						final var m = neighbors.size();
						for (var j = 0; j < m; j++)
							values[j] = displacements[neighbors.get(j) * n + d];
						final var median = median(values, m);
						for (var j = 0; j < m; j++)
							residuals[j] = Math.abs(values[j] - median);
						final var medianResidual = median(residuals, m);
						final var residual = Math.abs(displacements[i * n + d] - median) /
							(medianResidual + MEDIAN_TEST_EPSILON);
						if (residual > threshold) valid[i] = false;
					}
				}
			}
			increment(gridPoint, gridSize);
		}

		final var replaced = displacements.clone();
		Arrays.fill(gridPoint, 0);
		for (var i = 0; i < count; i++) {
			if (!valid[i]) {
				final var neighbors = neighbors(gridPoint, gridSize, valid);
				for (var d = 0; d < n; d++) {
					final var m = neighbors.size();
					for (var j = 0; j < m; j++)
						values[j] = displacements[neighbors.get(j) * n + d];
					replaced[i * n + d] = m == 0 ? Double.NaN : median(values, m);
				}
			}
			increment(gridPoint, gridSize);
		}
		System.arraycopy(replaced, 0, displacements, 0, displacements.length);
		return valid;
	}

	/** Gets the indices of the neighbors of a grid point that pass a filter. */
	private static List<Integer> neighbors(final int[] gridPoint,
		final long[] gridSize, final boolean[] filter)
	{
		final var n = gridPoint.length;
		final List<Integer> neighbors = new ArrayList<>();
		final var offset = new int[n];
		Arrays.fill(offset, -1);
		final var total = (int) Math.pow(3, n);
		for (var k = 0; k < total; k++) {
			var index = 0;
			var inside = true;
			var center = true;
			for (var d = n - 1; d >= 0; d--) {
				final var p = gridPoint[d] + offset[d];
				if (p < 0 || p >= gridSize[d]) inside = false;
				if (offset[d] != 0) center = false;
				index = index * (int) gridSize[d] + p;
			}
			if (inside && !center && filter[index]) neighbors.add(index);
			for (var d = 0; d < n; d++) {
				if (++offset[d] <= 1) break;
				offset[d] = -1;
			}
		}
		return neighbors;
	}

	private static double median(final double[] values, final int length) {
		final var sorted = Arrays.copyOf(values, length);
		Arrays.sort(sorted);
		return length % 2 == 1 ? sorted[length / 2] : 0.5 * (sorted[length / 2 -
			1] + sorted[length / 2]);
	}

	/**
	 * Correlates interrogation windows, reusing its buffers from one window to
	 * the next.
	 */
	private class Correlator {

		private final int n;
		private final int[] dims;
		private final int[] window;
		private final int[] range;
		private final boolean subpixel;

		/** The size of the search region: the window plus the search range. */
		private final int[] region;

		/** The number of candidate displacements in each dimension. */
		private final int[] shifts;

		private final int windowPixels;
		private final Img<FloatType> paddedWindow;
		private final Img<FloatType> paddedRegion;
		private final Img<FloatType> crossCorrelation;
		private final RandomAccessibleInterval<ComplexFloatType> windowFFT;
		private final RandomAccessibleInterval<ComplexFloatType> regionFFT;
		private final RandomAccessibleInterval<ComplexFloatType> maskFFT;
		private final RandomAccessibleInterval<ComplexFloatType> productFFT;
		private final FinalInterval windowInterval;
		private final FinalInterval regionInterval;
		private final FinalInterval shiftInterval;

		private Correlator(final int[] dims, final int[] window,
			final int[] range, final boolean subpixel)
		{
			n = dims.length;
			this.dims = dims;
			this.window = window;
			this.range = range;
			this.subpixel = subpixel;
			region = new int[n];
			shifts = new int[n];
			final var regionDims = new long[n];
			final var windowMax = new long[n];
			final var shiftMax = new long[n];
			for (var d = 0; d < n; d++) {
				region[d] = window[d] + 2 * range[d];
				shifts[d] = 2 * range[d] + 1;
				regionDims[d] = region[d];
				windowMax[d] = window[d] - 1;
				shiftMax[d] = shifts[d] - 1;
			}
			windowPixels = product(window);
			windowInterval = new FinalInterval(new long[n], windowMax);
			regionInterval = new FinalInterval(regionDims);
			shiftInterval = new FinalInterval(new long[n], shiftMax);

			final var padded = new long[n];
			FFTMethods.dimensionsRealToComplexFast(new FinalDimensions(regionDims),
				padded, new long[n]);
			final var paddedDims = new FinalDimensions(padded);
			paddedWindow = createImg.apply(paddedDims, new FloatType());
			paddedRegion = createImg.apply(paddedDims, new FloatType());
			crossCorrelation = createImg.apply(paddedDims, new FloatType());
			windowFFT = createFFTOutput.apply(paddedDims, new ComplexFloatType(),
				true);
			regionFFT = createFFTOutput.apply(paddedDims, new ComplexFloatType(),
				true);
			maskFFT = createFFTOutput.apply(paddedDims, new ComplexFloatType(),
				true);
			productFFT = createFFTOutput.apply(paddedDims, new ComplexFloatType(),
				true);
		}

		/**
		 * Correlates the window with the given minimum.
		 *
		 * @param vector receives the displacement, or {@code NaN}s
		 * @return the correlation peak, or {@code NaN}
		 */
		private double correlate(final double[] data1, final double[] data2,
			final int[] origin, final double[] vector)
		{
			Arrays.fill(vector, Double.NaN);

			// Extract the mean-subtracted window of the first frame.
			final var a = extract(data1, origin, window, null);
			var mean = 0.0;
			for (final var v : a)
				mean += v;
			mean /= windowPixels;
			var windowVariance = 0.0;
			for (var i = 0; i < a.length; i++) {
				a[i] -= mean;
				windowVariance += a[i] * a[i];
			}
			if (!(windowVariance > 0)) return Double.NaN;

			// Extract the search region of the second frame.
			final var regionOrigin = new int[n];
			for (var d = 0; d < n; d++)
				regionOrigin[d] = origin[d] - range[d];
			final var mask = new double[product(region)];
			final var b = extract(data2, regionOrigin, region, mask);
			final var b2 = new double[b.length];
			for (var i = 0; i < b2.length; i++)
				b2[i] = b[i] * b[i];
			final var sumB = boxSums(b, region, window);
			final var sumB2 = boxSums(b2, region, window);

			// Cross-correlate them: sumAB(k) = sum_x a(x) b(x + k).
			fill(paddedWindow, windowInterval, a);
			fftOp.compute(paddedWindow, windowFFT);
			fill(paddedRegion, regionInterval, b);
			fftOp.compute(paddedRegion, regionFFT);
			final var sumAB = crossCorrelate(windowFFT, regionFFT);

			// Note: where the shifted window leaves the second frame, the
			// correlation is restricted to the overlap, following the masked
			// normalized cross-correlation of Padfield (2012), "Masked object
			// registration in the Fourier domain", IEEE Trans. Image Process.
			// 21(5):2706-2718.
			final double[] count, sumA, sumA2;
			var inside = true;
			for (final var m : mask)
				if (m == 0) inside = false;
			if (inside) {
				count = null;
				sumA = null;
				sumA2 = null;
			}
			else {
				count = boxSums(mask, region, window);
				fill(paddedRegion, regionInterval, mask);
				fftOp.compute(paddedRegion, maskFFT);
				sumA = crossCorrelate(windowFFT, maskFFT);
				for (var i = 0; i < a.length; i++)
					a[i] *= a[i];
				fill(paddedWindow, windowInterval, a);
				fftOp.compute(paddedWindow, windowFFT);
				sumA2 = crossCorrelate(windowFFT, maskFFT);
			}

			// Normalize the cross-correlation.
			final var ncc = new double[sumAB.length];
			var best = -1;
			for (var i = 0; i < ncc.length; i++) {
				ncc[i] = Double.NaN;
				final double numerator, varianceA, varianceB;
				if (inside) {
					numerator = sumAB[i];
					varianceA = windowVariance;
					varianceB = sumB2[i] - sumB[i] * sumB[i] / windowPixels;
				}
				else {
					final var m = Math.round(count[i]);
					if (m < MIN_OVERLAP * windowPixels) continue;
					numerator = sumAB[i] - sumA[i] * sumB[i] / m;
					varianceA = sumA2[i] - sumA[i] * sumA[i] / m;
					varianceB = sumB2[i] - sumB[i] * sumB[i] / m;
				}
				if (varianceA > 1e-9 * windowVariance && varianceB > 1e-9 * sumB2[i]) {
					ncc[i] = numerator / Math.sqrt(varianceA * varianceB);
					if (best < 0 || ncc[i] > ncc[best]) best = i;
				}
			}
			if (best < 0) return Double.NaN;

			// Locate the peak.
			var stride = 1;
			var rest = best;
			for (var d = 0; d < n; d++) {
				final var k = rest % shifts[d];
				rest /= shifts[d];
				vector[d] = k - range[d];
				if (subpixel && k > 0 && k < shifts[d] - 1) {
					vector[d] += peakOffset(ncc[best - stride], ncc[best], ncc[best +
						stride]);
				}
				stride *= shifts[d];
			}
			return ncc[best];
		}

		/**
		 * Computes {@code c(k) = sum_x a(x) b(x + k)} for each candidate
		 * displacement index {@code k}, from the transforms of {@code a} and
		 * {@code b}.
		 */
		private double[] crossCorrelate(
			final RandomAccessibleInterval<ComplexFloatType> fa,
			final RandomAccessibleInterval<ComplexFloatType> fb)
		{
			LoopBuilder.setImages(fa, fb, productFFT).forEachPixel((x, y, p) -> {
				// y * conj(x)
				final var re = y.getRealDouble() * x.getRealDouble() + y
					.getImaginaryDouble() * x.getImaginaryDouble();
				final var im = y.getImaginaryDouble() * x.getRealDouble() - y
					.getRealDouble() * x.getImaginaryDouble();
				p.setComplexNumber(re, im);
			});
			ifftOp.compute(productFFT, crossCorrelation);
			final var values = new double[product(shifts)];
			var i = 0;
			for (final var c : Views.flatIterable(Views.interval(crossCorrelation,
				shiftInterval)))
				values[i++] = c.getRealDouble();
			return values;
		}

		/**
		 * Copies the box with the given minimum and size out of the frame data,
		 * with zeros outside of the frame.
		 *
		 * @param mask if not {@code null}, receives 1 inside the frame and 0
		 *          outside
		 */
		private double[] extract(final double[] data, final int[] min,
			final int[] size, final double[] mask)
		{
			final var values = new double[product(size)];
			final var position = new int[n];
			for (var i = 0; i < values.length; i++) {
				var index = 0;
				var inside = true;
				for (var d = n - 1; d >= 0; d--) {
					final var p = min[d] + position[d];
					if (p < 0 || p >= dims[d]) inside = false;
					index = index * dims[d] + p;
				}
				if (inside) {
					values[i] = data[index];
					if (mask != null) mask[i] = 1;
				}
				for (var d = 0; d < n; d++) {
					if (++position[d] < size[d]) break;
					position[d] = 0;
				}
			}
			return values;
		}

		/** Zeroes the image, then copies the values into the given interval. */
		private void fill(final Img<FloatType> image, final FinalInterval interval,
			final double[] values)
		{
			for (final var v : image)
				v.setZero();
			var i = 0;
			for (final var v : Views.flatIterable(Views.interval(image, interval)))
				v.setReal(values[i++]);
		}
	}

	/**
	 * Fits a peak to three equally spaced samples around a maximum, with a
	 * Gaussian if they are all positive and a parabola otherwise.
	 *
	 * @return the offset of the fitted peak from the middle sample, or 0 if no
	 *         peak within half a sample fits
	 */
	private static double peakOffset(final double below, final double center,
		final double above)
	{
		if (Double.isNaN(below) || Double.isNaN(above)) return 0;
		double a = below, b = center, c = above;
		if (below > 0 && center > 0 && above > 0) {
			a = Math.log(below);
			b = Math.log(center);
			c = Math.log(above);
		}
		final var curvature = a - 2 * b + c;
		if (!(curvature < 0)) return 0;
		final var offset = (a - c) / (2 * curvature);
		return Math.abs(offset) <= 0.5 ? offset : 0;
	}
}
