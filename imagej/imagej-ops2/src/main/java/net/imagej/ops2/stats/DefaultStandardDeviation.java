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

package net.imagej.ops2.stats;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.api.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * {@link Op} to calculate the {@code stats.stdDev} using
 * {@code stats.variance}.
 * 
 * @author Gabriel Selzer
 * @param <I> input type
 * @param <O> output type
 */
@Plugin(type = Op.class, name = "stats.stdDev", priority = Priority.HIGH)
public class DefaultStandardDeviation<I extends RealType<I>, O extends RealType<O>>
	implements Computers.Arity1<RandomAccessibleInterval<I>, O>
{

	@OpDependency(name = "stats.variance")
	private Computers.Arity1<RandomAccessibleInterval<I>, O> varianceComputer;

	@OpDependency(name = "math.sqrt")
	private Computers.Arity1<O, O> sqrtComputer;

	/**
	 * TODO
	 *
	 * @param raiInput
	 * @param stdDev
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input, final O output) {
		O variance = output.createVariable();
		varianceComputer.compute(input, variance);
		sqrtComputer.compute(variance, output);
	}

}
