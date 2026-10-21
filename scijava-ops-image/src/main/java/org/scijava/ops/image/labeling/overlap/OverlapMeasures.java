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

package org.scijava.ops.image.labeling.overlap;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

/**
 * Measures of agreement between two labelings of the same image, such as a
 * segmentation (the source) and a ground truth (the target), following the
 * definitions of N. J. Tustison and J. C. Gee ("Introducing Dice, Jaccard,
 * and other label overlap measures to ITK", <i>The Insight Journal</i>,
 * 2009).
 * <p>
 * Labels are matched by equality: for each label {@code r}, {@code S_r} is
 * the set of pixels carrying {@code r} in the source and {@code T_r} the set
 * of pixels carrying {@code r} in the target. The per-label ops give one
 * value for each label occurring in either labeling (which may be
 * {@code NaN} when undefined, e.g. a target overlap for a label absent from
 * the target). The total ops sum numerators and denominators over all
 * labels, e.g. the total Jaccard index is
 * {@code Σ|S_r ∩ T_r| / Σ|S_r ∪ T_r|}. Both labelings must have the same
 * dimensions.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class OverlapMeasures {

	private OverlapMeasures() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the total target overlap (sensitivity), {@code Σ|S_r ∩ T_r| /
	 * Σ|T_r|}.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return the total target overlap, between 0 and 1
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.targetOverlap', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>> Double
		targetOverlap(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).total(Measure.TARGET_OVERLAP);
	}

	/**
	 * Computes the target overlap {@code |S_r ∩ T_r| / |T_r|} of each label.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return a map from each label to its target overlap
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.targetOverlapByLabel', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>>
		Map<L, Double> targetOverlapByLabel(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).byLabel(Measure.TARGET_OVERLAP);
	}

	/**
	 * Computes the total Jaccard index (union overlap), {@code Σ|S_r ∩ T_r| /
	 * Σ|S_r ∪ T_r|}.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return the total Jaccard index, between 0 and 1
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.jaccard', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>> Double
		jaccard(final ImgLabeling<L, I> source, final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).total(Measure.JACCARD);
	}

	/**
	 * Computes the Jaccard index {@code |S_r ∩ T_r| / |S_r ∪ T_r|} of each
	 * label.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return a map from each label to its Jaccard index
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.jaccardByLabel', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>>
		Map<L, Double> jaccardByLabel(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).byLabel(Measure.JACCARD);
	}

	/**
	 * Computes the total Dice coefficient (mean overlap), {@code 2 Σ|S_r ∩ T_r|
	 * / Σ(|S_r| + |T_r|)}.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return the total Dice coefficient, between 0 and 1
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.dice', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>> Double
		dice(final ImgLabeling<L, I> source, final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).total(Measure.DICE);
	}

	/**
	 * Computes the Dice coefficient {@code 2 |S_r ∩ T_r| / (|S_r| + |T_r|)} of
	 * each label.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return a map from each label to its Dice coefficient
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.diceByLabel', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>>
		Map<L, Double> diceByLabel(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).byLabel(Measure.DICE);
	}

	/**
	 * Computes the total volume similarity, {@code 2 Σ(|S_r| - |T_r|) /
	 * Σ(|S_r| + |T_r|)}.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return the total volume similarity, between -2 and 2 (positive when the
	 *         source labels are larger)
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.volumeSimilarity', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>> Double
		volumeSimilarity(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).total(Measure.VOLUME_SIMILARITY);
	}

	/**
	 * Computes the volume similarity {@code 2 (|S_r| - |T_r|) / (|S_r| +
	 * |T_r|)} of each label.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return a map from each label to its volume similarity
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.volumeSimilarityByLabel', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>>
		Map<L, Double> volumeSimilarityByLabel(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).byLabel(Measure.VOLUME_SIMILARITY);
	}

	/**
	 * Computes the total false negative error, {@code Σ|T_r \ S_r| / Σ|T_r|}.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return the total false negative error, between 0 and 1
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.falseNegativeError', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>> Double
		falseNegativeError(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).total(Measure.FALSE_NEGATIVE);
	}

	/**
	 * Computes the false negative error {@code |T_r \ S_r| / |T_r|} of each
	 * label.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return a map from each label to its false negative error
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.falseNegativeErrorByLabel', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>>
		Map<L, Double> falseNegativeErrorByLabel(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).byLabel(Measure.FALSE_NEGATIVE);
	}

	/**
	 * Computes the total false positive error, {@code Σ|S_r \ T_r| / Σ|S_r|}.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return the total false positive error, between 0 and 1
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.falsePositiveError', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>> Double
		falsePositiveError(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).total(Measure.FALSE_POSITIVE);
	}

	/**
	 * Computes the false positive error {@code |S_r \ T_r| / |S_r|} of each
	 * label.
	 *
	 * @param source the labeling to evaluate
	 * @param target the reference labeling
	 * @return a map from each label to its false positive error
	 * @param <L> label type
	 * @param <I> index type of the source labeling
	 * @param <J> index type of the target labeling
	 * @implNote op names='labeling.overlap.falsePositiveErrorByLabel', type=Function
	 */
	public static <L, I extends IntegerType<I>, J extends IntegerType<J>>
		Map<L, Double> falsePositiveErrorByLabel(final ImgLabeling<L, I> source,
			final ImgLabeling<L, J> target)
	{
		return Counts.of(source, target).byLabel(Measure.FALSE_POSITIVE);
	}

	/** An overlap measure, as a ratio of sums of pixel counts. */
	private enum Measure {
			TARGET_OVERLAP, JACCARD, DICE, VOLUME_SIMILARITY, FALSE_NEGATIVE,
			FALSE_POSITIVE;

		/**
		 * Gets the numerator, given the source size, target size and
		 * intersection size.
		 */
		double numerator(final long s, final long t, final long st) {
			switch (this) {
				case TARGET_OVERLAP:
				case JACCARD:
					return st;
				case DICE:
					return 2.0 * st;
				case VOLUME_SIMILARITY:
					return 2.0 * (s - t);
				case FALSE_NEGATIVE:
					return t - st;
				default:
					return s - st;
			}
		}

		double denominator(final long s, final long t, final long st) {
			switch (this) {
				case TARGET_OVERLAP:
				case FALSE_NEGATIVE:
					return t;
				case JACCARD:
					return s + t - st;
				case DICE:
				case VOLUME_SIMILARITY:
					return s + t;
				default:
					return s;
			}
		}
	}

	/** Pixel counts of each label in the source, target and both. */
	private static final class Counts<L> {

		/** Per label: source count, target count, intersection count. */
		private final Map<L, long[]> counts = new LinkedHashMap<>();

		static <L, I extends IntegerType<I>, J extends IntegerType<J>> Counts<L>
			of(final ImgLabeling<L, I> source, final ImgLabeling<L, J> target)
		{
			if (!Arrays.equals(source.dimensionsAsLongArray(), target
				.dimensionsAsLongArray()))
			{
				throw new IllegalArgumentException(
					"Labeling dimensions differ: " + Arrays.toString(source
						.dimensionsAsLongArray()) + " vs " + Arrays.toString(target
							.dimensionsAsLongArray()));
			}
			// Count pixels per pair of label sets, then expand the sets.
			final var pairs = new LinkedHashMap<Long, long[]>();
			final var s = Views.flatIterable(source.getIndexImg()).cursor();
			final var t = Views.flatIterable(target.getIndexImg()).cursor();
			while (s.hasNext()) {
				final long key = ((long) s.next().getInteger() << 32) | t.next()
					.getInteger();
				pairs.computeIfAbsent(key, k -> new long[1])[0]++;
			}
			final var sm = source.getMapping();
			final var tm = target.getMapping();
			final var result = new Counts<L>();
			for (final var entry : pairs.entrySet()) {
				final var n = entry.getValue()[0];
				final var sLabels = sm.labelsAtIndex((int) (entry.getKey() >>> 32));
				final var tLabels = tm.labelsAtIndex((int) (entry
					.getKey() & 0xffffffffL));
				for (final var label : sLabels) {
					final var c = result.counts(label);
					c[0] += n;
					if (tLabels.contains(label)) c[2] += n;
				}
				for (final var label : tLabels)
					result.counts(label)[1] += n;
			}
			return result;
		}

		private long[] counts(final L label) {
			return counts.computeIfAbsent(label, k -> new long[3]);
		}

		double total(final Measure measure) {
			double num = 0, den = 0;
			for (final var c : counts.values()) {
				num += measure.numerator(c[0], c[1], c[2]);
				den += measure.denominator(c[0], c[1], c[2]);
			}
			return num / den;
		}

		Map<L, Double> byLabel(final Measure measure) {
			final var result = new LinkedHashMap<L, Double>();
			for (final var entry : counts.entrySet()) {
				final var c = entry.getValue();
				result.put(entry.getKey(), measure.numerator(c[0], c[1], c[2]) /
					measure.denominator(c[0], c[1], c[2]));
			}
			return result;
		}
	}
}
