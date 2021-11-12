package net.imagej.ops2.create;

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

import org.scijava.function.Producer;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class CreateTypes {

	/**
	 * @output output
	 * @implNote op names='create, create.bit'
	 */
	public final Producer<BitType> bitTypeSource = () -> new BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint2'
	 */
	public final Producer<Unsigned2BitType> uint2TypeSource = () -> new Unsigned2BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint4'
	 */
	public final Producer<Unsigned4BitType> uint4TypeSource = () -> new Unsigned4BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.byte'
	 */
	public final Producer<ByteType> byteTypeSource = () -> new ByteType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint8'
	 */
	public final Producer<UnsignedByteType> uint8TypeSource = () -> new UnsignedByteType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint12'
	 */
	public final Producer<Unsigned12BitType> uint12TypeSource = () -> new Unsigned12BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.int16'
	 */
	public final Producer<ShortType> shortTypeSource = () -> new ShortType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint16'
	 */
	public final Producer<UnsignedShortType> uint16TypeSource = () -> new UnsignedShortType();

	/**
	 * @output output
	 * @implNote op names='create, create.int32'
	 */
	public final Producer<IntType> int32TypeSource = () -> new IntType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint32'
	 */
	public final Producer<UnsignedIntType> uint32TypeSource = () -> new UnsignedIntType();

	/**
	 * @output output
	 * @implNote op names='create, create.int64'
	 */
	public final Producer<LongType> int64TypeSource = () -> new LongType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint64'
	 */
	public final Producer<UnsignedLongType> uint64TypeSource = () -> new UnsignedLongType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint128'
	 */
	public final Producer<Unsigned128BitType> uint128TypeSource = () -> new Unsigned128BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.float32'
	 */
	public final Producer<FloatType> float32TypeSource = () -> new FloatType();

	/**
	 * @output output
	 * @implNote op names='create, create.cfloat32'
	 */
	public final Producer<ComplexFloatType> cfloat32TypeSource = () -> new ComplexFloatType();

	/**
	 * @output output
	 * @implNote op names='create, create.float64'
	 */
	public final Producer<DoubleType> float64TypeSource = () -> new DoubleType();

	/**
	 * @output output
	 * @implNote op names='create, create.cfloat64'
	 */
	public final Producer<ComplexDoubleType> cfloat64TypeSource = () -> new ComplexDoubleType();

}
