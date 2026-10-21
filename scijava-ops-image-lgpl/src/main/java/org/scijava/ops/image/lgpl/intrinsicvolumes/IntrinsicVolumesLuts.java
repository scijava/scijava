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


package org.scijava.ops.image.lgpl.intrinsicvolumes;

/**
 * Look-up tables associating each binary configuration of 2&times;2 pixels
 * (16 configurations) or 2&times;2&times;2 voxels (256 configurations) with its
 * contribution to an intrinsic volume.
 * <p>
 * Configuration indices encode pixel {@code (dx, dy)} (or voxel
 * {@code (dx, dy, dz)}) of the tile in bit {@code dx + 2 dy (+ 4 dz)}.
 * </p>
 * <p>
 * Translated from {@code IntrinsicVolumes2DUtils},
 * {@code IntrinsicVolumes3DUtils}, {@code GeometryUtils} and
 * {@code InterfaceSurfaceArea} of <a href=
 * "https://github.com/ijpb/MorphoLibJ">MorphoLibJ</a> (LGPL-3, Copyright (C)
 * 2014 - 2026 INRA) by David Legland. See Legland, Ki&ecirc;u and Devaux,
 * "Computation of Minkowski measures on 2D and 3D binary images", <i>Image
 * Analysis and Stereology</i> 26(2): 83&ndash;92, 2007; Ohser and
 * M&uuml;cklich, <i>Statistical Analysis of Microstructures in Materials
 * Science</i>, Wiley, 2000; and Ohser and Schladitz, <i>3D Images of Material
 * Structures</i>, Wiley, 2009 (Euler number tables 3.2 and 3.3).
 * </p>
 *
 * @author David Legland
 * @author Curtis Rueden
 */
final class IntrinsicVolumesLuts {

	private IntrinsicVolumesLuts() {
		// Prevent instantiation of static utility class.
	}

	// -- 2D --

	/** Area contribution of each 2x2 configuration. */
	static double[] areaLut(final double[] spacing) {
		final var lut = new double[16];
		final var pixelArea = spacing[0] * spacing[1];
		for (var i = 0; i < 16; i++)
			lut[i] = Integer.bitCount(i) / 4.0 * pixelArea;
		return lut;
	}

	/** Crofton perimeter contribution of each 2x2 configuration. */
	static double[] perimeterLut(final double[] spacing, final int nDirs) {
		if (nDirs != 2 && nDirs != 4) {
			throw new IllegalArgumentException(
				"Number of directions must be 2 or 4, not " + nDirs);
		}
		final var d1 = spacing[0];
		final var d2 = spacing[1];
		final var d12 = Math.hypot(d1, d2);
		final var area = d1 * d2;

		// Weights of the four directions: [1 0], [0 1], [1 1] and [-1 1].
		final var theta = Math.atan2(d2, d1);
		final double[] weights = { theta / Math.PI, (Math.PI / 2 - theta) /
			Math.PI, 0.25, 0.25 };

		final var lut = new double[16];
		final var im = new boolean[2][2];
		for (var i = 0; i < 16; i++) {
			im[0][0] = (i & 1) > 0;
			im[0][1] = (i & 2) > 0;
			im[1][0] = (i & 4) > 0;
			im[1][1] = (i & 8) > 0;
			for (var y = 0; y < 2; y++) {
				for (var x = 0; x < 2; x++) {
					if (!im[y][x]) continue;
					// Note: Dividing by two converts intersection counts to
					// projected diameters.
					final var ke1 = im[y][1 - x] ? 0 : (area / d1) / 2;
					final var ke2 = im[1 - y][x] ? 0 : (area / d2) / 2;
					if (nDirs == 2) {
						// Note: Divide by two for the average over directions, and by
						// two for the multiplicity.
						lut[i] += (ke1 + ke2) / 4;
					}
					else {
						final var ke12 = im[1 - y][1 - x] ? 0 : (area / d12) / 2;
						lut[i] += (ke1 / 2) * weights[0] + (ke2 / 2) * weights[1] +
							ke12 * weights[2];
					}
				}
			}
			lut[i] *= Math.PI;
		}
		return lut;
	}

