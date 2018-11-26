package net.imagej.ops.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.Erosion;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.function.Function4;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.inplace.Inplace4First;
import org.scijava.ops.core.inplace.Inplace5First;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class Erosions<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function3<Img<R>, List<Shape>, Integer, Img<R>> erodeImgList = Erosion::erode;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function3<Img<R>, Shape, Integer, Img<R>> erodeImgSingle = Erosion::erode;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "minValue")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function4<Img<T>, List<Shape>, T, Integer, Img<T>> erodeImgListMinValue = Erosion::erode;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "minValue")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function4<Img<T>, Shape, T, Integer, Img<T>> erodeImgSingleMinValue = Erosion::erode;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "numThreads")
	@Parameter(key = "target", type = ItemIO.BOTH)
	public final Computer3<RandomAccessible<R>, List<Shape>, Integer, IterableInterval<R>> erodeImgListComputer = (in1, in2, in3, out) -> Erosion.erode(in1, out, in2, in3);

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "minVal")
	@Parameter(key = "numThreads")
	@Parameter(key = "target", type = ItemIO.BOTH)
	public final Computer4<RandomAccessible<T>, List<Shape>, T, Integer, IterableInterval<T>> erodeImgListMinValComputer = (in1, in2, in3, in4, out) -> Erosion.erode(in1, out, in2, in3, in4);

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "numThreads")
	@Parameter(key = "target", type = ItemIO.BOTH)
	public final Computer3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> erodeImgComputer = (in1, in2, in3, out) -> Erosion.erode(in1, out, in2, in3);

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "minVal")
	@Parameter(key = "numThreads")
	@Parameter(key = "target", type = ItemIO.BOTH)
	public final Computer4<RandomAccessible<T>, Shape, T, Integer, IterableInterval<T>> erodeImgMinValComputer = (in1, in2, in3, in4, out) -> Erosion.erode(in1, out, in2, in3, in4);

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function3<Img<R>, List<Shape>, Integer, Img<R>> erodeFullImgList = Erosion::erodeFull;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function3<Img<R>, Shape, Integer, Img<R>> erodeFullImgSingle = Erosion::erodeFull;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strels")
	@Parameter(key = "minValue")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function4<Img<T>, List<Shape>, T, Integer, Img<T>> erodeFullImgListMinValue = Erosion::erodeFull;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source")
	@Parameter(key = "strel")
	@Parameter(key = "minValue")
	@Parameter(key = "numThreads")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public final Function4<Img<T>, Shape, T, Integer, Img<T>> erodeFullImgSingleMinValue = Erosion::erodeFull;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source", type = ItemIO.BOTH)
	@Parameter(key = "interval")
	@Parameter(key = "strels")
	@Parameter(key = "numThreads")
	public final Inplace4First<RandomAccessibleInterval<R>, Interval, List<Shape>, Integer> erodeImgListInPlace = Erosion::erodeInPlace;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source", type = ItemIO.BOTH)
	@Parameter(key = "interval")
	@Parameter(key = "strels")
	@Parameter(key = "minVal")
	@Parameter(key = "numThreads")
	public final Inplace5First<RandomAccessibleInterval<T>, Interval, List<Shape>, T, Integer> erodeImgListMinValInplace = Erosion::erodeInPlace;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source", type = ItemIO.BOTH)
	@Parameter(key = "interval")
	@Parameter(key = "strel")
	@Parameter(key = "numThreads")
	public final Inplace4First<RandomAccessibleInterval<R>, Interval, Shape, Integer> erodeImgSingleInPlace = Erosion::erodeInPlace;

	@OpField(names = "morphology.erode")
	@Parameter(key = "source", type = ItemIO.BOTH)
	@Parameter(key = "interval")
	@Parameter(key = "strel")
	@Parameter(key = "minVal")
	@Parameter(key = "numThreads")
	public final Inplace5First<RandomAccessibleInterval<T>, Interval, Shape, T, Integer> erodeImgSingleMinValInplace = Erosion::erodeInPlace;
}
