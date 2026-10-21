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
import java.util.List;
import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

/**
 * Skeletonizes (thins) a 2D or 3D binary image with the 3D medial axis thinning
 * algorithm of Lee, Kashyap &amp; Chu.
 * <p>
 * The image is thinned in repeated passes over the six border directions
 * (north, south, east, west, up, bottom; only the first four for 2D images).
 * In each pass, foreground border points that are not end points, whose
 * removal preserves the Euler characteristic (26-connectivity) and which are
 * simple points (their removal does not change the connectivity of their
 * 3&times;3&times;3 neighborhood) are collected; they are then re-checked
 * sequentially for simplicity and removed. Thinning stops when a full round of
 * six passes removes nothing. Elements outside the image are background.
 * </p>
 * <p>
 * This is a translation of the Fiji
 * <a href="https://github.com/fiji/Skeletonize3D">Skeletonize3D</a> plugin by
 * Ignacio Arganda-Carreras (GPL v3+), itself based on the ITK implementation
 * by Hanno Homann (<a href="http://hdl.handle.net/1926/1292">Insight
 * Journal</a>). For a 2D input, the result matches the plugin run on a
 * single-slice image.
 * </p>
 * <p>
 * Reference: T.-C. Lee, R. L. Kashyap and C.-N. Chu, "Building skeleton models
 * via 3-D medial surface/axis thinning algorithms", CVGIP: Graphical Models
 * and Image Processing 56(6):462-478, 1994.
 * </p>
 *
 * @author Ignacio Arganda-Carreras
 * @author Curtis Rueden
 * @param <B> type of the binary input
 * @implNote op names='morphology.skeletonize3D'
 */