	/** Euler number contribution of each 2x2 configuration. */
	static double[] eulerNumberLut2D(final int conn) {
		switch (conn) {
			case 4:
				return new double[] { 0, 0.25, 0.25, 0, 0.25, 0, 0.5, -0.25, 0.25,
					0.5, 0, -0.25, 0, -0.25, -0.25, 0 };
			case 8:
				return new double[] { 0, 0.25, 0.25, 0, 0.25, 0, -0.5, -0.25, 0.25,
					-0.5, 0, -0.25, 0, -0.25, -0.25, 0 };
			default:
				throw new IllegalArgumentException(
					"2D connectivity must be 4 or 8, not " + conn);
		}
	}

	// -- 3D --

	/** Volume contribution of each 2x2x2 configuration. */
	static double[] volumeLut(final double[] spacing) {
		final var lut = new double[256];
		final var voxelVolume = spacing[0] * spacing[1] * spacing[2];
		for (var i = 0; i < 256; i++)
			lut[i] = Integer.bitCount(i) * voxelVolume / 8;
		return lut;
	}

	/** Crofton surface area contribution of each 2x2x2 configuration. */
	static double[] surfaceAreaLut(final double[] spacing, final int nDirs) {
		checkDirections3D(nDirs);
		final var d1 = spacing[0];
		final var d2 = spacing[1];
		final var d3 = spacing[2];
		final var vol = d1 * d2 * d3;
		final var d12 = Math.hypot(d1, d2);
		final var d13 = Math.hypot(d1, d3);
		final var d23 = Math.hypot(d2, d3);
		final var d123 = Math.hypot(d12, d3);
		final var weights = nDirs == 13 ? directionWeights13(spacing) : null;

		final var lut = new double[256];
		final var im = new boolean[2][2][2];
		final var kei = new double[7];
		for (var i = 0; i < 256; i++) {
			updateTile(im, i);
			for (var z = 0; z < 2; z++) {
				for (var y = 0; y < 2; y++) {
					for (var x = 0; x < 2; x++) {
						if (!im[z][y][x]) continue;
						if (nDirs == 3) {
							final var ke1 = im[z][y][1 - x] ? 0 : vol / d1 / 2.0;
							final var ke2 = im[z][1 - y][x] ? 0 : vol / d2 / 2.0;
							final var ke3 = im[1 - z][y][x] ? 0 : vol / d3 / 2.0;
							// Note: With 3 directions, the multiplicity 4 cancels the
							// factor 4 of the Crofton formula, so we just average.
							lut[i] += (ke1 + ke2 + ke3) / 3;
							continue;
						}
						kei[0] = im[z][y][1 - x] ? 0 : vol / d1 / 8.0;
						kei[1] = im[z][1 - y][x] ? 0 : vol / d2 / 8.0;
						kei[2] = im[1 - z][y][x] ? 0 : vol / d3 / 8.0;
						kei[3] = im[z][1 - y][1 - x] ? 0 : vol / d12 / 4.0;
						kei[4] = im[1 - z][y][1 - x] ? 0 : vol / d13 / 4.0;
						kei[5] = im[1 - z][1 - y][x] ? 0 : vol / d23 / 4.0;
						kei[6] = im[1 - z][1 - y][1 - x] ? 0 : vol / d123 / 2.0;
						for (var k = 0; k < 7; k++)
							lut[i] += 4 * kei[k] * weights[k];
					}
				}
			}
		}
		return lut;
	}

