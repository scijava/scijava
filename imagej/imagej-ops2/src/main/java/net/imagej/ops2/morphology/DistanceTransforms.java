/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imagej.ops2.morphology;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.distance.Distance;
import net.imglib2.algorithm.morphology.distance.DistanceTransform;
import net.imglib2.algorithm.morphology.distance.DistanceTransform.DISTANCE_TYPE;
import net.imglib2.type.numeric.RealType;

import org.scijava.concurrent.Parallelization;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.engine.util.ExceptionUtils;

public class DistanceTransforms<T extends RealType<T>, U extends RealType<U>> {

	/**
	 * @mutable source
	 * @input distanceType
	 * @input weights
	 * @implNote op names='morphology.distanceTransform'
	 */
	public final Inplaces.Arity3_1<RandomAccessibleInterval<T>, DISTANCE_TYPE, double[]> transformInplace = DistanceTransform::transform;

	/**
	 * @mutable source
	 * @input distanceType
	 * @input executorService
	 * @input numTasks
	 * @input weights
	 * @implNote op names='morphology.distanceTransform'
	 */
	public final Inplaces.Arity4_1<RandomAccessibleInterval<T>, DISTANCE_TYPE, Integer, double[]> transformExServiceInplace = (
			source, distanceType, numTasks, weights) -> ExceptionUtils.execute(
					() -> DistanceTransform.transform(source, distanceType,
							Parallelization.getExecutorService(), numTasks, weights));

	/**
	 * @input source
	 * @input distanceType
	 * @input weights
	 * @container target
	 * @implNote op names='morphology.distanceTransform'
	 */
	public final Computers.Arity3<RandomAccessibleInterval<T>, DISTANCE_TYPE, double[], RandomAccessibleInterval<T>> transformComputer = (
			in1, in2, in3, out) -> DistanceTransform.transform(in1, out, in2, in3);

	/**
	 * @input source
	 * @input distanceType
	 * @input executorService
	 * @input numTasks
	 * @input weights
	 * @container target
	 * @implNote op names='morphology.distanceTransform'
	 */
	public final Computers.Arity4<RandomAccessibleInterval<T>, DISTANCE_TYPE, Integer, double[], RandomAccessibleInterval<U>> transformExServiceComputer = (
			source, distanceType, numTasks, weights,
			target) -> ExceptionUtils.execute(() -> DistanceTransform.transform(source, target, distanceType,
					Parallelization.getExecutorService(), numTasks, weights));

	/**
	 * @mutable source
	 * @input distance
	 * @implNote op names='morphology.distanceTransform'
	 */
	public final Inplaces.Arity2_1<RandomAccessibleInterval<T>, Distance> transformInplaceDistance = DistanceTransform::transform;

	/**
	 * @mutable source
	 * @input distance
	 * @input executorService
	 * @input numTasks
	 * @implNote op names='morphology.distanceTransform'
	 */
	public final Inplaces.Arity3_1<RandomAccessibleInterval<T>, Distance, Integer> transformInplaceExServiceDistance = (
			source, distance, numTasks) -> ExceptionUtils
					.execute(() -> DistanceTransform.transform(source, distance, Parallelization.getExecutorService(), numTasks));

	/**
	 * @mutable source
	 * @input distance
	 * @container target
	 * @implNote op names='morphology.distanceTransform'
	 */
	public final Computers.Arity2<RandomAccessibleInterval<T>, Distance, RandomAccessibleInterval<T>> transformComputerDistance = (in1, in2, out) -> DistanceTransform.transform(in1, out, in2);

	/**
	 * @mutable source
	 * @input distance
	 * @input executorService
	 * @input numTasks
	 * @container target
	 * @implNote op names='morphology.distanceTransform'
	 */
	public final Computers.Arity3<RandomAccessibleInterval<T>, Distance, Integer, RandomAccessibleInterval<T>> transformComputerExServiceDistance = (
			source, distance, numTasks, target) -> ExceptionUtils
				.execute(() -> DistanceTransform.transform(source, target, distance, Parallelization.getExecutorService(), numTasks));
}
