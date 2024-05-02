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
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.image.AbstractOpTest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test {@code stats.pnorm} op.
 *
 * @author Edward Evans
 */

public class DefaultPNormTest extends AbstractOpTest {

	@Test
	public void testPNorm() {
		final double[] data = { 0.0, 5.082008361816406, 0.0, 1.206267237663269, 0.0,
			6.626776218414307, 0.0, 2.6551482677459717, 0.0, 4.625161170959473 };
		final double[] expected = { 0.5, 0.9999998132675161, 0.5,
			0.8861427670894226, 0.5, 0.9999999999828452, 0.5, 0.9960363221624154, 0.5,
			0.999998128463855 };

		// create input for stats.pnorm Op
		var input = ArrayImgs.doubles(data, 10);
		var pvalue = ArrayImgs.doubles(10);

		// run stats.pnorm op on data
		ops.op("stats.pnorm").input(input).output(pvalue).compute();

		// get random access and results are equal
		final RandomAccess<DoubleType> pRA = pvalue.randomAccess();
		for (int i = 0; i < data.length; i++) {
			pRA.setPosition(i, 0);
			assertEquals(expected[i], pRA.get().getRealDouble());
		}
	}
}
