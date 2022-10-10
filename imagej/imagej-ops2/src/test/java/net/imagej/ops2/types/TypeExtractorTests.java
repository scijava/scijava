/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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
package net.imagej.ops2.types;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsRandomValueFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Test;
import org.scijava.types.TypeExtractor;

/**
 * Tests various {@link TypeExtractor}s.
 * 
 * @author Gabriel Selzer
 *
 */
public class TypeExtractorTests extends AbstractOpTest {

	/**
	 * @implNote op names='test.oobcvfTypeExtractor'
	 */
	public final Function<OutOfBoundsConstantValueFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>>, String> func = (
			oobf) -> "oobcvf";

	@Test
	public void testOutOfBoundsConstantValueFactoryTypeExtractors() {
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>> oobf = new OutOfBoundsConstantValueFactory<>(
				new UnsignedByteType(5));

		String output = (String) ops.op("test.oobcvfTypeExtractor").input(oobf).apply();
		// make sure that output matches the return from the Op above, specific to the
		// type of OOBF we passed through.
		assert output.equals("oobcvf");
	}

	// Test Op returns a string different from the one above
	/**
	 * @implNote op names='test.oobrvfTypeExtractor'
	 */
	public final BiFunction<OutOfBoundsRandomValueFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>>, RandomAccessibleInterval<UnsignedByteType>, String> funcRandom = ( oobf, rai) -> "oobrvf"; 
	
	@Test public void testOutOfBoundsRandomValueFactoryTypeExtractors() { 
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>> oobf = new OutOfBoundsRandomValueFactory<>( 
				new UnsignedByteType(7), 7, 7);
		Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(new long[] { 10, 10 }); 
		String output = (String) ops.op("test.oobrvfTypeExtractor").input(oobf, img).apply(); // make sure that output matches the return from the Op above, specific to the // type of OOBF we passed through. 
		assert output.equals("oobrvf"); } 
	}
