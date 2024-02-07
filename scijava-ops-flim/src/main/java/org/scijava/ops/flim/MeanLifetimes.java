/*-
 * #%L
 * Fluorescence lifetime analysis in SciJava Ops.
 * %%
 * Copyright (C) 2024 SciJava developers.
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

package org.scijava.ops.flim;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Ops pertaining to mean lifetime calculation
 *
 * @author Dasong Gao
 * @author Gabriel Selzer
 */
public class MeanLifetimes {

	/**
	 * @param rslt the results from fitting an image
	 * @return the mean lifetime
	 * @implNote op names="flim.tauMean", type=Function
	 */
	public static Img<FloatType> defaultMeanLifetime(FitResults rslt) {
		RandomAccessibleInterval<FloatType> paramMap = rslt.paramMap;
		int nComp = (int) (paramMap.dimension(rslt.ltAxis) - 1) / 2;

		long[] dim = new long[paramMap.numDimensions() - 1];
		Views.hyperSlice(paramMap, rslt.ltAxis, 0).dimensions(dim);

		ArrayImg<FloatType, FloatArray> tauM = ArrayImgs.floats(dim);
		ArrayImg<FloatType, FloatArray> tauASum = ArrayImgs.floats(dim);

		// tauM = sum(a_i * tau_i ^ 2), tauASum = sum(a_j * tau_j)
		for (int c = 0; c < nComp; c++) {
			var A = Views.hyperSlice(rslt.paramMap, rslt.ltAxis, c * 2 + 1);
			var tau = Views.hyperSlice(rslt.paramMap, rslt.ltAxis, c * 2 + 2);
			LoopBuilder.setImages(tau, A, tauM, tauASum) //
				.forEachPixel((t, a, tM, tASum) -> {
					FloatType f = new FloatType();
					f.set(a);
					f.mul(t);
					tASum.add(f);
					f.mul(t);
					tM.add(f);
				});
		}
		FloatType f = new FloatType();
		f.setReal(Float.MIN_VALUE);
		LoopBuilder.setImages(tauASum, tauM).forEachPixel((tA, tM) -> {
			tA.add(f);
			tM.div(tA);
		});
		return tauM;
	}

}
