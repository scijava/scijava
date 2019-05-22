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

package net.imagej.ops.features.haralick;

import net.imagej.ops.image.cooccurrenceMatrix.CooccurrenceMatrix2D;
import net.imagej.ops.image.cooccurrenceMatrix.MatrixOrientation;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.function.Function4;

/**
 * Abstract class for HaralickFeatures.
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 */
public abstract class AbstractHaralickFeature<T extends RealType<T>>
		implements Function4<IterableInterval<T>, Integer, Integer, MatrixOrientation, DoubleType> {

	@OpDependency(name = "image.cooccurrenceMatrix")
	private Function4<IterableInterval<T>, Integer, Integer, MatrixOrientation, double[][]> coocFunc;

	/**
	 * Creates {@link CooccurrenceMatrix2D} from {@link IterableInterval} on demand,
	 * given the specified parameters. No caching!
	 * 
	 * @return the {@link CooccurrenceMatrix2D}
	 */
	protected double[][] getCooccurrenceMatrix(final IterableInterval<T> input, final Integer numGreyLevels,
			final Integer distance, final MatrixOrientation matrixOrientation) {
		if (matrixOrientation.numDims() != input.numDimensions())
			throw new IllegalArgumentException("MatrixOrientation must be of the same dimensions as the input!");
		return coocFunc.apply(input, numGreyLevels, distance, matrixOrientation);
	}

}
