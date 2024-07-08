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

package org.scijava.ops.image.image.cooccurrenceMatrix;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;

/**
 * Calculates cooccurrence matrix from an 3D-{@link RandomAccessibleInterval}.
 *
 * @author Stephan Sellien (University of Konstanz)
 * @author Andreas Graumann (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 */
public class CooccurrenceMatrix3D {

	public static final <T extends RealType<T>> double[][] apply(
		final RandomAccessibleInterval<T> input, final Integer nrGreyLevels,
		final Integer distance,
		final Function<RandomAccessibleInterval<T>, Pair<T, T>> minmax,
		final MatrixOrientation orientation)
	{

        var matrix = new double[nrGreyLevels][nrGreyLevels];

		final var minMax = minmax.apply(input);

        var localMin = minMax.getA().getRealDouble();
        var localMax = minMax.getB().getRealDouble();

		final var pixels = new int[(int) input.dimension(2)][(int) input
			.dimension(1)][(int) input.dimension(0)];

		final var minimumX = (int) input.min(0);
		final var minimumY = (int) input.min(1);
		final var minimumZ = (int) input.min(2);

		final var diff = localMax - localMin;
		LoopBuilder.setImages(input, Intervals.positions(input)).multiThreaded()
			.forEachPixel((pixel, pos) -> {
				pixels[pos.getIntPosition(2) - minimumZ][pos.getIntPosition(1) -
					minimumY][pos.getIntPosition(0) - minimumX] = (int) ((pixel
						.getRealDouble() - localMin) / diff * (nrGreyLevels - 1));
			});

		final double orientationAtX = orientation.getValueAtDim(0) * distance;
		final double orientationAtY = orientation.getValueAtDim(1) * distance;
		final double orientationAtZ = orientation.getValueAtDim(2) * distance;

        var nrPairs = 0;
		for (var z = 0; z < pixels.length; z++) {
			for (var y = 0; y < pixels[z].length; y++) {
				for (var x = 0; x < pixels[z][y].length; x++) {

					// ignore pixels not in mask
					if (pixels[z][y][x] == Integer.MAX_VALUE) {
						continue;
					}

					// get second pixel
					final var sx = (int) (x + orientationAtX);
					final var sy = (int) (y + orientationAtY);
					final var sz = (int) (z + orientationAtZ);

					// second pixel in interval and mask
					if (sx >= 0 && sy >= 0 && sz >= 0 && sz < pixels.length &&
						sy < pixels[sz].length && sx < pixels[sz][sy].length &&
						pixels[sz][sy][sx] != Integer.MAX_VALUE)
					{

						matrix[pixels[z][y][x]][pixels[sz][sy][sx]]++;
						nrPairs++;

					}
				}
			}
		}

		// normalize matrix
		if (nrPairs > 0) {
            var divisor = 1.0 / nrPairs;
			for (var row = 0; row < matrix.length; row++) {
				for (var col = 0; col < matrix[row].length; col++) {
					matrix[row][col] *= divisor;
				}
			}
		}

		return matrix;
	}
}
