package net.imagej.ops2.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.BlackTopHat;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

/**
 * Wrapper Ops for imglib2-algorithm's Black Top Hats algorithms TODO: Revert to
 * the nice lambda syntax with all of the List ops once imglib2-algorithm
 * reaches a new version.
 * 
 * @author Gabriel Selzer
 * @param <T>
 * @param <R>
 */
@Plugin(type = OpCollection.class)
public class BlackTopHats<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.BlackTopHat", params = "source, strels, numThreads, result")
	public final Functions.Arity3<Img<R>, List<? extends Shape>, Integer, Img<R>> BlackTopHatImgList = (in1, in2, in3) -> BlackTopHat.blackTopHat(in1, (List<Shape>) in2, in3);

	@OpField(names = "morphology.BlackTopHat", params = "source, strel, numThreads, result")
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> BlackTopHatImgSingle = BlackTopHat::blackTopHat;

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.BlackTopHat", params = "source, strels, minValue, maxValue, numThreads, result")
	public final Functions.Arity5<Img<T>, List<? extends Shape>, T, T, Integer, Img<T>> BlackTopHatImgListMinMax = (in1, in2, in3, in4, in5) -> BlackTopHat.blackTopHat(in1, (List<Shape>) in2, in3, in4, in5);

	@OpField(names = "morphology.BlackTopHat", params = "source, strel, minValue, maxValue, numThreads, result")
	public final Functions.Arity5<Img<T>, Shape, T, T, Integer, Img<T>> BlackTopHatImgSingleMinMax = BlackTopHat::blackTopHat;

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.BlackTopHat", params = "source, strels, numThreads, target")
	public final Computers.Arity3<RandomAccessible<R>, List<? extends Shape>, Integer, IterableInterval<R>> BlackTopHatImgListComputer = (in1, in2, in3, out) -> BlackTopHat.blackTopHat(in1, out, (List<Shape>) in2, in3);

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.BlackTopHat", params = "source, strels, minVal, maxVal, numThreads, target")
	public final Computers.Arity5<RandomAccessible<T>, List<? extends Shape>, T, T, Integer, IterableInterval<T>> BlackTopHatImgListMinMaxComputer = (in1, in2, in3, in4, in5, out) -> BlackTopHat.blackTopHat(in1, out, (List<Shape>) in2, in3, in4, in5);

	@OpField(names = "morphology.BlackTopHat", params = "source, strel, numThreads, target")
	public final Computers.Arity3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> BlackTopHatImgComputer = (in1, in2, in3, out) -> BlackTopHat.blackTopHat(in1, out, in2, in3);

	@OpField(names = "morphology.BlackTopHat", params = "source, strel, minVal, maxVal, numThreads, target")
	public final Computers.Arity5<RandomAccessible<T>, Shape, T, T, Integer, IterableInterval<T>> BlackTopHatImgMinMaxComputer = (in1, in2, in3, in4, in5, out) -> BlackTopHat.blackTopHat(in1, out, in2, in3, in4, in5);

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.BlackTopHat", params = "source, interval, strels, numThreads")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, List<? extends Shape>, Integer> BlackTopHatImgListInPlace = (io, in2, in3, in4) -> BlackTopHat.blackTopHatInPlace(io, in2, (List<Shape>) in3, in4);

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.BlackTopHat", params = "source, interval, strels, minVal, maxVal, numThreads")
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, List<? extends Shape>, T, T, Integer> BlackTopHatImgListMinMaxInplace = (io, in2, in3, in4, in5, in6) -> BlackTopHat.blackTopHatInPlace(io, in2, (List<Shape>) in3, in4, in5, in6);

	@OpField(names = "morphology.BlackTopHat", params = "source, interval, strel, numThreads")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, Shape, Integer> BlackTopHatImgSingleInPlace = BlackTopHat::blackTopHatInPlace;

	@OpField(names = "morphology.BlackTopHat", params = "source, interval, strel, minVal, maxVal, numThreads")
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, Shape, T, T, Integer> BlackTopHatImgSingleMinMaxInplace = BlackTopHat::blackTopHatInPlace;
}