	/** Crofton mean breadth contribution of each 2x2x2 configuration. */
	static double[] meanBreadthLut(final double[] spacing, final int nDirs,
		final int conn2d)
	{
		checkDirections3D(nDirs);
		if (conn2d != 4 && conn2d != 8) {
			throw new IllegalArgumentException(
				"2D connectivity must be 4 or 8, not " + conn2d);
		}
		final var d1 = spacing[0];
		final var d2 = spacing[1];
		final var d3 = spacing[2];
		final var vol = d1 * d2 * d3;
		final var d12 = Math.hypot(d1, d2);
		final var d13 = Math.hypot(d1, d3);
		final var d23 = Math.hypot(d2, d3);

		// Areas of the elementary profiles along each direction.
		final var s = (d12 + d13 + d23) / 2;
		final double[] areas = { d2 * d3, d1 * d3, d1 * d2, d3 * d12, d2 * d13,
			d1 * d23, 2 * Math.sqrt(s * (s - d12) * (s - d13) * (s - d23)) };
		final var weights = nDirs == 13 ? directionWeights13(spacing) : null;
		final var diams = new double[7];

		final var lut = new double[256];
		final var im = new boolean[2][2][2];
		// Note: The empty and full configurations contribute zero by definition.
		for (var i = 1; i < 255; i++) {
			updateTile(im, i);
			for (var v = 0; v < 8; v++) {
				final var p1 = v >> 1 & 1;
				final var p2 = v & 1;
				final var p3 = v >> 2 & 1;
				if (!im[p1][p2][p3]) continue;
				final var q1 = 1 - p1;
				final var q2 = 1 - p2;
				final var q3 = 1 - p3;

				// Faces in each isothetic direction.
				final boolean[] face1 = { true, im[p1][q2][p3], im[p1][p2][q3],
					im[p1][q2][q3] };
				final boolean[] face2 = { true, im[q1][p2][p3], im[p1][p2][q3],
					im[q1][p2][q3] };
				final boolean[] face3 = { true, im[q1][p2][p3], im[p1][q2][p3],
					im[q1][q2][p3] };
				final var f1 = eulerContribTile2D(face1, conn2d, d3, d2);
				final var f2 = eulerContribTile2D(face2, conn2d, d3, d1);
				final var f3 = eulerContribTile2D(face3, conn2d, d2, d1);
				if (nDirs == 3) {
					// Note: Divide by 3 for the average over directions, and by 2
					// because each face is shared by 2 configurations.
					lut[i] += vol * (f1 / areas[0] + f2 / areas[1] + f3 / areas[2]) /
						6;
					continue;
				}
				diams[0] = f1 / 2.0;
				diams[1] = f2 / 2.0;
				diams[2] = f3 / 2.0;

				// Faces normal to the square diagonals.
				final boolean[] face4 = { true, im[q1][q2][p3], im[p1][p2][q3],
					im[q1][q2][q3] };
				final boolean[] face6 = { true, im[q1][p2][q3], im[p1][q2][p3],
					im[q1][q2][q3] };
				final boolean[] face8 = { true, im[p1][q2][q3], im[q1][p2][p3],
					im[q1][q2][q3] };
				diams[3] = eulerContribTile2D(face4, conn2d, d12, d3);
				diams[4] = eulerContribTile2D(face6, conn2d, d13, d2);
				diams[5] = eulerContribTile2D(face8, conn2d, d23, d1);

				// Triangular faces normal to the cube diagonals.
				final boolean[] faceA = { true, im[q1][q2][p3], im[q1][p2][q3] };
				final boolean[] faceB = { true, im[q1][p2][q3], im[p1][q2][q3] };
				final boolean[] faceC = { true, im[q1][q2][p3], im[p1][q2][q3] };
				diams[6] = eulerContribTriangle(faceA, d12, d13, d23) +
					eulerContribTriangle(faceB, d13, d23, d12) + eulerContribTriangle(
						faceC, d12, d23, d13);

				for (var k = 0; k < 7; k++)
					lut[i] += vol * (diams[k] / areas[k]) * weights[k];
			}
		}
		return lut;
	}

	/** Euler number contribution of each 2x2x2 configuration. */
	static double[] eulerNumberLut3D(final int conn) {
		final int[] lut;
		if (conn == 6) lut = EULER_C6;
		else if (conn == 26) lut = EULER_C26;
		else {
			throw new IllegalArgumentException(
				"3D connectivity must be 6 or 26, not " + conn);
		}
		final var result = new double[256];
		for (var i = 0; i < 256; i++)
			result[i] = lut[i] / 8.0;
		return result;
	}

