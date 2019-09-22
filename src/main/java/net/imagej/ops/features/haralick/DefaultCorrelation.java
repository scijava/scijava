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

import java.util.function.Function;

import net.imagej.ops.image.cooccurrenceMatrix.MatrixOrientation;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * 
 * Implementation of texture correlation haralick feature based on
 * http://earlglynn.github.io/RNotes/package/EBImage/Haralick-Textural-Features.html .
 * 
 * @author Andreas Graumann (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
@Plugin(type = Op.class, name = "features.haralick.correlation")
@Parameter(key = "input")
@Parameter(key = "numGreyLevels")
@Parameter(key = "distance")
@Parameter(key = "matrixOrientation")
@Parameter(key = "output", type = ItemIO.OUTPUT)
public class DefaultCorrelation<T extends RealType<T>> extends
		AbstractHaralickFeature<T> {

	// required functions
	@OpDependency(name = "features.haralick.coocMeanX")
	private Function<double[][], DoubleType> coocMeanXFunc;
	@OpDependency(name = "features.haralick.coocMeanY")
	private Function<double[][], DoubleType> coocMeanYFunc;
	@OpDependency(name = "features.haralick.coocStdY")
	private Function<double[][], DoubleType> coocStdYFunc;
	@OpDependency(name = "features.haralick.coocStdX")
	private Function<double[][], DoubleType> coocStdXFunc;

	@Override
	public DoubleType apply(final IterableInterval<T> input, final Integer numGreyLevels, final Integer distance,
			final MatrixOrientation orientation) {
		final double[][] matrix = getCooccurrenceMatrix(input, numGreyLevels, distance, orientation);

		final int nrGrayLevels = matrix.length;

		final double meanx = coocMeanXFunc.apply(matrix).get();
		final double meany = coocMeanYFunc.apply(matrix).get();
		final double stdx = coocStdXFunc.apply(matrix).get();
		final double stdy = coocStdYFunc.apply(matrix).get();

		double sum = 0;
		for (int i = 0; i < nrGrayLevels; i++) {
			for (int j = 0; j < nrGrayLevels; j++) {
				sum += i*j*matrix[i][j];
			}
		}
		
		DoubleType output = new DoubleType();
		output.set((sum - meanx*meany)/(stdx*stdy));
		return output;
	}

}
