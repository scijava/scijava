package net.imagej.ops2.convert;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.type.Type;

import org.scijava.function.Functions;
import org.scijava.ops.OpCollection;
import org.scijava.ops.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class Converters<I, O extends Type<O>> {

	@OpField(names = "convert", params = "inputII, converter, outputType, outputII")
	public final Functions.Arity3<RandomAccessible<I>, Converter<? super I, ? super O>, O, RandomAccessible<O>> generalConverterRA = (
			inputRA, converter, type) -> net.imglib2.converter.Converters.convert(inputRA, converter, type);

	@OpField(names = "convert", params = "inputII, converter, outputType, outputII")
	public final Functions.Arity3<IterableInterval<I>, Converter<? super I, ? super O>, O, IterableInterval<O>> generalConverterII = (
			inputII, converter, type) -> net.imglib2.converter.Converters.convert(inputII, converter, type);

}
