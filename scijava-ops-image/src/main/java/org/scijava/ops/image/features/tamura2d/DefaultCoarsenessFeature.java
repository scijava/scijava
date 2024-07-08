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

package org.scijava.ops.image.features.tamura2d;

import java.util.ArrayList;
import java.util.HashMap;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Implementation of Tamura's Coarseness feature
 *
 * @author Andreas Graumann (University of Konstanz)
 * @param <I>
 * @param <O>
 * @implNote op names='features.tamura.coarseness'
 */
public class DefaultCoarsenessFeature<I extends RealType<I>, O extends RealType<O>>
	implements Computers.Arity1<RandomAccessibleInterval<I>, O>
{

	@OpDependency(name = "filter.mean")
	private Computers.Arity3<RandomAccessibleInterval<I>, Shape, //
			OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<I>> meanOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param output
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void compute(final RandomAccessibleInterval<I> input, final O output) {
		if (input.numDimensions() != 2) throw new IllegalArgumentException(
			"Only 2 dimensional images allowed!");
        var meanImages = new HashMap<Integer, Img<I>>();

		// get mean images
		for (var i = 1; i <= 5; i++) {
			meanImages.put(i, mean(input, i));
		}

        var maxDifferences = sizedLeadDiffValues(input, meanImages);

        var out = 0.0;
		for (var i : maxDifferences) {
			out += i;
		}

		out /= maxDifferences.size();

		output.set((O) new DoubleType(out));
	}

	/**
	 * For every point calculate differences between the not overlapping
	 * neighborhoods on opposite sides of the point in horizontal and vertical
	 * direction. At each point take the highest difference value when considering
	 * all directions together.
	 *
	 * @param input Input image
	 * @param meanImages Mean images
	 * @return Array containing all leading difference values
	 */
	private ArrayList<Double> sizedLeadDiffValues(
		final RandomAccessibleInterval<I> input,
		final HashMap<Integer, Img<I>> meanImages)
	{

        var pos = new long[input.numDimensions()];
        var dim = new long[input.numDimensions()];
		input.dimensions(dim);

        var maxDifferences = new ArrayList<Double>();
        var cursor = meanImages.get(1).cursor();

		while (cursor.hasNext()) {

			cursor.next();

			// NB: the smallest possible value for maxDiff is 0
			double maxDiff = 0;

			for (var i = 1; i <= 5; i++) {

                var ra1 = meanImages.get(i).randomAccess();
                var ra2 = meanImages.get(i).randomAccess();

				for (var d = 0; d < input.numDimensions(); d++) {

					cursor.localize(pos);

					if (pos[d] + 2 * i + 1 < dim[d]) {

						ra1.setPosition(pos);
                        var val1 = ra1.get().getRealDouble();

						pos[d] += 2 * i + 1;
						ra2.setPosition(pos);
                        var val2 = ra2.get().getRealDouble();

                        var diff = Math.abs(val2 - val1);
						maxDiff = diff >= maxDiff ? diff : maxDiff;
					}
				}
			}

			maxDifferences.add(maxDiff);
		}
		return maxDifferences;
	}

	/**
	 * Apply mean filter with given size of reactangle shape
	 *
	 * @param input Input image
	 * @param i Size of rectangle shape
	 * @return Filtered mean image
	 */
	@SuppressWarnings("unchecked")
	private Img<I> mean(final RandomAccessibleInterval<I> input, final int i) {

        var dims = new long[input.numDimensions()];
		input.dimensions(dims);

		final var array = new byte[(int) Intervals.numElements(new FinalInterval(
			dims))];
        var meanImg = (Img<I>) ArrayImgs.unsignedBytes(array, dims);

        var oobFactory =
			new OutOfBoundsMirrorFactory<I, RandomAccessibleInterval<I>>(Boundary.SINGLE);

		meanOp.compute(input, new RectangleShape(i, true), oobFactory, meanImg);

		return meanImg;
	}

}
