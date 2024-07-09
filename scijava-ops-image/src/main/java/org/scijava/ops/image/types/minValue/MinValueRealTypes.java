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

package org.scijava.ops.image.types.minValue;

import java.math.BigInteger;
import java.util.function.Function;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.logic.NativeBoolType;
import net.imglib2.type.numeric.RealType;
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

/**
 * This collection of Ops can be used to obtain the minimum value of any
 * {@link RealType}. This method of determining the minimum value of a
 * {@link RealType} is preferable since it is safe and extensible.
 *
 * @author Gabriel Selzer
 */
public class MinValueRealTypes {

	final BitType minBit = new BitType(false);

	/**
	 * @input in some {@link BitType}
	 * @output minValue a {@link BitType} containing the minimum value of a bit
	 * @implNote op names='types.minValue'
	 */
	public final Function<BitType, BitType> minBitType = in -> {
		return minBit;
	};

	final BoolType minBool = new BoolType(false);

	/**
	 * @input in some {@link BoolType}
	 * @output minValue a {@link BoolType} containing the minimum value of a
	 *         boolean
	 * @implNote op names='types.minValue'
	 */
	public final Function<BoolType, BoolType> minBoolType = in -> {
		return minBool;
	};

	final NativeBoolType minNativeBool = new NativeBoolType(false);

	/**
	 * @input in some {@link NativeBoolType}
	 * @output minValue a {@link NativeBoolType} containing the minimum value of a
	 *         boolean
	 * @implNote op names='types.minValue'
	 */
	public final Function<NativeBoolType, NativeBoolType> minNativeBoolType =
		in -> {
			return minNativeBool;
		};

	final ByteType minByte = new ByteType(Byte.MIN_VALUE);

	/**
	 * @input in some {@link ByteType}
	 * @output minValue a {@link ByteType} containing the minimum value of a byte
	 * @implNote op names='types.minValue'
	 */
	public final Function<ByteType, ByteType> minByteType = in -> {
		return minByte;
	};

	final UnsignedByteType minUnsignedByte = new UnsignedByteType(0);

	/**
	 * @input in some {@link UnsignedByteType}
	 * @output minValue a {@link UnsignedByteType} containing the minimum value of
	 *         an unsigned byte
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedByteType, UnsignedByteType> minUnsignedByteType =
		in -> {
			return minUnsignedByte;
		};

	final IntType minInt = new IntType(Integer.MIN_VALUE);

	/**
	 * @input in some {@link IntType}
	 * @output minValue a {@link IntType} containing the minimum value of an int
	 * @implNote op names='types.minValue'
	 */
	public final Function<IntType, IntType> minIntType = in -> {
		return minInt;
	};

	final UnsignedIntType minUnsignedInt = new UnsignedIntType(0);

	/**
	 * @input in some {@link UnsignedIntType}
	 * @output minValue a {@link UnsignedIntType} containing the minimum value of
	 *         an unsigned int
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedIntType, UnsignedIntType> minUnsignedIntType =
		in -> {
			return minUnsignedInt;
		};

	final LongType minLong = new LongType(Long.MIN_VALUE);

	/**
	 * @input in some {@link LongType}
	 * @output minValue a {@link LongType} containing the minimum value of a long
	 * @implNote op names='types.minValue'
	 */
	public final Function<LongType, LongType> minLongType = in -> {
		return minLong;
	};

	final UnsignedLongType minUnsignedLong = new UnsignedLongType(0);

	/**
	 * @input in some {@link UnsignedLongType}
	 * @output minValue a {@link UnsignedLongType} containing the minimum value of
	 *         an unsigned long
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedLongType, UnsignedLongType> minUnsignedLongType =
		in -> {
			return minUnsignedLong;
		};

	final ShortType minShort = new ShortType(Short.MIN_VALUE);

	/**
	 * @input in some {@link ShortType}
	 * @output minValue a {@link ShortType} containing the minimum value of a
	 *         short
	 * @implNote op names='types.minValue'
	 */
	public final Function<ShortType, ShortType> minShortType = in -> {
		return minShort;
	};

	final UnsignedShortType minUnsignedShort = new UnsignedShortType(0);

	/**
	 * @input in some {@link UnsignedShortType}
	 * @output minValue a {@link UnsignedShortType} containing the minimum value
	 *         of an unsigned short
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedShortType, UnsignedShortType> minUnsignedShortType =
		in -> {
			return minUnsignedShort;
		};

	final FloatType minFloat = new FloatType(Float.MIN_VALUE);

	/**
	 * @input in some {@link FloatType}
	 * @output minValue a {@link FloatType} containing the minimum value of a
	 *         32-bit floating point value
	 * @implNote op names='types.minValue'
	 */
	public final Function<FloatType, FloatType> minFloatType = in -> {
		return minFloat;
	};

	final DoubleType minDouble = new DoubleType(Double.MIN_VALUE);

	/**
	 * @input in some {@link DoubleType}
	 * @output minValue a {@link DoubleType} containing the minimum value of a
	 *         64-bit floating point value
	 * @implNote op names='types.minValue'
	 */
	public final Function<DoubleType, DoubleType> minDoubleType = in -> {
		return minDouble;
	};

	final Unsigned2BitType min2Bit = new Unsigned2BitType(0);

	/**
	 * @input in some {@link Unsigned2BitType}
	 * @output minValue a {@link Unsigned2BitType} containing the minimum value of
	 *         a 2-bit data structure
	 * @implNote op names='types.minValue'
	 */
	public final Function<Unsigned2BitType, Unsigned2BitType> min2BitType =
		in -> {
			return min2Bit;
		};

	final Unsigned4BitType min4Bit = new Unsigned4BitType(0);

	/**
	 * @input in some {@link Unsigned4BitType}
	 * @output minValue a {@link Unsigned4BitType} containing the minimum value of
	 *         a 4-bit data structure
	 * @implNote op names='types.minValue'
	 */
	public final Function<Unsigned4BitType, Unsigned4BitType> min4BitType =
		in -> {
			return min4Bit;
		};

	final Unsigned12BitType min12Bit = new Unsigned12BitType(0);

	/**
	 * @input in some {@link Unsigned12BitType}
	 * @output minValue a {@link Unsigned12BitType} containing the minimum value
	 *         of a 12-bit data structure
	 * @implNote op names='types.minValue'
	 */
	public final Function<Unsigned12BitType, Unsigned12BitType> min12BitType =
		in -> {
			return min12Bit;
		};

	final Unsigned128BitType min128Bit = new Unsigned128BitType(BigInteger.ZERO);

	/**
	 * @input in some {@link Unsigned128BitType}
	 * @output minValue a {@link Unsigned128BitType} containing the minimum value
	 *         of a 128-bit data structure
	 * @implNote op names='types.minValue'
	 */
	public final Function<Unsigned128BitType, Unsigned128BitType> min128BitType =
		in -> {
			return min128Bit;
		};

	// TODO: UnboundedIntegerType

	/**
	 * Due to the variable length of this type, we cannot simply return some final
	 * value. The best we can do is quickly compute the answer. TODO: Is there
	 * some way we could cache the values? Is that worth it??
	 *
	 * @input in some {@link UnsignedVariableBitLengthType}
	 * @output minValue a {@link UnsignedVariableBitLengthType} containing the
	 *         minimum value that can be stored in {@code in}
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedVariableBitLengthType, UnsignedVariableBitLengthType> minVarLengthType =
		in -> {
            var nBits = in.getBitsPerPixel();
			return new UnsignedVariableBitLengthType(0l, nBits);
		};

}
