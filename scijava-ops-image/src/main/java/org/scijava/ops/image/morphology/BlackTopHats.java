/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.morphology;

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

/**
 * Wrapper Ops for imglib2-algorithm's Black Top Hats algorithms TODO: Revert to
 * the nice lambda syntax with all of the List ops once imglib2-algorithm
 * reaches a new version.
 *
 * @author Gabriel Selzer
 * @param <T>
 * @param <R>
 */
public class BlackTopHats<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	/**
	 * Iteratively performs Black Top Hats with each passed {@link Shape} in
	 * order.
	 *
	 * @input img input data
	 * @input shapes a {@code List<Shape>} containing the {@link Shape}s to use
	 *        for each Black Top Hat neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @output img output data
	 * @implNote op names='morphology.BlackTopHat'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity3<Img<R>, List<? extends Shape>, Integer, Img<R>> BlackTopHatImgList =
		BlackTopHat::blackTopHat;

	/**
	 * Performs a Black Top Hat.
	 *
	 * @input img input data
	 * @input shape a {@link Shape} to use for the neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @output img output data
	 * @implNote op names='morphology.BlackTopHat'
	 */
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> BlackTopHatImgSingle =
		BlackTopHat::blackTopHat;

	/**
	 * Iteratively performs Black Top Hats with each passed {@link Shape} in
	 * order.
	 *
	 * @input img input data
	 * @input shapes a {@code List<Shape>} containing the {@link Shape}s to use
	 *        for each Black Top Hat neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @input minValue a {@link T} that is smaller than all values in the input
	 *        image. Used to speed computation.
	 * @input maxValue a {@link T} that is larger than all values in the input
	 *        image. Used to speed computation.
	 * @output img output data
	 * @implNote op names='morphology.BlackTopHat'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity5<Img<T>, List<? extends Shape>, T, T, Integer, Img<T>> BlackTopHatImgListMinMax =
		BlackTopHat::blackTopHat;

	/**
	 * Performs a Black Top Hat.
	 *
	 * @input img input data
	 * @input shape a {@link Shape} to use for the neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @input minValue a {@link T} that is smaller than all values in the input
	 *        image. Used to speed computation.
	 * @input maxValue a {@link T} that is larger than all values in the input
	 *        image. Used to speed computation.
	 * @output img output data
	 * @implNote op names='morphology.BlackTopHat'
	 */
	public final Functions.Arity5<Img<T>, Shape, T, T, Integer, Img<T>> BlackTopHatImgSingleMinMax =
		BlackTopHat::blackTopHat;

	/**
	 * Iteratively performs Black Top Hats with each passed {@link Shape} in
	 * order, placing the result into the provided preallocated output buffer.
	 *
	 * @input img input data
	 * @input shapes a {@code List<Shape>} containing the {@link Shape}s to use
	 *        for each Black Top Hat neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @container img output data
	 * @implNote op names='morphology.BlackTopHat'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity3<RandomAccessible<R>, List<? extends Shape>, Integer, IterableInterval<R>> BlackTopHatImgListComputer =
		(in1, in2, in3, out) -> BlackTopHat.blackTopHat(in1, out, in2, in3);

	/**
	 * Iteratively performs Black Top Hats with each passed {@link Shape} in
	 * order, placing the result into the provided preallocated output buffer.
	 *
	 * @input img input data
	 * @input shapes a {@code List<Shape>} containing the {@link Shape}s to use
	 *        for each Black Top Hat neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @input minValue a {@link T} that is smaller than all values in the input
	 *        image. Used to speed computation.
	 * @input maxValue a {@link T} that is larger than all values in the input
	 *        image. Used to speed computation.
	 * @container img output data
	 * @implNote op names='morphology.BlackTopHat'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity5<RandomAccessible<T>, List<? extends Shape>, T, T, Integer, IterableInterval<T>> BlackTopHatImgListMinMaxComputer =
		(in1, in2, in3, in4, in5, out) -> BlackTopHat.blackTopHat(in1, out, in2,
			in3, in4, in5);

	/**
	 * Performs a Black Top Hat, placing the result into the provided preallocated
	 * output buffer.
	 *
	 * @input img input data
	 * @input shape a {@link Shape} to use for the neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @container img output data
	 * @implNote op names='morphology.BlackTopHat'
	 */
	public final Computers.Arity3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> BlackTopHatImgComputer =
		(in1, in2, in3, out) -> BlackTopHat.blackTopHat(in1, out, in2, in3);

	/**
	 * Performs a Black Top Hat, placing the result into the provided preallocated
	 * output buffer.
	 *
	 * @input img input data
	 * @input shape a {@link Shape} to use for the neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @input minValue a {@link T} that is smaller than all values in the input
	 *        image. Used to speed computation.
	 * @input maxValue a {@link T} that is larger than all values in the input
	 *        image. Used to speed computation.
	 * @container img output data
	 * @implNote op names='morphology.BlackTopHat'
	 */
	public final Computers.Arity5<RandomAccessible<T>, Shape, T, T, Integer, IterableInterval<T>> BlackTopHatImgMinMaxComputer =
		(in1, in2, in3, in4, in5, out) -> BlackTopHat.blackTopHat(in1, out, in2,
			in3, in4, in5);

	/**
	 * Iteratively performs Black Top Hats <em>within the provided
	 * {@link Interval}</em> with each passed {@link Shape} in order, overwriting
	 * the provided input buffer with the results
	 *
	 * @mutable img input data
	 * @input interval the {@link Interval} restricting the bounds of computation.
	 * @input shapes a {@code List<Shape>} containing the {@link Shape}s to use
	 *        for each Black Top Hat neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @implNote op names='morphology.BlackTopHat'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, List<? extends Shape>, Integer> BlackTopHatImgListInPlace =
		(io, in2, in3, in4) -> BlackTopHat.blackTopHatInPlace(io, in2,
			(List<Shape>) in3, in4);

	/**
	 * Iteratively performs Black Top Hats <em>within the provided
	 * {@link Interval}</em> with each passed {@link Shape} in order, overwriting
	 * the provided input buffer with the results
	 *
	 * @mutable img input data
	 * @input interval the {@link Interval} restricting the bounds of computation.
	 * @input shapes a {@code List<Shape>} containing the {@link Shape}s to use
	 *        for each Black Top Hat neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @input minValue a {@link T} that is smaller than all values in the input
	 *        image. Used to speed computation.
	 * @input maxValue a {@link T} that is larger than all values in the input
	 *        image. Used to speed computation.
	 * @implNote op names='morphology.BlackTopHat'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, List<? extends Shape>, T, T, Integer> BlackTopHatImgListMinMaxInplace =
		(io, in2, in3, in4, in5, in6) -> BlackTopHat.blackTopHatInPlace(io, in2,
			(List<Shape>) in3, in4, in5, in6);

	/**
	 * Performs a Black Top Hat <em>within the provided {@link Interval}</em>,
	 * overwriting the provided input buffer with the results
	 *
	 * @mutable img input data
	 * @input interval the {@link Interval} restricting the bounds of computation.
	 * @input shape a {@link Shape} to use for the neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @implNote op names='morphology.BlackTopHat'
	 */
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, Shape, Integer> BlackTopHatImgSingleInPlace =
		BlackTopHat::blackTopHatInPlace;

	/**
	 * Performs a Black Top Hat <em>within the provided {@link Interval}</em>,
	 * overwriting the provided input buffer with the results
	 *
	 * @mutable img input data
	 * @input interval the {@link Interval} restricting the bounds of computation.
	 * @input shape a {@link Shape} to use for the neighborhood
	 * @input numThreads the number of threads to use in the execution
	 * @input minValue a {@link T} that is smaller than all values in the input
	 *        image. Used to speed computation.
	 * @input maxValue a {@link T} that is larger than all values in the input
	 *        image. Used to speed computation.
	 * @implNote op names='morphology.BlackTopHat'
	 */
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, Shape, T, T, Integer> BlackTopHatImgSingleMinMaxInplace =
		BlackTopHat::blackTopHatInPlace;
}
