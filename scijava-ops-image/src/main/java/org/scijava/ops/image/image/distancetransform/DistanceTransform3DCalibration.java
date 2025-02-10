/*
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
public final class DistanceTransform3DCalibration {

	private DistanceTransform3DCalibration() {
		// Prevent instantiation of static utility class
	}

	/*
	 * meijsters raster scan algorithm Source:
	 * http://fab.cba.mit.edu/classes/S62.12/docs/Meijster_distance.pdf
	 */
	public static <B extends BooleanType<B>, T extends RealType<T>> void compute(
		final RandomAccessibleInterval<B> in, final double[] calibration,
		final RandomAccessibleInterval<T> out)
	{

		// tempValues stores the integer values of the first phase, i.e. the
		// first two scans
		final var tempValues = new double[(int) in.dimension(0)][(int) out
			.dimension(1)][(int) out.dimension(2)];

		// first phase
		final List<Runnable> list = new ArrayList<>();

		for (var z = 0; z < in.dimension(2); z++) {
			for (var y = 0; y < in.dimension(1); y++) {
				list.add(new Phase1Runnable3DCal<>(tempValues, in, y, z, calibration));
			}
		}

		Parallelization.getTaskExecutor().runAll(list);

		list.clear();

		// second phase
		final var tempValues_new = new double[(int) in.dimension(
			0)][(int) out.dimension(1)][(int) out.dimension(2)];
		for (var z = 0; z < in.dimension(2); z++) {
			for (var x = 0; x < in.dimension(0); x++) {
				list.add(new Phase2Runnable3DCal<>(tempValues, tempValues_new, out, x,
					z, calibration));
			}
		}

		Parallelization.getTaskExecutor().runAll(list);

		// third phase
		for (var x = 0; x < in.dimension(0); x++) {
			for (var y = 0; y < in.dimension(1); y++) {

				list.add(new Phase3Runnable3DCal<>(tempValues_new, out, x, y,
					calibration));
			}
		}

		Parallelization.getTaskExecutor().runAll(list);
	}
}

class Phase1Runnable3DCal<B extends BooleanType<B>> implements Runnable {

	private final double[][][] tempValues;
	private final RandomAccess<B> raIn;
	private final int y;
	private final int z;
	private final double infinite;
	private final int width;
	private final double[] calibration;

	public Phase1Runnable3DCal(final double[][][] tempValues,
		final RandomAccessibleInterval<B> raIn, final int yPos, final int zPos,
		final double[] calibration)
	{
		this.tempValues = tempValues;
		this.raIn = raIn.randomAccess();
		this.y = yPos;
		this.z = zPos;
		this.infinite = calibration[0] * raIn.dimension(0) + calibration[1] * raIn
			.dimension(1) + calibration[2] * raIn.dimension(2);
		this.width = (int) raIn.dimension(0);
		this.calibration = calibration;
	}

	@Override
	public void run() {
		// scan1
		raIn.setPosition(0, 0);
		raIn.setPosition(y, 1);
		raIn.setPosition(z, 2);
		if (!raIn.get().get()) {
			tempValues[0][y][z] = 0;
		}
		else {
			tempValues[0][y][z] = infinite;
		}
		for (var x = 1; x < width; x++) {
			raIn.setPosition(x, 0);
			if (!raIn.get().get()) {
				tempValues[x][y][z] = 0;
			}
			else {
				tempValues[x][y][z] = tempValues[x - 1][y][z] + calibration[0];
			}
		}
		// scan2
		for (var x = width - 2; x >= 0; x--) {
			if (tempValues[x + 1][y][z] < tempValues[x][y][z]) {
				tempValues[x][y][z] = calibration[0] + tempValues[x + 1][y][z];
			}
		}
	}
}

class Phase2Runnable3DCal<T extends RealType<T>> implements Runnable {

	private final double[][][] tempValues;
	private final double[][][] tempValues_new;
	private final int xPos;
	private final int zPos;
	private final int height;
	private final double[] calibration;

