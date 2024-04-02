/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.threshold.apply;

import java.util.Comparator;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

/**
 * Applies the given threshold value to every element along the given
 * {@link Iterable} input.
 *
 * @author Martin Horn (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @implNote op names='threshold.apply'
 */
public class ApplyConstantThreshold<T extends RealType<T>> implements
	Computers.Arity3<Iterable<T>, T, Comparator<T>, Iterable<BitType>>
{

	@OpDependency(name = "threshold.apply")
	Computers.Arity3<T, T, Comparator<? super T>, BitType> applyThreshold;

	@OpDependency(name = "engine.adapt")
	Function<Computers.Arity1<T, BitType>, Computers.Arity1<Iterable<T>, Iterable<BitType>>> lifter;

	// TODO can/should the Comparator be of <? super T> instead of just <T>?
	/**
	 * TODO
	 *
	 * @param input
	 * @param threshold
	 * @param comparator
	 * @param output
	 */
	@Override
	public void compute(final Iterable<T> input1, final T input2,
		final Comparator<T> comparator, final Iterable<BitType> output)
	{
		Computers.Arity1<T, BitType> thresholdComputer = (in, out) -> applyThreshold
			.compute(in, input2, comparator, out);
		Computers.Arity1<Iterable<T>, Iterable<BitType>> liftedThreshold = lifter
			.apply(thresholdComputer);
		liftedThreshold.accept(input1, output);
	}

}

// -- CONVENIENCE OPS -- //

// If people don't want to / don't know how to make a comparator, they can just
// use this Op. The default comparator just returns true if the input is greater
// than the threshold.
/**
 * @implNote op names='threshold.apply'
 */
class ApplyConstantThresholdSimple<T extends RealType<T>> implements
	Computers.Arity2<Iterable<T>, T, Iterable<BitType>>
{

	@OpDependency(name = "threshold.apply")
	Computers.Arity3<Iterable<T>, T, Comparator<T>, Iterable<BitType>> applyThreshold;

	// TODO can/should the Comparator be of <? super T> instead of just <T>?
	/**
	 * TODO
	 *
	 * @param input
	 * @param threshold
	 * @param output
	 */
	@Override
	public void compute(final Iterable<T> input1, final T input2,
		final Iterable<BitType> output)
	{

		applyThreshold.compute(input1, input2, Comparable::compareTo, output);
	}

}
