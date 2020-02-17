package net.imagej.ops.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.TopHat;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Inplaces;
import org.scijava.ops.function.Inplaces;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class WhiteTopHats<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	@OpField(names = "morphology.topHat", params = "source, strels, numThreads, result")
	public final Functions.Arity3<Img<R>, List<Shape>, Integer, Img<R>> topHatImgList = TopHat::topHat;

	@OpField(names = "morphology.topHat", params = "source, strel, numThreads, result")
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> topHatImgSingle = TopHat::topHat;

	@OpField(names = "morphology.topHat", params = "source, strels, minValue, maxValue, numThreads, result")
	public final Functions.Arity5<Img<T>, List<Shape>, T, T, Integer, Img<T>> topHatImgListMinMax = TopHat::topHat;

	@OpField(names = "morphology.topHat", params = "source, strel, minValue, maxValue, numThreads, result")
	public final Functions.Arity5<Img<T>, Shape, T, T, Integer, Img<T>> topHatImgSingleMinMax = TopHat::topHat;

	@OpField(names = "morphology.topHat", params = "source, strels, numThreads, target")
	public final Computers.Arity3<RandomAccessible<R>, List<Shape>, Integer, IterableInterval<R>> topHatImgListComputer = (in1, in2, in3, out) -> TopHat.topHat(in1, out, in2, in3);

	@OpField(names = "morphology.topHat", params = "source, strels, minVal, maxVal, numThreads, target")
	public final Computers.Arity5<RandomAccessible<T>, List<Shape>, T, T, Integer, IterableInterval<T>> topHatImgListMinMaxComputer = (in1, in2, in3, in4, in5, out) -> TopHat.topHat(in1, out, in2, in3, in4, in5);

	@OpField(names = "morphology.topHat", params = "source, strel, numThreads, target")
	public final Computers.Arity3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> topHatImgComputer = (in1, in2, in3, out) -> TopHat.topHat(in1, out, in2, in3);

	@OpField(names = "morphology.topHat", params = "source, strel, minVal, maxVal, numThreads, target")
	public final Computers.Arity5<RandomAccessible<T>, Shape, T, T, Integer, IterableInterval<T>> topHatImgMinMaxComputer = (in1, in2, in3, in4, in5, out) -> TopHat.topHat(in1, out, in2, in3, in4, in5);

	@OpField(names = "morphology.topHat", params = "source, interval, strels, numThreads")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, List<Shape>, Integer> topHatImgListInPlace = TopHat::topHatInPlace;

	@OpField(names = "morphology.topHat", params = "source, interval, strels, minVal, maxVal, numThreads")
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, List<Shape>, T, T, Integer> topHatImgListMinMaxInplace = TopHat::topHatInPlace;

	@OpField(names = "morphology.topHat", params = "source, interval, strel, numThreads")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, Shape, Integer> topHatImgSingleInPlace = TopHat::topHatInPlace;

	@OpField(names = "morphology.topHat", params = "source, interval, strel, minVal, maxVal, numThreads")
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, Shape, T, T, Integer> topHatImgSingleMinMaxInplace = TopHat::topHatInPlace;
}
