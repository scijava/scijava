
package net.imagej.ops.types.adapt;

import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Lifts {@link Computers} operating on some types {@code I1, I2, ..., In},
 * {@code O extends Type<O>} to a Computer operating on
 * {@link RandomAccessibleInterval}s of those types.
 * The {@Computer}{@code <I, O>} is then applied iteratively
 * over each pixel of the input image(s). NOTE: It is assumed that the input
 * {@code RAI}s are the same size. If they are not, the lifted {@code Computer}
 * will only iteratively process the images until one image runs out of pixels
 * to iterate over.
 * 
 * @author Gabriel Selzer
 * @param <I1> - the {@code Type} of the first type parameter
 * @param <I2> - the {@code Type} of the second type parameter
 * @param <O> - the {@code Type} of the output
 */
@Plugin(type = OpCollection.class)
public class LiftComputersToRAI<I1, I2, O> {

	@OpField(names = "adapt")
	@Parameter(key = "from")
	@Parameter(key = "to")
	public final Function<Computers.Arity1<I1, O>, Computers.Arity1<RandomAccessibleInterval<I1>, RandomAccessibleInterval<O>>> lift1 =
		(computer) -> {
			return (raiInput, raiOutput) -> {
				Cursor<I1> inCursor = Views.flatIterable(raiInput).cursor();
				Cursor<O> outCursor = Views.flatIterable(raiOutput).cursor();
				while (inCursor.hasNext() && outCursor.hasNext()) {
					computer.compute(inCursor.next(), outCursor.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "from")
	@Parameter(key = "to")
	public final Function<Computers.Arity2<I1, I2, O>, Computers.Arity2<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>>> lift2 =
		(computer) -> {
			return (raiInput1, raiInput2, raiOutput) -> {
				Cursor<I1> inCursor1 = Views.flatIterable(raiInput1).cursor();
				Cursor<I2> inCursor2 = Views.flatIterable(raiInput2).cursor();
				Cursor<O> outCursor = Views.flatIterable(raiOutput).cursor();
				while (inCursor1.hasNext() && inCursor2.hasNext() && outCursor
					.hasNext())
		{
					computer.compute(inCursor1.next(), inCursor2.next(), outCursor
						.next());
				}
			};
		};

}
