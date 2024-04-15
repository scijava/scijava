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

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

public class MeanLifetimesTest extends AbstractFlimTest {

	@Test
	public void testCalcTauMean() {
		int i = 0;
		final Img<FloatType> tauM = ops.op("flim.tauMean") //
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

			Assertions.assertEquals(exp, pix.get(), TOLERANCE);
			i++;
		}
	}

}
