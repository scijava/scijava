package net.imagej.ops.transform;

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

import org.scijava.core.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.OpField;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.QuadFunction;
import org.scijava.ops.core.TriFunction;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

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

	@OpField(names = "transform.addDimensionView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessible<T>, MixedTransformView<T>> addDimensionView = Views::addDimension;

	@OpField(names = "transform.addDimensionView")
	@Parameter(key = "input")
	@Parameter(key = "min")
	@Parameter(key = "max")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessibleInterval<T>, Long, Long, IntervalView<T>> addDimensionViewMinMax = Views::addDimension;

	@OpField(names = "transform.collapseView", priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessible<T>, CompositeView<T, ? extends GenericComposite<T>>> collapseViewRA = Views::collapse;

	@OpField(names = "transform.collapseView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessibleInterval<T>, CompositeIntervalView<T, ? extends GenericComposite<T>>> collapseViewRAI = Views::collapse;

	@OpField(names = "transform.collapseRealView")
	@Parameter(key = "input")
	@Parameter(key = "numChannels")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<R>, Integer, CompositeView<R, ? extends RealComposite<R>>> collapseRealViewRA = Views::collapseReal;

	@OpField(names = "transform.collapseRealView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessibleInterval<R>, CompositeIntervalView<R, ? extends RealComposite<R>>> collapseRealViewRAI = Views::collapseReal;

	@OpField(names = "transform.collapseNumericView")
	@Parameter(key = "input")
	@Parameter(key = "numChannels")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<N>, Integer, CompositeView<N, ? extends NumericComposite<N>>> collapseNumericViewRA = Views::collapseNumeric;

	@OpField(names = "transform.collapseNumericView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessibleInterval<N>, CompositeIntervalView<N, ? extends NumericComposite<N>>> collapseNumericViewRAI = Views::collapseNumeric;

	@OpField(names = "transform.concatenateView")
	@Parameter(key = "concatenationAxis")
	@Parameter(key = "inputs")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<Integer, RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> concatenateArray = Views::concatenate;

	@OpField(names = "transform.concatenateView")
	@Parameter(key = "concatenationAxis")
	@Parameter(key = "inputs")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<Integer, List<RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> concatenateList = Views::concatenate;

	@OpField(names = "transform.concatenateView")
	@Parameter(key = "concatenationAxis")
	@Parameter(key = "stackAccess")
	@Parameter(key = "inputs")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<Integer, StackAccessMode, RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> concatenateStackArray = Views::concatenate;

	@OpField(names = "transform.concatenateView")
	@Parameter(key = "concatenationAxis")
	@Parameter(key = "stackAccess")
	@Parameter(key = "inputs")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<Integer, StackAccessMode, List<RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> concatenateStackList = Views::concatenate;

	@OpField(names = "transform.dropSingletonDimensionsView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> dropSingletonDimensions = Views::dropSingletonDimensions;

	@OpField(names = "transform.extendView")
	@Parameter(key = "input")
	@Parameter(key = "outOfBoundsFactory")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<F, OutOfBoundsFactory<T, ? super F>, ExtendedRandomAccessibleInterval<T, F>> extendView = Views::extend;

	@OpField(names = "transform.extendBorderView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendBorderView = Views::extendBorder;

	@OpField(names = "transform.extendMirrorDoubleView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendMirrorDoubleView = Views::extendMirrorDouble;

	@OpField(names = "transform.extendMirrorSingleView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendMirrorSingleView = Views::extendMirrorSingle;

	@OpField(names = "transform.extendPeriodicView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendPeriodicView = Views::extendPeriodic;

	@OpField(names = "transform.extendRandomView")
	@Parameter(key = "input")
	@Parameter(key = "min")
	@Parameter(key = "max")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessibleInterval<R>, Double, Double, ExtendedRandomAccessibleInterval<R, RandomAccessibleInterval<R>>> extendRandomView = Views::extendRandom;

	@OpField(names = "transform.extendValueView")
	@Parameter(key = "input")
	@Parameter(key = "value")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessibleInterval<Y>, Y, ExtendedRandomAccessibleInterval<Y, RandomAccessibleInterval<Y>>> extendValueView = Views::extendValue;

	@OpField(names = "transform.extendZeroView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessibleInterval<N>, ExtendedRandomAccessibleInterval<N, RandomAccessibleInterval<N>>> extendZeroView = Views::extendZero;

	@OpField(names = "transform.flatIterableView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessibleInterval<T>, IterableInterval<T>> flatIterbleView = Views::flatIterable;

	@OpField(names = "transform.hyperSliceView")
	@Parameter(key = "input")
	@Parameter(key = "dimension")
	@Parameter(key = "position")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessible<T>, Integer, Long, MixedTransformView<T>> hyperSliceRA = Views::hyperSlice;

	@OpField(names = "transform.hyperSliceView")
	@Parameter(key = "input")
	@Parameter(key = "dimension")
	@Parameter(key = "position")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessibleInterval<T>, Integer, Long, IntervalView<T>> hyperSliceRAI = Views::hyperSlice;

	@OpField(names = "transform.hyperSlicesView")
	@Parameter(key = "input")
	@Parameter(key = "axes")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<T>, int[], RandomAccessible<? extends RandomAccessible<T>>> hyperSlices = Views::hyperSlices;

	@OpField(names = "transform.interpolateView")
	@Parameter(key = "input")
	@Parameter(key = "interpolatorFactory")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<E, InterpolatorFactory<T, E>, RealRandomAccessible<T>> interpolateView = Views::interpolate;

	@OpField(names = "transform.intervalView")
	@Parameter(key = "input")
	@Parameter(key = "min")
	@Parameter(key = "max")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessible<T>, long[], long[], IntervalView<T>> intervalMinMax = Views::interval;

	@OpField(names = "transform.intervalView")
	@Parameter(key = "input")
	@Parameter(key = "interval")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<T>, Interval, IntervalView<T>> intervalWithInterval = Views::interval;

	@OpField(names = "transform.invertAxisView", priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "dimension")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<T>, Integer, MixedTransformView<T>> invertAxisRA = Views::invertAxis;

	@OpField(names = "transform.invertAxisView")
	@Parameter(key = "input")
	@Parameter(key = "dimension")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessibleInterval<T>, Integer, IntervalView<T>> invertAxisRAI = Views::invertAxis;

	@OpField(names = "transform.offsetView", priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "offset")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<T>, long[], MixedTransformView<T>> offsetRA = Views::offset;

	@OpField(names = "transform.offsetView")
	@Parameter(key = "input")
	@Parameter(key = "offset")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessibleInterval<T>, long[], IntervalView<T>> offsetRAI = Views::offset;

	@OpField(names = "transform.offsetView")
	@Parameter(key = "input")
	@Parameter(key = "intervalMin")
	@Parameter(key = "intervalMax")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessible<T>, long[], long[], IntervalView<T>> offsetIntervalMinMax = Views::offsetInterval;

	@OpField(names = "transform.offsetView")
	@Parameter(key = "input")
	@Parameter(key = "interval")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<T>, Interval, IntervalView<T>> offsetInterval = Views::offsetInterval;

	@OpField(names = "transform.permuteView", priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "fromAxis")
	@Parameter(key = "toAxis")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessible<T>, Integer, Integer, MixedTransformView<T>> permuteRA = Views::permute;

	@OpField(names = "transform.permuteView")
	@Parameter(key = "input")
	@Parameter(key = "fromAxis")
	@Parameter(key = "toAxis")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessibleInterval<T>, Integer, Integer, IntervalView<T>> permuteRAI = Views::permute;;

	@OpField(names = "transform.permuteCoordinatesInverseView")
	@Parameter(key = "input")
	@Parameter(key = "permutation")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessibleInterval<T>, int[], IntervalView<T>> permuteCoordinatesInverse = Views::permuteCoordinatesInverse;

	@OpField(names = "transform.permuteCoordinatesInverseView")
	@Parameter(key = "input")
	@Parameter(key = "permutation")
	@Parameter(key = "dimension")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessibleInterval<T>, int[], Integer, IntervalView<T>> permuteCoordinatesInverseSingleDim = Views::permuteCoordinatesInverse;

	@OpField(names = "transform.permuteCoordinatesView")
	@Parameter(key = "input")
	@Parameter(key = "permutation")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessibleInterval<T>, int[], IntervalView<T>> permuteCoordinates = Views::permuteCoordinates;

	@OpField(names = "transform.permuteCoordinatesView")
	@Parameter(key = "input")
	@Parameter(key = "permutation")
	@Parameter(key = "dimension")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessibleInterval<T>, int[], Integer, IntervalView<T>> permuteCoordinatesSingleDim = Views::permuteCoordinates;

	@OpField(names = "transform.rasterView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RealRandomAccessible<T>, RandomAccessibleOnRealRandomAccessible<T>> rasterize = Views::raster;

	@OpField(names = "transform.rotateView", priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "fromAxis")
	@Parameter(key = "toAxis")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessible<T>, Integer, Integer, MixedTransformView<T>> rotateRA = Views::rotate;

	@OpField(names = "transform.rotateView")
	@Parameter(key = "input")
	@Parameter(key = "fromAxis")
	@Parameter(key = "toAxis")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessibleInterval<T>, Integer, Integer, IntervalView<T>> rotateRAI = Views::rotate;

	@OpField(names = "transform.shearView")
	@Parameter(key = "input")
	@Parameter(key = "shearDimension")
	@Parameter(key = "refDimension")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessible<T>, Integer, Integer, TransformView<T>> shear = Views::shear;

	@OpField(names = "transform.shearView")
	@Parameter(key = "input")
	@Parameter(key = "interval")
	@Parameter(key = "shearDimension")
	@Parameter(key = "refDimension")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final QuadFunction<RandomAccessible<T>, Interval, Integer, Integer, IntervalView<T>> shearInterval = Views::shear;

	@OpField(names = "transform.stackView")
	@Parameter(key = "inputs")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<List<? extends RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> stackList = Views::stack;

	@OpField(names = "transform.stackView")
	@Parameter(key = "inputs")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> stackArray = Views::stack;

	@OpField(names = "transform.stackView")
	@Parameter(key = "stackAccessMode")
	@Parameter(key = "inputs")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<StackAccessMode, List<? extends RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> stackAccessList = Views::stack;

	@OpField(names = "transform.stackView")
	@Parameter(key = "stackAccessMode")
	@Parameter(key = "inputs")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<StackAccessMode, RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> stackAccessArray = Views::stack;

	@OpField(names = "transform.subsampleView", priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "step")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<T>, Long, SubsampleView<T>> subsampleRAUniform = Views::subsample;

	@OpField(names = "transform.subsampleView", priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "steps")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<T>, long[], SubsampleView<T>> subsampleRANonuniform = Views::subsample;

	@OpField(names = "transform.subsampleView")
	@Parameter(key = "input")
	@Parameter(key = "step")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessibleInterval<T>, Long, SubsampleIntervalView<T>> subsampleRAIUniform = Views::subsample;

	@OpField(names = "transform.subsampleView")
	@Parameter(key = "input")
	@Parameter(key = "steps")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessibleInterval<T>, long[], SubsampleIntervalView<T>> subsampleRAINonuniform = Views::subsample;

	@OpField(names = "transform.translateView", priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "steps")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessible<T>, long[], MixedTransformView<T>> translateRA = Views::translate;

	@OpField(names = "transform.translateView")
	@Parameter(key = "inputs")
	@Parameter(key = "steps")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final BiFunction<RandomAccessibleInterval<T>, long[], IntervalView<T>> translateRAI = Views::translate;

	@OpField(names = "transform.unshearView", priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "shearDimension")
	@Parameter(key = "refDimension")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final TriFunction<RandomAccessible<T>, Integer, Integer, TransformView<T>> unshearRA = Views::unshear;

	@OpField(names = "transform.unshearView")
	@Parameter(key = "input")
	@Parameter(key = "interval")
	@Parameter(key = "shearDimension")
	@Parameter(key = "refDimension")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final QuadFunction<RandomAccessible<T>, Interval, Integer, Integer, IntervalView<T>> unshearRAI = Views::unshear;

	@OpField(names = "transform.zeroMinView")
	@Parameter(key = "input")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function<RandomAccessibleInterval<T>, IntervalView<T>> zeroMinView = Views::zeroMin;
}
