
package net.imagej.ops2.tutorial;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.scijava.function.Computers;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.DefaultOpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

import net.imglib2.Interval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.IntervalChunks;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.parallel.Parallelization;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

/**
 * SciJava Ops includes a mechanism for automatically introducing concurrency to
 * Ops.
 *
 * Developers can utilize this mechanism by writing their Ops on the smallest
 * element of the computation, be that a single pixel, or a {@link Neighborhood}.
 * SciJava Ops will then "lift" these Ops, creating parallelized Ops that run
 * on an entire {@link net.imglib2.RandomAccessibleInterval}
 *
 * This tutorial showcases these lifting mechanisms.
 *
 * @author Gabriel Selzer
 */
public class OpParallelization implements OpCollection {

	/**
	 * This Op, which is really just a computation on a single pixel, lets the
	 * framework assume the burden of parallelization
	 * 
	 * @param input the input pixel
	 * @param output the preallocated output pixel
	 */
	@OpMethod(names = "tutorial.invertPerPixel", type = Computers.Arity1.class)
	public static void invertOp(UnsignedByteType input, UnsignedByteType output) {
		output.set(255 - input.get());
	}

	/**
	 * This Op, which computes some algorithm over a neighborhood, also lets the
	 * framework assume the burden of parallelization
	 *
	 * @param input the input pixel
	 * @param output the preallocated output pixel
	 */
	@OpMethod(names = "tutorial.neighborhoodAverage",
		type = Computers.Arity1.class)
	public static void averageNeighborhood(Neighborhood<UnsignedByteType> input,
		UnsignedByteType output)
	{
		long tmp = 0;
		var cursor = input.cursor();
		while (cursor.hasNext()) {
			tmp += cursor.next().getIntegerLong();
		}
		output.setInteger(tmp / input.size());
	}

	public static void main(String... args) {
		OpEnvironment ops = new DefaultOpEnvironment();

		// First, we show parallelization at work for our per-pixel Op.
		// SciJava Ops understands how to apply that Op to each pixel of the input
		// image

		// Fill an input image with a value
		var fillValue = new UnsignedByteType(5);
		var inImg = ArrayImgs.unsignedBytes(10, 10);
		ops.op("image.fill").arity1().input(fillValue).output(inImg).compute();
		// Run the Op
		var outImg = ArrayImgs.unsignedBytes(10, 10);
		ops.op("tutorial.invertPerPixel").arity1().input(inImg).output(outImg)
			.compute();
		// Get the original value, and the inverted value
		var original = inImg.firstElement().get();
		var inverted = outImg.firstElement().get();
		System.out.println("Original image was filled with value " + original +
			", and the inverted image is filled with value (255 - " + original +
			") = " + inverted);

		// Now, we show parallelization at work for our Parallelization Op.
		// For this example, we use a radius-1 rectangle; in other words, the
		// neighborhood
		// for a given pixel includes all of its immediate neighbors (including
		// diagonal)
		var shape = new RectangleShape(1, false);
		ops.op("tutorial.neighborhoodAverage").arity2().input(inImg, shape)
				.output(outImg).compute();
		// Get the original value, and the radius-1 neighborhood value
		original = inImg.firstElement().get();
		var mean = outImg.firstElement().get();
		System.out.println("Original image was filled with value " + original +
				", and the radius-1 mean at the corner is (4 * " + original + " / 9) = " +
				mean);
	}

}
