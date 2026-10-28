/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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
//

package org.scijava.ops.image.filter.correlate;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

import java.util.Arrays;

/**
 * Tests for {@code filter.correlate} Ops
 *
 * @author Gabriel Selzer
 */
public class CorrelateTest extends AbstractOpTest {

	@Test
	public void testCorrelate() {
		// Create an image with the center pixel set
		var img = ArrayImgs.unsignedBytes(9, 9);
		var impulse = new long[] {4, 4};
		img.randomAccess().setPositionAndGet(impulse).set(1);

		// Create an identity kernel
		var kernel = ArrayImgs.unsignedBytes(5, 5);
		kernel.getAt(2, 2).set(1);

		// Correlate with Ops
		var output = ops.op("filter.correlate").input(img, kernel, new FloatType(), new ComplexFloatType()).apply();
		Assertions.assertInstanceOf(RandomAccessibleInterval.class, output);
		var actual = (RandomAccessibleInterval<FloatType>) output;

		// Check the result
		var cursor = actual.cursor();
		while (cursor.hasNext()) {
			var actualValue = cursor.next().get();
			var pos = cursor.positionAsLongArray();
			// The only pixel that should be set is the center pixel.
			var expected = Arrays.equals(impulse, pos) ? 1 : 0;
			Assertions.assertEquals(expected, actualValue, 1e-6);
		}
	}
}
