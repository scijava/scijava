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

package org.scijava.ops.image.gpl.biovoxxel;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Binarizes an image with the histogram-limiting methods MoLiM (mode limited
 * mean) and DiLiM (differential limited mean): the histogram is first limited
 * by removing the bins up to its mode (MoLiM), or, with differential
 * limitation, up to its median when the median is closer to the mode than to
 * the mean (DiLiM); the threshold is then the mean of the remaining
 * histogram.
 * <p>
 * The mode is taken as the background: when it is above the mean (bright
 * background), the histogram is reversed first, so the foreground is then the
 * pixels darker than the threshold. With {@code force}, the opposite partition
 * is taken as the foreground (the smaller partition in the plugin dialog);
 * this option is ignored when the mode is in the first or last bin, as in the
 * plugin. When both the mode and the median are in the first bin, the
 * differential method removes the first bin, then all bins up to the mean of
 * the rest.
 * </p>
 * <p>
 * Images of {@link UnsignedByteType} use one bin per gray level, like the
 * plugin (which only handles 8-bit images); other types use 256 bins between
 * the minimum and maximum of the image. The median is computed as in the
 * plugin: one past the last bin whose cumulative count is at most half the
 * pixel count, except that it is 0 when that bin is the first one.
 * </p>
 * <p>
 * This is a translation of the {@code Limited_Mean} plugin of the
 * <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 * Toolbox</a> by Jan Brocher (GPL v3+).
 * </p>
 * <p>
 * Reference: J. Brocher, "Qualitative and Quantitative Evaluation of Two New
 * Histogram Limiting Binarization Algorithms", International Journal of Image
 * Processing 8(2):30-48, 2014.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jan Brocher
 * @param <T> type of the input image
 * @implNote op names='threshold.limitedMean'
 */
public class LimitedMeanThreshold<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Boolean, Boolean, RandomAccessibleInterval<BitType>>
{

	private static final int BINS = 256;

	/**
	 * Binarizes the image.
	 *
	 * @param input the image to binarize
	 * @param differential whether to use the differential limitation (DiLiM)
	 *          instead of the mode limitation (MoLiM); default true, as in the
	 *          plugin dialog
	 * @param force whether to take the other partition as the foreground
	 *          (default false)
	 * @param output the binary result, true for the foreground
	 */
	@Override
	public void compute( //
		final RandomAccessibleInterval<T> input, //
		@Nullable final Boolean differential, //
		@Nullable final Boolean force, //
		final RandomAccessibleInterval<BitType> output //
	) {
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		// Map each value to its bin.
		final double min, scale;
		if (input.getType() instanceof UnsignedByteType) {
			min = 0;
			scale = 1;
		}
		else {
			double lo = Double.POSITIVE_INFINITY, hi = Double.NEGATIVE_INFINITY;
			for (final T t : Views.iterable(input)) {
				final double v = t.getRealDouble();
				if (v < lo) lo = v;
				if (v > hi) hi = v;
			}
			min = lo;
			scale = hi > lo ? BINS / (hi - lo) : 0;
		}
		final long[] hist = new long[BINS];
		for (final T t : Views.iterable(input))
			hist[bin(t.getRealDouble(), min, scale)]++;

		final int[] result = threshold(hist, differential == null || differential,
			Boolean.TRUE.equals(force));
		final int t = result[0];
		final boolean inverted = result[1] != 0;
		LoopBuilder.setImages(input, output).forEachPixel((in, out) -> {
			final int b = bin(in.getRealDouble(), min, scale);
			out.set((inverted ? BINS - 1 - b : b) > t);
		});
	}

	private static int bin(final double v, final double min,
		final double scale)
	{
		final int b = (int) ((v - min) * scale);
		return Math.max(0, Math.min(BINS - 1, b));
	}

	/**
	 * Computes the limited mean threshold of a histogram.
	 *
	 * @return the threshold bin, and 1 if the threshold applies to the reversed
	 *         histogram (0 otherwise); bins above the threshold are foreground
	 */
	static int[] threshold(final long[] histogram, final boolean differential,
		final boolean force)
	{
		final int n = histogram.length;
		long[] hist = histogram.clone();
		double modal = mode(hist);
		double mean = mean(hist);
		// Note: The plugin does not offer the force option when the mode is in
		// the first or last bin.
		final boolean forced = force && modal != 0 && modal != n - 1;
		final boolean inverted = (modal > mean) != forced;
		if (inverted) {
			final long[] reversed = new long[n];
			for (int i = 0; i < n; i++)
				reversed[i] = hist[n - 1 - i];
			hist = reversed;
			modal = mode(hist);
			mean = mean(hist);
		}
		final double median = median(hist);

		if (modal == 0 && median == 0 && differential) {
			hist[0] = 0;
			final double intermediateMean = mean(hist);
			for (int b = 0; b <= intermediateMean && b < n; b++)
				hist[b] = 0;
		}
		else if (modal == 0 && median > 0 && differential) {
			clear(hist, median);
		}
		else if (Math.abs(median - modal) < Math.abs(mean - median) &&
			differential)
		{
			clear(hist, median);
		}
		else {
			clear(hist, modal);
		}
		final double newMean = mean(hist);
		// Note: A NaN mean (empty histogram) gives 0, as the int cast does.
		return new int[] { (int) newMean, inverted ? 1 : 0 };
	}

	/** Clears the bins up to {@code limit}, inclusive. */
	private static void clear(final long[] hist, final double limit) {
		for (int b = 0; b <= limit && b < hist.length; b++)
			hist[b] = 0;
	}

	/** Returns the first bin with the highest count. */
	private static int mode(final long[] hist) {
		int mode = 0;
		long max = 0;
		for (int i = 0; i < hist.length; i++) {
			if (hist[i] > max) {
				max = hist[i];
				mode = i;
			}
		}
		return mode;
	}

	private static double mean(final long[] hist) {
		double sum = 0, count = 0;
		for (int i = 0; i < hist.length; i++) {
			sum += (double) hist[i] * i;
			count += hist[i];
		}
		return sum / count;
	}

	/** The median as computed by the plugin; see the class description. */
	private static int median(final long[] hist) {
		long total = 0;
		for (final long h : hist)
			total += h;
		final long half = total / 2;
		long cumulative = 0;
		int last = 0;
		for (int i = 0; i < hist.length; i++) {
			cumulative += hist[i];
			if (cumulative <= half) last = i;
		}
		return last == 0 ? 0 : last + 1;
	}
}
