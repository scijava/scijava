/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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
package org.scijava.ops.image.image.distancetransform;

import java.util.ArrayList;
import java.util.List;

import org.scijava.concurrent.Parallelization;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;

/**
 * Computes a distance transform, i.e. for every foreground pixel its distance
 * to the nearest background pixel.
 * 
 * @author Simon Schmid (University of Konstanz)
 */
public final class DistanceTransform3D {

	private DistanceTransform3D() {
		// Prevent instantiation of static utility class
	}

	/*
	 * meijsters raster scan alogrithm Source:
	 * http://fab.cba.mit.edu/classes/S62.12/docs/Meijster_distance.pdf
	 */
	public static <B extends BooleanType<B>, T extends RealType<T>> void compute(final RandomAccessibleInterval<B> in,
			final RandomAccessibleInterval<T> out) {

		// tempValues stores the integer values of the first phase, i.e. the
		// first two scans
		final int[][][] tempValues = new int[(int) in.dimension(0)][(int) out.dimension(1)][(int) out.dimension(2)];

		// first phase
		final List<Runnable> list = new ArrayList<>();

		for (int z = 0; z < in.dimension(2); z++) {
			for (int y = 0; y < in.dimension(1); y++) {
				list.add(new Phase1Runnable3D<>(tempValues, in, y, z));
			}
		}

		Parallelization.getTaskExecutor().runAll(list);

		list.clear();

		// second phase
		final int[][][] tempValues_new = new int[(int) in.dimension(0)][(int) out.dimension(1)][(int) out.dimension(2)];
		for (int z = 0; z < in.dimension(2); z++) {
			for (int x = 0; x < in.dimension(0); x++) {
				list.add(new Phase2Runnable3D<>(tempValues, tempValues_new, out, x, z));
			}
		}

		Parallelization.getTaskExecutor().runAll(list);

		// third phase
		for (int x = 0; x < in.dimension(0); x++) {
			for (int y = 0; y < in.dimension(1); y++) {

				list.add(new Phase3Runnable3D<>(tempValues_new, out, x, y));
			}
		}

		Parallelization.getTaskExecutor().runAll(list);
	}
}

class Phase1Runnable3D<B extends BooleanType<B>> implements Runnable {

	private final int[][][] tempValues;
	private final RandomAccess<B> raIn;
	private final int y;
	private final int z;
	private final int infinite;
	private final int width;

	public Phase1Runnable3D(final int[][][] tempValues, final RandomAccessibleInterval<B> raIn, final int yPos,
			final int zPos) {
		this.tempValues = tempValues;
		this.raIn = raIn.randomAccess();
		this.y = yPos;
		this.z = zPos;
		this.infinite = (int) (raIn.dimension(0) + raIn.dimension(1));
		this.width = (int) raIn.dimension(0);
	}

	@Override
	public void run(){
		// scan1
		raIn.setPosition(0, 0);
		raIn.setPosition(y, 1);
		raIn.setPosition(z, 2);
		if (!raIn.get().get()) {
			tempValues[0][y][z] = 0;
		} else {
			tempValues[0][y][z] = infinite;
		}
		for (int x = 1; x < width; x++) {
			raIn.setPosition(x, 0);
			if (!raIn.get().get()) {
				tempValues[x][y][z] = 0;
			} else {
				tempValues[x][y][z] = tempValues[x - 1][y][z] + 1;
			}
		}
		// scan2
		for (int x = width - 2; x >= 0; x--) {
			if (tempValues[x + 1][y][z] < tempValues[x][y][z]) {
				tempValues[x][y][z] = 1 + tempValues[x + 1][y][z];
			}
		}
	}

}

class Phase2Runnable3D<T extends RealType<T>> implements Runnable {

	private final int[][][] tempValues;
	private final int[][][] tempValues_new;
	private final int xPos;
	private final int zPos;
	private final int height;

