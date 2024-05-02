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

package org.scijava.ops.image.util;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.*;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.list.ListImg;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.scijava.common3.MersenneTwisterFast;
import org.scijava.ops.image.util.UnboundedIntegerType;

import java.math.BigInteger;
import java.util.Random;

public final class TestImgGeneration {

	private TestImgGeneration() {
		// Prevent instantiation of static utility class
	}

	private static int SEED = 17;

	public static ArrayImg<BitType, LongArray> bitArray(final boolean fill,
		final long... dims)
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

	public static ArrayImg<Unsigned12BitType, LongArray> unsigned12BitArray(
		final boolean fill, final long... dims)
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

	public static ArrayImg<Unsigned128BitType, LongArray> unsigned128BitArray(
		final boolean fill, final long... dims)
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

	public static ArrayImg<ByteType, ByteArray> byteArray(final boolean fill,
		final long... dims)
	{
		final byte[] array = new byte[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			Random r = new Random(SEED);
			r.nextBytes(array);
		}

		return ArrayImgs.bytes(array, dims);
	}

	public static ArrayImg<UnsignedByteType, ByteArray> unsignedByteArray(
		final boolean fill, final long... dims)
	{
		final byte[] array = new byte[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			Random r = new Random(SEED);
			r.nextBytes(array);
		}

		return ArrayImgs.unsignedBytes(array, dims);
	}

	public static ArrayImg<IntType, IntArray> intArray(final boolean fill,
		final long... dims)
	{
		final int[] array = new int[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			Random r = new Random(SEED);
			for (int i = 0; i < array.length; i++) {
				array[i] = r.nextInt();
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
			Random r = new Random(SEED);
			for (int i = 0; i < array.length; i++) {
				array[i] = r.nextInt();
			}
		}

		return ArrayImgs.unsignedInts(array, dims);
	}

	public CellImg<ByteType, ?> byteCell(final boolean fill, final long... dims) {
		final CellImg<ByteType, ?> img = new CellImgFactory<>(new ByteType())
			.create(dims);

		if (fill) {
			Random r = new Random(SEED);
			final Cursor<ByteType> c = img.cursor();
			while (c.hasNext())
				c.next().set((byte) r.nextInt());
		}

		return img;
	}

	public CellImg<ByteType, ?> byteCell(final boolean fill, final int[] cellDims,
		final long... dims)
	{
		final CellImg<ByteType, ?> img = new CellImgFactory<>(new ByteType(),
			cellDims).create(dims);

		if (fill) {
			Random r = new Random(SEED);
			final Cursor<ByteType> c = img.cursor();
			while (c.hasNext())
				c.next().set((byte) r.nextInt());
		}

		return img;
	}

	public static ArrayImg<DoubleType, DoubleArray> doubleArray(
		final boolean fill, final long... dims)
	{
		final double[] array = new double[(int) Intervals.numElements(
			new FinalInterval(dims))];

		if (fill) {
			Random r = new Random(SEED);
			for (int i = 0; i < array.length; i++) {
				array[i] = r.nextDouble();
			}
		}

		return ArrayImgs.doubles(array, dims);
	}

	public static ArrayImg<LongType, LongArray> longArray(final boolean fill,
		final long... dims)
	{
		final long[] array = new long[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			Random r = new Random(SEED);
			for (int i = 0; i < array.length; i++) {
				array[i] = r.nextLong();
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
			Random r = new Random(SEED);
			for (int i = 0; i < array.length; i++) {
				array[i] = r.nextLong();
			}
		}

		return ArrayImgs.unsignedLongs(array, dims);
	}

	public static ArrayImg<ShortType, ShortArray> shortArray(final boolean fill,
		final long... dims)
	{
		final short[] array = new short[(int) Intervals.numElements(
			new FinalInterval(dims))];

		if (fill) {
			Random r = new Random(SEED);
			for (int i = 0; i < array.length; i++) {
				array[i] = (short) r.nextInt();
			}
		}

		return ArrayImgs.shorts(array, dims);
	}

	public static ArrayImg<UnsignedShortType, ShortArray> unsignedShortArray(
		final boolean fill, final long... dims)
	{
		final short[] array = new short[(int) Intervals.numElements(
			new FinalInterval(dims))];

		if (fill) {
			Random r = new Random(SEED);
			for (int i = 0; i < array.length; i++) {
				array[i] = (short) r.nextInt();
			}
		}

		return ArrayImgs.unsignedShorts(array, dims);
	}

	public static ArrayImg<UnsignedVariableBitLengthType, LongArray>
		unsignedVariableBitLengthTypeArray(final boolean fill, final int nbits,
			final long... dims)
	{
		final long[] array = new long[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			Random r = new Random(SEED);
			for (int i = 0; i < array.length; i++) {
				array[i] = (long) (r.nextInt() % Math.pow(2, nbits));
			}
		}

		final LongArray l = new LongArray(array);

		return ArrayImgs.unsignedVariableBitLengths(l, nbits, dims);
	}

	public static ListImg<UnboundedIntegerType>
		generateUnboundedIntegerTypeListTestImg(final boolean fill,
			final long... dims)
	{

		final ListImg<UnboundedIntegerType> l = new ListImgFactory<>(
			new UnboundedIntegerType()).create(dims);

		final BigInteger[] array = new BigInteger[(int) Intervals.numElements(
			dims)];

		RandomAccess<UnboundedIntegerType> ra = l.randomAccess();

		if (fill) {
			MersenneTwisterFast betterRNG = new MersenneTwisterFast(0xf1eece);
			for (int i = 0; i < Intervals.numElements(dims); i++) {
				BigInteger val = BigInteger.valueOf(betterRNG.nextLong());
				ra.get().set(val);
				ra.fwd(0);
			}
		}

		return l;
	}

	public static Img<UnsignedByteType> randomlyFilledUnsignedByteWithSeed(
		final long[] dims, final long tempSeed)
	{

		final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(dims);

		final Random rand = new Random(tempSeed);
		final Cursor<UnsignedByteType> cursor = img.cursor();
		while (cursor.hasNext()) {
			cursor.next().set(rand.nextInt((int) img.firstElement().getMaxValue()));
		}

		return img;
	}

	public static ArrayImg<FloatType, FloatArray> floatArray(final boolean fill,
		final long... dims)
	{
		final float[] array = new float[(int) Intervals.numElements(
			new FinalInterval(dims))];

		if (fill) {
			Random r = new Random(SEED);
			for (int i = 0; i < array.length; i++) {
				array[i] = (float) r.nextFloat();
			}
		}

		return ArrayImgs.floats(array, dims);
	}

}
