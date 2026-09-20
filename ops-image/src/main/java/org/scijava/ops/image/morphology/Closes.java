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
import net.imglib2.algorithm.morphology.Closing;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;

public class Closes<T extends RealType<T> & Comparable<T>, R extends RealType<R>> {

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.close'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity3<Img<R>, List<? extends Shape>, Integer, Img<R>> closeImgList =
		(in1, in2, in3) -> Closing.close(in1, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.close'
	 */
	public final Functions.Arity3<Img<R>, Shape, Integer, Img<R>> closeImgSingle =
		Closing::close;

	/**
	 * @input source
	 * @input strels
	 * @input minValue
	 * @input maxValue
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.close'
	 */
	@SuppressWarnings("unchecked")
	public final Functions.Arity5<Img<T>, List<? extends Shape>, T, T, Integer, Img<T>> closeImgListMinMax =
		(in1, in2, in3, in4, in5) -> Closing.close(in1, (List<Shape>) in2, in3, in4,
			in5);

	/**
	 * @input source
	 * @input strel
	 * @input minValue
	 * @input maxValue
	 * @input numThreads
	 * @output result
	 * @implNote op names='morphology.close'
	 */
	public final Functions.Arity5<Img<T>, Shape, T, T, Integer, Img<T>> closeImgSingleMinMax =
		Closing::close;

	/**
	 * @input source
	 * @input strels
	 * @input numThreads
	 * @container result
	 * @implNote op names='morphology.close'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity3<RandomAccessible<R>, List<? extends Shape>, Integer, IterableInterval<R>> closeImgListComputer =
		(in1, in2, in3, out) -> Closing.close(in1, out, (List<Shape>) in2, in3);

	/**
	 * @input source
	 * @input strels
	 * @input minValue
	 * @input maxValue
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.close'
	 */
	@SuppressWarnings("unchecked")
	public final Computers.Arity5<RandomAccessible<T>, List<? extends Shape>, T, T, Integer, IterableInterval<T>> closeImgListMinMaxComputer =
		(in1, in2, in3, in4, in5, out) -> Closing.close(in1, out, (List<Shape>) in2,
			in3, in4, in5);

	/**
	 * @input source
	 * @input strel
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.close'
	 */
	public final Computers.Arity3<RandomAccessible<R>, Shape, Integer, IterableInterval<R>> closeImgComputer =
		(in1, in2, in3, out) -> Closing.close(in1, out, in2, in3);

	/**
	 * @input source
	 * @input strel
	 * @input minValue
	 * @input maxValue
	 * @input numThreads
	 * @container target
	 * @implNote op names='morphology.close'
	 */
	public final Computers.Arity5<RandomAccessible<T>, Shape, T, T, Integer, IterableInterval<T>> closeImgMinMaxComputer =
		(in1, in2, in3, in4, in5, out) -> Closing.close(in1, out, in2, in3, in4,
			in5);

	/**
	 * @mutable source
	 * @input interval
	 * @input strels
	 * @input numThreads
	 * @implNote op names='morphology.close'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, List<? extends Shape>, Integer> closeImgListInPlace =
		(io, in2, in3, in4) -> Closing.closeInPlace(io, in2, (List<Shape>) in3,
			in4);

	/**
	 * @mutable source
	 * @input interval
	 * @input strels
	 * @input minValue
	 * @input maxValue
	 * @input numThreads
	 * @implNote op names='morphology.close'
	 */
	@SuppressWarnings("unchecked")
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, List<? extends Shape>, T, T, Integer> closeImgListMinMaxInplace =
		(io, in2, in3, in4, in5, in6) -> Closing.closeInPlace(io, in2,
			(List<Shape>) in3, in4, in5, in6);

	/**
	 * @mutable source
	 * @input interval
	 * @input strel
	 * @input numThreads
	 * @implNote op names='morphology.close'
	 */
	public final Inplaces.Arity4_1<RandomAccessibleInterval<R>, Interval, Shape, Integer> closeImgSingleInPlace =
		Closing::closeInPlace;

	/**
	 * @mutable source
	 * @input interval
	 * @input strel
	 * @input minValue
	 * @input maxValue
	 * @input numThreads
	 * @implNote op names='morphology.close'
	 */
	public final Inplaces.Arity6_1<RandomAccessibleInterval<T>, Interval, Shape, T, T, Integer> closeImgSingleMinMaxInplace =
		Closing::closeInPlace;
}
