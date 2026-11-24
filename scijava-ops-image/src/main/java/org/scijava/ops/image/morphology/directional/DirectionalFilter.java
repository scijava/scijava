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

package org.scijava.ops.image.morphology.directional;

import java.util.Locale;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Directional filtering with oriented line segments (Soille &amp; Talbot
 * 2001).
 * <p>
 * The image is filtered with a line-segment structuring element (see
 * {@link OrientedLineShape}) at {@code n} orientations {@code θ_i = i·π/n},
 * {@code i = 0, ..., n-1}, and the {@code n} results are combined pixel-wise
 * by their maximum or minimum. The filter applied per orientation is one of:
 * </p>
 * <ul>
 * <li>{@code "erosion"} (minimum along the line), {@code "dilation"}
 * (maximum), {@code "opening"} (erosion followed by dilation) or
 * {@code "closing"} (dilation followed by erosion);</li>
 * <li>{@code "mean"} or {@code "median"} along the line.</li>
 * </ul>
 * <p>
 * The maximum of directional openings (the default) keeps bright thin
 * structures, such as fibers or vessels, that contain a line segment of the
 * given length in some orientation, while removing bright noise and blobs
 * smaller than the segment; dually, the minimum of directional closings keeps
 * dark curvilinear structures. The maximum of directional means or medians
 * smooths along the local structure orientation, which enhances thin bright
 * lines without blurring them across.
 * </p>
 * <p>
 * The lines lie in the plane of the first two dimensions, so images with more
 * dimensions are filtered plane by plane. Out-of-bounds values are taken from
 * the nearest border pixel.
 * </p>
 * <p>
 * Implemented from the papers. This is the method of the "Directional
 * Filtering" plugin of MorphoLibJ, but the line segments are discretized
 * differently (see {@link OrientedLineShape}), so results are not identical
 * to those of the plugin.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @see <a href="https://doi.org/10.1109/34.969115">P. Soille and H. Talbot,
 *      "Directional morphological filtering", IEEE Trans. Pattern Anal. Mach.
 *      Intell. 23(11):1313-1329, 2001</a>
 * @implNote op names='morphology.directionalFilter'
 */
public class DirectionalFilter<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity5<RandomAccessibleInterval<T>, String, Double, Integer, String, RandomAccessibleInterval<V>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	@OpDependency(name = "filter.min")
	private Computers.Arity3<RandomAccessibleInterval<DoubleType>, Shape, OutOfBoundsFactory<DoubleType, RandomAccessibleInterval<DoubleType>>, RandomAccessibleInterval<DoubleType>> minOp;

	@OpDependency(name = "filter.max")
	private Computers.Arity3<RandomAccessibleInterval<DoubleType>, Shape, OutOfBoundsFactory<DoubleType, RandomAccessibleInterval<DoubleType>>, RandomAccessibleInterval<DoubleType>> maxOp;

	@OpDependency(name = "filter.mean")
	private Computers.Arity3<RandomAccessibleInterval<DoubleType>, Shape, OutOfBoundsFactory<DoubleType, RandomAccessibleInterval<DoubleType>>, RandomAccessibleInterval<DoubleType>> meanOp;

	@OpDependency(name = "filter.median")
	private Computers.Arity3<RandomAccessibleInterval<DoubleType>, Shape, OutOfBoundsFactory<DoubleType, RandomAccessibleInterval<DoubleType>>, RandomAccessibleInterval<DoubleType>> medianOp;

	/**
	 * Applies the directional filter.
	 *
	 * @param input the image to filter, with at least 2 dimensions
	 * @param operation the filter applied along each line: one of "opening",
	 *          "closing", "erosion", "dilation", "mean" or "median" (default:
	 *          "opening")
	 * @param lineLength the approximate Euclidean length of the line segments,
	 *          in pixels (default: 20)
	 * @param directions the number of orientations, evenly spaced over 180
	 *          degrees (default: 32)
	 * @param combination how the results of the orientations are combined:
	 *          "max" or "min" (default: "max")
	 * @param output the filtered image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		@Nullable final String operation, @Nullable final Double lineLength,
		@Nullable final Integer directions, @Nullable final String combination,
		final RandomAccessibleInterval<V> output)
	{
		if (input.numDimensions() < 2) {
			throw new IllegalArgumentException(
				"Directional filtering needs at least 2 dimensions");
		}
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		final var op = operation == null ? "opening" : operation.toLowerCase(
			Locale.ROOT);
		if (!op.matches("opening|closing|erosion|dilation|mean|median")) {
			throw new IllegalArgumentException("Unknown operation: " + operation +
				" (expected opening, closing, erosion, dilation, mean or median)");
		}
		final double length = lineLength == null ? 20 : lineLength;
		final int n = directions == null ? 32 : directions;
		if (n < 1) {
			throw new IllegalArgumentException(
				"Number of directions must be positive: " + n);
		}
		final var comb = combination == null ? "max" : combination.toLowerCase(
			Locale.ROOT);
		final boolean useMax;
		if (comb.equals("max")) useMax = true;
		else if (comb.equals("min")) useMax = false;
		else throw new IllegalArgumentException("Combination must be max or min: " +
			combination);

		final var source = createOp.apply(input, new DoubleType());
		LoopBuilder.setImages(input, source).forEachPixel((i, s) -> s.set(i
			.getRealDouble()));
		final var result = createOp.apply(input, new DoubleType());
		final var tmp = createOp.apply(input, new DoubleType());
		final var filtered = createOp.apply(input, new DoubleType());
		final OutOfBoundsFactory<DoubleType, RandomAccessibleInterval<DoubleType>> oob =
			new OutOfBoundsBorderFactory<>();
		for (var i = 0; i < n; i++) {
			final var shape = new OrientedLineShape(length, i * Math.PI / n);
			switch (op) {
				case "erosion":
					minOp.compute(source, shape, oob, filtered);
					break;
				case "dilation":
					maxOp.compute(source, shape, oob, filtered);
					break;
				case "opening":
					minOp.compute(source, shape, oob, tmp);
					maxOp.compute(tmp, shape, oob, filtered);
					break;
				case "closing":
					maxOp.compute(source, shape, oob, tmp);
					minOp.compute(tmp, shape, oob, filtered);
					break;
				case "mean":
					meanOp.compute(source, shape, oob, filtered);
					break;
				default:
					medianOp.compute(source, shape, oob, filtered);
			}
			if (i == 0) {
				LoopBuilder.setImages(filtered, result).forEachPixel((f, r) -> r.set(
					f));
			}
			else if (useMax) {
				LoopBuilder.setImages(filtered, result).forEachPixel((f, r) -> {
					if (f.get() > r.get()) r.set(f);
				});
			}
			else {
				LoopBuilder.setImages(filtered, result).forEachPixel((f, r) -> {
					if (f.get() < r.get()) r.set(f);
				});
			}
		}
		LoopBuilder.setImages(result, output).forEachPixel((r, o) -> o.setReal(r
			.get()));
	}
}