	/**
	 * Contribution of each pair of voxels within a 2x2x2 configuration to the
	 * interface surface area, for the 7 neighbor offsets X, Y, Z, XY, XZ, YZ and
	 * XYZ.
	 * <p>
	 * Unlike MorphoLibJ, the contributions for 3 directions are calibrated and
	 * consistent with {@link #surfaceAreaLut}: MorphoLibJ uses an uncalibrated
	 * contribution of 1/3 per voxel pair, which yields twice the 3-direction
	 * surface area estimate of a region bounded by a single other region.
	 * </p>
	 */
	static double[] interfaceContributions(final double[] spacing,
		final int nDirs)
	{
		checkDirections3D(nDirs);
		final var d1 = spacing[0];
		final var d2 = spacing[1];
		final var d3 = spacing[2];
		final var vol = d1 * d2 * d3;
		if (nDirs == 3) {
			return new double[] { vol / d1 / 6, vol / d2 / 6, vol / d3 / 6, 0, 0, 0,
				0 };
		}
		final var d12 = Math.hypot(d1, d2);
		final var d13 = Math.hypot(d1, d3);
		final var d23 = Math.hypot(d2, d3);
		final var d123 = Math.hypot(d12, d3);
		final var w = directionWeights13(spacing);
		return new double[] { 4 * w[0] * vol / d1 / 8, 4 * w[1] * vol / d2 / 8,
			4 * w[2] * vol / d3 / 8, 4 * w[3] * vol / d12 / 4, 4 * w[4] * vol / d13 /
				4, 4 * w[5] * vol / d23 / 4, 4 * w[6] * vol / d123 / 2 };
	}

	/**
	 * Weights of the 7 direction classes (X, Y, Z, XY, XZ, YZ, XYZ) among the 13
	 * directions of the cubic grid, given by the areas of the spherical Voronoi
	 * domains of the 26 neighbor directions. The weights satisfy
	 * {@code w0 + w1 + w2 + 2 (w3 + w4 + w5) + 4 w6 = 1}.
	 */
	static double[] directionWeights13(final double[] spacing) {
		final var dx = spacing[0];
		final var dy = spacing[1];
		final var dz = spacing[2];
		if (dx == dy && dx == dz) {
			// Precomputed weights for cubic voxels.
			final var w1 = 0.04577789120476 * 2;
			final var w2 = 0.03698062787608 * 2;
			final var w3 = 0.03519563978232 * 2;
			return new double[] { w1, w1, w1, w2, w2, w2, w3 };
		}
		// Reference vectors are named after the sign of each coordinate:
		// P (positive), N (negative) or Z (zero).
		final var vPNN = unit(dx, -dy, -dz);
		final var vPZN = unit(dx, 0, -dz);
		final var vNPN = unit(-dx, dy, -dz);
		final var vZPN = unit(0, dy, -dz);
		final var vPPN = unit(dx, dy, -dz);
		final var vPNZ = unit(dx, -dy, 0);
		final var vPZZ = unit(dx, 0, 0);
		final var vNPZ = unit(-dx, dy, 0);
		final var vZPZ = unit(0, dy, 0);
		final var vPPZ = unit(dx, dy, 0);
		final var vNNP = unit(-dx, -dy, dz);
		final var vZNP = unit(0, -dy, dz);
		final var vPNP = unit(dx, -dy, dz);
		final var vNZP = unit(-dx, 0, dz);
		final var vZZP = unit(0, 0, dz);
		final var vPZP = unit(dx, 0, dz);
		final var vNPP = unit(-dx, dy, dz);
		final var vZPP = unit(0, dy, dz);
		final var vPPP = unit(dx, dy, dz);

		final var weights = new double[7];
		weights[0] = voronoiArea(vPZZ, vPNN, vPNZ, vPNP, vPZP, vPPP, vPPZ, vPPN,
			vPZN);
		weights[1] = voronoiArea(vZPZ, vPPZ, vPPP, vZPP, vNPP, vNPZ, vNPN, vZPN,
			vPPN);
		weights[2] = voronoiArea(vZZP, vPZP, vPPP, vZPP, vNPP, vNZP, vNNP, vZNP,
			vPNP);
		weights[3] = voronoiArea(vPPZ, vPZZ, vPPP, vZPZ, vPPN);
		weights[4] = voronoiArea(vPZP, vPZZ, vPPP, vZZP, vPNP);
		weights[5] = voronoiArea(vZPP, vZPZ, vNPP, vZZP, vPPP);
		weights[6] = voronoiArea(vPPP, vPZP, vZZP, vZPP, vZPZ, vPPZ, vPZZ);
		for (var k = 0; k < 7; k++)
			weights[k] /= 2 * Math.PI;
		return weights;
	}

	// -- Helper methods --

	private static void checkDirections3D(final int nDirs) {
		if (nDirs != 3 && nDirs != 13) {
			throw new IllegalArgumentException(
				"Number of directions must be 3 or 13, not " + nDirs);
		}
	}

