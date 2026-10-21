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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.gpl.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link TagSkeleton}, {@link DefaultSkeletonGraph} and
 * {@link DefaultSkeletonStatistics} on synthetic skeletons with known
 * structure.
 *
 * @author Curtis Rueden
 */
public class SkeletonAnalysisTest extends AbstractOpTest {

	private static final double EPS = 1e-12;

	private static Img<BitType> image2D(final int[][] points) {
		final Img<BitType> img = ArrayImgs.bits(20, 20);
		final RandomAccess<BitType> ra = img.randomAccess();
		for (final int[] p : points)
			ra.setPositionAndGet(p).set(true);
		return img;
	}

	private static int[][] line2D() {
		final int[][] points = new int[11][];
		for (int i = 0; i <= 10; i++)
			points[i] = new int[] { 2 + i, 2 };
		return points;
	}

	/** A plus with arms of length 5 around (10, 10). */
	private static int[][] cross2D() {
		final int[][] points = new int[21][];
		int k = 0;
		for (int i = 5; i <= 15; i++)
			points[k++] = new int[] { i, 10 };
		for (int i = 5; i <= 15; i++)
			if (i != 10) points[k++] = new int[] { 10, i };
		return points;
	}

	/** A diamond-shaped ring of 16 pixels. */
	private static int[][] ring2D() {
		final int[][] points = new int[16][];
		for (int i = 0; i < 4; i++) {
			points[i] = new int[] { 5 + i, 5 + i };
			points[4 + i] = new int[] { 9 - i, 9 + i };
			points[8 + i] = new int[] { 5 - i, 13 - i };
			points[12 + i] = new int[] { 1 + i, 9 - i };
		}
		return points;
	}

	private List<SkeletonStatistics> statistics(
		final RandomAccessibleInterval<BitType> img)
	{
		return ops.op("topology.skeletonStatistics").input(img).outType(
			new Nil<List<SkeletonStatistics>>()
			{}).apply();
	}

	private List<SkeletonGraph> graphs(
		final RandomAccessibleInterval<BitType> img, final double[] calibration,
		final RandomAccessibleInterval<DoubleType> intensity)
	{
		return ops.op("topology.skeletonGraph").input(img, calibration, null, null,
			intensity).outType(new Nil<List<SkeletonGraph>>()
			{}).apply();
	}

	private RandomAccessibleInterval<UnsignedByteType> tag(
		final RandomAccessibleInterval<BitType> img)
	{
		return ops.op("morphology.tagSkeleton").input(img).outType(
			new Nil<RandomAccessibleInterval<UnsignedByteType>>()
			{}).apply();
	}

	@Test
	public void testTagLine() {
		final RandomAccess<UnsignedByteType> ra = tag(image2D(line2D()))
			.randomAccess();
		assertEquals(TagSkeleton.END_POINT, ra.setPositionAndGet(2, 2).get());
		assertEquals(TagSkeleton.END_POINT, ra.setPositionAndGet(12, 2).get());
		for (int x = 3; x < 12; x++)
			assertEquals(TagSkeleton.SLAB, ra.setPositionAndGet(x, 2).get());
		assertEquals(0, ra.setPositionAndGet(2, 3).get());
	}

	@Test
	public void testTagCross() {
		final RandomAccess<UnsignedByteType> ra = tag(image2D(cross2D()))
			.randomAccess();
		// The center and its four direct neighbors each touch three or more
		// foreground pixels.
		assertEquals(TagSkeleton.JUNCTION, ra.setPositionAndGet(10, 10).get());
		assertEquals(TagSkeleton.JUNCTION, ra.setPositionAndGet(11, 10).get());
		assertEquals(TagSkeleton.JUNCTION, ra.setPositionAndGet(10, 9).get());
		assertEquals(TagSkeleton.SLAB, ra.setPositionAndGet(12, 10).get());
		assertEquals(TagSkeleton.END_POINT, ra.setPositionAndGet(10, 15).get());
	}

