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

import org.scijava.ops.image.image.cooccurrenceMatrix.CooccurrenceMatrix2D;
import org.scijava.ops.image.image.cooccurrenceMatrix.MatrixOrientation;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

/**
 * Abstract class for HaralickFeatures.
 *
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 */
public abstract class AbstractHaralickFeature<T extends RealType<T>> implements
	Functions.Arity4<RandomAccessibleInterval<T>, Integer, Integer, MatrixOrientation, DoubleType>
{

	@OpDependency(name = "image.cooccurrenceMatrix")
	private Functions.Arity4<RandomAccessibleInterval<T>, MatrixOrientation, Integer, Integer, double[][]> coocFunc;

	/**
	 * given the specified parameters. No caching!
	 *
	 * @return the {@link CooccurrenceMatrix2D}
	 */
	protected double[][] getCooccurrenceMatrix(
		final RandomAccessibleInterval<T> input, final Integer numGreyLevels,
		final Integer distance, final MatrixOrientation matrixOrientation)
	{
		if (matrixOrientation.numDims() != input.numDimensions())
			throw new IllegalArgumentException(
				"MatrixOrientation must be of the same dimensions as the input!");
		return coocFunc.apply(input, matrixOrientation, numGreyLevels, distance);
	}

}
