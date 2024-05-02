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

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.BeforeAll;
import org.scijava.ops.api.OpEnvironment;

import java.util.Random;

/**
 * Common behavior across FLIM analysis tests
 *
 * @author Dasong Gao
 * @author Gabriel Selzer
 */
public abstract class AbstractFlimTest {

	static final FitResults rslt = new FitResults();

	static final long[] DIM = { 2, 3, 7 };
	static final float[] data = new float[(int) (DIM[0] * DIM[1] * DIM[2])];

	static final long SEED = 0x1226;

	static final Random rng = new Random(SEED);

	static final float TOLERANCE = 1e-5f;

	static final OpEnvironment ops = OpEnvironment.build();

	@BeforeAll
	public static void init() {
		// create a image of size DIM filled with random values
		rslt.paramMap = ArrayImgs.floats(DIM);
		rslt.ltAxis = 2;

		int i = 0;
		for (final FloatType pix : rslt.paramMap) {
			data[i] = rng.nextFloat();
			pix.set(data[i++]);
		}
	}

	/**
	 * Gets pixel i of plane p from an ram image of dimension
	 */
	static float getVal(int i, int p) {
		return data[(int) (i + DIM[0] * DIM[1] * p)];
	}

}
