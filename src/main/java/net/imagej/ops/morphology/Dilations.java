package net.imagej.ops.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.Dilation;
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
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class Dilations<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	@OpField(names = "morphology.dilate", params = "source, strels, numThreads, result")
	public final Functions.Arity3<Img<R>, List<Shape>, Integer, Img<R>> dilateImgList = Dilation::dilate;

	@OpField(names = "morphology.dilate", params = "source, strel, numThreads, result")
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> dilateImgSingle = Dilation::dilate;

	@OpField(names = "morphology.dilate", params = "source, strels, minValue, numThreads, result")
	public final Functions.Arity4<Img<T>, List<Shape>, T, Integer, Img<T>> dilateImgListMinValue = Dilation::dilate;

	@OpField(names = "morphology.dilate", params = "source, strel, minValue, numThreads, result")
	public final Functions.Arity4<Img<T>, Shape, T, Integer, Img<T>> dilateImgSingleMinValue = Dilation::dilate;

	@OpField(names = "morphology.dilate", params = "source, strels, numThreads, target")
	public final Computers.Arity3<RandomAccessible<R>, List<Shape>, Integer, IterableInterval<R>> dilateImgListComputer = (in1,
			in2, in3, out) -> Dilation.dilate(in1, out, in2, in3);

	@OpField(names = "morphology.dilate", params = "source, strels, minVal, numThreads, target")
	public final Computers.Arity4<RandomAccessible<T>, List<Shape>, T, Integer, IterableInterval<T>> dilateImgListMinValComputer = (
			in1, in2, in3, in4, out) -> Dilation.dilate(in1, out, in2, in3, in4);

	@OpField(names = "morphology.dilate", params = "source, strel, numThreads, target")
	public final Computers.Arity3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> dilateImgComputer = (in1, in2, in3,
			out) -> Dilation.dilate(in1, out, in2, in3);

	@OpField(names = "morphology.dilate", params = "source, strel, minVal, numThreads, target")
	public final Computers.Arity4<RandomAccessible<T>, Shape, T, Integer, IterableInterval<T>> dilateImgMinValComputer = (in1,
			in2, in3, in4, out) -> Dilation.dilate(in1, out, in2, in3, in4);

	@OpField(names = "morphology.dilate", params = "source, strels, numThreads, result")
	public final Functions.Arity3<Img<R>, List<Shape>, Integer, Img<R>> dilateFullImgList = Dilation::dilateFull;

	@OpField(names = "morphology.dilate", params = "source, strel, numThreads, result")
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> dilateFullImgSingle = Dilation::dilateFull;

	@OpField(names = "morphology.dilate", params = "source, strels, minValue, numThreads, result")
	public final Functions.Arity4<Img<T>, List<Shape>, T, Integer, Img<T>> dilateFullImgListMinValue = Dilation::dilateFull;

	@OpField(names = "morphology.dilate", params = "source, strel, minValue, numThreads, result")
	public final Functions.Arity4<Img<T>, Shape, T, Integer, Img<T>> dilateFullImgSingleMinValue = Dilation::dilateFull;

	@OpField(names = "morphology.dilate", params = "source, interval, strels, numThreads")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, List<Shape>, Integer> dilateImgListInPlace = Dilation::dilateInPlace;

	@OpField(names = "morphology.dilate", params = "source, interval, strels, minVal, numThreads")
	public final Inplaces.Arity5_1<RandomAccessibleInterval<T>, Interval, List<Shape>, T, Integer> dilateImgListMinValInplace = Dilation::dilateInPlace;

	@OpField(names = "morphology.dilate", params = "source, interval, strel, numThreads")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, Shape, Integer> dilateImgSingleInPlace = Dilation::dilateInPlace;

	@OpField(names = "morphology.dilate", params = "source, interval, strel, minVal, numThreads")
	public final Inplaces.Arity5_1<RandomAccessibleInterval<T>, Interval, Shape, T, Integer> dilateImgSingleMinValInplace = Dilation::dilateInPlace;
}
