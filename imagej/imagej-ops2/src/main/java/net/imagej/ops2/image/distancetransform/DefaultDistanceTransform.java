/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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
package net.imagej.ops2.image.distancetransform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.concurrent.Parallelization;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IntervalIndexer;

/**
 * Computes a distance transform, i.e. for every foreground pixel its distance
 * to the nearest background pixel.
 * 
 * @author Simon Schmid (University of Konstanz)
 */
public class DefaultDistanceTransform {

	/*
	 * extended version of meijsters raster scan alogrithm to compute n-d inputs
	 * Source: http://fab.cba.mit.edu/classes/S62.12/docs/Meijster_distance.pdf
	 */
	public static <B extends BooleanType<B>, T extends RealType<T>> void compute(final RandomAccessibleInterval<B> in,
			final RandomAccessibleInterval<T> out) {
		// stores the size of each dimension
		final int[] dimensSizes = new int[in.numDimensions()];

		// stores the actual position in the image
		final int[] positions = new int[in.numDimensions()];

		// calculates the number of points in the n-d space
		int numPoints = 1;
		for (int i = 0; i < in.numDimensions(); i++) {
			numPoints *= in.dimension(i);
			dimensSizes[i] = (int) in.dimension(i);
			positions[i] = 0;
		}
		// stores the values calculated after each phase
		final int[] actualValues = new int[numPoints];

		// stores each Thread to execute
		final List<Runnable> list = new ArrayList<>();

		/*
		 * initial phase calculates the first dimension
		 */
		int index = dimensSizes.length - 1;
		list.add(new InitPhase<>(actualValues, in, dimensSizes, positions.clone()));
		while (index > 0) {
			if (positions[index] < dimensSizes[index] - 1) {
				positions[index]++;
				index = positions.length - 1;
				list.add(new InitPhase<>(actualValues, in, dimensSizes, positions.clone()));
			} else {
				positions[index] = 0;
				index--;
			}
		}

		Parallelization.getTaskExecutor().runAll(list);

		list.clear();

		// squared values needed for further calculations
		for (int i = 0; i < actualValues.length; i++) {
			actualValues[i] *= actualValues[i];
		}

		/*
		 * next phases calculates remaining dimensions
		 */
		for (int actualDimension = 1; actualDimension < in.numDimensions(); actualDimension++) {
			Arrays.fill(positions, 0);
			positions[actualDimension] = -1;
			index = positions.length - 1;
			list.add(new NextPhase<>(actualValues, dimensSizes, positions.clone(), actualDimension));
			while (index >= 0) {
				if (positions[index] == -1) {
					index--;
				} else {
					if (positions[index] < dimensSizes[index] - 1) {
						positions[index]++;
						index = positions.length - 1;
						list.add(new NextPhase<>(actualValues, dimensSizes, positions.clone(), actualDimension));
					} else {
						positions[index] = 0;
						index--;
					}
				}
			}

			Parallelization.getTaskExecutor().runAll(list);

			list.clear();
		}

		/*
		 * create output
		 */
		Arrays.fill(positions, 0);
		final RandomAccess<T> raOut = out.randomAccess();
		raOut.setPosition(positions);
		raOut.get().setReal(Math.sqrt(actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)]));
		index = dimensSizes.length - 1;
		while (index >= 0) {
			if (positions[index] < dimensSizes[index] - 1) {
				positions[index]++;
				index = positions.length - 1;
				raOut.setPosition(positions);
				raOut.get().setReal(Math.sqrt(actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)]));
			} else {
				positions[index] = 0;
				index--;
			}
		}
	}
}

class InitPhase<B extends BooleanType<B>, T extends RealType<T>> implements Runnable {
	private final int[] actualValues;
	private final RandomAccess<B> raIn;
	private final int infinite;
	private final int[] dimensSizes;
	private final int[] positions;

