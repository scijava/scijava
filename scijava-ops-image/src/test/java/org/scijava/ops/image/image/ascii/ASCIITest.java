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

package org.scijava.ops.image.image.ascii;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Pair;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Tests {@link org.scijava.ops.image.Ops.Image.ASCII}.
 *
 * @author Leon Yang
 */
public class ASCIITest extends AbstractOpTest {

	@Test
	public void testDefaultASCII() {
		// character set used in DefaultASCII, could be updated if necessary
		final String CHARS = "#O*o+-,. ";
		final int len = CHARS.length();
		final int width = 10;
		final int offset = 0;
		final byte[] array = new byte[width * len];
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < width; j++) {
				array[i * width + j] = (byte) (offset + i * width + j);
			}
		}
		final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(array, width,
			len);
		final Pair<UnsignedByteType, UnsignedByteType> minMax = ops.op(
			"stats.minMax").input(img).outType(
				new Nil<Pair<UnsignedByteType, UnsignedByteType>>()
				{}).apply();
		final String ascii = ops.op("image.ascii").input(img, minMax.getA(), minMax
			.getB()).outType(String.class).apply();
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < width; j++) {
				assertTrue(ascii.charAt(i * (width + 1) + j) == CHARS.charAt(i));
			}
			assertTrue(ascii.charAt(i * (width + 1) + width) == '\n');
		}
	}
}
