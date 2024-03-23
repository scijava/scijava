
package org.scijava.ops.benchmarks.matching;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.basictypeaccess.ByteAccess;
import net.imglib2.img.basictypeaccess.DoubleAccess;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Class containing Ops used in benchmarking
 *
 * @author Gabriel Selzer
 */
public class MatchingOpCollection {

	/**
	 * @param in the data to input to our function
	 * @param d the value to add to each element in the input
	 * @param out the preallocated storage buffer
	 * @implNote op name="benchmark.match",type=Computer
	 */
	public static void op( //
		final RandomAccessibleInterval<DoubleType> in, //
		final Double d, //
		final RandomAccessibleInterval<DoubleType> out //
	) {
		LoopBuilder.setImages(in, out).multiThreaded().forEachPixel((i, o) -> o.set(
			i.get() + d));
	}

	/**
	 * @param in the {@link RandomAccessibleInterval} containing {@link ByteType}s
	 * @return a {@link RandomAccessibleInterval} wrapping {@code in}.
	 * @implNote op name="engine.convert", type=Function, priority='1000.'
	 */
	public static RandomAccessibleInterval<DoubleType> toDoubleType(
		RandomAccessibleInterval<ByteType> in)
	{
		return Converters.convert(in, sampler -> new DoubleType(new DoubleAccess() {

			@Override
			public double getValue(int index) {
				return sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, double value) {
				sampler.get().setReal(value);
			}
		}));
	}

	/**
	 * @param in the {@link RandomAccessibleInterval} containing
	 *          {@link DoubleType}s
	 * @return a {@link RandomAccessibleInterval} wrapping {@code in}.
	 * @implNote op name="engine.convert", type=Function, priority='1000.'
	 */
	public static RandomAccessibleInterval<ByteType> toByteType(
		RandomAccessibleInterval<DoubleType> in)
	{
		return Converters.convert(in, sampler -> new ByteType(new ByteAccess() {

			@Override
			public byte getValue(int index) {
				return (byte) sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, byte value) {
				sampler.get().setReal(value);
			}
		}));
	}

}
