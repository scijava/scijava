/* #%L
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

package org.scijava.ops.image.labeling.utils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import org.scijava.ops.spi.Nullable;

/**
 * Ops analyzing the spatial relations between the labels of an
 * {@link ImgLabeling}: their boundaries and their adjacency.
 * <p>
 * Neighborhoods are given as a {@link StructuringElement}, as for
 * {@code labeling.cca}: {@code FOUR_CONNECTED} means face neighbors only (4
 * in 2D, 6 in 3D) and {@code EIGHT_CONNECTED} means all neighbors sharing a
 * face, edge or corner (8 in 2D, 26 in 3D). Neighbors outside the labeling
 * interval are ignored.
 * </p>
 * <p>
 * These are clean-room implementations of the label boundaries and region
 * adjacency graph of MorphoLibJ (D. Legland, I. Arganda-Carreras and P.
 * Andrey, "MorphoLibJ: integrated library and plugins for mathematical
 * morphology with ImageJ", <i>Bioinformatics</i> 32(22): 3532&ndash;3534,
 * 2016), generalized to n-dimensional labelings with arbitrary, possibly
 * overlapping labels.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class LabelTopology {

	private LabelTopology() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the boundaries of the labeled regions as a binary image. A pixel
	 * is a boundary pixel if it carries at least one label and has a neighbor
	 * with a different set of labels (another label, no label, or a different
	 * overlap). Between two touching regions, the boundary pixels of both
	 * regions are marked.
	 *
	 * @param labeling the labeled objects
	 * @param connectivity the neighborhood (default: {@code FOUR_CONNECTED})
	 * @return a binary image of the boundary pixels, with the interval of the
	 *         labeling
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.boundaries', type=Function
	 */
	public static <L, I extends IntegerType<I>> RandomAccessibleInterval<BitType>
		boundaries(final ImgLabeling<L, I> labeling,
			@Nullable StructuringElement connectivity)
	{
		final var se = connectivity == null ? StructuringElement.FOUR_CONNECTED
			: connectivity;
		final var indexImg = labeling.getIndexImg();
		final var mapping = labeling.getMapping();
		final var n = labeling.numDimensions();
		final var empty = new boolean[mapping.numSets()];
		for (var i = 0; i < empty.length; i++)
			empty[i] = mapping.labelsAtIndex(i).isEmpty();

		final RandomAccessibleInterval<BitType> output = Views.translate(ArrayImgs
			.bits(indexImg.dimensionsAsLongArray()), indexImg.minAsLongArray());
		final var out = output.randomAccess();
		final var ra = indexImg.randomAccess();
		final var offsets = Labelings.offsets(n, se, false);
		final var position = new long[n];
		final var neighbor = new long[n];
		final var cursor = Views.flatIterable(indexImg).localizingCursor();
		while (cursor.hasNext()) {
			final var index = cursor.next().getInteger();
			if (empty[index]) continue;
			cursor.localize(position);
			for (final var offset : offsets) {
				if (!Labelings.shift(position, offset, indexImg, neighbor)) continue;
				if (ra.setPositionAndGet(neighbor).getInteger() != index) {
					out.setPositionAndGet(position).set(true);
					break;
				}
			}
		}
		return output;
	}

	/**
	 * Computes the region adjacency graph of the labeling. Two labels are
	 * adjacent if a pixel of one is a neighbor of a pixel of the other, or if
	 * they overlap (share a pixel).
	 *
	 * @param labeling the labeled objects
	 * @param connectivity the neighborhood (default: {@code FOUR_CONNECTED})
	 * @return a map from each label present in the labeling to the set of its
	 *         adjacent labels (empty if it has none); the relation is
	 *         symmetric and never contains a label itself
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.adjacency', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Set<L>> adjacency(
		final ImgLabeling<L, I> labeling,
		@Nullable StructuringElement connectivity)
	{
		final var se = connectivity == null ? StructuringElement.FOUR_CONNECTED
			: connectivity;
		final var indexImg = labeling.getIndexImg();
		final var mapping = labeling.getMapping();
		final var n = labeling.numDimensions();
		final long numSets = mapping.numSets();

		final var used = new boolean[(int) numSets];
		final var pairs = new HashSet<Long>();
		final var ra = indexImg.randomAccess();
		final var offsets = Labelings.offsets(n, se, true);
		final var position = new long[n];
		final var neighbor = new long[n];
		final var cursor = Views.flatIterable(indexImg).localizingCursor();
		while (cursor.hasNext()) {
			final var index = cursor.next().getInteger();
			used[index] = true;
			cursor.localize(position);
			for (final var offset : offsets) {
				if (!Labelings.shift(position, offset, indexImg, neighbor)) continue;
				final var other = ra.setPositionAndGet(neighbor).getInteger();
				if (other == index) continue;
				final long lo = Math.min(index, other);
				final long hi = Math.max(index, other);
				pairs.add(lo * numSets + hi);
			}
		}

		final var graph = new LinkedHashMap<L, Set<L>>();
		for (var i = 0; i < used.length; i++) {
			if (!used[i]) continue;
			final var labels = mapping.labelsAtIndex(i);
			for (final var label : labels)
				graph.computeIfAbsent(label, k -> new LinkedHashSet<>());
			connect(graph, labels, labels);
		}
		for (final long pair : pairs) {
			connect(graph, mapping.labelsAtIndex((int) (pair / numSets)), mapping
				.labelsAtIndex((int) (pair % numSets)));
		}
		return graph;
	}

	private static <L> void connect(final Map<L, Set<L>> graph,
		final Set<L> labels1, final Set<L> labels2)
	{
		for (final var a : labels1) {
			for (final var b : labels2) {
				if (a.equals(b)) continue;
				graph.get(a).add(b);
				graph.get(b).add(a);
			}
		}
	}
}
