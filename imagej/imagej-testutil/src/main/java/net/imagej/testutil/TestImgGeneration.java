package net.imagej.testutil;

import java.math.BigInteger;
import java.util.Random;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.logic.BitType;
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
import net.imglib2.util.Intervals;

import org.scijava.util.MersenneTwisterFast;

public class TestImgGeneration {

	private static int seed;
	
	private static int pseudoRandom() {
		return seed = 3170425 * seed + 132102;
	}

	public static ArrayImg<BitType, LongArray> bitArray(
		final boolean fill, final long... dims)
	{
		ArrayImg<BitType, LongArray> bits = ArrayImgs.bits(dims);

		if (fill) {
			MersenneTwisterFast betterRNG = new MersenneTwisterFast(0xf1eece);
			for (BitType b : bits) {
				b.set(betterRNG.nextBoolean());
			}
		}
		return bits;
	}

	public static ArrayImg<Unsigned2BitType, LongArray> unsigned2BitArray(
		final boolean fill, final long... dims)
	{
		ArrayImg<Unsigned2BitType, LongArray> bits = ArrayImgs.unsigned2Bits(dims);

		if (fill) {
			MersenneTwisterFast betterRNG = new MersenneTwisterFast(0xf1eece);
			for (Unsigned2BitType b : bits) {
				b.set(betterRNG.nextLong());
			}
		}
		return bits;
	}

	public static ArrayImg<Unsigned4BitType, LongArray> unsigned4BitArray(
		final boolean fill, final long... dims)
	{
		ArrayImg<Unsigned4BitType, LongArray> bits = ArrayImgs.unsigned4Bits(dims);

		if (fill) {
			MersenneTwisterFast betterRNG = new MersenneTwisterFast(0xf1eece);
			for (Unsigned4BitType b : bits) {
				b.set(betterRNG.nextLong());
			}
		}
		return bits;
	}

	public static ArrayImg<Unsigned12BitType, LongArray>
		unsigned12BitArray(final boolean fill, final long... dims)
	{
		ArrayImg<Unsigned12BitType, LongArray> bits = ArrayImgs.unsigned12Bits(
			dims);

		if (fill) {
			MersenneTwisterFast betterRNG = new MersenneTwisterFast(0xf1eece);
			for (Unsigned12BitType b : bits) {
				b.set(betterRNG.nextLong());
			}
		}
		return bits;
	}

	public static ArrayImg<Unsigned128BitType, LongArray>
		unsigned128BitArray(final boolean fill, final long... dims)
	{
		ArrayImg<Unsigned128BitType, LongArray> bits = ArrayImgs.unsigned128Bits(
			dims);

		if (fill) {
			MersenneTwisterFast betterRNG = new MersenneTwisterFast(0xf1eece);
			for (Unsigned128BitType b : bits) {
				BigInteger big = BigInteger.valueOf(betterRNG.nextLong());
				b.set(big);
			}
		}
		return bits;
	}

	public static ArrayImg<ByteType, ByteArray> byteArray(
		final boolean fill, final long... dims)
	{
		final byte[] array = new byte[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = (byte) pseudoRandom();
			}
		}

