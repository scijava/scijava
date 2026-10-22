/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.directionality;

import java.util.Arrays;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * Measures the preferred orientation of the structures of a 2D image: a
 * histogram of the amount of structures in each direction, and a Gaussian fit
 * of its highest peak giving the dominant direction, its dispersion, the
 * amount of structures in that direction and the goodness of the fit. Images
 * with isotropic content give a flat histogram, while images with a preferred
 * orientation give a peak at that orientation.
 * <p>
 * Two methods are available. With
 * {@link DirectionalityMethod#FOURIER_COMPONENTS} (the default), the image is
 * cut into square blocks, each windowed by a Blackman window and padded to a
 * power of two; the power spectrum of each block is then measured in angular
 * wedges using the spatial filters of Liu (1991). Structures with a preferred
 * orientation give a periodic pattern at +90 degrees in the Fourier spectrum.
 * With {@link DirectionalityMethod#LOCAL_GRADIENT_ORIENTATION}, the gradient
 * is computed with 5x5 Sobel kernels, and each pixel adds its squared gradient
 * magnitude to the bin of its orientation (rotated by 90 degrees, so that it
 * is the orientation of the structure and not of its normal).
 * </p>
 * <p>
 * The highest peak of the histogram is fitted by
 * {@code a + (b - a) exp(-(x - c)^2 / (2 d^2))}, as the plugin does with the
 * ImageJ {@code CurveFitter}; the fit does not wrap around the ends of the
 * histogram, so a peak near the ends of the analyzed range is fitted poorly.
 * </p>
 * <p>
 * This is a translation of the Fiji
 * <a href="https://github.com/fiji/Directionality">Directionality</a> plugin
 * by Jean-Yves Tinevez (GPL v3+), without its display parts and without the
 * orientation map. Deviations from the plugin: the analysis is of one 2D
 * plane; the fit is computed with a Nelder-Mead search instead of the ImageJ
 * {@code Minimizer}, which reaches the same least-squares optimum; and the
 * amount takes the 180 degree periodicity of the histogram into account, as
 * documented by the plugin (whose implementation compares radians with
 * degrees, so it never wraps). With
 * {@link DirectionalityMethod#LOCAL_GRADIENT_ORIENTATION}, each pixel goes to
 * the bin whose center is nearest to its orientation; the plugin truncates
 * instead, so its bins collect the orientations in
 * {@code [center, center + width)} and its histogram and fitted direction are
 * offset by half a bin (about 1 degree with the default 90 bins). The first
 * and last bins thus cover only half a bin width each.
 * </p>
 * <p>
 * Reference: Z.-Q. Liu, "Scale space approach to directional analysis of
 * images", Applied Optics 30(11):1369-1373, 1991.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jean-Yves Tinevez
 * @param <T> type of the input image
 * @implNote op names='features.directionality'
 */
public class DefaultDirectionality<T extends RealType<T>> implements
	Functions.Arity5<RandomAccessibleInterval<T>, DirectionalityMethod, Integer, Double, Double, Directionality>
{

	/** Spectrum pixels closer than this to the zero frequency are ignored. */
	private static final double FREQ_THRESHOLD = 5.0;

	/** Number of standard deviations around the center summed for the amount. */
	private static final double SIGMA_NUMBER = 2;

	/**
	 * Measures the directionality of a 2D image.
	 *
	 * @param input the 2D image to analyze
	 * @param method the analysis method (defaults to
	 *          {@link DirectionalityMethod#FOURIER_COMPONENTS})
	 * @param binCount the number of bins of the histogram (default 90, at least
	 *          2)
	 * @param binStart the direction of the first bin, in degrees (default -90)
	 * @param binEnd the direction of the last bin, in degrees (defaults to 180
	 *          degrees after the start, which is also its maximum)
	 * @return the directionality histogram and its Gaussian fit
	 */
	@Override
	public Directionality apply( //
		final RandomAccessibleInterval<T> input, //
		@Nullable final DirectionalityMethod method, //
		@Nullable final Integer binCount, //
		@Nullable final Double binStart, //
		@Nullable final Double binEnd //
	) {
		if (input.numDimensions() != 2) {
			throw new IllegalArgumentException(
				"Directionality requires a 2D image, not " + input.numDimensions() +
					"D");
		}
		final int nBins = binCount == null ? 90 : binCount;
		if (nBins < 2) {
			throw new IllegalArgumentException("At least 2 bins are needed: " +
				nBins);
		}
		final double start = binStart == null ? -90 : binStart;
		final double end = binEnd == null ? start + 180 : Math.min(binEnd, start +
			180);
		if (!(end > start)) {
			throw new IllegalArgumentException("The bin end (" + end +
				") must be greater than the bin start (" + start + ")");
		}
		final int width = (int) input.dimension(0);
		final int height = (int) input.dimension(1);
		final float[] pixels = new float[width * height];
		final Cursor<T> c = Views.flatIterable(input).cursor();
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = c.next().getRealFloat();

		// The bin centers, in radians.
		final double[] bins = new double[nBins];
		for (int i = 0; i < nBins; i++)
			bins[i] = (start + i * (end - start) / (nBins - 1)) / 180. * Math.PI;

		final double[] dir = method == DirectionalityMethod.LOCAL_GRADIENT_ORIENTATION
			? gradientHistogram(pixels, width, height, bins, start, end)
			: fourierHistogram(pixels, width, height, bins);
		double sum = 0;
		for (final double v : dir)
			sum += v;
		for (int j = 0; j < dir.length; j++)
			dir[j] /= sum;

		final GaussianFit fit = new GaussianFit(bins, dir);
		final double center = fit.params[2];
		final double std = fit.params[3];
		double amount = 0;
		for (int j = 0; j < dir.length; j++) {
			double xn = bins[j];
			if (Math.abs(xn - center) > Math.PI / 2) xn += xn > center ? -Math.PI
				: Math.PI;
			if (xn >= center - SIGMA_NUMBER * std && xn <= center + SIGMA_NUMBER *
				std) amount += dir[j];
		}

		final double[] degrees = new double[nBins];
		for (int j = 0; j < nBins; j++)
			degrees[j] = 180 * bins[j] / Math.PI;
		return new Directionality(degrees, dir, fit.params[0], fit.params[1], Math
			.toDegrees(center), Math.toDegrees(std), amount, fit.goodness);
	}

	// -- Local gradient orientation --

	private static double[] gradientHistogram(final float[] pixels,
		final int width, final int height, final double[] bins,
		final double binStart, final double binEnd)
	{
		final int nBins = bins.length;
		final double[] normDir = new double[nBins];
		// Note: The kernels are swapped and transposed relative to the usual
		// Sobel naming, to get the orientation of the structures (90 degrees from
		// the gradient), as in the plugin.
		final float[] kernelY = { //
			-5f, -4f, 0f, 4f, 5f, //
			-8f, -10f, 0f, 10f, 8f, //
			-10f, -20f, 0f, 20f, 10f, //
			-8f, -10f, 0f, 10f, 8f, //
			-5f, -4f, 0f, 4f, 5f };
		final float[] kernelX = { //
			5f, 8f, 10f, 8f, 5f, //
			4f, 10f, 20f, 10f, 4f, //
			0f, 0f, 0f, 0f, 0f, //
			-4f, -10f, -20f, -10f, -4f, //
			-5f, -8f, -10f, -8f, -5f };
		final float[] gradX = correlate5x5(pixels, width, height, kernelX);
		final float[] gradY = correlate5x5(pixels, width, height, kernelY);

		// The range of allowed angles, wrapped within [-90, 90] degrees.
		final double wrappedStart = ((binStart + 90) % 180 + 180) % 180 - 90;
		final double wrappedEnd = ((binEnd + 90) % 180 + 180) % 180 - 90;
		final double range1Min, range1Max, range2Min, range2Max;
		if (wrappedEnd <= wrappedStart) {
			range1Min = -90;
			range1Max = wrappedEnd;
			range2Min = wrappedStart;
			range2Max = 90;
		}
		else {
			range1Min = wrappedStart;
			range1Max = wrappedEnd;
			range2Min = wrappedStart;
			range2Max = wrappedEnd;
		}
		final double binWidth = (bins[1] - bins[0]) * 180.0 / Math.PI;
		for (int i = 0; i < gradX.length; i++) {
			// Note: Float arithmetic, as in the plugin.
			final float dx = gradX[i];
			final float dy = -gradY[i]; // upright orientation
			final double norm = dx * dx + dy * dy;
			final double angle = Math.atan(dy / dx) * 180.0 / Math.PI;
			if ((angle >= range1Min && angle <= range1Max) || (angle >= range2Min &&
				angle <= range2Max))
			{
				double angleRef = angle;
				while (angleRef > binEnd)
					angleRef -= 180;
				while (angleRef < binStart)
					angleRef += 180;
				// Note: The plugin truncates here, which fills each bin with the
				// angles in [center, center + width) and so biases the histogram by
				// half a bin. Rounding assigns each angle to its nearest bin center.
				final int index = (int) Math.round((angleRef - binStart) / binWidth);
				normDir[Math.min(index, nBins - 1)] += norm;
			}
		}
		return normDir;
	}

	/**
	 * Correlates the image with a 5x5 kernel, extending the image by its edge
	 * pixels, like the ImageJ {@code Convolver} with a zero-sum kernel.
	 */
	private static float[] correlate5x5(final float[] pixels, final int width,
		final int height, final float[] kernel)
	{
		final float[] out = new float[pixels.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double sum = 0;
				int i = 0;
				for (int v = -2; v <= 2; v++) {
					final int yy = Math.min(Math.max(y + v, 0), height - 1);
					for (int u = -2; u <= 2; u++) {
						final int xx = Math.min(Math.max(x + u, 0), width - 1);
						sum += pixels[xx + yy * width] * kernel[i++];
					}
				}
				out[x + y * width] = (float) sum;
			}
		}
		return out;
	}

	// -- Fourier components --

	private static double[] fourierHistogram(final float[] pixels,
		final int width, final int height, final double[] bins)
	{
		final int nBins = bins.length;
		// Cut the image into overlapping square blocks covering it.
		final int smallSide = Math.min(width, height);
		final int nPadX, nPadY, step;
		if (width == height) {
			nPadX = 1;
			nPadY = 1;
			step = 0;
		}
		else {
			final int longSide = Math.max(width, height);
			final int nPad = longSide / smallSide + 1;
			nPadX = width == longSide ? nPad : 1;
			nPadY = width == longSide ? 1 : nPad;
			step = (longSide - smallSide) / (nPad - 1);
		}
		int padSize = 2;
		while (padSize < smallSide)
			padSize *= 2;
		final int offset = (padSize - smallSide) / 2;

		final float[] window = blackmanWindow(smallSide);
		final float[][] filters = makeFilters(padSize, bins);

		final double[] dir = new double[nBins];
		final double[] re = new double[padSize * padSize];
		final double[] im = new double[padSize * padSize];
		final float[] spectrum = new float[padSize * padSize];
		for (int ix = 0; ix < nPadX; ix++) {
			for (int iy = 0; iy < nPadY; iy++) {
				// Window the block and pad it to a power of two.
				Arrays.fill(re, 0);
				Arrays.fill(im, 0);
				for (int y = 0; y < smallSide; y++) {
					for (int x = 0; x < smallSide; x++) {
						final float v = pixels[(ix * step + x) + (iy * step + y) * width] *
							window[x + y * smallSide];
						re[(x + offset) + (y + offset) * padSize] = v;
					}
				}
				fft2D(re, im, padSize);
				// Power spectrum, with the zero frequency at the center.
				final int half = padSize / 2;
				for (int r = 0; r < padSize; r++) {
					final int sr = (r + half) % padSize;
					for (int col = 0; col < padSize; col++) {
						final int k = (col + half) % padSize + sr * padSize;
						spectrum[col + r * padSize] = (float) (re[k] * re[k] + im[k] *
							im[k]);
					}
				}
				for (int bin = 0; bin < nBins; bin++) {
					final float[] f = filters[bin];
					for (int i = 0; i < spectrum.length; i++)
						dir[bin] += spectrum[i] * f[i];
				}
			}
		}
		return dir;
	}

	/**
	 * Separable 2D Blackman window. Note: The cosine of the third term does not
	 * depend on the position (it should be {@code cos(4 pi i / n)}); this is
	 * kept from the plugin to give identical results.
	 */
	private static float[] blackmanWindow(final int n) {
		final double[] w = new double[n];
		for (int i = 0; i < n; i++)
			w[i] = 0.42 - 0.5 * Math.cos(2 * Math.PI * i / n) + 0.08 * Math.cos(4 *
				Math.PI / n);
		final float[] window = new float[n * n];
		for (int i = 0; i < window.length; i++)
			window[i] = (float) (w[i % n] * w[i / n]);
		return window;
	}

	/** The angular filters of Liu (1991), one per bin, centered spectra. */
	private static float[][] makeFilters(final int padSize, final double[] bins) {
		final int nBins = bins.length;
		final int center = padSize / 2;
		final float[] r = new float[padSize * padSize];
		final float[] theta = new float[padSize * padSize];
		for (int i = 0; i < r.length; i++) {
			final int dy = i / padSize - center;
			final int dx = i % padSize - center;
			r[i] = (float) Math.sqrt(dx * dx + dy * dy);
			theta[i] = (float) Math.atan2(-dy, dx); // upright orientation
		}
		final double thetaBw = bins[1] - bins[0];
		final double rc = padSize / 4;
		final double rBw = rc / 2;
		final float[][] filters = new float[nBins][];
		for (int b = 0; b < nBins; b++) {
			final float[] f = new float[padSize * padSize];
			double thetaC = bins[b];
			while (thetaC >= Math.PI)
				thetaC -= Math.PI;
			while (thetaC < 0)
				thetaC += Math.PI;
			thetaC -= Math.PI / 2;
			for (int i = 0; i < f.length; i++) {
				final double cr = r[i];
				if (cr < FREQ_THRESHOLD || cr > padSize / 2) continue;
				final double radial = Math.exp(-(cr - rc) * (cr - rc) / (rBw * rBw));
				final double ct = theta[i];
				final double delta;
				if (Math.abs(ct - thetaC) < thetaBw) delta = ct - thetaC;
				else if (Math.abs(ct - (thetaC - Math.PI)) < thetaBw) delta = ct -
					(thetaC - Math.PI);
				else if (Math.abs(ct - (thetaC + Math.PI)) < thetaBw) delta = ct -
					(thetaC + Math.PI);
				else continue;
				final double angular = Math.cos(delta / thetaBw * Math.PI / 2.0);
				f[i] = (float) (angular * angular * radial);
			}
			filters[b] = f;
		}
		return filters;
	}

	/** In-place 2D FFT of a square power-of-two array, rows then columns. */
	private static void fft2D(final double[] re, final double[] im,
		final int n)
	{
		final double[] rowRe = new double[n];
		final double[] rowIm = new double[n];
		for (int y = 0; y < n; y++) {
			System.arraycopy(re, y * n, rowRe, 0, n);
			System.arraycopy(im, y * n, rowIm, 0, n);
			fft1D(rowRe, rowIm);
			System.arraycopy(rowRe, 0, re, y * n, n);
			System.arraycopy(rowIm, 0, im, y * n, n);
		}
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < n; y++) {
				rowRe[y] = re[x + y * n];
				rowIm[y] = im[x + y * n];
			}
			fft1D(rowRe, rowIm);
			for (int y = 0; y < n; y++) {
				re[x + y * n] = rowRe[y];
				im[x + y * n] = rowIm[y];
			}
		}
	}

	/** In-place iterative radix-2 FFT. */
	private static void fft1D(final double[] re, final double[] im) {
		final int n = re.length;
		for (int i = 1, j = 0; i < n; i++) {
			int bit = n >> 1;
			for (; (j & bit) != 0; bit >>= 1)
				j ^= bit;
			j ^= bit;
			if (i < j) {
				double t = re[i];
				re[i] = re[j];
				re[j] = t;
				t = im[i];
				im[i] = im[j];
				im[j] = t;
			}
		}
		for (int len = 2; len <= n; len <<= 1) {
			final double angle = -2 * Math.PI / len;
			final double wRe = Math.cos(angle), wIm = Math.sin(angle);
			for (int i = 0; i < n; i += len) {
				double cRe = 1, cIm = 0;
				for (int k = 0; k < len / 2; k++) {
					final int a = i + k, b = a + len / 2;
					final double tRe = re[b] * cRe - im[b] * cIm;
					final double tIm = re[b] * cIm + im[b] * cRe;
					re[b] = re[a] - tRe;
					im[b] = im[a] - tIm;
					re[a] += tRe;
					im[a] += tIm;
					final double nRe = cRe * wRe - cIm * wIm;
					cIm = cRe * wIm + cIm * wRe;
					cRe = nRe;
				}
			}
		}
	}
}
