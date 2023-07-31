package net.imagej.ops2.adapt.complexLift;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.img.Img;
import net.imglib2.util.Util;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Producer;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpMethod;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.Type;
import net.imglib2.type.operators.SetZero;
import net.imglib2.view.Views;

public class LiftNeighborhoodComputersToFunctionsOnImgs {


	/**
	 * @implNote op names='adapt', priority='100.', type='java.util.function.Function'
	 */
	public static <T, U> BiFunction<RandomAccessibleInterval<T>, Shape, Img<T>>
	adaptUsingShape(
			@OpDependency(name="create", adaptable = false) BiFunction<Dimensions, T, Img<T>> creator,
			Computers.Arity1<Neighborhood<T>, T> op
	)
	{
		OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>> oobf //
			= new OutOfBoundsMirrorFactory<>(OutOfBoundsMirrorFactory.Boundary.SINGLE);
		return (in, shape) -> {
			var output = creator.apply(in, Util.getTypeFromInterval(in));
			var extended = Views.extend(in, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in);
			LoopBuilder.setImages(intervaled, output).forEachPixel(op);
			return output;
		};
	}

	/**
	 * @implNote op names='adapt', priority='100.', type='java.util.function.Function'
	 */
	@OpMethod(names = "adapt", type = Function.class)
	public static <T, F extends RandomAccessibleInterval<T>>
	Functions.Arity3<F, Shape, OutOfBoundsFactory<T, ? super F>, Img<T>>
	adaptUsingShapeAndOOBF(
			@OpDependency(name="create", adaptable = false) BiFunction<Dimensions, T, Img<T>> creator,
			Computers.Arity1<Neighborhood<T>, T> op)
	{
		return (in, shape, oobf) -> {
			var output = creator.apply(in, Util.getTypeFromInterval(in));
			var extended = Views.extend(in, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in);
			LoopBuilder.setImages(intervaled, output).forEachPixel(op);
			return output;
		};
	}

}
