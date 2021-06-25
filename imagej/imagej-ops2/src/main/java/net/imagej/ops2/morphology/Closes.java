package net.imagej.ops2.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.Closing;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class Closes<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.close", params = "source, strels, numThreads, result")
	public final Functions.Arity3<Img<R>, List<? extends Shape>, Integer, Img<R>> closeImgList = (in1, in2, in3) -> Closing.close(in1, (List<Shape>) in2, in3);

	@OpField(names = "morphology.close", params = "source, strel, numThreads, result")
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> closeImgSingle = Closing::close;

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.close", params = "source, strels, minValue, maxValue, numThreads, result")
	public final Functions.Arity5<Img<T>, List<? extends Shape>, T, T, Integer, Img<T>> closeImgListMinMax = (in1, in2, in3, in4, in5) -> Closing.close(in1, (List<Shape>) in2, in3, in4, in5);

	@OpField(names = "morphology.close", params = "source, strel, minValue, maxValue, numThreads, result")
	public final Functions.Arity5<Img<T>, Shape, T, T, Integer, Img<T>> closeImgSingleMinMax = Closing::close;

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.close", params = "source, strels, numThreads, target")
	public final Computers.Arity3<RandomAccessible<R>, List<? extends Shape>, Integer, IterableInterval<R>> closeImgListComputer = (in1, in2, in3, out) -> Closing.close(in1, out, (List<Shape>) in2, in3);

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.close", params = "source, strels, minVal, maxVal, numThreads, target")
	public final Computers.Arity5<RandomAccessible<T>, List<? extends Shape>, T, T, Integer, IterableInterval<T>> closeImgListMinMaxComputer = (in1, in2, in3, in4, in5, out) -> Closing.close(in1, out, (List<Shape>) in2, in3, in4, in5);

	@OpField(names = "morphology.close", params = "source, strel, numThreads, target")
	public final Computers.Arity3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> closeImgComputer = (in1, in2, in3, out) -> Closing.close(in1, out, in2, in3);

	@OpField(names = "morphology.close", params = "source, strel, minVal, maxVal, numThreads, target")
	public final Computers.Arity5<RandomAccessible<T>, Shape, T, T, Integer, IterableInterval<T>> closeImgMinMaxComputer = (in1, in2, in3, in4, in5, out) -> Closing.close(in1, out, in2, in3, in4, in5);

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.close", params = "source, interval, strels, numThreads")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, List<? extends Shape>, Integer> closeImgListInPlace = (io, in2, in3, in4) -> Closing.closeInPlace(io, in2, (List<Shape>) in3, in4);

	@SuppressWarnings("unchecked")
	@OpField(names = "morphology.close", params = "source, interval, strels, minVal, maxVal, numThreads")
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, List<? extends Shape>, T, T, Integer> closeImgListMinMaxInplace = (io, in2, in3, in4, in5, in6) -> Closing.closeInPlace(io, in2, (List<Shape>) in3, in4, in5, in6);

	@OpField(names = "morphology.close", params = "source, interval, strel, numThreads")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, Shape, Integer> closeImgSingleInPlace = Closing::closeInPlace;

	@OpField(names = "morphology.close", params = "source, interval, strel, minVal, maxVal, numThreads")
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, Shape, T, T, Integer> closeImgSingleMinMaxInplace = Closing::closeInPlace;
}
