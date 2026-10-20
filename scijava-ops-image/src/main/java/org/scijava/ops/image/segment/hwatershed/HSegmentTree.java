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

/*
 * This file is derived from Interactive-H-Watershed
 * (https://github.com/mpicbg-scicomp/Interactive-H-Watershed), whose original
 * notice follows.
 *
 * Author: Benoit Lombardot, Scientific Computing Facility, MPI-CBG, Dresden
 *
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 * Dresden, Germany
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1 - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2 - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3 - Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.scijava.ops.image.segment.hwatershed;

import java.util.ArrayDeque;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Hierarchy of the peaks (regional maxima) of an n-dimensional image, together
 * with the watershed partition of the image into one catchment basin per peak.
 * <p>
 * The hierarchy is built once by flooding the image from its regional maxima in
 * decreasing intensity order (an IFT-style watershed). Whenever two flooded
 * regions meet at a saddle, the <em>dynamics</em> of the lower peak (peak
 * height minus saddle height) is recorded and the two nodes are merged under a
 * new node. The resulting binary tree can then be cut cheaply for any seed
 * dynamics {@code hMin}, intensity threshold and peak flooding percentage via
 * {@link #labels(double, double, double, boolean)}.
 * </p>
 * <p>
 * This is a translation of {@code HWatershedLabeling},
 * {@code imgTools.LocalMaximaLabeling}, {@code HTreeLabeling} and
 * {@code SegmentHierarchyToLabelMap} from the Interactive H-Watershed Fiji
 * plugin by Benoit Lombardot (Scientific Computing Facility, MPI-CBG, Dresden),
 * released under the BSD-3-Clause license. Differences from the original:
 * intensities are kept as {@code double} instead of being truncated to
 * {@code int}, so the hierarchical FIFO queue is replaced by an equivalent
 * priority queue (higher value first, FIFO among equal values); the interactive
 * UI, progress dialog and slice-wise relabelling were dropped. On
 * integer-valued images, results are identical to the original.
 * </p>
 * <p>
 * Node ids: {@code 0} is unused (background), leaves (one per regional maximum)
 * are {@code 1..numLeaves()}, and merge nodes follow.
 * </p>
 *
 * @author Benoit Lombardot (MPI-CBG)
 */
public final class HSegmentTree {

	/** Value subtracted to break ties between sibling peaks of equal height. */
	private static final double EPSILON = 1e-9;

	private final double[] values;
	private final int[] leafMap;
	private final int numLeaves;
	private final int[] parent;
	private final int[][] children;
	private final double[] dynamics;
	private final double[] imax;
	private final double min;
	private final double max;

	// These derived features are used to cut the tree (see HTreeLabeling).
	private final double[] imin;
	private final double[] criteria;
	private final int[] roots;

	private HSegmentTree(final double[] values, final int[] leafMap,
		final int numLeaves, final int[] parent, final int[][] children,
		final double[] dynamics, final double[] imax, final double min,
		final double max)
	{
		this.values = values;
		this.leafMap = leafMap;
		this.numLeaves = numLeaves;
		this.parent = parent;
		this.children = children;
		this.dynamics = dynamics;
		this.imax = imax;
		this.min = min;
		this.max = max;

		final var nNodes = parent.length;
		// Break ties between sibling peaks of identical height.
		for (var node = 0; node < nNodes; node++) {
			final var c = children[node];
			if (c[0] >= 0 && c[1] >= 0 && imax[c[0]] == imax[c[1]]) {
				imax[c[1]] -= EPSILON;
				dynamics[c[1]] -= EPSILON;
			}
		}
		imin = new double[nNodes];
		criteria = new double[nNodes];
		var rootCount = 0;
		for (var node = 0; node < nNodes; node++) {
			imin[node] = imax[node] - dynamics[node];
			final var c = children[node];
			criteria[node] = isLeaf(node) ? 0 : //
				Math.min(dynamics[c[0]], dynamics[c[1]]);
			if (parent[node] == node) rootCount++;
		}
		roots = new int[rootCount];
		for (int node = 0, r = 0; node < nNodes; node++) {
			if (parent[node] == node) roots[r++] = node;
		}
	}

	// -- Construction --

