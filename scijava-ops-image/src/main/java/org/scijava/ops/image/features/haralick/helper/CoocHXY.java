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

package org.scijava.ops.image.features.haralick.helper;

import java.util.function.Function;

import org.scijava.ops.spi.OpDependency;

/**
 * NB: Helper class. Internal usage only.
 *
 * @author Andreas Graumann (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @implNote op names='features.haralick.coocHXY'
 */
public class CoocHXY implements Function<double[][], double[]> {

	private static final double EPSILON = Double.MIN_NORMAL;

	@OpDependency(name = "features.haralick.coocPX")
	private Function<double[][], double[]> coocPXFunc;
	@OpDependency(name = "features.haralick.coocPY")
	private Function<double[][], double[]> coocPYFunc;

	/**
	 * TODO
	 *
	 * @param matrix
	 * @return the output
	 */
	@Override
	public double[] apply(double[][] matrix) {
        var hx = 0.0d;
        var hy = 0.0d;
        var hxy1 = 0.0d;
        var hxy2 = 0.0d;

		final var nrGrayLevels = matrix.length;

		final var px = coocPXFunc.apply(matrix);
		final var py = coocPYFunc.apply(matrix);

		for (var i = 0; i < px.length; i++) {
			hx += px[i] * Math.log(px[i] + EPSILON);
		}
		hx = -hx;

		for (var j = 0; j < py.length; j++) {
			hy += py[j] * Math.log(py[j] + EPSILON);
		}
		hy = -hy;
		for (var i = 0; i < nrGrayLevels; i++) {
			for (var j = 0; j < nrGrayLevels; j++) {
				hxy1 += matrix[i][j] * Math.log(px[i] * py[j] + EPSILON);
				hxy2 += px[i] * py[j] * Math.log(px[i] * py[j] + EPSILON);
			}
		}
		hxy1 = -hxy1;
		hxy2 = -hxy2;

		return new double[] { hx, hy, hxy1, hxy2 };
	}
}
