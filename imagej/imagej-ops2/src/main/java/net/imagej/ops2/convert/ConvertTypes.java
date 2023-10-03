/*
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

package net.imagej.ops2.convert;

import java.math.BigDecimal;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
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

import org.scijava.function.Computers;

/**
 * Converters for converting between Complex types
 *
 * @author Alison Walter
 */
public final class ConvertTypes<C extends ComplexType<C>, T extends IntegerType<T>> {

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.bit'
	 */
	public final Computers.Arity1<C, BitType> complexToBit = (input, output) -> output.set(input.getRealDouble() != 0);

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.bit'
	 */
	public final Computers.Arity1<T, BitType> integerToBit = (input, output) -> output.set(input.getIntegerLong() != 0);

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint2'
	 */
	public final Computers.Arity1<C, Unsigned2BitType> complexToUint2 = (input, output) -> output.set((long) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint2'
	 */
	public final Computers.Arity1<T, Unsigned2BitType> integerToUint2 = (input, output) -> output.set(input.getIntegerLong());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint4'
	 */
	public final Computers.Arity1<C, Unsigned4BitType> complexToUint4 = (input, output) -> output.set((long) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint4'
	 */
	public final Computers.Arity1<T, Unsigned4BitType> integerToUint4 = (input, output) -> output.set(input.getIntegerLong());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.byte'
	 */
	public final Computers.Arity1<C, ByteType> complexToInt8 = (input, output) -> output.set((byte) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.byte'
	 */
	public final Computers.Arity1<T, ByteType> integerToInt8 = (input, output) -> output.set((byte) input.getIntegerLong());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint8'
	 */
	public final Computers.Arity1<C, UnsignedByteType> complexToUint8 = (input, output) -> output.set((int) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint8'
	 */
	public final Computers.Arity1<T, UnsignedByteType> integerToUint8 = (input, output) -> output.set(input.getInteger());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint12'
	 */
	public final Computers.Arity1<C, Unsigned12BitType> complexToUint12 = (input, output) -> output.set((long) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint12'
	 */
	public final Computers.Arity1<T, Unsigned12BitType> integerToUint12 = (input, output) -> output.set(input.getIntegerLong());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.int16'
	 */
	public final Computers.Arity1<C, ShortType> complexToInt16 = (input, output) -> output.set((short) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.int16'
	 */
	public final Computers.Arity1<T, ShortType> integerToInt16 = (input, output) -> output.set((short) input.getIntegerLong());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint16'
	 */
	public final Computers.Arity1<C, UnsignedShortType> complexToUint16 = (input, output) -> output.set((int) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint16'
	 */
	public final Computers.Arity1<T, UnsignedShortType> integerToUint16 = (input, output) -> output.set(input.getInteger());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.int32'
	 */
	public final Computers.Arity1<C, IntType> complexToInt32 = (input, output) -> output.set((int) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.int32'
	 */
	public final Computers.Arity1<T, IntType> integerToInt32 = (input, output) -> output.set(input.getInteger());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint32'
	 */
	public final Computers.Arity1<C, UnsignedIntType> complexToUint32 = (input, output) -> output.set((long) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint32'
	 */
	public final Computers.Arity1<T, UnsignedIntType> integerToUint32 = (input, output) -> output.set(input.getIntegerLong());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.int64'
	 */
	public final Computers.Arity1<C, LongType> complexToInt64 = (input, output) -> output.set((long) input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.int64'
	 */
	public final Computers.Arity1<T, LongType> integerToInt64 = (input, output) -> output.set(input.getIntegerLong());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint64'
	 */
	public final Computers.Arity1<C, UnsignedLongType> complexToUint64 = (input, output) -> {
		final BigDecimal bd = BigDecimal.valueOf(input.getRealDouble());
		final BigDecimal r = bd.remainder(BigDecimal.ONE);
		if (r.compareTo(BigDecimal.ZERO) == 0) {
			output.set(bd.toBigIntegerExact().longValue());
		} else {
			output.set(bd.toBigInteger().longValue());
		}
	};

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint128'
	 */
	public final Computers.Arity1<C, Unsigned128BitType> complexToUint128 = (input, output) -> {
		final BigDecimal bd = BigDecimal.valueOf(input.getRealDouble());
		final BigDecimal r = bd.remainder(BigDecimal.ONE);
		if (r.compareTo(BigDecimal.ZERO) == 0) {
			output.set(bd.toBigIntegerExact());
		} else {
			output.set(bd.toBigInteger());
		}
	};

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.uint128'
	 */
	public final Computers.Arity1<T, Unsigned128BitType> integerToUint128 = (input, output) -> output.set(input.getBigInteger());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.float32'
	 */
	public final Computers.Arity1<C, FloatType> complexToFloat32 = (input, output) -> output.set(input.getRealFloat());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.cfloat32'
	 */
	public final Computers.Arity1<C, ComplexFloatType> complexToCfloat32 = (input, output) -> output.set(input.getRealFloat(),
			input.getImaginaryFloat());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.float64'
	 */
	public final Computers.Arity1<C, DoubleType> complexToFloat64 = (input, output) -> output.set(input.getRealDouble());

	/**
	 * @input input
	 * @container output
	 * @implNote op names='convert.cfloat64'
	 */
	public final Computers.Arity1<C, ComplexDoubleType> complexToCfloat64 = (input, output) -> output.set(input.getRealDouble(),
			input.getImaginaryDouble());
}