	/**
	 * Builds the peak hierarchy of the given image.
	 *
	 * @param image the image whose bright peaks are to be organized.
	 * @param fullConnectivity if true, pixels touching by a corner are neighbors
	 *          (8-connectivity in 2D, 26 in 3D); otherwise only pixels sharing a
	 *          face are (4-connectivity in 2D, 6 in 3D). This applies to the
	 *          flooding; regional maxima are always detected with full
	 *          connectivity, as in the original plugin.
	 * @return the peak hierarchy
	 */
	public static <T extends RealType<T>> HSegmentTree build(
		final RandomAccessibleInterval<T> image, final boolean fullConnectivity)
	{
		final var size = Intervals.numElements(image);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image too large: " + size +
				" elements");
		}
		final var dims = image.dimensionsAsLongArray();
		final var values = new double[(int) size];
		var min = Double.POSITIVE_INFINITY;
		var max = Double.NEGATIVE_INFINITY;
		var i = 0;
		for (final var t : Views.flatIterable(image)) {
			final var v = t.getRealDouble();
			values[i++] = v;
			if (v < min) min = v;
			if (v > max) max = v;
		}

		// Label the regional maxima; these are the leaves of the tree.
		final var leafMap = new int[values.length];
		final var numLeaves = labelMaxima(values, dims, min, leafMap);
		if (numLeaves <= 0) {
			// A flat image has no peaks at all.
			return new HSegmentTree(values, new int[values.length], 0, new int[0],
				new int[0][], new double[0], new double[0], min, max);
		}

		// Initialize the tree.
		final var nNodes = 2 * numLeaves;
		final var dynamics = new double[nNodes];
		final var imax = new double[nNodes];
		final var parent = new int[nNodes];
		final var children = new int[nNodes][];
		for (var n = 0; n < nNodes; n++) {
			children[n] = new int[] { -1, -1 };
			parent[n] = n;
			imax[n] = min;
		}

		// Queue the seeds.
		final var queue = new PixelQueue(values);
		for (var p = 0; p < values.length; p++) {
			if (leafMap[p] > 0) {
				queue.add(p);
				imax[leafMap[p]] = values[p];
			}
		}

		// Flood the image from the seeds, building the tree as basins meet.
		final var offsets = neighborOffsets(dims.length, fullConnectivity);
		final var dequeued = new boolean[values.length];
		final var pos = new long[dims.length];
		final var nPos = new long[dims.length];
		var newNode = numLeaves;
		while (!queue.isEmpty()) {
			final var p = queue.poll();
			final var pVal = values[p];
			indexToPosition(p, dims, pos);
			final var pLeaf = leafMap[p];
			var pNode = findRoot(pLeaf, parent);
			dequeued[p] = true;

			for (final var offset : offsets) {
				if (!neighbor(pos, offset, dims, nPos)) continue;
				final var n = positionToIndex(nPos, dims);
				if (dequeued[n]) {
					// p is the lowest point between the two regions.
					final var nLeaf = leafMap[n];
					final var nNode = findRoot(nLeaf, parent);
					if (nNode != pNode) {
						// Two distinct nodes meet and p is the saddle: merge them.
						newNode++;
						final var hn = imax[nNode] - pVal;
						dynamics[nNode] = hn;
						final var hp = imax[pNode] - pVal;
						dynamics[pNode] = hp;

						// Merge the node with the smallest dynamics with the first
						// node on the other branch that has a higher dynamics.
						final int node1;
						final int node2;
						if (hp == hn) {
							node1 = pNode;
							node2 = nNode;
						}
						else if (hp < hn) {
							node1 = pNode;
							node2 = mergeTarget(nLeaf, hp, parent, children, dynamics);
						}
						else {
							node1 = nNode;
							node2 = mergeTarget(pLeaf, hn, parent, children, dynamics);
						}
						mergeNodes(node1, node2, newNode, parent, children);
						pNode = findRoot(newNode, parent);
						imax[newNode] = Math.max(imax[node1], imax[node2]);
						dynamics[newNode] = Math.max(dynamics[node1], dynamics[node2]);
					}
				}
				if (leafMap[n] == 0) {
					// The neighbor is not queued yet.
					leafMap[n] = pLeaf;
					queue.add(n);
				}
			}
		}

		// Root heights are measured relative to the image minimum.
		for (var n = 0; n < nNodes; n++) {
			if (dynamics[n] > 0 && parent[n] == n) dynamics[n] = imax[n] - min;
		}

		return new HSegmentTree(values, leafMap, numLeaves, parent, children,
			dynamics, imax, min, max);
	}

	// -- Accessors --

	/** @return the number of regional maxima (leaves of the tree). */
	public int numLeaves() {
		return numLeaves;
	}

	/** @return the number of node slots in the tree (including unused id 0). */
	public int numNodes() {
		return parent.length;
	}

	/**
	 * @param node a node id
	 * @return the parent of the node, or the node itself if it is a root
	 */
	public int parent(final int node) {
		return parent[node];
	}

	/**
	 * @param node a node id
	 * @return the dynamics (peak height above its merge saddle) of the node
	 */
	public double dynamics(final int node) {
		return dynamics[node];
	}

	/**
	 * @param node a node id
	 * @return the maximum intensity of the node
	 */
	public double peakValue(final int node) {
		return imax[node];
	}

	/** @return the minimum intensity of the image. */
	public double min() {
		return min;
	}

	/** @return the maximum intensity of the image. */
	public double max() {
		return max;
	}

	/**
	 * @return the leaf (regional maximum basin) each pixel was flooded from, in
	 *         flat iteration order; 0 only for a flat image.
	 */
	public int[] leafMap() {
		return leafMap;
	}

	// -- Tree cut --

	/**
	 * Cuts the tree and labels the image accordingly.
	 *
	 * @param hMin minimal dynamics of a peak for it to seed its own segment.
	 * @param threshold pixels below this intensity are background.
	 * @param peakFlooding percentage (0-100) of each segment's peak height above
	 *          the threshold that is flooded; pixels below
	 *          {@code threshold + (peak - threshold) * (1 - peakFlooding / 100)}
	 *          are background.
	 * @param allowSplitting if true, peaks with dynamics below {@code hMin} that
	 *          get separated from their parent segment by the threshold (or by
	 *          the peak flooding level) become segments of their own; otherwise
	 *          they are discarded.
	 * @return the label of each pixel (in flat iteration order), 0 for
	 *         background; labels are numbered consecutively from 1
	 */
	public int[] labels(final double hMin, final double threshold,
		double peakFlooding, final boolean allowSplitting)
	{
		final var out = new int[values.length];
		final var nNodes = parent.length;
		if (nNodes == 0) return out;

		peakFlooding = Math.min(100, Math.max(0, peakFlooding)) / 100;
		final var thresholds = new double[nNodes];
		for (var n = 0; n < nNodes; n++) {
			thresholds[n] = threshold + (imax[n] - threshold) * (1 - peakFlooding);
		}

		final var labelRoot = new int[nNodes];

		// Search breadth-first for the topmost nodes below the hMin cut.
		final var queue = new ArrayDeque<Integer>();
		final var queueA = new ArrayDeque<Integer>();
		for (final var r : roots) {
			labelRoot[r] = r;
			queue.add(r);
		}
		while (!queue.isEmpty()) {
			final int node = queue.poll();
			if (criteria[node] <= hMin) {
				labelRoot[node] = node;
				queueA.add(node);
			}
			else {
				labelRoot[node] = 0;
				for (final var c : children[node]) {
					if (c >= 0) queue.add(c);
				}
			}
		}

		// Propagate labels down, removing or splitting off the peaks that are
		// disconnected from their segment by the threshold or flooding level.
		while (!queueA.isEmpty()) {
			final int node = queueA.poll();
			final var root = labelRoot[node];
			if (root == 0) {
				// There is nothing to do.
			}
			else if (imax[node] < threshold) {
				labelRoot[node] = 0;
			}
			else if (threshold > imin[node] && imax[root] > imax[node]) {
				labelRoot[node] = allowSplitting ? node : 0;
			}
			else if (threshold <= imin[node]) {
				if (thresholds[root] > imin[node] && imax[root] > imax[node]) {
					labelRoot[node] = allowSplitting ? node : 0;
				}
			}
			for (final var c : children[node]) {
				if (c >= 0) {
					labelRoot[c] = labelRoot[node];
					queueA.add(c);
				}
			}
		}

		// Number the segments consecutively.
		final var nodeToLabel = new int[nNodes];
		var label = 0;
		for (var n = 0; n < nNodes; n++) {
			if (labelRoot[n] == n && n > 0) nodeToLabel[n] = ++label;
		}

		// Label the pixels.
		for (var p = 0; p < values.length; p++) {
			final var v = values[p];
			if (v < threshold) continue;
			final var node = leafMap[p];
			final var root = labelRoot[node];
			if (v >= thresholds[root]) out[p] = nodeToLabel[root];
		}
		return out;
	}

	// -- Helper methods --

	private boolean isLeaf(final int node) {
		return children[node][0] < 0 && children[node][1] < 0;
	}

	/**
	 * Labels the regional maxima (plateaus with full connectivity whose
	 * neighbors are all lower) strictly above {@code threshold}. Labels are
	 * assigned in reverse flat order, as in the original plugin.
	 *
	 * @return the number of maxima
	 */
	private static int labelMaxima(final double[] values, final long[] dims,
		final double threshold, final int[] labels)
	{
		final var n = values.length;
		final var par = labels; // These are union-find parents, reused as output.
		final var isMaxRoot = new boolean[n];
		java.util.Arrays.fill(par, -1);

		// Only the neighbors preceding the center pixel in flat order are needed.
		final var allOffsets = neighborOffsets(dims.length, true);
		final var offsets = java.util.Arrays.copyOf(allOffsets, allOffsets.length /
			2);
		final var pos = new long[dims.length];
		final var nPos = new long[dims.length];

		for (var p = 0; p < n; p++) {
			final var pVal = values[p];
			if (!(pVal > threshold)) continue;
			par[p] = p;
			isMaxRoot[p] = true;
			indexToPosition(p, dims, pos);
			for (final var offset : offsets) {
				if (!neighbor(pos, offset, dims, nPos)) continue;
				final var q = positionToIndex(nPos, dims);
				final var nVal = values[q];
				if (!(nVal > threshold)) continue;
				if (pVal >= nVal) {
					final var r = findRootCompress(q, par);
					if (r == p) continue;
					if (pVal == nVal) {
						par[r] = p;
						isMaxRoot[p] &= isMaxRoot[r];
					}
					isMaxRoot[r] = false;
				}
				else {
					isMaxRoot[p] = false;
				}
			}
		}

		// Plateau roots always come after their members in flat order.
		var label = 0;
		for (var i = n - 1; i >= 0; i--) {
			if (par[i] < 0) continue;
			if (par[i] == i) par[i] = isMaxRoot[i] ? ++label : 0;
			else par[i] = par[par[i]];
		}
		for (var i = 0; i < n; i++) {
			if (par[i] < 0) par[i] = 0;
		}
		return label;
	}

	private static int findRootCompress(final int p, final int[] par) {
		var root = p;
		while (par[root] != root)
			root = par[root];
		var q = p;
		while (par[q] != root) {
			final var next = par[q];
			par[q] = root;
			q = next;
		}
		return root;
	}

	/**
	 * Walks up from {@code leaf} to the first node with dynamics greater than
	 * {@code h1} whose parent merge does not involve a sibling of lower dynamics
	 * than {@code h1}.
	 */
	private static int mergeTarget(final int leaf, final double h1,
		final int[] parent, final int[][] children, final double[] dynamics)
	{
		var node2 = leaf;
		while (dynamics[node2] <= h1)
			node2 = parent[node2];
		while (h1 > mergeLevel(node2, parent, children, dynamics))
			node2 = parent[node2];
		return node2;
	}

	private static double mergeLevel(final int node, final int[] parent,
		final int[][] children, final double[] dynamics)
	{
		if (parent[node] == node) return Double.POSITIVE_INFINITY;
		final var c = children[parent[node]];
		return Math.min(dynamics[c[0]], dynamics[c[1]]);
	}

	private static void mergeNodes(final int node1, final int node2,
		final int newNode, final int[] parent, final int[][] children)
	{
		replaceChild(node1, newNode, parent, children);
		replaceChild(node2, newNode, parent, children);
		parent[node1] = newNode;
		parent[node2] = newNode;
		children[newNode][0] = node1;
		children[newNode][1] = node2;
	}

	private static void replaceChild(final int node, final int newNode,
		final int[] parent, final int[][] children)
	{
		final var par = parent[node];
		if (par == node) return;
		for (var i = 0; i < 2; i++) {
			if (children[par][i] == node) {
				children[par][i] = newNode;
				parent[newNode] = par;
			}
		}
	}

	private static int findRoot(int node, final int[] parent) {
		while (parent[node] != node)
			node = parent[node];
		return node;
	}

	/**
	 * Offsets of the neighbors of a pixel, in flat (raster) order with dimension
	 * 0 varying fastest.
	 */
	private static long[][] neighborOffsets(final int nDims,
		final boolean fullConnectivity)
	{
		final var count = (int) Math.pow(3, nDims);
		final var list = new java.util.ArrayList<long[]>();
		for (var i = 0; i < count; i++) {
			final var offset = new long[nDims];
			var idx = i;
			var nonZero = 0;
			for (var d = 0; d < nDims; d++) {
				offset[d] = idx % 3 - 1;
				idx /= 3;
				if (offset[d] != 0) nonZero++;
			}
			if (nonZero == 0) continue;
			if (fullConnectivity || nonZero == 1) list.add(offset);
		}
		return list.toArray(new long[0][]);
	}

	private static boolean neighbor(final long[] pos, final long[] offset,
		final long[] dims, final long[] nPos)
	{
		for (var d = 0; d < dims.length; d++) {
			final var v = pos[d] + offset[d];
			if (v < 0 || v >= dims[d]) return false;
			nPos[d] = v;
		}
		return true;
	}

	private static void indexToPosition(int index, final long[] dims,
		final long[] pos)
	{
		for (var d = 0; d < dims.length; d++) {
			pos[d] = index % dims[d];
			index /= (int) dims[d];
		}
	}

	private static int positionToIndex(final long[] pos, final long[] dims) {
		long index = 0;
		for (var d = dims.length - 1; d >= 0; d--) {
			index = index * dims[d] + pos[d];
		}
		return (int) index;
	}

	/**
	 * Priority queue of pixel indices: higher values first, first-in first-out
	 * among equal values. Equivalent to the hierarchical FIFO queue of the
	 * original implementation, without requiring integer intensities.
	 */
	private static final class PixelQueue {

		private final double[] values;
		private int[] heap = new int[16];
		private long[] order = new long[16];
		private int size;
		private long counter;

		PixelQueue(final double[] values) {
			this.values = values;
		}

		boolean isEmpty() {
			return size == 0;
		}

		void add(final int index) {
			if (size == heap.length) {
				final var capacity = (int) Math.min(Integer.MAX_VALUE - 8, 2L * size);
				heap = java.util.Arrays.copyOf(heap, capacity);
				order = java.util.Arrays.copyOf(order, capacity);
			}
			var i = size++;
			final var seq = counter++;
			while (i > 0) {
				final var parent = (i - 1) / 2;
				if (!before(index, seq, heap[parent], order[parent])) break;
				heap[i] = heap[parent];
				order[i] = order[parent];
				i = parent;
			}
			heap[i] = index;
			order[i] = seq;
		}

		int poll() {
			final var result = heap[0];
			final var lastIndex = heap[--size];
			final var lastSeq = order[size];
			var i = 0;
			while (true) {
				var child = 2 * i + 1;
				if (child >= size) break;
				if (child + 1 < size && before(heap[child + 1], order[child + 1],
					heap[child], order[child])) child++;
				if (!before(heap[child], order[child], lastIndex, lastSeq)) break;
				heap[i] = heap[child];
				order[i] = order[child];
				i = child;
			}
			heap[i] = lastIndex;
			order[i] = lastSeq;
			return result;
		}

		private boolean before(final int a, final long seqA, final int b,
			final long seqB)
		{
			final var va = values[a];
			final var vb = values[b];
			return va > vb || (va == vb && seqA < seqB);
		}
	}
}
