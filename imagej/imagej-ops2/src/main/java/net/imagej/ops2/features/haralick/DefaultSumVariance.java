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

package net.imagej.ops2.features.haralick;

import java.util.function.Function;

import net.imagej.ops2.image.cooccurrenceMatrix.MatrixOrientation;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

/**
 * Implementation of Sum Variance Haralick Feature according to
 * http://murphylab.web.cmu.edu/publications/boland/boland_node26.html .
 * 
 * @author Andreas Graumann (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Tim-Oliver Buchholz (University of Konstanz)
 *@implNote op names='features.haralick.sumVariance'
 */
public class DefaultSumVariance<T extends RealType<T>> extends AbstractHaralickFeature<T> {

	@OpDependency(name = "features.haralick.coocPXPlusY")
	private Function<double[][], double[]> coocPXPlusYFunc;
	@SuppressWarnings("rawtypes")
	@OpDependency(name = "features.haralick.sumEntropy")
	private Functions.Arity4<RandomAccessibleInterval<T>, Integer, Integer, MatrixOrientation, RealType> sumEntropyFunc;

	/**
	 * TODO
	 *
	 * @param input
	 * @param numGreyLevels
	 * @param distance
	 * @param matrixOrientation
	 * @return the output
	 */
	@Override
	public DoubleType apply(final RandomAccessibleInterval<T> input, final Integer numGreyLevels, final Integer distance,
			final MatrixOrientation orientation) {
		final double[][] matrix = getCooccurrenceMatrix(input, numGreyLevels, distance, orientation);

		final double[] pxplusy = coocPXPlusYFunc.apply(matrix);
		final int nrGrayLevels = matrix.length;
		final double sumEntropy = sumEntropyFunc.apply(input, numGreyLevels, distance, orientation).getRealDouble();

		double res = 0;
		for (int i = 2; i <= 2 * nrGrayLevels; i++) {
			res += (i - sumEntropy) * (i - sumEntropy) * pxplusy[i];
		}

		DoubleType output = new DoubleType();
		output.set(res);
		return output;
	}
}