	public InitPhase(final int[] actualValues, final RandomAccessibleInterval<B> raIn, final int[] dimensSizes,
			final int[] positions) {
		this.actualValues = actualValues;
		this.raIn = raIn.randomAccess();
		int inf = 0;
		for (int i = 0; i < dimensSizes.length; i++)
			inf += dimensSizes[i];
		this.infinite = inf;
		this.dimensSizes = dimensSizes;
		this.positions = positions;
	}

	@Override
	public void run() {
		// scan1
		positions[0] = 0;
		raIn.setPosition(positions);
		if (!raIn.get().get()) {
			actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)] = 0;
		} else {
			actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)] = infinite;
		}
		for (int x = 1; x < dimensSizes[0]; x++) {
			positions[0] = x;
			raIn.setPosition(positions);
			if (!raIn.get().get()) {
				actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)] = 0;
			} else {
				final int[] temp = positions.clone();
				temp[0] = x - 1;
				actualValues[IntervalIndexer.positionToIndex(positions,
						dimensSizes)] = actualValues[IntervalIndexer.positionToIndex(temp, dimensSizes)] + 1;
			}
		}
		// scan2
		for (int x = dimensSizes[0] - 2; x >= 0; x--) {
			positions[0] = x;
			final int[] temp = positions.clone();
			temp[0] = x + 1;
			if (actualValues[IntervalIndexer.positionToIndex(temp, dimensSizes)] < actualValues[IntervalIndexer
					.positionToIndex(positions, dimensSizes)]) {
				actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)] = 1
						+ actualValues[IntervalIndexer.positionToIndex(temp, dimensSizes)];
			}
		}
	}

}

class NextPhase<T extends RealType<T>> implements Runnable {
	private final int[] actualValues;
	private final int[] dimensSizes;
	private final int[] positions;
	private final int[] positions2;
	private final int actualDimension;

	public NextPhase(final int[] actualValues, final int[] dimensSizes, final int[] positions,
			final int actualDimension) {
		this.actualValues = actualValues;
		this.dimensSizes = dimensSizes;
		this.positions = positions;
		this.positions2 = positions.clone();
		this.actualDimension = actualDimension;
	}

	// help function
	private int distancefunc(final int x, final int i, final int raOutValue) {
		return (x - i) * (x - i) + raOutValue;
	}

	// help function
	private int sep(final int i, final int u, final int w, final int v) {
		return (u * u - i * i + w - v) / (2 * (u - i));
	}

	@Override
	public void run() {
		final int[] s = new int[dimensSizes[actualDimension]];
		final int[] t = new int[dimensSizes[actualDimension]];
		int q = 0;
		s[0] = 0;
		t[0] = 0;

		// scan 3
		for (int u = 1; u < dimensSizes[actualDimension]; u++) {
			positions[actualDimension] = s[q];
			positions2[actualDimension] = u;
			while (q >= 0 && distancefunc(t[q], s[q],
					actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)]) > distancefunc(t[q], u,
							actualValues[IntervalIndexer.positionToIndex(positions2, dimensSizes)])) {
				q--;
				if (q >= 0)
					positions[actualDimension] = s[q];
			}
			if (q < 0) {
				q = 0;
				s[0] = u;
			} else {
				positions[actualDimension] = s[q];
				positions2[actualDimension] = u;
				final double w = 1
						+ sep(s[q], u, actualValues[IntervalIndexer.positionToIndex(positions2, dimensSizes)],
								actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)]);
				if (w < dimensSizes[actualDimension]) {
					q++;
					s[q] = u;
					t[q] = (int) w;
				}
			}
		}

		// scan 4
		final int[] newValues = new int[dimensSizes[actualDimension]];
		for (int u = dimensSizes[actualDimension] - 1; u >= 0; u--) {
			positions[actualDimension] = s[q];
			newValues[u] = distancefunc(u, s[q], actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)]);
			if (u == t[q]) {
				q--;
			}
		}
		for (int u = dimensSizes[actualDimension] - 1; u >= 0; u--) {
			positions[actualDimension] = u;
			actualValues[IntervalIndexer.positionToIndex(positions, dimensSizes)] = newValues[u];
		}
	}

}
