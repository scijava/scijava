package net.imagej.ops2.transform;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.EuclideanSpace;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.RandomAccessibleOnRealRandomAccessible;
import net.imglib2.view.StackView.StackAccessMode;
import net.imglib2.view.SubsampleIntervalView;
import net.imglib2.view.SubsampleView;
import net.imglib2.view.TransformView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.GenericComposite;
import net.imglib2.view.composite.NumericComposite;
import net.imglib2.view.composite.RealComposite;

import org.scijava.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Functions;
import org.scijava.plugin.Plugin;

/**
 * {@link OpCollection} containing all of the transform {@link Op}s.
 *
 * TODO move these type variables into each Op?
 *
 * @author Gabe Selzer
 *
 *
 * @param <T>
 *            - a {@link TypeVariable} with no bounds.
 * @param <R>
 *            - a {@link TypeVariable} extending {@link RealType}
 * @param <N>
 *            - a {@link TypeVariable} extending {@link NumericType}
 * @param <Y>
 *            - a {@link TypeVariable} extending {@link Type}
 * @param <F>
 *            - a {@link TypeVariable} extending
 *            {@link RandomAccessibleInterval} of type <T>
 * @param <E>
 *            - a {@link TypeVariable} extending {@link EuclideanSpace}
 */
@Plugin(type = OpCollection.class)
public class Transforms<T, R extends RealType<R>, N extends NumericType<N>, Y extends Type<Y>, F extends RandomAccessibleInterval<T>, E extends EuclideanSpace> {

	@OpField(names = "transform.addDimensionView", params = "input, result")
	public final Function<RandomAccessible<T>, MixedTransformView<T>> addDimensionView = Views::addDimension;

	@OpField(names = "transform.addDimensionView", params = "input, min, max, result")
	public final Functions.Arity3<RandomAccessibleInterval<T>, Long, Long, IntervalView<T>> addDimensionViewMinMax = Views::addDimension;

	@OpField(names = "transform.collapseView", params = "input, result", priority = Priority.LOW)
	public final Function<RandomAccessible<T>, CompositeView<T, ? extends GenericComposite<T>>> collapseViewRA = Views::collapse;

	@OpField(names = "transform.collapseView", params = "input, result")
	public final Function<RandomAccessibleInterval<T>, CompositeIntervalView<T, ? extends GenericComposite<T>>> collapseViewRAI = Views::collapse;

	@OpField(names = "transform.collapseRealView", params = "input, numChannels, result")
	public final BiFunction<RandomAccessible<R>, Integer, CompositeView<R, RealComposite<R>>> collapseRealViewRA = Views::collapseReal;

	@OpField(names = "transform.collapseRealView", params = "input, result")
	public final Function<RandomAccessibleInterval<R>, CompositeIntervalView<R, RealComposite<R>>> collapseRealViewRAI = Views::collapseReal;

	@OpField(names = "transform.collapseNumericView", params = "input, numChannels, result")
	public final BiFunction<RandomAccessible<N>, Integer, CompositeView<N, NumericComposite<N>>> collapseNumericViewRA = Views::collapseNumeric;

	@OpField(names = "transform.collapseNumericView", params = "input, result")
	public final Function<RandomAccessibleInterval<N>, CompositeIntervalView<N, NumericComposite<N>>> collapseNumericViewRAI = Views::collapseNumeric;

	@OpField(names = "transform.concatenateView", params = "concatenationAxis, inputs, result")
	public final BiFunction<Integer, RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> concatenateArray = Views::concatenate;

	@OpField(names = "transform.concatenateView", params = "concatenationAxis, inputs, result")
	public final BiFunction<Integer, List<RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> concatenateList = Views::concatenate;

	@OpField(names = "transform.concatenateView", params = "concatenationAxis, stackAccess, inputs, result")
	public final Functions.Arity3<Integer, StackAccessMode, RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> concatenateStackArray = Views::concatenate;

	@OpField(names = "transform.concatenateView", params = "concatenationAxis, stackAccess, inputs, result")
	public final Functions.Arity3<Integer, StackAccessMode, List<RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> concatenateStackList = Views::concatenate;

	@OpField(names = "transform.dropSingletonDimensionsView", params = "input, result")
	public final Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> dropSingletonDimensions = Views::dropSingletonDimensions;

	@OpField(names = "transform.extendView", params = "input, outofBoundsFactory, result")
	public final BiFunction<F, OutOfBoundsFactory<T, ? super F>, ExtendedRandomAccessibleInterval<T, F>> extendView = Views::extend;

	@OpField(names = "transform.extendBorderView", params = "input, result")
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendBorderView = Views::extendBorder;

