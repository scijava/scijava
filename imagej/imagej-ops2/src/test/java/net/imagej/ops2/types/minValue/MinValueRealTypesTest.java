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
package net.imagej.ops2.types.minValue;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.logic.NativeBoolType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.Unsigned128BitType;
import net.imglib2.type.numeric.integer.Unsigned12BitType;
import net.imglib2.type.numeric.integer.Unsigned2BitType;
import net.imglib2.type.numeric.integer.Unsigned4BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.integer.UnsignedVariableBitLengthType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Regression Tests for {@link MinValueRealTypes} ops.
 * 
 * @author Gabriel Selzer
 */
public class MinValueRealTypesTest extends AbstractOpTest{
	
	@Test
	public void testMinValueBitType() {
		BitType minValue = ops.op("types.minValue").input(new BitType()).outType(BitType.class).apply();
		BitType expected = new BitType(false);
		Assertions.assertTrue(minValue.equals(expected));
	}

	@Test
	public void testMinValueBoolType() {
		BoolType minValue = ops.op("types.minValue").input(new BoolType()).outType(BoolType.class).apply();
		BoolType expected = new BoolType(false);
		Assertions.assertTrue(minValue.equals(expected));
	}

	@Test
	public void testMinValueNativeBoolType() {
		NativeBoolType minValue = ops.op("types.minValue").input(new NativeBoolType()).outType(NativeBoolType.class).apply();
		NativeBoolType expected = new NativeBoolType(false);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueByteType() {
		ByteType minValue = ops.op("types.minValue").input(new ByteType()).outType(ByteType.class).apply();
		ByteType expected = new ByteType(Byte.MIN_VALUE);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueUnsignedByteType() {
		UnsignedByteType minValue = ops.op("types.minValue").input(new UnsignedByteType()).outType(UnsignedByteType.class).apply();
		UnsignedByteType expected = new UnsignedByteType(0);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueIntType() {
		IntType minValue = ops.op("types.minValue").input(new IntType()).outType(IntType.class).apply();
		IntType expected = new IntType(Integer.MIN_VALUE);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueUnsignedIntType() {
		UnsignedIntType minValue = ops.op("types.minValue").input(new UnsignedIntType()).outType(UnsignedIntType.class).apply();
		UnsignedIntType expected = new UnsignedIntType(0);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueLongType() {
		LongType minValue = ops.op("types.minValue").input(new LongType()).outType(LongType.class).apply();
		LongType expected = new LongType(Long.MIN_VALUE);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueUnsignedLongType() {
		UnsignedLongType minValue = ops.op("types.minValue").input(new UnsignedLongType()).outType(UnsignedLongType.class).apply();
		UnsignedLongType expected = new UnsignedLongType(0);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueShortType() {
		ShortType minValue = ops.op("types.minValue").input(new ShortType()).outType(ShortType.class).apply();
		ShortType expected = new ShortType(Short.MIN_VALUE);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueUnsignedShortType() {
		UnsignedShortType minValue = ops.op("types.minValue").input(new UnsignedShortType()).outType(UnsignedShortType.class).apply();
		UnsignedShortType expected = new UnsignedShortType(0);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueUnsigned2BitType() {
		Unsigned2BitType minValue = ops.op("types.minValue").input(new Unsigned2BitType()).outType(Unsigned2BitType.class).apply();
		Unsigned2BitType expected = new Unsigned2BitType(0);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueUnsigned4BitType() {
		Unsigned4BitType minValue = ops.op("types.minValue").input(new Unsigned4BitType()).outType(Unsigned4BitType.class).apply();
		Unsigned4BitType expected = new Unsigned4BitType(0);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueUnsigned12BitType() {
		Unsigned12BitType minValue = ops.op("types.minValue").input(new Unsigned12BitType()).outType(Unsigned12BitType.class).apply();
		Unsigned12BitType expected = new Unsigned12BitType(0);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueUnsigned128BitType() {
		Unsigned128BitType minValue = ops.op("types.minValue").input(new Unsigned128BitType()).outType(Unsigned128BitType.class).apply();
		Unsigned128BitType expected = new Unsigned128BitType(0, 0);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueFloatType() {
		FloatType minValue = ops.op("types.minValue").input(new FloatType()).outType(FloatType.class).apply();
		FloatType expected = new FloatType(Float.MIN_VALUE);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueDoubleType() {
		DoubleType minValue = ops.op("types.minValue").input(new DoubleType()).outType(DoubleType.class).apply();
		DoubleType expected = new DoubleType(Double.MIN_VALUE);
		Assertions.assertTrue(minValue.equals(expected));
	}
	
	@Test
	public void testMinValueUnsignedVariableBitLengthType() {
		// bit length of 5
		UnsignedVariableBitLengthType minValue5 = ops.op("types.minValue").input(new UnsignedVariableBitLengthType(5)).outType(UnsignedVariableBitLengthType.class).apply();
		UnsignedVariableBitLengthType expected5 = new UnsignedVariableBitLengthType(0, 5);
		Assertions.assertTrue(minValue5.equals(expected5));

		// - Ensure different bitlength results in same minimum value - //
		
		// bit length of 17
		UnsignedVariableBitLengthType minValue17 = ops.op("types.minValue").input(new UnsignedVariableBitLengthType(17)).outType(UnsignedVariableBitLengthType.class).apply();
		UnsignedVariableBitLengthType expected17 = new UnsignedVariableBitLengthType(0, 17);
		Assertions.assertTrue(minValue17.equals(expected17));
	}

}

