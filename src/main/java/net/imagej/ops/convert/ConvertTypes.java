/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops.convert;

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

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.Computer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Converters for converting between Complex types
 *
 * @author Alison Walter
 */
@Plugin(type = OpCollection.class)
public final class ConvertTypes<C extends ComplexType<C>, T extends IntegerType<T>> {

	@OpField(names = "convert.bit")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, BitType> complexToBit = (input, output) -> output.set(input.getRealDouble() != 0);

	@OpField(names = "convert.bit")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, BitType> integerToBit = (input, output) -> output.set(input.getIntegerLong() != 0);

	@OpField(names = "convert.uint2")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, Unsigned2BitType> complexToUint2 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.uint2")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, Unsigned2BitType> integerToUint2 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.uint4")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, Unsigned4BitType> complexToUint4 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.uint4")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, Unsigned4BitType> integerToUint4 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.byte")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, ByteType> complexToInt8 = (input, output) -> output.set((byte) input.getRealDouble());

	@OpField(names = "convert.byte")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, ByteType> integerToInt8 = (input, output) -> output.set((byte) input.getIntegerLong());

	@OpField(names = "convert.uint8")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, UnsignedByteType> complexToUint8 = (input, output) -> output.set((int) input.getRealDouble());

	@OpField(names = "convert.uint8")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, UnsignedByteType> integerToUint8 = (input, output) -> output.set(input.getInteger());

	@OpField(names = "convert.uint12")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, Unsigned12BitType> complexToUint12 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.uint12")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, Unsigned12BitType> integerToUint12 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.int16")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, ShortType> complexToInt16 = (input, output) -> output.set((short) input.getRealDouble());

	@OpField(names = "convert.int16")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, ShortType> integerToInt16 = (input, output) -> output.set((short) input.getIntegerLong());

	@OpField(names = "convert.uint16")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, UnsignedShortType> complexToUint16 = (input, output) -> output.set((int) input.getRealDouble());

	@OpField(names = "convert.uint16")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, UnsignedShortType> integerToUint16 = (input, output) -> output.set(input.getInteger());

	@OpField(names = "convert.int32")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, IntType> complexToInt32 = (input, output) -> output.set((int) input.getRealDouble());

	@OpField(names = "convert.int32")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, IntType> integerToInt32 = (input, output) -> output.set(input.getInteger());

	@OpField(names = "convert.uint32")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, UnsignedIntType> complexToUint32 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.uint32")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, UnsignedIntType> integerToUint32 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.int64")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, LongType> complexToInt64 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.int64")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, LongType> integerToInt64 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.uint64")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, UnsignedLongType> complexToUint64 = (input, output) -> {
		final BigDecimal bd = BigDecimal.valueOf(input.getRealDouble());
		final BigDecimal r = bd.remainder(BigDecimal.ONE);
		if (r.compareTo(BigDecimal.ZERO) == 0) {
			output.set(bd.toBigIntegerExact().longValue());
		} else {
			output.set(bd.toBigInteger().longValue());
		}
	};

	@OpField(names = "convert.uint128")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, Unsigned128BitType> complexToUint128 = (input, output) -> {
		final BigDecimal bd = BigDecimal.valueOf(input.getRealDouble());
		final BigDecimal r = bd.remainder(BigDecimal.ONE);
		if (r.compareTo(BigDecimal.ZERO) == 0) {
			output.set(bd.toBigIntegerExact());
		} else {
			output.set(bd.toBigInteger());
		}
	};

	@OpField(names = "convert.uint128")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<T, Unsigned128BitType> integerToUint128 = (input, output) -> output.set(input.getBigInteger());

	@OpField(names = "convert.float32")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, FloatType> complexToFloat32 = (input, output) -> output.set(input.getRealFloat());

	@OpField(names = "convert.cfloat32")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, ComplexFloatType> complexToCfloat32 = (input, output) -> output.set(input.getRealFloat(),
			input.getImaginaryFloat());

	@OpField(names = "convert.float64")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, DoubleType> complexToFloat64 = (input, output) -> output.set(input.getRealDouble());

	@OpField(names = "convert.cfloat64")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computer<C, ComplexDoubleType> complexToCfloat64 = (input, output) -> output.set(input.getRealDouble(),
			input.getImaginaryDouble());
}
