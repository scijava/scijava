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

package org.scijava.ops.image.filter.addNoise;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

import java.util.Arrays;
import java.util.List;

public class NoiseAddersTest extends AbstractOpTest {

	@Test
	public void testAddUniformNoiseRegression() {
		var input = ArrayImgs.bytes(2, 2);
		ops.op("image.fill").input(new ByteType((byte) 1)).output(input).compute();
		var output = ArrayImgs.bytes(2, 2);
		var rangeMin = new ByteType((byte) -1);
		var rangeMax = new ByteType((byte) 1);
		ops.op("filter.addUniformNoise").input(input, rangeMin, rangeMax).output(
			output).compute();
		var cursor = output.cursor();
		List<Byte> expected = Arrays.asList((byte) 0, (byte) 2, (byte) 1, (byte) 1);
		for (var e : expected) {
			Assertions.assertEquals(e, cursor.next().get());
		}
		Assertions.assertFalse(cursor.hasNext());
	}
}