	private static void updateTile(final boolean[][][] tile, final int index) {
		for (var b = 0; b < 8; b++)
			tile[b >> 2 & 1][b >> 1 & 1][b & 1] = (index & 1 << b) != 0;
	}

	/**
	 * Euler number contribution of the reference vertex {@code face[0]} of a
	 * rectangular tile; {@code d1} and {@code d2} are the distances from vertex 0
	 * to vertices 1 and 2, and vertex 3 is opposite to vertex 0.
	 */
	private static double eulerContribTile2D(final boolean[] face,
		final int conn2d, final double d1, final double d2)
	{
		var n = 0;
		for (final var b : face)
			if (b) n++;
		switch (n) {
			case 1:
				return 0.25;
			case 2:
				// An isothetic edge is shared with another tile; a diagonal one is
				// an isolated pair (C4) or an edge (C8).
				return face[3] ? (conn2d == 4 ? 0.25 : -0.25) : 0;
			case 3:
				if (conn2d == 4) return face[3] ? 0 : -0.25;
				if (!face[3]) return 0;
				// Triangle seen from an acute angle.
				final var alpha = face[2] ? Math.atan2(d2, d1) : Math.atan2(d1, d2);
				return (Math.PI - alpha) / (2 * Math.PI) - 0.5;
			default:
				return 0;
		}
	}

	/**
	 * Euler number contribution of the reference vertex {@code face[0]} of a
	 * triangular tile, with side lengths {@code d1} (vertices 0-1), {@code d2}
	 * (vertices 0-2) and {@code d3} (vertices 1-2).
	 */
	private static double eulerContribTriangle(final boolean[] face,
		final double d1, final double d2, final double d3)
	{
		var n = 1;
		if (face[1]) n++;
		if (face[2]) n++;
		switch (n) {
			case 1:
				// Isolated vertex, of multiplicity 6.
				return 1.0 / 6.0;
			case 2:
				// Vertex (1/6) minus half an edge of multiplicity 2.
				return -1.0 / 12.0;
			default:
				// Full triangle: angle at the reference vertex by the cosine law.
				final var alpha = Math.acos((d1 * d1 + d2 * d2 - d3 * d3) / (2 * d1 *
					d2));
				return (Math.PI - alpha) / (2 * Math.PI) - 1.0 / 3.0;
		}
	}

	private static double[] unit(final double x, final double y,
		final double z)
	{
		final var norm = Math.sqrt(x * x + y * y + z * z);
		return new double[] { x / norm, y / norm, z / norm };
	}

	private static double[] minus(final double[] a, final double[] b) {
		return new double[] { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
	}

	private static double[] cross(final double[] a, final double[] b) {
		return new double[] { a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2],
			a[0] * b[1] - a[1] * b[0] };
	}

	private static double norm(final double[] a) {
		return Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
	}

	private static double[] normalize(final double[] a) {
		final var n = norm(a);
		return new double[] { a[0] / n, a[1] / n, a[2] / n };
	}

	private static double angle(final double[] a, final double[] b) {
		final var dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
		return Math.atan2(norm(cross(a, b)), dot);
	}

	/**
	 * Area of the spherical Voronoi domain of {@code germ}, given its neighbor
	 * germs in cyclic order.
	 */
	private static double voronoiArea(final double[] germ,
		final double[]... neighbors)
	{
		final var g = normalize(germ);
		final var n = neighbors.length;
		// Normals of the median planes between germ and neighbors.
		final var normals = new double[n][];
		for (var i = 0; i < n; i++)
			normals[i] = normalize(minus(normalize(neighbors[i]), g));
		// Directions of the intersection lines of consecutive median planes.
		final var lines = new double[n][];
		for (var i = 0; i < n; i++)
			lines[i] = cross(normals[i], normals[(i + 1) % n]);
		// Sum of the spherical angles of the polygon vertices.
		var area = 0.0;
		for (var i = 0; i < n; i++) {
			final var v1 = lines[i];
			final var v2 = lines[(i + 1) % n];
			final var v3 = lines[(i + 2) % n];
			final var u = cross(v2, cross(v1, v2));
			final var w = cross(cross(v2, v3), v2);
			var a = angle(u, w);
			if (a > Math.PI) a = 2 * Math.PI - a;
			area += a;
		}
		return area - Math.PI * (n - 2);
	}