		return ArrayImgs.bytes(array, dims);
	}

	public static ArrayImg<UnsignedByteType, ByteArray> unsignedByteArray(
		final boolean fill, final long... dims)
	{
		final byte[] array = new byte[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = (byte) pseudoRandom();
			}
		}

		return ArrayImgs.unsignedBytes(array, dims);
	}

	public static ArrayImg<IntType, IntArray> intArray(final boolean fill,
		final long... dims)
	{
		final int[] array = new int[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = pseudoRandom() / Integer.MAX_VALUE;
			}
		}

		return ArrayImgs.ints(array, dims);
	}

	public static ArrayImg<UnsignedIntType, IntArray> unsignedIntArray(
		final boolean fill, final long... dims)
	{
		final int[] array = new int[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = pseudoRandom() / Integer.MAX_VALUE;
			}
		}

		return ArrayImgs.unsignedInts(array, dims);
	}

	public CellImg<ByteType, ?> byteCell(final boolean fill,
		final long... dims)
	{
		final CellImg<ByteType, ?> img = new CellImgFactory<>(new ByteType()).create(dims);

		if (fill) {
			final Cursor<ByteType> c = img.cursor();
			while (c.hasNext())
				c.next().set((byte) pseudoRandom());
		}

		return img;
	}

	public CellImg<ByteType, ?> byteCell(final boolean fill,
		final int[] cellDims, final long... dims)
	{
		final CellImg<ByteType, ?> img = new CellImgFactory<>(new ByteType(), cellDims).create(dims);

		if (fill) {
			final Cursor<ByteType> c = img.cursor();
			while (c.hasNext())
				c.next().set((byte) pseudoRandom());
		}

		return img;
	}

	public static ArrayImg<DoubleType, DoubleArray> doubleArray(
		final boolean fill, final long... dims)
	{
		final double[] array = new double[(int) Intervals.numElements(
			new FinalInterval(dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = (double) pseudoRandom() / (double) Integer.MAX_VALUE;
			}
		}

		return ArrayImgs.doubles(array, dims);
	}

	public static ArrayImg<LongType, LongArray> longArray(
		final boolean fill, final long... dims)
	{
		final long[] array = new long[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = pseudoRandom() / Integer.MAX_VALUE;
			}
		}

		return ArrayImgs.longs(array, dims);
	}

	public static ArrayImg<UnsignedLongType, LongArray> unsignedLongArray(
		final boolean fill, final long... dims)
	{
		final long[] array = new long[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = pseudoRandom() / Integer.MAX_VALUE;
			}
		}

		return ArrayImgs.unsignedLongs(array, dims);
	}

	public static ArrayImg<ShortType, ShortArray> shortArray(
		final boolean fill, final long... dims)
	{
		final short[] array = new short[(int) Intervals.numElements(
			new FinalInterval(dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = (short) (pseudoRandom() / Integer.MAX_VALUE);
			}
		}

		return ArrayImgs.shorts(array, dims);
	}

	public static ArrayImg<UnsignedShortType, ShortArray>
		unsignedShortArray(final boolean fill, final long... dims)
	{
		final short[] array = new short[(int) Intervals.numElements(
			new FinalInterval(dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = (short) (pseudoRandom() / Integer.MAX_VALUE);
			}
		}

		return ArrayImgs.unsignedShorts(array, dims);
	}

	public static ArrayImg<UnsignedVariableBitLengthType, LongArray>
		unsignedVariableBitLengthTypeArray(final boolean fill,
			final int nbits, final long... dims)
	{
		final long[] array = new long[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = (long) (pseudoRandom() / Integer.MAX_VALUE % Math.pow(2, nbits)) ;
			}
		}

		final LongArray l = new LongArray(array);

		return ArrayImgs.unsignedVariableBitLengths(l, nbits, dims);
	}

	// TODO: uncomment and add dependency on imagej-common or delete
//	public ListImg<UnboundedIntegerType> generateUnboundedIntegerTypeListTestImg(
//		final boolean fill, final long... dims)
//	{
//
//		final ListImg<UnboundedIntegerType> l =
//			new ListImgFactory<>(new UnboundedIntegerType()).create(dims);
//
//		final BigInteger[] array = new BigInteger[(int) Intervals.numElements(
//			dims)];
//
//		RandomAccess<UnboundedIntegerType> ra = l.randomAccess();
//
//		if (fill) {
//			MersenneTwisterFast betterRNG = new MersenneTwisterFast(0xf1eece);
//			for (int i = 0; i < Intervals.numElements(dims); i++) {
//				BigInteger val = BigInteger.valueOf(betterRNG.nextLong());
//				ra.get().set(val);
//				ra.fwd(0);
//			}
//		}
//
//		return l;
//	}

	public static Img<UnsignedByteType>
		randomlyFilledUnsignedByteWithSeed(final long[] dims,
			final long tempSeed)
	{

		final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(dims);

		final Random rand = new Random(tempSeed);
		final Cursor<UnsignedByteType> cursor = img.cursor();
		while (cursor.hasNext()) {
			cursor.next().set(rand.nextInt((int) img.firstElement().getMaxValue()));
		}

		return img;
	}

	public static ArrayImg<FloatType, FloatArray> floatArray(
		final boolean fill, final long... dims)
	{
		final float[] array = new float[(int) Intervals.numElements(
			new FinalInterval(dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = (float) pseudoRandom() / (float) Integer.MAX_VALUE;
			}
		}

		return ArrayImgs.floats(array, dims);
	}

}
