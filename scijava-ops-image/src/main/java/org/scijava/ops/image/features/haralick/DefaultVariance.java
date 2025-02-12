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

package org.scijava.ops.image.features.haralick;

import org.scijava.ops.image.image.cooccurrenceMatrix.MatrixOrientation;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Implementation of Variance Haralick Feature based on
 * http://murphylab.web.cmu.edu/publications/boland/boland_node26.html
 *
 * @author Andreas Graumann (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @implNote op names='features.haralick.variance'
 */
public class DefaultVariance<T extends RealType<T>> extends
	AbstractHaralickFeature<T>
{

	/**
	 * TODO
	 *
	 * @param input
	 * @param numGreyLevels
	 * @param distance
	 * @param orientation
	 * @return the output
	 */
	@Override
	public DoubleType apply(final RandomAccessibleInterval<T> input,
		final Integer numGreyLevels, final Integer distance,
		final MatrixOrientation orientation)
	{
		final var matrix = getCooccurrenceMatrix(input, numGreyLevels,
			distance, orientation);

		final var nrGreyLevel = matrix.length;

        var mean = 0.0;
		for (var i = 0; i < nrGreyLevel; i++) {
			for (var j = 0; j < nrGreyLevel; j++) {
				mean += matrix[i][j];
			}
		}
		mean /= nrGreyLevel * nrGreyLevel;

        var result = 0.0;
		for (var i = 0; i < nrGreyLevel; i++) {
			for (var j = 0; j < nrGreyLevel; j++) {
				result += (i - mean) * (i - mean) * matrix[i][j];
			}
		}

        var output = new DoubleType();
		output.set(result);
		return output;
	}

}