	@OpField(names = "transform.extendMirrorDoubleView", params = "input, result")
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendMirrorDoubleView = Views::extendMirrorDouble;

	@OpField(names = "transform.extendMirrorSingleView", params = "input, result")
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendMirrorSingleView = Views::extendMirrorSingle;

	@OpField(names = "transform.extendPeriodicView", params = "input, result")
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendPeriodicView = Views::extendPeriodic;

	@OpField(names = "transform.extendRandomView", params = "input, min, max, result")
	public final Functions.Arity3<RandomAccessibleInterval<R>, Double, Double, ExtendedRandomAccessibleInterval<R, RandomAccessibleInterval<R>>> extendRandomView = Views::extendRandom;

	@OpField(names = "transform.extendValueView", params = "input, value, result")
	public final BiFunction<RandomAccessibleInterval<Y>, Y, ExtendedRandomAccessibleInterval<Y, RandomAccessibleInterval<Y>>> extendValueView = Views::extendValue;

	@OpField(names = "transform.extendZeroView", params = "input, result")
	public final Function<RandomAccessibleInterval<N>, ExtendedRandomAccessibleInterval<N, RandomAccessibleInterval<N>>> extendZeroView = Views::extendZero;

	@OpField(names = "transform.flatIterableView", params = "input, result")
	public final Function<RandomAccessibleInterval<T>, IterableInterval<T>> flatIterbleView = Views::flatIterable;

	@OpField(names = "transform.hyperSliceView", params = "input, dimesnion, position, result")
	public final Functions.Arity3<RandomAccessible<T>, Integer, Long, MixedTransformView<T>> hyperSliceRA = Views::hyperSlice;

	@OpField(names = "transform.hyperSliceView", params = "input, dimension, position, result")
	public final Functions.Arity3<RandomAccessibleInterval<T>, Integer, Long, IntervalView<T>> hyperSliceRAI = Views::hyperSlice;

	@OpField(names = "transform.hyperSlicesView", params = "input, axes, result")
	public final BiFunction<RandomAccessible<T>, int[], RandomAccessible<? extends RandomAccessible<T>>> hyperSlices = Views::hyperSlices;

	@OpField(names = "transform.interpolateView", params = "input, interpolatorFactory, result")
	public final BiFunction<E, InterpolatorFactory<T, E>, RealRandomAccessible<T>> interpolateView = Views::interpolate;

	@OpField(names = "transform.intervalView", params = "input, min, max, result")
	public final Functions.Arity3<RandomAccessible<T>, long[], long[], IntervalView<T>> intervalMinMax = Views::interval;

	@OpField(names = "transform.intervalView", params = "input, interval, result")
	public final BiFunction<RandomAccessible<T>, Interval, IntervalView<T>> intervalWithInterval = Views::interval;

	@OpField(names = "transform.invertAxisView", params = "input, dimension, result", priority = Priority.LOW)
	public final BiFunction<RandomAccessible<T>, Integer, MixedTransformView<T>> invertAxisRA = Views::invertAxis;

	@OpField(names = "transform.invertAxisView", params = "input, dimension, result")
	public final BiFunction<RandomAccessibleInterval<T>, Integer, IntervalView<T>> invertAxisRAI = Views::invertAxis;

	@OpField(names = "transform.offsetView", params = "input, offset, result", priority = Priority.LOW)
	public final BiFunction<RandomAccessible<T>, long[], MixedTransformView<T>> offsetRA = Views::offset;

	@OpField(names = "transform.offsetView", params = "input, offset, result")
	public final BiFunction<RandomAccessibleInterval<T>, long[], IntervalView<T>> offsetRAI = Views::offset;

	@OpField(names = "transform.offsetView", params = "input, intervalMin, intervalMax, result")
	public final Functions.Arity3<RandomAccessible<T>, long[], long[], IntervalView<T>> offsetIntervalMinMax = Views::offsetInterval;

	@OpField(names = "transform.offsetView", params = "input, interval, result")
	public final BiFunction<RandomAccessible<T>, Interval, IntervalView<T>> offsetInterval = Views::offsetInterval;

	@OpField(names = "transform.permuteView", params = "input, fromAxis, toAxis, result", priority = Priority.LOW)
	public final Functions.Arity3<RandomAccessible<T>, Integer, Integer, MixedTransformView<T>> permuteRA = Views::permute;

	@OpField(names = "transform.permuteView", params = "input, fromAxis, toAxis, result")
	public final Functions.Arity3<RandomAccessibleInterval<T>, Integer, Integer, IntervalView<T>> permuteRAI = Views::permute;

	@OpField(names = "transform.permuteCoordinatesInverseView", params = "input, permutation, result")
	public final BiFunction<RandomAccessibleInterval<T>, int[], IntervalView<T>> permuteCoordinatesInverse = Views::permuteCoordinatesInverse;

