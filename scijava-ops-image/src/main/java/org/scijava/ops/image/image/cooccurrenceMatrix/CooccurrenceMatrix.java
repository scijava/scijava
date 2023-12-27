/*-
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
package org.scijava.ops.image.image.cooccurrenceMatrix;

import java.util.function.Function;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

/**
 * Handler Op delegating between {@link CooccurrenceMatrix2D} and
 * {@link CooccurrenceMatrix3D}.
 * 
 * @author Gabriel Selzer
 *
 * @param <T> - the input {@link RealType}.
 *@implNote op names='image.cooccurrenceMatrix'
 */
public class CooccurrenceMatrix<T extends RealType<T>>
		implements Functions.Arity4<RandomAccessibleInterval<T>, Integer, Integer, MatrixOrientation, double[][]> {

	@OpDependency(name = "stats.minMax")
	private Function<RandomAccessibleInterval<T>, Pair<T, T>> minmax;

	/**
	 * TODO
	 *
	 * @param input the input data
	 * @param nrGreyLevels the number of gray levels within the input data
	 * @param distance the <em>number of pixels</em> in the direction specified by
	 *          {@code orientation} to find the co-occurring pixel
	 * @param orientation specifies the offset between the co-occurring pixels.
	 * @return the co-occurence matrix
	 */
	@Override
	public double[][] apply(RandomAccessibleInterval<T> input, Integer nrGreyLevels, Integer distance,
			MatrixOrientation orientation) {
		if (input.numDimensions() == 3 && orientation.isCompatible(3)) {
			return CooccurrenceMatrix3D.apply(input, nrGreyLevels, distance, minmax, orientation);
		} else if (input.numDimensions() == 2 && orientation.isCompatible(2)) {
			return CooccurrenceMatrix2D.apply(input, nrGreyLevels, distance, minmax, orientation);
		} else
			throw new IllegalArgumentException("Only 2 and 3-dimensional inputs are supported!");
	}

}
