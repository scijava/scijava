
package net.imagej.ops2.types.minValue;

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

import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

/**
 * This collection of Ops can be used to obtain the minimum value of any
 * {@link RealType}. This method of determining the minimum value of a
 * {@link RealType} is preferable since it is safe and extensible.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class MinValueRealTypes {

	final BitType minBit = new BitType(false);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<BitType, BitType> minBitType = in -> {
		return minBit;
	};

	final BoolType minBool = new BoolType(false);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<BoolType, BoolType> minBoolType = in -> {
		return minBool;
	};

	final NativeBoolType minNativeBool = new NativeBoolType(false);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<NativeBoolType, NativeBoolType> minNativeBoolType =
		in -> {
			return minNativeBool;
		};

	final ByteType minByte = new ByteType(Byte.MIN_VALUE);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<ByteType, ByteType> minByteType = in -> {
		return minByte;
	};

	final UnsignedByteType minUnsignedByte = new UnsignedByteType(0);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedByteType, UnsignedByteType> minUnsignedByteType =
		in -> {
			return minUnsignedByte;
		};

	final IntType minInt = new IntType(Integer.MIN_VALUE);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<IntType, IntType> minIntType = in -> {
		return minInt;
	};

	final UnsignedIntType minUnsignedInt = new UnsignedIntType(0);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedIntType, UnsignedIntType> minUnsignedIntType =
		in -> {
			return minUnsignedInt;
		};

	final LongType minLong = new LongType(Long.MIN_VALUE);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<LongType, LongType> minLongType = in -> {
		return minLong;
	};

	final UnsignedLongType minUnsignedLong = new UnsignedLongType(0);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedLongType, UnsignedLongType> minUnsignedLongType =
		in -> {
			return minUnsignedLong;
		};

	final ShortType minShort = new ShortType(Short.MIN_VALUE);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<ShortType, ShortType> minShortType = in -> {
		return minShort;
	};

	final UnsignedShortType minUnsignedShort = new UnsignedShortType(0);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedShortType, UnsignedShortType> minUnsignedShortType =
		in -> {
			return minUnsignedShort;
		};

	final FloatType minFloat = new FloatType(Float.MIN_VALUE);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<FloatType, FloatType> minFloatType = in -> {
		return minFloat;
	};

	final DoubleType minDouble = new DoubleType(Double.MIN_VALUE);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<DoubleType, DoubleType> minDoubleType = in -> {
		return minDouble;
	};

	final Unsigned2BitType min2Bit = new Unsigned2BitType(0);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<Unsigned2BitType, Unsigned2BitType> min2BitType =
		in -> {
			return min2Bit;
		};

	final Unsigned4BitType min4Bit = new Unsigned4BitType(0);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<Unsigned4BitType, Unsigned4BitType> min4BitType =
		in -> {
			return min4Bit;
		};

	final Unsigned12BitType min12Bit = new Unsigned12BitType(0);

	/**
	 * @implNote op names='types.minValue'
	 */
	public final Function<Unsigned12BitType, Unsigned12BitType> min12BitType =
		in -> {
			return min12Bit;
		};

	final Unsigned128BitType min128Bit = new Unsigned128BitType(BigInteger.ZERO);

	/**
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
	 * @implNote op names='types.minValue'
	 */
	public final Function<UnsignedVariableBitLengthType, UnsignedVariableBitLengthType> minVarLengthType =
		in -> {
			int nBits = in.getBitsPerPixel();
			return new UnsignedVariableBitLengthType(0l, nBits);
		};

}
