
package net.imagej.ops2.tutorial;

import java.util.Arrays;
import java.util.function.Function;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpMethod;

/**
 * A simple showcase of an Op that uses an {@link OpDependency}. Using
 * {@link OpDependency}s to break up your code into simple, reusable blocks, Ops
 * become more modular and enable specialization for unseen types.
 * 
 * @author Gabriel Selzer
 */
public class OpDependencies implements OpCollection {

	/**
	 * An Op that computes the size of a {@link double[]}
	 */
	@OpMethod(names = "stats.size", type = Function.class)
	public static double size(final double[] inArray) {
		return inArray.length;
	}

	/**
	 * An Op that computes the sum of a {@link double[]}
	 */
	@OpMethod(names = "stats.sum", type = Function.class)
	public static double sum(final double[] inArray) {
		return Arrays.stream(inArray).sum();
	}

	/**
	 * An Op that computes the mean of a {@link double[]}
	 */
	@OpMethod(names = "stats.mean", type = Function.class)
	public static double mean( //
		@OpDependency(name = "stats.sum") Function<double[], Double> sumOp, //
		@OpDependency(name = "stats.size") Function<double[], Double> sizeOp, //
		final double[] inArray)
	{
		return sumOp.apply(inArray) / sizeOp.apply(inArray);
	}

	public static void main(String... args) {
		OpEnvironment ops = OpEnvironment.getEnvironment();
		// The mean of this array is 3.0
		double[] arr = {1.0, 2.0, 3.0, 4.0, 5.0};

		// Note that we call only deal with the "stats.mean" Op - the Engine takes care of matching the dependencies for us!
		double mean = ops.unary("stats.mean").input(arr).outType(Double.class).apply();

		System.out.println("The mean of array " + Arrays.toString(arr) + " is " + mean);


	}

}
