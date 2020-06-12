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
package net.imagej.ops2.features.haralick;

import net.imagej.ops2.image.cooccurrenceMatrix.MatrixOrientation;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * 
 * Implementation of Entropy Haralick Feature Definition: -( sum_{i=1}^q
 * sum_{j=1}^q c(i,j) log(c(i,j)) )
 * 
 * @author Andreas Graumann (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
@Plugin(type = Op.class, name = "features.haralick.entropy")
@Parameter(key = "input")
@Parameter(key = "numGreyLevels")
@Parameter(key = "distance")
@Parameter(key = "matrixOrientation")
@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
public class DefaultEntropy<T extends RealType<T>> extends AbstractHaralickFeature<T> {

	// Avoid log 0
	private static final double EPSILON = Double.MIN_NORMAL;

	@Override
	public DoubleType apply(final IterableInterval<T> input, final Integer numGreyLevels, final Integer distance,
			final MatrixOrientation orientation) {
		final double[][] matrix = getCooccurrenceMatrix(input, numGreyLevels, distance, orientation);
		double res = 0;

		final int nrGrayLevels = matrix.length;

		for (int i = 0; i < nrGrayLevels; i++) {
			for (int j = 0; j < nrGrayLevels; j++) {
				res += matrix[i][j] * Math.log(matrix[i][j] + EPSILON);
			}
		}

		DoubleType output = new DoubleType();
		output.set(-res);
		return output;
	}
}
