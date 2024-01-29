
package org.scijava.ops.image.image.watershed;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * <p>
 * The Watershed algorithm segments and labels a grayscale image analogous to a
 * heightmap. In short, a drop of water following the gradient of an image flows
 * along a path to finally reach a local minimum.
 * </p>
 * This Op wraps {@link WatershedBinary} as a Function for convenience.
 *
 * @author Gabriel Selzer
 * @implNote op names='image.watershed'
 */
public class WatershedBinaryFunction<T extends BooleanType<T>, B extends BooleanType<B>>
	implements
	Functions.Arity5<RandomAccessibleInterval<T>, Boolean, Boolean, double[], RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>>
{

	@OpDependency(name = "image.watershed")
	private Computers.Arity5<RandomAccessibleInterval<T>, Boolean, Boolean, double[], RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>> watershedOp;
	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	/**
	 * TODO
	 *
	 * @param in
	 * @param useEightConnectivity
	 * @param drawWatersheds
	 * @param sigma
	 * @param mask
	 * @return the outputLabeling
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply( //
		RandomAccessibleInterval<T> in, //
		Boolean useEightConnectivity, //
		Boolean drawWatersheds, //
		double[] sigma, //
		@Nullable RandomAccessibleInterval<B> mask //
	) {
		ImgLabeling<Integer, IntType> outputLabeling = labelingCreator.apply(in,
			new IntType());
		watershedOp.compute(in, useEightConnectivity, drawWatersheds, sigma, mask,
			outputLabeling);
		return outputLabeling;
	}
}
