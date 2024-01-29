/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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
import net.imglib2.algorithm.morphology.Erosion;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;

public class Erosions<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.erode'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity3<Img<R>, List<? extends Shape>, Integer, Img<R>> erodeImgList =
		(in1, in2, in3) -> Erosion.erode(in1, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.erode'
	 */
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> erodeImgSingle =
		Erosion::erode;

	/**
	 * @input source
	 * @input strels
	 * @input minValue
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.erode'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity4<Img<T>, List<? extends Shape>, T, Integer, Img<T>> erodeImgListMinValue =
		(in1, in2, in3, in4) -> Erosion.erode(in1, (List<Shape>) in2, in3, in4);

	/**
	 * @input source
	 * @input strel
	 * @input minValue
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.erode'
	 */
	public final Functions.Arity4<Img<T>, Shape, T, Integer, Img<T>> erodeImgSingleMinValue =
		Erosion::erode;

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.erode'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity3<RandomAccessible<R>, List<? extends Shape>, Integer, IterableInterval<R>> erodeImgListComputer =
		(in1, in2, in3, out) -> Erosion.erode(in1, out, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strels
	 * @input minVal
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.erode'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity4<RandomAccessible<T>, List<? extends Shape>, T, Integer, IterableInterval<T>> erodeImgListMinValComputer =
		(in1, in2, in3, in4, out) -> Erosion.erode(in1, out, (List<Shape>) in2, in3,
			in4);

	/**
	 * @input source
	 * @input strel
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.erode'
	 */
	public final Computers.Arity3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> erodeImgComputer =
		(in1, in2, in3, out) -> Erosion.erode(in1, out, in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input minVal
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.erode'
	 */
	public final Computers.Arity4<RandomAccessible<T>, Shape, T, Integer, IterableInterval<T>> erodeImgMinValComputer =
		(in1, in2, in3, in4, out) -> Erosion.erode(in1, out, in2, in3, in4);

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.erode'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity3<Img<R>, List<? extends Shape>, Integer, Img<R>> erodeFullImgList =
		(in1, in2, in3) -> Erosion.erodeFull(in1, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.erode'
	 */
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> erodeFullImgSingle =
		Erosion::erodeFull;

	/**
	 * @input source
	 * @input strels
	 * @input minVal
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.erode'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity4<Img<T>, List<? extends Shape>, T, Integer, Img<T>> erodeFullImgListMinValue =
		(in1, in2, in3, in4) -> Erosion.erodeFull(in1, (List<Shape>) in2, in3, in4);

	/**
	 * @input source
	 * @input strel
	 * @input minVal
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.erode'
	 */
	public final Functions.Arity4<Img<T>, Shape, T, Integer, Img<T>> erodeFullImgSingleMinValue =
		Erosion::erodeFull;

	/**
	 * @mutable source
	 * @input interval
	 * @input strels
	 * @input numThreads
	 * @implNote op names='morphology.erode'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, List<? extends Shape>, Integer> erodeImgListInPlace =
		(io, in2, in3, in4) -> Erosion.erodeInPlace(io, in2, (List<Shape>) in3,
			in4);

	/**
	 * @mutable source
	 * @input interval
	 * @input strels
	 * @input minVal
	 * @input numThreads
	 * @implNote op names='morphology.erode'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity5_1<RandomAccessibleInterval<T>, Interval, List<? extends Shape>, T, Integer> erodeImgListMinValInplace =
		(io, in2, in3, in4, in5) -> Erosion.erodeInPlace(io, in2, (List<Shape>) in3,
			in4, in5);

	/**
	 * @mutable source
	 * @input interval
	 * @input strel
	 * @input numThreads
	 * @implNote op names='morphology.erode'
	 */
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, Shape, Integer> erodeImgSingleInPlace =
		Erosion::erodeInPlace;

	/**
	 * @mutable source
	 * @input interval
	 * @input strel
	 * @input minVal
	 * @input numThreads
	 * @implNote op names='morphology.erode'
	 */
	public final Inplaces.Arity5_1<RandomAccessibleInterval<T>, Interval, Shape, T, Integer> erodeImgSingleMinValInplace =
		Erosion::erodeInPlace;
}