	@OpField(names = "transform.permuteCoordinatesInverseView", params = "input, permutation, dimension, result")
	public final Functions.Arity3<RandomAccessibleInterval<T>, int[], Integer, IntervalView<T>> permuteCoordinatesInverseSingleDim = Views::permuteCoordinatesInverse;

	@OpField(names = "transform.permuteCoordinatesView", params = "input, permutation, result")
	public final BiFunction<RandomAccessibleInterval<T>, int[], IntervalView<T>> permuteCoordinates = Views::permuteCoordinates;

	@OpField(names = "transform.permuteCoordinatesView", params = "input, permutation, dimension, result")
	public final Functions.Arity3<RandomAccessibleInterval<T>, int[], Integer, IntervalView<T>> permuteCoordinatesSingleDim = Views::permuteCoordinates;

	@OpField(names = "transform.rasterView", params = "input, result")
	public final Function<RealRandomAccessible<T>, RandomAccessibleOnRealRandomAccessible<T>> rasterize = Views::raster;

	@OpField(names = "transform.rotateView", params = "input, fromAxis, toAxis, result", priority = Priority.LOW)
	public final Functions.Arity3<RandomAccessible<T>, Integer, Integer, MixedTransformView<T>> rotateRA = Views::rotate;

	@OpField(names = "transform.rotateView", params = "input, fromAxis, toAxis, result")
	public final Functions.Arity3<RandomAccessibleInterval<T>, Integer, Integer, IntervalView<T>> rotateRAI = Views::rotate;

	@OpField(names = "transform.shearView", params = "input, shearDimension, refDimension, result")
	public final Functions.Arity3<RandomAccessible<T>, Integer, Integer, TransformView<T>> shear = Views::shear;

	@OpField(names = "transform.shearView", params = "input, interval, shearDimension, refDimension, result")
	public final Functions.Arity4<RandomAccessible<T>, Interval, Integer, Integer, IntervalView<T>> shearInterval = Views::shear;

	@OpField(names = "transform.stackView", params = "inputs, result")
	public final Function<List<? extends RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> stackList = Views::stack;

	@OpField(names = "transform.stackView", params = "inputs, result")
	public final Function<RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> stackArray = Views::stack;

	@OpField(names = "transform.stackView", params = "stackAccessMode, inputs, result")
	public final BiFunction<StackAccessMode, List<? extends RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> stackAccessList = Views::stack;

	@OpField(names = "transform.stackView", params = "stackAccessMode, inputs, result")
	public final BiFunction<StackAccessMode, RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> stackAccessArray = Views::stack;

	@OpField(names = "transform.subsampleView", params = "input, step, result", priority = Priority.LOW)
	public final BiFunction<RandomAccessible<T>, Long, SubsampleView<T>> subsampleRAUniform = Views::subsample;

	@OpField(names = "transform.subsampleView", params = "input, steps, result", priority = Priority.LOW)
	public final BiFunction<RandomAccessible<T>, long[], SubsampleView<T>> subsampleRANonuniform = Views::subsample;

	@OpField(names = "transform.subsampleView", params = "input, step, result")
	public final BiFunction<RandomAccessibleInterval<T>, Long, SubsampleIntervalView<T>> subsampleRAIUniform = Views::subsample;

	@OpField(names = "transform.subsampleView", params = "input, steps, result")
	public final BiFunction<RandomAccessibleInterval<T>, long[], SubsampleIntervalView<T>> subsampleRAINonuniform = Views::subsample;

	@OpField(names = "transform.translateView", params = "input, steps, result", priority = Priority.LOW)
	public final BiFunction<RandomAccessible<T>, long[], MixedTransformView<T>> translateRA = Views::translate;

	@OpField(names = "transform.translateView", params = "inputs, steps, result")
	public final BiFunction<RandomAccessibleInterval<T>, long[], IntervalView<T>> translateRAI = Views::translate;

	@OpField(names = "transform.unshearView", params = "input, shearDimesion, refDimension, result", priority = Priority.LOW)
	public final Functions.Arity3<RandomAccessible<T>, Integer, Integer, TransformView<T>> unshearRA = Views::unshear;

	@OpField(names = "transform.unshearView", params = "input, interval, shearDimension, refDimension, result")
	public final Functions.Arity4<RandomAccessible<T>, Interval, Integer, Integer, IntervalView<T>> unshearRAI = Views::unshear;

	@OpField(names = "transform.zeroMinView", params = "input, result")
	public final Function<RandomAccessibleInterval<T>, IntervalView<T>> zeroMinView = Views::zeroMin;
}