	@Test
	public void testTagND() {
		final Img<BitType> img = ArrayImgs.bits(4, 4, 4, 4);
		final RandomAccess<BitType> in = img.randomAccess();
		for (int i = 0; i < 4; i++)
			in.setPositionAndGet(i, i, i, i).set(true);
		in.setPositionAndGet(0, 3, 0, 3).set(true);
		final RandomAccess<UnsignedByteType> ra = tag(img).randomAccess();
		assertEquals(TagSkeleton.END_POINT, ra.setPositionAndGet(0, 0, 0, 0)
			.get());
		assertEquals(TagSkeleton.SLAB, ra.setPositionAndGet(1, 1, 1, 1).get());
		assertEquals(TagSkeleton.END_POINT, ra.setPositionAndGet(3, 3, 3, 3)
			.get());
		assertEquals(TagSkeleton.END_POINT, ra.setPositionAndGet(0, 3, 0, 3)
			.get());
	}

	@Test
	public void testLine() {
		final List<SkeletonStatistics> stats = statistics(image2D(line2D()));
		assertEquals(1, stats.size());
		final SkeletonStatistics s = stats.get(0);
		assertEquals(1, s.branches());
		assertEquals(0, s.junctions());
		assertEquals(2, s.endPointVoxels());
		assertEquals(9, s.slabVoxels());
		assertEquals(10, s.averageBranchLength(), EPS);
		assertEquals(10, s.maximumBranchLength(), EPS);
		assertTrue(Double.isNaN(s.longestShortestPath()));
		assertTrue(s.longestShortestPathPoints().isEmpty());

		final SkeletonGraph g = s.graph();
		assertEquals(2, g.vertices().size());
		assertEquals(1, g.edges().size());
		final SkeletonEdge e = g.edges().get(0);
		assertEquals(9, e.slabs().size());
		assertEquals(10, e.euclideanDistance(), EPS);
		assertTrue(Double.isNaN(e.averageIntensity()));
		assertSame(g.root(), e.v1());
		assertSame(e.v2(), e.opposite(e.v1()));
		assertEquals(1, g.edgesOf(e.v2()).size());
		final Localizable p = e.v1().points().get(0);
		assertEquals(2, p.numDimensions());
		assertEquals(2, p.getLongPosition(0));
	}

	@Test
	public void testLongestShortestPath() {
		final List<SkeletonStatistics> stats = ops.op(
			"topology.skeletonStatistics").input(image2D(cross2D()), null, null,
				null, null, true).outType(new Nil<List<SkeletonStatistics>>()
				{}).apply();
		final SkeletonStatistics s = stats.get(0);
		assertEquals(4, s.branches());
		assertEquals(1, s.junctions());
		assertEquals(5, s.junctionVoxels());
		assertEquals(4, s.endPointVoxels());
		assertEquals(1, s.quadruplePoints());
		assertEquals(0, s.triplePoints());
		// Branch lengths are measured to the first voxel of the junction cluster
		// (10, 9), so the arms measure 4, 4 + sqrt(2), 4 + sqrt(2) and 6, as in
		// the original plugin.
		assertEquals(6, s.maximumBranchLength(), EPS);
		assertEquals(10 + Math.sqrt(2), s.longestShortestPath(), EPS);
		assertEquals(5, s.graph().vertices().size());
		assertEquals(4, s.graph().edgesOf(s.graph().edges().get(0).v2()).size());
		final List<Localizable> path = s.longestShortestPathPoints();
		assertTrue(path.size() >= 11);
	}

	@Test
	public void testCalibrationAndTranslation() {
		final RandomAccessibleInterval<BitType> img = Views.translate(image2D(
			line2D()), 100, 200);
		final List<SkeletonGraph> g = graphs(img, new double[] { 0.5, 2 }, null);
		final SkeletonEdge e = g.get(0).edges().get(0);
		assertEquals(5, e.length(), EPS);
		assertEquals(5, e.euclideanDistance(), EPS);
		final Localizable p = e.v1().points().get(0);
		assertEquals(102, p.getLongPosition(0));
		assertEquals(202, p.getLongPosition(1));
		assertEquals(103, e.slabs().get(0).getLongPosition(0));
	}

