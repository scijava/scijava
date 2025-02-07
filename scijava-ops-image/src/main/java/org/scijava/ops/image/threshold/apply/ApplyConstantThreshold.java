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
import org.scijava.ops.spi.Nullable;
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
public class ApplyConstantThreshold < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> implements Computers.Arity3<I, T, Comparator<? super T>, J>
{

	@OpDependency(name = "threshold.apply")
	Computers.Arity3<T, T, Comparator<? super T>, BitType> applyThreshold;

	// NB we lift manually in this Op because it's much more likely to find optimized
	// lifters for Computers.Arity1<I, J> than it is to find optimized lifters
	// that can handle the threshold and value as parameters.
	@OpDependency(name = "engine.adapt")
	Function<Computers.Arity1<T, BitType>, Computers.Arity1<I, J>> lifter;

	private final Comparator<T> DEFAULT = Comparable::compareTo;

	/**
	 * Thresholds each input value against the threshold, optionally using a
	 * custom comparator.
	 *
	 * @param input the input data
	 * @param threshold the threshold value
	 * @param comparator defines whether the input is above or below the
	 *           threshold. If not provided, {@link Comparable#compareTo}
	 *           will be used.
	 * @param output a preallocated output buffer
	 */
	@Override
	public void compute( //
		 final I input, //
		 final T threshold, //
		 final @Nullable Comparator<? super T> comparator, //
		 final J output //
	) {
		final Comparator<? super T> comp = comparator != null ? comparator : DEFAULT;
		Computers.Arity1<T, BitType> thresholdComputer = //
			 (in, out) -> applyThreshold.compute(in, threshold, comp, out);
		lifter.apply(thresholdComputer).compute(input, output);
	}

}
