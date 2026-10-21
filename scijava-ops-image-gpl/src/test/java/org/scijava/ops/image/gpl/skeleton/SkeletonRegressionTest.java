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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.gpl.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Regression tests of {@code topology.skeletonStatistics} and
 * {@code topology.skeletonGraph} against the original Fiji AnalyzeSkeleton
 * plugin.
 * <p>
 * The expected values were produced by running the original plugin (source
 * of version 3.4.3-SNAPSHOT, with ImageJ 1.54p) from a throwaway harness on
 * the same synthetic skeleton, drawn into an 8-bit stack with the same
 * {@link #draw} code, and printing its SkeletonResult and graphs in the
 * format of {@link #describe}. The skeleton holds a straight line, a 3D
 * tripod, a 2D cross, a square loop with two tails and a diamond ring.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SkeletonRegressionTest extends AbstractOpTest {

	static final int[][] SEGMENTS = { //
		{ 2, 2, 1, 12, 2, 1 }, //
		{ 20, 6, 1, 30, 6, 1 }, { 25, 1, 1, 25, 11, 1 }, //
		{ 5, 15, 1, 9, 19, 1 }, { 9, 19, 1, 5, 23, 1 }, { 5, 23, 1, 1, 19, 1 }, {
			1, 19, 1, 5, 15, 1 }, //
		{ 20, 20, 4, 14, 14, 1 }, { 20, 20, 4, 27, 20, 7 }, { 20, 20, 4, 20, 27,
			4 }, //
		{ 30, 14, 1, 36, 14, 1 }, { 36, 14, 1, 36, 20, 1 }, { 36, 20, 1, 30, 20,
			1 }, { 30, 20, 1, 30, 14, 1 }, { 36, 17, 1, 39, 17, 1 }, { 30, 17, 1,
				27, 17, 1 } };

	static final String[][] EXPECTED = {
		// A none
		{ "tree 0: branches=1 junctions=0 endPoints=2 junctionVoxels=0 slabs=9 avg=10.0 triples=0 quads=0 max=10.0 lsp=10.0", //
			"  vertices=2 edges=1", //
			"  edge (2, 2, 1) (12, 2, 1) len=10.0 ra=10.0 slabs=9", //
			"tree 1: branches=3 junctions=1 endPoints=3 junctionVoxels=3 slabs=15 avg=8.227144598981733 triples=1 quads=0 max=9.438793109825918 lsp=17.6814337969452", //
			"  vertices=4 edges=3", //
			"  edge (14, 14, 1) (20, 20, 4) len=9.438793109825918 ra=9.301445722753648 slabs=5", //
			"  edge (20, 27, 4) (20, 20, 4) len=7.0 ra=7.0 slabs=5", //
			"  edge (27, 20, 7) (20, 20, 4) len=8.242640687119284 ra=7.53338387128521 slabs=5", //
			"tree 2: branches=4 junctions=1 endPoints=4 junctionVoxels=5 slabs=12 avg=5.207106781186548 triples=0 quads=1 max=6.0 lsp=11.414213562373096", //
			"  vertices=5 edges=4", //
			"  edge (20, 6, 1) (24, 6, 1) len=4.0 ra=4.0 slabs=3", //
			"  edge (25, 1, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (25, 11, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (30, 6, 1) (24, 6, 1) len=6.0 ra=6.0 slabs=3", //
			"tree 3: branches=8 junctions=2 endPoints=2 junctionVoxels=16 slabs=12 avg=5.298476161873042 triples=0 quads=0 max=13.60555127546399 lsp=15.21110255092798", //
			"  vertices=4 edges=8", //
			"  edge (27, 17, 1) (29, 17, 1) len=2.0 ra=2.0 slabs=1", //
			"  edge (39, 17, 1) (35, 14, 1) len=5.60555127546399 ra=5.60555127546399 slabs=1", //
			"  edge (29, 17, 1) (29, 17, 1) len=3.23606797749979 ra=0.0 slabs=1", //
			"  edge (29, 17, 1) (35, 14, 1) len=13.60555127546399 ra=9.0 slabs=3", //
			"  edge (29, 17, 1) (29, 17, 1) len=3.23606797749979 ra=0.0 slabs=1", //
			"  edge (29, 17, 1) (35, 14, 1) len=7.60555127546399 ra=3.0 slabs=3", //
			"  edge (35, 14, 1) (35, 14, 1) len=1.0 ra=0.0 slabs=1", //
			"  edge (35, 14, 1) (35, 14, 1) len=6.0990195135927845 ra=0.0 slabs=1", //
			"tree 4: branches=1 junctions=0 endPoints=0 junctionVoxels=0 slabs=16 avg=21.213203435596434 triples=0 quads=0 max=21.213203435596434 lsp=0.0", //
			"  vertices=1 edges=1", //
			"  edge (1, 19, 1) (1, 19, 1) len=21.213203435596434 ra=17.606629560649026 slabs=16" },
		// A calibrated
		{ "tree 0: branches=1 junctions=0 endPoints=2 junctionVoxels=0 slabs=9 avg=5.0 triples=0 quads=0 max=5.0 lsp=5.0", //
			"  vertices=2 edges=1", //
			"  edge (2, 2, 1) (12, 2, 1) len=5.0 ra=5.0 slabs=9", //
			"tree 1: branches=3 junctions=1 endPoints=3 junctionVoxels=3 slabs=15 avg=6.723313270888354 triples=1 quads=0 max=8.48528137423857 lsp=16.66993981266506", //
			"  vertices=4 edges=3", //
			"  edge (14, 14, 1) (20, 20, 4) len=8.48528137423857 ra=8.5405875956206 slabs=5", //
			"  edge (20, 27, 4) (20, 20, 4) len=3.5 ra=3.5 slabs=5", //
			"  edge (27, 20, 7) (20, 20, 4) len=8.18465843842649 ra=6.312799424038784 slabs=5", //
			"tree 2: branches=4 junctions=1 endPoints=4 junctionVoxels=5 slabs=12 avg=2.603553390593274 triples=0 quads=1 max=3.0 lsp=5.707106781186548", //
			"  vertices=5 edges=4", //
			"  edge (20, 6, 1) (24, 6, 1) len=2.0 ra=2.0 slabs=3", //
			"  edge (25, 1, 1) (24, 6, 1) len=2.7071067811865475 ra=2.7071067811865475 slabs=3", //
			"  edge (25, 11, 1) (24, 6, 1) len=2.7071067811865475 ra=2.7071067811865475 slabs=3", //
			"  edge (30, 6, 1) (24, 6, 1) len=3.0 ra=3.0 slabs=3", //
			"tree 3: branches=8 junctions=2 endPoints=2 junctionVoxels=16 slabs=12 avg=2.649238080936521 triples=0 quads=0 max=6.802775637731995 lsp=7.60555127546399", //
			"  vertices=4 edges=8", //
			"  edge (27, 17, 1) (29, 17, 1) len=1.0 ra=1.0 slabs=1", //
			"  edge (39, 17, 1) (35, 14, 1) len=2.802775637731995 ra=2.802775637731995 slabs=1", //
			"  edge (29, 17, 1) (29, 17, 1) len=1.618033988749895 ra=0.0 slabs=1", //
			"  edge (29, 17, 1) (35, 14, 1) len=6.802775637731995 ra=4.5 slabs=3", //
			"  edge (29, 17, 1) (29, 17, 1) len=1.618033988749895 ra=0.0 slabs=1", //
			"  edge (29, 17, 1) (35, 14, 1) len=3.802775637731995 ra=1.5 slabs=3", //
			"  edge (35, 14, 1) (35, 14, 1) len=0.5 ra=0.0 slabs=1", //
			"  edge (35, 14, 1) (35, 14, 1) len=3.0495097567963922 ra=0.0 slabs=1", //
			"tree 4: branches=1 junctions=0 endPoints=0 junctionVoxels=0 slabs=16 avg=10.606601717798217 triples=0 quads=0 max=10.606601717798217 lsp=0.0", //
			"  vertices=1 edges=1", //
			"  edge (1, 19, 1) (1, 19, 1) len=10.606601717798217 ra=8.803314780324513 slabs=16" },
		// A shortest
		{ "tree 0: branches=1 junctions=0 endPoints=2 junctionVoxels=0 slabs=9 avg=10.0 triples=0 quads=0 max=10.0 lsp=-", //
			"  vertices=2 edges=1", //
			"  edge (2, 2, 1) (12, 2, 1) len=10.0 ra=10.0 slabs=9", //
			"tree 1: branches=1 junctions=0 endPoints=2 junctionVoxels=0 slabs=13 avg=19.798989873223338 triples=0 quads=0 max=19.798989873223338 lsp=-", //
			"  vertices=2 edges=1", //
			"  edge (2, 18, 1) (2, 20, 1) len=19.798989873223338 ra=16.144259608056768 slabs=13", //
			"tree 2: branches=3 junctions=1 endPoints=3 junctionVoxels=3 slabs=15 avg=8.227144598981733 triples=1 quads=0 max=9.438793109825918 lsp=-", //
			"  vertices=4 edges=3", //
			"  edge (14, 14, 1) (20, 20, 4) len=9.438793109825918 ra=9.301445722753648 slabs=5", //
			"  edge (20, 27, 4) (20, 20, 4) len=7.0 ra=7.0 slabs=5", //
			"  edge (27, 20, 7) (20, 20, 4) len=8.242640687119284 ra=7.53338387128521 slabs=5", //
			"tree 3: branches=4 junctions=1 endPoints=4 junctionVoxels=5 slabs=12 avg=5.207106781186548 triples=0 quads=1 max=6.0 lsp=-", //
			"  vertices=5 edges=4", //
			"  edge (20, 6, 1) (24, 6, 1) len=4.0 ra=4.0 slabs=3", //
			"  edge (25, 1, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (25, 11, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (30, 6, 1) (24, 6, 1) len=6.0 ra=6.0 slabs=3", //
			"tree 4: branches=5 junctions=2 endPoints=4 junctionVoxels=8 slabs=13 avg=5.179898987322334 triples=2 quads=0 max=10.242640687119286 lsp=-", //
			"  vertices=6 edges=5", //
			"  edge (27, 17, 1) (29, 17, 1) len=2.0 ra=2.0 slabs=1", //
			"  edge (32, 20, 1) (29, 17, 1) len=4.82842712474619 ra=4.475056592705053 slabs=2", //
			"  edge (34, 20, 1) (36, 16, 1) len=5.414213562373095 ra=5.0608430303319585 slabs=2", //
			"  edge (39, 17, 1) (36, 16, 1) len=3.414213562373095 ra=3.414213562373095 slabs=1", //
			"  edge (29, 17, 1) (36, 16, 1) len=10.242640687119286 ra=7.551151507353894 slabs=7" },
		// A pruneEnds
		{ "tree 0: branches=1 junctions=0 endPoints=1 junctionVoxels=0 slabs=0 avg=10.0 triples=0 quads=0 max=10.0 lsp=-", //
			"  vertices=1 edges=0", //
			"tree 1: branches=3 junctions=1 endPoints=0 junctionVoxels=3 slabs=0 avg=8.227144598981733 triples=0 quads=0 max=9.438793109825918 lsp=-", //
			"  vertices=1 edges=0", //
			"tree 2: branches=4 junctions=1 endPoints=0 junctionVoxels=5 slabs=0 avg=5.207106781186548 triples=0 quads=0 max=6.0 lsp=-", //
			"  vertices=1 edges=0", //
			"tree 3: branches=8 junctions=2 endPoints=0 junctionVoxels=16 slabs=10 avg=5.298476161873042 triples=0 quads=0 max=13.60555127546399 lsp=-", //
			"  vertices=2 edges=6", //
			"  edge (29, 17, 1) (29, 17, 1) len=3.23606797749979 ra=0.0 slabs=1", //
			"  edge (29, 17, 1) (35, 14, 1) len=13.60555127546399 ra=9.0 slabs=3", //
			"  edge (29, 17, 1) (29, 17, 1) len=3.23606797749979 ra=0.0 slabs=1", //
			"  edge (29, 17, 1) (35, 14, 1) len=7.60555127546399 ra=3.0 slabs=3", //
			"  edge (35, 14, 1) (35, 14, 1) len=1.0 ra=0.0 slabs=1", //
			"  edge (35, 14, 1) (35, 14, 1) len=6.0990195135927845 ra=0.0 slabs=1", //
			"tree 4: branches=1 junctions=0 endPoints=0 junctionVoxels=0 slabs=16 avg=21.213203435596434 triples=0 quads=0 max=21.213203435596434 lsp=-", //
			"  vertices=1 edges=1", //
			"  edge (1, 19, 1) (1, 19, 1) len=21.213203435596434 ra=17.606629560649026 slabs=16" },
		// A threshold3
		{ "tree 0: branches=1 junctions=0 endPoints=2 junctionVoxels=0 slabs=9 avg=10.0 triples=0 quads=0 max=10.0 lsp=-", //
			"  vertices=2 edges=1", //
			"  edge (2, 2, 1) (12, 2, 1) len=10.0 ra=10.0 slabs=9", //
			"tree 1: branches=3 junctions=1 endPoints=3 junctionVoxels=3 slabs=15 avg=8.227144598981733 triples=1 quads=0 max=9.438793109825918 lsp=-", //
			"  vertices=4 edges=3", //
			"  edge (14, 14, 1) (20, 20, 4) len=9.438793109825918 ra=9.301445722753648 slabs=5", //
			"  edge (20, 27, 4) (20, 20, 4) len=7.0 ra=7.0 slabs=5", //
			"  edge (27, 20, 7) (20, 20, 4) len=8.242640687119284 ra=7.53338387128521 slabs=5", //
			"tree 2: branches=4 junctions=1 endPoints=4 junctionVoxels=5 slabs=12 avg=5.207106781186548 triples=0 quads=1 max=6.0 lsp=-", //
			"  vertices=5 edges=4", //
			"  edge (20, 6, 1) (24, 6, 1) len=4.0 ra=4.0 slabs=3", //
			"  edge (25, 1, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (25, 11, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (30, 6, 1) (24, 6, 1) len=6.0 ra=6.0 slabs=3", //
			"tree 3: branches=8 junctions=2 endPoints=1 junctionVoxels=16 slabs=11 avg=5.298476161873042 triples=0 quads=0 max=13.60555127546399 lsp=-", //
			"  vertices=3 edges=7", //
			"  edge (39, 17, 1) (35, 14, 1) len=5.60555127546399 ra=5.60555127546399 slabs=1", //
			"  edge (29, 17, 1) (29, 17, 1) len=3.23606797749979 ra=0.0 slabs=1", //
			"  edge (29, 17, 1) (35, 14, 1) len=13.60555127546399 ra=9.0 slabs=3", //
			"  edge (29, 17, 1) (29, 17, 1) len=3.23606797749979 ra=0.0 slabs=1", //
			"  edge (29, 17, 1) (35, 14, 1) len=7.60555127546399 ra=3.0 slabs=3", //
			"  edge (35, 14, 1) (35, 14, 1) len=1.0 ra=0.0 slabs=1", //
			"  edge (35, 14, 1) (35, 14, 1) len=6.0990195135927845 ra=0.0 slabs=1", //
			"tree 4: branches=1 junctions=0 endPoints=0 junctionVoxels=0 slabs=16 avg=21.213203435596434 triples=0 quads=0 max=21.213203435596434 lsp=-", //
			"  vertices=1 edges=1", //
			"  edge (1, 19, 1) (1, 19, 1) len=21.213203435596434 ra=17.606629560649026 slabs=16" },
		// A lowVoxel
		{ "tree 0: branches=1 junctions=0 endPoints=2 junctionVoxels=0 slabs=9 avg=10.0 triples=0 quads=0 max=10.0 lsp=-", //
			"  vertices=2 edges=1", //
			"  edge (2, 2, 1) (12, 2, 1) len=10.0 ra=10.0 slabs=9", //
			"tree 1: branches=1 junctions=0 endPoints=2 junctionVoxels=0 slabs=13 avg=19.798989873223338 triples=0 quads=0 max=19.798989873223338 lsp=-", //
			"  vertices=2 edges=1", //
			"  edge (2, 18, 1) (2, 20, 1) len=19.798989873223338 ra=16.144259608056768 slabs=13", //
			"tree 2: branches=3 junctions=1 endPoints=3 junctionVoxels=3 slabs=15 avg=8.227144598981733 triples=1 quads=0 max=9.438793109825918 lsp=-", //
			"  vertices=4 edges=3", //
			"  edge (14, 14, 1) (20, 20, 4) len=9.438793109825918 ra=9.301445722753648 slabs=5", //
			"  edge (20, 27, 4) (20, 20, 4) len=7.0 ra=7.0 slabs=5", //
			"  edge (27, 20, 7) (20, 20, 4) len=8.242640687119284 ra=7.53338387128521 slabs=5", //
			"tree 3: branches=4 junctions=1 endPoints=4 junctionVoxels=5 slabs=12 avg=5.207106781186548 triples=0 quads=1 max=6.0 lsp=-", //
			"  vertices=5 edges=4", //
			"  edge (20, 6, 1) (24, 6, 1) len=4.0 ra=4.0 slabs=3", //
			"  edge (25, 1, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (25, 11, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (30, 6, 1) (24, 6, 1) len=6.0 ra=6.0 slabs=3", //
			"tree 4: branches=5 junctions=2 endPoints=4 junctionVoxels=8 slabs=13 avg=5.179898987322334 triples=2 quads=0 max=10.242640687119286 lsp=-", //
			"  vertices=6 edges=5", //
			"  edge (27, 17, 1) (29, 17, 1) len=2.0 ra=2.0 slabs=1", //
			"  edge (33, 20, 1) (29, 17, 1) len=5.82842712474619 ra=5.369693934628552 slabs=3", //
			"  edge (35, 20, 1) (36, 16, 1) len=4.414213562373095 ra=4.53224755112299 slabs=1", //
			"  edge (39, 17, 1) (36, 16, 1) len=3.414213562373095 ra=3.414213562373095 slabs=1", //
			"  edge (29, 17, 1) (36, 16, 1) len=10.242640687119286 ra=7.551151507353894 slabs=7" },
		// A lowBranch
		{ "tree 0: branches=1 junctions=0 endPoints=2 junctionVoxels=0 slabs=9 avg=10.0 triples=0 quads=0 max=10.0 lsp=-", //
			"  vertices=2 edges=1", //
			"  edge (2, 2, 1) (12, 2, 1) len=10.0 ra=10.0 slabs=9", //
			"tree 1: branches=1 junctions=0 endPoints=2 junctionVoxels=0 slabs=13 avg=19.798989873223338 triples=0 quads=0 max=19.798989873223338 lsp=-", //
			"  vertices=2 edges=1", //
			"  edge (2, 18, 1) (2, 20, 1) len=19.798989873223338 ra=16.144259608056768 slabs=13", //
			"tree 2: branches=3 junctions=1 endPoints=3 junctionVoxels=3 slabs=15 avg=8.227144598981733 triples=1 quads=0 max=9.438793109825918 lsp=-", //
			"  vertices=4 edges=3", //
			"  edge (14, 14, 1) (20, 20, 4) len=9.438793109825918 ra=9.301445722753648 slabs=5", //
			"  edge (20, 27, 4) (20, 20, 4) len=7.0 ra=7.0 slabs=5", //
			"  edge (27, 20, 7) (20, 20, 4) len=8.242640687119284 ra=7.53338387128521 slabs=5", //
			"tree 3: branches=4 junctions=1 endPoints=4 junctionVoxels=5 slabs=12 avg=5.207106781186548 triples=0 quads=1 max=6.0 lsp=-", //
			"  vertices=5 edges=4", //
			"  edge (20, 6, 1) (24, 6, 1) len=4.0 ra=4.0 slabs=3", //
			"  edge (25, 1, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (25, 11, 1) (24, 6, 1) len=5.414213562373095 ra=5.414213562373095 slabs=3", //
			"  edge (30, 6, 1) (24, 6, 1) len=6.0 ra=6.0 slabs=3", //
			"tree 4: branches=5 junctions=2 endPoints=4 junctionVoxels=8 slabs=13 avg=5.179898987322334 triples=2 quads=0 max=10.242640687119286 lsp=-", //
			"  vertices=6 edges=5", //
			"  edge (27, 17, 1) (29, 17, 1) len=2.0 ra=2.0 slabs=1", //
			"  edge (33, 20, 1) (29, 17, 1) len=5.82842712474619 ra=5.369693934628552 slabs=3", //
			"  edge (35, 20, 1) (36, 16, 1) len=4.414213562373095 ra=4.53224755112299 slabs=1", //
			"  edge (39, 17, 1) (36, 16, 1) len=3.414213562373095 ra=3.414213562373095 slabs=1", //
			"  edge (29, 17, 1) (36, 16, 1) len=10.242640687119286 ra=7.551151507353894 slabs=7" },
	};

	/** Draws line segments (x0, y0, z0, x1, y1, z1) into a 3D image. */
	static Img<BitType> draw(final int w, final int h, final int d,
		final int[][] segments)
	{
		final Img<BitType> img = ArrayImgs.bits(w, h, d);
		final RandomAccess<BitType> ra = img.randomAccess();
		for (final int[] s : segments) {
			final int n = Math.max(Math.abs(s[3] - s[0]), Math.max(Math.abs(s[4] -
				s[1]), Math.abs(s[5] - s[2])));
			for (int i = 0; i <= n; i++) {
				final int x = (int) Math.round(s[0] + (double) i * (s[3] - s[0]) / n);
				final int y = (int) Math.round(s[1] + (double) i * (s[4] - s[1]) / n);
				final int z = (int) Math.round(s[2] + (double) i * (s[5] - s[2]) / n);
				ra.setPositionAndGet(x, y, z).set(true);
			}
		}
		return img;
	}

	static Img<UnsignedByteType> intensity() {
		final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(40, 30, 9);
		final Cursor<UnsignedByteType> c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set((c.getIntPosition(0) * 7 + c.getIntPosition(1) * 13 + c
				.getIntPosition(2) * 29) % 251);
		}
		return img;
	}

	private static String pt(final Localizable l) {
		return "(" + l.getLongPosition(0) + ", " + l.getLongPosition(1) + ", " + l
			.getLongPosition(2) + ")";
	}

	private List<String> describe(final double[] calibration,
		final CyclePruning pruning, final Double pruneEnds,
		final RandomAccessibleInterval<UnsignedByteType> intensity,
		final boolean longestShortestPath)
	{
		final List<SkeletonStatistics> stats = ops.op(
			"topology.skeletonStatistics").input(draw(40, 30, 9, SEGMENTS),
				calibration, pruning, pruneEnds, intensity, longestShortestPath)
			.outType(new Nil<List<SkeletonStatistics>>()
			{}).apply();
		final List<String> lines = new ArrayList<>();
		for (int t = 0; t < stats.size(); t++) {
			final SkeletonStatistics s = stats.get(t);
			lines.add(String.format("tree %d: branches=%d junctions=%d " +
				"endPoints=%d junctionVoxels=%d slabs=%d avg=%s triples=%d " +
				"quads=%d max=%s lsp=%s", t, s.branches(), s.junctions(), s
					.endPointVoxels(), s.junctionVoxels(), s.slabVoxels(), Double
						.toString(s.averageBranchLength()), s.triplePoints(), s
							.quadruplePoints(), Double.toString(s.maximumBranchLength()),
				longestShortestPath ? Double.toString(s.longestShortestPath()) : "-"));
			final SkeletonGraph g = s.graph();
			lines.add("  vertices=" + g.vertices().size() + " edges=" + g.edges()
				.size());
			for (final SkeletonEdge e : g.edges()) {
				lines.add("  edge " + pt(e.v1().points().get(0)) + " " + pt(e.v2()
					.points().get(0)) + " len=" + e.length() + " ra=" + e
						.runningAverageLength() + " slabs=" + e.slabs().size());
			}
		}
		return lines;
	}

	@Test
	public void testNoPruning() {
		assertEquals(Arrays.asList(EXPECTED[0]), describe(null, null, null, null,
			true));
	}

	@Test
	public void testCalibrated() {
		assertEquals(Arrays.asList(EXPECTED[1]), describe(new double[] { 0.5, 0.5,
			2 }, null, null, null, true));
	}

	@Test
	public void testShortestBranchPruning() {
		assertEquals(Arrays.asList(EXPECTED[2]), describe(null,
			CyclePruning.SHORTEST_BRANCH, null, null, false));
	}

	@Test
	public void testPruneEnds() {
		assertEquals(Arrays.asList(EXPECTED[3]), describe(null, null,
			Double.POSITIVE_INFINITY, null, false));
	}

	@Test
	public void testPruneEndsThreshold() {
		assertEquals(Arrays.asList(EXPECTED[4]), describe(null, null, 3.5, null,
			false));
	}

	@Test
	public void testPruneEndsThenCycles() {
		// Note: The original plugin prunes no cycles of the trees with end points
		// here, because pruning the end branches removes the root vertex of its
		// cycle search, so only the ring is cut and the re-analyzed loop tree
		// still has 6 cycles; this port picks a new root. After end pruning alone,
		// the loop tree has 5 independent cycles (6 edges between 2 vertices)
		// and the ring has 1.
		assertEquals(6, cycles(graphs(null, Double.POSITIVE_INFINITY)));
		final int remaining = cycles(graphs(CyclePruning.SHORTEST_BRANCH,
			Double.POSITIVE_INFINITY));
		assertTrue(remaining < 5, "remaining cycles: " + remaining);
	}

	private List<SkeletonGraph> graphs(final CyclePruning pruning,
		final Double pruneEnds)
	{
		return ops.op("topology.skeletonGraph").input(draw(40, 30, 9, SEGMENTS),
			null, pruning, pruneEnds).outType(new Nil<List<SkeletonGraph>>()
			{}).apply();
	}

	/** Counts the independent cycles (edges - vertices + 1) of all trees. */
	private static int cycles(final List<SkeletonGraph> graphs) {
		int count = 0;
		for (final SkeletonGraph g : graphs)
			count += g.edges().size() - g.vertices().size() + 1;
		return count;
	}

	@Test
	public void testLowestIntensityVoxelPruning() {
		assertEquals(Arrays.asList(EXPECTED[5]), describe(null,
			CyclePruning.LOWEST_INTENSITY_VOXEL, null, intensity(), false));
	}

	@Test
	public void testLowestIntensityBranchPruning() {
		assertEquals(Arrays.asList(EXPECTED[6]), describe(null,
			CyclePruning.LOWEST_INTENSITY_BRANCH, null, intensity(), false));
	}
}
