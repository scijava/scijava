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
	 * @implNote op names='engine.convert', type=Function
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
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert', type=Function
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
	 * @implNote op names='engine.convert', type=Function
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
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert', type=Function
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
	 * @implNote op names='engine.convert', type=Function
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
	 * @implNote op names='engine.convert', type=Function
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
	 * @implNote op names='engine.convert', type=Function
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
	 * @implNote op names='engine.convert', type=Function
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
	 * @implNote op names='engine.convert', type=Function
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
	 * 	values but whose element types are {@link FloatType}s.
	 * @implNote op names='engine.convert', type=Function
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
	 * @implNote op names='engine.convert', type=Function
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
	 * @implNote op names='engine.convert', type=Function
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
	 * @implNote op names='engine.convert', type=Function
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
