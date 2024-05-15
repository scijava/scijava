
package org.scijava.ops.image.filter.gaussianSub;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * @author Edward Evans
 * @param <I> input type
 * @implNote op names='filter.gaussianSub', priority='100.'
 */
public class GaussianSubtraction<I extends RealType<I>> implements
	BiFunction<RandomAccessibleInterval<I>, Double, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	@OpDependency(name = "filter.gauss")
	private BiFunction<RandomAccessibleInterval<I>, Double, RandomAccessibleInterval<DoubleType>> gaussOp;

	@OpDependency(name = "math.sub")
	private Computers.Arity2<RandomAccessibleInterval<I>, RandomAccessibleInterval<DoubleType>, RandomAccessibleInterval<DoubleType>> subOp;

	/**
	 * Gaussian blur subtraction Op. This performs basic feature extraction by
	 * subtracting the input image with a Gaussian blurred copy, leaving the
	 * non-blurred structures.
	 *
	 * @param input input image
	 * @param sigma sigma value for gaussian blur
	 * @return guassian blur subtracted image
	 */
	@Override
	public RandomAccessibleInterval<DoubleType> apply(
		RandomAccessibleInterval<I> input, Double sigma)
	{
		RandomAccessibleInterval<DoubleType> blur = gaussOp.apply(input, sigma);
		RandomAccessibleInterval<DoubleType> output = createOp.apply(input,
			new DoubleType());
		subOp.compute(input, blur, output);
		return output;
	}
}
