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

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.functions.Producer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class CreateTypes {
	
	@OpField(names = "create, create.bit", params = "output")
	public final Producer<BitType> bitTypeSource = () -> new BitType();

	@OpField(names = "create, create.uint2", params = "output")
	public final Producer<Unsigned2BitType> uint2TypeSource = () -> new Unsigned2BitType();

	@OpField(names = "create, create.uint4", params = "output")
	public final Producer<Unsigned4BitType> uint4TypeSource = () -> new Unsigned4BitType();

	@OpField(names = "create, create.byte", params = "output")
	public final Producer<ByteType> byteTypeSource = () -> new ByteType();

	@OpField(names = "create, create.uint8", params = "output")
	public final Producer<UnsignedByteType> uint8TypeSource = () -> new UnsignedByteType();

	@OpField(names = "create, create.uint12", params = "output")
	public final Producer<Unsigned12BitType> uint12TypeSource = () -> new Unsigned12BitType();

	@OpField(names = "create, create.int16", params = "output")
	public final Producer<ShortType> shortTypeSource = () -> new ShortType();

	@OpField(names = "create, create.uint16", params = "output")
	public final Producer<UnsignedShortType> uint16TypeSource = () -> new UnsignedShortType();

	@OpField(names = "create, create.int32", params = "output")
	public final Producer<IntType> int32TypeSource = () -> new IntType();

	@OpField(names = "create, create.uint32", params = "output")
	public final Producer<UnsignedIntType> uint32TypeSource = () -> new UnsignedIntType();

	@OpField(names = "create, create.int64", params = "output")
	public final Producer<LongType> int64TypeSource = () -> new LongType();

	@OpField(names = "create, create.uint64", params = "output")
	public final Producer<UnsignedLongType> uint64TypeSource = () -> new UnsignedLongType();

	@OpField(names = "create, create.uint128", params = "output")
	public final Producer<Unsigned128BitType> uint128TypeSource = () -> new Unsigned128BitType();

	@OpField(names = "create, create.float32", params = "output")
	public final Producer<FloatType> float32TypeSource = () -> new FloatType();

	@OpField(names = "create, create.cfloat32", params = "output")
	public final Producer<ComplexFloatType> cfloat32TypeSource = () -> new ComplexFloatType();

	@OpField(names = "create, create.float64", params = "output")
	public final Producer<DoubleType> float64TypeSource = () -> new DoubleType();

	@OpField(names = "create, create.cfloat64", params = "output")
	public final Producer<ComplexDoubleType> cfloat64TypeSource = () -> new ComplexDoubleType();

}
