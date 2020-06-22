/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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
package net.imagej.ops2.image.cooccurrenceMatrix;

import java.util.Arrays;
import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;

/**
 * Calculates coocccurrence matrix from an 2D-{@link RandomAccessibleInterval}.
 * 
 * @author Stephan Sellien (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Andreas Graumann (University of Konstanz)
 */
public class CooccurrenceMatrix2D {

	public static final <T extends RealType<T>> double[][] apply(final RandomAccessibleInterval<T> input,
			final Integer nrGreyLevels, final Integer distance, final Function<RandomAccessibleInterval<T>, Pair<T, T>> minmax,
			final MatrixOrientation orientation) {

		final double[][] output = new double[nrGreyLevels][nrGreyLevels];

		final Pair<T, T> minMax = minmax.apply(input);

		final double localMin = minMax.getA().getRealDouble();
		final double localMax = minMax.getB().getRealDouble();

		final int[][] pixels = new int[(int) input.dimension(1)][(int) input.dimension(0)];

		for (int i = 0; i < pixels.length; i++) {
			Arrays.fill(pixels[i], Integer.MAX_VALUE);
		}

		final int minimumX = (int) input.min(0);
		final int minimumY = (int) input.min(1);
		final double diff = localMax - localMin;
		LoopBuilder.setImages(input, Intervals.positions(input)).multiThreaded().forEachPixel((pixel, pos) -> {
			final int bin = (int) ((pixel.getRealDouble() - localMin) / diff * nrGreyLevels);
			pixels[pos.getIntPosition(1) - minimumY][pos.getIntPosition(0) - minimumX] = bin < nrGreyLevels - 1
					? bin
					: nrGreyLevels - 1;
		});

		int nrPairs = 0;

		final int orientationAtX = orientation.getValueAtDim(0) * distance;
		final int orientationAtY = orientation.getValueAtDim(1) * distance;
		for (int y = 0; y < pixels.length; y++) {
			for (int x = 0; x < pixels[y].length; x++) {
				// ignore pixels not in mask
				if (pixels[y][x] == Integer.MAX_VALUE) {
					continue;
				}

				// // get second pixel
				final int sx = x + orientationAtX;
				final int sy = y + orientationAtY;

				// second pixel in interval and mask
				if (sx >= 0 && sy >= 0 && sy < pixels.length && sx < pixels[sy].length
						&& pixels[sy][sx] != Integer.MAX_VALUE) {
					output[pixels[y][x]][pixels[sy][sx]]++;
					nrPairs++;
				}

			}
		}

		if (nrPairs > 0) {
			double divisor = 1.0 / nrPairs;
			for (int row = 0; row < output.length; row++) {
				for (int col = 0; col < output[row].length; col++) {
					output[row][col] *= divisor;
				}
			}
		}

		return output;
	}
}
