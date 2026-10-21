/*-
 * #%L
 * LGPL-licensed image processing operations for SciJava Ops, translated from LGPL ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.lgpl.morphology.attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * The max-tree (component tree of the upper level sets) of an n-dimensional
 * image, built with the union-find algorithm of Berger et al.
 * <p>
 * Each node of the tree is a connected component of an upper level set
 * {@code {f >= t}} at a level {@code t} taken by the image, and its parent is
 * the smallest strictly larger such component. The tree is stored in a single
 * {@code parent} array over the pixels (flattened, first dimension varying
 * fastest): each node is represented by one canonical pixel at its level, the
 * other pixels of the node at that level point to it, and a canonical pixel
 * points to the canonical pixel of the parent node. The root (the whole image,
 * at the minimum value) points to itself; a connectivity that does not link
 * all pixels yields several roots, one per connected component of the image.
 * A min-tree is the max-tree of the
 * negated image.
 * </p>
 * <p>
 * References:
 * </p>
 * <ul>
 * <li>P. Salembier, A. Oliveras and L. Garrido, "Antiextensive connected
 * operators for image and sequence processing", <em>IEEE Transactions on Image
 * Processing</em> 7(4):555-570, 1998.</li>
 * <li>L. Najman and M. Couprie, "Building the component tree in quasi-linear
 * time", <em>IEEE Transactions on Image Processing</em> 15(11):3531-3539,
 * 2006.</li>
 * <li>C. Berger, T. Geraud, R. Levillain, N. Widynski, A. Baillard and E.
 * Bertin, "Effective component tree computation with application to pattern
 * recognition in astronomical imaging", <em>IEEE International Conference on
 * Image Processing</em>, 2007.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class ComponentTree {

	/** Number of dimensions. */
	final int n;

	/** Size of each dimension. */
	final int[] dims;

	/** Total number of pixels. */
	final int size;

	/** Whether this is a min-tree rather than a max-tree. */
	final boolean minTree;

	/** Pixel values (negated for a min-tree). */
	final double[] values;

	/** Rank of each pixel value among the distinct values. */
	final int[] ranks;

	/** Pixels sorted by decreasing value (leaves first, root last). */
	final int[] order;

	/** Parent of each pixel, as described in the class documentation. */
	final int[] parent;

	/**
	 * Builds the max-tree, or the min-tree, of an image.
	 *
	 * @param image the image
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          or null for full connectivity (a {@link RectangleShape} of span
	 *          1, i.e. 8-connectivity in 2D and 26-connectivity in 3D)
	 * @param minTree whether to build the min-tree instead of the max-tree
	 */
	ComponentTree(final RandomAccessibleInterval<? extends RealType<?>> image,
		final Shape connectivity, final boolean minTree)
	{
		if (Intervals.numElements(image) > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException(
				"Images with more than 2^31 pixels are not supported");
		}
		this.minTree = minTree;
		n = image.numDimensions();
		dims = new int[n];
		final var strides = new int[n];
		var s = 1;
		for (var d = 0; d < n; d++) {
			dims[d] = (int) image.dimension(d);
			strides[d] = s;
			s *= dims[d];
		}
		size = s;

		values = new double[size];
		var i = 0;
		for (final var t : Views.flatIterable(image)) {
			final var v = t.getRealDouble();
			if (Double.isNaN(v)) {
				throw new IllegalArgumentException("NaN values are not supported");
			}
			// Note: adding 0.0 turns -0.0 into 0.0, so they share one rank.
			values[i++] = (minTree ? -v : v) + 0.0;
		}
		ranks = new int[size];
		order = sortDecreasing(values, ranks);
		parent = new int[size];
		build(neighborOffsets(connectivity, n), strides);
	}

	/**
	 * Tests whether a pixel is the canonical pixel of its node.
	 *
	 * @param p the pixel index
	 * @return true if the pixel represents a node
	 */
	boolean isCanonical(final int p) {
		final var q = parent[p];
		return q == p || ranks[q] != ranks[p];
	}

	/**
	 * Computes the position of the pixel with the given linear index.
	 *
	 * @param index the linear index
	 * @param pos the array to fill with the position
	 */
	void position(int index, final int[] pos) {
		for (var d = 0; d < n; d++) {
			pos[d] = index % dims[d];
			index /= dims[d];
		}
	}

	/**
	 * Computes the pixel count of every node, accumulated at its canonical
	 * pixel.
	 *
	 * @return the area of the node of each canonical pixel
	 */
	double[] areas() {
		final var area = new double[size];
		Arrays.fill(area, 1);
		for (final var p : order) {
			if (parent[p] != p) area[parent[p]] += area[p];
		}
		return area;
	}

	/**
	 * Computes the bounding box diagonal of every node, at its canonical pixel:
	 * the Euclidean distance between the centers of two opposite corner pixels
	 * of the bounding box (0 for a single pixel).
	 *
	 * @return the bounding box diagonal of the node of each canonical pixel
	 */
	double[] boxDiagonals() {
		final var min = new int[size * n];
		final var max = new int[size * n];
		final var pos = new int[n];
		for (var p = 0; p < size; p++) {
			position(p, pos);
			System.arraycopy(pos, 0, min, p * n, n);
			System.arraycopy(pos, 0, max, p * n, n);
		}
		for (final var o : order) {
			if (parent[o] == o) continue;
			final var p = o * n;
			final var q = parent[o] * n;
			for (var d = 0; d < n; d++) {
				if (min[p + d] < min[q + d]) min[q + d] = min[p + d];
				if (max[p + d] > max[q + d]) max[q + d] = max[p + d];
			}
		}
		final var diagonal = new double[size];
		for (var p = 0; p < size; p++) {
			var sum = 0.0;
			for (var d = 0; d < n; d++) {
				final double extent = max[p * n + d] - min[p * n + d];
				sum += extent * extent;
			}
			diagonal[p] = Math.sqrt(sum);
		}
		return diagonal;
	}

	/**
	 * Computes the volume of every node, at its canonical pixel: the sum, over
	 * the pixels of the node, of their height above the level of the parent
	 * node. A root has an infinite volume.
	 *
	 * @return the volume of the node of each canonical pixel
	 */
	double[] volumes() {
		final var area = areas();
		final var sum = values.clone();
		for (final var p : order) {
			if (parent[p] != p) sum[parent[p]] += sum[p];
		}
		for (var p = 0; p < size; p++) {
			final var q = parent[p];
			sum[p] = q == p ? Double.POSITIVE_INFINITY : sum[p] - area[p] *
				values[q];
		}
		return sum;
	}

	/**
	 * Filters the image with the direct rule: each node whose attribute is
	 * below the threshold is removed, its pixels taking the level of the
	 * nearest preserved ancestor node, while all other nodes keep their level.
	 * The roots are always preserved.
	 *
	 * @param attributes the attribute of each node, at its canonical pixel
	 * @param threshold the minimal attribute value of preserved nodes
	 * @return the filtered values (negated back for a min-tree)
	 */
	double[] filter(final double[] attributes, final double threshold) {
		final var out = new double[size];
		for (var i = size - 1; i >= 0; i--) {
			final var p = order[i];
			final var q = parent[p];
			if (q == p) out[p] = values[p];
			else if (ranks[q] != ranks[p]) {
				out[p] = attributes[p] >= threshold ? values[p] : out[q];
			}
			else out[p] = out[q];
		}
		if (minTree) {
			for (var p = 0; p < size; p++)
				out[p] = -out[p];
		}
		return out;
	}

	/**
	 * Writes values into an image of the dimensions of the tree, in flat
	 * iteration order.
	 *
	 * @param result the values to write
	 * @param image the image to write into
	 */
	void write(final double[] result,
		final RandomAccessibleInterval<? extends RealType<?>> image)
	{
		if (image.numDimensions() != n) {
			throw new IllegalArgumentException("Expected " + n +
				" dimensions but got " + image.numDimensions());
		}
		for (var d = 0; d < n; d++) {
			if (image.dimension(d) != dims[d]) {
				throw new IllegalArgumentException("Output dimensions " + Arrays
					.toString(image.dimensionsAsLongArray()) + " do not match " + Arrays
						.toString(dims));
			}
		}
		var i = 0;
		for (final var t : Views.flatIterable(image))
			t.setReal(result[i++]);
	}

	/**
	 * Links the pixels into the tree: union-find over the pixels in decreasing
	 * order of value, followed by canonicalization.
	 */
	private void build(final int[][] offsets, final int[] strides) {
		final var steps = new int[offsets.length];
		var reach = 0;
		for (var k = 0; k < offsets.length; k++) {
			for (var d = 0; d < n; d++) {
				steps[k] += offsets[k][d] * strides[d];
				reach = Math.max(reach, Math.abs(offsets[k][d]));
			}
		}

		final var zpar = new int[size];
		Arrays.fill(zpar, -1);
		final var pos = new int[n];
		for (final var p : order) {
			parent[p] = p;
			zpar[p] = p;
			position(p, pos);
			final var interior = isInterior(pos, reach);
			for (var k = 0; k < offsets.length; k++) {
				if (!interior && !contains(pos, offsets[k])) continue;
				final var q = p + steps[k];
				if (zpar[q] < 0) continue;
				final var r = find(zpar, q);
				if (r != p) {
					parent[r] = p;
					zpar[r] = p;
				}
			}
		}

		for (var i = size - 1; i >= 0; i--) {
			final var p = order[i];
			final var q = parent[p];
			if (ranks[parent[q]] == ranks[q]) parent[p] = parent[q];
		}
	}

	private boolean isInterior(final int[] pos, final int reach) {
		for (var d = 0; d < n; d++) {
			if (pos[d] < reach || pos[d] >= dims[d] - reach) return false;
		}
		return true;
	}

	private boolean contains(final int[] pos, final int[] offset) {
		for (var d = 0; d < n; d++) {
			final var c = pos[d] + offset[d];
			if (c < 0 || c >= dims[d]) return false;
		}
		return true;
	}

	/** Finds the root of a union-find set, halving the path on the way. */
	private static int find(final int[] zpar, int p) {
		while (zpar[p] != p) {
			zpar[p] = zpar[zpar[p]];
			p = zpar[p];
		}
		return p;
	}

	/**
	 * Sorts the pixels by decreasing value (ties in increasing index order),
	 * using a counting sort over the ranks of the distinct values.
	 */
	private static int[] sortDecreasing(final double[] values,
		final int[] ranks)
	{
		final var sorted = values.clone();
		Arrays.sort(sorted);
		var levels = 0;
		for (var i = 0; i < sorted.length; i++) {
			if (i == 0 || sorted[i] != sorted[levels - 1]) sorted[levels++] =
				sorted[i];
		}
		final var count = new int[levels + 1];
		for (var p = 0; p < values.length; p++) {
			ranks[p] = Arrays.binarySearch(sorted, 0, levels, values[p]);
			count[levels - 1 - ranks[p] + 1]++;
		}
		for (var r = 1; r <= levels; r++)
			count[r] += count[r - 1];
		final var order = new int[values.length];
		for (var p = 0; p < values.length; p++)
			order[count[levels - 1 - ranks[p]]++] = p;
		return order;
	}

	/**
	 * Lists the offsets of a shape around the origin, made symmetric and
	 * without the origin itself.
	 */
	private static int[][] neighborOffsets(final Shape connectivity,
		final int n)
	{
		final var shape = connectivity == null ? new RectangleShape(1, false)
			: connectivity;
		final var source = ConstantUtils.constantRandomAccessible(new DoubleType(),
			n);
		final var ra = shape.neighborhoodsRandomAccessible(source).randomAccess();
		ra.setPosition(new long[n]);
		final var set = new LinkedHashSet<List<Integer>>();
		final var c = ra.get().localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final var o = new ArrayList<Integer>(n);
			final var neg = new ArrayList<Integer>(n);
			var zero = true;
			for (var d = 0; d < n; d++) {
				final var v = c.getIntPosition(d);
				o.add(v);
				neg.add(-v);
				if (v != 0) zero = false;
			}
			if (zero) continue;
			set.add(o);
			set.add(neg);
		}
		final var result = new int[set.size()][];
		var k = 0;
		for (final var o : set) {
			result[k++] = o.stream().mapToInt(Integer::intValue).toArray();
		}
		return result;
	}
}
