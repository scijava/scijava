/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.threshold.apply;

import java.util.Comparator;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.util.Adapt;
import org.scijava.ops.util.Maps;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Applies the given threshold value to every element along the given
 * {@link Iterable} input.
 *
 * @author Martin Horn (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 */
@Plugin(type = Op.class, name = "threshold.apply")
@Parameter(key = "input")
@Parameter(key = "threshold")
@Parameter(key = "comparator")
@Parameter(key = "output")
public class ApplyConstantThreshold<T extends RealType<T>>
		implements Computers.Arity3<Iterable<T>, T, Comparator<T>, Iterable<BitType>> {

	@OpDependency(name = "threshold.apply")
	Computers.Arity3<T, T, Comparator<? super T>, BitType> applyThreshold;

	// TODO can/should the Comparator be of <? super T> instead of just <T>?
	@Override
	public void compute(final Iterable<T> input1, final T input2, final Comparator<T> comparator,
			final Iterable<BitType> output) {
		Computers.Arity1<T, BitType> thresholdComputer = Adapt.ComputerAdapt.asComputer(applyThreshold, input2, comparator);
		Computers.Arity1<Iterable<T>, Iterable<BitType>> liftedThreshold = Maps.ComputerMaps.Iterables.liftBoth(thresholdComputer);
		liftedThreshold.accept(input1, output);
	}

}

// -- CONVENIENCE OPS -- //

// If people don't want to / don't know how to make a comparator, they can just
// use this Op. The default comparator just returns true if the input is greater
// than the threshold.
@Plugin(type = Op.class, name = "threshold.apply")
@Parameter(key = "input")
@Parameter(key = "threshold")
@Parameter(key = "output")
class ApplyConstantThresholdSimple<T extends RealType<T>> implements Computers.Arity2<Iterable<T>, T, Iterable<BitType>> {

	@OpDependency(name = "threshold.apply")
	Computers.Arity3<Iterable<T>, T, Comparator<T>, Iterable<BitType>> applyThreshold;

	// TODO can/should the Comparator be of <? super T> instead of just <T>?
	@Override
	public void compute(final Iterable<T> input1, final T input2, final Iterable<BitType> output) {

		applyThreshold.compute(input1, input2, (in1, in2) -> in1.compareTo(in2), output);
	}

}
