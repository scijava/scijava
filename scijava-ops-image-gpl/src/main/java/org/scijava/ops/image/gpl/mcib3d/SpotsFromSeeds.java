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

package org.scijava.ops.image.gpl.mcib3d;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Seeded spot segmentation: grows one region (spot) from each seed, keeping
 * the connected pixels above a local threshold computed for that seed, in any
 * number of dimensions.
 * <p>
 * The seeds are processed in the given order (typically the local maxima of
 * the image, e.g. from {@code image.localExtrema}, in flat iteration order).
 * For each seed not already part of a spot, a local threshold is computed with
 * one of these methods:
 * </p>
 * <ul>
 * <li>{@code "constant"}: the given {@code value}.</li>
 * <li>{@code "diff"}: the seed value minus the given {@code value}.</li>
 * <li>{@code "mean"}: {@code w * spotMean + (1 - w) * backgroundMean}, where
 * {@code w} is the given {@code value} (default 0.5), {@code spotMean} the
 * mean in the ball of radius {@code radii[0]} (default 2) around the seed, and
 * {@code backgroundMean} the mean in the shell of radii {@code radii[1]} (inner,
 * inclusive, default 4) and {@code radii[2]} (outer, exclusive, default 6).
 * If the shell lies outside the image, the spot mean is used.</li>
 * <li>{@code "gaussian"}: the radial profile of the seed (the mean in the
 * unit-thick shells of radii 0 to {@code radii[0]}, default 10, mirrored to
 * make it symmetric) is fitted by a Gaussian {@code a + (b - a) exp(-(r - c)^2
 * / (2 sigma^2))}, and the threshold is the value of this Gaussian at
 * {@code value * sigma} (default 1.285, the 90th percentile of the normal
 * distribution). Seeds whose fit fails produce no spot.</li>
 * </ul>
 * <p>
 * Then the region grows from the seed to the pixels (fully connected: 8 in
 * 2D, 26 in 3D) not yet in a spot whose value is at least the threshold, with
 * one of these growth rules:
 * </p>
 * <ul>
 * <li>{@code "classical"}: no further condition; the spot is the connected
 * component of the seed.</li>
 * <li>{@code "maximum"}: a pixel joins only from a neighbor at least as
 * bright, so the spot only descends from the seed.</li>
 * <li>{@code "block"}: a spot pixel adds its candidate neighbors only if it has
 * at least two of them and none is brighter than itself, which stops the
 * growth at the valley between touching spots.</li>
 * </ul>
 * <p>
 * Spots whose pixel count lies outside {@code [minVolume, maxVolume]} are
 * discarded, and their pixels become available to later seeds, except when a
 * {@code "classical"} or {@code "block"} growth is stopped for exceeding
 * {@code maxVolume}: the pixels reached until then stay excluded from later
 * spots, as in the original. The seed pixel itself always belongs to its spot,
 * even below the threshold. With an integer image, the computed local
 * thresholds are truncated to integers and the {@code "diff"} threshold is at
 * least 1, as in the original.
 * </p>
 * <p>
 * This is a translation of {@code Segment3DSpots} of the
 * <a href="https://mcib3d.frama.io/3d-suite-imagej/">3D ImageJ Suite</a>
 * ({@code mcib3d-core}, GPL v3+) by Thomas Boudier, generalized to n-D; see J.
 * Ollion, J. Cochennec, F. Loll, C. Escud&eacute; and T. Boudier, "TANGO: a
 * generic tool for high-throughput 3D image analysis for studying nuclear
 * organization", Bioinformatics 29(14), 2013. Differences from the original:
 * the radii are in pixels, with no spatial calibration; the watershed option
 * (which confines each spot to the zone of influence of its seed) is not
 * supported; the Gaussian fit uses its own least-squares solver rather than
 * the ImageJ {@code CurveFitter}, so ill-conditioned fits may differ; the
 * {@code "diff"} threshold is based on the image value at the seed (the
 * original uses the value of the seed image, which is the same for seeds
 * found as local maxima); a seed whose Gaussian fit fails produces no spot
 * (the original reuses the threshold of the previous seed); the pixels of a
 * spot stopped for exceeding {@code maxVolume} are not reused as part of the
 * next spot (a quirk of the original); and the computed thresholds are not
 * truncated for non-integer images.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thomas Boudier
 * @param <T> image type
 * @param <P> seed type
 * @implNote op names='segment.spotsFromSeeds'
 */
