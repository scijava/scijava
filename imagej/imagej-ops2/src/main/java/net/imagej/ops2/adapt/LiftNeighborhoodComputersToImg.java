
package net.imagej.ops2.adapt;

import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpMethod;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.view.Views;

public class LiftNeighborhoodComputersToImg {

	/**
	 * @implNote op names='adapt', priority='100.',
	 *           type='java.util.function.Function'
	 */
	public static <T, U>
		Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<U>>
		adapt1UsingShape(Computers.Arity1<Neighborhood<T>, U> op)
	{
		OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>> oobf //
			= new OutOfBoundsMirrorFactory<>(
				OutOfBoundsMirrorFactory.Boundary.SINGLE);
		return (in, shape, out) -> {
			var extended = Views.extend(in, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in);
			LoopBuilder.setImages(intervaled, out).forEachPixel(op);
		};
	}

	/**
	 * @implNote op names='adapt', priority='100.',
	 *           type='java.util.function.Function'
	 */
	@OpMethod(names = "adapt", type = Function.class)
	public static <T, U, F extends RandomAccessibleInterval<T>>
		Computers.Arity3<F, Shape, OutOfBoundsFactory<T, F>, RandomAccessibleInterval<U>>
		adapt1UsingShapeAndOOBF(Computers.Arity1<Neighborhood<T>, U> op)
	{
		return (in, shape, oobf, out) -> {
			var extended = Views.extend(in, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in);
			LoopBuilder.setImages(intervaled, out).forEachPixel(op);
		};
	}

	/**
	 * @implNote op names='adapt', priority='100.',
	 *           type='java.util.function.Function'
	 */
	public static <T, U, V>
		Computers.Arity3<RandomAccessibleInterval<T>, V, Shape, RandomAccessibleInterval<U>>
		adapt2UsingShape(Computers.Arity2<Neighborhood<T>, V, U> op)
	{
		OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>> oobf //
			= new OutOfBoundsMirrorFactory<>(
				OutOfBoundsMirrorFactory.Boundary.SINGLE);
		return (in1, in2, shape, out) -> {
			var extended = Views.extend(in1, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in1);
			LoopBuilder.setImages(intervaled, out).forEachPixel((in, container) -> op
				.compute(in, in2, container));
		};
	}

	/**
	 * @implNote op names='adapt', priority='100.',
	 *           type='java.util.function.Function'
	 */
	@OpMethod(names = "adapt", type = Function.class)
	public static <T, U, V, F extends RandomAccessibleInterval<T>>
		Computers.Arity4<F, V, Shape, OutOfBoundsFactory<T, F>, RandomAccessibleInterval<U>>
		adapt2UsingShapeAndOOBF(Computers.Arity2<Neighborhood<T>, V, U> op)
	{
		return (in1, in2, shape, oobf, out) -> {
			var extended = Views.extend(in1, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in1);
			LoopBuilder.setImages(intervaled, out).forEachPixel((in, container) -> op
				.compute(in, in2, container));
		};
	}

	/**
	 * @implNote op names='adapt', priority='100.',
	 *           type='java.util.function.Function'
	 */
	public static <T, U, V, W>
		Computers.Arity4<RandomAccessibleInterval<T>, V, W, Shape, RandomAccessibleInterval<U>>
		adapt3UsingShape(Computers.Arity3<Neighborhood<T>, V, W, U> op)
	{
		OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>> oobf //
			= new OutOfBoundsMirrorFactory<>(
				OutOfBoundsMirrorFactory.Boundary.SINGLE);
		return (in1, in2, in3, shape, out) -> {
			var extended = Views.extend(in1, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in1);
			LoopBuilder.setImages(intervaled, out).forEachPixel((in, container) -> op
				.compute(in, in2, in3, container));
		};
	}

	/**
	 * @implNote op names='adapt', priority='100.',
	 *           type='java.util.function.Function'
	 */
	@OpMethod(names = "adapt", type = Function.class)
	public static <T, U, V, W, F extends RandomAccessibleInterval<T>>
		Computers.Arity5<F, V, W, Shape, OutOfBoundsFactory<T, F>, RandomAccessibleInterval<U>>
		adapt3UsingShapeAndOOBF(Computers.Arity3<Neighborhood<T>, V, W, U> op)
	{
		return (in1, in2, in3, shape, oobf, out) -> {
			var extended = Views.extend(in1, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in1);
			LoopBuilder.setImages(intervaled, out).forEachPixel((in, container) -> op
				.compute(in, in2, in3, container));
		};
	}
}
