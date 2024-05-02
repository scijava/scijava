/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.BoundingBox;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * An N-dimensional box counting that can be used to estimate the fractal
 * dimension of an interval
 * <p>
 * The algorithm repeatedly lays a fixed grid on the interval, and counts the
 * number of sections that contain foreground. After each step the grid is made
 * finer by a factor of {@link #scaling}. If the objects in the interval are
 * fractal, the proportion of foreground sections should increase as the grid
 * gets finer.
 * </p>
 * <p>
 * Produces a set of points (log(foreground count), -log(section size)) for
 * curve fitting. The slope of the function gives the fractal dimension of the
 * interval.
 * </p>
 *
 * @author Richard Domander (Royal Veterinary College, London)
 * @author Per Christian Henden
 * @author Jens Bache-Wiig
 */
public final class BoxCount {

	private BoxCount() {
		// Prevent instantiation of static utility class
	}

	// /** Starting size of the grid sections in pixels */
	// private Long maxSize = 48L;
	//
	// /** Minimum size of the grid sections in pixels */
	// private Long minSize = 6L;
	//
	// /** Grid downscaling factor */
	// private Double scaling = 1.2;
	//
	// /**
	// * Number of times the grid is moved in each dimension to find the best fit
	// * <p>
	// * The best fitting grid covers the objects in the interval with the least
	// * amount of sections.
	// * </p>
	// * <p>
	// * NB Additional moves multiply algorithm's time complexity by n^d!
	// * </p>
	// */
	// private Long gridMoves = 0L;

	/**
	 * Counts the number of foreground sections in the interval repeatedly with
	 * different size sections
	 *
	 * @param input an n-dimensional binary interval
	 * @return A list of (log(foreground count), -log(section size))
	 *         {@link ValuePair} objects for curve fitting
	 */
	public static <B extends BooleanType<B>> //
	List<ValuePair<DoubleType, DoubleType>> apply( //
		final RandomAccessibleInterval<B> input, //
		final Long maxSize, //
		final Long minSize, //
		final Double scaling, //
		final Long gridMoves //
	) {

		if (scaling <= 1.0) {
			throw new IllegalArgumentException(
				"Scaling must be > 1.0 or algorithm won't stop.");
		}
		final List<ValuePair<DoubleType, DoubleType>> points = new ArrayList<>();
		final int dimensions = input.numDimensions();
		final long[] sizes = new long[dimensions];
		input.dimensions(sizes);
		for (long sectionSize = maxSize; sectionSize >= minSize; sectionSize /=
			scaling)
		{
			final long numTranslations = limitTranslations(sectionSize, 1 +
				gridMoves);
			final long translationAmount = sectionSize / numTranslations;
			final Stream<long[]> translations = translationStream(numTranslations,
				translationAmount, dimensions - 1, new long[dimensions]);
			final LongStream foregroundCounts = countTranslatedGrids(input,
				translations, sizes, sectionSize);
			final long foreground = foregroundCounts.min().orElse(0);
			final double logSize = -Math.log(sectionSize);
			final double logCount = Math.log(foreground);
			final ValuePair<DoubleType, DoubleType> point = new ValuePair<>(
				new DoubleType(logSize), new DoubleType(logCount));
			points.add(point);
		}
		return points;
	}

	/**
	 * A helper method that culls unnecessary translations for finding the best
	 * fit.
	 * <p>
	 * For example, if size = 2 and there's a 2 x 2 x 2 object in a 3D image, then
	 * at worst it's in 8 different boxes. The best fit in this case is one box.
	 * However the boxes can only be adjusted by 1 pixel in each direction, so
	 * more than 2 translations is unnecessary.
	 * </p>
	 * <p>
	 * NB the minimum number of translations is 1, which means the boxes are
	 * counted once, and there are no attempts at adjusting their coordinates for
	 * a better fit.
	 * </p>
	 *
	 * @param size Size n of a box in the algorithm. E.g. in the 3D case it's n *
	 *          n * n.
	 * @param translations Number of times the box counting grid is moved in each
	 *          dimension to find the best fit.
	 * @return The original or maximum number of meaningful translations if the
	 *         original was too many.
	 * @throws IllegalArgumentException if size is non-positive.
	 */
	static long limitTranslations(final long size, final long translations)
		throws IllegalArgumentException
	{
		if (size < 1L) {
			throw new IllegalArgumentException("Size must be positive");
		}

		if (translations < 1L) {
			return 1L;
		}

		return Math.min(size, translations);
	}

	/**
	 * Count foreground sections in all grids created from the translations
	 *
	 * @param input N-dimensional binary interval
	 * @param translations Stream of translation coordinates in n-dimensions
	 * @param sizes Sizes of the interval's dimensions in pixels
	 * @param sectionSize Size of a section in the grids
	 * @return Foreground sections counted in each grid
	 */
	private static <B extends BooleanType<B>> LongStream countTranslatedGrids(
		final RandomAccessibleInterval<B> input, final Stream<long[]> translations,
		final long[] sizes, final long sectionSize)
	{
		final int lastDimension = sizes.length - 1;
		return translations.parallel().mapToLong(gridOffset -> {
			final LongType foreground = new LongType();
			final long[] sectionPosition = new long[sizes.length];
			countGrid(input, lastDimension, sizes, gridOffset, sectionPosition,
				sectionSize, foreground);
			return foreground.get();
		});
	}

	/**
	 * Creates a {@link net.imglib2.View} of the given grid section in the
	 * interval
	 * <p>
	 * Fits the view inside the bounds of the interval.
	 * </p>
	 *
	 * @param interval An n-dimensional interval with binary elements
	 * @param sizes Sizes of the interval's dimensions
	 * @param coordinates Starting coordinates of the section
	 * @param sectionSize Size of the section (n * n * ... n)
	 * @return A view of the interval spanning n pixels in each dimension from the
	 *         coordinates. Null if view couldn't be set inside the interval
	 */
	private static <B extends BooleanType<B>> IntervalView<B> sectionView(
		final RandomAccessibleInterval<B> interval, final long[] sizes,
		final long[] coordinates, final long sectionSize)
	{
		final int n = sizes.length;
		final long[] startPosition = IntStream.range(0, n).mapToLong(i -> Math.max(
			0, coordinates[i])).toArray();
		final long[] endPosition = IntStream.range(0, n).mapToLong(i -> Math.min(
			(sizes[i] - 1), (coordinates[i] + sectionSize - 1))).toArray();
		final boolean badBox = IntStream.range(0, n).anyMatch(
			d -> (startPosition[d] >= sizes[d]) || (endPosition[d] < 0) ||
				(endPosition[d] < startPosition[d]));
		if (badBox) {
			return null;
		}
		final BoundingBox box = new BoundingBox(n);
		box.update(startPosition);
		box.update(endPosition);
		return Views.offsetInterval(interval, box);
	}

	/** Checks if the view has any foreground elements */
	private static <B extends BooleanType<B>> boolean hasForeground(
		IntervalView<B> view)
	{
		final Spliterator<B> spliterator = view.spliterator();
		return StreamSupport.stream(spliterator, false).anyMatch(BooleanType::get);
	}

	/**
	 * Recursively counts the number of foreground sections in the grid over the
	 * given interval
	 *
	 * @param interval An n-dimensional interval with binary elements
	 * @param dimension Current dimension processed, start from the last
	 * @param sizes Sizes of the interval's dimensions in pixels
	 * @param translation Translation of grid start in each dimension
	 * @param sectionPosition The accumulated position of the current grid section
	 *          (start from [0, 0, ... 0])
	 * @param sectionSize Size of a grid section (n * n * ... n)
	 * @param foreground Number of foreground sections found so far (start from 0)
	 */
	private static <B extends BooleanType<B>> void countGrid(
		final RandomAccessibleInterval<B> interval, final int dimension,
		final long[] sizes, final long[] translation, final long[] sectionPosition,
		final long sectionSize, final LongType foreground)
	{
		for (int p = 0; p < sizes[dimension]; p += sectionSize) {
			sectionPosition[dimension] = translation[dimension] + p;
			if (dimension == 0) {
				final IntervalView<B> box = sectionView(interval, sizes,
					sectionPosition, sectionSize);
				if (box != null && hasForeground(box)) {
					foreground.inc();
				}
			}
			else {
				countGrid(interval, dimension - 1, sizes, translation, sectionPosition,
					sectionSize, foreground);
			}
		}
	}

	/**
	 * Creates a {@link Stream} of t * 2^n translations in n-dimensions
	 * <p>
	 * The elements in the stream are arrays of coordinates [0, 0, .. 0], [-i, 0,
	 * .. 0], [0, -i, 0, .. 0], .. [-i, -i, .. -i], [-2i, 0, .. 0] .. [-ti, -ti,
	 * .. -ti], where each array has n elements, i = number of pixels translated,
	 * and t = number of translations. If the translations were positive, a part
	 * of the interval would not get inspected, because it always starts from [0,
	 * 0, ... 0].
	 * </p>
	 * <p>
	 * The order of arrays in the stream is not guaranteed.
	 * </p>
	 *
	 * @param numTranslations Number of translations (1 produces
	 *          Stream.of(long[]{0, 0, .. 0}))
	 * @param amount Number of pixels shifted in translations
	 * @param dimension Current translation dimension (start from last)
	 * @param translation The accumulated position of the current translation
	 *          (start from {0, 0, .. 0})
	 * @return A stream of coordinates of the translations
	 */
	private static Stream<long[]> translationStream(final long numTranslations,
		final long amount, final int dimension, final long[] translation)
	{
		final Stream.Builder<long[]> builder = Stream.builder();
		generateTranslations(numTranslations, amount, dimension, translation,
			builder);
		return builder.build();
	}

	/**
	 * Adds translations to the given {@link Stream.Builder}
	 *
	 * @see #translationStream(long, long, int, long[])
	 */
	private static void generateTranslations(final long numTranslations,
		final long amount, final int dimension, final long[] translation,
		final Stream.Builder<long[]> builder)
	{
		for (int t = 0; t < numTranslations; t++) {
			translation[dimension] = -t * amount;
			if (dimension == 0) {
				builder.add(translation.clone());
			}
			else {
				generateTranslations(numTranslations, amount, dimension - 1,
					translation, builder);
			}
		}
	}
}

/**
 * @implNote op names='topology.boxCount'
 */
class DefaultBoxCount<B extends BooleanType<B>> implements
	Functions.Arity5<RandomAccessibleInterval<B>, Long, Long, Double, Long, List<ValuePair<DoubleType, DoubleType>>>
{

	/**
	 * TODO
	 *
	 * @param input
	 * @param maxSize
	 * @param minSize
	 * @param scaling
	 * @param gridMoves
	 * @return the output
	 */
	@Override
	public List<ValuePair<DoubleType, DoubleType>> apply( //
		final RandomAccessibleInterval<B> input, //
		@Nullable Long maxSize, //
		@Nullable Long minSize, //
		@Nullable Double scaling, //
		@Nullable Long gridMoves //
	) {
		if (maxSize == null) {
			maxSize = 48L;
		}
		if (minSize == null) {
			minSize = 5L;
		}
		if (scaling == null) {
			scaling = 1.2;
		}
		if (gridMoves == null) {
			gridMoves = 0L;
		}
		return BoxCount.apply(input, maxSize, minSize, scaling, gridMoves);
	}

}
