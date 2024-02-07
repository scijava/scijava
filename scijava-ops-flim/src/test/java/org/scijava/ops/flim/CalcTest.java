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

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.flim.DefaultCalc;
import org.scijava.ops.flim.FitResults;
import org.scijava.types.Nil;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link DefaultCalc} ops.
 *
 * @author Dasong Gao
 */
@SuppressWarnings("unchecked")
public class CalcTest {

	private static FitResults rslt;

	private static long[] DIM = { 2, 3, 7 };
	private static float[] data = new float[(int) (DIM[0] * DIM[1] * DIM[2])];

	private static final Random rng = new Random(0x1226);

	private static final float TOLERANCE = 1e-5f;

	private static OpEnvironment ops;

	@BeforeAll
	public static void init() throws IOException {
		ops = OpEnvironment.build();

		// create a image of size DIM filled with random values
		rslt = new FitResults();
		rslt.paramMap = ArrayImgs.floats(DIM);
		rslt.ltAxis = 2;

		int i = 0;
		for (final FloatType pix : rslt.paramMap) {
			data[i] = rng.nextFloat();
			pix.set(data[i++]);
		}
	}

	@Test
	public void testCalcTauMean() {
		int i = 0;
		final Img<FloatType> tauM = ops.unary("flim.calcTauMean") //
			.input(rslt) //
			.outType(new Nil<Img<FloatType>>()
			{}) //
			.apply();
		for (final FloatType pix : tauM) {
			int nComp = (int) (DIM[2] - 1) / 2;
			float[] AjTauj = new float[nComp];
			float sumAjTauj = 0;

			for (int j = 0; j < nComp; j++) {
				final float Aj = getVal(i, j * 2 + 1);
				final float tauj = getVal(i, j * 2 + 2);
				AjTauj[j] = Aj * tauj;
				sumAjTauj += AjTauj[j];
			}
			float exp = 0;
			for (int j = 0; j < nComp; j++) {
				final float tauj = getVal(i, j * 2 + 2);
				exp += tauj * AjTauj[j] / sumAjTauj;
			}

			assertEquals(exp, pix.get(), TOLERANCE);
			i++;
		}
	}

	@Test
	public void testCalcAPercent() {
		int i = 0;
		final Img<FloatType> A1Perc = ops.binary("flim.calcAPercent") //
			.input(rslt, 0) //
			.outType(new Nil<Img<FloatType>>()
			{}) //
			.apply();
		for (final FloatType pix : A1Perc) {
			int nComp = (int) (DIM[2] - 1) / 2;
			float sumAj = 0;

			for (int j = 0; j < nComp; j++)
				sumAj += getVal(i, j * 2 + 1);
			final float A1 = getVal(i, 1);
			final float exp = A1 / sumAj;

			assertEquals(exp, pix.get(), TOLERANCE);
			i++;
		}
	}

	/**
	 * Gets the i'th pixel of plane p from an ram image of dimension
	 */
	private static float getVal(int i, int p) {
		return data[(int) (i + DIM[0] * DIM[1] * p)];
	}
}
