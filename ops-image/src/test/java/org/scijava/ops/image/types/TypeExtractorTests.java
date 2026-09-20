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

package org.scijava.ops.image.types;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsRandomValueFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;
import org.scijava.types.extract.TypeExtractor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Tests various {@link TypeExtractor}s.
 *
 * @author Gabriel Selzer
 */
public class TypeExtractorTests extends AbstractOpTest {

	@Test
	public void testExtendedRAITypeExtractor() {
		Img<DoubleType> data = ArrayImgs.doubles(20, 20);
		// Use an Op to get a histogram
		var eRAI = Views.extendZero(data);
		// Get the generic type (indirectly using the Type reification system through ops)
		Type objType = ops.genericType(eRAI);
		Assertions.assertInstanceOf(ParameterizedType.class, objType);
		ParameterizedType pType = (ParameterizedType) objType;
		// Assert raw type
		Assertions.assertEquals(ExtendedRandomAccessibleInterval.class, pType.getRawType());
		// Assert first type parameter
		Assertions.assertEquals(DoubleType.class, pType.getActualTypeArguments()[0]);
		// Assert second type parameter
		ParameterizedType secondArgType = (ParameterizedType) pType.getActualTypeArguments()[1];
		Assertions.assertEquals(ArrayImg.class, secondArgType.getRawType());
		Assertions.assertEquals(DoubleType.class, secondArgType.getActualTypeArguments()[0]);
		Assertions.assertEquals(DoubleArray.class, secondArgType.getActualTypeArguments()[1]);
	}

	@Test
	public void testHistogram1dTypeExtractor() {
		Img<DoubleType> data = ArrayImgs.doubles(20, 20);
		// Use an Op to get a histogram
		Histogram1d<DoubleType> doubles = ops.op("image.histogram") //
				.input(data) //
				.outType(new Nil<Histogram1d<DoubleType>>() {}) //
				.apply();
		// Get the generic type (indirectly using the Type reification system through ops)
		Type objType = ops.genericType(doubles);
		Assertions.assertInstanceOf(ParameterizedType.class, objType);
		ParameterizedType pType = (ParameterizedType) objType;
		// Assert raw type
		Assertions.assertEquals(Histogram1d.class, pType.getRawType());
		// Assert first type parameter
		Assertions.assertArrayEquals(new Type[] {DoubleType.class}, pType.getActualTypeArguments());
	}

	@Test
	public void testOutOfBoundsConstantValueFactoryTypeExtractors() {
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>> oobf =
			new OutOfBoundsConstantValueFactory<>(new UnsignedByteType(5));
		// Get the generic type (indirectly using the Type reification system through ops)
		Type objType = ops.genericType(oobf);
		Assertions.assertInstanceOf(ParameterizedType.class, objType);
		ParameterizedType pType = (ParameterizedType) objType;
		// Assert raw type
		Assertions.assertEquals(OutOfBoundsConstantValueFactory.class, pType.getRawType());
		// Assert first type parameter
		Assertions.assertEquals(UnsignedByteType.class, pType.getActualTypeArguments()[0]);
		// Assert second type parameter
		ParameterizedType secondArgType = (ParameterizedType) pType.getActualTypeArguments()[1];
		Assertions.assertEquals(RandomAccessibleInterval.class, secondArgType.getRawType());
		Assertions.assertEquals(UnsignedByteType.class, secondArgType.getActualTypeArguments()[0]);
	}

	@Test
	public void testOutOfBoundsRandomValueFactoryTypeExtractors() {
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>> oobf =
			new OutOfBoundsRandomValueFactory<>(new UnsignedByteType(7), 7, 7);
		// Get the generic type (indirectly using the Type reification system through ops)
		Type objType = ops.genericType(oobf);
		Assertions.assertInstanceOf(ParameterizedType.class, objType);
		ParameterizedType pType = (ParameterizedType) objType;
		// Assert raw type
		Assertions.assertEquals(OutOfBoundsRandomValueFactory.class, pType.getRawType());
		// Assert first type parameter
		Assertions.assertEquals(UnsignedByteType.class, pType.getActualTypeArguments()[0]);
		// Assert second type parameter
		ParameterizedType secondArgType = (ParameterizedType) pType.getActualTypeArguments()[1];
		Assertions.assertEquals(RandomAccessibleInterval.class, secondArgType.getRawType());
		Assertions.assertEquals(UnsignedByteType.class, secondArgType.getActualTypeArguments()[0]);
	}

	@Test
	public void testRAITypeExtractor() {
		Img<DoubleType> data = ArrayImgs.doubles(20, 20);
		// Get the generic type (indirectly using the Type reification system through ops)
		Type objType = ops.genericType(data);
		Assertions.assertInstanceOf(ParameterizedType.class, objType);
		ParameterizedType pType = (ParameterizedType) objType;
		// Assert raw type
		Assertions.assertEquals(ArrayImg.class, pType.getRawType());
		// Assert type parameters
		Assertions.assertArrayEquals( //
			new Type[] {DoubleType.class, DoubleArray.class}, //
			pType.getActualTypeArguments() //
		);
	}
}
