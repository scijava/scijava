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
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Converters for converting between Complex types
 *
 * @author Alison Walter
 */
@Plugin(type = OpCollection.class)
public final class ConvertTypes<C extends ComplexType<C>, T extends IntegerType<T>> {

	@OpField(names = "convert.bit", params = "input, output")
	public final Computers.Arity1<C, BitType> complexToBit = (input, output) -> output.set(input.getRealDouble() != 0);

	@OpField(names = "convert.bit", params = "input, output")
	public final Computers.Arity1<T, BitType> integerToBit = (input, output) -> output.set(input.getIntegerLong() != 0);

	@OpField(names = "convert.uint2", params = "input, output")
	public final Computers.Arity1<C, Unsigned2BitType> complexToUint2 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.uint2", params = "input, output")
	public final Computers.Arity1<T, Unsigned2BitType> integerToUint2 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.uint4", params = "input, output")
	public final Computers.Arity1<C, Unsigned4BitType> complexToUint4 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.uint4", params = "input, output")
	public final Computers.Arity1<T, Unsigned4BitType> integerToUint4 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.byte", params = "input, output")
	public final Computers.Arity1<C, ByteType> complexToInt8 = (input, output) -> output.set((byte) input.getRealDouble());

	@OpField(names = "convert.byte", params = "input, output")
	public final Computers.Arity1<T, ByteType> integerToInt8 = (input, output) -> output.set((byte) input.getIntegerLong());

	@OpField(names = "convert.uint8", params = "input, output")
	public final Computers.Arity1<C, UnsignedByteType> complexToUint8 = (input, output) -> output.set((int) input.getRealDouble());

	@OpField(names = "convert.uint8", params = "input, output")
	public final Computers.Arity1<T, UnsignedByteType> integerToUint8 = (input, output) -> output.set(input.getInteger());

	@OpField(names = "convert.uint12", params = "input, output")
	public final Computers.Arity1<C, Unsigned12BitType> complexToUint12 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.uint12", params = "input, output")
	public final Computers.Arity1<T, Unsigned12BitType> integerToUint12 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.int16", params = "input, output")
	public final Computers.Arity1<C, ShortType> complexToInt16 = (input, output) -> output.set((short) input.getRealDouble());

	@OpField(names = "convert.int16", params = "input, output")
	public final Computers.Arity1<T, ShortType> integerToInt16 = (input, output) -> output.set((short) input.getIntegerLong());

	@OpField(names = "convert.uint16", params = "input, output")
	public final Computers.Arity1<C, UnsignedShortType> complexToUint16 = (input, output) -> output.set((int) input.getRealDouble());

	@OpField(names = "convert.uint16", params = "input, output")
	public final Computers.Arity1<T, UnsignedShortType> integerToUint16 = (input, output) -> output.set(input.getInteger());

	@OpField(names = "convert.int32", params = "input, output")
	public final Computers.Arity1<C, IntType> complexToInt32 = (input, output) -> output.set((int) input.getRealDouble());

	@OpField(names = "convert.int32", params = "input, output")
	public final Computers.Arity1<T, IntType> integerToInt32 = (input, output) -> output.set(input.getInteger());

	@OpField(names = "convert.uint32", params = "input, output")
	public final Computers.Arity1<C, UnsignedIntType> complexToUint32 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.uint32", params = "input, output")
	public final Computers.Arity1<T, UnsignedIntType> integerToUint32 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.int64", params = "input, output")
	public final Computers.Arity1<C, LongType> complexToInt64 = (input, output) -> output.set((long) input.getRealDouble());

	@OpField(names = "convert.int64", params = "input, output")
	public final Computers.Arity1<T, LongType> integerToInt64 = (input, output) -> output.set(input.getIntegerLong());

	@OpField(names = "convert.uint64", params = "input, output")
	public final Computers.Arity1<C, UnsignedLongType> complexToUint64 = (input, output) -> {
		final BigDecimal bd = BigDecimal.valueOf(input.getRealDouble());
		final BigDecimal r = bd.remainder(BigDecimal.ONE);
		if (r.compareTo(BigDecimal.ZERO) == 0) {
			output.set(bd.toBigIntegerExact().longValue());
		} else {
			output.set(bd.toBigInteger().longValue());
		}
	};

	@OpField(names = "convert.uint128", params = "input, output")
	public final Computers.Arity1<C, Unsigned128BitType> complexToUint128 = (input, output) -> {
		final BigDecimal bd = BigDecimal.valueOf(input.getRealDouble());
		final BigDecimal r = bd.remainder(BigDecimal.ONE);
		if (r.compareTo(BigDecimal.ZERO) == 0) {
			output.set(bd.toBigIntegerExact());
		} else {
			output.set(bd.toBigInteger());
		}
	};

	@OpField(names = "convert.uint128", params = "input, output")
	public final Computers.Arity1<T, Unsigned128BitType> integerToUint128 = (input, output) -> output.set(input.getBigInteger());

	@OpField(names = "convert.float32", params = "input, output")
	public final Computers.Arity1<C, FloatType> complexToFloat32 = (input, output) -> output.set(input.getRealFloat());

	@OpField(names = "convert.cfloat32", params = "input, output")
	public final Computers.Arity1<C, ComplexFloatType> complexToCfloat32 = (input, output) -> output.set(input.getRealFloat(),
			input.getImaginaryFloat());

	@OpField(names = "convert.float64", params = "input, output")
	public final Computers.Arity1<C, DoubleType> complexToFloat64 = (input, output) -> output.set(input.getRealDouble());

	@OpField(names = "convert.cfloat64", params = "input, output")
	public final Computers.Arity1<C, ComplexDoubleType> complexToCfloat64 = (input, output) -> output.set(input.getRealDouble(),
			input.getImaginaryDouble());
}
