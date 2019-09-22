package net.imagej.ops.convert;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.type.Type;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.function.Function3;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class Converters<I, O extends Type<O>> {

	@OpField(names = "convert")
	@Parameter(key = "inputII")
	@Parameter(key = "converter")
	@Parameter(key = "outputType")
	@Parameter(key = "outputII", itemIO = ItemIO.OUTPUT)
	public final Function3<RandomAccessible<I>, Converter<? super I, ? super O>, O, RandomAccessible<O>> generalConverterRA = (
			inputRA, converter, type) -> net.imglib2.converter.Converters.convert(inputRA, converter, type);

	@OpField(names = "convert")
	@Parameter(key = "inputII")
	@Parameter(key = "converter")
	@Parameter(key = "outputType")
	@Parameter(key = "outputII", itemIO = ItemIO.OUTPUT)
	public final Function3<IterableInterval<I>, Converter<? super I, ? super O>, O, IterableInterval<O>> generalConverterII = (
			inputII, converter, type) -> net.imglib2.converter.Converters.convert(inputII, converter, type);

}
