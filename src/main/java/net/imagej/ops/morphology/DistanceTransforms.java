package net.imagej.ops.morphology;

import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.distance.Distance;
import net.imglib2.algorithm.morphology.distance.DistanceTransform;
import net.imglib2.algorithm.morphology.distance.DistanceTransform.DISTANCE_TYPE;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpField;
import org.scijava.ops.core.ExceptionUtils;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.inplace.BiInplaceFirst;
import org.scijava.ops.core.inplace.Inplace3First;
import org.scijava.ops.core.inplace.Inplace4First;
import org.scijava.ops.core.inplace.Inplace5First;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class DistanceTransforms<T extends RealType<T>, U extends RealType<U>> {

	@OpField(names = "morphology.distanceTransform")
	@Parameter(key = "source", type = ItemIO.BOTH)
	@Parameter(key = "distanceType")
	@Parameter(key = "weights")
	public final Inplace3First<RandomAccessibleInterval<T>, DISTANCE_TYPE, double[]> transformInplace = DistanceTransform::transform;

	@OpField(names = "morphology.distanceTransform")
	@Parameter(key = "source", type = ItemIO.BOTH)
	@Parameter(key = "distanceType")
	@Parameter(key = "executorService")
	@Parameter(key = "numTasks")
	@Parameter(key = "weights")
	public final Inplace5First<RandomAccessibleInterval<T>, DISTANCE_TYPE, ExecutorService, Integer, double[]> transformExServiceInplace = (
			source, distanceType, executorService, numTasks, weights) -> ExceptionUtils.execute(
					() -> DistanceTransform.transform(source, distanceType, executorService, numTasks, weights));

	@OpField(names = "morphology.distanceTransform")
	@Parameter(key = "source")
	@Parameter(key = "distanceType")
	@Parameter(key = "weights")
	@Parameter(key = "target", type = ItemIO.BOTH)
	public final Computer3<RandomAccessibleInterval<T>, DISTANCE_TYPE, double[], RandomAccessibleInterval<T>> transformComputer = (
			in1, in2, in3, out) -> DistanceTransform.transform(in1, out, in2, in3);

	@OpField(names = "morphology.distanceTransform")
	@Parameter(key = "source")
	@Parameter(key = "distanceType")
	@Parameter(key = "executorService")
	@Parameter(key = "numTasks")
	@Parameter(key = "weights")
	@Parameter(key = "target", type = ItemIO.BOTH)
	public final Computer5<RandomAccessibleInterval<T>, DISTANCE_TYPE, ExecutorService, Integer, double[], RandomAccessibleInterval<U>> transformExServiceComputer = (
			source, distanceType, executorService, numTasks, weights,
			target) -> ExceptionUtils.execute(() -> DistanceTransform.transform(source, target, distanceType,
					executorService, numTasks, weights));

	@OpField(names = "morphology.distanceTransform")
	@Parameter(key = "source", type = ItemIO.BOTH)
	@Parameter(key = "distance")
	public final BiInplaceFirst<RandomAccessibleInterval<T>, Distance> transformInplaceDistance = DistanceTransform::transform;

	@OpField(names = "morphology.distanceTransform")
	@Parameter(key = "source", type = ItemIO.BOTH)
	@Parameter(key = "distance")
	@Parameter(key = "executorService")
	@Parameter(key = "numTasks")
	public final Inplace4First<RandomAccessibleInterval<T>, Distance, ExecutorService, Integer> transformInplaceExServiceDistance = (
			source, distance, executorService, numTasks) -> ExceptionUtils
					.execute(() -> DistanceTransform.transform(source, distance, executorService, numTasks));
			
	@OpField(names = "morphology.distanceTransform")
	@Parameter(key = "source")
	@Parameter(key = "distance")
	@Parameter(key = "target", type = ItemIO.BOTH)
	public final BiComputer<RandomAccessibleInterval<T>, Distance, RandomAccessibleInterval<T>> transformComputerDistance = (in1, in2, out) -> DistanceTransform.transform(in1, out, in2);
	
	@OpField(names = "morphology.distanceTransform")
	@Parameter(key = "source")
	@Parameter(key = "distance")
	@Parameter(key = "executorService")
	@Parameter(key = "numTasks")
	@Parameter(key = "target", type = ItemIO.OUTPUT)
	public final Computer4<RandomAccessibleInterval<T>, Distance, ExecutorService, Integer, RandomAccessibleInterval<T>> transformComputerExServiceDistance = (
			source, distance, executorService, numTasks, target) -> ExceptionUtils
				.execute(() -> DistanceTransform.transform(source, target, distance, executorService, numTasks));
}
