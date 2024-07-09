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
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Ops pertaining to fractional contribution calculation
 *
 * @author Dasong Gao
 * @author Gabriel Selzer
 */
public class AmplitudeFractions {

	/**
	 * @param rslt the results from fitting an image
	 * @param index the index
	 * @return a percentage image
	 * @implNote op names="flim.amplitudeFraction", type=Function
	 */
	public static Img<FloatType> defaultAmplitudeFraction( //
		FitResults rslt, //
		int index //
	) {

		RandomAccessibleInterval<FloatType> paramMap = rslt.paramMap;
        var nComp = (int) (paramMap.dimension(rslt.ltAxis) - 1) / 2;

        var dim = new long[paramMap.numDimensions() - 1];
		Views.hyperSlice(paramMap, rslt.ltAxis, 0).dimensions(dim);

        var APercent = ArrayImgs.floats(dim);
        var ASum = ArrayImgs.floats(dim);

		for (var c = 0; c < nComp; c++)
			LoopBuilder.setImages(ASum, getSlice(rslt, c * 2 + 1)) //
				.forEachPixel(FloatType::add);
        var f = new FloatType();
		f.setReal(Float.MIN_VALUE);
		LoopBuilder.setImages(ASum, APercent, getSlice(rslt, index * 2 + 1))
			.forEachPixel((aS, aP, s) -> {
				aP.set(s);
				aP.div(aS);
			});
		return APercent;
	}

	private static IntervalView<FloatType> getSlice(FitResults rslt, int index) {
		return Views.hyperSlice(rslt.paramMap, rslt.ltAxis, index);
	}
}
