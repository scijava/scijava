/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.stats;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test {@code stats.pnorm} op.
 *
 * @author Edward Evans
 */

public class DefaultPNormTest extends AbstractOpTest {

	@Test
	public void testImg() {
		final int[] xPositions = { 30, 79, 77, 104, 7, 52, 164, 88, 119, 65 };
		final int[] yPositions = { 30, 36, 80, 79, 139, 102, 77, 41, 142, 118 };
		final double[] pvalueExpected = { 0.5, 0.9999998132675161, 0.5,
			0.8861427670894226, 0.5, 0.9999999999828452, 0.5, 0.9960363221624154, 0.5,
			0.999998128463855 };

		// load Z-score heatmap slice
		Img<FloatType> zscore = openFloatImg("zscore_test_data.tif");

		// create p-value image container
		Img<DoubleType> pvalue = ops.op("create.img").input(zscore,
			new DoubleType()).outType(new Nil<Img<DoubleType>>()
		{}).apply();

		// run stats.pnorm op on Z-score data
		ops.op("stats.pnorm").input(zscore).output(pvalue).compute();

		// get random access and compare pixels
		final RandomAccess<DoubleType> pRA = pvalue.randomAccess();

		// assert results are equal
		for (int i = 0; i < xPositions.length; i++) {
			pRA.setPosition(xPositions[i], 0);
			pRA.setPosition(yPositions[i], 1);
			assertEquals(pvalueExpected[i], pRA.get().getRealDouble());
		}
	}
}
