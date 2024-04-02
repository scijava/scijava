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

package org.scijava.ops.image.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
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
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Tests the {@link ConvertTypes} ops.
 *
 * @author Alison Walter
 */
public class TestConvertType extends AbstractOpTest {

	private final BigInteger p64 = new BigInteger("AEF234567ABCD123", 16);
	private final BigInteger n64 = new BigInteger("-1399890AB", 16);
	private final BigInteger beef = BigInteger.valueOf(0xbeef);
	private final BigInteger biZero = BigInteger.ZERO;
	private final BigInteger p128 = new BigInteger(
		"2CAFE0321BEEF0717BABE0929DEAD0311", 16);
	private final BigInteger n128 = new BigInteger(
		"-482301498A285BFD0982EE7DE02398BC9080459284CCDE90E9F0D00C043981210481AAADEF2",
		16);

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testBitToBit() {

		final BitType b = new BitType(true);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(1), result.get());

		b.set(false);
		assertEquals(Types.bit(0), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint2ToBit() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(2), result.get());

		b.set(0);
		assertEquals(Types.bit(0), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint4ToBit() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(15), result.get());

		b.set(0);
		assertEquals(Types.bit(0), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testInt8ToBit() {

		final ByteType b = new ByteType((byte) 8);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.bit((byte) 0), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.bit((byte) -12), ops.unary("convert.bit").input(b)
			.outType(BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint8ToBit() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(100), result.get());

		b.set(0);
		assertEquals(Types.bit(0), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint12ToBit() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(212L), result.get());

		b.set(0L);
		assertEquals(Types.bit(0L), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testInt16ToBit() {

		final ShortType b = new ShortType((short) 52);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.bit((short) 0), ops.unary("convert.bit").input(b)
			.outType(BitType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.bit((short) -154), ops.unary("convert.bit").input(b)
			.outType(BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint16ToBit() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(480), result.get());

		b.set(0);
		assertEquals(Types.bit(0), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testInt32ToBit() {

		final IntType b = new IntType(301);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(301), result.get());

		b.set(0);
		assertEquals(Types.bit(0), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

		b.set(-89);
		assertEquals(Types.bit(-89), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint32ToBit() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(20L), result.get());

		b.set(0L);
		assertEquals(Types.bit(0L), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testInt64ToBit() {

		final LongType b = new LongType(891L);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(891L), result.get());

		b.set(0L);
		assertEquals(Types.bit(0L), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.bit(-1024L), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint64ToBit() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(1049L), result.get());

		b.set(0L);
		assertEquals(Types.bit(0L), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

		b.set(p64);
		assertEquals(Types.bit(p64), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint128ToBit() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(beef), result.get());

		b.set(biZero);
		assertEquals(Types.bit(biZero), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

		b.set(p128);
		assertEquals(Types.bit(p128), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testFloat32ToBit() {

		final FloatType b = new FloatType(123453.125f);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.bit(0f), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.bit(-2523485349058.0f), ops.unary("convert.bit").input(b)
			.outType(BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testCfloat32ToBit() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.bit(0f), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.bit(-4.25f), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testFloat64ToBit() {

		final DoubleType b = new DoubleType(4098d);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(4098d), result.get());

		b.set(0d);
		assertEquals(Types.bit(0d), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.bit(-10948.015625d), ops.unary("convert.bit").input(b)
			.outType(BitType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.bit(1.0000152587890625e20), ops.unary("convert.bit")
			.input(b).outType(BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testCfloat64ToBit() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final BitType result = ops.unary("convert.bit").input(b).outType(
			BitType.class).apply();
		assertEquals(Types.bit(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.bit(0d), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.bit(-234.25d), ops.unary("convert.bit").input(b).outType(
			BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testBitToUint2() {

		final BitType b = new BitType(true);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(1), result.get());

		b.set(false);
		assertEquals(Types.uint2(0), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint2ToUint2() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(2), result.get());

		b.set(0);
		assertEquals(Types.uint2(0), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint4ToUint2() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(15), result.get());

		b.set(0);
		assertEquals(Types.uint2(0), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testInt8ToUint2() {

		final ByteType b = new ByteType((byte) 8);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.uint2((byte) 0), ops.unary("convert.uint2").input(b)
			.outType(Unsigned2BitType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.uint2((byte) -12), ops.unary("convert.uint2").input(b)
			.outType(Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint8ToUint2() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(100), result.get());

		b.set(0);
		assertEquals(Types.uint2(0), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint12ToUint2() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(212L), result.get());

		b.set(0L);
		assertEquals(Types.uint2(0L), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testInt16ToUint2() {

		final ShortType b = new ShortType((short) 52);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.uint2((short) 0), ops.unary("convert.uint2").input(b)
			.outType(Unsigned2BitType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.uint2((short) -154), ops.unary("convert.uint2").input(b)
			.outType(Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint16ToUint2() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(480), result.get());

		b.set(0);
		assertEquals(Types.uint2(0), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testInt32ToUint2() {

		final IntType b = new IntType(301);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(301), result.get());

		b.set(0);
		assertEquals(Types.uint2(0), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

		b.set(-89);
		assertEquals(Types.uint2(-89), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint32ToUint2() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(20L), result.get());

		b.set(0L);
		assertEquals(Types.uint2(0L), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testInt64ToUint2() {

		final LongType b = new LongType(891L);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(891L), result.get());

		b.set(0L);
		assertEquals(Types.uint2(0L), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.uint2(-1024L), ops.unary("convert.uint2").input(b)
			.outType(Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint64ToUint2() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(1049L), result.get());

		b.set(0L);
		assertEquals(Types.uint2(0L), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

		b.set(p64);
		assertEquals(Types.uint2(p64), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint128ToUint2() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(beef), result.get());

		b.set(biZero);
		assertEquals(Types.uint2(biZero), ops.unary("convert.uint2").input(b)
			.outType(Unsigned2BitType.class).apply().get());

		b.set(p128);
		assertEquals(Types.uint2(p128), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testFloat32ToUint2() {

		final FloatType b = new FloatType(123453.125f);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.uint2(0f), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.uint2(-2523485349058.0f), ops.unary("convert.uint2")
			.input(b).outType(Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testCfloat32ToUint2() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.uint2(0f), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.uint2(-4.25f), ops.unary("convert.uint2").input(b)
			.outType(Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testFloat64ToUint2() {

		final DoubleType b = new DoubleType(4098d);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(4098d), result.get());

		b.set(0d);
		assertEquals(Types.uint2(0d), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.uint2(-10948.015625d), ops.unary("convert.uint2").input(
			b).outType(Unsigned2BitType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.uint2(1.0000152587890625e20), ops.unary("convert.uint2")
			.input(b).outType(Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testCfloat64ToUint2() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final Unsigned2BitType result = ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply();
		assertEquals(Types.uint2(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.uint2(0d), ops.unary("convert.uint2").input(b).outType(
			Unsigned2BitType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.uint2(-234.25d), ops.unary("convert.uint2").input(b)
			.outType(Unsigned2BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testBitToUint4() {

		final BitType b = new BitType(true);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(1), result.get());

		b.set(false);
		assertEquals(Types.uint4(0), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint2ToUint4() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(2), result.get());

		b.set(0);
		assertEquals(Types.uint4(0), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint4ToUint4() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(15), result.get());

		b.set(0);
		assertEquals(Types.uint4(0), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testInt8ToUint4() {

		final ByteType b = new ByteType((byte) 8);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.uint4((byte) 0), ops.unary("convert.uint4").input(b)
			.outType(Unsigned4BitType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.uint4((byte) -12), ops.unary("convert.uint4").input(b)
			.outType(Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint8ToUint4() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(100), result.get());

		b.set(0);
		assertEquals(Types.uint4(0), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint12ToUint4() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(212L), result.get());

		b.set(0L);
		assertEquals(Types.uint4(0L), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testInt16ToUint4() {

		final ShortType b = new ShortType((short) 52);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.uint4((short) 0), ops.unary("convert.uint4").input(b)
			.outType(Unsigned4BitType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.uint4((short) -154), ops.unary("convert.uint4").input(b)
			.outType(Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint16ToUint4() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(480), result.get());

		b.set(0);
		assertEquals(Types.uint4(0), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testInt32ToUint4() {

		final IntType b = new IntType(301);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(301), result.get());

		b.set(0);
		assertEquals(Types.uint4(0), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

		b.set(-89);
		assertEquals(Types.uint4(-89), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint32ToUint4() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(20L), result.get());

		b.set(0L);
		assertEquals(Types.uint4(0L), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testInt64ToUint4() {

		final LongType b = new LongType(891L);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(891L), result.get());

		b.set(0L);
		assertEquals(Types.uint4(0L), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.uint4(-1024L), ops.unary("convert.uint4").input(b)
			.outType(Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint64ToUint4() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(1049L), result.get());

		b.set(0L);
		assertEquals(Types.uint4(0L), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

		b.set(p64);
		assertEquals(Types.uint4(p64), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint128ToUint4() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(beef), result.get());

		b.set(biZero);
		assertEquals(Types.uint4(biZero), ops.unary("convert.uint4").input(b)
			.outType(Unsigned4BitType.class).apply().get());

		b.set(p128);
		assertEquals(Types.uint4(p128), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testFloat32ToUint4() {

		final FloatType b = new FloatType(123453.125f);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.uint4(0f), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.uint4(-2523485349058.0f), ops.unary("convert.uint4")
			.input(b).outType(Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testCfloat32ToUint4() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.uint4(0f), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.uint4(-4.25f), ops.unary("convert.uint4").input(b)
			.outType(Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testFloat64ToUint4() {

		final DoubleType b = new DoubleType(4098d);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(4098d), result.get());

		b.set(0d);
		assertEquals(Types.uint4(0d), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.uint4(-10948.015625d), ops.unary("convert.uint4").input(
			b).outType(Unsigned4BitType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.uint4(1.0000152587890625e20), ops.unary("convert.uint4")
			.input(b).outType(Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testCfloat64ToUint4() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final Unsigned4BitType result = ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply();
		assertEquals(Types.uint4(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.uint4(0d), ops.unary("convert.uint4").input(b).outType(
			Unsigned4BitType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.uint4(-234.25d), ops.unary("convert.uint4").input(b)
			.outType(Unsigned4BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testBitToInt8() {

		final BitType b = new BitType(true);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(1), result.get());

		b.set(false);
		assertEquals(Types.int8(0), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint2ToInt8() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(2), result.get());

		b.set(0);
		assertEquals(Types.int8(0), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint4ToInt8() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(15), result.get());

		b.set(0);
		assertEquals(Types.int8(0), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testInt8ToInt8() {

		final ByteType b = new ByteType((byte) 8);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.int8((byte) 0), ops.unary("convert.int8").input(b)
			.outType(ByteType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.int8((byte) -12), ops.unary("convert.int8").input(b)
			.outType(ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint8ToInt8() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(100), result.get());

		b.set(0);
		assertEquals(Types.int8(0), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint12ToInt8() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(212L), result.get());

		b.set(0L);
		assertEquals(Types.int8(0L), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testInt16ToInt8() {

		final ShortType b = new ShortType((short) 52);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.int8((short) 0), ops.unary("convert.int8").input(b)
			.outType(ByteType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.int8((short) -154), ops.unary("convert.int8").input(b)
			.outType(ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint16ToInt8() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(480), result.get());

		b.set(0);
		assertEquals(Types.int8(0), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testInt32ToInt8() {

		final IntType b = new IntType(301);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(301), result.get());

		b.set(0);
		assertEquals(Types.int8(0), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

		b.set(-89);
		assertEquals(Types.int8(-89), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint32ToInt8() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(20L), result.get());

		b.set(0L);
		assertEquals(Types.int8(0L), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testInt64ToInt8() {

		final LongType b = new LongType(891L);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(891L), result.get());

		b.set(0L);
		assertEquals(Types.int8(0L), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.int8(-1024L), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint64ToInt8() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(1049L), result.get());

		b.set(0L);
		assertEquals(Types.int8(0L), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

		b.set(p64);
		assertEquals(Types.int8(p64), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint128ToInt8() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(beef), result.get());

		b.set(biZero);
		assertEquals(Types.int8(biZero), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

		b.set(p128);
		assertEquals(Types.int8(p128), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testFloat32ToInt8() {

		final FloatType b = new FloatType(123453.125f);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.int8(0f), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.int8(-2523485349058.0f), ops.unary("convert.int8").input(
			b).outType(ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testCfloat32ToInt8() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.int8(0f), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.int8(-4.25f), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testFloat64ToInt8() {

		final DoubleType b = new DoubleType(4098d);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(4098d), result.get());

		b.set(0d);
		assertEquals(Types.int8(0d), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.int8(-10948.015625d), ops.unary("convert.int8").input(b)
			.outType(ByteType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.int8(1.0000152587890625e20), ops.unary("convert.int8")
			.input(b).outType(ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testCfloat64ToInt8() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final ByteType result = ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply();
		assertEquals(Types.int8(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.int8(0d), ops.unary("convert.int8").input(b).outType(
			ByteType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.int8(-234.25d), ops.unary("convert.int8").input(b)
			.outType(ByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testBitToUint8() {

		final BitType b = new BitType(true);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(1), result.get());

		b.set(false);
		assertEquals(Types.uint8(0), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint2ToUint8() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(2), result.get());

		b.set(0);
		assertEquals(Types.uint8(0), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint4ToUint8() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(15), result.get());

		b.set(0);
		assertEquals(Types.uint8(0), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testInt8ToUint8() {

		final ByteType b = new ByteType((byte) 8);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.uint8((byte) 0), ops.unary("convert.uint8").input(b)
			.outType(UnsignedByteType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.uint8((byte) -12), ops.unary("convert.uint8").input(b)
			.outType(UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint8ToUint8() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(100), result.get());

		b.set(0);
		assertEquals(Types.uint8(0), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint12ToUint8() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(212L), result.get());

		b.set(0L);
		assertEquals(Types.uint8(0L), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testInt16ToUint8() {

		final ShortType b = new ShortType((short) 52);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.uint8((short) 0), ops.unary("convert.uint8").input(b)
			.outType(UnsignedByteType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.uint8((short) -154), ops.unary("convert.uint8").input(b)
			.outType(UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint16ToUint8() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(480), result.get());

		b.set(0);
		assertEquals(Types.uint8(0), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testInt32ToUint8() {

		final IntType b = new IntType(301);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(301), result.get());

		b.set(0);
		assertEquals(Types.uint8(0), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

		b.set(-89);
		assertEquals(Types.uint8(-89), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint32ToUint8() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(20L), result.get());

		b.set(0L);
		assertEquals(Types.uint8(0L), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testInt64ToUint8() {

		final LongType b = new LongType(891L);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(891L), result.get());

		b.set(0L);
		assertEquals(Types.uint8(0L), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.uint8(-1024L), ops.unary("convert.uint8").input(b)
			.outType(UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint64ToUint8() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(1049L), result.get());

		b.set(0L);
		assertEquals(Types.uint8(0L), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

		b.set(p64);
		assertEquals(Types.uint8(p64), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint128ToUint8() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(beef), result.get());

		b.set(biZero);
		assertEquals(Types.uint8(biZero), ops.unary("convert.uint8").input(b)
			.outType(UnsignedByteType.class).apply().get());

		b.set(p128);
		assertEquals(Types.uint8(p128), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testFloat32ToUint8() {

		final FloatType b = new FloatType(123453.125f);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.uint8(0f), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.uint8(-2523485349058.0f), ops.unary("convert.uint8")
			.input(b).outType(UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testCfloat32ToUint8() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.uint8(0f), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.uint8(-4.25f), ops.unary("convert.uint8").input(b)
			.outType(UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testFloat64ToUint8() {

		final DoubleType b = new DoubleType(4098d);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(4098d), result.get());

		b.set(0d);
		assertEquals(Types.uint8(0d), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.uint8(-10948.015625d), ops.unary("convert.uint8").input(
			b).outType(UnsignedByteType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.uint8(1.0000152587890625e20), ops.unary("convert.uint8")
			.input(b).outType(UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testCfloat64ToUint8() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final UnsignedByteType result = ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply();
		assertEquals(Types.uint8(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.uint8(0d), ops.unary("convert.uint8").input(b).outType(
			UnsignedByteType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.uint8(-234.25d), ops.unary("convert.uint8").input(b)
			.outType(UnsignedByteType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testBitToUint12() {

		final BitType b = new BitType(true);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(1), result.get());

		b.set(false);
		assertEquals(Types.uint12(0), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint2ToUint12() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(2), result.get());

		b.set(0);
		assertEquals(Types.uint12(0), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint4ToUint12() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(15), result.get());

		b.set(0);
		assertEquals(Types.uint12(0), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testInt8ToUint12() {

		final ByteType b = new ByteType((byte) 8);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.uint12((byte) 0), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.uint12((byte) -12), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint8ToUint12() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(100), result.get());

		b.set(0);
		assertEquals(Types.uint12(0), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint12ToUint12() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(212L), result.get());

		b.set(0L);
		assertEquals(Types.uint12(0L), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testInt16ToUint12() {

		final ShortType b = new ShortType((short) 52);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.uint12((short) 0), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.uint12((short) -154), ops.unary("convert.uint12").input(
			b).outType(Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint16ToUint12() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(480), result.get());

		b.set(0);
		assertEquals(Types.uint12(0), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testInt32ToUint12() {

		final IntType b = new IntType(301);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(301), result.get());

		b.set(0);
		assertEquals(Types.uint12(0), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

		b.set(-89);
		assertEquals(Types.uint12(-89), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint32ToUint12() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(20L), result.get());

		b.set(0L);
		assertEquals(Types.uint12(0L), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testInt64ToUint12() {

		final LongType b = new LongType(891L);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(891L), result.get());

		b.set(0L);
		assertEquals(Types.uint12(0L), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.uint12(-1024L), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint64ToUint12() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(1049L), result.get());

		b.set(0L);
		assertEquals(Types.uint12(0L), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

		b.set(p64);
		assertEquals(Types.uint12(p64), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint128ToUint12() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(beef), result.get());

		b.set(biZero);
		assertEquals(Types.uint12(biZero), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

		b.set(p128);
		assertEquals(Types.uint12(p128), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testFloat32ToUint12() {

		final FloatType b = new FloatType(123453.125f);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.uint12(0f), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.uint12(-2523485349058.0f), ops.unary("convert.uint12")
			.input(b).outType(Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testCfloat32ToUint12() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.uint12(0f), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.uint12(-4.25f), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testFloat64ToUint12() {

		final DoubleType b = new DoubleType(4098d);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(4098d), result.get());

		b.set(0d);
		assertEquals(Types.uint12(0d), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.uint12(-10948.015625d), ops.unary("convert.uint12")
			.input(b).outType(Unsigned12BitType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.uint12(1.0000152587890625e20), ops.unary(
			"convert.uint12").input(b).outType(Unsigned12BitType.class).apply()
			.get());

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testCfloat64ToUint12() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final Unsigned12BitType result = ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply();
		assertEquals(Types.uint12(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.uint12(0d), ops.unary("convert.uint12").input(b).outType(
			Unsigned12BitType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.uint12(-234.25d), ops.unary("convert.uint12").input(b)
			.outType(Unsigned12BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testBitToInt16() {

		final BitType b = new BitType(true);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(1), result.get());

		b.set(false);
		assertEquals(Types.int16(0), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint2ToInt16() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(2), result.get());

		b.set(0);
		assertEquals(Types.int16(0), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint4ToInt16() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(15), result.get());

		b.set(0);
		assertEquals(Types.int16(0), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testInt8ToInt16() {

		final ByteType b = new ByteType((byte) 8);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.int16((byte) 0), ops.unary("convert.int16").input(b)
			.outType(ShortType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.int16((byte) -12), ops.unary("convert.int16").input(b)
			.outType(ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint8ToInt16() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(100), result.get());

		b.set(0);
		assertEquals(Types.int16(0), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint12ToInt16() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(212L), result.get());

		b.set(0L);
		assertEquals(Types.int16(0L), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testInt16ToInt16() {

		final ShortType b = new ShortType((short) 52);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.int16((short) 0), ops.unary("convert.int16").input(b)
			.outType(ShortType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.int16((short) -154), ops.unary("convert.int16").input(b)
			.outType(ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint16ToInt16() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(480), result.get());

		b.set(0);
		assertEquals(Types.int16(0), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testInt32ToInt16() {

		final IntType b = new IntType(301);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(301), result.get());

		b.set(0);
		assertEquals(Types.int16(0), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

		b.set(-89);
		assertEquals(Types.int16(-89), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint32ToInt16() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(20L), result.get());

		b.set(0L);
		assertEquals(Types.int16(0L), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testInt64ToInt16() {

		final LongType b = new LongType(891L);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(891L), result.get());

		b.set(0L);
		assertEquals(Types.int16(0L), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.int16(-1024L), ops.unary("convert.int16").input(b)
			.outType(ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint64ToInt16() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(1049L), result.get());

		b.set(0L);
		assertEquals(Types.int16(0L), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

		b.set(p64);
		assertEquals(Types.int16(p64), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint128ToInt16() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(beef), result.get());

		b.set(biZero);
		assertEquals(Types.int16(biZero), ops.unary("convert.int16").input(b)
			.outType(ShortType.class).apply().get());

		b.set(p128);
		assertEquals(Types.int16(p128), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testFloat32ToInt16() {

		final FloatType b = new FloatType(123453.125f);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.int16(0f), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.int16(-2523485349058.0f), ops.unary("convert.int16")
			.input(b).outType(ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testCfloat32ToInt16() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.int16(0f), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.int16(-4.25f), ops.unary("convert.int16").input(b)
			.outType(ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testFloat64ToInt16() {

		final DoubleType b = new DoubleType(4098d);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(4098d), result.get());

		b.set(0d);
		assertEquals(Types.int16(0d), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.int16(-10948.015625d), ops.unary("convert.int16").input(
			b).outType(ShortType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.int16(1.0000152587890625e20), ops.unary("convert.int16")
			.input(b).outType(ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testCfloat64ToInt16() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final ShortType result = ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply();
		assertEquals(Types.int16(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.int16(0d), ops.unary("convert.int16").input(b).outType(
			ShortType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.int16(-234.25d), ops.unary("convert.int16").input(b)
			.outType(ShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testBitToUint16() {

		final BitType b = new BitType(true);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(1), result.get());

		b.set(false);
		assertEquals(Types.uint16(0), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint2ToUint16() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(2), result.get());

		b.set(0);
		assertEquals(Types.uint16(0), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint4ToUint16() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(15), result.get());

		b.set(0);
		assertEquals(Types.uint16(0), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testInt8ToUint16() {

		final ByteType b = new ByteType((byte) 8);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.uint16((byte) 0), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.uint16((byte) -12), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint8ToUint16() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(100), result.get());

		b.set(0);
		assertEquals(Types.uint16(0), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint12ToUint16() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(212L), result.get());

		b.set(0L);
		assertEquals(Types.uint16(0L), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testInt16ToUint16() {

		final ShortType b = new ShortType((short) 52);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.uint16((short) 0), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.uint16((short) -154), ops.unary("convert.uint16").input(
			b).outType(UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint16ToUint16() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(480), result.get());

		b.set(0);
		assertEquals(Types.uint16(0), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testInt32ToUint16() {

		final IntType b = new IntType(301);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(301), result.get());

		b.set(0);
		assertEquals(Types.uint16(0), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

		b.set(-89);
		assertEquals(Types.uint16(-89), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint32ToUint16() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(20L), result.get());

		b.set(0L);
		assertEquals(Types.uint16(0L), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testInt64ToUint16() {

		final LongType b = new LongType(891L);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(891L), result.get());

		b.set(0L);
		assertEquals(Types.uint16(0L), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.uint16(-1024L), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint64ToUint16() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(1049L), result.get());

		b.set(0L);
		assertEquals(Types.uint16(0L), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

		b.set(p64);
		assertEquals(Types.uint16(p64), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint128ToUint16() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(beef), result.get());

		b.set(biZero);
		assertEquals(Types.uint16(biZero), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

		b.set(p128);
		assertEquals(Types.uint16(p128), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testFloat32ToUint16() {

		final FloatType b = new FloatType(123453.125f);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.uint16(0f), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.uint16(-2523485349058.0f), ops.unary("convert.uint16")
			.input(b).outType(UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testCfloat32ToUint16() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.uint16(0f), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.uint16(-4.25f), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testFloat64ToUint16() {

		final DoubleType b = new DoubleType(4098d);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(4098d), result.get());

		b.set(0d);
		assertEquals(Types.uint16(0d), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.uint16(-10948.015625d), ops.unary("convert.uint16")
			.input(b).outType(UnsignedShortType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.uint16(1.0000152587890625e20), ops.unary(
			"convert.uint16").input(b).outType(UnsignedShortType.class).apply()
			.get());

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testCfloat64ToUint16() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final UnsignedShortType result = ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply();
		assertEquals(Types.uint16(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.uint16(0d), ops.unary("convert.uint16").input(b).outType(
			UnsignedShortType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.uint16(-234.25d), ops.unary("convert.uint16").input(b)
			.outType(UnsignedShortType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testBitToInt32() {

		final BitType b = new BitType(true);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(1), result.get());

		b.set(false);
		assertEquals(Types.int32(0), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint2ToInt32() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(2), result.get());

		b.set(0);
		assertEquals(Types.int32(0), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint4ToInt32() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(15), result.get());

		b.set(0);
		assertEquals(Types.int32(0), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testInt8ToInt32() {

		final ByteType b = new ByteType((byte) 8);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.int32((byte) 0), ops.unary("convert.int32").input(b)
			.outType(IntType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.int32((byte) -12), ops.unary("convert.int32").input(b)
			.outType(IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint8ToInt32() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(100), result.get());

		b.set(0);
		assertEquals(Types.int32(0), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint12ToInt32() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(212L), result.get());

		b.set(0L);
		assertEquals(Types.int32(0L), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testInt16ToInt32() {

		final ShortType b = new ShortType((short) 52);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.int32((short) 0), ops.unary("convert.int32").input(b)
			.outType(IntType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.int32((short) -154), ops.unary("convert.int32").input(b)
			.outType(IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint16ToInt32() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(480), result.get());

		b.set(0);
		assertEquals(Types.int32(0), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testInt32ToInt32() {

		final IntType b = new IntType(301);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(301), result.get());

		b.set(0);
		assertEquals(Types.int32(0), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

		b.set(-89);
		assertEquals(Types.int32(-89), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint32ToInt32() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(20L), result.get());

		b.set(0L);
		assertEquals(Types.int32(0L), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testInt64ToInt32() {

		final LongType b = new LongType(891L);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(891L), result.get());

		b.set(0L);
		assertEquals(Types.int32(0L), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.int32(-1024L), ops.unary("convert.int32").input(b)
			.outType(IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint64ToInt32() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(1049L), result.get());

		b.set(0L);
		assertEquals(Types.int32(0L), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

		b.set(p64);
		assertEquals(Types.int32(p64), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint128ToInt32() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(beef), result.get());

		b.set(biZero);
		assertEquals(Types.int32(biZero), ops.unary("convert.int32").input(b)
			.outType(IntType.class).apply().get());

		b.set(p128);
		assertEquals(Types.int32(p128), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testFloat32ToInt32() {

		final FloatType b = new FloatType(123453.125f);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.int32(0f), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.int32(-2523485349058.0f), ops.unary("convert.int32")
			.input(b).outType(IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testCfloat32ToInt32() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.int32(0f), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.int32(-4.25f), ops.unary("convert.int32").input(b)
			.outType(IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testFloat64ToInt32() {

		final DoubleType b = new DoubleType(4098d);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(4098d), result.get());

		b.set(0d);
		assertEquals(Types.int32(0d), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.int32(-10948.015625d), ops.unary("convert.int32").input(
			b).outType(IntType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.int32(1.0000152587890625e20), ops.unary("convert.int32")
			.input(b).outType(IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testCfloat64ToInt32() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final IntType result = ops.unary("convert.int32").input(b).outType(
			IntType.class).apply();
		assertEquals(Types.int32(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.int32(0d), ops.unary("convert.int32").input(b).outType(
			IntType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.int32(-234.25d), ops.unary("convert.int32").input(b)
			.outType(IntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testBitToUint32() {

		final BitType b = new BitType(true);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(1), result.get());

		b.set(false);
		assertEquals(Types.uint32(0), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint2ToUint32() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(2), result.get());

		b.set(0);
		assertEquals(Types.uint32(0), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint4ToUint32() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(15), result.get());

		b.set(0);
		assertEquals(Types.uint32(0), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testInt8ToUint32() {

		final ByteType b = new ByteType((byte) 8);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.uint32((byte) 0), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.uint32((byte) -12), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint8ToUint32() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(100), result.get());

		b.set(0);
		assertEquals(Types.uint32(0), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint12ToUint32() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(212L), result.get());

		b.set(0L);
		assertEquals(Types.uint32(0L), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testInt16ToUint32() {

		final ShortType b = new ShortType((short) 52);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.uint32((short) 0), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.uint32((short) -154), ops.unary("convert.uint32").input(
			b).outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint16ToUint32() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(480), result.get());

		b.set(0);
		assertEquals(Types.uint32(0), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testInt32ToUint32() {

		final IntType b = new IntType(301);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(301), result.get());

		b.set(0);
		assertEquals(Types.uint32(0), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

		b.set(-89);
		assertEquals(Types.uint32(-89), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint32ToUint32() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(20L), result.get());

		b.set(0L);
		assertEquals(Types.uint32(0L), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testInt64ToUint32() {

		final LongType b = new LongType(891L);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(891L), result.get());

		b.set(0L);
		assertEquals(Types.uint32(0L), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.uint32(-1024L), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint64ToUint32() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(1049L), result.get());

		b.set(0L);
		assertEquals(Types.uint32(0L), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

		b.set(p64);
		assertEquals(Types.uint32(p64), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint128ToUint32() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(beef), result.get());

		b.set(biZero);
		assertEquals(Types.uint32(biZero), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

		b.set(p128);
		assertEquals(Types.uint32(p128), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testFloat32ToUint32() {

		final FloatType b = new FloatType(123453.125f);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.uint32(0f), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.uint32(-2523485349058.0f), ops.unary("convert.uint32")
			.input(b).outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testCfloat32ToUint32() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.uint32(0f), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.uint32(-4.25f), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testFloat64ToUint32() {

		final DoubleType b = new DoubleType(4098d);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(4098d), result.get());

		b.set(0d);
		assertEquals(Types.uint32(0d), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.uint32(-10948.015625d), ops.unary("convert.uint32")
			.input(b).outType(UnsignedIntType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.uint32(1.0000152587890625e20), ops.unary(
			"convert.uint32").input(b).outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testCfloat64ToUint32() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final UnsignedIntType result = ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply();
		assertEquals(Types.uint32(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.uint32(0d), ops.unary("convert.uint32").input(b).outType(
			UnsignedIntType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.uint32(-234.25d), ops.unary("convert.uint32").input(b)
			.outType(UnsignedIntType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testBitToInt64() {

		final BitType b = new BitType(true);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(1), result.get());

		b.set(false);
		assertEquals(Types.int64(0), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint2ToInt64() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(2), result.get());

		b.set(0);
		assertEquals(Types.int64(0), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint4ToInt64() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(15), result.get());

		b.set(0);
		assertEquals(Types.int64(0), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testInt8ToInt64() {

		final ByteType b = new ByteType((byte) 8);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.int64((byte) 0), ops.unary("convert.int64").input(b)
			.outType(LongType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.int64((byte) -12), ops.unary("convert.int64").input(b)
			.outType(LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint8ToInt64() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(100), result.get());

		b.set(0);
		assertEquals(Types.int64(0), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint12ToInt64() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(212L), result.get());

		b.set(0L);
		assertEquals(Types.int64(0L), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testInt16ToInt64() {

		final ShortType b = new ShortType((short) 52);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.int64((short) 0), ops.unary("convert.int64").input(b)
			.outType(LongType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.int64((short) -154), ops.unary("convert.int64").input(b)
			.outType(LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint16ToInt64() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(480), result.get());

		b.set(0);
		assertEquals(Types.int64(0), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testInt32ToInt64() {

		final IntType b = new IntType(301);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(301), result.get());

		b.set(0);
		assertEquals(Types.int64(0), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

		b.set(-89);
		assertEquals(Types.int64(-89), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint32ToInt64() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(20L), result.get());

		b.set(0L);
		assertEquals(Types.int64(0L), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testInt64ToInt64() {

		final LongType b = new LongType(891L);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(891L), result.get());

		b.set(0L);
		assertEquals(Types.int64(0L), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.int64(-1024L), ops.unary("convert.int64").input(b)
			.outType(LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint64ToInt64() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(1049L), result.get());

		b.set(0L);
		assertEquals(Types.int64(0L), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

		b.set(p64);
		assertEquals(Types.int64(p64), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint128ToInt64() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(beef), result.get());

		b.set(biZero);
		assertEquals(Types.int64(biZero), ops.unary("convert.int64").input(b)
			.outType(LongType.class).apply().get());

		b.set(p128);
		assertEquals(Types.int64(p128), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testFloat32ToInt64() {

		final FloatType b = new FloatType(123453.125f);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.int64(0f), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.int64(-2523485349058.0f), ops.unary("convert.int64")
			.input(b).outType(LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testCfloat32ToInt64() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.int64(0f), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.int64(-4.25f), ops.unary("convert.int64").input(b)
			.outType(LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testFloat64ToInt64() {

		final DoubleType b = new DoubleType(4098d);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(4098d), result.get());

		b.set(0d);
		assertEquals(Types.int64(0d), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.int64(-10948.015625d), ops.unary("convert.int64").input(
			b).outType(LongType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.int64(1.0000152587890625e20), ops.unary("convert.int64")
			.input(b).outType(LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testCfloat64ToInt64() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final LongType result = ops.unary("convert.int64").input(b).outType(
			LongType.class).apply();
		assertEquals(Types.int64(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.int64(0d), ops.unary("convert.int64").input(b).outType(
			LongType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.int64(-234.25d), ops.unary("convert.int64").input(b)
			.outType(LongType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testBitToUint64() {

		final BitType b = new BitType(true);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(1), result.getBigInteger());

		b.set(false);
		assertEquals(Types.uint64(0), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint2ToUint64() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(2), result.getBigInteger());

		b.set(0);
		assertEquals(Types.uint64(0), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint4ToUint64() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(15), result.getBigInteger());

		b.set(0);
		assertEquals(Types.uint64(0), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testInt8ToUint64() {

		final ByteType b = new ByteType((byte) 8);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64((byte) 8), result.getBigInteger());

		b.set((byte) 0);
		assertEquals(Types.uint64((byte) 0), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

		b.set((byte) -12);
		assertEquals(Types.uint64((byte) -12), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint8ToUint64() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(100), result.getBigInteger());

		b.set(0);
		assertEquals(Types.uint64(0), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint12ToUint64() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(212L), result.getBigInteger());

		b.set(0L);
		assertEquals(Types.uint64(0L), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testInt16ToUint64() {

		final ShortType b = new ShortType((short) 52);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64((short) 52), result.getBigInteger());

		b.set((short) 0);
		assertEquals(Types.uint64((short) 0), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

		b.set((short) -154);
		assertEquals(Types.uint64((short) -154), ops.unary("convert.uint64").input(
			b).outType(UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint16ToUint64() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(480), result.getBigInteger());

		b.set(0);
		assertEquals(Types.uint64(0), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testInt32ToUint64() {

		final IntType b = new IntType(301);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(301), result.getBigInteger());

		b.set(0);
		assertEquals(Types.uint64(0), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

		b.set(-89);
		assertEquals(Types.uint64(-89), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint32ToUint64() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(20L), result.getBigInteger());

		b.set(0L);
		assertEquals(Types.uint64(0L), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testInt64ToUint64() {

		final LongType b = new LongType(891L);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(891L), result.getBigInteger());

		b.set(0L);
		assertEquals(Types.uint64(0L), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

		b.set(-1024L);
		assertEquals(Types.uint64(-1024L), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint64ToUint64() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(1049L), result.getBigInteger());

		b.set(0L);
		assertEquals(Types.uint64(0L), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

		b.set(p64);
		assertEquals(Types.uint64(p64), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint128ToUint64() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(beef), result.getBigInteger());

		b.set(biZero);
		assertEquals(Types.uint64(biZero), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

		b.set(p128);
		assertEquals(Types.uint64(p128), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testFloat32ToUint64() {

		final FloatType b = new FloatType(123453.125f);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(123453.125f), result.getBigInteger());

		b.set(0f);
		assertEquals(Types.uint64(0f), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

		b.set(-2523485349058.0f);
		assertEquals(Types.uint64(-2523485349058.0f), ops.unary("convert.uint64")
			.input(b).outType(UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testCfloat32ToUint64() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(5839.25f), result.getBigInteger());

		b.set(0f, 0f);
		assertEquals(Types.uint64(0f), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.uint64(-4.25f), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testFloat64ToUint64() {

		final DoubleType b = new DoubleType(4098d);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(4098d), result.getBigInteger());

		b.set(0d);
		assertEquals(Types.uint64(0d), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

		b.set(-10948.015625d);
		assertEquals(Types.uint64(-10948.015625d), ops.unary("convert.uint64")
			.input(b).outType(UnsignedLongType.class).apply().getBigInteger());

		b.set(1.0000152587890625e20);
		assertEquals(Types.uint64(1.0000152587890625e20), ops.unary(
			"convert.uint64").input(b).outType(UnsignedLongType.class).apply()
			.getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testCfloat64ToUint64() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final UnsignedLongType result = ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply();
		assertEquals(Types.uint64(9087d), result.getBigInteger());

		b.set(0d, 0d);
		assertEquals(Types.uint64(0d), ops.unary("convert.uint64").input(b).outType(
			UnsignedLongType.class).apply().getBigInteger());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.uint64(-234.25d), ops.unary("convert.uint64").input(b)
			.outType(UnsignedLongType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToUint128() {

		final BitType b = new BitType(true);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(1), result.get());

		b.set(false);
		assertEquals(Types.uint128(0), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToUint128() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(2), result.get());

		b.set(0);
		assertEquals(Types.uint128(0), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToUint128() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(15), result.get());

		b.set(0);
		assertEquals(Types.uint128(0), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToUint128() {

		final ByteType b = new ByteType((byte) 8);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128((byte) 8), result.get());

		b.set((byte) 0);
		assertEquals(Types.uint128((byte) 0), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set((byte) -12);
		assertEquals(Types.uint128((byte) -12), ops.unary("convert.uint128").input(
			b).outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToUint128() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(100), result.get());

		b.set(0);
		assertEquals(Types.uint128(0), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToUint128() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(212L), result.get());

		b.set(0L);
		assertEquals(Types.uint128(0L), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToUint128() {

		final ShortType b = new ShortType((short) 52);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128((short) 52), result.get());

		b.set((short) 0);
		assertEquals(Types.uint128((short) 0), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set((short) -154);
		assertEquals(Types.uint128((short) -154), ops.unary("convert.uint128")
			.input(b).outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToUint128() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(480), result.get());

		b.set(0);
		assertEquals(Types.uint128(0), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToUint128() {

		final IntType b = new IntType(301);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(301), result.get());

		b.set(0);
		assertEquals(Types.uint128(0), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set(-89);
		assertEquals(Types.uint128(-89), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToUint128() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(20L), result.get());

		b.set(0L);
		assertEquals(Types.uint128(0L), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToUint128() {

		final LongType b = new LongType(891L);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(891L), result.get());

		b.set(0L);
		assertEquals(Types.uint128(0L), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set(-1024L);
		assertEquals(Types.uint128(-1024L), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToUint128() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(1049L), result.get());

		b.set(0L);
		assertEquals(Types.uint128(0L), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set(p64);
		assertEquals(Types.uint64Uint128(p64), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().getBigInteger());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToUint128() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(beef), result.get());

		b.set(biZero);
		assertEquals(Types.uint128(biZero), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set(p128);
		assertEquals(Types.uint128(p128), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToUint128() {

		final FloatType b = new FloatType(123453.125f);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(123453.125f), result.get());

		b.set(0f);
		assertEquals(Types.uint128(0f), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set(-2523485349058.0f);
		assertEquals(Types.uint128(-2523485349058.0f), ops.unary("convert.uint128")
			.input(b).outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToUint128() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(5839.25f), result.get());

		b.set(0f, 0f);
		assertEquals(Types.uint128(0f), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.uint128(-4.25f), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToUint128() {

		final DoubleType b = new DoubleType(4098d);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(4098d), result.get());

		b.set(0d);
		assertEquals(Types.uint128(0d), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set(-10948.015625d);
		assertEquals(Types.uint128(-10948.015625d), ops.unary("convert.uint128")
			.input(b).outType(Unsigned128BitType.class).apply().get());

		b.set(1.0000152587890625e20);
		assertEquals(Types.uint128(1.0000152587890625e20), ops.unary(
			"convert.uint128").input(b).outType(Unsigned128BitType.class).apply()
			.get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToUint128() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final Unsigned128BitType result = ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply();
		assertEquals(Types.uint128(9087d), result.get());

		b.set(0d, 0d);
		assertEquals(Types.uint128(0d), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

		b.set(-234.25d, -9.0d);
		assertEquals(Types.uint128(-234.25d), ops.unary("convert.uint128").input(b)
			.outType(Unsigned128BitType.class).apply().get());

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToFloat32() {

		final BitType b = new BitType(true);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(1), result.get(), 0);

		b.set(false);
		assertEquals(Types.float32(0), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToFloat32() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(2), result.get(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToFloat32() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(15), result.get(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToFloat32() {

		final ByteType b = new ByteType((byte) 8);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32((byte) 8), result.get(), 0);

		b.set((byte) 0);
		assertEquals(Types.float32((byte) 0), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set((byte) -12);
		assertEquals(Types.float32((byte) -12), ops.unary("convert.float32").input(
			b).outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToFloat32() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(100), result.get(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToFloat32() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(212L), result.get(), 0);

		b.set(0L);
		assertEquals(Types.float32(0L), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToFloat32() {

		final ShortType b = new ShortType((short) 52);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32((short) 52), result.get(), 0);

		b.set((short) 0);
		assertEquals(Types.float32((short) 0), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set((short) -154);
		assertEquals(Types.float32((short) -154), ops.unary("convert.float32")
			.input(b).outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToFloat32() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(480), result.get(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToFloat32() {

		final IntType b = new IntType(301);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(301), result.get(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set(-89);
		assertEquals(Types.float32(-89), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToFloat32() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(20L), result.get(), 0);

		b.set(0L);
		assertEquals(Types.float32(0L), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToFloat32() {

		final LongType b = new LongType(891L);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(891L), result.get(), 0);

		b.set(0L);
		assertEquals(Types.float32(0L), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set(-1024L);
		assertEquals(Types.float32(-1024L), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToFloat32() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(1049L), result.get(), 0);

		b.set(0L);
		assertEquals(Types.float32(0L), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set(p64);
		assertEquals(Types.float32(p64), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToFloat32() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(beef), result.get(), 0);

		b.set(biZero);
		assertEquals(Types.float32(biZero), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set(p128);
		assertEquals(Types.float32(p128), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToFloat32() {

		final FloatType b = new FloatType(123453.125f);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(123453.125f), result.get(), 0);

		b.set(0f);
		assertEquals(Types.float32(0f), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set(-2523485349058.0f);
		assertEquals(Types.float32(-2523485349058.0f), ops.unary("convert.float32")
			.input(b).outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToFloat32() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(5839.25f), result.get(), 0);

		b.set(0f, 0f);
		assertEquals(Types.float32(0f), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.float32(-4.25f), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToFloat32() {

		final DoubleType b = new DoubleType(4098d);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(4098d), result.get(), 0);

		b.set(0d);
		assertEquals(Types.float32(0d), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set(-10948.015625d);
		assertEquals(Types.float32(-10948.015625d), ops.unary("convert.float32")
			.input(b).outType(FloatType.class).apply().get(), 0);

		b.set(1.0000152587890625e20);
		assertEquals(Types.float32(1.0000152587890625e20), ops.unary(
			"convert.float32").input(b).outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToFloat32() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final FloatType result = ops.unary("convert.float32").input(b).outType(
			FloatType.class).apply();
		assertEquals(Types.float32(9087d), result.get(), 0);

		b.set(0d, 0d);
		assertEquals(Types.float32(0d), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

		b.set(-234.25d, -9.0d);
		assertEquals(Types.float32(-234.25d), ops.unary("convert.float32").input(b)
			.outType(FloatType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToCfloat32() {

		final BitType b = new BitType(true);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(1), result.getRealFloat(), 0);
		assertEquals(Types.float32(0), result.getImaginaryFloat(), 0);

		b.set(false);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToCfloat32() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(2), result.getRealFloat(), 0);
		assertEquals(Types.float32(0), result.getImaginaryFloat(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToCfloat32() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(15), result.getRealFloat(), 0);
		assertEquals(Types.float32(0), result.getImaginaryFloat(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToCfloat32() {

		final ByteType b = new ByteType((byte) 8);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32((byte) 8), result.getRealFloat(), 0);
		assertEquals(Types.float32((byte) 0), result.getImaginaryFloat(), 0);

		b.set((byte) 0);
		assertEquals(Types.float32((byte) 0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32((byte) 0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set((byte) -12);
		assertEquals(Types.float32((byte) -12), ops.unary("convert.cfloat32").input(
			b).outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32((byte) 0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToCfloat32() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(100), result.getRealFloat(), 0);
		assertEquals(Types.float32(0), result.getImaginaryFloat(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToCfloat32() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(212L), result.getRealFloat(), 0);
		assertEquals(Types.float32(0L), result.getImaginaryFloat(), 0);

		b.set(0L);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToCfloat32() {

		final ShortType b = new ShortType((short) 52);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32((short) 52), result.getRealFloat(), 0);
		assertEquals(Types.float32((short) 0), result.getImaginaryFloat(), 0);

		b.set((short) 0);
		assertEquals(Types.float32((short) 0), ops.unary("convert.cfloat32").input(
			b).outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32((short) 0), ops.unary("convert.cfloat32").input(
			b).outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set((short) -154);
		assertEquals(Types.float32((short) -154), ops.unary("convert.cfloat32")
			.input(b).outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32((short) 0), ops.unary("convert.cfloat32").input(
			b).outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToCfloat32() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(480), result.getRealFloat(), 0);
		assertEquals(Types.float32(0), result.getImaginaryFloat(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToCfloat32() {

		final IntType b = new IntType(301);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(301), result.getRealFloat(), 0);
		assertEquals(Types.float32(0), result.getImaginaryFloat(), 0);

		b.set(0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set(-89);
		assertEquals(Types.float32(-89), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToCfloat32() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(20L), result.getRealFloat(), 0);
		assertEquals(Types.float32(0L), result.getImaginaryFloat(), 0);

		b.set(0L);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToCfloat32() {

		final LongType b = new LongType(891L);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(891L), result.getRealFloat(), 0);
		assertEquals(Types.float32(0L), result.getImaginaryFloat(), 0);

		b.set(0L);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set(-1024L);
		assertEquals(Types.float32(-1024L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToCfloat32() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(1049L), result.getRealFloat(), 0);
		assertEquals(Types.float32(0L), result.getImaginaryFloat(), 0);

		b.set(0L);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set(p64);
		assertEquals(Types.float32(p64), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0L), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToCfloat32() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(beef), result.getRealFloat(), 0);
		assertEquals(Types.float32(biZero), result.getImaginaryFloat(), 0);

		b.set(biZero);
		assertEquals(Types.float32(biZero), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(biZero), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set(p128);
		assertEquals(Types.float32(p128), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(biZero), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToCfloat32() {

		final FloatType b = new FloatType(123453.125f);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(123453.125f), result.getRealFloat(), 0);
		assertEquals(Types.float32(0f), result.getImaginaryFloat(), 0);

		b.set(0f);
		assertEquals(Types.float32(0f), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0f), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set(-2523485349058.0f);
		assertEquals(Types.float32(-2523485349058.0f), ops.unary("convert.cfloat32")
			.input(b).outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0f), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToCfloat32() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(5839.25f), result.getRealFloat(), 0);
		assertEquals(Types.float32(120f), result.getImaginaryFloat(), 0);

		b.set(0f, 0f);
		assertEquals(Types.float32(0f), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0f), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.float32(-4.25f), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(-123.0625f), ops.unary("convert.cfloat32").input(
			b).outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToCfloat32() {

		final DoubleType b = new DoubleType(4098d);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(4098d), result.getRealFloat(), 0);
		assertEquals(Types.float32(0d), result.getImaginaryFloat(), 0);

		b.set(0d);
		assertEquals(Types.float32(0d), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0d), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set(-10948.015625d);
		assertEquals(Types.float32(-10948.015625d), ops.unary("convert.cfloat32")
			.input(b).outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0d), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set(1.0000152587890625e20);
		assertEquals(Types.float32(1.0000152587890625e20), ops.unary(
			"convert.cfloat32").input(b).outType(ComplexFloatType.class).apply()
			.getRealFloat(), 0);
		assertEquals(Types.float32(0), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToCfloat32() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final ComplexFloatType result = ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply();
		assertEquals(Types.float32(9087d), result.getRealFloat(), 0);
		assertEquals(Types.float32(879542.125d), result.getImaginaryFloat(), 0);

		b.set(0d, 0d);
		assertEquals(Types.float32(0d), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(0d), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

		b.set(-234.25d, -9.0d);
		assertEquals(Types.float32(-234.25d), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getRealFloat(), 0);
		assertEquals(Types.float32(-9.0d), ops.unary("convert.cfloat32").input(b)
			.outType(ComplexFloatType.class).apply().getImaginaryFloat(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToFloat64() {

		final BitType b = new BitType(true);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(1), result.get(), 0);

		b.set(false);
		assertEquals(Types.float64(0), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToFloat64() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(2), result.get(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToFloat64() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(15), result.get(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToFloat64() {

		final ByteType b = new ByteType((byte) 8);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64((byte) 8), result.get(), 0);

		b.set((byte) 0);
		assertEquals(Types.float64((byte) 0), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set((byte) -12);
		assertEquals(Types.float64((byte) -12), ops.unary("convert.float64").input(
			b).outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToFloat64() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(100), result.get(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToFloat64() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(212L), result.get(), 0);

		b.set(0L);
		assertEquals(Types.float64(0L), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToFloat64() {

		final ShortType b = new ShortType((short) 52);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64((short) 52), result.get(), 0);

		b.set((short) 0);
		assertEquals(Types.float64((short) 0), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set((short) -154);
		assertEquals(Types.float64((short) -154), ops.unary("convert.float64")
			.input(b).outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToFloat64() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(480), result.get(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToFloat64() {

		final IntType b = new IntType(301);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(301), result.get(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set(-89);
		assertEquals(Types.float64(-89), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToFloat64() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(20L), result.get(), 0);

		b.set(0L);
		assertEquals(Types.float64(0L), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToFloat64() {

		final LongType b = new LongType(891L);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(891L), result.get(), 0);

		b.set(0L);
		assertEquals(Types.float64(0L), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set(-1024L);
		assertEquals(Types.float64(-1024L), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToFloat64() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(1049L), result.get(), 0);

		b.set(0L);
		assertEquals(Types.float64(0L), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set(p64);
		assertEquals(Types.float64(p64), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToFloat64() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(beef), result.get(), 0);

		b.set(biZero);
		assertEquals(Types.float64(biZero), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set(p128);
		assertEquals(Types.float64(p128), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToFloat64() {

		final FloatType b = new FloatType(123453.125f);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(123453.125f), result.get(), 0);

		b.set(0f);
		assertEquals(Types.float64(0f), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set(-2523485349058.0f);
		assertEquals(Types.float64(-2523485349058.0f), ops.unary("convert.float64")
			.input(b).outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToFloat64() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(5839.25f), result.get(), 0);

		b.set(0f, 0f);
		assertEquals(Types.float64(0f), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.float64(-4.25f), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToFloat64() {

		final DoubleType b = new DoubleType(4098d);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(4098d), result.get(), 0);

		b.set(0d);
		assertEquals(Types.float64(0d), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set(-10948.015625d);
		assertEquals(Types.float64(-10948.015625d), ops.unary("convert.float64")
			.input(b).outType(DoubleType.class).apply().get(), 0);

		b.set(1.0000152587890625e20);
		assertEquals(Types.float64(1.0000152587890625e20), ops.unary(
			"convert.float64").input(b).outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToFloat64() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final DoubleType result = ops.unary("convert.float64").input(b).outType(
			DoubleType.class).apply();
		assertEquals(Types.float64(9087d), result.get(), 0);

		b.set(0d, 0d);
		assertEquals(Types.float64(0d), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

		b.set(-234.25d, -9.0d);
		assertEquals(Types.float64(-234.25d), ops.unary("convert.float64").input(b)
			.outType(DoubleType.class).apply().get(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToCfloat64() {

		final BitType b = new BitType(true);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(1), result.getRealDouble(), 0);
		assertEquals(Types.float64(0), result.getImaginaryDouble(), 0);

		b.set(false);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToCfloat64() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(2), result.getRealDouble(), 0);
		assertEquals(Types.float64(0), result.getImaginaryDouble(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToCfloat64() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(15), result.getRealDouble(), 0);
		assertEquals(Types.float64(0), result.getImaginaryDouble(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToCfloat64() {

		final ByteType b = new ByteType((byte) 8);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64((byte) 8), result.getRealDouble(), 0);
		assertEquals(Types.float64((byte) 0), result.getImaginaryDouble(), 0);

		b.set((byte) 0);
		assertEquals(Types.float64((byte) 0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64((byte) 0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set((byte) -12);
		assertEquals(Types.float64((byte) -12), ops.unary("convert.cfloat64").input(
			b).outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64((byte) 0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToCfloat64() {

		final UnsignedByteType b = new UnsignedByteType(100);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(100), result.getRealDouble(), 0);
		assertEquals(Types.float64(0), result.getImaginaryDouble(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToCfloat64() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(212L), result.getRealDouble(), 0);
		assertEquals(Types.float64(0L), result.getImaginaryDouble(), 0);

		b.set(0L);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToCfloat64() {

		final ShortType b = new ShortType((short) 52);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64((short) 52), result.getRealDouble(), 0);
		assertEquals(Types.float64((short) 0), result.getImaginaryDouble(), 0);

		b.set((short) 0);
		assertEquals(Types.float64((short) 0), ops.unary("convert.cfloat64").input(
			b).outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64((short) 0), ops.unary("convert.cfloat64").input(
			b).outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set((short) -154);
		assertEquals(Types.float64((short) -154), ops.unary("convert.cfloat64")
			.input(b).outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64((short) 0), ops.unary("convert.cfloat64").input(
			b).outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToCfloat64() {

		final UnsignedShortType b = new UnsignedShortType(480);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(480), result.getRealDouble(), 0);
		assertEquals(Types.float64(0), result.getImaginaryDouble(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToCfloat64() {

		final IntType b = new IntType(301);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(301), result.getRealDouble(), 0);
		assertEquals(Types.float64(0), result.getImaginaryDouble(), 0);

		b.set(0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set(-89);
		assertEquals(Types.float64(-89), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToCfloat64() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(20L), result.getRealDouble(), 0);
		assertEquals(Types.float64(0L), result.getImaginaryDouble(), 0);

		b.set(0L);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToCfloat64() {

		final LongType b = new LongType(891L);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(891L), result.getRealDouble(), 0);
		assertEquals(Types.float64(0L), result.getImaginaryDouble(), 0);

		b.set(0L);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set(-1024L);
		assertEquals(Types.float64(-1024L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToCfloat64() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(1049L), result.getRealDouble(), 0);
		assertEquals(Types.float64(0L), result.getImaginaryDouble(), 0);

		b.set(0L);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set(p64);
		assertEquals(Types.float64(p64), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0L), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToCfloat64() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(beef), result.getRealDouble(), 0);
		assertEquals(Types.float64(biZero), result.getImaginaryDouble(), 0);

		b.set(biZero);
		assertEquals(Types.float64(biZero), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(biZero), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set(p128);
		assertEquals(Types.float64(p128), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(biZero), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToCfloat64() {

		final FloatType b = new FloatType(123453.125f);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(123453.125f), result.getRealDouble(), 0);
		assertEquals(Types.float64(0f), result.getImaginaryDouble(), 0);

		b.set(0f);
		assertEquals(Types.float64(0f), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0f), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set(-2523485349058.0f);
		assertEquals(Types.float64(-2523485349058.0f), ops.unary("convert.cfloat64")
			.input(b).outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0f), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToCfloat64() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(5839.25f), result.getRealDouble(), 0);
		assertEquals(Types.float64(120f), result.getImaginaryDouble(), 0);

		b.set(0f, 0f);
		assertEquals(Types.float64(0f), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0f), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set(-4.25f, -123.0625f);
		assertEquals(Types.float64(-4.25f), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(-123.0625f), ops.unary("convert.cfloat64").input(
			b).outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToCfloat64() {

		final DoubleType b = new DoubleType(4098d);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(4098d), result.getRealDouble(), 0);
		assertEquals(Types.float64(0d), result.getImaginaryDouble(), 0);

		b.set(0d);
		assertEquals(Types.float64(0d), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0d), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set(-10948.015625d);
		assertEquals(Types.float64(-10948.015625d), ops.unary("convert.cfloat64")
			.input(b).outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0d), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set(1.0000152587890625e20);
		assertEquals(Types.float64(1.0000152587890625e20), ops.unary(
			"convert.cfloat64").input(b).outType(ComplexDoubleType.class).apply()
			.getRealDouble(), 0);
		assertEquals(Types.float64(0), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToCfloat64() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		final ComplexDoubleType result = ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply();
		assertEquals(Types.float64(9087d), result.getRealDouble(), 0);
		assertEquals(Types.float64(879542.125d), result.getImaginaryDouble(), 0);

		b.set(0d, 0d);
		assertEquals(Types.float64(0d), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(0d), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

		b.set(-234.25d, -9.0d);
		assertEquals(Types.float64(-234.25d), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getRealDouble(), 0);
		assertEquals(Types.float64(-9.0d), ops.unary("convert.cfloat64").input(b)
			.outType(ComplexDoubleType.class).apply().getImaginaryDouble(), 0);

	}

}
