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
package org.scijava.ops.image.convert;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.basictypeaccess.*;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Wrapper Ops for {@link RandomAccessibleInterval}s of {@link ComplexType}s.
 * TODO: Add more types
 *
 * @author Gabriel Selzer
 */
public class RAIWrappers {

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.bit', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<BitType> toBitType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new BitType(new LongAccess() {

			@Override
			public long getValue(int index) {
				// TODO: Check correctness
				return sampler.get().getRealDouble() != 0 ? -1L : 0L;
			}

			@Override
			public void setValue(int index, long value) {
				sampler.get().setReal(value != 0 ? 1 : 0);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link Unsigned2BitType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.uint2', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<Unsigned2BitType> toU2BitType(
			final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new Unsigned2BitType(new LongAccess() {

			@Override
			public long getValue(int index) {
				return Types.uint2(sampler.get().getRealDouble());
			}

			@Override
			public void setValue(int index, long value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link Unsigned4BitType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.uint4', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<Unsigned4BitType> toU4BitType(
			final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new Unsigned4BitType(new LongAccess() {

			@Override
			public long getValue(int index) {
				return Types.uint4(sampler.get().getRealDouble());
			}

			@Override
			public void setValue(int index, long value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.int8', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<ByteType> toByteType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new ByteType(new ByteAccess() {

			@Override
			public byte getValue(int index) {
				// TODO: Check correctness
				return (byte) sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, byte value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.uint8', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<UnsignedByteType> toUnsignedByteType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new UnsignedByteType(new ByteAccess() {

			@Override
			public byte getValue(int index) {
				// TODO: Check correctness
				return UnsignedByteType.getCodedSignedByte((int) sampler.get().getRealDouble());
			}

			@Override
			public void setValue(int index, byte value) {
				sampler.get().setReal(UnsignedByteType.getUnsignedByte(value));
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link Unsigned12BitType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.uint12', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<Unsigned12BitType> toU12BitType(
			final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new Unsigned12BitType(new LongAccess() {

			@Override
			public long getValue(int index) {
				return Types.uint12(sampler.get().getRealDouble());
			}

			@Override
			public void setValue(int index, long value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.int16', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<ShortType> toShortType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new ShortType(new ShortAccess() {

			@Override
			public short getValue(int index) {
				return (short) sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, short value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.uint16', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<UnsignedShortType> toUnsignedShortType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new UnsignedShortType(new ShortAccess() {

			@Override
			public short getValue(int index) {
				// TODO: Check correctness
				return UnsignedShortType.getCodedSignedShort((int) sampler.get().getRealDouble());
			}

			@Override
			public void setValue(int index, short value) {
				sampler.get().setReal(UnsignedShortType.getUnsignedShort(value));
				sampler.get().setImaginary(0);
			}
		}));
	}


	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.int32', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<IntType> toIntType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new IntType(new IntAccess() {

			@Override
			public int getValue(int index) {
				return (int) sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, int value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.uint32', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<UnsignedIntType> toUnsignedIntType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new UnsignedIntType(new IntAccess() {

			@Override
			public int getValue(int index) {
				return UnsignedIntType.getCodedSignedInt((long) sampler.get().getRealDouble());
			}

			@Override
			public void setValue(int index, int value) {
				sampler.get().setReal(UnsignedIntType.getUnsignedInt(value));
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.int64', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<LongType> toLongType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new LongType(new LongAccess() {

			@Override
			public long getValue(int index) {
				return (long) sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, long value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.uint64', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<UnsignedLongType> toUnsignedLong(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new UnsignedLongType(new LongAccess() {

			@Override
			public long getValue(int index) {
				return (long) sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, long value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link Unsigned128BitType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.uint128', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<Unsigned128BitType> toU128BitType(
			final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new Unsigned128BitType(new LongAccess() {

			@Override
			public long getValue(int index) {
				// This code is adapted from:
				// net.imglib2.type.numeric.integer.Unsigned128BitType.setReal
				double value = sampler.get().getRealDouble();
				value = Math.floor( value + 0.5 );
				final double base = Math.pow( 2, 64 );
				double upper = Math.floor(value / base );
				double lower = value - base * upper;
				if (index % 2 == 0) { // lower long
					return Types.int64(lower);
				}
				else { // upper long
					return Types.int64(upper);
				}
			}

			@Override
			public void setValue(int index, long value) {
				// FIXME
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.float32', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<FloatType> toFloatType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new FloatType(new FloatAccess() {

			@Override
			public float getValue(int index) {
				return sampler.get().getRealFloat();
			}

			@Override
			public void setValue(int index, float value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.cfloat32', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<ComplexFloatType> toComplexFloatType(
		final RAIC input)
	{
		return Converters.convert(input, sampler -> new ComplexFloatType(new FloatAccess() {

			@Override
			public float getValue(int index) {
				if (index % 2 == 0) {
					return sampler.get().getRealFloat();
				}
				else {
					return sampler.get().getImaginaryFloat();
				}
			}

			@Override
			public void setValue(int index, float value) {
				if (index % 2 == 0) {
					sampler.get().setReal(value);
				}
				else {
					sampler.get().setImaginary(value);
				}
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link DoubleType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.float64', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<DoubleType> toDoubleType(
		final RAIC input)
	{
		return net.imglib2.converter.Converters.convert(input, sampler -> new DoubleType(new DoubleAccess() {

			@Override
			public double getValue(int index) {
				return sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, double value) {
				sampler.get().setReal(value);
				sampler.get().setImaginary(0);
			}
		}));
	}

	/**
	 * @param input the input image
	 * @return an output image whose values are equivalent to {@code input}'s
	 * 	values but whose element types are {@link ComplexDoubleType}s.
	 * @implNote op names='engine.convert, engine.wrap, convert.uint64', type=Function
	 */
	public static <C extends ComplexType<C>, RAIC extends RandomAccessibleInterval<C>> RandomAccessibleInterval<ComplexDoubleType> toComplexDoubleType(
		final RAIC input)
	{
		return Converters.convert(input, sampler -> new ComplexDoubleType(new DoubleAccess() {

			@Override
			public double getValue(int index) {
				if (index % 2 == 0) {
					return sampler.get().getRealDouble();
				}
				else {
					return sampler.get().getImaginaryDouble();
				}
			}

			@Override
			public void setValue(int index, double value) {
				if (index % 2 == 0) {
					sampler.get().setReal(value);
				}
				else {
					sampler.get().setImaginary(value);
				}
			}
		}));
	}
}
