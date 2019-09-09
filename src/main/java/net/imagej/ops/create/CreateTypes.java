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
import org.scijava.ops.core.function.Source;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class CreateTypes {
	
	@OpField(names = "create, create.bit")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<BitType> bitTypeSource = () -> new BitType();

	@OpField(names = "create, create.uint2")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<Unsigned2BitType> uint2TypeSource = () -> new Unsigned2BitType();

	@OpField(names = "create, create.uint4")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<Unsigned4BitType> uint4TypeSource = () -> new Unsigned4BitType();

	@OpField(names = "create, create.byte")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<ByteType> byteTypeSource = () -> new ByteType();

	@OpField(names = "create, create.uint8")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<UnsignedByteType> uint8TypeSource = () -> new UnsignedByteType();

	@OpField(names = "create, create.uint12")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<Unsigned12BitType> uint12TypeSource = () -> new Unsigned12BitType();

	@OpField(names = "create, create.int16")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<ShortType> shortTypeSource = () -> new ShortType();

	@OpField(names = "create, create.uint16")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<UnsignedShortType> uint16TypeSource = () -> new UnsignedShortType();

	@OpField(names = "create, create.int32")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<IntType> int32TypeSource = () -> new IntType();

	@OpField(names = "create, create.uint32")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<UnsignedIntType> uint32TypeSource = () -> new UnsignedIntType();

	@OpField(names = "create, create.int64")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<LongType> int64TypeSource = () -> new LongType();

	@OpField(names = "create, create.uint64")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<UnsignedLongType> uint64TypeSource = () -> new UnsignedLongType();

	@OpField(names = "create, create.uint128")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<Unsigned128BitType> uint128TypeSource = () -> new Unsigned128BitType();

	@OpField(names = "create, create.float32")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<FloatType> float32TypeSource = () -> new FloatType();

	@OpField(names = "create, create.cfloat32")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<ComplexFloatType> cfloat32TypeSource = () -> new ComplexFloatType();

	@OpField(names = "create, create.float64")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<DoubleType> float64TypeSource = () -> new DoubleType();

	@OpField(names = "create, create.cfloat64")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Source<ComplexDoubleType> cfloat64TypeSource = () -> new ComplexDoubleType();

}
