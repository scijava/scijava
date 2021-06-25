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
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class FloodFills<T extends Type<T>, U extends Type<U>> {

	@OpField(names = "morphology.floodFill", params = "source, seed, fillLabel, shape, target")
	public final Computers.Arity4<RandomAccessible<T>, Localizable, U, Shape, RandomAccessible<U>> fill = (source, seed,
			fillLabel, shape, target) -> FloodFill.fill(source, target, seed, fillLabel, shape);

	@OpField(names = "morphology.floodFill", params = "source, seed, fillLabel, shape, filter, target")
	public final Computers.Arity5<RandomAccessible<T>, Localizable, U, Shape, BiPredicate<T, U>, RandomAccessible<U>> fillWithPredicate = (
			source, seed, fillLabel, shape, filter,
			target) -> FloodFill.fill(source, target, seed, fillLabel, shape, filter);

	@OpField(names = "morphology.floodFill", params = "source, seed, shape, filter, writer, target")
	public final Computers.Arity5<RandomAccessible<T>, Localizable, Shape, BiPredicate<T, U>, Consumer<U>, RandomAccessible<U>> fillWithPredicateAndConsumer = (
			source, seed, shape, filter, writer, target) -> FloodFill.fill(source, target, seed, shape, filter, writer);

	@OpField(names = "morphology.floodFill", params = "source, seed, shape, target")
	public final Computers.Arity3<RandomAccessible<T>, Localizable, Shape, RandomAccessible<T>> fillSimple = (source, seed,
			shape, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		T fillLabel = sourceRA.get().copy();
		FloodFill.fill(source, target, seed, fillLabel, shape);
	};

	@OpField(names = "morphology.floodFill", priority = Priority.HIGH, params = "source, seed, fillLabel, shape, target")
	public final Computers.Arity4<RandomAccessibleInterval<T>, Localizable, U, Shape, RandomAccessibleInterval<U>> fillRAI = (
			source, seed, fillLabel, shape, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		FloodFill.fill(Views.extendValue(source, sourceRA.get()), Views.extendValue(target, fillLabel), seed, fillLabel,
				shape);
	};

	@OpField(names = "morphology.floodFill", priority = Priority.HIGH, params = "source, seed, fillLabel, shape, filter, target")
	public final Computers.Arity5<RandomAccessibleInterval<T>, Localizable, U, Shape, BiPredicate<T, U>, RandomAccessibleInterval<U>> fillWithPredicateRAI = (
			source, seed, fillLabel, shape, filter, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		FloodFill.fill(Views.extendValue(source, sourceRA.get()), Views.extendValue(target, fillLabel), seed, fillLabel, shape, filter);
	};


	@OpField(names = "morphology.floodFill", priority = Priority.HIGH, params = "source, seed, shape, target")
	public final Computers.Arity3<RandomAccessibleInterval<T>, Localizable, Shape, RandomAccessibleInterval<T>> fillSimpleRAI = (
			source, seed, shape, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		T fillLabel = sourceRA.get().copy();
		FloodFill.fill(Views.extendValue(source, fillLabel), Views.extendValue(target, fillLabel), seed, fillLabel, shape);
	};

}
