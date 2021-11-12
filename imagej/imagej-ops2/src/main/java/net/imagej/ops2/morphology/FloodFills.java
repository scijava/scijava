package net.imagej.ops2.morphology;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fill.FloodFill;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class FloodFills<T extends Type<T>, U extends Type<U>> {

	/**
	 * @input source
	 * @input seed
	 * @input fillLabel
	 * @input shape
	 * @container target
	 * @implNote op names='morphology.floodFill'
	 */
	public final Computers.Arity4<RandomAccessible<T>, Localizable, U, Shape, RandomAccessible<U>> fill = (source, seed,
			fillLabel, shape, target) -> FloodFill.fill(source, target, seed, fillLabel, shape);

	/**
	 * @input source
	 * @input seed
	 * @input fillLabel
	 * @input shape
	 * @input filter
	 * @container target
	 * @implNote op names='morphology.floodFill'
	 */
	public final Computers.Arity5<RandomAccessible<T>, Localizable, U, Shape, BiPredicate<T, U>, RandomAccessible<U>> fillWithPredicate = (
			source, seed, fillLabel, shape, filter,
			target) -> FloodFill.fill(source, target, seed, fillLabel, shape, filter);

	/**
	 * @input source
	 * @input seed
	 * @input shape
	 * @input filter
	 * @input writer
	 * @container target
	 * @implNote op names='morphology.floodFill'
	 */
	public final Computers.Arity5<RandomAccessible<T>, Localizable, Shape, BiPredicate<T, U>, Consumer<U>, RandomAccessible<U>> fillWithPredicateAndConsumer = (
			source, seed, shape, filter, writer, target) -> FloodFill.fill(source, target, seed, shape, filter, writer);

	/**
	 * @input source
	 * @input seed
	 * @input shape
	 * @container target
	 * @implNote op names='morphology.floodFill'
	 */
	public final Computers.Arity3<RandomAccessible<T>, Localizable, Shape, RandomAccessible<T>> fillSimple = (source, seed,
			shape, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		T fillLabel = sourceRA.get().copy();
		FloodFill.fill(source, target, seed, fillLabel, shape);
	};

	/**
	 * @input source
	 * @input seed
	 * @input fillLabel
	 * @input shape
	 * @container target
	 * @implNote op names='morphology.floodFill', priority='100.'
	 */
	public final Computers.Arity4<RandomAccessibleInterval<T>, Localizable, U, Shape, RandomAccessibleInterval<U>> fillRAI = (
			source, seed, fillLabel, shape, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		FloodFill.fill(Views.extendValue(source, sourceRA.get()), Views.extendValue(target, fillLabel), seed, fillLabel,
				shape);
	};

	/**
	 * @input source
	 * @input seed
	 * @input fillLabel
	 * @input shape
	 * @input filter
	 * @container target
	 * @implNote op names='morphology.floodFill', priority='100.'
	 */
	public final Computers.Arity5<RandomAccessibleInterval<T>, Localizable, U, Shape, BiPredicate<T, U>, RandomAccessibleInterval<U>> fillWithPredicateRAI = (
			source, seed, fillLabel, shape, filter, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		FloodFill.fill(Views.extendValue(source, sourceRA.get()), Views.extendValue(target, fillLabel), seed, fillLabel, shape, filter);
	};

	/**
	 * @input source
	 * @input seed
	 * @input shape
	 * @container target
	 * @implNote op names='morphology.floodFill', priority='100.'
	 */
	public final Computers.Arity3<RandomAccessibleInterval<T>, Localizable, Shape, RandomAccessibleInterval<T>> fillSimpleRAI = (
			source, seed, shape, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		T fillLabel = sourceRA.get().copy();
		FloodFill.fill(Views.extendValue(source, fillLabel), Views.extendValue(target, fillLabel), seed, fillLabel, shape);
	};

}
