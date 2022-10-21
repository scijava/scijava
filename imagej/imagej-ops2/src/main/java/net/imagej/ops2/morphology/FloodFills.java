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

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fill.FloodFill;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

import org.scijava.function.Computers;

public class FloodFills<T extends Type<T>, U extends Type<U>> {

	/**
	 * @input source
	 * @input seed
	 * @input fillLabel
	 * @input shape
	 * @container target
	 * @implNote op names='morphology.floodFill'
	 */
	public final Computers.Arity4<RandomAccessible<T>, Localizable, U, Shape, RandomAccessible<U>> fill = (source, seed,
			fillLabel, shape, target) -> FloodFill.fill(source, target, seed, fillLabel, shape);

	/**
	 * @input source
	 * @input seed
	 * @input fillLabel
	 * @input shape
	 * @input filter
	 * @container target
	 * @implNote op names='morphology.floodFill'
	 */
	public final Computers.Arity5<RandomAccessible<T>, Localizable, U, Shape, BiPredicate<T, U>, RandomAccessible<U>> fillWithPredicate = (
			source, seed, fillLabel, shape, filter,
			target) -> FloodFill.fill(source, target, seed, fillLabel, shape, filter);

	/**
	 * @input source
	 * @input seed
	 * @input shape
	 * @input filter
	 * @input writer
	 * @container target
	 * @implNote op names='morphology.floodFill'
	 */
	public final Computers.Arity5<RandomAccessible<T>, Localizable, Shape, BiPredicate<T, U>, Consumer<U>, RandomAccessible<U>> fillWithPredicateAndConsumer = (
			source, seed, shape, filter, writer, target) -> FloodFill.fill(source, target, seed, shape, filter, writer);

	/**
	 * @input source
	 * @input seed
	 * @input shape
	 * @container target
	 * @implNote op names='morphology.floodFill'
	 */
	public final Computers.Arity3<RandomAccessible<T>, Localizable, Shape, RandomAccessible<T>> fillSimple = (source, seed,
			shape, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		T fillLabel = sourceRA.get().copy();
		FloodFill.fill(source, target, seed, fillLabel, shape);
	};

	/**
	 * @input source
	 * @input seed
	 * @input fillLabel
	 * @input shape
	 * @container target
	 * @implNote op names='morphology.floodFill', priority='100.'
	 */
	public final Computers.Arity4<RandomAccessibleInterval<T>, Localizable, U, Shape, RandomAccessibleInterval<U>> fillRAI = (
			source, seed, fillLabel, shape, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		FloodFill.fill(Views.extendValue(source, sourceRA.get()), Views.extendValue(target, fillLabel), seed, fillLabel,
				shape);
	};

	/**
	 * @input source
	 * @input seed
	 * @input fillLabel
	 * @input shape
	 * @input filter
	 * @container target
	 * @implNote op names='morphology.floodFill', priority='100.'
	 */
	public final Computers.Arity5<RandomAccessibleInterval<T>, Localizable, U, Shape, BiPredicate<T, U>, RandomAccessibleInterval<U>> fillWithPredicateRAI = (
			source, seed, fillLabel, shape, filter, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		FloodFill.fill(Views.extendValue(source, sourceRA.get()), Views.extendValue(target, fillLabel), seed, fillLabel, shape, filter);
	};

	/**
	 * @input source
	 * @input seed
	 * @input shape
	 * @container target
	 * @implNote op names='morphology.floodFill', priority='100.'
	 */
	public final Computers.Arity3<RandomAccessibleInterval<T>, Localizable, Shape, RandomAccessibleInterval<T>> fillSimpleRAI = (
			source, seed, shape, target) -> {
		RandomAccess<T> sourceRA = source.randomAccess();
		sourceRA.setPosition(seed);
		T fillLabel = sourceRA.get().copy();
		FloodFill.fill(Views.extendValue(source, fillLabel), Views.extendValue(target, fillLabel), seed, fillLabel, shape);
	};

}
