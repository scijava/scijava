/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.skeleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable, undirected graph of one skeleton (tree): one connected
 * component of the skeleton image. Two vertices may be joined by several
 * edges, and an edge may join a vertex to itself.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Ignacio Arganda-Carreras
 */
public final class SkeletonGraph {

	private final List<SkeletonVertex> vertices;
	private final List<SkeletonEdge> edges;
	private final SkeletonVertex root;
	private final Map<SkeletonVertex, List<SkeletonEdge>> incidence;

	SkeletonGraph(final List<SkeletonVertex> vertices,
		final List<SkeletonEdge> edges, final SkeletonVertex root)
	{
		this.vertices = Collections.unmodifiableList(vertices);
		this.edges = Collections.unmodifiableList(edges);
		this.root = root;
		incidence = new IdentityHashMap<>();
		for (final SkeletonVertex v : vertices)
			incidence.put(v, new ArrayList<>());
		for (final SkeletonEdge e : edges) {
			incidence.computeIfAbsent(e.v1(), k -> new ArrayList<>()).add(e);
			if (e.v2() != e.v1()) {
				incidence.computeIfAbsent(e.v2(), k -> new ArrayList<>()).add(e);
			}
		}
		for (final Map.Entry<SkeletonVertex, List<SkeletonEdge>> entry : incidence
			.entrySet())
		{
			entry.setValue(Collections.unmodifiableList(entry.getValue()));
		}
	}

	/** @return the vertices of this graph */
	public List<SkeletonVertex> vertices() {
		return vertices;
	}

	/** @return the edges (branches) of this graph */
	public List<SkeletonEdge> edges() {
		return edges;
	}

	/**
	 * @return the root vertex: the first end point of the tree, or its first
	 *         junction if it has no end points, or {@code null} for a ring
	 */
	public SkeletonVertex root() {
		return root;
	}

	/**
	 * Gets the edges incident to a vertex; a loop edge is listed once.
	 *
	 * @param v a vertex of this graph
	 * @return the incident edges (empty if {@code v} is not in this graph)
	 */
	public List<SkeletonEdge> edgesOf(final SkeletonVertex v) {
		final List<SkeletonEdge> result = incidence.get(v);
		return result == null ? Collections.emptyList() : result;
	}

	@Override
	public String toString() {
		return "SkeletonGraph[" + vertices.size() + " vertices, " + edges.size() +
			" edges]";
	}
}
