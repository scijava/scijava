package net.imagej.ops2.convert;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.type.Type;

import org.scijava.function.Functions;

public class Converters<I, O extends Type<O>> {

	/**
	 * @input inputRAI
	 * @input converter
	 * @input outputType
	 * @output outputII
	 * @implNote op names='convert'
	 */
	public final Functions.Arity3<RandomAccessible<I>, Converter<? super I, ? super O>, O, RandomAccessible<O>> generalConverterRA = (
			inputRA, converter, type) -> net.imglib2.converter.Converters.convert(inputRA, converter, type);

	/**
	 * @input inputII
	 * @input converter
	 * @input outputType
	 * @output outputII
	 * @implNote op names='convert'
	 */
	public final Functions.Arity3<IterableInterval<I>, Converter<? super I, ? super O>, O, IterableInterval<O>> generalConverterII = (
			inputII, converter, type) -> net.imglib2.converter.Converters.convert(inputII, converter, type);

}
