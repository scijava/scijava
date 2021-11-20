
package net.imagej.ops2.types.maxValue;

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

import org.scijava.ops.OpCollection;
import org.scijava.ops.OpField;
import org.scijava.plugin.Plugin;

/**
 * This collection of Ops can be used to obtain the maximum value of any
 * {@link RealType}. This method of determining the maximum value of a
 * {@link RealType} is preferable since it is safe and extensible.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class MaxValueRealTypes {

	final BitType maxBit = new BitType(true);

	@OpField(names = "types.maxValue")
	public final Function<BitType, BitType> maxBitType = in -> {
		return maxBit;
	};

	final BoolType maxBool = new BoolType(true);

	@OpField(names = "types.maxValue")
	public final Function<BoolType, BoolType> maxBoolType = in -> {
		return maxBool;
	};

	final NativeBoolType maxNativeBool = new NativeBoolType(true);

	@OpField(names = "types.maxValue")
	public final Function<NativeBoolType, NativeBoolType> maxNativeBoolType =
		in -> {
			return maxNativeBool;
		};

	final ByteType maxByte = new ByteType(Byte.MAX_VALUE);

	@OpField(names = "types.maxValue")
	public final Function<ByteType, ByteType> maxByteType = in -> {
		return maxByte;
	};

	final UnsignedByteType maxUnsignedByte = new UnsignedByteType(
		-Byte.MIN_VALUE + Byte.MAX_VALUE);

	@OpField(names = "types.maxValue")
	public final Function<UnsignedByteType, UnsignedByteType> maxUnsignedByteType =
		in -> {
			return maxUnsignedByte;
		};

	final IntType maxInt = new IntType(Integer.MAX_VALUE);

	@OpField(names = "types.maxValue")
	public final Function<IntType, IntType> maxIntType = in -> {
		return maxInt;
	};

	final UnsignedIntType maxUnsignedInt = new UnsignedIntType(0xffffffffL);

	@OpField(names = "types.maxValue")
	public final Function<UnsignedIntType, UnsignedIntType> maxUnsignedIntType =
		in -> {
			return maxUnsignedInt;
		};

	final LongType maxLong = new LongType(Long.MAX_VALUE);

	@OpField(names = "types.maxValue")
	public final Function<LongType, LongType> maxLongType = in -> {
		return maxLong;
	};

	final UnsignedLongType maxUnsignedLong = new UnsignedLongType(
		new UnsignedLongType().getMaxBigIntegerValue());

	@OpField(names = "types.maxValue")
	public final Function<UnsignedLongType, UnsignedLongType> maxUnsignedLongType =
		in -> {
			return maxUnsignedLong;
		};

	final ShortType maxShort = new ShortType(Short.MAX_VALUE);

	@OpField(names = "types.maxValue")
	public final Function<ShortType, ShortType> maxShortType = in -> {
		return maxShort;
	};

	final UnsignedShortType maxUnsignedShort = new UnsignedShortType(
		-Short.MIN_VALUE + Short.MAX_VALUE);

	@OpField(names = "types.maxValue")
	public final Function<UnsignedShortType, UnsignedShortType> maxUnsignedShortType =
		in -> {
			return maxUnsignedShort;
		};

	final FloatType maxFloat = new FloatType(Float.MAX_VALUE);

	@OpField(names = "types.maxValue")
	public final Function<FloatType, FloatType> maxFloatType = in -> {
		return maxFloat;
	};

	final DoubleType maxDouble = new DoubleType(Double.MAX_VALUE);

	@OpField(names = "types.maxValue")
	public final Function<DoubleType, DoubleType> maxDoubleType = in -> {
		return maxDouble;
	};

	final Unsigned2BitType max2Bit = new Unsigned2BitType(3);

	@OpField(names = "types.maxValue")
	public final Function<Unsigned2BitType, Unsigned2BitType> max2BitType =
		in -> {
			return max2Bit;
		};

	final Unsigned4BitType max4Bit = new Unsigned4BitType(15);

	@OpField(names = "types.maxValue")
	public final Function<Unsigned4BitType, Unsigned4BitType> max4BitType =
		in -> {
			return max4Bit;
		};

	final Unsigned12BitType max12Bit = new Unsigned12BitType(4095);

	@OpField(names = "types.maxValue")
	public final Function<Unsigned12BitType, Unsigned12BitType> max12BitType =
		in -> {
			return max12Bit;
		};

	final Unsigned128BitType max128Bit = new Unsigned128BitType(
		new Unsigned128BitType().getMaxBigIntegerValue());

	@OpField(names = "types.maxValue")
	public final Function<Unsigned128BitType, Unsigned128BitType> max128BitType =
		in -> {
			return max128Bit;
		};

	// TODO: UnboundedIntegerType

	/**
	 * Due to the variable length of this type, we cannot simply return some final
	 * value. The best we can do is quickly compute the answer. Note that so long
	 * as the bit length of the type is less than 64, we can losslessly compute
	 * the maximum within long math. If it is 64 or larger, we must use BigInteger
	 * (this should never happen in practice since {@link UnsignedLongType} is
	 * more efficient as a 64 bit type and bit lengths greater than 64 are
	 * unsupported). TODO: Is there some way we could cache the values? Is that
	 * worth it??
	 */
	@OpField(names = "types.maxValue")
	public final Function<UnsignedVariableBitLengthType, UnsignedVariableBitLengthType> maxVarLengthType =
		in -> {
			int nBits = in.getBitsPerPixel();
			if (nBits < 64) {
				long maxVal = (1l << nBits) - 1;
				return new UnsignedVariableBitLengthType(maxVal, nBits);
			}
			BigInteger maxVal = BigInteger.TWO.pow(nBits - 1).subtract(
				BigInteger.ONE);
			UnsignedVariableBitLengthType typeMax = new UnsignedVariableBitLengthType(
				nBits);
			typeMax.setBigInteger(maxVal);
			return typeMax;
		};

}
