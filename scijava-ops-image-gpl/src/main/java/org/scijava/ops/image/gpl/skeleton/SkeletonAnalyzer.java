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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Skeleton analysis engine shared by the skeleton graph and statistics ops: a
 * translation of the analysis part of the Fiji AnalyzeSkeleton plugin by
 * Ignacio Arganda-Carreras (GPL v3+), including the longest shortest path
 * computation by Huub Hovens. Images are handled as flat 3D arrays (a 2D image
 * is a single slice).
 *
 * @author Ignacio Arganda-Carreras
 * @author Huub Hovens
 * @author Curtis Rueden
 */
final class SkeletonAnalyzer {

	private static final byte END_POINT = TagSkeleton.END_POINT;
	private static final byte SLAB = TagSkeleton.SLAB;
	private static final byte JUNCTION = TagSkeleton.JUNCTION;

	private static final int TREE = 0;
	private static final int BACK = 1;
	private static final int UNDEFINED = -1;

	// -- Input --

	private final int width, height, depth;
	private final long[] min;
	private final int numDims;
	private final byte[] image;
	private final double[] intensity;
	private final double pw, ph, pd;
	private int xOffset = 1, yOffset = 1, zOffset = 1;

	// -- Working state --

	private byte[] tagged;
	private int[] labels;
	private boolean[] visited;

	private List<P> listOfEndPoints;
	private List<P> listOfJunctionVoxels;
	private List<P> listOfSlabVoxels;
	private List<P> listOfStartingSlabVoxels;

	private int numOfTrees;
	private int[] numberOfBranches;
	private int[] numberOfEndPoints;
	private int[] numberOfJunctionVoxels;
	private int[] numberOfSlabs;
	private int[] numberOfJunctions;
	private int[] numberOfTriplePoints;
	private int[] numberOfQuadruplePoints;
	private double[] averageBranchLength;
	private double[] maximumBranchLength;
	private List<List<P>> endPointsTree;
	private List<List<P>> junctionVoxelTree;
	private List<List<P>> startingSlabTree;
	private List<List<List<P>>> listOfSingleJunctions;
	private Vertex[][] junctionVertex;
	private List<Map<P, Vertex>> junctionVertexMap;
	private Graph[] graph;

	private List<P> slabList;
	private P auxPoint;
	private Vertex auxFinalVertex;

	// -- Results --

	private List<SkeletonGraph> graphs = Collections.emptyList();
	private List<SkeletonStatistics> statistics = Collections.emptyList();

	private SkeletonAnalyzer(final byte[] image, final long[] dims,
		final long[] min, final double[] intensity, final double[] calibration)
	{
		numDims = dims.length;
		width = (int) dims[0];
		height = (int) dims[1];
		depth = numDims == 3 ? (int) dims[2] : 1;
		this.min = min;
		this.image = image;
		this.intensity = intensity;
		pw = calibration[0];
		ph = calibration[1];
		pd = numDims == 3 ? calibration[2] : 1;
	}

