package net.imagej.ops.create;

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
import org.scijava.ops.function.Producer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class CreateTypes {
	
	@OpField(names = "create, create.bit", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<BitType> bitTypeSource = () -> new BitType();

	@OpField(names = "create, create.uint2", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<Unsigned2BitType> uint2TypeSource = () -> new Unsigned2BitType();

	@OpField(names = "create, create.uint4", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<Unsigned4BitType> uint4TypeSource = () -> new Unsigned4BitType();

	@OpField(names = "create, create.byte", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<ByteType> byteTypeSource = () -> new ByteType();

	@OpField(names = "create, create.uint8", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<UnsignedByteType> uint8TypeSource = () -> new UnsignedByteType();

	@OpField(names = "create, create.uint12", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<Unsigned12BitType> uint12TypeSource = () -> new Unsigned12BitType();

	@OpField(names = "create, create.int16", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<ShortType> shortTypeSource = () -> new ShortType();

	@OpField(names = "create, create.uint16", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<UnsignedShortType> uint16TypeSource = () -> new UnsignedShortType();

	@OpField(names = "create, create.int32", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<IntType> int32TypeSource = () -> new IntType();

	@OpField(names = "create, create.uint32", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<UnsignedIntType> uint32TypeSource = () -> new UnsignedIntType();

	@OpField(names = "create, create.int64", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<LongType> int64TypeSource = () -> new LongType();

	@OpField(names = "create, create.uint64", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<UnsignedLongType> uint64TypeSource = () -> new UnsignedLongType();

	@OpField(names = "create, create.uint128", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<Unsigned128BitType> uint128TypeSource = () -> new Unsigned128BitType();

	@OpField(names = "create, create.float32", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<FloatType> float32TypeSource = () -> new FloatType();

	@OpField(names = "create, create.cfloat32", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<ComplexFloatType> cfloat32TypeSource = () -> new ComplexFloatType();

	@OpField(names = "create, create.float64", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<DoubleType> float64TypeSource = () -> new DoubleType();

	@OpField(names = "create, create.cfloat64", params = "x")
	@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
	public final Producer<ComplexDoubleType> cfloat64TypeSource = () -> new ComplexDoubleType();

}