	// Note: Tables 3.2 and 3.3 of Ohser and Schladitz (2009), scaled by 8.
	private static final int[] EULER_C6 = { //
		0, 1, 1, 0, 1, 0, 2, -1, 1, 2, 0, -1, 0, -1, -1, 0, //
		1, 0, 2, -1, 2, -1, 3, -2, 2, 1, 1, -2, 1, -2, 0, -1, //
		1, 2, 0, -1, 2, 1, 1, -2, 2, 3, -1, -2, 1, 0, -2, -1, //
		0, -1, -1, 0, 1, -2, 0, -1, 1, 0, -2, -1, 0, -3, -3, 0, //
		1, 2, 2, 1, 0, -1, 1, -2, 2, 3, 1, 0, -1, -2, -2, -1, //
		0, -1, 1, -2, -1, 0, 0, -1, 1, 0, 0, -3, -2, -1, -3, 0, //
		2, 3, 1, 0, 1, 0, 0, -3, 3, 4, 0, -1, 0, -1, -3, -2, //
		-1, -2, -2, -1, -2, -1, -3, 0, 0, -1, -3, -2, -3, -2, -6, 1, //
		1, 2, 2, 1, 2, 1, 3, 0, 0, 1, -1, -2, -1, -2, -2, -1, //
		2, 1, 3, 0, 3, 0, 4, -1, 1, 0, 0, -3, 0, -3, -1, -2, //
		0, 1, -1, -2, 1, 0, 0, -3, -1, 0, 0, -1, -2, -3, -1, 0, //
		-1, -2, -2, -1, 0, -3, -1, -2, -2, -3, -1, 0, -3, -6, -2, 1, //
		0, 1, 1, 0, -1, -2, 0, -3, -1, 0, -2, -3, 0, -1, -1, 0, //
		-1, -2, 0, -3, -2, -1, -1, -2, -2, -3, -3, -6, -1, 0, -2, 1, //
		-1, 0, -2, -3, -2, -3, -3, -6, -2, -1, -1, -2, -1, -2, 0, 1, //
		0, -1, -1, 0, -1, 0, -2, 1, -1, -2, 0, 1, 0, 1, 1, 0 };

	private static final int[] EULER_C26 = { //
		0, 1, 1, 0, 1, 0, -2, -1, 1, -2, 0, -1, 0, -1, -1, 0, //
		1, 0, -2, -1, -2, -1, -1, -2, -6, -3, -3, -2, -3, -2, 0, -1, //
		1, -2, 0, -1, -6, -3, -3, -2, -2, -1, -1, -2, -3, 0, -2, -1, //
		0, -1, -1, 0, -3, -2, 0, -1, -3, 0, -2, -1, 0, 1, 1, 0, //
		1, -2, -6, -3, 0, -1, -3, -2, -2, -1, -3, 0, -1, -2, -2, -1, //
		0, -1, -3, -2, -1, 0, 0, -1, -3, 0, 0, 1, -2, -1, 1, 0, //
		-2, -1, -3, 0, -3, 0, 0, 1, -1, 4, 0, 3, 0, 3, 1, 2, //
		-1, -2, -2, -1, -2, -1, 1, 0, 0, 3, 1, 2, 1, 2, 2, 1, //
		1, -6, -2, -3, -2, -3, -1, 0, 0, -3, -1, -2, -1, -2, -2, -1, //
		-2, -3, -1, 0, -1, 0, 4, 3, -3, 0, 0, 1, 0, 1, 3, 2, //
		0, -3, -1, -2, -3, 0, 0, 1, -1, 0, 0, -1, -2, 1, -1, 0, //
		-1, -2, -2, -1, 0, 1, 3, 2, -2, 1, -1, 0, 1, 2, 2, 1, //
		0, -3, -3, 0, -1, -2, 0, 1, -1, 0, -2, 1, 0, -1, -1, 0, //
		-1, -2, 0, 1, -2, -1, 3, 2, -2, 1, 1, 2, -1, 0, 2, 1, //
		-1, 0, -2, 1, -2, 1, 1, 2, -2, 3, -1, 2, -1, 2, 0, 1, //
		0, -1, -1, 0, -1, 0, 2, 1, -1, 2, 0, 1, 0, 1, 1, 0 };

}
