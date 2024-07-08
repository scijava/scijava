/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import org.scijava.concurrent.Parallelization;

/**
 * Computes a distance transform, i.e. for every foreground pixel its distance
 * to the nearest background pixel.
 *
 * @author Simon Schmid (University of Konstanz)
 */
public final class DistanceTransform2D {

	private DistanceTransform2D() {
		// Prevent instantiation of static utility class
	}

	/*
	 * meijsters raster scan algorithm Source:
	 * http://fab.cba.mit.edu/classes/S62.12/docs/Meijster_distance.pdf
	 */
	public static <B extends BooleanType<B>, T extends RealType<T>> void compute(
		final RandomAccessibleInterval<B> in, final RandomAccessibleInterval<T> out)
	{

		// tempValues stores the integer values of the first phase, i.e. the
		// first two scans
		final var tempValues = new int[(int) in.dimension(0)][(int) out
			.dimension(1)];

		// first phase
		final List<Runnable> list = new ArrayList<>();

		for (var y = 0; y < in.dimension(1); y++) {
			list.add(new Phase1Runnable2D<>(tempValues, in, y));
		}

		Parallelization.getTaskExecutor().runAll(list);

		list.clear();

		// second phase
		for (var x = 0; x < in.dimension(0); x++) {
			list.add(new Phase2Runnable2D<>(tempValues, out, x));
		}

		Parallelization.getTaskExecutor().runAll(list);
	}
}

class Phase1Runnable2D<B extends BooleanType<B>> implements Runnable {

	private final int[][] tempValues;
	private final RandomAccess<B> raIn;
	private final int y;
	private final int infinite;
	private final int width;

	public Phase1Runnable2D(final int[][] tempValues,
		final RandomAccessibleInterval<B> raIn, final int yPos)
	{
		this.tempValues = tempValues;
		this.raIn = raIn.randomAccess();
		this.y = yPos;
		this.infinite = (int) (raIn.dimension(0) + raIn.dimension(1));
		this.width = (int) raIn.dimension(0);
	}

	@Override
	public void run() {
		// scan1
		raIn.setPosition(0, 0);
		raIn.setPosition(y, 1);
		if (!raIn.get().get()) {
			tempValues[0][y] = 0;
		}
		else {
			tempValues[0][y] = infinite;
		}
		for (var x = 1; x < width; x++) {
			raIn.setPosition(x, 0);
			if (!raIn.get().get()) {
				tempValues[x][y] = 0;
			}
			else {
				tempValues[x][y] = tempValues[x - 1][y] + 1;
			}
		}
		// scan2
		for (var x = width - 2; x >= 0; x--) {
			if (tempValues[x + 1][y] < tempValues[x][y]) {
				tempValues[x][y] = 1 + tempValues[x + 1][y];
			}

		}
	}

}

class Phase2Runnable2D<T extends RealType<T>> implements Runnable {

	private final RandomAccessibleInterval<T> raOut;
	private final int[][] tempValues;
	private final int xPos;
	private final int height;

	public Phase2Runnable2D(final int[][] tempValues,
		final RandomAccessibleInterval<T> raOut, final int xPos)
	{
		this.tempValues = tempValues;
		this.raOut = raOut;
		this.xPos = xPos;
		this.height = (int) raOut.dimension(1);
	}

	// help function used from the algorithm to compute distances
	private int distancefunc(final int x, final int i, final int raOutValue) {
		return (x - i) * (x - i) + raOutValue * raOutValue;
	}

	// help function used from the algorithm
	private int sep(final int i, final int u, final int w, final int v) {
		return (u * u - i * i + w * w - v * v) / (2 * (u - i));
	}

	@Override
	public void run() {
		final var s = new int[height];
		final var t = new int[height];
        var q = 0;
		s[0] = 0;
		t[0] = 0;

		// scan 3
		for (var u = 1; u < height; u++) {
			while (q >= 0 && distancefunc(t[q], s[q],
				tempValues[xPos][s[q]]) > distancefunc(t[q], u, tempValues[xPos][u]))
			{
				q--;
			}
			if (q < 0) {
				q = 0;
				s[0] = u;
			}
			else {
				final var w = 1 + sep(s[q], u, tempValues[xPos][u],
					tempValues[xPos][s[q]]);
				if (w < height) {
					q++;
					s[q] = u;
					t[q] = w;
				}
			}
		}

		// scan 4
		final var ra = raOut.randomAccess();
		for (var u = height - 1; u >= 0; u--) {
			ra.setPosition(u, 1);
			ra.setPosition(xPos, 0);
			ra.get().setReal(Math.sqrt(distancefunc(u, s[q],
				tempValues[xPos][s[q]])));
			if (u == t[q]) {
				q--;
			}
		}
	}

}
