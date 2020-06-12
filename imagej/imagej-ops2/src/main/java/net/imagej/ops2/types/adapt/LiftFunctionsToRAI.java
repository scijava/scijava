
package net.imagej.ops2.types.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.Type;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Lifts {@link Functions} operating on some types {@code I1, I2, ..., In},
 * {@code O extends Type<O>} to a Function operating on
 * {@link RandomAccessibleInterval}s of those types. An output
 * {@link RandomAccessibleInterval} is created based off of the dimensions of
 * the first input image and using the output type of the passed
 * {@link Function}. The {@Function}{@code <I, O>} is then applied iteratively
 * over each pixel of the input image(s). NOTE: It is assumed that the input
 * {@code RAI}s are the same size. If they are not, the lifted {@link Function}
 * will only iteratively process the images until one image runs out of pixels
 * to iterate over.
 * 
 * @author Gabriel Selzer
 * @param <I1> - the {@code Type} of the first type parameter of the
 *          {@link Function}
 * @param <I2> - the {@code Type} of the second type parameter of the
 *          {@link Function}
 * @param <O> - the {@code Type} of the output of the {@link Function}
 */
@Plugin(type = OpCollection.class)
public class LiftFunctionsToRAI<I1, I2, O extends Type<O>> {

	@OpField(names = "adapt")
	@Parameter(key = "from")
	@Parameter(key = "to")
	public final Function<Function<I1, O>, Function<RandomAccessibleInterval<I1>, RandomAccessibleInterval<O>>> lift1 =
		(function) -> {
			return (raiInput) -> {
				O outType = function.apply(Util.getTypeFromInterval(raiInput));
				Img<O> outImg = Util.getSuitableImgFactory(raiInput, outType).create(
					raiInput);
				Cursor<I1> inCursor = Views.flatIterable(raiInput).cursor();
				Cursor<O> outCursor = Views.flatIterable(outImg).cursor();
				while (inCursor.hasNext()) {
					outCursor.next().set(function.apply(inCursor.next()));
				}
				return outImg;
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "from")
	@Parameter(key = "to")
	public final Function<BiFunction<I1, I2, O>, BiFunction<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>>> lift2 =
		(function) -> {
			return (raiInput1, raiInput2) -> {
				O outType = function.apply(Util.getTypeFromInterval(raiInput1), Util
					.getTypeFromInterval(raiInput2));
				Img<O> outImg = Util.getSuitableImgFactory(raiInput1, outType).create(
					raiInput1);
				Cursor<I1> inCursor1 = Views.flatIterable(raiInput1).cursor();
				Cursor<I2> inCursor2 = Views.flatIterable(raiInput2).cursor();
				Cursor<O> outCursor = Views.flatIterable(outImg).cursor();
				while (inCursor1.hasNext() && inCursor2.hasNext()) {
					outCursor.next().set(function.apply(inCursor1.next(), inCursor2
						.next()));
				}
				return outImg;
			};
		};

}
