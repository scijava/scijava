package net.imagej.ops2.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.Opening;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;

public class Opens<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.open'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity3<Img<R>, List<? extends Shape>, Integer, Img<R>> openImgList = (in1, in2, in3) -> Opening.open(in1, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.open'
	 */
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> openImgSingle = Opening::open;

/**
 * @input source
 * @input strels
 * @input minValue
 * @input maxValue
 * @input numThreads
 * @output result
 * @implNote op names='morphology.open'
 */
 @SuppressWarnings("unchecked")
	public final Functions.Arity5<Img<T>, List<? extends Shape>, T, T, Integer, Img<T>> openImgListMinMax = (in1, in2, in3, in4, in5) -> Opening.open(in1, (List<Shape>) in2, in3, in4, in5);

	/**
	 * @input source
	 * @input strel
	 * @input minValue
	 * @input maxValue
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.open'
	 */
	public final Functions.Arity5<Img<T>, Shape, T, T, Integer, Img<T>> openImgSingleMinMax = Opening::open;

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.open'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity3<RandomAccessible<R>, List<? extends Shape>, Integer, IterableInterval<R>> openImgListComputer = (in1, in2, in3, out) -> Opening.open(in1, out, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strels
	 * @input minVal
	 * @input maxVal
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.open'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity5<RandomAccessible<T>, List<? extends Shape>, T, T, Integer, IterableInterval<T>> openImgListMinMaxComputer = (in1, in2, in3, in4, in5, out) -> Opening.open(in1, out, (List<Shape>) in2, in3, in4, in5);

	/**
	 * @input source
	 * @input strel
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.open'
	 */
	public final Computers.Arity3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> openImgComputer = (in1, in2, in3, out) -> Opening.open(in1, out, in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input minVal
	 * @input maxVal
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.open'
	 */
	public final Computers.Arity5<RandomAccessible<T>, Shape, T, T, Integer, IterableInterval<T>> openImgMinMaxComputer = (in1, in2, in3, in4, in5, out) -> Opening.open(in1, out, in2, in3, in4, in5);

	/**
	 * @mutable source
	 * @input interval
	 * @input strels
	 * @input numThreads
	 * @implNote op names='morphology.open'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, List<? extends Shape>, Integer> openImgListInPlace = (io, in2, in3, in4) -> Opening.openInPlace(io, in2, (List<Shape>) in3, in4);

	/**
	 * @mutable source
	 * @input interval
	 * @input strels
	 * @input minVal
	 * @input maxVal
	 * @input numThreads
	 * @implNote op names='morphology.open'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, List<? extends Shape>, T, T, Integer> openImgListMinMaxInplace = (io, in2, in3, in4, in5, in6) -> Opening.openInPlace(io, in2, (List<Shape>) in3, in4, in5, in6);

	/**
	 * @mutable source
	 * @input interval
	 * @input strel
	 * @input numThreads
	 * @implNote op names='morphology.open'
	 */
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, Shape, Integer> openImgSingleInPlace = Opening::openInPlace;

	/**
	 * @mutable source
	 * @input interval
	 * @input strel
	 * @input minVal
	 * @input maxVal
	 * @input numThreads
	 * @implNote op names='morphology.open'
	 */
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, Shape, T, T, Integer> openImgSingleMinMaxInplace = Opening::openInPlace;
}
