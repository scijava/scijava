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

package org.scijava.ops.image.image.histogram;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Tests {@link HistogramCreate}
 *
 * @author Gabe Selzer
 */
public class DefaultHistogramTest extends AbstractOpTest {

	byte[] data = { 96, 41, 2, 82, 43, 28, 76, 103, 96, 81, 121, 53, 115, 47, 119,
		41, 4, 87, 102, 45, 63, 109, 70, 127, 94, 97, 119, 88, 5, 45, 106, 66, 9,
		50, 102, 76, 49, 51, 57, 127, 21, 33, 0, 127, 88, 21, 61, 99, 41, 113, 117,
		110, 88, 45, 64, 28, 18, 119, 91, 37, 86, 30, 45, 63 };

	/**
	 * Simple regression test. A lot of this is just testing {@link Histogram1d}
	 * however theoretically if {@link HistogramCreate} is changed it would
	 * reflect in the values.
	 */
	@Test
	public void testRegression() {
		Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(data, 8, 8);
		Histogram1d<UnsignedByteType> histogram = ops.op("image.histogram").arity2()
			.input(img, 10).outType(new Nil<Histogram1d<UnsignedByteType>>()
			{}).apply();

		Assertions.assertEquals(false, histogram.hasTails());
		Assertions.assertEquals(0.078125, histogram.relativeFrequency(5, false),
			0.00001d);
		Assertions.assertEquals(10, histogram.getBinCount());

		UnsignedByteType type = new UnsignedByteType();
		histogram.getLowerBound(5, type);
		Assertions.assertEquals(64.0, type.getRealDouble(), 0.00001d);
		histogram.getUpperBound(5, type);
		Assertions.assertEquals(76.0, type.getRealDouble(), 0.00001d);
	}

}
