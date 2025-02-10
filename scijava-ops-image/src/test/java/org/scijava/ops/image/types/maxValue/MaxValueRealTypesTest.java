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

package org.scijava.ops.image.types.maxValue;

import org.scijava.ops.image.AbstractOpTest;
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
 * Regression Tests for {@link MaxValueRealTypes} ops.
 *
 * @author Gabriel Selzer
 */
public class MaxValueRealTypesTest extends AbstractOpTest {

	@Test
	public void testMaxValueBitType() {
		BitType maxValue = ops.op("types.maxValue").input(new BitType()).outType(
			BitType.class).apply();
		BitType expected = new BitType(true);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueBoolType() {
		BoolType maxValue = ops.op("types.maxValue").input(new BoolType()).outType(
			BoolType.class).apply();
		BoolType expected = new BoolType(true);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueNativeBoolType() {
		NativeBoolType maxValue = ops.op("types.maxValue").input(
			new NativeBoolType()).outType(NativeBoolType.class).apply();
		NativeBoolType expected = new NativeBoolType(true);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueByteType() {
		ByteType maxValue = ops.op("types.maxValue").input(new ByteType()).outType(
			ByteType.class).apply();
		ByteType expected = new ByteType(Byte.MAX_VALUE);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueUnsignedByteType() {
		UnsignedByteType maxValue = ops.op("types.maxValue").input(
			new UnsignedByteType()).outType(UnsignedByteType.class).apply();
		UnsignedByteType expected = new UnsignedByteType(-Byte.MIN_VALUE +
			Byte.MAX_VALUE);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueIntType() {
		IntType maxValue = ops.op("types.maxValue").input(new IntType()).outType(
			IntType.class).apply();
		IntType expected = new IntType(Integer.MAX_VALUE);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueUnsignedIntType() {
		UnsignedIntType maxValue = ops.op("types.maxValue").input(
			new UnsignedIntType()).outType(UnsignedIntType.class).apply();
		UnsignedIntType expected = new UnsignedIntType(0xffffffffL);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueLongType() {
		LongType maxValue = ops.op("types.maxValue").input(new LongType()).outType(
			LongType.class).apply();
		LongType expected = new LongType(Long.MAX_VALUE);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueUnsignedLongType() {
		UnsignedLongType maxValue = ops.op("types.maxValue").input(
			new UnsignedLongType()).outType(UnsignedLongType.class).apply();
		UnsignedLongType expected = new UnsignedLongType(new UnsignedLongType()
			.getMaxBigIntegerValue());
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueShortType() {
		ShortType maxValue = ops.op("types.maxValue").input(new ShortType())
			.outType(ShortType.class).apply();
		ShortType expected = new ShortType(Short.MAX_VALUE);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueUnsignedShortType() {
		UnsignedShortType maxValue = ops.op("types.maxValue").input(
			new UnsignedShortType()).outType(UnsignedShortType.class).apply();
		UnsignedShortType expected = new UnsignedShortType(-Short.MIN_VALUE +
			Short.MAX_VALUE);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueUnsigned2BitType() {
		Unsigned2BitType maxValue = ops.op("types.maxValue").input(
			new Unsigned2BitType()).outType(Unsigned2BitType.class).apply();
		Unsigned2BitType expected = new Unsigned2BitType(3);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueUnsigned4BitType() {
		Unsigned4BitType maxValue = ops.op("types.maxValue").input(
			new Unsigned4BitType()).outType(Unsigned4BitType.class).apply();
		Unsigned4BitType expected = new Unsigned4BitType(15);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueUnsigned12BitType() {
		Unsigned12BitType maxValue = ops.op("types.maxValue").input(
			new Unsigned12BitType()).outType(Unsigned12BitType.class).apply();
		Unsigned12BitType expected = new Unsigned12BitType(4095);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueUnsigned128BitType() {
		Unsigned128BitType maxValue = ops.op("types.maxValue").input(
			new Unsigned128BitType()).outType(Unsigned128BitType.class).apply();
		Unsigned128BitType expected = new Unsigned128BitType(
			new Unsigned128BitType().getMaxBigIntegerValue());
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueFloatType() {
		FloatType maxValue = ops.op("types.maxValue").input(new FloatType())
			.outType(FloatType.class).apply();
		FloatType expected = new FloatType(Float.MAX_VALUE);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueDoubleType() {
		DoubleType maxValue = ops.op("types.maxValue").input(new DoubleType())
			.outType(DoubleType.class).apply();
		DoubleType expected = new DoubleType(Double.MAX_VALUE);
		Assertions.assertTrue(maxValue.equals(expected));
	}

	@Test
	public void testMaxValueUnsignedVariableBitLengthType() {
		// bit length of 5
		UnsignedVariableBitLengthType maxValue5 = ops.op("types.maxValue").input(
			new UnsignedVariableBitLengthType(5)).outType(
				UnsignedVariableBitLengthType.class).apply();
		UnsignedVariableBitLengthType expected5 = new UnsignedVariableBitLengthType(
			31, 5);
		Assertions.assertTrue(maxValue5.equals(expected5));

		// - Ensure different bitlength results in different maximum value - //

		// bit length of 17
		UnsignedVariableBitLengthType maxValue17 = ops.op("types.maxValue").input(
			new UnsignedVariableBitLengthType(17)).outType(
				UnsignedVariableBitLengthType.class).apply();
		UnsignedVariableBitLengthType expected17 =
			new UnsignedVariableBitLengthType(131071, 17);
		Assertions.assertTrue(maxValue17.equals(expected17));
	}

}
