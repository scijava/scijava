
package net.imagej.ops2.tutorial;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.DefaultOpEnvironment;
import org.scijava.types.Nil;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.Parallelization;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Using the {@link net.imglib2.parallel.Parallelization} class, we can perform
 * independent computations in parallel. This tutorial showcases running many
 * Ops in parallel using {@link net.imglib2.parallel.Parallelization}.
 *
 * @author Gabriel Selzer
 */
public class ParallelComputation {

	public static void main(String... args) {
		OpEnvironment ops = new DefaultOpEnvironment();
		// To compute tasks using Parallelization, we must first gather a list of
		// parameters.
		List<Double> fillValues = Arrays.asList(1.0, 2.0, 3.0, 4.0);
		Img<UnsignedByteType> data = ArrayImgs.unsignedBytes(10, 10, 10);
		Nil<Img<UnsignedByteType>> outNil = new Nil<>() {};

		// Note that this function will be run many times in parallel
		// - it's not terribly complex, but we could do much more
		Function<Double, Img<UnsignedByteType>> fillImage = fillValue -> {
			// create a new image of the same size as our data
			var output = ops.op("create.img").arity1().input(data).outType(outNil)
				.apply();
			// fill it with the fill value
			LoopBuilder.setImages(output).forEachPixel(pixel -> pixel.setReal(
				fillValue));
			// and return it
			return output;
		};

		// Parallelization.getTaskExecutor().forEachApply() takes a list of
		// parameters,
		// and a function to apply on each parameter in the list. The function will
		// then be applied in parallel on each parameter, and the return is a list,
		// with the ith output being the application of the function on the ith
		// parameter.
		List<Img<UnsignedByteType>> filledImages = //
			Parallelization.getTaskExecutor().forEachApply(fillValues, fillImage);

		for (int i = 0; i < fillValues.size(); i++) {
			if (filledImages.get(i) != null) {
				System.out.println("Image " + i + " was filled with value " +
					filledImages.get(i).firstElement().get());
			}
		}

	}

}
