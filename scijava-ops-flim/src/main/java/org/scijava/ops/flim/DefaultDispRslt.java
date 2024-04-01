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

import net.imglib2.*;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealLUTConverter;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;
import org.scijava.util.ColorRGB;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DefaultDispRslt {

	private DefaultDispRslt() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * @implNote op names="flim.pseudocolor"
	 */
	public static class Pseudocolor implements
		Functions.Arity6<FitResults, Float, Float, Float, Float, ColorTable, Img<ARGBType>>
	{

		@OpDependency(name = "create.img")
		BiFunction<Dimensions, ARGBType, Img<ARGBType>> imgCreator;

		@OpDependency(name = "flim.calcTauMean")
		Function<FitResults, Img<FloatType>> tauMean;

		@OpDependency(name = "transform.stackView")
		Function<List<RandomAccessibleInterval<FloatType>>, RandomAccessibleInterval<FloatType>> stacker;

		@OpDependency(name = "transform.permuteView")
		Functions.Arity3<RandomAccessibleInterval<FloatType>, Integer, Integer, RandomAccessibleInterval<FloatType>> permuter;

		@OpDependency(name = "stats.mean")
		Function<IterableInterval<FloatType>, FloatType> meaner;

		@OpDependency(name = "stats.percentile")
		BiFunction<IterableInterval<FloatType>, Float, FloatType> percentiler;

		/**
		 * @param rslt
		 * @param cMin
		 * @param cMax
		 * @param bMin
		 * @param bMax
		 * @param lut
		 * @return
		 */
		@Override
		public Img<ARGBType> apply(final FitResults rslt, @Nullable Float cMin,
			@Nullable Float cMax, @Nullable Float bMin, @Nullable Float bMax,
			@Nullable ColorTable lut)
		{
			if (lut == null) {
				lut = tri2();
			}
			List<RandomAccessibleInterval<FloatType>> hRaws = new LinkedList<>();
			// List<RandomAccessibleInterval<FloatType>> bRaws = new LinkedList<>();
			int nComp = (int) (rslt.paramMap.dimension(rslt.ltAxis) - 1) / 2;
			for (int c = 0; c < nComp; c++) {
				hRaws.add(Views.hyperSlice(rslt.paramMap, rslt.ltAxis, c * 2 + 2));
				// bRaws.add(Views.hyperSlice(rslt.paramMap, rslt.ltAxis, c * 2 + 1));
			}
			hRaws.add(tauMean.apply(rslt));
			RandomAccessibleInterval<FloatType> hRaw = stacker.apply(hRaws);
			// bRaw = ops().transform().stackView(bRaws);
			RandomAccessibleInterval<FloatType> bRaw = rslt.intensityMap;
			bRaw = rslt.ltAxis <= 0 ? permuter.apply(bRaw, 0, 1) : bRaw;
			bRaw = rslt.ltAxis <= 1 ? permuter.apply(bRaw, 1, 2) : bRaw;
			// min, max = 20%, 80%
			IterableInterval<FloatType> hRawII = Views.iterable(hRaw);
			if (cMin == null) {
				cMin = meaner.apply(hRawII).getRealFloat() * 0.2f;
				System.out.println("color_min automatically set to " + cMin);
			}
			if (cMax == null) {
				cMax = percentiler.apply(hRawII, 95.0f).getRealFloat();
				System.out.println("color_max automatically set to " + cMax);
			}
			// min, max = 0%, 99.5%
			IterableInterval<FloatType> bRawII = Views.iterable(rslt.intensityMap);
			if (bMin == null) {
				bMin = 0f;
				System.out.println("brightness_min automatically set to 0.0");
			}
			if (bMax == null) {
				bMax = percentiler.apply(bRawII, 99.5f).getRealFloat();
				System.out.println("brightness_max automatically set to " + bMax);
			}

			// RealLUTConverter<FloatType> hConverter = new RealLUTConverter<>(cMax,
			// cMin, lut);
			RealLUTConverter<FloatType> hConverter = new RealLUTConverter<>(cMin,
				cMax, lut);
			RandomAccessibleInterval<ARGBType> hImg = Converters.convert(hRaw,
				hConverter, new ARGBType());

			Img<ARGBType> colored = imgCreator.apply(hImg, new ARGBType());
			Cursor<ARGBType> csr = colored.localizingCursor();
			RandomAccess<FloatType> bRA = bRaw.randomAccess();
			RandomAccess<ARGBType> hRA = hImg.randomAccess();
			while (csr.hasNext()) {
				csr.fwd();
				bRA.setPosition(csr);
				bRA.setPosition(0, 2);
				hRA.setPosition(csr);
				float b = Math.min(Math.max(bRA.get().get() - bMin, 0) / (bMax - bMin),
					1);
				// System.out.print(b + " ");
				ARGBType h = hRA.get();
				// b = 1;
				h.mul(b);

				csr.get().set(h);
			}
			System.out.println();

			return colored;
		}
	}

	private static ColorTable8 tri2() {
		final byte[] r = new byte[256], g = new byte[256], b = new byte[256];
		final int[] c = new int[] { 0, 0, 0, 255, 3, 0, 255, 6, 0, 255, 9, 0, 255,
			12, 0, 255, 15, 0, 255, 18, 0, 255, 21, 0, 255, 24, 0, 255, 27, 0, 255,
			30, 0, 255, 33, 0, 255, 36, 0, 255, 39, 0, 255, 42, 0, 255, 45, 0, 255,
			48, 0, 255, 51, 0, 255, 54, 0, 255, 57, 0, 255, 60, 0, 255, 63, 0, 255,
			66, 0, 255, 69, 0, 255, 72, 0, 255, 75, 0, 255, 78, 0, 255, 81, 0, 255,
			84, 0, 255, 87, 0, 255, 90, 0, 255, 93, 0, 255, 96, 0, 255, 99, 0, 255,
			102, 0, 255, 105, 0, 255, 108, 0, 255, 111, 0, 255, 114, 0, 255, 117, 0,
			255, 120, 0, 255, 123, 0, 255, 126, 0, 255, 129, 0, 255, 132, 0, 255, 135,
			0, 255, 138, 0, 255, 141, 0, 255, 144, 0, 255, 147, 0, 255, 150, 0, 255,
			153, 0, 255, 156, 0, 255, 159, 0, 255, 162, 0, 255, 165, 0, 255, 168, 0,
			255, 171, 0, 255, 174, 0, 255, 177, 0, 255, 180, 0, 255, 183, 0, 255, 186,
			0, 255, 189, 0, 255, 192, 0, 255, 195, 0, 255, 198, 0, 255, 201, 0, 255,
			204, 0, 255, 207, 0, 255, 210, 0, 255, 213, 0, 255, 216, 0, 255, 219, 0,
			255, 222, 0, 255, 225, 0, 255, 228, 0, 255, 231, 0, 255, 234, 0, 255, 237,
			0, 255, 240, 0, 255, 243, 0, 255, 246, 0, 255, 249, 0, 255, 252, 0, 255,
			255, 0, 252, 255, 3, 249, 255, 6, 246, 255, 9, 243, 255, 12, 240, 255, 15,
			237, 255, 18, 234, 255, 21, 231, 255, 24, 228, 255, 27, 225, 255, 30, 222,
			255, 33, 219, 255, 36, 216, 255, 39, 213, 255, 42, 210, 255, 45, 207, 255,
			48, 204, 255, 51, 201, 255, 54, 198, 255, 57, 195, 255, 60, 192, 255, 63,
			189, 255, 66, 186, 255, 69, 183, 255, 72, 180, 255, 75, 177, 255, 78, 174,
			255, 81, 171, 255, 84, 168, 255, 87, 165, 255, 90, 162, 255, 93, 159, 255,
			96, 156, 255, 99, 153, 255, 102, 150, 255, 105, 147, 255, 108, 144, 255,
			111, 141, 255, 114, 138, 255, 117, 135, 255, 120, 132, 255, 123, 129, 255,
			126, 126, 255, 129, 123, 255, 132, 120, 255, 135, 117, 255, 138, 114, 255,
			141, 111, 255, 144, 108, 255, 147, 105, 255, 150, 102, 255, 153, 99, 255,
			156, 96, 255, 159, 93, 255, 162, 90, 255, 165, 87, 255, 168, 84, 255, 171,
			81, 255, 174, 78, 255, 177, 75, 255, 180, 72, 255, 183, 69, 255, 186, 66,
			255, 189, 63, 255, 192, 60, 255, 195, 57, 255, 198, 54, 255, 201, 51, 255,
			204, 48, 255, 207, 45, 255, 210, 42, 255, 213, 39, 255, 216, 36, 255, 219,
			33, 255, 222, 30, 255, 225, 27, 255, 228, 24, 255, 231, 21, 255, 234, 18,
			255, 237, 15, 255, 240, 12, 255, 243, 9, 255, 246, 6, 255, 249, 3, 255,
			252, 0, 255, 255, 0, 252, 255, 0, 249, 255, 0, 246, 255, 0, 243, 255, 0,
			240, 255, 0, 237, 255, 0, 234, 255, 0, 231, 255, 0, 228, 255, 0, 225, 255,
			0, 222, 255, 0, 219, 255, 0, 216, 255, 0, 213, 255, 0, 210, 255, 0, 207,
			255, 0, 204, 255, 0, 201, 255, 0, 198, 255, 0, 195, 255, 0, 192, 255, 0,
			189, 255, 0, 186, 255, 0, 183, 255, 0, 180, 255, 0, 177, 255, 0, 174, 255,
			0, 171, 255, 0, 168, 255, 0, 165, 255, 0, 162, 255, 0, 159, 255, 0, 156,
			255, 0, 153, 255, 0, 150, 255, 0, 147, 255, 0, 144, 255, 0, 141, 255, 0,
			138, 255, 0, 135, 255, 0, 132, 255, 0, 129, 255, 0, 126, 255, 0, 123, 255,
			0, 120, 255, 0, 117, 255, 0, 114, 255, 0, 111, 255, 0, 108, 255, 0, 105,
			255, 0, 102, 255, 0, 99, 255, 0, 96, 255, 0, 93, 255, 0, 90, 255, 0, 87,
			255, 0, 84, 255, 0, 81, 255, 0, 78, 255, 0, 75, 255, 0, 72, 255, 0, 69,
			255, 0, 66, 255, 0, 63, 255, 0, 60, 255, 0, 57, 255, 0, 54, 255, 0, 51,
			255, 0, 48, 255, 0, 45, 255, 0, 42, 255, 0, 39, 255, 0, 36, 255, 0, 33,
			255, 0, 30, 255, 0, 27, 255, 0, 24, 255, 0, 21, 255, 0, 18, 255, 0, 15,
			255, 0, 12, 255, 0, 9, 255, 0, 6, 255, 0, 3, 255, 0, 0, 255, };
		// for (int i = 1; i < 256; i++) {
		// final ColorRGB c = ColorRGB.fromHSVColor(i / 255d * 240d / 360d, 1d, 1d);
		// r[i] = (byte) c.getRed();
		// g[i] = (byte) c.getGreen();
		// b[i] = (byte) c.getBlue();
		// }
		for (int i = 0; i < 256; i++) {
			int idx = i * 3;
			r[i] = (byte) c[idx];
			g[i] = (byte) c[idx + 1];
			b[i] = (byte) c[idx + 2];
		}
		return new ColorTable8(r, g, b);
	}

	private static ColorTable8 spci() {
		final byte[] r = new byte[256], g = new byte[256], b = new byte[256];
		for (int i = 0; i < 256; i++) {
			final ColorRGB c = ColorRGB.fromHSVColor((i / 255d * 200d + 20) / 360d,
				1d, 1d);
			r[i] = (byte) c.getRed();
			g[i] = (byte) c.getGreen();
			b[i] = (byte) c.getBlue();
		}
		return new ColorTable8(r, g, b);
	}
}
