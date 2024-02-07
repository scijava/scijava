/*-
 * #%L
 * Fluorescence lifetime analysis in ImageJ.
 * %%
 * Copyright (C) 2017 - 2022 Board of Regents of the University of Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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

public class DefaultCalc {

//		 * @param imageAdder a {@link Inplaces.Arity2_1} that increments each pixel in the first argument by the corresponding value in the second argument
//		 * @param pixelAdder a {@link Inplaces.Arity2_1} that increments each pixel in the first argument by the second argument
//		 * @param multiplier a {@link BiFunction} that multiplies each pixel in the first argument by the corresponding value in the second argument, storing the result in a new image.
//		 * @param divider an {@link Inplaces.Arity2_1} that divides each pixel in the first argument by the second argument
	/**
	 * @param rslt the results from fitting an image
	 * @return the mean
	 * @implNote op names="flim.calcTauMean", type=Function
	 */
	public static Img<FloatType> caluclateTauMean(
//			@OpDependency(name="math.add") Inplaces.Arity2_1<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> imageAdder,
//			@OpDependency(name="math.add") Inplaces.Arity2_1<RandomAccessibleInterval<FloatType>, FloatType> pixelAdder,
//			@OpDependency(name="math.multiply") BiFunction<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> multiplier,
//			@OpDependency(name="math.divide") Inplaces.Arity2_1<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> divider,
		FitResults rslt)
	{
		RandomAccessibleInterval<FloatType> paramMap = rslt.paramMap;
		int nComp = (int) (paramMap.dimension(rslt.ltAxis) - 1) / 2;

		long[] dim = new long[paramMap.numDimensions() - 1];
		Views.hyperSlice(paramMap, rslt.ltAxis, 0).dimensions(dim);

		ArrayImg<FloatType, FloatArray> tauM = ArrayImgs.floats(dim);
		ArrayImg<FloatType, FloatArray> tauASum = ArrayImgs.floats(dim);

		// tauM = sum(a_i * tau_i ^ 2), tauASum = sum(a_j * tau_j)
		for (int c = 0; c < nComp; c++) {
			IntervalView<FloatType> A = getSlice(rslt, c * 2 + 1);
			IntervalView<FloatType> tau = getSlice(rslt, c * 2 + 2);
			LoopBuilder.setImages(tau, A, tauM, tauASum).forEachPixel((t, a, tM,
				tASum) -> {
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

//	 * @param imageAdder a {@link Inplaces.Arity2_1} that increments each pixel in the first argument by the corresponding value in the second argument
//	 * @param pixelAdder a {@link Inplaces.Arity2_1} that increments each pixel in the first argument by the second argument
//	 * @param divider a {@link BiFunction} that divides each pixel in the first argument by the corresponding value in the second argument, storing the result in a new image.
	/**
	 * @param rslt the results from fitting an image
	 * @param index the index
	 * @return a percentage image
	 * @implNote op names="flim.calcAPercent", type=Function
	 */
	public static Img<FloatType> calcAPercent( //
		FitResults rslt, //
		int index //
	) {

		RandomAccessibleInterval<FloatType> paramMap = rslt.paramMap;
		int nComp = (int) (paramMap.dimension(rslt.ltAxis) - 1) / 2;

		long[] dim = new long[paramMap.numDimensions() - 1];
		Views.hyperSlice(paramMap, rslt.ltAxis, 0).dimensions(dim);

		ArrayImg<FloatType, FloatArray> APercent = ArrayImgs.floats(dim);
		ArrayImg<FloatType, FloatArray> ASum = ArrayImgs.floats(dim);

		for (int c = 0; c < nComp; c++)
			LoopBuilder.setImages(ASum, getSlice(rslt, c * 2 + 1)).forEachPixel(
				FloatType::add);
//			imageAdder.mutate(ASum, getSlice(rslt, c * 2 + 1));
		FloatType f = new FloatType();
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
