package net.imagej.ops2.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;

public class Dilations<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.dilate'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity3<Img<R>, List<? extends Shape>, Integer, Img<R>> dilateImgList = (in1, in2, in3) -> Dilation.dilate(in1, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.dilate'
	 */
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> dilateImgSingle = Dilation::dilate;

	/**
	 * @input source
	 * @input strels
	 * @input minValue
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.dilate'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity4<Img<T>, List<? extends Shape>, T, Integer, Img<T>> dilateImgListMinValue = (in1, in2, in3, in4) -> Dilation.dilate(in1, (List<Shape>) in2, in3, in4);

	/**
	 * @input source
	 * @input strel
	 * @input minValue
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.dilate'
	 */
	public final Functions.Arity4<Img<T>, Shape, T, Integer, Img<T>> dilateImgSingleMinValue = Dilation::dilate;

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.dilate'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity3<RandomAccessible<R>, List<? extends Shape>, Integer, IterableInterval<R>> dilateImgListComputer = (in1,
			in2, in3, out) -> Dilation.dilate(in1, out, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strels
	 * @input minVal
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.dilate'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity4<RandomAccessible<T>, List<? extends Shape>, T, Integer, IterableInterval<T>> dilateImgListMinValComputer = (
			in1, in2, in3, in4, out) -> Dilation.dilate(in1, out, (List<Shape>) in2, in3, in4);

	/**
	 * @input source
	 * @input strels
	 * @input minVal
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.dilate'
	 */
	public final Computers.Arity3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> dilateImgComputer = (in1, in2, in3,
			out) -> Dilation.dilate(in1, out, in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input minVal
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.dilate'
	 */
	public final Computers.Arity4<RandomAccessible<T>, Shape, T, Integer, IterableInterval<T>> dilateImgMinValComputer = (in1,
			in2, in3, in4, out) -> Dilation.dilate(in1, out, in2, in3, in4);

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.dilate'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity3<Img<R>, List<? extends Shape>, Integer, Img<R>> dilateFullImgList = (in1, in2, in3) -> Dilation.dilateFull(in1, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.dilate'
	 */
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> dilateFullImgSingle = Dilation::dilateFull;

	/**
	 * @input source
	 * @input strels
	 * @input minValue
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.dilate'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity4<Img<T>, List<? extends Shape>, T, Integer, Img<T>> dilateFullImgListMinValue = (in1, in2, in3, in4) -> Dilation.dilateFull(in1, (List<Shape>) in2, in3, in4);

	/**
	 * @input source
	 * @input strel
	 * @input minValue
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.dilate'
	 */
	public final Functions.Arity4<Img<T>, Shape, T, Integer, Img<T>> dilateFullImgSingleMinValue = Dilation::dilateFull;

	/**
	 * @mutable source
	 * @input interval
	 * @input strels
	 * @input numThreads
	 * @implNote op names='morphology.dilate'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, List<? extends Shape>, Integer> dilateImgListInPlace = (io, in2, in3, in4) -> Dilation.dilateInPlace(io, in2, (List<Shape>) in3, in4);

	/**
	 * @mutable source
	 * @input interval
	 * @input strels
	 * @input minValue
	 * @input numThreads
	 * @implNote op names='morphology.dilate'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity5_1<RandomAccessibleInterval<T>, Interval, List<? extends Shape>, T, Integer> dilateImgListMinValInplace = (io, in2, in3, in4, in5) -> Dilation.dilateInPlace(io, in2, (List<Shape>) in3, in4, in5);

	/**
	 * @mutable source
	 * @input interval
	 * @input strel
	 * @input numThreads
	 * @implNote op names='morphology.dilate'
	 */
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, Shape, Integer> dilateImgSingleInPlace = Dilation::dilateInPlace;

	/**
	 * @mutable source
	 * @input interval
	 * @input strel
	 * @input minVal
	 * @input numThreads
	 * @implNote op names='morphology.dilate'
	 */
	public final Inplaces.Arity5_1<RandomAccessibleInterval<T>, Interval, Shape, T, Integer> dilateImgSingleMinValInplace = Dilation::dilateInPlace;
}