	public Phase2Runnable3D(final int[][][] tempValues, final int[][][] tempValues_new,
			final RandomAccessibleInterval<T> raOut, final int xPos, final int zPos) {
		this.tempValues = tempValues;
		this.tempValues_new = tempValues_new;
		this.xPos = xPos;
		this.zPos = zPos;
		this.height = (int) raOut.dimension(1);
	}

	// help function used from the algorithm to compute distances
	private int distancefunc(final int x, final int i, final int raOutValue) {
		return (x - i) * (x - i) + raOutValue * raOutValue;
	}

	// help function used from the algorithm
	private double sep(final double i, final double u, final double w, final double v) {
		return (u * u - i * i + w * w - v * v) / (2 * (u - i));
	}

	@Override
	public void run() {
		final int[] s = new int[height];
		final int[] t = new int[height];
		int q = 0;
		s[0] = 0;
		t[0] = 0;

		// scan 3
		for (int u = 1; u < height; u++) {
			while (q >= 0 && distancefunc(t[q], s[q], tempValues[xPos][s[q]][zPos]) > distancefunc(t[q], u,
					tempValues[xPos][u][zPos])) {
				q--;
			}
			if (q < 0) {
				q = 0;
				s[0] = u;
			} else {
				final double w = 1 + sep(s[q], u, tempValues[xPos][u][zPos], tempValues[xPos][s[q]][zPos]);
				if (w < height) {
					q++;
					s[q] = u;
					t[q] = (int) w;
				}
			}
		}

		// scan 4
		for (int u = height - 1; u >= 0; u--) {
			tempValues_new[xPos][u][zPos] = distancefunc(u, s[q], tempValues[xPos][s[q]][zPos]);
			if (u == t[q]) {
				q--;
			}
		}
	}

}

class Phase3Runnable3D<T extends RealType<T>> implements Runnable {

	private final RandomAccessibleInterval<T> raOut;
	private final int[][][] tempValues;
	private final int xPos;
	private final int yPos;
	private final int deep;

	public Phase3Runnable3D(final int[][][] tempValues, final RandomAccessibleInterval<T> raOut, final int xPos,
			final int yPos) {
		this.tempValues = tempValues;
		this.raOut = raOut;
		this.xPos = xPos;
		this.yPos = yPos;
		this.deep = (int) raOut.dimension(2);
	}

	// help function used from the algorithm to compute distances
	private int distancefunc(final int x, final int i, final int raOutValue) {
		return (x - i) * (x - i) + raOutValue;
	}

	// help function used from the algorithm
	private int sep(final int i, final int u, final int w, final int v) {
		return (u * u - i * i + w - v) / (2 * (u - i));
	}

	@Override
	public void run() {
		final int[] s = new int[deep];
		final int[] t = new int[deep];
		int q = 0;
		s[0] = 0;
		t[0] = 0;

		// scan 3
		for (int u = 1; u < deep; u++) {
			while (q >= 0 && distancefunc(t[q], s[q], tempValues[xPos][yPos][s[q]]) > distancefunc(t[q], u,
					tempValues[xPos][yPos][u])) {
				q--;
			}
			if (q < 0) {
				q = 0;
				s[0] = u;
			} else {
				final int w = 1 + sep(s[q], u, tempValues[xPos][yPos][u], tempValues[xPos][yPos][s[q]]);
				if (w < deep) {
					q++;
					s[q] = u;
					t[q] = w;
				}
			}
		}

		// scan 4
		final RandomAccess<T> ra = raOut.randomAccess();
		for (int u = deep - 1; u >= 0; u--) {
			ra.setPosition(xPos, 0);
			ra.setPosition(yPos, 1);
			ra.setPosition(u, 2);
			ra.get().setReal(Math.sqrt(distancefunc(u, s[q], tempValues[xPos][yPos][s[q]])));
			if (u == t[q]) {
				q--;
			}
		}
	}

}
