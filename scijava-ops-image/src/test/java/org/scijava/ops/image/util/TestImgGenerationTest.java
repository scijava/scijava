/*-
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

package org.scijava.ops.image.util;

import net.imglib2.Cursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestImgGenerationTest {

	@Test
	public void testVerifyImages() {
		ensureNotEmpty(TestImgGeneration.byteArray(true, 10, 10), new ByteType());
		ensureNotEmpty(TestImgGeneration.unsignedByteArray(true, 10, 10),
			new UnsignedByteType());
		ensureNotEmpty(TestImgGeneration.intArray(true, 10, 10), new IntType());
		ensureNotEmpty(TestImgGeneration.unsignedIntArray(true, 10, 10),
			new UnsignedIntType());
		ensureNotEmpty(TestImgGeneration.floatArray(true, 10, 10), new FloatType());
		ensureNotEmpty(TestImgGeneration.doubleArray(true, 10, 10),
			new DoubleType());
		ensureNotEmpty(TestImgGeneration.bitArray(true, 10, 10), new BitType());
		ensureNotEmpty(TestImgGeneration.longArray(true, 10, 10), new LongType());
		ensureNotEmpty(TestImgGeneration.unsignedLongArray(true, 10, 10),
			new UnsignedLongType());
		ensureNotEmpty(TestImgGeneration.shortArray(true, 10, 10), new ShortType());
		ensureNotEmpty(TestImgGeneration.unsignedShortArray(true, 10, 10),
			new UnsignedShortType());
		ensureNotEmpty(TestImgGeneration.unsigned2BitArray(true, 10, 10),
			new Unsigned2BitType());
		ensureNotEmpty(TestImgGeneration.unsigned4BitArray(true, 10, 10),
			new Unsigned4BitType());
		ensureNotEmpty(TestImgGeneration.unsigned12BitArray(true, 10, 10),
			new Unsigned12BitType());
		ensureNotEmpty(TestImgGeneration.unsigned128BitArray(true, 10, 10),
			new Unsigned128BitType());
	}

	private void ensureNotEmpty(ArrayImg img, NativeType nt) {
		Cursor<?> cursor = img.cursor();
		boolean foundNonZero = false;
		while (cursor.hasNext()) {
			foundNonZero = (!cursor.next().equals(nt)) || foundNonZero;
		}
		assertTrue(foundNonZero, "Randomly generated " + nt.getClass() +
			" array contains all 0's");
	}
}