public class Skeletonize3D<B extends BooleanType<B>> implements
	Function<RandomAccessibleInterval<B>, RandomAccessibleInterval<BitType>>
{

	/**
	 * Euler characteristic contribution of each octant configuration, for odd
	 * indices 1, 3, ..., 255 (even indices contribute 0), as tabulated by Lee
	 * et al. (1994).
	 */
	private static final int[] EULER_ODD = { //
		1, -1, -1, 1, -3, -1, -1, 1, -1, 1, 1, -1, 3, 1, 1, -1, //
		-3, -1, 3, 1, 1, -1, 3, 1, -1, 1, 1, -1, 3, 1, 1, -1, //
		-3, 3, -1, 1, 1, 3, -1, 1, -1, 1, 1, -1, 3, 1, 1, -1, //
		1, 3, 3, 1, 5, 3, 3, 1, -1, 1, 1, -1, 3, 1, 1, -1, //
		-7, -1, -1, 1, -3, -1, -1, 1, -1, 1, 1, -1, 3, 1, 1, -1, //
		-3, -1, 3, 1, 1, -1, 3, 1, -1, 1, 1, -1, 3, 1, 1, -1, //
		-3, 3, -1, 1, 1, 3, -1, 1, -1, 1, 1, -1, 3, 1, 1, -1, //
		1, 3, 3, 1, 5, 3, 3, 1, -1, 1, 1, -1, 3, 1, 1, -1 };

	/**
	 * For each of the eight octants (SWU, SEU, NWU, NEU, SWB, SEB, NWB, NEB),
	 * the neighborhood indices encoded in bits 128, 64, 32, 16, 8, 4 and 2 of
	 * the octant configuration (bit 1 is the center point).
	 */
	private static final int[][] OCTANTS = { //
		{ 24, 25, 15, 16, 21, 22, 12 }, //
		{ 26, 23, 17, 14, 25, 22, 16 }, //
		{ 18, 21, 9, 12, 19, 22, 10 }, //
		{ 20, 23, 19, 22, 11, 14, 10 }, //
		{ 6, 15, 7, 16, 3, 12, 4 }, //
		{ 8, 7, 17, 16, 5, 4, 14 }, //
		{ 0, 9, 3, 12, 1, 10, 4 }, //
		{ 2, 1, 11, 10, 5, 4, 14 } };

	/**
	 * Octree labeling table: for each octant 1-8, pairs of (cube index, octants
	 * to recurse into) for the points of that octant; the cube excludes the
	 * center point.
	 */
	private static final int[][][] OCTREE = { //
		{ { 0 }, { 1, 2 }, { 3, 3 }, { 4, 2, 3, 4 }, { 9, 5 }, { 10, 2, 5, 6 }, {
			12, 3, 5, 7 } }, //
		{ { 1, 1 }, { 4, 1, 3, 4 }, { 10, 1, 5, 6 }, { 2 }, { 5, 4 }, { 11, 6 }, {
			13, 4, 6, 8 } }, //
		{ { 3, 1 }, { 4, 1, 2, 4 }, { 12, 1, 5, 7 }, { 6 }, { 7, 4 }, { 14, 7 }, {
			15, 4, 7, 8 } }, //
		{ { 4, 1, 2, 3 }, { 5, 2 }, { 13, 2, 6, 8 }, { 7, 3 }, { 15, 3, 7, 8 }, {
			8 }, { 16, 8 } }, //
		{ { 9, 1 }, { 10, 1, 2, 6 }, { 12, 1, 3, 7 }, { 17 }, { 18, 6 }, { 20, 7 },
			{ 21, 6, 7, 8 } }, //
		{ { 10, 1, 2, 5 }, { 11, 2 }, { 13, 2, 4, 8 }, { 18, 5 }, { 21, 5, 7, 8 },
			{ 19 }, { 22, 8 } }, //
		{ { 12, 1, 3, 5 }, { 14, 3 }, { 15, 3, 4, 8 }, { 20, 5 }, { 21, 5, 6, 8 },
			{ 23 }, { 24, 8 } }, //
		{ { 13, 2, 4, 6 }, { 15, 3, 4, 7 }, { 16, 4 }, { 21, 5, 6, 7 }, { 22, 6 },
			{ 24, 7 }, { 25 } } };

	/** The octant from which to start labeling each cube index. */
	private static final int[] START_OCTANT = { 1, 1, 2, 1, 1, 2, 3, 3, 4, 1, 1,
		2, 1, 2, 3, 3, 4, 5, 5, 6, 5, 5, 6, 7, 7, 8 };

	/**
	 * Skeletonizes a binary image.
	 *
	 * @param input the 2D or 3D binary image; {@code true} elements are
	 *          foreground
	 * @return the skeleton, with the same interval as the input
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply(
		final RandomAccessibleInterval<B> input)
	{
		final int n = input.numDimensions();
		if (n != 2 && n != 3) {
			throw new IllegalArgumentException(
				"Only 2D and 3D images are supported");
		}
		final long[] dims = input.dimensionsAsLongArray();
		final long size = dims[0] * dims[1] * (n == 3 ? dims[2] : 1);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" elements");
		}
		final int w = (int) dims[0];
		final int h = (int) dims[1];
		final int d = n == 3 ? (int) dims[2] : 1;

		final byte[] img = new byte[(int) size];
		int i = 0;
		for (final B b : Views.flatIterable(input))
			img[i++] = (byte) (b.get() ? 1 : 0);

		new Thinner(img, w, h, d).thin();

		final ArrayImg<BitType, LongArray> out = ArrayImgs.bits(dims);
		i = 0;
		for (final BitType b : out)
			b.set(img[i++] != 0);
		return Views.translate(out, input.minAsLongArray());
	}

	private static final class Thinner {

		private final byte[] img;
		private final int w, h, d;
		private final byte[] nb = new byte[27];
		private final int[] cube = new int[26];

		Thinner(final byte[] img, final int w, final int h, final int d) {
			this.img = img;
			this.w = w;
			this.h = h;
			this.d = d;
		}

		void thin() {
			final List<int[]> simpleBorderPoints = new ArrayList<>();
			final int borders = d > 1 ? 6 : 4;
			int unchangedBorders = 0;
			while (unchangedBorders < 6) {
				unchangedBorders = 0;
				for (int border = 1; border <= 6; border++) {
					boolean noChange = true;
					if (border <= borders) {
						for (int z = 0; z < d; z++) {
							for (int y = 0; y < h; y++) {
								for (int x = 0; x < w; x++) {
									if (img[x + w * (y + h * z)] != 1) continue;
									if (!isBorderPoint(border, x, y, z)) continue;
									neighborhood(x, y, z);
									if (isEndPoint()) continue;
									if (!isEulerInvariant()) continue;
									if (!isSimplePoint()) continue;
									simpleBorderPoints.add(new int[] { x, y, z });
								}
							}
						}
						// Note: Sequential re-checking preserves connectivity when the
						// candidates collected above are deleted.
						for (final int[] p : simpleBorderPoints) {
							neighborhood(p[0], p[1], p[2]);
							if (isSimplePoint()) {
								img[p[0] + w * (p[1] + h * p[2])] = 0;
								noChange = false;
							}
						}
						simpleBorderPoints.clear();
					}
					if (noChange) unchangedBorders++;
				}
			}
		}

		private boolean isBorderPoint(final int border, final int x, final int y,
			final int z)
		{
			switch (border) {
				case 1:
					return get(x, y - 1, z) <= 0;
				case 2:
					return get(x, y + 1, z) <= 0;
				case 3:
					return get(x + 1, y, z) <= 0;
				case 4:
					return get(x - 1, y, z) <= 0;
				case 5:
					return get(x, y, z + 1) <= 0;
				default:
					return get(x, y, z - 1) <= 0;
			}
		}

		private byte get(final int x, final int y, final int z) {
			if (x < 0 || x >= w || y < 0 || y >= h || z < 0 || z >= d) return 0;
			return img[x + w * (y + h * z)];
		}

		private void neighborhood(final int x, final int y, final int z) {
			int k = 0;
			for (int dz = -1; dz <= 1; dz++)
				for (int dy = -1; dy <= 1; dy++)
					for (int dx = -1; dx <= 1; dx++)
						nb[k++] = get(x + dx, y + dy, z + dz);
		}

		private boolean isEndPoint() {
			int count = -1;
			for (int k = 0; k < 27; k++)
				if (nb[k] == 1) count++;
			return count == 1;
		}

		private boolean isEulerInvariant() {
			int euler = 0;
			for (final int[] octant : OCTANTS) {
				int c = 1;
				for (int b = 0; b < 7; b++)
					if (nb[octant[b]] == 1) c |= 128 >> b;
				euler += EULER_ODD[c >> 1];
			}
			return euler == 0;
		}

		private boolean isSimplePoint() {
			for (int k = 0; k < 13; k++)
				cube[k] = nb[k];
			for (int k = 14; k < 27; k++)
				cube[k - 1] = nb[k];
			int label = 2;
			for (int k = 0; k < 26; k++) {
				if (cube[k] == 1) {
					octreeLabeling(START_OCTANT[k], label);
					label++;
					if (label - 2 >= 2) return false;
				}
			}
			return true;
		}

		private void octreeLabeling(final int octant, final int label) {
			for (final int[] entry : OCTREE[octant - 1]) {
				final int idx = entry[0];
				if (cube[idx] == 1) {
					cube[idx] = label;
					for (int e = 1; e < entry.length; e++)
						octreeLabeling(entry[e], label);
				}
			}
		}
	}
}
