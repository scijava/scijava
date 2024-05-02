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

package org.scijava.ops.image.features.haralick;

import java.util.function.Function;

import org.scijava.ops.image.image.cooccurrenceMatrix.MatrixOrientation;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

/**
 * Implementation of Information Measure of Correlation 2 Haralick Feature
 *
 * @author Andreas Graumann (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @implNote op names='features.haralick.icm2'
 */
public class DefaultICM2<T extends RealType<T>> extends
	AbstractHaralickFeature<T>
{

	@OpDependency(name = "features.haralick.coocHXY")
	private Function<double[][], double[]> coocHXYFunc;
	@OpDependency(name = "features.haralick.entropy")
	private Functions.Arity4<RandomAccessibleInterval<T>, Integer, Integer, MatrixOrientation, DoubleType> entropy;

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
	public DoubleType apply(final RandomAccessibleInterval<T> input,
		final Integer numGreyLevels, final Integer distance,
		final MatrixOrientation orientation)
	{
		final double[][] matrix = getCooccurrenceMatrix(input, numGreyLevels,
			distance, orientation);

		double res = 0;
		final double[] coochxy = coocHXYFunc.apply(matrix);
		res = Math.sqrt(1 - Math.exp(-2 * (coochxy[3] - entropy.apply(input,
			numGreyLevels, distance, orientation).get())));

		DoubleType output = new DoubleType();
		// if NaN
		if (Double.isNaN(res)) {
			output.set(0);
		}
		else {
			output.set(res);
		}
		return output;
	}
}
