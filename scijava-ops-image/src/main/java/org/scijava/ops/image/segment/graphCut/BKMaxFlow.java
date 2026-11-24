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

package org.scijava.ops.image.segment.graphCut;

import java.util.Arrays;

/**
 * Computes the maximum flow, and the corresponding minimum cut, of a directed
 * graph with a source and a sink terminal, using the augmenting path algorithm
 * of Boykov &amp; Kolmogorov (2004), "An experimental comparison of
 * min-cut/max-flow algorithms for energy minimization in vision", IEEE Trans.
 * Pattern Anal. Mach. Intell. 26(9):1124-1137.
 * <p>
 * The algorithm grows two search trees, from the source and from the sink,
 * through edges with residual capacity. When the trees touch, the flow is
 * augmented along the path they form, and the nodes whose tree edges became
 * saturated are orphaned. The orphans are then adopted by other nodes of the
 * same tree, or freed. Unlike other augmenting path methods, the trees are
 * reused from one augmentation to the next, which makes the algorithm fast on
 * the sparse, grid-like graphs of image segmentation. A new parent of an
 * orphan is chosen among its candidates with the shortest path to the
 * terminal, which is tracked with time stamps as described in the paper.
 * </p>
 * <p>
 * Usage: create the graph with its number of nodes, set the terminal
 * capacities with {@link #addTerminalWeights} and the edges with
 * {@link #addEdge}, then call {@link #computeMaximumFlow()} once and query
 * {@link #isSource} for the segmentation. Capacities must be nonnegative.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class BKMaxFlow {

	private static final byte FREE = 0;
	private static final byte SOURCE = 1;
	private static final byte SINK = 2;

	/** Parent marker of a node attached directly to its terminal. */
	private static final int TERMINAL = -1;

	/** Parent marker of a node which has lost its parent. */
	private static final int ORPHAN = -2;

	private final int nodeCount;

	/**
	 * The residual terminal capacity of each node: from the source if positive,
	 * to the sink if negative.
	 */
	private final double[] terminalCapacity;

	/** The first edge leaving each node, or -1. */
	private final int[] firstEdge;

	/** The tree of each node. */
	private final byte[] tree;

	/** The edge from each node to its parent in its tree, or a marker. */
	private final int[] parent;

	/** The augmentation at which the distance of each node was last known. */
	private final int[] timestamp;

	/** The number of edges from each node to its terminal. */
	private final int[] distance;

	private final boolean[] active;

	/** The node each edge points to. Edge {@code e ^ 1} is its reverse. */
	private int[] head;

	/** The next edge leaving the same node as each edge, or -1. */
	private int[] nextEdge;

	/** The residual capacity of each edge. */
	private double[] capacity;

	private int edgeCount;
	private double flow;
	private int time;
	private boolean computed;

	/**
	 * Creates a graph without edges.
	 *
	 * @param nodeCount the number of nodes, which are numbered from 0
	 * @param edgeCountHint the expected number of calls to {@link #addEdge}
	 */
	public BKMaxFlow(final int nodeCount, final int edgeCountHint) {
		this.nodeCount = nodeCount;
		terminalCapacity = new double[nodeCount];
		firstEdge = new int[nodeCount];
		Arrays.fill(firstEdge, -1);
		tree = new byte[nodeCount];
		parent = new int[nodeCount];
		timestamp = new int[nodeCount];
		distance = new int[nodeCount];
		active = new boolean[nodeCount];
		final var capacityHint = 2 * Math.max(1, edgeCountHint);
		head = new int[capacityHint];
		nextEdge = new int[capacityHint];
		capacity = new double[capacityHint];
	}

	/** Gets the number of nodes. */
	public int nodeCount() {
		return nodeCount;
	}

	/**
	 * Adds capacities to the edges from the source to a node and from the node
	 * to the sink.
	 *
	 * @param node the node
	 * @param source the capacity to add to the edge from the source
	 * @param sink the capacity to add to the edge to the sink
	 */
	public void addTerminalWeights(final int node, final double source,
		final double sink)
	{
		checkModifiable();
		checkCapacity(source);
		checkCapacity(sink);
		// Note: pushing min(source, sink) through the node right away leaves
		// residual capacity on at most one of its terminal edges.
		final var residual = terminalCapacity[node];
		final double fromSource = Math.max(residual, 0) + source;
		final double toSink = Math.max(-residual, 0) + sink;
		flow += Math.min(fromSource, toSink);
		terminalCapacity[node] = fromSource - toSink;
	}

	/**
	 * Adds a pair of opposite edges between two nodes.
	 *
	 * @param p the first node
	 * @param q the second node
	 * @param capacityPQ the capacity of the edge from {@code p} to {@code q}
	 * @param capacityQP the capacity of the edge from {@code q} to {@code p}
	 */
	public void addEdge(final int p, final int q, final double capacityPQ,
		final double capacityQP)
	{
		checkModifiable();
		checkCapacity(capacityPQ);
		checkCapacity(capacityQP);
		if (p == q) throw new IllegalArgumentException("Self loops are not allowed.");
		if (edgeCount + 2 > head.length) {
			final var length = Math.max(4, 2 * head.length);
			head = Arrays.copyOf(head, length);
			nextEdge = Arrays.copyOf(nextEdge, length);
			capacity = Arrays.copyOf(capacity, length);
		}
		final var e = edgeCount;
		head[e] = q;
		capacity[e] = capacityPQ;
		nextEdge[e] = firstEdge[p];
		firstEdge[p] = e;
		head[e + 1] = p;
		capacity[e + 1] = capacityQP;
		nextEdge[e + 1] = firstEdge[q];
		firstEdge[q] = e + 1;
		edgeCount += 2;
	}

	/**
	 * Computes the maximum flow from the source to the sink.
	 *
	 * @return the value of the maximum flow, which equals the capacity of the
	 *         minimum cut
	 */
	public double computeMaximumFlow() {
		checkModifiable();
		computed = true;

		final var queue = new IntQueue(nodeCount);
		final var orphans = new IntQueue(16);
		for (var p = 0; p < nodeCount; p++) {
			if (terminalCapacity[p] == 0) continue;
			tree[p] = terminalCapacity[p] > 0 ? SOURCE : SINK;
			parent[p] = TERMINAL;
			distance[p] = 1;
			activate(queue, p);
		}

		var current = -1;
		while (true) {
			// Growth: extend the trees from an active node until they touch.
			// Note: the current node is active, but no longer queued.
			if (current >= 0 && tree[current] == FREE) {
				active[current] = false;
				current = -1;
			}
			var meeting = -1;
			while (meeting < 0) {
				if (current < 0) {
					if (queue.isEmpty()) return flow;
					current = queue.poll();
					if (tree[current] == FREE) {
						active[current] = false;
						current = -1;
						continue;
					}
				}
				meeting = grow(queue, current);
				if (meeting < 0) {
					active[current] = false;
					current = -1;
				}
			}

			// Augmentation: push the bottleneck flow along the path.
			time++;
			augment(meeting, orphans);

			// Adoption: find new parents for the orphans, or free them.
			while (!orphans.isEmpty())
				adopt(orphans.poll(), queue, orphans);
		}
	}

	/**
	 * Gets whether a node is on the source side of the minimum cut, i.e. can be
	 * reached from the source through edges with residual capacity.
	 */
	public boolean isSource(final int node) {
		if (!computed) throw new IllegalStateException(
			"The maximum flow has not been computed.");
		return tree[node] == SOURCE;
	}

	/** Gets the value of the maximum flow, once computed. */
	public double getFlow() {
		if (!computed) throw new IllegalStateException(
			"The maximum flow has not been computed.");
		return flow;
	}

	/**
	 * Grows the tree of a node into its neighbors.
	 *
	 * @return an edge from the source tree to the sink tree with residual
	 *         capacity, or -1 if the node has no neighbor in the other tree
	 */
	private int grow(final IntQueue queue, final int p) {
		final var fromSource = tree[p] == SOURCE;
		for (var e = firstEdge[p]; e >= 0; e = nextEdge[e]) {
			// The residual capacity in the direction of the tree's flow.
			final var residual = fromSource ? capacity[e] : capacity[e ^ 1];
			if (residual <= 0) continue;
			final var q = head[e];
			if (tree[q] == FREE) {
				tree[q] = tree[p];
				parent[q] = e ^ 1;
				timestamp[q] = timestamp[p];
				distance[q] = distance[p] + 1;
				activate(queue, q);
			}
			else if (tree[q] != tree[p]) {
				return fromSource ? e : e ^ 1;
			}
		}
		return -1;
	}

	/** Augments the flow along the path through the given edge. */
	private void augment(final int meeting, final IntQueue orphans) {
		// Find the bottleneck capacity.
		var bottleneck = capacity[meeting];
		var p = head[meeting ^ 1];
		while (parent[p] != TERMINAL) {
			final var e = parent[p];
			bottleneck = Math.min(bottleneck, capacity[e ^ 1]);
			p = head[e];
		}
		bottleneck = Math.min(bottleneck, terminalCapacity[p]);
		p = head[meeting];
		while (parent[p] != TERMINAL) {
			final var e = parent[p];
			bottleneck = Math.min(bottleneck, capacity[e]);
			p = head[e];
		}
		bottleneck = Math.min(bottleneck, -terminalCapacity[p]);

		// Push it, orphaning the nodes whose tree edges become saturated.
		capacity[meeting] -= bottleneck;
		capacity[meeting ^ 1] += bottleneck;
		p = head[meeting ^ 1];
		while (parent[p] != TERMINAL) {
			final var e = parent[p];
			capacity[e] += bottleneck;
			capacity[e ^ 1] -= bottleneck;
			final var q = head[e];
			if (capacity[e ^ 1] <= 0) orphan(p, orphans);
			p = q;
		}
		terminalCapacity[p] -= bottleneck;
		if (terminalCapacity[p] <= 0) {
			terminalCapacity[p] = 0;
			orphan(p, orphans);
		}
		p = head[meeting];
		while (parent[p] != TERMINAL) {
			final var e = parent[p];
			capacity[e] -= bottleneck;
			capacity[e ^ 1] += bottleneck;
			final var q = head[e];
			if (capacity[e] <= 0) orphan(p, orphans);
			p = q;
		}
		terminalCapacity[p] += bottleneck;
		if (terminalCapacity[p] >= 0) {
			terminalCapacity[p] = 0;
			orphan(p, orphans);
		}
		flow += bottleneck;
	}

	private void orphan(final int p, final IntQueue orphans) {
		parent[p] = ORPHAN;
		orphans.add(p);
	}

	/** Finds a new parent for an orphan, or frees it. */
	private void adopt(final int p, final IntQueue queue,
		final IntQueue orphans)
	{
		final var fromSource = tree[p] == SOURCE;

		// Look for the valid parent with the shortest path to the terminal.
		var bestEdge = -1;
		var bestDistance = Integer.MAX_VALUE;
		for (var e = firstEdge[p]; e >= 0; e = nextEdge[e]) {
			final var q = head[e];
			if (tree[q] != tree[p]) continue;
			final var residual = fromSource ? capacity[e ^ 1] : capacity[e];
			if (residual <= 0) continue;
			final var d = rootDistance(q);
			if (d < bestDistance) {
				bestDistance = d;
				bestEdge = e;
			}
		}
		if (bestEdge >= 0) {
			parent[p] = bestEdge;
			timestamp[p] = time;
			distance[p] = bestDistance + 1;
			return;
		}

		// Free the orphan, orphaning its children.
		for (var e = firstEdge[p]; e >= 0; e = nextEdge[e]) {
			final var q = head[e];
			if (tree[q] != tree[p]) continue;
			final var residual = fromSource ? capacity[e ^ 1] : capacity[e];
			if (residual > 0) activate(queue, q);
			if (parent[q] >= 0 && head[parent[q]] == p) orphan(q, orphans);
		}
		tree[p] = FREE;
	}

	/**
	 * Gets the number of edges from a node to its terminal, or
	 * {@link Integer#MAX_VALUE} if its path leads to an orphan. Updates the
	 * distances along the path.
	 */
	private int rootDistance(final int q) {
		var steps = 0;
		var u = q;
		int d;
		while (true) {
			if (timestamp[u] == time) {
				d = distance[u] + steps;
				break;
			}
			if (parent[u] == TERMINAL) {
				d = 1 + steps;
				break;
			}
			if (parent[u] == ORPHAN) return Integer.MAX_VALUE;
			u = head[parent[u]];
			steps++;
		}
		u = q;
		for (var k = d; timestamp[u] != time; k--) {
			timestamp[u] = time;
			distance[u] = k;
			if (parent[u] == TERMINAL) break;
			u = head[parent[u]];
		}
		return d;
	}

	private void activate(final IntQueue queue, final int p) {
		if (active[p]) return;
		active[p] = true;
		queue.add(p);
	}

	private void checkModifiable() {
		if (computed) throw new IllegalStateException(
			"The maximum flow has already been computed.");
	}

	private static void checkCapacity(final double c) {
		if (!(c >= 0) || Double.isInfinite(c)) throw new IllegalArgumentException(
			"Capacities must be finite and nonnegative: " + c);
	}

	/** A growable FIFO queue of ints. */
	private static class IntQueue {

		private int[] values;
		private int start;
		private int size;

		private IntQueue(final int capacity) {
			values = new int[Math.max(1, capacity)];
		}

		private boolean isEmpty() {
			return size == 0;
		}

		private void add(final int value) {
			if (size == values.length) {
				final var grown = new int[2 * values.length];
				for (var i = 0; i < size; i++)
					grown[i] = values[(start + i) % values.length];
				values = grown;
				start = 0;
			}
			values[(start + size) % values.length] = value;
			size++;
		}

		private int poll() {
			final var value = values[start];
			start = (start + 1) % values.length;
			size--;
			return value;
		}
	}
}
