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

package org.scijava.ops.image.labeling.measure;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.geom.real.ClosedWritableSphere;
import net.imglib2.roi.geom.real.Sphere;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.ops.image.util.Calibrations;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * The largest inscribed ball of each labeled region: a ball centered at the
 * pixel farthest from the region boundary, whose radius is that distance.
 * <p>
 * Distances are those of {@code labeling.distanceMap}: exact Euclidean
 * distances from each pixel center to the nearest pixel center outside its
 * region, in the units of the pixel spacing. As there, the edge of the
 * labeling interval does not bound the regions. This is the classical
 * maximal-ball characterization of the medial axis (H. Blum, "A
 * transformation for extracting new descriptors of shape", in <i>Models for
 * the Perception of Speech and Visual Form</i>, MIT Press, 1967), restricted
 * to the largest ball.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class InscribedBalls {

	private InscribedBalls() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Finds the largest inscribed ball of each label. Its center is the center
	 * of the pixel with the largest distance to the region boundary (the first
	 * such pixel in flat iteration order), in calibrated coordinates (the
	 * pixel position times the spacing), and its radius is that distance. A
	 * single pixel has the smallest pixel spacing as radius. With overlapping
	 * labels, the distance map is that of the label sets, so the regions of
	 * overlap count as separate regions.
	 *
	 * @param distanceMap the op computing the label distance map
	 * @param labeling the labeled objects
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @return a map from each label to its largest inscribed ball
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.inscribedBall', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Sphere> inscribedBalls(
		@OpDependency(
			name = "labeling.distanceMap") BiFunction<ImgLabeling<L, I>, double[], RandomAccessibleInterval<DoubleType>> distanceMap,
		final ImgLabeling<L, I> labeling, @Nullable double[] spacing)
	{
		final var n = labeling.numDimensions();
		final var s = Calibrations.spacing(spacing, n, "spacing");
		final var map = distanceMap.apply(labeling, s);
		final var mapping = labeling.getMapping();
		final var best = new double[mapping.numSets()];
		final var where = new long[mapping.numSets()][];
		Arrays.fill(best, -1);
		final var labels = Views.flatIterable(labeling.getIndexImg())
			.localizingCursor();
		final var distances = Views.flatIterable(Views.interval(map, labeling))
			.cursor();
		while (labels.hasNext()) {
			final var index = labels.next().getInteger();
			final var d = distances.next().get();
			if (d > best[index]) {
				best[index] = d;
				where[index] = labels.positionAsLongArray();
			}
		}
		// Combine the label sets of each label.
		final var radii = new LinkedHashMap<L, Double>();
		final var centers = new LinkedHashMap<L, long[]>();
		for (var i = 0; i < best.length; i++) {
			if (where[i] == null) continue;
			for (final var label : mapping.labelsAtIndex(i)) {
				final var r = radii.get(label);
				if (r == null || best[i] > r || best[i] == r && before(where[i],
					centers.get(label)))
				{
					radii.put(label, best[i]);
					centers.put(label, where[i]);
				}
			}
		}
		final var result = new LinkedHashMap<L, Sphere>();
		for (final var entry : radii.entrySet()) {
			final var pos = centers.get(entry.getKey());
			final var center = new double[n];
			for (var d = 0; d < n; d++)
				center[d] = pos[d] * s[d];
			result.put(entry.getKey(), new ClosedWritableSphere(center, entry
				.getValue()));
		}
		return result;
	}

	/** Checks whether position a comes before b in flat iteration order. */
	private static boolean before(final long[] a, final long[] b) {
		for (var d = a.length - 1; d >= 0; d--)
			if (a[d] != b[d]) return a[d] < b[d];
		return false;
	}

}
