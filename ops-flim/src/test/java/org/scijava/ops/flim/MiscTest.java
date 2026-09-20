/*-
 * #%L
 * Fluorescence lifetime analysis in SciJava Ops.
 * %%
 * Copyright (C) 2024 - 2025 SciJava developers.
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

import flimlib.FitFunc;
import flimlib.NoiseType;
import flimlib.RestrainType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Regression tests for miscellaneous components.
 *
 * @author Dasong Gao
 */
public class MiscTest {

	private static final long SEED = 0x1226;

	private static final Random rng = new Random(SEED);

	private static final float TOLERANCE = 1e-5f;

	@Test
	public void testParamSerialization() {
		final FitParams<FloatType> expected = new FitParams<>();
		expected.xInc = rng.nextFloat();
		expected.trans = randFloatArray(64);
		expected.fitEnd = rng.nextInt(expected.trans.length);
		expected.fitStart = rng.nextInt(expected.fitEnd);
		expected.ltAxis = rng.nextInt();
		expected.instr = randFloatArray(16);
		expected.noise = NoiseType.values()[rng.nextInt(NoiseType.values().length)];
		expected.sig = randFloatArray(64);
		expected.nComp = rng.nextInt(3) + 1;
		expected.param = randFloatArray(expected.nComp * 2 + 1);
		expected.paramFree = randBooleanArray(expected.param.length);
		expected.restrain = RestrainType.values()[rng.nextInt(RestrainType
			.values().length)];
		expected.restraintMin = randFloatArray(expected.param.length);
		expected.restraintMax = randFloatArray(expected.param.length);
		expected.fitFunc = new FitFunc[] { FitFunc.GCI_MULTIEXP_LAMBDA,
			FitFunc.GCI_MULTIEXP_TAU, FitFunc.GCI_STRETCHEDEXP }[rng.nextInt(3)];
		expected.chisq_target = rng.nextFloat();
		expected.chisq_delta = rng.nextFloat();
		expected.chisq_percent = rng.nextInt(101);
		expected.iThresh = rng.nextFloat();
		expected.iThreshPercent = rng.nextFloat() * 100;
		expected.multithread = rng.nextBoolean();
		expected.dropBad = rng.nextBoolean();
		expected.getParamMap = rng.nextBoolean();
		expected.getFittedMap = rng.nextBoolean();
		expected.getResidualsMap = rng.nextBoolean();
		expected.getChisqMap = rng.nextBoolean();
		expected.getReturnCodeMap = rng.nextBoolean();

		final FitParams<FloatType> actual = FitParams.fromJSON(expected.toJSON());
		assertEquals(expected.xInc, actual.xInc, TOLERANCE);
		assertNull(actual.trans);
		assertEquals(expected.ltAxis, actual.ltAxis);
		assertEquals(expected.fitStart, actual.fitStart);
		assertEquals(expected.fitEnd, actual.fitEnd);
		assertArrayEquals(expected.instr, actual.instr, TOLERANCE);
		assertEquals(expected.noise, actual.noise);
		assertArrayEquals(expected.sig, actual.sig, TOLERANCE);
		assertEquals(expected.nComp, actual.nComp);
		assertArrayEquals(expected.param, actual.param, TOLERANCE);
		assertArrayEquals(expected.paramFree, actual.paramFree);
		assertEquals(expected.restrain, actual.restrain);
		assertArrayEquals(expected.restraintMin, actual.restraintMin, TOLERANCE);
		assertArrayEquals(expected.restraintMax, actual.restraintMax, TOLERANCE);
		assertEquals(expected.fitFunc, actual.fitFunc);
		assertEquals(expected.chisq_target, actual.chisq_target, TOLERANCE);
		assertEquals(expected.chisq_delta, actual.chisq_delta, TOLERANCE);
		assertEquals(expected.chisq_percent, actual.chisq_percent);
		assertEquals(expected.iThresh, actual.iThresh, TOLERANCE);
		assertEquals(expected.iThreshPercent, actual.iThreshPercent, TOLERANCE);
		assertEquals(expected.multithread, actual.multithread);
		assertEquals(expected.dropBad, actual.dropBad);
		assertEquals(expected.getParamMap, actual.getParamMap);
		assertEquals(expected.getFittedMap, actual.getFittedMap);
		assertEquals(expected.getResidualsMap, actual.getResidualsMap);
		assertEquals(expected.getChisqMap, actual.getChisqMap);
		assertEquals(expected.getReturnCodeMap, actual.getReturnCodeMap);
	}

	private float[] randFloatArray(int length) {
		float[] arr = new float[length];
		for (int i = 0; i < arr.length; i++)
			arr[i] = rng.nextFloat();
		return arr;
	}

	private boolean[] randBooleanArray(int length) {
		boolean[] arr = new boolean[length];
		for (int i = 0; i < arr.length; i++)
			arr[i] = rng.nextBoolean();
		return arr;
	}
}
