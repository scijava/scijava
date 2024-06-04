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

package org.scijava.ops.image.adapt;

import java.util.function.Function;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.spi.OpCollection;
import org.scijava.types.Nil;

public class LiftFunctionsToImgTest extends AbstractOpTest implements
	OpCollection
{

	/**
	 * @input in
	 * @output out
	 * @implNote op names="test.liftFunctionToImg"
	 */
	public final Function<UnsignedByteType, UnsignedByteType> inc = //
		(in) -> new UnsignedByteType(in.get() + 1);

	@Test
	public void testLiftingArity1() {
		Img<UnsignedByteType> foo = TestImgGeneration.unsignedByteArray(true, 10,
			10, 10);
		Img<UnsignedByteType> result = ops.op("test.liftFunctionToImg") //
			//
			.input(foo) //
			.outType(new Nil<Img<UnsignedByteType>>()
			{}) //
			.apply();
		Cursor<UnsignedByteType> cursor = result.localizingCursor();
		RandomAccess<UnsignedByteType> fooRA = foo.randomAccess();
		while (cursor.hasNext()) {
			cursor.next();
			cursor.localize(fooRA);
			Assertions.assertEquals(cursor.get().get(), (fooRA.get().get() + 1) %
				256);
		}
	}

}