public class SpotsFromSeeds<T extends RealType<T>, P extends Localizable>
	implements
	Functions.Arity8<RandomAccessibleInterval<T>, List<P>, String, Double, double[], String, Integer, Integer, ImgLabeling<Integer, IntType>>
{

	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	/**
	 * Segments one spot per seed.
	 *
	 * @param input the image
	 * @param seeds the seed positions, in the coordinates of {@code input}
	 * @param thresholdMethod how to compute the local threshold of each seed:
	 *          {@code "constant"} (default), {@code "diff"}, {@code "mean"} or
	 *          {@code "gaussian"}
	 * @param value the threshold for {@code "constant"} (required), the
	 *          difference from the seed value for {@code "diff"} (required), the
	 *          spot weight for {@code "mean"} (default 0.5), or the threshold
	 *          distance in standard deviations for {@code "gaussian"} (default
	 *          1.285)
	 * @param radii for {@code "mean"}, the radius of the spot ball and the inner
	 *          and outer radii of the background shell (default {2, 4, 6}); for
	 *          {@code "gaussian"}, the maximum radius of the radial profile
	 *          (default {10}); ignored otherwise
	 * @param growth the growth rule: {@code "classical"} (default),
	 *          {@code "maximum"} or {@code "block"}
	 * @param minVolume the minimum pixel count of a spot (default 2)
	 * @param maxVolume the maximum pixel count of a spot (default: no maximum)
	 * @return a labeling of the spots, numbered from 1 in the order of their
	 *         seeds
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply( //
		final RandomAccessibleInterval<T> input, //
		final List<P> seeds, //
		@Nullable final String thresholdMethod, //
		@Nullable final Double value, //
		@Nullable final double[] radii, //
		@Nullable final String growth, //
		@Nullable final Integer minVolume, //
		@Nullable final Integer maxVolume //
	) {
		final Segmenter s = new Segmenter(input, //
			method(thresholdMethod, value), value, radii, //
			growth(growth), //
			minVolume == null ? 2 : minVolume, //
			maxVolume == null ? Integer.MAX_VALUE : maxVolume);
		final int[] labels = s.segment(input, seeds);
		final ImgLabeling<Integer, IntType> output = labelingCreator.apply(input,
			new IntType());
		final Cursor<LabelingType<Integer>> c = Views.flatIterable(output)
			.cursor();
		int i = 0;
		while (c.hasNext()) {
			final LabelingType<Integer> l = c.next();
			if (labels[i] > 0) l.add(labels[i]);
			i++;
		}
		return output;
	}

	private static final int CONSTANT = 0, DIFF = 1, MEAN = 2, GAUSSIAN = 3;
	private static final int CLASSICAL = 0, MAXIMUM = 1, BLOCK = 2;

	private static int method(final String name, final Double value) {
		final String m = name == null ? "constant" : name.toLowerCase(Locale.ROOT);
		final int method;
		switch (m) {
			case "constant":
				method = CONSTANT;
				break;
			case "diff":
				method = DIFF;
				break;
			case "mean":
				method = MEAN;
				break;
			case "gaussian":
				method = GAUSSIAN;
				break;
			default:
				throw new IllegalArgumentException("Unknown threshold method: " + name);
		}
		if (value == null && (method == CONSTANT || method == DIFF)) {
			throw new IllegalArgumentException("The " + m +
				" threshold method needs a value");
		}
		return method;
	}

	private static int growth(final String name) {
		if (name == null) return CLASSICAL;
		switch (name.toLowerCase(Locale.ROOT)) {
			case "classical":
				return CLASSICAL;
			case "maximum":
				return MAXIMUM;
			case "block":
				return BLOCK;
			default:
				throw new IllegalArgumentException("Unknown growth rule: " + name);
		}
	}

	/** Marks the pixels of a spot stopped for exceeding the maximum volume. */
	private static final int CLAIMED = -1;

	private static final class Segmenter {

		private final int n;
		private final int[] dims;
		private final int[] strides;
		private final double[] v;
		private final int[] label;
		private final boolean integer;
		private final int method;
		private final double value;
		private final double[] radii;
		private final int growth;
		private final int minVolume;
		private final int maxVolume;

		/** Flat offsets and coordinate deltas of the 3^n neighborhood. */
		private final int[] offsets;
		private final int[][] deltas;

		// Region growing state.
		private final int[] lo, hi, pos;
		private int sens;
		private boolean change;
		private int val;
		private double threshold;
		private int[] object = new int[16];
		private int volume;

		Segmenter(final RandomAccessibleInterval<? extends RealType<?>> input,
			final int method, final Double value, final double[] radii,
			final int growth, final int minVolume, final int maxVolume)
		{
			n = input.numDimensions();
			dims = new int[n];
			strides = new int[n];
			long total = 1;
			for (int d = 0; d < n; d++) {
				dims[d] = (int) input.dimension(d);
				strides[d] = (int) total;
				total *= input.dimension(d);
			}
			if (total > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Image too large");
			}
			v = new double[(int) total];
			int i = 0;
			for (final RealType<?> t : Views.flatIterable(input))
				v[i++] = t.getRealDouble();
			label = new int[v.length];
			integer = Util.getTypeFromInterval(input) instanceof IntegerType;
			this.method = method;
			this.value = value == null ? (method == MEAN ? 0.5 : 1.285) : value;
			if (radii == null) {
				this.radii = method == MEAN ? new double[] { 2, 4, 6 } : new double[] {
					10 };
			}
			else {
				final int needed = method == MEAN ? 3 : method == GAUSSIAN ? 1 : 0;
				if (radii.length < needed) {
					throw new IllegalArgumentException("Expected " + needed + " radii");
				}
				this.radii = radii;
			}
			this.growth = growth;
			this.minVolume = minVolume;
			this.maxVolume = maxVolume;

			int count = 1;
			for (int d = 0; d < n; d++)
				count *= 3;
			offsets = new int[count];
			deltas = new int[count][n];
			for (int k = 0; k < count; k++) {
				int r = k;
				for (int d = 0; d < n; d++) {
					deltas[k][d] = r % 3 - 1;
					r /= 3;
					offsets[k] += deltas[k][d] * strides[d];
				}
			}
			lo = new int[n];
			hi = new int[n];
			pos = new int[n];
		}

		int[] segment(final RandomAccessibleInterval<?> input,
			final List<? extends Localizable> seeds)
		{
			val = 1;
			for (final Localizable seed : seeds) {
				final int[] p = new int[n];
				int index = 0;
				for (int d = 0; d < n; d++) {
					p[d] = (int) (seed.getLongPosition(d) - input.min(d));
					if (p[d] < 0 || p[d] >= dims[d]) {
						throw new IllegalArgumentException("Seed outside the image: " +
							seed);
					}
					index += p[d] * strides[d];
				}
				if (label[index] != 0) continue;
				final double t = localThreshold(p, index);
				if (Double.isNaN(t)) continue;
				threshold = t;
				if (!grow(p, index)) {
					for (int k = 0; k < volume; k++)
						label[object[k]] = CLAIMED;
				}
				else if (volume >= minVolume && volume <= maxVolume) {
					val++;
				}
				else {
					for (int k = 0; k < volume; k++)
						label[object[k]] = 0;
				}
			}
			for (int k = 0; k < label.length; k++)
				if (label[k] == CLAIMED) label[k] = 0;
			return label;
		}

		// -- Local thresholds --

		private double localThreshold(final int[] p, final int index) {
			switch (method) {
				case CONSTANT:
					return value;
				case DIFF:
					return integer ? Math.max(1, (long) (v[index] - value)) : v[index] -
						value;
				case MEAN:
					return truncate(localMean(p));
				default:
					return truncate(gaussianThreshold(p));
			}
		}

		private double truncate(final double t) {
			return integer && !Double.isNaN(t) ? (long) t : t;
		}

		private double localMean(final int[] p) {
			final double r0 = radii[0];
			final double[] spot = shellSum(p, 0, r0 == 0 ? 1 : r0 * r0, true, (int) Math
				.ceil(r0));
			final double[] back = shellSum(p, radii[1] * radii[1], radii[2] *
				radii[2], false, (int) Math.ceil(radii[2]));
			final double mSpot = spot[0] / spot[1];
			if (back[1] == 0) return mSpot;
			final double mBack = back[0] / back[1];
			return mSpot * value + (1 - value) * mBack;
		}

		private double gaussianThreshold(final int[] p) {
			final int maxR = (int) radii[0];
			final double[] profile = new double[2 * maxR + 1];
			for (int r = 0; r <= maxR; r++) {
				final double[] s = shellSum(p, r * r, (r + 1) * (r + 1), false, r + 1);
				final double m = s[1] == 0 ? Double.NaN : s[0] / s[1];
				profile[maxR - r] = m;
				profile[maxR + r] = m;
			}
			int cm = 0;
			while (Double.isNaN(profile[cm]))
				cm++;
			final int len = profile.length - 2 * cm;
			final double[] x = new double[len];
			final double[] y = new double[len];
			for (int i = 0; i < len; i++) {
				x[i] = i - (maxR - cm);
				y[i] = profile[cm + i];
			}
			final double[] g = ProfileGaussianFit.fit(x, y, 0, 3);
			if (g == null) return Double.NaN;
			final double dx = value * g[3] - g[2];
			return g[0] + (g[1] - g[0]) * Math.exp(-dx * dx / (2 * g[3] * g[3]));
		}

		/**
		 * Sums the pixels at squared distance {@code d2} from {@code p} with
		 * {@code min <= d2 < max} (or {@code d2 <= max} if {@code inclusive}),
		 * within the box of the given half size.
		 *
		 * @return {sum, count}
		 */
		private double[] shellSum(final int[] p, final double min,
			final double max, final boolean inclusive, final int half)
		{
			final int[] q = new int[n];
			final int[] qMin = new int[n];
			final int[] qMax = new int[n];
			for (int d = 0; d < n; d++) {
				qMin[d] = Math.max(0, p[d] - half);
				qMax[d] = Math.min(dims[d] - 1, p[d] + half);
				q[d] = qMin[d];
			}
			double sum = 0;
			long count = 0;
			while (true) {
				double d2 = 0;
				int index = 0;
				for (int d = 0; d < n; d++) {
					final double dd = q[d] - p[d];
					d2 += dd * dd;
					index += q[d] * strides[d];
				}
				if (d2 >= min && (inclusive ? d2 <= max : d2 < max)) {
					sum += v[index];
					count++;
				}
				int d = 0;
				while (d < n && ++q[d] > qMax[d]) {
					q[d] = qMin[d];
					d++;
				}
				if (d == n) break;
			}
			return new double[] { sum, count };
		}

		// -- Region growing --

		/**
		 * Grows the spot of the current label from a seed, alternating forward
		 * and backward raster scans of the growing bounding box until no pixel is
		 * added, like the original.
		 *
		 * @return false if the growth was stopped for exceeding the maximum
		 *         volume
		 */
		private boolean grow(final int[] p, final int index) {
			for (int d = 0; d < n; d++) {
				lo[d] = p[d];
				hi[d] = p[d] + 1;
			}
			label[index] = val;
			volume = 0;
			add(index);
			sens = 1;
			change = true;
			while (change) {
				change = false;
				if (!scan(n - 1)) return false;
				sens = -sens;
			}
			return true;
		}

		private boolean scan(final int d) {
			if (sens > 0) {
				for (pos[d] = lo[d]; pos[d] <= hi[d]; pos[d]++) {
					if (!(d == 0 ? visit() : scan(d - 1))) return false;
				}
			}
			else {
				for (pos[d] = hi[d]; pos[d] >= lo[d]; pos[d]--) {
					if (!(d == 0 ? visit() : scan(d - 1))) return false;
				}
			}
			return true;
		}

		private boolean visit() {
			int index = 0;
			for (int d = 0; d < n; d++) {
				if (pos[d] < 0 || pos[d] >= dims[d]) return true;
				index += pos[d] * strides[d];
			}
			if (label[index] != val) return true;
			final double center = v[index];
			if (growth == BLOCK) return visitBlock(index, center);
			for (int k = 0; k < offsets.length; k++) {
				if (!inside(deltas[k])) continue;
				final int q = index + offsets[k];
				if (label[q] != 0 || v[q] < threshold) continue;
				if (growth == MAXIMUM && v[q] > center) continue;
				label[q] = val;
				add(q);
				if (growth == CLASSICAL && volume > maxVolume) return false;
				extend(deltas[k]);
				change = true;
			}
			return true;
		}

		private boolean visitBlock(final int index, final double center) {
			int count = 0;
			boolean ok = true;
			final int[] neigh = new int[offsets.length];
			for (int k = 0; k < offsets.length; k++) {
				if (!inside(deltas[k])) continue;
				final int q = index + offsets[k];
				if (label[q] != 0 || v[q] < threshold) continue;
				neigh[count++] = k;
				if (v[q] > center) ok = false;
			}
			if (count < 2 || !ok) return true;
			change = true;
			if (count > maxVolume) return false;
			for (int i = 0; i < count; i++) {
				final int k = neigh[i];
				final int q = index + offsets[k];
				label[q] = val;
				add(q);
				if (volume > maxVolume) return false;
				extend(deltas[k]);
			}
			return true;
		}

		private boolean inside(final int[] delta) {
			for (int d = 0; d < n; d++) {
				final int q = pos[d] + delta[d];
				if (q < 0 || q >= dims[d]) return false;
			}
			return true;
		}

		/** Extends the bounding box to the neighbor at the given delta. */
		private void extend(final int[] delta) {
			for (int d = 0; d < n; d++) {
				final int q = pos[d] + delta[d];
				if (q < lo[d]) lo[d]--;
				if (q > hi[d]) hi[d]++;
			}
		}

		private void add(final int index) {
			if (volume == object.length) {
				object = Arrays.copyOf(object, 2 * volume);
			}
			object[volume++] = index;
		}
	}
}
