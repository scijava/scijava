/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.scijava.ops.image.transform;

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

import org.scijava.function.Functions;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;

/**
 * {@link OpCollection} containing all of the transform Ops.
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
public class Transforms<T, R extends RealType<R>, N extends NumericType<N>, Y extends Type<Y>, F extends RandomAccessibleInterval<T>, E extends EuclideanSpace> {

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.addDimensionView'
	 */
	public final Function<RandomAccessible<T>, MixedTransformView<T>> addDimensionView = Views::addDimension;

	/**
	 * @input input
	 * @input min
	 * @input max
	 * @output result
	 * @implNote op names='transform.addDimensionView'
	 */
	public final Functions.Arity3<RandomAccessibleInterval<T>, Long, Long, IntervalView<T>> addDimensionViewMinMax = Views::addDimension;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.collapseView', priority='-100.'
	 */
	public final Function<RandomAccessible<T>, CompositeView<T, ? extends GenericComposite<T>>> collapseViewRA = Views::collapse;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.collapseView'
	 */
	public final Function<RandomAccessibleInterval<T>, CompositeIntervalView<T, ? extends GenericComposite<T>>> collapseViewRAI = Views::collapse;

	/**
	 * @input input
	 * @input numChannels
	 * @output result
	 * @implNote op names='transform.collapseRealView'
	 */
	public final BiFunction<RandomAccessible<R>, Integer, CompositeView<R, RealComposite<R>>> collapseRealViewRA = Views::collapseReal;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.collapseRealView'
	 */
	public final Function<RandomAccessibleInterval<R>, CompositeIntervalView<R, RealComposite<R>>> collapseRealViewRAI = Views::collapseReal;

	/**
	 * @input input
	 * @input numChannels
	 * @output result
	 * @implNote op names='transform.collapseNumericView'
	 */
	public final BiFunction<RandomAccessible<N>, Integer, CompositeView<N, NumericComposite<N>>> collapseNumericViewRA = Views::collapseNumeric;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.collapseNumericView'
	 */
	public final Function<RandomAccessibleInterval<N>, CompositeIntervalView<N, NumericComposite<N>>> collapseNumericViewRAI = Views::collapseNumeric;

	/**
	 * @input concatenationAxis
	 * @input inputs
	 * @output result
	 * @implNote op names='transform.concatenateView'
	 */
	public final BiFunction<Integer, RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> concatenateArray = Views::concatenate;

	/**
	 * @input concatenationAxis
	 * @input inputs
	 * @output result
	 * @implNote op names='transform.concatenateView'
	 */
	public final BiFunction<Integer, List<RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> concatenateList = Views::concatenate;

	/**
	 * @input concatenationAxis
	 * @input stackAccess
	 * @input inputs
	 * @output result
	 * @implNote op names='transform.concatenateView'
	 */
	public final Functions.Arity3<Integer, StackAccessMode, RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> concatenateStackArray = Views::concatenate;

	/**
	 * @input concatenationAxis
	 * @input stackAccess
	 * @input inputs
	 * @output result
	 * @implNote op names='transform.concatenateView'
	 */
	public final Functions.Arity3<Integer, StackAccessMode, List<RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> concatenateStackList = Views::concatenate;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.dropSingletonDimensionsView'
	 */
	public final Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> dropSingletonDimensions = Views::dropSingletonDimensions;

	/**
	 * @input input
	 * @input outofBoundsFactory
	 * @output result
	 * @implNote op names='transform.extendView'
	 */
	public final BiFunction<F, OutOfBoundsFactory<T, ? super F>, ExtendedRandomAccessibleInterval<T, F>> extendView = Views::extend;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.extendBorderView'
	 */
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendBorderView = Views::extendBorder;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.extendMirrorDoubleView'
	 */
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendMirrorDoubleView = Views::extendMirrorDouble;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.extendMirrorSingleView'
	 */
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendMirrorSingleView = Views::extendMirrorSingle;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.extendPeriodicView'
	 */
	public final Function<F, ExtendedRandomAccessibleInterval<T, F>> extendPeriodicView = Views::extendPeriodic;

	/**
	 * @input input
	 * @input min
	 * @input max
	 * @output result
	 * @implNote op names='transform.extendRandomView'
	 */
	public final Functions.Arity3<RandomAccessibleInterval<R>, Double, Double, ExtendedRandomAccessibleInterval<R, RandomAccessibleInterval<R>>> extendRandomView = Views::extendRandom;

	/**
	 * @input input
	 * @input value
	 * @output result
	 * @implNote op names='transform.extendValueView'
	 */
	public final BiFunction<RandomAccessibleInterval<Y>, Y, ExtendedRandomAccessibleInterval<Y, RandomAccessibleInterval<Y>>> extendValueView = Views::extendValue;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.extendZeroView'
	 */
	public final Function<RandomAccessibleInterval<N>, ExtendedRandomAccessibleInterval<N, RandomAccessibleInterval<N>>> extendZeroView = Views::extendZero;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.flatIterableView'
	 */
	public final Function<RandomAccessibleInterval<T>, IterableInterval<T>> flatIterbleView = Views::flatIterable;

	/**
	 * @input input
	 * @input dimension
	 * @input position
	 * @output result
	 * @implNote op names='transform.hyperSliceView'
	 */
	public final Functions.Arity3<RandomAccessible<T>, Integer, Long, MixedTransformView<T>> hyperSliceRA = Views::hyperSlice;

	/**
	 * @input input
	 * @input dimension
	 * @input position
	 * @output result
	 * @implNote op names='transform.hyperSliceView'
	 */
	public final Functions.Arity3<RandomAccessibleInterval<T>, Integer, Long, IntervalView<T>> hyperSliceRAI = Views::hyperSlice;

	/**
	 * @input input
	 * @input axes
	 * @output result
	 * @implNote op names='transform.hyperSlicesView'
	 */
	public final BiFunction<RandomAccessible<T>, int[], RandomAccessible<? extends RandomAccessible<T>>> hyperSlices = Views::hyperSlices;

	/**
	 * @input input
	 * @input interpolatorFactory
	 * @output result
	 * @implNote op names='transform.interpolateView'
	 */
	public final BiFunction<E, InterpolatorFactory<T, E>, RealRandomAccessible<T>> interpolateView = Views::interpolate;

	/**
	 * @input input
	 * @input min
	 * @input max
	 * @output result
	 * @implNote op names='transform.intervalView'
	 */
	public final Functions.Arity3<RandomAccessible<T>, long[], long[], IntervalView<T>> intervalMinMax = Views::interval;

	/**
	 * @input input
	 * @input interval
	 * @output result
	 * @implNote op names='transform.intervalView'
	 */
	public final BiFunction<RandomAccessible<T>, Interval, IntervalView<T>> intervalWithInterval = Views::interval;

	/**
	 * @input input
	 * @input dimension
	 * @output result
	 * @implNote op names='transform.invertAxisView', priority='-100.'
	 */
	public final BiFunction<RandomAccessible<T>, Integer, MixedTransformView<T>> invertAxisRA = Views::invertAxis;

	/**
	 * @input input
	 * @input dimension
	 * @output result
	 * @implNote op names='transform.invertAxisView'
	 */
	public final BiFunction<RandomAccessibleInterval<T>, Integer, IntervalView<T>> invertAxisRAI = Views::invertAxis;

	/**
	 * @input input
	 * @input offset
	 * @output result
	 * @implNote op names='transform.offsetView', priority='-100.'
	 */
	public final BiFunction<RandomAccessible<T>, long[], MixedTransformView<T>> offsetRA = Views::offset;

	/**
	 * @input input
	 * @input offset
	 * @output result
	 * @implNote op names='transform.offsetView'
	 */
	public final BiFunction<RandomAccessibleInterval<T>, long[], IntervalView<T>> offsetRAI = Views::offset;

	/**
	 * @input input
	 * @input intervalMin
	 * @input intervalMax
	 * @output result
	 * @implNote op names='transform.offsetView'
	 */
	public final Functions.Arity3<RandomAccessible<T>, long[], long[], IntervalView<T>> offsetIntervalMinMax = Views::offsetInterval;

	/**
	 * @input input
	 * @input interval
	 * @output result
	 * @implNote op names='transform.offsetView'
	 */
	public final BiFunction<RandomAccessible<T>, Interval, IntervalView<T>> offsetInterval = Views::offsetInterval;

	/**
	 * @input input
	 * @input fromAxis
	 * @input toAxis
	 * @output result
	 * @implNote op names='transform.permuteView', priority='-100.'
	 */
	public final Functions.Arity3<RandomAccessible<T>, Integer, Integer, MixedTransformView<T>> permuteRA = Views::permute;

	/**
	 * @input input
	 * @input fromAxis
	 * @input toAxis
	 * @output result
	 * @implNote op names='transform.permuteView'
	 */
	public final Functions.Arity3<RandomAccessibleInterval<T>, Integer, Integer, IntervalView<T>> permuteRAI = Views::permute;

	/**
	 * @input input
	 * @input permutation
	 * @output result
	 * @implNote op names='transform.permuteCoordinatesInverseView'
	 */
	public final BiFunction<RandomAccessibleInterval<T>, int[], IntervalView<T>> permuteCoordinatesInverse = Views::permuteCoordinatesInverse;

	/**
	 * @input input
	 * @input permutation
	 * @input dimension
	 * @output result
	 * @implNote op names='transform.permuteCoordinatesInverseView'
	 */
	public final Functions.Arity3<RandomAccessibleInterval<T>, int[], Integer, IntervalView<T>> permuteCoordinatesInverseSingleDim = Views::permuteCoordinatesInverse;

	/**
	 * @input input
	 * @input permutation
	 * @output result
	 * @implNote op names='transform.permuteCoordinatesView'
	 */
	public final BiFunction<RandomAccessibleInterval<T>, int[], IntervalView<T>> permuteCoordinates = Views::permuteCoordinates;

	/**
	 * @input input
	 * @input permutation
	 * @input dimension
	 * @output result
	 * @implNote op names='transform.permuteCoordinatesView'
	 */
	public final Functions.Arity3<RandomAccessibleInterval<T>, int[], Integer, IntervalView<T>> permuteCoordinatesSingleDim = Views::permuteCoordinates;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.rasterView'
	 */
	public final Function<RealRandomAccessible<T>, RandomAccessibleOnRealRandomAccessible<T>> rasterize = Views::raster;

	/**
	 * @input input
	 * @input fromAxis
	 * @input toAxis
	 * @output result
	 * @implNote op names='transform.rotateView', priority='-100.'
	 */
	public final Functions.Arity3<RandomAccessible<T>, Integer, Integer, MixedTransformView<T>> rotateRA = Views::rotate;

	/**
	 * @input input
	 * @input fromAxis
	 * @input toAxis
	 * @output result
	 * @implNote op names='transform.rotateView'
	 */
	public final Functions.Arity3<RandomAccessibleInterval<T>, Integer, Integer, IntervalView<T>> rotateRAI = Views::rotate;

	/**
	 * @input input
	 * @input shearDimension
	 * @input refDimension
	 * @output result
	 * @implNote op names='transform.shearView'
	 */
	public final Functions.Arity3<RandomAccessible<T>, Integer, Integer, TransformView<T>> shear = Views::shear;

	/**
	 * @input input
	 * @input interval
	 * @input shearDimension
	 * @input refDimension
	 * @output result
	 * @implNote op names='transform.shearView'
	 */
	public final Functions.Arity4<RandomAccessible<T>, Interval, Integer, Integer, IntervalView<T>> shearInterval = Views::shear;

	/**
	 * @input inputs
	 * @output result
	 * @implNote op names='transform.stackView'
	 */
	public final Function<List<? extends RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> stackList = Views::stack;

	/**
	 * @input inputs
	 * @output result
	 * @implNote op names='transform.stackView'
	 */
	public final Function<RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> stackArray = Views::stack;

	/**
	 * @input stackAccessMode
	 * @input inputs
	 * @output result
	 * @implNote op names='transform.stackView'
	 */
	public final BiFunction<StackAccessMode, List<? extends RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> stackAccessList = Views::stack;

	/**
	 * @input stackAccessMode
	 * @input inputs
	 * @output result
	 * @implNote op names='transform.stackView'
	 */
	public final BiFunction<StackAccessMode, RandomAccessibleInterval<T>[], RandomAccessibleInterval<T>> stackAccessArray = Views::stack;

	/**
	 * @input input
	 * @input step
	 * @output result
	 * @implNote op names='transform.subsampleView', priority='-100.'
	 */
	public final BiFunction<RandomAccessible<T>, Long, SubsampleView<T>> subsampleRAUniform = Views::subsample;

	/**
	 * @input input
	 * @input steps
	 * @output result
	 * @implNote op names='transform.subsampleView', priority='-100.'
	 */
	public final BiFunction<RandomAccessible<T>, long[], SubsampleView<T>> subsampleRANonuniform = Views::subsample;

	/**
	 * @input input
	 * @input step
	 * @output result
	 * @implNote op names='transform.subsampleView'
	 */
	public final BiFunction<RandomAccessibleInterval<T>, Long, SubsampleIntervalView<T>> subsampleRAIUniform = Views::subsample;

	/**
	 * @input input
	 * @input steps
	 * @output result
	 * @implNote op names='transform.subsampleView'
	 */
	public final BiFunction<RandomAccessibleInterval<T>, long[], SubsampleIntervalView<T>> subsampleRAINonuniform = Views::subsample;

	/**
	 * @input input
	 * @input steps
	 * @output result
	 * @implNote op names='transform.translateView', priority='-100.'
	 */
	public final BiFunction<RandomAccessible<T>, long[], MixedTransformView<T>> translateRA = Views::translate;

	/**
	 * @input input
	 * @input steps
	 * @output result
	 * @implNote op names='transform.translateView'
	 */
	public final BiFunction<RandomAccessibleInterval<T>, long[], IntervalView<T>> translateRAI = Views::translate;

	/**
	 * @input input
	 * @input shearDimension
	 * @input refDimension
	 * @output result
	 * @implNote op names='transform.unshearView', priority='-100.'
	 */
	public final Functions.Arity3<RandomAccessible<T>, Integer, Integer, TransformView<T>> unshearRA = Views::unshear;

	/**
	 * @input input
	 * @input interval
	 * @input shearDimension
	 * @input refDimension
	 * @output result
	 * @implNote op names='transform.unshearView'
	 */
	public final Functions.Arity4<RandomAccessible<T>, Interval, Integer, Integer, IntervalView<T>> unshearRAI = Views::unshear;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='transform.zeroMinView'
	 */
	public final Function<RandomAccessibleInterval<T>, IntervalView<T>> zeroMinView = Views::zeroMin;
}
