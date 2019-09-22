package net.imagej.ops.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.BlackTopHat;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function5;
import org.scijava.ops.core.inplace.Inplace4First;
import org.scijava.ops.core.inplace.Inplace6First;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class BlackTopHats<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", itemIO = ItemIO.OUTPUT)
	public final Function3<Img<R>, List<Shape>, Integer, Img<R>> BlackTopHatImgList = BlackTopHat::blackTopHat;

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", itemIO = ItemIO.OUTPUT)
	public final Function3<Img<R>, Shape, Integer, Img<R>> BlackTopHatImgSingle = BlackTopHat::blackTopHat;

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "minValue")
	@Parameter(key = "maxValue")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", itemIO = ItemIO.OUTPUT)
	public final Function5<Img<T>, List<Shape>, T, T, Integer, Img<T>> BlackTopHatImgListMinMax = BlackTopHat::blackTopHat;

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "minValue")
	@Parameter(key = "maxValue")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", itemIO = ItemIO.OUTPUT)
	public final Function5<Img<T>, Shape, T, T, Integer, Img<T>> BlackTopHatImgSingleMinMax = BlackTopHat::blackTopHat;

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "numThreads")
	@Parameter(key = "target", itemIO = ItemIO.BOTH)
	public final Computer3<RandomAccessible<R>, List<Shape>, Integer, IterableInterval<R>> BlackTopHatImgListComputer = (in1, in2, in3, out) -> BlackTopHat.blackTopHat(in1, out, in2, in3);

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "minVal")
	@Parameter(key = "maxVal")
	@Parameter(key = "numThreads")
	@Parameter(key = "target", itemIO = ItemIO.BOTH)
	public final Computer5<RandomAccessible<T>, List<Shape>, T, T, Integer, IterableInterval<T>> BlackTopHatImgListMinMaxComputer = (in1, in2, in3, in4, in5, out) -> BlackTopHat.blackTopHat(in1, out, in2, in3, in4, in5);

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "numThreads")
	@Parameter(key = "target", itemIO = ItemIO.BOTH)
	public final Computer3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> BlackTopHatImgComputer = (in1, in2, in3, out) -> BlackTopHat.blackTopHat(in1, out, in2, in3);

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "minVal")
	@Parameter(key = "maxVal")
	@Parameter(key = "numThreads")
	@Parameter(key = "target", itemIO = ItemIO.BOTH)
	public final Computer5<RandomAccessible<T>, Shape, T, T, Integer, IterableInterval<T>> BlackTopHatImgMinMaxComputer = (in1, in2, in3, in4, in5, out) -> BlackTopHat.blackTopHat(in1, out, in2, in3, in4, in5);

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source", itemIO = ItemIO.BOTH)
	@Parameter(key = "interval")
	@Parameter(key = "strels")
	@Parameter(key = "numThreads")
	public final Inplace4First<RandomAccessibleInterval<R>, Interval, List<Shape>, Integer> BlackTopHatImgListInPlace = BlackTopHat::blackTopHatInPlace;

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source", itemIO = ItemIO.BOTH)
	@Parameter(key = "interval")
	@Parameter(key = "strels")
	@Parameter(key = "minVal")
	@Parameter(key = "maxVal")
	@Parameter(key = "numThreads")
	public final Inplace6First<RandomAccessibleInterval<T>, Interval, List<Shape>, T, T, Integer> BlackTopHatImgListMinMaxInplace = BlackTopHat::blackTopHatInPlace;

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source", itemIO = ItemIO.BOTH)
	@Parameter(key = "interval")
	@Parameter(key = "strel")
	@Parameter(key = "numThreads")
	public final Inplace4First<RandomAccessibleInterval<R>, Interval, Shape, Integer> BlackTopHatImgSingleInPlace = BlackTopHat::blackTopHatInPlace;

	@OpField(names = "morphology.BlackTopHat")
	@Parameter(key = "source", itemIO = ItemIO.BOTH)
	@Parameter(key = "interval")
	@Parameter(key = "strel")
	@Parameter(key = "minVal")
	@Parameter(key = "maxVal")
	@Parameter(key = "numThreads")
	public final Inplace6First<RandomAccessibleInterval<T>, Interval, Shape, T, T, Integer> BlackTopHatImgSingleMinMaxInplace = BlackTopHat::blackTopHatInPlace;
}