	public Phase2Runnable3DCal(final double[][][] tempValues,
		final double[][][] tempValues_new, final RandomAccessibleInterval<T> raOut,
		final int xPos, final int zPos, final double[] calibration)
	{
		this.tempValues = tempValues;
		this.tempValues_new = tempValues_new;
		this.xPos = xPos;
		this.zPos = zPos;
		this.height = (int) raOut.dimension(1);
		this.calibration = calibration;
	}

	// help function used from the algorithm to compute distances
	private double distancefunc(final int x, final int i,
		final double raOutValue)
	{
		return calibration[1] * calibration[1] * (x - i) * (x - i) + raOutValue *
			raOutValue;
	}

	// help function used from the algorithm
	private int sep(final int i, final int u, final double w, final double v) {
		return (int) Math.floor(Math.nextUp((u * u - i * i + w * w /
			(calibration[1] * calibration[1]) - v * v / (calibration[1] *
				calibration[1])) / (2 * (u - i))));
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
				tempValues[xPos][s[q]][zPos]) > distancefunc(t[q], u,
					tempValues[xPos][u][zPos]))
			{
				q--;
			}
			if (q < 0) {
				q = 0;
				s[0] = u;
			}
			else {
				final var w = 1 + sep(s[q], u, tempValues[xPos][u][zPos],
					tempValues[xPos][s[q]][zPos]);
				if (w < height) {
					q++;
					s[q] = u;
					t[q] = w;
				}
			}
		}

		// scan 4
		for (var u = height - 1; u >= 0; u--) {
			tempValues_new[xPos][u][zPos] = distancefunc(u, s[q],
				tempValues[xPos][s[q]][zPos]);
			if (u == t[q]) {
				q--;
			}
		}
	}
}

class Phase3Runnable3DCal<T extends RealType<T>> implements Runnable {

	private final RandomAccessibleInterval<T> raOut;
	private final double[][][] tempValues;
	private final int xPos;
	private final int yPos;
	private final int deep;
	private final double[] calibration;

	public Phase3Runnable3DCal(final double[][][] tempValues,
		final RandomAccessibleInterval<T> raOut, final int xPos, final int yPos,
		final double[] calibration)
	{
		this.tempValues = tempValues;
		this.raOut = raOut;
		this.xPos = xPos;
		this.yPos = yPos;
		this.deep = (int) raOut.dimension(2);
		this.calibration = calibration;
	}

	// help function used from the algorithm to compute distances
	private double distancefunc(final int x, final int i,
		final double raOutValue)
	{
		return calibration[2] * calibration[2] * (x - i) * (x - i) + raOutValue;
	}

	// help function used from the algorithm
	private int sep(final int i, final int u, final double w, final double v) {
		return (int) Math.floor(Math.nextUp((u * u - i * i + w / (calibration[2] *
			calibration[2]) - v / (calibration[2] * calibration[2])) / (2 * (u -
				i))));
	}

	@Override
	public void run() {
		final var s = new int[deep];
		final var t = new int[deep];
        var q = 0;
		s[0] = 0;
		t[0] = 0;

		// scan 3
		for (var u = 1; u < deep; u++) {
			while (q >= 0 && distancefunc(t[q], s[q],
				tempValues[xPos][yPos][s[q]]) > distancefunc(t[q], u,
					tempValues[xPos][yPos][u]))
			{
				q--;
			}
			if (q < 0) {
				q = 0;
				s[0] = u;
			}
			else {
				final var w = 1 + sep(s[q], u, tempValues[xPos][yPos][u],
					tempValues[xPos][yPos][s[q]]);
				if (w < deep) {
					q++;
					s[q] = u;
					t[q] = w;
				}
			}
		}

		// scan 4
		final var ra = raOut.randomAccess();
		for (var u = deep - 1; u >= 0; u--) {
			ra.setPosition(xPos, 0);
			ra.setPosition(yPos, 1);
			ra.setPosition(u, 2);
			ra.get().setReal(Math.sqrt(distancefunc(u, s[q],
				tempValues[xPos][yPos][s[q]])));
			if (u == t[q]) {
				q--;
			}
		}
	}
}
