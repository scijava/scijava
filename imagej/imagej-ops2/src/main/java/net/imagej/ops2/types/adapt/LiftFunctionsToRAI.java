
package net.imagej.ops2.types.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.Type;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.function.Functions;
import org.scijava.ops.OpCollection;
import org.scijava.ops.OpField;
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
public class LiftFunctionsToRAI<I1, I2, I3, I4, I5, O extends Type<O>> {

	@OpField(names = "adapt", priority = Priority.HIGH)
	public final Function<Function<I1, O>, Function<RandomAccessibleInterval<I1>, RandomAccessibleInterval<O>>> lift1 =
		(function) -> {
			return (raiInput) -> {
				O outType = function.apply(Util.getTypeFromInterval(raiInput));
				Img<O> outImg = Util.getSuitableImgFactory(raiInput, outType).create(
					raiInput);
				LoopBuilder.setImages(raiInput, outImg).multiThreaded().forEachPixel((
					in, out) -> out.set(function.apply(in)));
				return outImg;
			};
		};

	@OpField(names = "adapt")
	public final Function<BiFunction<I1, I2, O>, BiFunction<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>>> lift2 =
		(function) -> {
			return (raiInput1, raiInput2) -> {
				O outType = function.apply(Util.getTypeFromInterval(raiInput1), Util
					.getTypeFromInterval(raiInput2));
				Img<O> outImg = Util.getSuitableImgFactory(raiInput1, outType).create(
					raiInput1);
				LoopBuilder.setImages(raiInput1, raiInput2, outImg).multiThreaded()
					.forEachPixel((in1, in2, out) -> out.set(function.apply(in1, in2)));
				return outImg;
			};
		};

	@OpField(names = "adapt")
	public final Function<Functions.Arity3<I1, I2, I3, O>, Functions.Arity3<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<I3>, RandomAccessibleInterval<O>>> lift3 =
		(function) -> {
			return (raiInput1, raiInput2, raiInput3) -> {
				O outType = function.apply(Util.getTypeFromInterval(raiInput1), Util
					.getTypeFromInterval(raiInput2), Util.getTypeFromInterval(raiInput3));
				Img<O> outImg = Util.getSuitableImgFactory(raiInput1, outType).create(
					raiInput1);
				LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, outImg)
					.multiThreaded().forEachPixel((in1, in2, in3, out) -> out.set(function
						.apply(in1, in2, in3)));
				return outImg;
			};
		};

		@OpField(names = "adapt")
		public final Function<Functions.Arity4<I1, I2, I3, I4, O>, Functions.Arity4<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<I3>, RandomAccessibleInterval<I4>, RandomAccessibleInterval<O>>> lift4 =
			(function) -> {
				return (raiInput1, raiInput2, raiInput3, raiInput4) -> {
					O outType = function.apply(Util.getTypeFromInterval(raiInput1), Util
						.getTypeFromInterval(raiInput2), Util.getTypeFromInterval(
							raiInput3), Util.getTypeFromInterval(raiInput4));
					Img<O> outImg = Util.getSuitableImgFactory(raiInput1, outType).create(
						raiInput1);
					LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiInput4,
						outImg).multiThreaded().forEachPixel((in1, in2, in3, in4,
							out) -> out.set(function.apply(in1, in2, in3, in4)));
					return outImg;
				};
			};

		@OpField(names = "adapt")
		public final Function<Functions.Arity5<I1, I2, I3, I4, I5, O>, Functions.Arity5<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<I3>, RandomAccessibleInterval<I4>, RandomAccessibleInterval<I5>, RandomAccessibleInterval<O>>> lift5 =
		(function) -> {
			return (raiInput1, raiInput2, raiInput3, raiInput4, raiInput5) -> {
				O outType = function.apply(Util.getTypeFromInterval(raiInput1), Util
					.getTypeFromInterval(raiInput2), Util.getTypeFromInterval(raiInput3),
					Util.getTypeFromInterval(raiInput4), Util.getTypeFromInterval(
						raiInput5));
				Img<O> outImg = Util.getSuitableImgFactory(raiInput1, outType).create(
					raiInput1);
				LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiInput4,
					raiInput5, outImg).multiThreaded().forEachPixel((in1, in2, in3, in4,
						in5, out) -> out.set(function.apply(in1, in2, in3, in4, in5)));
				return outImg;
			};
		};

}
