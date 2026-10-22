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

package org.scijava.ops.image.filter.deriche;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Functions;
import org.scijava.ops.image.filter.FlatImage;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Edge detection with the recursive filters of Deriche, in any number of
 * dimensions.
 * <p>
 * R. Deriche, "Using Canny's criteria to derive a recursively implemented
 * optimal edge detector", International Journal of Computer Vision 1(2),
 * 1987, derived from J. Canny's criteria ("A computational approach to edge
 * detection", IEEE PAMI 8(6), 1986) the optimal derivative filter of infinite
 * extent {@code f(x) = -c x exp(-alpha |x|)}, and its integral, the smoothing
 * filter {@code h(x) = k (alpha |x| + 1) exp(-alpha |x|)}. Both are computed
 * exactly with second-order causal and anti-causal recursions, whatever the
 * scale. The partial derivative along a dimension is the derivative filter
 * along it and the smoothing filter along all other dimensions, which
 * extends the detector to 3D and beyond as in O. Monga, R. Deriche, G.
 * Malandain and J.-P. Cocquerez, "Recursive filtering and edge tracking: two
 * primary tools for 3D edge detection", Image and Vision Computing 9(4),
 * 1991.
 * </p>
 * <p>
 * The parameter {@code alpha} (in inverse physical units) sets the scale: the
 * smaller it is, the smoother the result; the filters roughly correspond to
 * Gaussian derivatives of standard deviation {@code sqrt(pi) / alpha}. The
 * smoothing filter is normalized to unit sum and the derivative filter to a
 * unit response to a unit ramp, so derivatives are in intensity per physical
 * unit. With a {@code calibration} (pixel spacing per dimension, default 1),
 * the filters along dimension {@code d} use {@code alpha * calibration[d]} per
 * pixel. The image is extended by border replication. Outputs have
 * zero-based coordinates.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class DericheFilters {

	private static final double TAN_22_5 = Math.tan(Math.PI / 8);

	private DericheFilters() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the Deriche partial derivative of an image along one dimension:
	 * the Deriche derivative filter along that dimension, and the Deriche
	 * smoothing filter along all others.
	 *
	 * @param input the image
	 * @param alpha the (positive) scale parameter of the filters, in inverse
	 *          physical units; smaller values smooth more
	 * @param dimension the dimension along which to differentiate
	 * @param calibration the pixel spacing along each dimension (default: 1 for
	 *          every dimension)
	 * @return the partial derivative, in intensity per physical unit
	 * @implNote op names='filter.derichePartialDerivative'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		partialDerivative( //
			final RandomAccessibleInterval<T> input, //
			final Double alpha, //
			final Integer dimension, //
			@Nullable final double[] calibration //
	) {
		final Grid grid = new Grid(input, alpha, calibration);
		if (dimension < 0 || dimension >= grid.dims.length) {
			throw new IllegalArgumentException("Invalid dimension: " + dimension);
		}
		return grid.image(grid.derivative(dimension));
	}

	/**
	 * Computes the magnitude of the Deriche gradient of an image: the Euclidean
	 * norm of its {@code filter.derichePartialDerivative} along all dimensions.
	 *
	 * @param input the image
	 * @param alpha the (positive) scale parameter of the filters, in inverse
	 *          physical units; smaller values smooth more
	 * @param calibration the pixel spacing along each dimension (default: 1 for
	 *          every dimension)
	 * @return the gradient magnitude, in intensity per physical unit
	 * @implNote op names='filter.cannyDeriche'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		gradientMagnitude( //
			final RandomAccessibleInterval<T> input, //
			final Double alpha, //
			@Nullable final double[] calibration //
	) {
		final Grid grid = new Grid(input, alpha, calibration);
		return grid.image(grid.magnitude(grid.gradient()));
	}

	/**
	 * Detects edges with the Canny method on the Deriche gradient: the gradient
	 * magnitude is thinned by non-maximum suppression along the gradient
	 * direction, then thresholded with hysteresis.
	 * <p>
	 * The gradient direction at each pixel is quantized to the nearest of its
	 * neighbors (a component counts if its magnitude is at least tan(22.5
	 * degrees) times the largest one, which gives the classical four directions
	 * in 2D). A pixel survives the non-maximum suppression if its gradient
	 * magnitude is larger than that of the backward neighbor, and not smaller
	 * than that of the forward neighbor, which gives one-pixel-thick edges even
	 * where two pixels have the same magnitude, such as at an ideal step. The surviving pixels with
	 * a magnitude of at least {@code high}, and those of at least {@code low}
	 * connected to them (with full connectivity), are the edges.
	 * </p>
	 *
	 * @param hysteresis the {@code threshold.hysteresis} op
	 * @param input the image
	 * @param alpha the (positive) scale parameter of the filters, in inverse
	 *          physical units; smaller values smooth more
	 * @param low the low threshold of the gradient magnitude
	 * @param high the high threshold of the gradient magnitude; at least
	 *          {@code low}
	 * @param calibration the pixel spacing along each dimension (default: 1 for
	 *          every dimension)
	 * @return the edge mask
	 * @implNote op names='segment.cannyDericheEdges'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<BitType>
		edges( //
			@OpDependency(
				name = "threshold.hysteresis") Functions.Arity3<RandomAccessibleInterval<DoubleType>, Double, Double, RandomAccessibleInterval<BitType>> hysteresis,
			final RandomAccessibleInterval<T> input, //
			final Double alpha, //
			final Double low, //
			final Double high, //
			@Nullable final double[] calibration //
	) {
		final Grid grid = new Grid(input, alpha, calibration);
		final double[][] gradient = grid.gradient();
		final double[] magnitude = grid.magnitude(gradient);
		return hysteresis.apply(grid.image(grid.suppressNonMaxima(gradient,
			magnitude)), low, high);
	}

	/** The image as a flat array, with the filters applying to it. */
	private static final class Grid {

		private final int[] dims;
		private final int[] strides;
		private final double[] data;
		private final double[] alphas;
		private final double[] spacing;

		Grid(final RandomAccessibleInterval<? extends RealType<?>> input,
			final double alpha, final double[] calibration)
		{
			if (!(alpha > 0) || Double.isInfinite(alpha)) {
				throw new IllegalArgumentException("Alpha must be positive: " + alpha);
			}
			final int n = input.numDimensions();
			if (calibration != null && calibration.length != n) {
				throw new IllegalArgumentException("Expected " + n +
					" calibration values");
			}
			data = FlatImage.read(input);
			dims = new int[n];
			strides = FlatImage.strides(input.dimensionsAsLongArray());
			alphas = new double[n];
			spacing = new double[n];
			for (int d = 0; d < n; d++) {
				dims[d] = (int) input.dimension(d);
				spacing[d] = calibration == null ? 1 : calibration[d];
				if (!(spacing[d] > 0)) {
					throw new IllegalArgumentException("Calibration must be positive");
				}
				alphas[d] = alpha * spacing[d];
			}
		}

		double[] derivative(final int dimension) {
			final double[] out = data.clone();
			for (int d = 0; d < dims.length; d++) {
				filterAlong(out, d, d == dimension);
			}
			final double scale = 1 / spacing[dimension];
			for (int i = 0; i < out.length; i++)
				out[i] *= scale;
			return out;
		}

		double[][] gradient() {
			final double[][] gradient = new double[dims.length][];
			for (int d = 0; d < dims.length; d++)
				gradient[d] = derivative(d);
			return gradient;
		}

		double[] magnitude(final double[][] gradient) {
			final double[] magnitude = new double[data.length];
			for (int i = 0; i < magnitude.length; i++) {
				double sum = 0;
				for (final double[] g : gradient)
					sum += g[i] * g[i];
				magnitude[i] = Math.sqrt(sum);
			}
			return magnitude;
		}

		double[] suppressNonMaxima(final double[][] gradient,
			final double[] magnitude)
		{
			final int n = dims.length;
			final double[] out = new double[magnitude.length];
			final int[] pos = new int[n];
			final double[] dir = new double[n];
			for (int i = 0; i < magnitude.length; i++) {
				final double m = magnitude[i];
				if (m > 0) {
					// Gradient direction in pixel coordinates, quantized to the
					// nearest neighbor offset.
					double max = 0;
					for (int d = 0; d < n; d++) {
						dir[d] = gradient[d][i] / spacing[d];
						max = Math.max(max, Math.abs(dir[d]));
					}
					int forward = i, backward = i;
					for (int d = 0; d < n; d++) {
						if (Math.abs(dir[d]) < TAN_22_5 * max) continue;
						final int step = dir[d] > 0 ? 1 : -1;
						if (pos[d] + step >= 0 && pos[d] + step < dims[d]) {
							forward += step * strides[d];
						}
						if (pos[d] - step >= 0 && pos[d] - step < dims[d]) {
							backward -= step * strides[d];
						}
					}
					if ((backward == i || m > magnitude[backward]) && m >=
						magnitude[forward]) out[i] = m;
				}
				for (int d = 0; d < n && ++pos[d] == dims[d]; d++)
					pos[d] = 0;
			}
			return out;
		}

		/** Applies the derivative or smoothing filter along one dimension. */
		private void filterAlong(final double[] values, final int d,
			final boolean derivative)
		{
			final int len = dims[d];
			final int stride = strides[d];
			final int block = stride * len;
			final double[] line = new double[len];
			final double[] causal = new double[len];
			for (int outer = 0; outer < values.length; outer += block) {
				for (int inner = 0; inner < stride; inner++) {
					final int start = outer + inner;
					for (int k = 0; k < len; k++)
						line[k] = values[start + k * stride];
					if (derivative) derive(line, causal, alphas[d]);
					else smooth(line, causal, alphas[d]);
					for (int k = 0; k < len; k++)
						values[start + k * stride] = line[k];
				}
			}
		}

		Img<DoubleType> image(final double[] values) {
			final long[] size = new long[dims.length];
			for (int d = 0; d < dims.length; d++)
				size[d] = dims[d];
			return ArrayImgs.doubles(values, size);
		}
	}

	/**
	 * Smooths a line in place with the recursive Deriche smoothing filter
	 * {@code h(x) = k (alpha |x| + 1) exp(-alpha |x|)}, normalized to unit sum,
	 * extending the line by border replication.
	 */
	static void smooth(final double[] x, final double[] causal,
		final double alpha)
	{
		final int len = x.length;
		final double a = Math.exp(-alpha);
		final double k = (1 - a) * (1 - a) / (1 + 2 * alpha * a - a * a);
		// h = k (alpha n + 1) a^n for n >= 0, and its mirror for n < 0.
		final double b0 = k, b1 = k * a * (alpha - 1);
		final double c1 = k * a * (alpha + 1), c2 = -k * a * a;
		final double a1 = 2 * a, a2 = -a * a;
		final double gain = 1 - a1 - a2;

		// Causal part, starting in the steady state of the first value.
		double xPrev = x[0];
		double y1 = (b0 + b1) * x[0] / gain, y2 = y1;
		for (int i = 0; i < len; i++) {
			final double y = b0 * x[i] + b1 * xPrev + a1 * y1 + a2 * y2;
			causal[i] = y;
			y2 = y1;
			y1 = y;
			xPrev = x[i];
		}
		// Anti-causal part, starting in the steady state of the last value.
		double xNext1 = x[len - 1], xNext2 = x[len - 1];
		y1 = (c1 + c2) * x[len - 1] / gain;
		y2 = y1;
		for (int i = len - 1; i >= 0; i--) {
			final double y = c1 * xNext1 + c2 * xNext2 + a1 * y1 + a2 * y2;
			y2 = y1;
			y1 = y;
			xNext2 = xNext1;
			xNext1 = x[i];
			x[i] = causal[i] + y;
		}
	}

	/**
	 * Differentiates a line in place with the recursive Deriche derivative
	 * filter {@code f(x) = -c x exp(-alpha |x|)}, normalized to a unit response
	 * to a unit ramp, extending the line by border replication.
	 */
	static void derive(final double[] x, final double[] causal,
		final double alpha)
	{
		final int len = x.length;
		final double a = Math.exp(-alpha);
		final double c = (1 - a) * (1 - a) * (1 - a) / (2 * a * (1 + a));
		final double a1 = 2 * a, a2 = -a * a;
		final double gain = 1 - a1 - a2;

		// Causal part: the response to n a^n for n >= 1.
		double xPrev = x[0];
		double y1 = a * x[0] / gain, y2 = y1;
		for (int i = 0; i < len; i++) {
			final double y = a * xPrev + a1 * y1 + a2 * y2;
			causal[i] = y;
			y2 = y1;
			y1 = y;
			xPrev = x[i];
		}
		// Anti-causal part: the mirrored response.
		double xNext = x[len - 1];
		y1 = a * x[len - 1] / gain;
		y2 = y1;
		for (int i = len - 1; i >= 0; i--) {
			final double y = a * xNext + a1 * y1 + a2 * y2;
			y2 = y1;
			y1 = y;
			xNext = x[i];
			x[i] = c * (y - causal[i]);
		}
	}
}
