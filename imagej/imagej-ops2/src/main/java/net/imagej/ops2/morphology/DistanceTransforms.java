package net.imagej.ops2.morphology;

import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.distance.Distance;
import net.imglib2.algorithm.morphology.distance.DistanceTransform;
import net.imglib2.algorithm.morphology.distance.DistanceTransform.DISTANCE_TYPE;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.ExceptionUtils;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class DistanceTransforms<T extends RealType<T>, U extends RealType<U>> {

	@OpField(names = "morphology.distanceTransform", params = "source, distanceType, weights")
	public final Inplaces.Arity3_1<RandomAccessibleInterval<T>, DISTANCE_TYPE, double[]> transformInplace = DistanceTransform::transform;

	@OpField(names = "morphology.distanceTransform", params = "source, distanceType, executorService, numTasks, weights")
	public final Inplaces.Arity5_1<RandomAccessibleInterval<T>, DISTANCE_TYPE, ExecutorService, Integer, double[]> transformExServiceInplace = (
			source, distanceType, executorService, numTasks, weights) -> ExceptionUtils.execute(
					() -> DistanceTransform.transform(source, distanceType, executorService, numTasks, weights));

	@OpField(names = "morphology.distanceTransform", params = "source, distanceType, weights, target")
	public final Computers.Arity3<RandomAccessibleInterval<T>, DISTANCE_TYPE, double[], RandomAccessibleInterval<T>> transformComputer = (
			in1, in2, in3, out) -> DistanceTransform.transform(in1, out, in2, in3);

	@OpField(names = "morphology.distanceTransform", params = "source, distanceType, executorService, numTasks, weights, target")
	public final Computers.Arity5<RandomAccessibleInterval<T>, DISTANCE_TYPE, ExecutorService, Integer, double[], RandomAccessibleInterval<U>> transformExServiceComputer = (
			source, distanceType, executorService, numTasks, weights,
			target) -> ExceptionUtils.execute(() -> DistanceTransform.transform(source, target, distanceType,
					executorService, numTasks, weights));

	@OpField(names = "morphology.distanceTransform", params = "source, distance")
	public final Inplaces.Arity2_1<RandomAccessibleInterval<T>, Distance> transformInplaceDistance = DistanceTransform::transform;

	@OpField(names = "morphology.distanceTransform", params = "source, distance, executorService, numTasks")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<T>, Distance, ExecutorService, Integer> transformInplaceExServiceDistance = (
			source, distance, executorService, numTasks) -> ExceptionUtils
					.execute(() -> DistanceTransform.transform(source, distance, executorService, numTasks));
			
	@OpField(names = "morphology.distanceTransform", params = "source, distance, target")
	public final Computers.Arity2<RandomAccessibleInterval<T>, Distance, RandomAccessibleInterval<T>> transformComputerDistance = (in1, in2, out) -> DistanceTransform.transform(in1, out, in2);
	
	@OpField(names = "morphology.distanceTransform", params = "source, distance, executorService, numTasks, target")
	public final Computers.Arity4<RandomAccessibleInterval<T>, Distance, ExecutorService, Integer, RandomAccessibleInterval<T>> transformComputerExServiceDistance = (
			source, distance, executorService, numTasks, target) -> ExceptionUtils
				.execute(() -> DistanceTransform.transform(source, target, distance, executorService, numTasks));
}
