/*
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

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ComplexType;
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
import org.scijava.ops.spi.OpDependency;

/**
 * Converters for converting between images of Complex types
 *
 * @author Gabriel Selzer
 */
public final class ConvertImages {

	/**
	 * @implNote op names='convert.bit, engine.convert', type=Function
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link BitType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}s
	 *         values but whose element types are {@link BitType}s.
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<BitType> typeToBit(@OpDependency(
			name = "create.img") BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> creator,
			@OpDependency(
				name = "convert.bit") Computers.Arity1<C, BitType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<BitType> output = creator.apply(input,
			new BitType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link Unsigned2BitType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link Unsigned2BitType}s.
	 * @implNote op names='convert.uint2, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<Unsigned2BitType> typeToUnsigned2Bit(@OpDependency(
			name = "create.img") BiFunction<Dimensions, Unsigned2BitType, RandomAccessibleInterval<Unsigned2BitType>> creator,
			@OpDependency(
				name = "convert.uint2") Computers.Arity1<C, Unsigned2BitType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<Unsigned2BitType> output = creator.apply(input,
			new Unsigned2BitType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link Unsigned4BitType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link Unsigned4BitType}s.
	 * @implNote op names='convert.uint4, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<Unsigned4BitType> typeToUnsigned4Bit(@OpDependency(
			name = "create.img") BiFunction<Dimensions, Unsigned4BitType, RandomAccessibleInterval<Unsigned4BitType>> creator,
			@OpDependency(
				name = "convert.uint4") Computers.Arity1<C, Unsigned4BitType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<Unsigned4BitType> output = creator.apply(input,
			new Unsigned4BitType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link ByteType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link ByteType}s.
	 * @implNote op names='convert.int8, convert.byte, engine.convert',
	 *           type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<ByteType> typeToByte(@OpDependency(
			name = "create.img") BiFunction<Dimensions, ByteType, RandomAccessibleInterval<ByteType>> creator,
			@OpDependency(
				name = "convert.int8") Computers.Arity1<C, ByteType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<ByteType> output = creator.apply(input,
			new ByteType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link UnsignedByteType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link UnsignedByteType}s.
	 * @implNote op names='convert.uint8, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<UnsignedByteType> typeToUnsignedByte(@OpDependency(
			name = "create.img") BiFunction<Dimensions, UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>> creator,
			@OpDependency(
				name = "convert.uint8") Computers.Arity1<C, UnsignedByteType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<UnsignedByteType> output = creator.apply(input,
			new UnsignedByteType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link Unsigned12BitType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link Unsigned12BitType}s.
	 * @implNote op names='convert.uint12, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<Unsigned12BitType> typeToUnsigned12Bit(
			@OpDependency(
				name = "create.img") BiFunction<Dimensions, Unsigned12BitType, RandomAccessibleInterval<Unsigned12BitType>> creator,
			@OpDependency(
				name = "convert.uint12") Computers.Arity1<C, Unsigned12BitType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<Unsigned12BitType> output = creator.apply(input,
			new Unsigned12BitType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link ShortType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link ShortType}s.
	 * @implNote op names='convert.int16, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<ShortType> typeToShort(@OpDependency(
			name = "create.img") BiFunction<Dimensions, ShortType, RandomAccessibleInterval<ShortType>> creator,
			@OpDependency(
				name = "convert.int16") Computers.Arity1<C, ShortType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<ShortType> output = creator.apply(input,
			new ShortType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link UnsignedShortType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link UnsignedShortType}s.
	 * @implNote op names='convert.uint16, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<UnsignedShortType> typeToUnsignedShort(
			@OpDependency(
				name = "create.img") BiFunction<Dimensions, UnsignedShortType, RandomAccessibleInterval<UnsignedShortType>> creator,
			@OpDependency(
				name = "convert.uint16") Computers.Arity1<C, UnsignedShortType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<UnsignedShortType> output = creator.apply(input,
			new UnsignedShortType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link IntType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link IntType}s.
	 * @implNote op names='convert.int32, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<IntType> typeToInt(@OpDependency(
			name = "create.img") BiFunction<Dimensions, IntType, RandomAccessibleInterval<IntType>> creator,
			@OpDependency(
				name = "convert.int32") Computers.Arity1<C, IntType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<IntType> output = creator.apply(input,
			new IntType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link UnsignedIntType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link UnsignedIntType}s.
	 * @implNote op names='convert.uint32, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<UnsignedIntType> typeToUnsignedInt(@OpDependency(
			name = "create.img") BiFunction<Dimensions, UnsignedIntType, RandomAccessibleInterval<UnsignedIntType>> creator,
			@OpDependency(
				name = "convert.uint32") Computers.Arity1<C, UnsignedIntType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<UnsignedIntType> output = creator.apply(input,
			new UnsignedIntType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link LongType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link LongType}s.
	 * @implNote op names='convert.int64, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<LongType> typeToLong(@OpDependency(
			name = "create.img") BiFunction<Dimensions, LongType, RandomAccessibleInterval<LongType>> creator,
			@OpDependency(
				name = "convert.int64") Computers.Arity1<C, LongType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<LongType> output = creator.apply(input,
			new LongType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link UnsignedLongType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link UnsignedLongType}s.
	 * @implNote op names='convert.uint64, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<UnsignedLongType> typeToUnsignedLong(@OpDependency(
			name = "create.img") BiFunction<Dimensions, UnsignedLongType, RandomAccessibleInterval<UnsignedLongType>> creator,
			@OpDependency(
				name = "convert.uint64") Computers.Arity1<C, UnsignedLongType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<UnsignedLongType> output = creator.apply(input,
			new UnsignedLongType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link Unsigned128BitType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link Unsigned128BitType}s.
	 * @implNote op names='convert.uint128, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<Unsigned128BitType> typeToUnsigned128Bit(
			@OpDependency(
				name = "create.img") BiFunction<Dimensions, Unsigned128BitType, RandomAccessibleInterval<Unsigned128BitType>> creator,
			@OpDependency(
				name = "convert.uint128") Computers.Arity1<C, Unsigned128BitType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<Unsigned128BitType> output = creator.apply(input,
			new Unsigned128BitType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link FloatType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link FloatType}s.
	 * @implNote op names='convert.float32, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<FloatType> typeToFloat32(@OpDependency(
			name = "create.img") BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> creator,
			@OpDependency(
				name = "convert.float32") Computers.Arity1<C, FloatType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<FloatType> output = creator.apply(input,
			new FloatType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link ComplexFloatType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link ComplexFloatType}s.
	 * @implNote op names='convert.cfloat32, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<ComplexFloatType> typeToComplexFloat32(
			@OpDependency(
				name = "create.img") BiFunction<Dimensions, ComplexFloatType, RandomAccessibleInterval<ComplexFloatType>> creator,
			@OpDependency(
				name = "convert.cfloat32") Computers.Arity1<C, ComplexFloatType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<ComplexFloatType> output = creator.apply(input,
			new ComplexFloatType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link DoubleType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link DoubleType}s.
	 * @implNote op names='convert.float64, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<DoubleType> typeToDouble32(@OpDependency(
			name = "create.img") BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> creator,
			@OpDependency(
				name = "convert.float64") Computers.Arity1<C, DoubleType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<DoubleType> output = creator.apply(input,
			new DoubleType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

	/**
	 * @param creator a {@link BiFunction} to create the output image
	 * @param converter a {@link Computers.Arity1} to convert the type to a
	 *          {@link ComplexDoubleType}
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 *         values but whose element types are {@link ComplexDoubleType}s.
	 * @implNote op names='convert.cfloat64, engine.convert', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>>
		RandomAccessibleInterval<ComplexDoubleType> typeToComplexDouble32(
			@OpDependency(
				name = "create.img") BiFunction<Dimensions, ComplexDoubleType, RandomAccessibleInterval<ComplexDoubleType>> creator,
			@OpDependency(
				name = "convert.cfloat64") Computers.Arity1<C, ComplexDoubleType> converter,
			final RAIC input)
	{
		RandomAccessibleInterval<ComplexDoubleType> output = creator.apply(input,
			new ComplexDoubleType());
		LoopBuilder.setImages(input, output).forEachPixel(converter);
		return output;
	}

}