	/**
	 * Analyzes a 2D or 3D skeleton.
	 *
	 * @param input the skeleton
	 * @param calibration element spacing (null for 1 along each dimension)
	 * @param pruning the cycle pruning mode (null for none)
	 * @param pruneEnds maximum length of end branches to prune; null for no
	 *          pruning, infinity to prune all end-point branches
	 * @param intensityImage optional intensity image
	 * @param shortestPath whether to compute the longest shortest paths
	 * @return the analyzer, with results available
	 */
	static <B extends BooleanType<B>, T extends RealType<T>> SkeletonAnalyzer
		analyze(final RandomAccessibleInterval<B> input, double[] calibration,
			CyclePruning pruning, final Double pruneEnds,
			final RandomAccessibleInterval<T> intensityImage,
			final boolean shortestPath)
	{
		final int n = input.numDimensions();
		if (n != 2 && n != 3) {
			throw new IllegalArgumentException(
				"Only 2D and 3D images are supported");
		}
		if (calibration == null) {
			calibration = new double[n];
			Arrays.fill(calibration, 1);
		}
		if (calibration.length != n) {
			throw new IllegalArgumentException("Calibration length (" +
				calibration.length + ") does not match image dimensionality (" + n +
				")");
		}
		for (final double c : calibration) {
			if (!(c > 0) || Double.isInfinite(c)) {
				throw new IllegalArgumentException(
					"Calibration values must be positive and finite");
			}
		}
		if (pruning == null) pruning = CyclePruning.NONE;
		if (intensityImage == null && (pruning == CyclePruning.LOWEST_INTENSITY_VOXEL ||
			pruning == CyclePruning.LOWEST_INTENSITY_BRANCH))
		{
			throw new IllegalArgumentException("Cycle pruning mode " + pruning +
				" requires an intensity image");
		}
		if (pruneEnds != null && Double.isNaN(pruneEnds)) {
			throw new IllegalArgumentException("End pruning length must not be NaN");
		}
		final long[] dims = input.dimensionsAsLongArray();
		final long size = Intervals.numElements(dims);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" elements");
		}
		final byte[] img = new byte[(int) size];
		int i = 0;
		for (final B b : Views.flatIterable(input))
			img[i++] = (byte) (b.get() ? 1 : 0);
		double[] values = null;
		if (intensityImage != null) {
			if (!Intervals.equalDimensions(input, intensityImage)) {
				throw new IllegalArgumentException(
					"Intensity image dimensions do not match the skeleton");
			}
			values = new double[(int) size];
			i = 0;
			for (final T t : Views.flatIterable(intensityImage))
				values[i++] = t.getRealDouble();
		}
		final SkeletonAnalyzer analyzer = new SkeletonAnalyzer(img, dims, input
			.minAsLongArray(), values, calibration);
		analyzer.run(pruning, pruneEnds, shortestPath);
		return analyzer;
	}

	List<SkeletonGraph> graphs() {
		return graphs;
	}

	List<SkeletonStatistics> statistics() {
		return statistics;
	}

	// -- Main procedure --

	private void run(final CyclePruning pruning, final Double pruneEnds,
		final boolean shortestPath)
	{
		boolean pruneCycles = pruning != CyclePruning.NONE;
		if (pruning == CyclePruning.LOWEST_INTENSITY_VOXEL ||
			pruning == CyclePruning.LOWEST_INTENSITY_BRANCH)
		{
			calculateNeighborhoodOffsets();
		}

		resetVisited();
		processSkeleton();

		if (pruneEnds != null) pruneEndBranches(pruneEnds);

		if (pruneCycles && pruneCycles(pruning)) {
			resetVisited();
			processSkeleton();
		}

		calculateTripleAndQuadruplePoints();

		// Convert the graphs before the shortest path computation.
		final Map<Vertex, SkeletonVertex> vertexMap = new IdentityHashMap<>();
		final List<SkeletonGraph> result = new ArrayList<>(numOfTrees);
		for (int t = 0; t < numOfTrees; t++)
			result.add(toSkeletonGraph(graph[t], vertexMap));
		graphs = Collections.unmodifiableList(result);

		final List<SkeletonStatistics> stats = new ArrayList<>(numOfTrees);
		for (int t = 0; t < numOfTrees; t++) {
			double longest = Double.NaN;
			final List<P> path = new ArrayList<>();
			if (shortestPath) longest = warshallAlgorithm(graph[t], path);
			final List<Localizable> pathPoints = new ArrayList<>(path.size());
			for (final P p : path)
				pathPoints.add(toPoint(p));
			stats.add(new SkeletonStatistics(result.get(t), numberOfBranches[t],
				numberOfJunctions[t], numberOfEndPoints[t], numberOfJunctionVoxels[t],
				numberOfSlabs[t], averageBranchLength[t], numberOfTriplePoints[t],
				numberOfQuadruplePoints[t], maximumBranchLength[t], longest,
				pathPoints));
		}
		statistics = Collections.unmodifiableList(stats);
	}

	private void calculateNeighborhoodOffsets() {
		double max = Math.max(pw, ph);
		if (numDims == 3) max = Math.max(max, pd);
		xOffset = Math.max(1, (int) Math.round(max / pw));
		yOffset = Math.max(1, (int) Math.round(max / ph));
		// Note: For a 2D image, out-of-image slices only scale the neighborhood
		// averages, so they are skipped.
		zOffset = numDims == 3 ? Math.max(1, (int) Math.round(max / pd)) : 0;
	}

	private void processSkeleton() {
		listOfEndPoints = new ArrayList<>();
		listOfJunctionVoxels = new ArrayList<>();
		listOfSlabVoxels = new ArrayList<>();
		listOfStartingSlabVoxels = new ArrayList<>();

		tagged = tagImage();
		labels = markTrees();
		if (numOfTrees == 0) return;

		initializeTrees();
		divideVoxelsByTrees();
		groupJunctions();
		resetVisited();
		for (int i = 0; i < numOfTrees; i++)
			visitSkeleton(i);
	}

	// -- Pruning --

	private void pruneEndBranches(final double length) {
		final boolean all = length == Double.POSITIVE_INFINITY;
		for (int t = 0; t < numOfTrees; t++) {
			final ListIterator<Vertex> vit = graph[t].vertices.listIterator();
			while (vit.hasNext()) {
				final Vertex v = vit.next();
				if (v.branches.size() != 1) continue;
				final boolean prune = all ? isEndPoint(v.points.get(0)) : //
					v.branches.get(0).length <= length;
				if (!prune) continue;

				for (final P p : v.points) {
					setPixel(image, p, (byte) 0);
					setPixel(tagged, p, (byte) 0);
					numberOfEndPoints[t]--;
				}
				final Edge branch = v.branches.get(0);
				for (final P p : branch.slabs) {
					setPixel(image, p, (byte) 0);
					setPixel(tagged, p, (byte) 0);
					numberOfSlabs[t]--;
				}
				removeIdentity(graph[t].edges, branch);
				final Vertex opp = branch.opposite(v);
				if (opp != v) removeIdentity(opp.branches, branch);
				v.branches.clear();
				vit.remove();
				graph[t].vertexSet.remove(v);
			}
			// Note: The original plugin keeps a pruned root, from which the cycle
			// search then finds nothing; here a remaining vertex becomes the root.
			final Graph g = graph[t];
			if (g.root != null && !g.vertexSet.contains(g.root)) {
				g.root = null;
				for (final Vertex v : g.vertices) {
					if (!v.branches.isEmpty()) {
						g.root = v;
						break;
					}
				}
				if (g.root == null && !g.vertices.isEmpty()) g.root = g.vertices.get(0);
			}
		}
	}

	private static <E> void removeIdentity(final List<E> list, final E e) {
		final Iterator<E> it = list.iterator();
		while (it.hasNext()) {
			if (it.next() == e) {
				it.remove();
				return;
			}
		}
	}

	private boolean pruneCycles(final CyclePruning mode) {
		boolean pruned = false;
		for (int t = 0; t < numOfTrees; t++) {
			if (startingSlabTree.get(t).size() == 1) {
				// A ring without junctions: remove one voxel.
				setPixel(image, startingSlabTree.get(t).get(0), (byte) 0);
				pruned = true;
				continue;
			}
			final List<Edge> backEdges = graph[t].depthFirstSearch();
			if (backEdges.isEmpty()) continue;
			for (final Edge e : backEdges) {
				final List<Edge> loopEdges = new ArrayList<>();
				loopEdges.add(e);
				Edge minEdge = e;
				final boolean v1First = e.v1.visitOrder < e.v2.visitOrder;
				final Vertex finalLoopVertex = v1First ? e.v1 : e.v2;
				Vertex backtrackVertex = v1First ? e.v2 : e.v1;
				while (finalLoopVertex != backtrackVertex) {
					final Edge pre = backtrackVertex.predecessor;
					if (pre == null) break;
					if (mode == CyclePruning.SHORTEST_BRANCH && pre.slabs.size() < minEdge.slabs.size()) {
						minEdge = pre;
					}
					loopEdges.add(pre);
					backtrackVertex = pre.v1 == backtrackVertex ? pre.v2 : pre.v1;
				}
				switch (mode) {
					case SHORTEST_BRANCH:
						final P removeCoords = minEdge.slabs.isEmpty() ? //
							minEdge.v1.points.get(0) : //
							minEdge.slabs.get(minEdge.slabs.size() / 2);
						setPixel(image, removeCoords, (byte) 0);
						break;
					case LOWEST_INTENSITY_VOXEL:
						removeLowestIntensityVoxel(loopEdges);
						break;
					case LOWEST_INTENSITY_BRANCH:
						cutLowestIntensityBranch(loopEdges);
						break;
					default:
				}
			}
			pruned = true;
		}
		return pruned;
	}

	private void removeLowestIntensityVoxel(final List<Edge> loopEdges) {
		P lowestIntensityVoxel = null;
		double lowestIntensityValue = Double.MAX_VALUE;
		for (final Edge e : loopEdges) {
			for (final P p : e.slabs) {
				final double avg = averageNeighborhoodValue(p);
				if (avg < lowestIntensityValue) {
					lowestIntensityValue = avg;
					lowestIntensityVoxel = p;
				}
			}
		}
		if (lowestIntensityVoxel != null) {
			setPixel(image, lowestIntensityVoxel, (byte) 0);
		}
	}

	private void cutLowestIntensityBranch(final List<Edge> loopEdges) {
		Edge lowestIntensityEdge = null;
		double lowestIntensityValue = Double.MAX_VALUE;
		P cutPoint = null;
		for (final Edge e : loopEdges) {
			double minVal = Double.MAX_VALUE;
			P darkestPoint = null;
			double edgeIntensity = 0;
			double nVox = 0;
			for (final P p : e.slabs) {
				final double avg = averageNeighborhoodValue(p);
				if (avg < minVal) {
					minVal = avg;
					darkestPoint = p;
				}
				edgeIntensity += avg;
				nVox++;
			}
			for (final P p : e.v1.points) {
				edgeIntensity += averageNeighborhoodValue(p);
				nVox++;
			}
			for (final P p : e.v2.points) {
				edgeIntensity += averageNeighborhoodValue(p);
				nVox++;
			}
			if (nVox != 0) edgeIntensity /= nVox;
			if (edgeIntensity < lowestIntensityValue) {
				lowestIntensityEdge = e;
				lowestIntensityValue = edgeIntensity;
				cutPoint = darkestPoint;
			}
		}
		if (lowestIntensityEdge == null) return;
		final P removeCoords = lowestIntensityEdge.slabs.isEmpty() ? //
			lowestIntensityEdge.v1.points.get(0) : cutPoint;
		setPixel(image, removeCoords, (byte) 0);
	}

	private double averageNeighborhoodValue(final P p) {
		double sum = 0;
		int count = 0;
		for (int z = p.z - zOffset; z <= p.z + zOffset; z++) {
			for (int y = p.y - yOffset; y <= p.y + yOffset; y++) {
				for (int x = p.x - xOffset; x <= p.x + xOffset; x++) {
					if (inBounds(x, y, z)) sum += intensity[index(x, y, z)];
					count++;
				}
			}
		}
		return sum / count;
	}

	// -- Tagging and tree marking --

	private byte[] tagImage() {
		final byte[] out = new byte[image.length];
		for (int z = 0; z < depth; z++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (getPixel(image, x, y, z) == 0) continue;
					final int neighbors = numberOfNeighbors(x, y, z);
					final P p = new P(x, y, z);
					if (neighbors < 2) {
						out[index(x, y, z)] = END_POINT;
						listOfEndPoints.add(p);
					}
					else if (neighbors > 2) {
						out[index(x, y, z)] = JUNCTION;
						listOfJunctionVoxels.add(p);
					}
					else {
						out[index(x, y, z)] = SLAB;
						listOfSlabVoxels.add(p);
					}
				}
			}
		}
		return out;
	}

	private int numberOfNeighbors(final int x, final int y, final int z) {
		int n = 0;
		for (int dz = -1; dz <= 1; dz++)
			for (int dy = -1; dy <= 1; dy++)
				for (int dx = -1; dx <= 1; dx++)
					if (getPixel(image, x + dx, y + dy, z + dz) != 0) n++;
		return n - 1;
	}

	private int[] markTrees() {
		final int[] out = new int[image.length];
		numOfTrees = 0;
		int color = 0;

		for (final P p : listOfEndPoints) {
			if (isVisited(p)) continue;
			color++;
			visitTree(p, out, color);
			numOfTrees++;
		}
		for (final P p : listOfJunctionVoxels) {
			if (isVisited(p)) continue;
			color++;
			if (visitTree(p, out, color) == 0) {
				color--;
				continue;
			}
			numOfTrees++;
		}
		// Circular trees without junctions.
		for (final P p : listOfSlabVoxels) {
			if (isVisited(p)) continue;
			listOfStartingSlabVoxels.add(p);
			color++;
			if (visitTree(p, out, color) == 0) {
				color--;
				continue;
			}
			numOfTrees++;
		}
		resetVisited();
		return out;
	}

	private int visitTree(final P start, final int[] out, final int color) {
		int numOfVoxels = 0;
		if (isVisited(start)) return 0;
		out[index(start)] = color;
		setVisited(start);

		final Deque<P> toRevisit = new ArrayDeque<>();
		if (isJunction(start)) toRevisit.add(start);

		P nextPoint = nextUnvisitedVoxel(start);
		while (nextPoint != null || !toRevisit.isEmpty()) {
			if (nextPoint != null) {
				if (!isVisited(nextPoint)) {
					numOfVoxels++;
					out[index(nextPoint)] = color;
					setVisited(nextPoint);
					if (isJunction(nextPoint)) toRevisit.add(nextPoint);
					nextPoint = nextUnvisitedVoxel(nextPoint);
				}
			}
			else {
				nextPoint = nextUnvisitedVoxel(toRevisit.peekFirst());
				if (nextPoint == null) toRevisit.removeFirst();
			}
		}
		return numOfVoxels;
	}

	private void initializeTrees() {
		numberOfBranches = new int[numOfTrees];
		numberOfEndPoints = new int[numOfTrees];
		numberOfJunctionVoxels = new int[numOfTrees];
		numberOfJunctions = new int[numOfTrees];
		numberOfSlabs = new int[numOfTrees];
		numberOfTriplePoints = new int[numOfTrees];
		numberOfQuadruplePoints = new int[numOfTrees];
		averageBranchLength = new double[numOfTrees];
		maximumBranchLength = new double[numOfTrees];
		endPointsTree = new ArrayList<>();
		junctionVoxelTree = new ArrayList<>();
		startingSlabTree = new ArrayList<>();
		listOfSingleJunctions = new ArrayList<>();
		junctionVertexMap = new ArrayList<>();
		for (int i = 0; i < numOfTrees; i++) {
			endPointsTree.add(new ArrayList<>());
			junctionVoxelTree.add(new ArrayList<>());
			startingSlabTree.add(new ArrayList<>());
			listOfSingleJunctions.add(new ArrayList<>());
			junctionVertexMap.add(new HashMap<>());
		}
		graph = new Graph[numOfTrees];
		junctionVertex = new Vertex[numOfTrees][];
	}

	private void divideVoxelsByTrees() {
		for (final P p : listOfEndPoints)
			endPointsTree.get(labels[index(p)] - 1).add(p);
		for (final P p : listOfJunctionVoxels)
			junctionVoxelTree.get(labels[index(p)] - 1).add(p);
		for (final P p : listOfStartingSlabVoxels)
			startingSlabTree.get(labels[index(p)] - 1).add(p);
		for (int t = 0; t < numOfTrees; t++) {
			numberOfEndPoints[t] = endPointsTree.get(t).size();
			numberOfJunctionVoxels[t] = junctionVoxelTree.get(t).size();
		}
	}

	private void groupJunctions() {
		resetVisited();
		for (int t = 0; t < numOfTrees; t++) {
			for (final P p : junctionVoxelTree.get(t)) {
				if (!isVisited(p)) fusionNeighborJunction(p, listOfSingleJunctions.get(
					t));
			}
		}
		for (int t = 0; t < numOfTrees; t++) {
			final List<List<P>> groups = listOfSingleJunctions.get(t);
			numberOfJunctions[t] = groups.size();
			junctionVertex[t] = new Vertex[groups.size()];
			for (int j = 0; j < groups.size(); j++) {
				final Vertex v = new Vertex();
				for (final P p : groups.get(j)) {
					v.points.add(p);
					junctionVertexMap.get(t).put(p, v);
				}
				junctionVertex[t][j] = v;
			}
		}
		resetVisited();
	}

	private void fusionNeighborJunction(final P start,
		final List<List<P>> singleJunctions)
	{
		final List<P> group = new ArrayList<>();
		group.add(start);
		setVisited(start);
		final Deque<P> toRevisit = new ArrayDeque<>();
		toRevisit.add(start);
		P nextPoint = nextUnvisitedJunctionVoxel(start);
		while (nextPoint != null || !toRevisit.isEmpty()) {
			if (nextPoint != null && !isVisited(nextPoint)) {
				group.add(nextPoint);
				setVisited(nextPoint);
				toRevisit.add(nextPoint);
				nextPoint = nextUnvisitedJunctionVoxel(nextPoint);
			}
			else {
				nextPoint = nextUnvisitedJunctionVoxel(toRevisit.peekFirst());
				if (nextPoint == null) toRevisit.removeFirst();
			}
		}
		singleJunctions.add(group);
	}

	private void calculateTripleAndQuadruplePoints() {
		for (int t = 0; t < numOfTrees; t++) {
			for (final List<P> group : listOfSingleJunctions.get(t)) {
				int nBranch = 0;
				for (final P p : group) {
					for (int dz = -1; dz <= 1; dz++)
						for (int dy = -1; dy <= 1; dy++)
							for (int dx = -1; dx <= 1; dx++) {
								final byte tag = getPixel(tagged, p.x + dx, p.y + dy, p.z +
									dz);
								if (tag == SLAB || tag == END_POINT) nBranch++;
							}
				}
				if (nBranch == 3) numberOfTriplePoints[t]++;
				else if (nBranch == 4) numberOfQuadruplePoints[t]++;
			}
		}
	}

	// -- Graph construction --

	private void visitSkeleton(final int t) {
		final Graph g = graph[t] = new Graph();
		for (final Vertex v : junctionVertex[t])
			g.addVertex(v);

		double branchLength = 0;
		maximumBranchLength[t] = 0;
		numberOfSlabs[t] = 0;

		// Visit branches starting at end points.
		final List<P> endPoints = endPointsTree.get(t);
		for (int i = 0; i < numberOfEndPoints[t]; i++) {
			final P endPoint = endPoints.get(i);
			if (isVisited(endPoint)) continue;

			final Vertex v1 = new Vertex();
			v1.points.add(endPoint);
			g.addVertex(v1);
			if (i == 0) g.root = v1;

			slabList = new ArrayList<>();
			final double[] properties = visitBranch(endPoint, t);
			double length = properties[0];

			if (length == 0) {
				// A single voxel tree, or an end point next to a junction.
				final P aux = visitedJunctionNeighbor(endPoint, v1);
				if (aux != null) {
					auxFinalVertex = findPointVertex(t, aux);
					length += distance(endPoint, aux);
					length += distance(auxFinalVertex.points.get(0), endPoint);
					g.addVertex(auxFinalVertex);
					g.addEdge(new Edge(v1, auxFinalVertex, slabList, length,
						properties[1], properties[2], properties[3]));
					numberOfBranches[t]++;
					branchLength += length;
				}
				continue;
			}

			if (isSlab(auxPoint)) {
				// Connect to the neighbor junction not belonging to the initial
				// vertex, unless it is a self loop.
				final P aux = auxPoint;
				auxPoint = visitedJunctionNeighbor(auxPoint, v1);
				auxFinalVertex = findPointVertex(t, auxPoint);
				if (auxPoint == null) {
					auxFinalVertex = v1;
					auxPoint = aux;
				}
				length += distance(auxPoint, aux);
				length += distance(auxFinalVertex.points.get(0), auxPoint);
			}

			g.addVertex(auxFinalVertex);
			g.addEdge(new Edge(v1, auxFinalVertex, slabList, length, properties[1],
				properties[2], properties[3]));
			numberOfBranches[t]++;
			branchLength += length;
			if (length > maximumBranchLength[t]) maximumBranchLength[t] = length;
		}

		if (numberOfEndPoints[t] == 0 && !junctionVoxelTree.get(t).isEmpty()) {
			g.root = junctionVertex[t][0];
		}

		// Visit branches starting at junctions.
		for (final Vertex jv : junctionVertex[t]) {
			for (final P junctionCoord : jv.points) {
				setVisited(junctionCoord);
				P nextPoint = nextUnvisitedVoxel(junctionCoord);
				while (nextPoint != null) {
					if (!isJunction(nextPoint)) {
						slabList = new ArrayList<>();
						slabList.add(nextPoint);
						numberOfSlabs[t]++;

						double length = distance(junctionCoord, nextPoint);
						auxPoint = null;
						final double[] properties = visitBranch(nextPoint, t);
						length += properties[0];

						if (length != 0) {
							if (auxPoint == null) auxPoint = nextPoint;
							numberOfBranches[t]++;

							final Vertex initialVertex = findPointVertex(t, junctionCoord);

							if (isSlab(auxPoint)) {
								final P aux = auxPoint;
								auxPoint = visitedJunctionNeighbor(auxPoint, initialVertex);
								auxFinalVertex = findPointVertex(t, auxPoint);
								if (auxPoint == null) {
									auxFinalVertex = initialVertex;
									auxPoint = aux;
								}
								length += distance(auxPoint, aux);
							}

							if (length > maximumBranchLength[t]) {
								maximumBranchLength[t] = length;
							}

							// Measure from the main voxel of the initial junction.
							length += distance(initialVertex.points.get(0), junctionCoord);

							g.addEdge(new Edge(initialVertex, auxFinalVertex, slabList,
								length, properties[1], properties[2], properties[3]));
							branchLength += length;
							if (length > maximumBranchLength[t]) {
								maximumBranchLength[t] = length;
							}
						}
					}
					else setVisited(nextPoint);

					nextPoint = nextUnvisitedVoxel(junctionCoord);
				}
			}
		}

		// Visit circular trees starting at a slab.
		if (startingSlabTree.get(t).size() == 1) {
			final P startCoord = startingSlabTree.get(t).get(0);
			final Vertex v1 = new Vertex();
			v1.points.add(startCoord);
			g.addVertex(v1);

			slabList = new ArrayList<>();
			slabList.add(startCoord);
			numberOfSlabs[t]++;

			final double[] properties = visitBranch(startCoord, t);
			final double length = properties[0];
			if (length != 0) {
				numberOfBranches[t]++;
				branchLength += length;
				if (length > maximumBranchLength[t]) maximumBranchLength[t] = length;
			}
			g.addEdge(new Edge(v1, v1, slabList, length, properties[1],
				properties[2], properties[3]));
		}

		if (numberOfBranches[t] == 0) return;
		averageBranchLength[t] = branchLength / numberOfBranches[t];
	}

	/**
	 * Visits a branch until reaching an end point or junction, and returns its
	 * length, average intensity of the inner third, average intensity and
	 * running average length.
	 */
	private double[] visitBranch(final P start, final int t) {
		double length = 0;
		double lengthRa = 0;
		final double noIntensity = intensity == null ? Double.NaN : 0;
		final double[] ret = { 0, noIntensity, noIntensity, 0 };

		final List<P> pointHistory = new ArrayList<>();
		pointHistory.add(start);
		setVisited(start);

		P nextPoint = nextUnvisitedVoxel(start);
		if (nextPoint == null) return ret;

		P previousPoint = start;
		while (nextPoint != null && isSlab(nextPoint)) {
			numberOfSlabs[t]++;
			slabList.add(nextPoint);
			length += distance(previousPoint, nextPoint);
			pointHistory.add(0, nextPoint);
			lengthRa += runningAverageDistance(pointHistory);
			setVisited(nextPoint);
			previousPoint = nextPoint;
			nextPoint = nextUnvisitedVoxel(previousPoint);
		}

		if (nextPoint != null) {
			length += distance(previousPoint, nextPoint);
			pointHistory.add(0, nextPoint);
			lengthRa += runningAverageDistance(pointHistory);
			setVisited(nextPoint);

			if (isEndPoint(nextPoint)) {
				auxFinalVertex = new Vertex();
				auxFinalVertex.points.add(nextPoint);
			}
			else if (isJunction(nextPoint)) {
				auxFinalVertex = findPointVertex(t, nextPoint);
				final double d = distance(auxFinalVertex.points.get(0), nextPoint);
				length += d;
				lengthRa += d;
			}
			auxPoint = nextPoint;
		}
		else auxPoint = previousPoint;

		ret[0] = length;
		ret[3] = lengthRa;
		if (intensity != null) {
			// Average intensity of the whole branch, and of its inner third (which
			// is less affected by the junctions at both ends).
			final int size = pointHistory.size();
			final int start3rd = (int) (size / 3.0);
			final int end3rd = (int) (2 * size / 3.0);
			double sum = 0, sum3rd = 0;
			for (int i = 0; i < size; i++) {
				final double value = intensity[index(pointHistory.get(i))];
				sum += value;
				if (i >= start3rd && i < end3rd) sum3rd += value;
			}
			ret[1] = sum3rd / (end3rd - start3rd);
			ret[2] = sum / size;
		}
		return ret;
	}

	private Vertex findPointVertex(final int t, final P p) {
		return p == null ? null : junctionVertexMap.get(t).get(p);
	}

	private double distance(final P p1, final P p2) {
		final double dx = (p1.x - p2.x) * pw;
		final double dy = (p1.y - p2.y) * ph;
		final double dz = (p1.z - p2.z) * pd;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Linearly corrected distance between the two most recent points (index 0
	 * is the most recent), using up to the five last points.
	 */
	private double runningAverageDistance(final List<P> points) {
		final int indexOfLast = points.size() - 1;
		if (indexOfLast < 1) return 0;
		final int poi = Math.min(5, indexOfLast);
		return distance(points.get(poi), points.get(0)) / poi;
	}

	// -- Longest shortest path --

	private double warshallAlgorithm(final Graph g, final List<P> path) {
		final List<Edge> edgeList = g.edges;
		final List<Vertex> vertexList = g.vertices;
		final int nv = vertexList.size();
		if (nv == 0) return 0;
		if (nv == 1) {
			path.add(vertexList.get(0).points.get(0));
			return 0;
		}

		final Map<Vertex, Integer> indices = new IdentityHashMap<>();
		for (int i = 0; i < nv; i++)
			indices.putIfAbsent(vertexList.get(i), i);

		final double[][] adjacency = new double[nv][nv];
		final int[][] predecessor = new int[nv][nv];
		for (int i = 0; i < nv; i++) {
			Arrays.fill(adjacency[i], Double.POSITIVE_INFINITY);
			Arrays.fill(predecessor[i], -1);
		}
		for (final Edge edge : edgeList) {
			final Integer row = indices.get(edge.v1);
			final Integer column = indices.get(edge.v2);
			if (row == null || column == null) continue;
			adjacency[row][row] = 0;
			adjacency[column][column] = 0;
			adjacency[row][column] = edge.length;
			adjacency[column][row] = edge.length;
			predecessor[row][row] = -1;
			predecessor[column][column] = -1;
			predecessor[row][column] = row;
			predecessor[column][row] = column;
		}

		for (int k = 0; k < nv; k++) {
			for (int i = 0; i < nv; i++) {
				for (int j = 0; j < nv; j++) {
					if (adjacency[i][k] + adjacency[k][j] < adjacency[i][j]) {
						adjacency[i][j] = adjacency[i][k] + adjacency[k][j];
						predecessor[i][j] = predecessor[k][j];
					}
				}
			}
		}

		double maxPath = 0;
		int a = 0, b = 0;
		for (int i = 0; i < nv; i++) {
			for (int j = 0; j < nv; j++) {
				if (adjacency[i][j] > maxPath &&
					adjacency[i][j] != Double.POSITIVE_INFINITY)
				{
					maxPath = adjacency[i][j];
					a = i;
					b = j;
				}
			}
		}

		reconstructPath(predecessor, a, b, edgeList, vertexList, path);
		return maxPath;
	}

	private static void reconstructPath(final int[][] predecessorMatrix,
		final int a, int b, final List<Edge> edgeList, final List<Vertex> vertexList,
		final List<P> path)
	{
		while (b != a) {
			final Vertex predecessor = vertexList.get(predecessorMatrix[a][b]);
			final Vertex endVertex = vertexList.get(b);
			// There may be several edges between two vertices: take the shortest.
			Edge shortestEdge = null;
			double shortestLength = Double.POSITIVE_INFINITY;
			for (final Edge edge : edgeList) {
				if ((edge.v1 == predecessor && edge.v2 == endVertex) ||
					(edge.v1 == endVertex && edge.v2 == predecessor))
				{
					if (edge.length < shortestLength) {
						shortestEdge = edge;
						shortestLength = edge.length;
					}
				}
			}
			final boolean reverse = shortestEdge.v2 != predecessor;
			final Vertex first = reverse ? shortestEdge.v2 : shortestEdge.v1;
			final Vertex second = reverse ? shortestEdge.v1 : shortestEdge.v2;
			for (final P p : first.points)
				if (!path.contains(p)) path.add(p);
			final List<P> slabs = new ArrayList<>(shortestEdge.slabs);
			if (reverse) Collections.reverse(slabs);
			path.addAll(slabs);
			for (final P p : second.points)
				if (!path.contains(p)) path.add(p);
			b = predecessorMatrix[a][b];
		}
	}

	// -- Conversion to the public model --

	private SkeletonGraph toSkeletonGraph(final Graph g,
		final Map<Vertex, SkeletonVertex> vertexMap)
	{
		final List<SkeletonVertex> vertices = new ArrayList<>(g.vertices.size());
		for (final Vertex v : g.vertices)
			vertices.add(toSkeletonVertex(v, vertexMap));
		final List<SkeletonEdge> edges = new ArrayList<>(g.edges.size());
		for (final Edge e : g.edges) {
			final List<Localizable> slabs = new ArrayList<>(e.slabs.size());
			for (final P p : e.slabs)
				slabs.add(toPoint(p));
			edges.add(new SkeletonEdge(toSkeletonVertex(e.v1, vertexMap),
				toSkeletonVertex(e.v2, vertexMap), slabs, e.length, e.lengthRa,
				distance(e.v1.points.get(0), e.v2.points.get(0)), e.color,
				e.color3rd));
		}
		final SkeletonVertex root = g.root == null ? null : toSkeletonVertex(
			g.root, vertexMap);
		return new SkeletonGraph(vertices, edges, root);
	}

	private SkeletonVertex toSkeletonVertex(final Vertex v,
		final Map<Vertex, SkeletonVertex> vertexMap)
	{
		return vertexMap.computeIfAbsent(v, key -> {
			final List<Localizable> points = new ArrayList<>(key.points.size());
			for (final P p : key.points)
				points.add(toPoint(p));
			return new SkeletonVertex(points);
		});
	}

	private Localizable toPoint(final P p) {
		final long[] pos = new long[numDims];
		pos[0] = p.x + min[0];
		pos[1] = p.y + min[1];
		if (numDims == 3) pos[2] = p.z + min[2];
		return Point.wrap(pos);
	}

	// -- Voxel helpers --

	private interface VoxelTest {

		boolean test(int x, int y, int z);
	}

	/**
	 * Searches the 26-neighborhood of a point. Note: Like the original plugin,
	 * whose loop exits only the innermost (z) loop on a match, this returns the
	 * first match along z of the last (x, y) column with a match; the visit
	 * order of the skeleton, and hence its graph, depends on it.
	 */
	private static P findNeighbor(final P p, final VoxelTest test) {
		P result = null;
		for (int x = -1; x < 2; x++)
			for (int y = -1; y < 2; y++)
				for (int z = -1; z < 2; z++) {
					if (x == 0 && y == 0 && z == 0) continue;
					if (test.test(p.x + x, p.y + y, p.z + z)) {
						result = new P(p.x + x, p.y + y, p.z + z);
						break;
					}
				}
		return result;
	}

	private P nextUnvisitedVoxel(final P p) {
		return findNeighbor(p, (x, y, z) -> getPixel(image, x, y, z) != 0 &&
			!isVisited(x, y, z));
	}

	private P nextUnvisitedJunctionVoxel(final P p) {
		return findNeighbor(p, (x, y, z) -> getPixel(image, x, y, z) != 0 &&
			!isVisited(x, y, z) && getPixel(tagged, x, y, z) == JUNCTION);
	}

	private P visitedJunctionNeighbor(final P p, final Vertex exclude) {
		return findNeighbor(p, (x, y, z) -> getPixel(image, x, y, z) != 0 &&
			isVisited(x, y, z) && getPixel(tagged, x, y, z) == JUNCTION && !exclude
				.points.contains(new P(x, y, z)));
	}

	private boolean isSlab(final P p) {
		return getPixel(tagged, p.x, p.y, p.z) == SLAB;
	}

	private boolean isJunction(final P p) {
		return getPixel(tagged, p.x, p.y, p.z) == JUNCTION;
	}

	private boolean isEndPoint(final P p) {
		return getPixel(tagged, p.x, p.y, p.z) == END_POINT;
	}

	private void resetVisited() {
		visited = new boolean[image.length];
	}

	private boolean isVisited(final P p) {
		return isVisited(p.x, p.y, p.z);
	}

	/** Out-of-image voxels count as visited. */
	private boolean isVisited(final int x, final int y, final int z) {
		return !inBounds(x, y, z) || visited[index(x, y, z)];
	}

	private void setVisited(final P p) {
		if (inBounds(p.x, p.y, p.z)) visited[index(p)] = true;
	}

	private boolean inBounds(final int x, final int y, final int z) {
		return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
	}

	private int index(final int x, final int y, final int z) {
		return x + width * (y + height * z);
	}

	private int index(final P p) {
		return index(p.x, p.y, p.z);
	}

	private byte getPixel(final byte[] img, final int x, final int y,
		final int z)
	{
		return inBounds(x, y, z) ? img[index(x, y, z)] : 0;
	}

	private void setPixel(final byte[] img, final P p, final byte value) {
		if (inBounds(p.x, p.y, p.z)) img[index(p)] = value;
	}

	// -- Helper classes --

	private static final class P {

		final int x, y, z;

		P(final int x, final int y, final int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof P)) return false;
			final P p = (P) o;
			return x == p.x && y == p.y && z == p.z;
		}

		@Override
		public int hashCode() {
			return (x * 31 + y) * 31 + z;
		}
	}

	private static final class Vertex {

		final List<P> points = new ArrayList<>();
		final List<Edge> branches = new ArrayList<>();
		boolean visited;
		Edge predecessor;
		int visitOrder = -1;
	}

	private static final class Edge {

		final Vertex v1, v2;
		final List<P> slabs;
		final double length, color3rd, color, lengthRa;
		int type = UNDEFINED;

		Edge(final Vertex v1, final Vertex v2, final List<P> slabs,
			final double length, final double color3rd, final double color,
			final double lengthRa)
		{
			this.v1 = v1;
			this.v2 = v2;
			this.slabs = slabs;
			this.length = length;
			this.color3rd = color3rd;
			this.color = color;
			this.lengthRa = lengthRa;
		}

		Vertex opposite(final Vertex v) {
			if (v1 == v) return v2;
			if (v2 == v) return v1;
			return null;
		}
	}

	private static final class Graph {

		final List<Edge> edges = new ArrayList<>();
		final List<Vertex> vertices = new ArrayList<>();
		final Set<Vertex> vertexSet = Collections.newSetFromMap(
			new IdentityHashMap<>());
		Vertex root;

		void addEdge(final Edge e) {
			e.v1.branches.add(e);
			if (e.v1 != e.v2) e.v2.branches.add(e);
			edges.add(e);
		}

		void addVertex(final Vertex v) {
			if (vertexSet.add(v)) vertices.add(v);
		}

		/** Depth-first search from the root, returning the back edges. */
		List<Edge> depthFirstSearch() {
			final List<Edge> backEdges = new ArrayList<>();
			if (root == null) return backEdges;
			for (final Vertex v : vertices)
				v.visited = false;
			final Deque<Vertex> stack = new ArrayDeque<>();
			stack.push(root);
			int visitOrder = 0;
			while (!stack.isEmpty()) {
				final Vertex u = stack.pop();
				if (u.visited) continue;
				if (u.predecessor != null) u.predecessor.type = TREE;
				u.visited = true;
				u.visitOrder = visitOrder++;
				for (final Edge e : u.branches) {
					if (e.type != UNDEFINED) continue;
					final Vertex ov = e.opposite(u);
					if (!ov.visited) {
						stack.push(ov);
						ov.predecessor = e;
					}
					else {
						e.type = BACK;
						backEdges.add(e);
					}
				}
			}
			return backEdges;
		}
	}
}