	@Test
	public void testIntensity() {
		final Img<DoubleType> intensity = ArrayImgs.doubles(20, 20);
		for (final DoubleType t : intensity)
			t.set(7);
		final SkeletonEdge e = graphs(image2D(line2D()), null, intensity).get(0)
			.edges().get(0);
		assertEquals(7, e.averageIntensity(), EPS);
		assertEquals(7, e.averageIntensityInnerThird(), EPS);
	}

	@Test
	public void testRing() {
		final List<SkeletonStatistics> stats = statistics(image2D(ring2D()));
		assertEquals(1, stats.size());
		final SkeletonStatistics s = stats.get(0);
		assertEquals(1, s.branches());
		assertEquals(0, s.junctions());
		assertEquals(0, s.endPointVoxels());
		assertEquals(16, s.slabVoxels());
		// The closing step back to the start voxel is not counted.
		assertEquals(15 * Math.sqrt(2), s.averageBranchLength(), EPS);
		final SkeletonGraph g = s.graph();
		assertNull(g.root());
		assertEquals(1, g.vertices().size());
		final SkeletonEdge e = g.edges().get(0);
		assertSame(e.v1(), e.v2());
		assertEquals(1, g.edgesOf(e.v1()).size());

		// Cycle pruning breaks the ring into a line.
		final List<SkeletonStatistics> pruned = ops.op(
			"topology.skeletonStatistics").input(image2D(ring2D()), null,
				CyclePruning.SHORTEST_BRANCH).outType(
					new Nil<List<SkeletonStatistics>>()
					{}).apply();
		assertEquals(2, pruned.get(0).endPointVoxels());
		assertEquals(1, pruned.get(0).branches());
	}

	@Test
	public void testSeveralTrees() {
		final int[][] line = line2D();
		final int[][] points = new int[line.length + 1][];
		System.arraycopy(line, 0, points, 0, line.length);
		points[line.length] = new int[] { 15, 15 };
		final List<SkeletonStatistics> stats = statistics(image2D(points));
		assertEquals(2, stats.size());
		final SkeletonStatistics single = stats.get(1);
		assertEquals(0, single.branches());
		assertEquals(1, single.endPointVoxels());
		assertEquals(1, single.graph().vertices().size());
		assertEquals(0, single.graph().edges().size());
		assertTrue(statistics(ArrayImgs.bits(5, 5)).isEmpty());
	}

	@Test
	public void testTripod3D() {
		final Img<BitType> img = SkeletonRegressionTest.draw(12, 12, 12,
			new int[][] { { 5, 5, 5, 10, 5, 5 }, { 5, 5, 5, 5, 0, 5 }, { 5, 5, 5, 5,
				5, 11 } });
		final SkeletonStatistics s = statistics(img).get(0);
		assertEquals(3, s.branches());
		assertEquals(1, s.junctions());
		assertEquals(3, s.endPointVoxels());
		assertEquals(1, s.triplePoints());
		assertEquals(3, s.graph().edgesOf(s.graph().edges().get(0).v2()).size());
		// The arms have 5, 5 and 6 steps from the center; branches are measured
		// along voxels from the first voxel of the junction cluster.
		final double total = s.averageBranchLength() * s.branches();
		assertEquals(16, total, 0.5);
	}

	@Test
	public void testInvalidArguments() {
		assertThrows(RuntimeException.class, () -> ops.op(
			"topology.skeletonGraph").input(image2D(ring2D()), null,
				CyclePruning.LOWEST_INTENSITY_VOXEL).outType(
					new Nil<List<SkeletonGraph>>()
					{}).apply());
		assertThrows(RuntimeException.class, () -> graphs(image2D(line2D()),
			new double[] { 1, 1, 1 }, null));
		assertThrows(RuntimeException.class, () -> statistics(ArrayImgs.bits(2, 2,
			2, 2)));
	}
}
