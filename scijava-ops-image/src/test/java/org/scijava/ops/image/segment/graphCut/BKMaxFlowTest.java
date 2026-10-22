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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link BKMaxFlow} against an Edmonds-Karp reference implementation and
 * a brute-force minimum cut.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class BKMaxFlowTest {

	/**
	 * On small random graphs, the maximum flow equals the brute-force minimum
	 * cut and the Edmonds-Karp flow, and the cut given by
	 * {@link BKMaxFlow#isSource} is a minimum cut.
	 */
	@Test
	public void testSmallRandomGraphs() {
		final var rnd = new Random(42);
		for (var trial = 0; trial < 500; trial++) {
			final var nodes = 1 + rnd.nextInt(10);
			final var density = 0.2 + 0.6 * rnd.nextDouble();
			final var integer = trial % 2 == 0;
			final var graph = randomGraph(rnd, nodes, density, integer);
			final var bk = graph.build();
			final var flow = bk.computeMaximumFlow();
			final var cut = graph.minCutBruteForce();
			final var reference = graph.edmondsKarp();
			assertEquals(reference, flow, 1e-9, "trial " + trial);
			assertEquals(cut, flow, 1e-9, "trial " + trial);
			final var source = new boolean[nodes];
			for (var p = 0; p < nodes; p++)
				source[p] = bk.isSource(p);
			assertEquals(flow, graph.cutCapacity(source), 1e-9, "trial " + trial);
			assertEquals(flow, bk.getFlow(), 0);
		}
	}

	/** Larger sparse random graphs, against Edmonds-Karp. */
	@Test
	public void testLargerRandomGraphs() {
		final var rnd = new Random(7);
		for (var trial = 0; trial < 20; trial++) {
			final var nodes = 100 + rnd.nextInt(200);
			final var graph = randomGraph(rnd, nodes, 4.0 / nodes, trial % 2 == 0);
			final var bk = graph.build();
			final var flow = bk.computeMaximumFlow();
			assertEquals(graph.edmondsKarp(), flow, 1e-6, "trial " + trial);
			final var source = new boolean[nodes];
			for (var p = 0; p < nodes; p++)
				source[p] = bk.isSource(p);
			assertEquals(flow, graph.cutCapacity(source), 1e-6, "trial " + trial);
		}
	}

	/** A path s - 0 - 1 - 2 - t whose middle edge is the bottleneck. */
	@Test
	public void testChain() {
		final var bk = new BKMaxFlow(3, 2);
		bk.addTerminalWeights(0, 5, 0);
		bk.addTerminalWeights(2, 0, 4);
		bk.addEdge(0, 1, 3, 0);
		bk.addEdge(1, 2, 2, 0);
		assertEquals(2, bk.computeMaximumFlow(), 0);
		assertTrue(bk.isSource(0));
		assertTrue(bk.isSource(1));
		assertFalse(bk.isSource(2));
		assertThrows(IllegalStateException.class, () -> bk.addEdge(0, 2, 1, 1));
	}

	@Test
	public void testInvalidCapacities() {
		final var bk = new BKMaxFlow(2, 1);
		assertThrows(IllegalArgumentException.class, () -> bk.addEdge(0, 1, -1,
			0));
		assertThrows(IllegalArgumentException.class, () -> bk.addTerminalWeights(
			0, Double.NaN, 0));
	}

	private static RandomGraph randomGraph(final Random rnd, final int nodes,
		final double density, final boolean integer)
	{
		final var graph = new RandomGraph(nodes);
		for (var p = 0; p < nodes; p++) {
			if (rnd.nextDouble() < 0.4) graph.capacity[graph.s][p] += value(rnd,
				integer);
			if (rnd.nextDouble() < 0.4) graph.capacity[p][graph.t] += value(rnd,
				integer);
			for (var q = p + 1; q < nodes; q++) {
				if (rnd.nextDouble() >= density) continue;
				// Note: one-way edges are common, since a capacity may be zero.
				final var pq = rnd.nextDouble() < 0.7 ? value(rnd, integer) : 0;
				final var qp = rnd.nextDouble() < 0.7 ? value(rnd, integer) : 0;
				graph.edges.add(new int[] { p, q });
				graph.capacity[p][q] += pq;
				graph.capacity[q][p] += qp;
				graph.edgeCapacities.add(new double[] { pq, qp });
			}
		}
		return graph;
	}

	private static double value(final Random rnd, final boolean integer) {
		return integer ? rnd.nextInt(10) : 10 * rnd.nextDouble();
	}

	/** A graph as a capacity matrix, with the terminals as extra nodes. */
	private static class RandomGraph {

		private final int nodes;
		private final int s;
		private final int t;
		private final double[][] capacity;
		private final List<int[]> edges = new ArrayList<>();
		private final List<double[]> edgeCapacities =
			new ArrayList<>();

		private RandomGraph(final int nodes) {
			this.nodes = nodes;
			s = nodes;
			t = nodes + 1;
			capacity = new double[nodes + 2][nodes + 2];
		}

		private BKMaxFlow build() {
			final var bk = new BKMaxFlow(nodes, edges.size());
			for (var p = 0; p < nodes; p++)
				bk.addTerminalWeights(p, capacity[s][p], capacity[p][t]);
			for (var i = 0; i < edges.size(); i++) {
				final var e = edges.get(i);
				final var c = edgeCapacities.get(i);
				bk.addEdge(e[0], e[1], c[0], c[1]);
			}
			return bk;
		}

		private double cutCapacity(final boolean[] source) {
			final var side = Arrays.copyOf(source, nodes + 2);
			side[s] = true;
			side[t] = false;
			var sum = 0.0;
			for (var p = 0; p < nodes + 2; p++)
				for (var q = 0; q < nodes + 2; q++)
					if (side[p] && !side[q]) sum += capacity[p][q];
			return sum;
		}

		private double minCutBruteForce() {
			var best = Double.POSITIVE_INFINITY;
			final var source = new boolean[nodes];
			for (var mask = 0; mask < 1 << nodes; mask++) {
				for (var p = 0; p < nodes; p++)
					source[p] = (mask >> p & 1) != 0;
				best = Math.min(best, cutCapacity(source));
			}
			return best;
		}

		private double edmondsKarp() {
			final var size = nodes + 2;
			final var residual = new double[size][];
			for (var p = 0; p < size; p++)
				residual[p] = capacity[p].clone();
			var flow = 0.0;
			while (true) {
				final var previous = new int[size];
				Arrays.fill(previous, -1);
				previous[s] = s;
				final var queue = new ArrayDeque<Integer>();
				queue.add(s);
				while (!queue.isEmpty() && previous[t] < 0) {
					final int p = queue.poll();
					for (var q = 0; q < size; q++) {
						if (previous[q] < 0 && residual[p][q] > 1e-12) {
							previous[q] = p;
							queue.add(q);
						}
					}
				}
				if (previous[t] < 0) return flow;
				var bottleneck = Double.POSITIVE_INFINITY;
				for (var q = t; q != s; q = previous[q])
					bottleneck = Math.min(bottleneck, residual[previous[q]][q]);
				for (var q = t; q != s; q = previous[q]) {
					residual[previous[q]][q] -= bottleneck;
					residual[q][previous[q]] += bottleneck;
				}
				flow += bottleneck;
			}
		}
	}
}
