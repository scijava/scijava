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
package org.scijava.ops.image.convert;

import net.imglib2.img.array.ArrayImgs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.common3.MersenneTwisterFast;

public class ConvertImagesCorrectnessTest {

	@Test
	public void realTypeToComplexDoubleTypeTest() {
		MersenneTwisterFast mt = new MersenneTwisterFast(0xdeadbeefL);
		int width = 10, height = 10;
		var backing = ArrayImgs.bytes(width, height);
		var converted = RAIWrappers.toComplexDoubleType(backing);

		var cursor = backing.cursor();
		var ra = converted.randomAccess();
		double inc = 3.2;
		byte max = (byte) (Byte.MAX_VALUE - (byte) Math.ceil(inc));
		while(cursor.hasNext()) {
			cursor.next();
			ra.setPosition(cursor);
			byte b = mt.nextByte();
			if (b > max) b = max;
			cursor.get().set(b);
			Assertions.assertEquals(b, ra.get().getRealDouble());
			ra.get().set(b + inc, inc);
			Assertions.assertEquals(b + (byte) inc, cursor.get().get());
			Assertions.assertEquals(0, cursor.get().getImaginaryDouble());
		}
	}

	@Test
	public void complexTypeToComplexDoubleTypeTest() {
		MersenneTwisterFast mt = new MersenneTwisterFast(0xdeadbeefL);
		int width = 10, height = 10;
		var backing = ArrayImgs.complexFloats(width, height);
		var converted = RAIWrappers.toComplexDoubleType(backing);

		var cursor = backing.cursor();
		var ra = converted.randomAccess();
		while(cursor.hasNext()) {
			cursor.next();
			ra.setPosition(cursor);
			var r = mt.nextFloat();
			var i = mt.nextFloat();
			cursor.get().set(r, i);
			Assertions.assertEquals(r, ra.get().getRealDouble(), 1e-6);
			Assertions.assertEquals(i, ra.get().getImaginaryDouble(), 1e-6);
			ra.get().set(r + r, i + i);
			Assertions.assertEquals(r + r, cursor.get().getRealFloat(), 1e-6);
			Assertions.assertEquals(i + i, cursor.get().getImaginaryFloat(), 1e-6);
		}
	}



}
