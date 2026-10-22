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

package org.scijava.ops.image.image.kymograph;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.converter.Converters;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.roi.geom.real.Polyline;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Builds a kymograph: samples a time series along a (poly)line at every time
 * point, yielding a space-time image.
 * <p>
 * The polyline lives in the plane of dimensions 0 and 1 (x and y) of the
 * input. It is sampled at unit arc-length steps from its first vertex, giving
 * {@code floor(length) + 1} samples. With a line width {@code w > 1},
 * {@code w} parallel lines are sampled, offset along the normal of each
 * segment by {@code i - (w - 1) / 2} pixels for {@code i = 0..w-1}, and
 * combined with the chosen reducer: {@code "max"} (default, as in
 * KymographBuilder), {@code "mean"}, or {@code "none"} to keep each parallel
 * line separately.
 * </p>
 * <p>
 * The output has dimensions {@code [position, time, (line if reduce is
 * "none"), other...]}, where {@code other} are the remaining input dimensions
 * (e.g. channel or z) in their original order. Samples outside the image are
 * zero.
 * </p>
 * <p>
 * Adapted from the
 * <a href="https://github.com/fiji/KymographBuilder">KymographBuilder</a> Fiji
 * plugin by Hadrien Mary, MIT License, Copyright (C) 2016 - 2026 Fiji
 * developers. Differences from the original: samples are taken at uniform
 * arc-length steps along the whole polyline in real coordinates (rather than
 * from integer-rounded segment endpoints via {@code ij.gui.Line}); pixels in
 * row 0 and column 0 are sampled (the original skipped them due to an
 * off-by-one bounds check); interpolation defaults to n-linear (use
 * {@code "nearest"} for the original's rounding); and all non-spatial,
 * non-time dimensions are kept rather than a single z plane.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Hadrien Mary
 * @param <T> image type
 * @implNote op names='image.kymograph'
 */
public class DefaultKymograph<T extends RealType<T> & NativeType<T>>
	implements
	Functions.Arity6<RandomAccessibleInterval<T>, Polyline, Integer, Integer, String, String, RandomAccessibleInterval<T>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, T, RandomAccessibleInterval<T>> createOp;

	/**
	 * Builds the kymograph.
	 *
	 * @param input the time series, whose dimensions 0 and 1 are the spatial
	 *          plane of the polyline
	 * @param polyline the path along which to sample, in the coordinates of
	 *          input dimensions 0 and 1; it must have at least two vertices
	 * @param timeDim the index of the time dimension; defaults to the last
	 *          dimension. It must be at least 2.
	 * @param lineWidth the number of parallel lines to sample; defaults to 1
	 * @param reduce how to combine the parallel lines: {@code "max"}
	 *          (default), {@code "mean"} or {@code "none"}
	 * @param interpolation how to sample between pixels: {@code "linear"}
	 *          (default) or {@code "nearest"}
	 * @return the kymograph, with dimensions {@code [position, time, (line),
	 *         other...]}
	 */
	@Override
	public RandomAccessibleInterval<T> apply(
		final RandomAccessibleInterval<T> input, final Polyline polyline,
		@Nullable Integer timeDim, @Nullable Integer lineWidth,
		@Nullable String reduce, @Nullable String interpolation)
	{
		final var n = input.numDimensions();
		if (n < 3) throw new IllegalArgumentException(
			"Input must have at least 3 dimensions (x, y, time)");
		if (timeDim == null) timeDim = n - 1;
		if (timeDim < 2 || timeDim >= n) throw new IllegalArgumentException(
			"Invalid time dimension: " + timeDim);
		if (lineWidth == null) lineWidth = 1;
		if (lineWidth < 1) throw new IllegalArgumentException(
			"Line width must be positive");
		if (reduce == null) reduce = "max";
		reduce = reduce.trim().toLowerCase(Locale.ROOT);
		if (!reduce.equals("max") && !reduce.equals("mean") && !reduce.equals(
			"none"))
		{
			throw new IllegalArgumentException("Unknown reduce method: '" + reduce +
				"'. Expected one of: max, mean, none.");
		}
		if (interpolation == null) interpolation = "linear";
		final InterpolatorFactory<DoubleType, RandomAccessible<DoubleType>> interp;
		switch (interpolation.trim().toLowerCase(Locale.ROOT)) {
			case "linear":
				interp = new NLinearInterpolatorFactory<>();
				break;
			case "nearest":
				interp = new NearestNeighborInterpolatorFactory<>();
				break;
			default:
				throw new IllegalArgumentException("Unknown interpolation: '" +
					interpolation + "'. Expected one of: linear, nearest.");
		}

		// Compute the sample coordinates of each parallel line.
		final var samples = samplePolyline(polyline, lineWidth);
		final var xs = samples[0];
		final var ys = samples[1];
		final var length = xs.length;

		// Determine the output dimensions.
		final List<Integer> otherDims = new ArrayList<>();
		for (var d = 2; d < n; d++)
			if (d != timeDim) otherDims.add(d);
		final var keepLines = reduce.equals("none");
		final var outN = 2 + (keepLines ? 1 : 0) + otherDims.size();
		final var outDims = new long[outN];
		outDims[0] = length;
		outDims[1] = input.dimension(timeDim);
		if (keepLines) outDims[2] = lineWidth;
		final var otherOffset = keepLines ? 3 : 2;
		for (var k = 0; k < otherDims.size(); k++)
			outDims[otherOffset + k] = input.dimension(otherDims.get(k));

		final var output = createOp.apply(new FinalDimensions(outDims), input
			.getType().createVariable());
		final var outType = output.getType();
		final var round = outType instanceof IntegerType;
		final var outMin = outType.getMinValue();
		final var outMax = outType.getMaxValue();

		// Note: Sampling happens in double precision; outside samples are zero.
		final RandomAccessible<DoubleType> asDouble = Converters.convert(
			(RandomAccessible<T>) Views.extendZero(input), (a, b) -> b.set(a
				.getRealDouble()), new DoubleType());
		final var rra = Views.interpolate(asDouble, interp).realRandomAccess();

		final var useMean = reduce.equals("mean");
		final var cursor = Views.flatIterable(output).localizingCursor();
		final var pos = new long[outN];
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.localize(pos);
			final var j = (int) pos[0];
			rra.setPosition(input.min(timeDim) + pos[1], timeDim);
			for (var k = 0; k < otherDims.size(); k++) {
				final int d = otherDims.get(k);
				rra.setPosition(input.min(d) + pos[otherOffset + k], d);
			}
			final double value;
			if (keepLines) {
				final var i = (int) pos[2];
				value = sample(rra, xs[j][i], ys[j][i]);
			}
			else {
				var acc = useMean ? 0 : Double.NEGATIVE_INFINITY;
				for (var i = 0; i < lineWidth; i++) {
					final var v = sample(rra, xs[j][i], ys[j][i]);
					acc = useMean ? acc + v : Math.max(acc, v);
				}
				value = useMean ? acc / lineWidth : acc;
			}
			final var v = round ? Math.floor(value + 0.5) : value;
			cursor.get().setReal(Math.max(outMin, Math.min(outMax, v)));
		}
		return output;
	}

	private static double sample(
		final RealRandomAccess<DoubleType> rra, final double x,
		final double y)
	{
		rra.setPosition(x, 0);
		rra.setPosition(y, 1);
		return rra.get().get();
	}

	/**
	 * Computes sample coordinates along the polyline at unit arc-length steps,
	 * for each of the {@code lineWidth} parallel lines.
	 *
	 * @return {@code {xs, ys}}, each indexed as {@code [sample][line]}
	 */
	static double[][][] samplePolyline(final Polyline polyline,
		final int lineWidth)
	{
		if (polyline == null) throw new NullPointerException(
			"Polyline must be given");
		if (polyline.numDimensions() < 2) throw new IllegalArgumentException(
			"Polyline must be at least 2-dimensional");

		// Collect the vertices, skipping repeated points.
		final List<double[]> vertices = new ArrayList<>();
		for (var v = 0; v < polyline.numVertices(); v++) {
			final var p = polyline.vertex(v);
			final double[] xy = { p.getDoublePosition(0), p.getDoublePosition(1) };
			if (!vertices.isEmpty()) {
				final var last = vertices.get(vertices.size() - 1);
				if (last[0] == xy[0] && last[1] == xy[1]) continue;
			}
			vertices.add(xy);
		}
		if (vertices.size() < 2) throw new IllegalArgumentException(
			"Polyline must have at least two distinct vertices");

		// Compute the cumulative arc length at each vertex.
		final var segments = vertices.size() - 1;
		final var cumulative = new double[segments + 1];
		for (var s = 0; s < segments; s++) {
			final var a = vertices.get(s);
			final var b = vertices.get(s + 1);
			cumulative[s + 1] = cumulative[s] + Math.hypot(b[0] - a[0], b[1] -
				a[1]);
		}
		final var total = cumulative[segments];
		// Note: The epsilon keeps a length of exactly k from losing a sample.
		final var count = (int) Math.floor(total + 1e-9) + 1;

		final var xs = new double[count][lineWidth];
		final var ys = new double[count][lineWidth];
		var s = 0;
		for (var j = 0; j < count; j++) {
			final double arc = j;
			while (s < segments - 1 && arc >= cumulative[s + 1])
				s++;
			final var a = vertices.get(s);
			final var b = vertices.get(s + 1);
			final var segLength = cumulative[s + 1] - cumulative[s];
			final var ux = (b[0] - a[0]) / segLength;
			final var uy = (b[1] - a[1]) / segLength;
			final var t = arc - cumulative[s];
			final var x = a[0] + t * ux;
			final var y = a[1] + t * uy;
			for (var i = 0; i < lineWidth; i++) {
				// Note: The normal (-uy, ux) matches KymographBuilder's offsets.
				final var offset = i - (lineWidth - 1) / 2.0;
				xs[j][i] = x - offset * uy;
				ys[j][i] = y + offset * ux;
			}
		}
		return new double[][][] { xs, ys };
	}
}
