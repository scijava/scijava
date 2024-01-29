
package org.scijava.ops.image.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.FinalDimensions;
import net.imglib2.IterableInterval;
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
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link ConvertTypes} ops.
 *
 * @author Alison Walter
 */
public class TestConvertImages extends AbstractOpTest {

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
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint2ToBit() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint4ToBit() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testInt8ToBit() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint8ToBit() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint12ToBit() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testInt16ToBit() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint16ToBit() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testInt32ToBit() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint32ToBit() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testInt64ToBit() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint64ToBit() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testUint128ToBit() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testFloat32ToBit() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testCfloat32ToBit() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testFloat64ToBit() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToBit}. */
	@Test
	public void testCfloat64ToBit() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.bit").input(img).apply();
		var cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.bit").input(img).apply();
		cursor = ((IterableInterval<BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.bit(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testBitToUint2() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint2ToUint2() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint4ToUint2() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testInt8ToUint2() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint8ToUint2() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint12ToUint2() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testInt16ToUint2() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint16ToUint2() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testInt32ToUint2() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint32ToUint2() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testInt64ToUint2() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint64ToUint2() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testUint128ToUint2() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testFloat32ToUint2() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testCfloat32ToUint2() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testFloat64ToUint2() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint2}. */
	@Test
	public void testCfloat64ToUint2() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint2").input(img).apply();
		var cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint2").input(img).apply();
		cursor = ((IterableInterval<Unsigned2BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint2(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testBitToUint4() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint2ToUint4() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint4ToUint4() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testInt8ToUint4() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint8ToUint4() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint12ToUint4() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testInt16ToUint4() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint16ToUint4() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testInt32ToUint4() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint32ToUint4() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testInt64ToUint4() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint64ToUint4() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testUint128ToUint4() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testFloat32ToUint4() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testCfloat32ToUint4() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testFloat64ToUint4() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint4}. */
	@Test
	public void testCfloat64ToUint4() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint4").input(img).apply();
		var cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint4").input(img).apply();
		cursor = ((IterableInterval<Unsigned4BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint4(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testBitToInt8() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint2ToInt8() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint4ToInt8() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testInt8ToInt8() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint8ToInt8() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint12ToInt8() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testInt16ToInt8() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint16ToInt8() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testInt32ToInt8() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint32ToInt8() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testInt64ToInt8() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint64ToInt8() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testUint128ToInt8() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testFloat32ToInt8() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testCfloat32ToInt8() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testFloat64ToInt8() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt8}. */
	@Test
	public void testCfloat64ToInt8() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int8").input(img).apply();
		var cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int8").input(img).apply();
		cursor = ((IterableInterval<ByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int8(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testBitToUint8() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint2ToUint8() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint4ToUint8() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testInt8ToUint8() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint8ToUint8() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint12ToUint8() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testInt16ToUint8() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint16ToUint8() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testInt32ToUint8() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint32ToUint8() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testInt64ToUint8() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint64ToUint8() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testUint128ToUint8() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testFloat32ToUint8() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testCfloat32ToUint8() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testFloat64ToUint8() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint8}. */
	@Test
	public void testCfloat64ToUint8() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint8").input(img).apply();
		var cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint8").input(img).apply();
		cursor = ((IterableInterval<UnsignedByteType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint8(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testBitToUint12() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint2ToUint12() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint4ToUint12() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testInt8ToUint12() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint8ToUint12() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint12ToUint12() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testInt16ToUint12() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint16ToUint12() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testInt32ToUint12() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint32ToUint12() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testInt64ToUint12() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint64ToUint12() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testUint128ToUint12() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testFloat32ToUint12() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testCfloat32ToUint12() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testFloat64ToUint12() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint12}. */
	@Test
	public void testCfloat64ToUint12() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint12").input(img).apply();
		var cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint12").input(img).apply();
		cursor = ((IterableInterval<Unsigned12BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint12(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testBitToInt16() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint2ToInt16() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint4ToInt16() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testInt8ToInt16() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint8ToInt16() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint12ToInt16() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testInt16ToInt16() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint16ToInt16() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testInt32ToInt16() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint32ToInt16() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testInt64ToInt16() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint64ToInt16() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testUint128ToInt16() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testFloat32ToInt16() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testCfloat32ToInt16() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testFloat64ToInt16() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt16}. */
	@Test
	public void testCfloat64ToInt16() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int16").input(img).apply();
		var cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int16").input(img).apply();
		cursor = ((IterableInterval<ShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int16(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testBitToUint16() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint2ToUint16() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint4ToUint16() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testInt8ToUint16() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint8ToUint16() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint12ToUint16() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testInt16ToUint16() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint16ToUint16() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testInt32ToUint16() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint32ToUint16() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testInt64ToUint16() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint64ToUint16() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testUint128ToUint16() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testFloat32ToUint16() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testCfloat32ToUint16() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testFloat64ToUint16() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint16}. */
	@Test
	public void testCfloat64ToUint16() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint16").input(img).apply();
		var cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint16").input(img).apply();
		cursor = ((IterableInterval<UnsignedShortType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint16(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testBitToInt32() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint2ToInt32() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint4ToInt32() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testInt8ToInt32() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint8ToInt32() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint12ToInt32() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testInt16ToInt32() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint16ToInt32() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testInt32ToInt32() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint32ToInt32() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testInt64ToInt32() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint64ToInt32() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testUint128ToInt32() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testFloat32ToInt32() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testCfloat32ToInt32() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testFloat64ToInt32() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt32}. */
	@Test
	public void testCfloat64ToInt32() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int32").input(img).apply();
		var cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int32").input(img).apply();
		cursor = ((IterableInterval<IntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int32(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testBitToUint32() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint2ToUint32() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint4ToUint32() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testInt8ToUint32() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint8ToUint32() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint12ToUint32() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testInt16ToUint32() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint16ToUint32() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testInt32ToUint32() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint32ToUint32() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testInt64ToUint32() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint64ToUint32() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testUint128ToUint32() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testFloat32ToUint32() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testCfloat32ToUint32() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testFloat64ToUint32() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint32}. */
	@Test
	public void testCfloat64ToUint32() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint32").input(img).apply();
		var cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint32").input(img).apply();
		cursor = ((IterableInterval<UnsignedIntType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint32(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testBitToInt64() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint2ToInt64() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint4ToInt64() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testInt8ToInt64() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint8ToInt64() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint12ToInt64() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testInt16ToInt64() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint16ToInt64() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testInt32ToInt64() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint32ToInt64() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testInt64ToInt64() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint64ToInt64() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(p64), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testUint128ToInt64() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testFloat32ToInt64() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testCfloat32ToInt64() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testFloat64ToInt64() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToInt64}. */
	@Test
	public void testCfloat64ToInt64() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.int64").input(img).apply();
		var cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.int64").input(img).apply();
		cursor = ((IterableInterval<LongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.int64(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testBitToUint64() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(1), cursor.next().getBigInteger());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint2ToUint64() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(2), cursor.next().getBigInteger());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint4ToUint64() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(15), cursor.next().getBigInteger());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testInt8ToUint64() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64((byte) 8), cursor.next().getBigInteger());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64((byte) 0), cursor.next().getBigInteger());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64((byte) -12), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint8ToUint64() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(100), cursor.next().getBigInteger());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint12ToUint64() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(212L), cursor.next().getBigInteger());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0L), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testInt16ToUint64() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64((short) 52), cursor.next().getBigInteger());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64((short) 0), cursor.next().getBigInteger());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64((short) -154), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint16ToUint64() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(480), cursor.next().getBigInteger());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testInt32ToUint64() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(301), cursor.next().getBigInteger());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0), cursor.next().getBigInteger());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(-89), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint32ToUint64() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(20L), cursor.next().getBigInteger());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0L), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testInt64ToUint64() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(891L), cursor.next().getBigInteger());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0L), cursor.next().getBigInteger());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(-1024L), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint64ToUint64() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(1049L), cursor.next().getBigInteger());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0L), cursor.next().getBigInteger());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(p64), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testUint128ToUint64() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(beef), cursor.next().getBigInteger());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(biZero), cursor.next().getBigInteger());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(p128), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testFloat32ToUint64() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(123453.125f), cursor.next().getBigInteger());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0f), cursor.next().getBigInteger());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(-2523485349058.0f), cursor.next()
				.getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testCfloat32ToUint64() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(5839.25f), cursor.next().getBigInteger());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0f), cursor.next().getBigInteger());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(-4.25f), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testFloat64ToUint64() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(4098d), cursor.next().getBigInteger());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0d), cursor.next().getBigInteger());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(-10948.015625d), cursor.next().getBigInteger());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(1.0000152587890625e20), cursor.next()
				.getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint64}. */
	@Test
	public void testCfloat64ToUint64() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint64").input(img).apply();
		var cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(9087d), cursor.next().getBigInteger());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(0d), cursor.next().getBigInteger());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint64").input(img).apply();
		cursor = ((IterableInterval<UnsignedLongType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64(-234.25d), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToUint128() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(1), cursor.next().get());
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToUint128() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(2), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToUint128() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(15), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToUint128() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128((byte) 8), cursor.next().get());
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128((byte) 0), cursor.next().get());
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128((byte) -12), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToUint128() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(100), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToUint128() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(212L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToUint128() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128((short) 52), cursor.next().get());
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128((short) 0), cursor.next().get());
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128((short) -154), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToUint128() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(480), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToUint128() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(301), cursor.next().get());
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0), cursor.next().get());
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(-89), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToUint128() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(20L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToUint128() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(891L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0L), cursor.next().get());
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(-1024L), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToUint128() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(1049L), cursor.next().get());
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0L), cursor.next().get());
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint64Uint128(p64), cursor.next().getBigInteger());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToUint128() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(beef), cursor.next().get());
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(biZero), cursor.next().get());
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(p128), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToUint128() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(123453.125f), cursor.next().get());
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0f), cursor.next().get());
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(-2523485349058.0f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToUint128() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(5839.25f), cursor.next().get());
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0f), cursor.next().get());
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(-4.25f), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToUint128() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(4098d), cursor.next().get());
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0d), cursor.next().get());
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(-10948.015625d), cursor.next().get());
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(1.0000152587890625e20), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToUint128() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.uint128").input(img).apply();
		var cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(9087d), cursor.next().get());
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(0d), cursor.next().get());
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.uint128").input(img).apply();
		cursor = ((IterableInterval<Unsigned128BitType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.uint128(-234.25d), cursor.next().get());
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToFloat32() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(1), cursor.next().get(), 0);
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToFloat32() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(2), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToFloat32() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(15), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToFloat32() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((byte) 8), cursor.next().get(), 0);
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((byte) 0), cursor.next().get(), 0);
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((byte) -12), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToFloat32() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(100), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToFloat32() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(212L), cursor.next().get(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0L), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToFloat32() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((short) 52), cursor.next().get(), 0);
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((short) 0), cursor.next().get(), 0);
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((short) -154), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToFloat32() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(480), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToFloat32() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(301), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().get(), 0);
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-89), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToFloat32() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(20L), cursor.next().get(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0L), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToFloat32() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(891L), cursor.next().get(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0L), cursor.next().get(), 0);
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-1024L), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToFloat32() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(1049L), cursor.next().get(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0L), cursor.next().get(), 0);
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(p64), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToFloat32() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(beef), cursor.next().get(), 0);
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(biZero), cursor.next().get(), 0);
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(p128), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToFloat32() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(123453.125f), cursor.next().get(), 0);
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0f), cursor.next().get(), 0);
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-2523485349058.0f), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToFloat32() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(5839.25f), cursor.next().get(), 0);
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0f), cursor.next().get(), 0);
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-4.25f), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToFloat32() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(4098d), cursor.next().get(), 0);
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0d), cursor.next().get(), 0);
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-10948.015625d), cursor.next().get(), 0);
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(1.0000152587890625e20), cursor.next().get(),
				0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToFloat32() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float32").input(img).apply();
		var cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(9087d), cursor.next().get(), 0);
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0d), cursor.next().get(), 0);
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float32").input(img).apply();
		cursor = ((IterableInterval<FloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-234.25d), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToCfloat32() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(1), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToCfloat32() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(2), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToCfloat32() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(15), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToCfloat32() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((byte) 8), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32((byte) 0), cursor.next().getImaginaryFloat(),
				0);
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((byte) 0), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32((byte) 0), cursor.next().getImaginaryFloat(),
				0);
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((byte) -12), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32((byte) 0), cursor.next().getImaginaryFloat(),
				0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToCfloat32() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(100), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToCfloat32() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(212L), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0L), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToCfloat32() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((short) 52), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32((short) 0), cursor.next().getImaginaryFloat(),
				0);
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((short) 0), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32((short) 0), cursor.next().getImaginaryFloat(),
				0);
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32((short) -154), cursor.next().getRealFloat(),
				0);
			assertEquals(Types.float32((short) 0), cursor.next().getImaginaryFloat(),
				0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToCfloat32() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(480), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToCfloat32() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(301), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-89), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToCfloat32() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(20L), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0L), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToCfloat32() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(891L), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0L), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-1024L), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToCfloat32() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(1049L), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0L), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(p64), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0L), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToCfloat32() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(beef), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(biZero), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(biZero), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(biZero), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(p128), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(biZero), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToCfloat32() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(123453.125f), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0f), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0f), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0f), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-2523485349058.0f), cursor.next()
				.getRealFloat(), 0);
			assertEquals(Types.float32(0f), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToCfloat32() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(5839.25f), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(120f), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0f), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0f), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-4.25f), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(-123.0625f), cursor.next().getImaginaryFloat(),
				0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToCfloat32() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(4098d), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0d), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0d), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0d), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-10948.015625d), cursor.next().getRealFloat(),
				0);
			assertEquals(Types.float32(0d), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(1.0000152587890625e20), cursor.next()
				.getRealFloat(), 0);
			assertEquals(Types.float32(0), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToCfloat32() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat32").input(img).apply();
		var cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(9087d), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(879542.125d), cursor.next()
				.getImaginaryFloat(), 0);
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(0d), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(0d), cursor.next().getImaginaryFloat(), 0);
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat32").input(img).apply();
		cursor = ((IterableInterval<ComplexFloatType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float32(-234.25d), cursor.next().getRealFloat(), 0);
			assertEquals(Types.float32(-9.0d), cursor.next().getImaginaryFloat(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToFloat64() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(1), cursor.next().get(), 0);
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToFloat64() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(2), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToFloat64() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(15), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToFloat64() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((byte) 8), cursor.next().get(), 0);
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((byte) 0), cursor.next().get(), 0);
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((byte) -12), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToFloat64() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(100), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToFloat64() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(212L), cursor.next().get(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0L), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToFloat64() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((short) 52), cursor.next().get(), 0);
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((short) 0), cursor.next().get(), 0);
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((short) -154), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToFloat64() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(480), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToFloat64() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(301), cursor.next().get(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().get(), 0);
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-89), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToFloat64() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(20L), cursor.next().get(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0L), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToFloat64() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(891L), cursor.next().get(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0L), cursor.next().get(), 0);
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-1024L), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToFloat64() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(1049L), cursor.next().get(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0L), cursor.next().get(), 0);
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(p64), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToFloat64() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(beef), cursor.next().get(), 0);
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(biZero), cursor.next().get(), 0);
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(p128), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToFloat64() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(123453.125f), cursor.next().get(), 0);
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0f), cursor.next().get(), 0);
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-2523485349058.0f), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToFloat64() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(5839.25f), cursor.next().get(), 0);
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0f), cursor.next().get(), 0);
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-4.25f), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToFloat64() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(4098d), cursor.next().get(), 0);
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0d), cursor.next().get(), 0);
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-10948.015625d), cursor.next().get(), 0);
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(1.0000152587890625e20), cursor.next().get(),
				0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToFloat64() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.float64").input(img).apply();
		var cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(9087d), cursor.next().get(), 0);
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0d), cursor.next().get(), 0);
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.float64").input(img).apply();
		cursor = ((IterableInterval<DoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-234.25d), cursor.next().get(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testBitToCfloat64() {

		final BitType b = new BitType(true);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(1), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(false);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint2ToCfloat64() {

		final Unsigned2BitType b = new Unsigned2BitType(2);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(2), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint4ToCfloat64() {

		final Unsigned4BitType b = new Unsigned4BitType(15);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(15), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt8ToCfloat64() {

		final ByteType b = new ByteType((byte) 8);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((byte) 8), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64((byte) 0), cursor.next().getImaginaryDouble(),
				0);
		}

		b.set((byte) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((byte) 0), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64((byte) 0), cursor.next().getImaginaryDouble(),
				0);
		}

		b.set((byte) -12);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((byte) -12), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64((byte) 0), cursor.next().getImaginaryDouble(),
				0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint8ToCfloat64() {

		final UnsignedByteType b = new UnsignedByteType(100);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(100), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint12ToCfloat64() {

		final Unsigned12BitType b = new Unsigned12BitType(212L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(212L), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0L), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt16ToCfloat64() {

		final ShortType b = new ShortType((short) 52);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((short) 52), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64((short) 0), cursor.next().getImaginaryDouble(),
				0);
		}

		b.set((short) 0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((short) 0), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64((short) 0), cursor.next().getImaginaryDouble(),
				0);
		}

		b.set((short) -154);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64((short) -154), cursor.next().getRealDouble(),
				0);
			assertEquals(Types.float64((short) 0), cursor.next().getImaginaryDouble(),
				0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint16ToCfloat64() {

		final UnsignedShortType b = new UnsignedShortType(480);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(480), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt32ToCfloat64() {

		final IntType b = new IntType(301);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(301), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(-89);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-89), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint32ToCfloat64() {

		final UnsignedIntType b = new UnsignedIntType(20L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(20L), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0L), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testInt64ToCfloat64() {

		final LongType b = new LongType(891L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(891L), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0L), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(-1024L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-1024L), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint64ToCfloat64() {

		final UnsignedLongType b = new UnsignedLongType(1049L);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(1049L), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0L);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0L), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(p64);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(p64), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0L), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testUint128ToCfloat64() {

		final Unsigned128BitType b = new Unsigned128BitType(beef);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(beef), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(biZero), cursor.next().getImaginaryDouble(),
				0);
		}

		b.set(biZero);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(biZero), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(biZero), cursor.next().getImaginaryDouble(),
				0);
		}

		b.set(p128);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(p128), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(biZero), cursor.next().getImaginaryDouble(),
				0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat32ToCfloat64() {

		final FloatType b = new FloatType(123453.125f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(123453.125f), cursor.next().getRealDouble(),
				0);
			assertEquals(Types.float64(0f), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0f), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0f), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(-2523485349058.0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-2523485349058.0f), cursor.next()
				.getRealDouble(), 0);
			assertEquals(Types.float64(0f), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat32ToCfloat64() {

		final ComplexFloatType b = new ComplexFloatType(5839.25f, 120f);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(5839.25f), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(120f), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0f, 0f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0f), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0f), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(-4.25f, -123.0625f);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-4.25f), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(-123.0625f), cursor.next()
				.getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testFloat64ToCfloat64() {

		final DoubleType b = new DoubleType(4098d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(4098d), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0d), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0d), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0d), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(-10948.015625d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-10948.015625d), cursor.next().getRealDouble(),
				0);
			assertEquals(Types.float64(0d), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(1.0000152587890625e20);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(1.0000152587890625e20), cursor.next()
				.getRealDouble(), 0);
			assertEquals(Types.float64(0), cursor.next().getImaginaryDouble(), 0);
		}

	}

	/** Tests {@link ConvertTypes#integerToUint128}. */
	@Test
	public void testCfloat64ToCfloat64() {

		final ComplexDoubleType b = new ComplexDoubleType(9087d, 879542.125d);
		// Create the input image
		final var img = ops.binary("create.img") //
			.input(new FinalDimensions(2, 2), b) //
			.apply();
		ops.unary("image.fill").input(b).output(img).compute();
		// Create the converted image
		var converted = ops.unary("convert.cfloat64").input(img).apply();
		var cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(9087d), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(879542.125d), cursor.next()
				.getImaginaryDouble(), 0);
		}

		b.set(0d, 0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(0d), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(0d), cursor.next().getImaginaryDouble(), 0);
		}

		b.set(-234.25d, -9.0d);
		ops.unary("image.fill").input(b).output(img).compute();
		converted = ops.unary("convert.cfloat64").input(img).apply();
		cursor = ((IterableInterval<ComplexDoubleType>) converted).cursor();
		while (cursor.hasNext()) {
			assertEquals(Types.float64(-234.25d), cursor.next().getRealDouble(), 0);
			assertEquals(Types.float64(-9.0d), cursor.next().getImaginaryDouble(), 0);
		}

	}

}
